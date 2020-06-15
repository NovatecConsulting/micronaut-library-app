package library.service.security

import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.every
import io.mockk.mockk
import io.restassured.RestAssured
import library.service.business.books.BookDataStore
import library.service.business.books.BookIdGenerator
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.types.BookId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.Books
import utils.MutableClock
import javax.inject.Inject

private val bookDataStore: BookDataStore = mockk()
private val bookIdGenerator: BookIdGenerator = mockk()

@MicronautTest(environments = ["test-secure"])
class BasicAuthTest {

    @MockBean(BookDataStore::class)
    fun bookDataStore(): BookDataStore = bookDataStore
    @MockBean(BookIdGenerator::class)
    fun bookIdGenerator(): BookIdGenerator = bookIdGenerator

    @Inject
    lateinit var server: EmbeddedServer

    @Inject lateinit var clock: MutableClock


    @BeforeEach
    fun setTime() {
        RestAssured.port = server.url.port
        clock.setFixedTime("2017-08-20T12:34:56.789Z")
    }

    @BeforeEach
    fun initMocks() {
        every { bookDataStore.findById(any()) } returns null
        every { bookDataStore.createOrUpdate(any()) } answers { firstArg() }
        every { bookDataStore.existsById(any()) } returns false
    }

    @Test fun `user is authorized`() {
        every { bookDataStore.findAll() } returns emptyList()

        RestAssured.given().auth().preemptive().basic("user","user")
                .`when`().get("/api/books").then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
    }

    @Test
    fun `user is not authorized`() {
        every { bookDataStore.findAll() } returns emptyList()

        RestAssured.given().auth().preemptive().basic("no valid","user")
                .`when`().get("/api/books").then()
                .statusCode(HttpStatus.UNAUTHORIZED.code)
    }

    @Test fun `curator can delete book`() {
        val id = BookId.generate()
        val book = Books.CLEAN_CODE
        val availableBookRecord = availableBook(id, book)
        every { bookDataStore.findById(id) } returns availableBookRecord
        every { bookDataStore.delete(availableBookRecord) } returns Unit

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).auth().preemptive().basic("curator","curator")
                .`when`().delete("/api/books/$id").then()
                .statusCode(HttpStatus.NO_CONTENT.code)
    }

    @Test fun `user can not delete book`() {
        val id = BookId.generate()
        val book = Books.CLEAN_CODE
        val availableBookRecord = availableBook(id, book)
        every { bookDataStore.findById(id) } returns availableBookRecord
        every { bookDataStore.delete(availableBookRecord) } returns Unit

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).auth().preemptive().basic("user","user")
                .`when`().delete("/api/books/$id").then()
                .statusCode(HttpStatus.FORBIDDEN.code)
    }

    private fun availableBook(id: BookId, book: Book) = BookRecord(id, book)
}
