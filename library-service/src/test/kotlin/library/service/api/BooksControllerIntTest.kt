package library.service.api

import io.micronaut.context.annotation.Property
import io.micronaut.http.MediaType
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.every
import io.mockk.mockk
import io.restassured.RestAssured
import library.service.api.books.BooksController
import library.service.business.books.BookDataStore
import library.service.business.books.BookIdGenerator
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.events.BookEvent
import library.service.business.books.domain.types.Author
import library.service.business.books.domain.types.BookId
import library.service.business.books.domain.types.Borrower
import library.service.business.events.EventDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.Books
import utils.JsonMatcher
import utils.MutableClock
import utils.ResetMocksAfterEachTest
import utils.classification.IntegrationTest
import java.time.OffsetDateTime
import javax.inject.Inject

@MicronautTest
@Property(name="micronaut.server.port", value="8080")
@IntegrationTest
@ResetMocksAfterEachTest
internal class BooksControllerIntTest {


    @Inject lateinit var bookDataStore: BookDataStore
    @Inject lateinit var bookIdGenerator: BookIdGenerator
    @Inject lateinit var booksController: BooksController
    @Inject lateinit var clock: MutableClock

    @MockBean(BookDataStore::class)
    fun bookDataStore(): BookDataStore = mockk(relaxed = true)
    @MockBean(BookIdGenerator::class)
    fun bookIdGenerator(): BookIdGenerator = mockk()
    @MockBean(EventDispatcher::class)
    fun eventDispatcher(): EventDispatcher<BookEvent> = mockk()

    @BeforeEach fun setTime() {
        clock.setFixedTime("2017-08-20T12:34:56.789Z")
    }

    @BeforeEach fun initMocks() {
        every { bookDataStore.findById(any()) } returns null
        every { bookDataStore.createOrUpdate(any()) } answers { firstArg() }
    }

    @Test fun `when there are no books, the response is empty`() {
        every { bookDataStore.findAll() } returns emptyList()

        RestAssured.`when`().get("/api/books").then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo("[]"))
    }

    @Test fun `when there are books, the response contains them`() {
        val availableBook = availableBook(
                id = BookId.from("883a2931-325b-4482-8972-8cb6f7d33816"),
                book = Books.CLEAN_CODE
        )
        val borrowedBook = borrowedBook(
                id = BookId.from("53397dc0-932d-4198-801a-3e00b2742ba7"),
                book = Books.CLEAN_CODER,
                borrowedBy = "Uncle Bob",
                borrowedOn = "2017-08-20T12:34:56.789Z"
        )
        every { bookDataStore.findAll() } returns listOf(availableBook, borrowedBook)

        val expectedResponse = """
                    [
                        {
                            "isbn":"9780132350884",
                            "title":"Clean Code: A Handbook of Agile Software Craftsmanship",
                            "authors":[
                                "Robert C. Martin",
                                "Dean Wampler"
                            ],
                            "numberOfPages":462
                        },
                        {
                            "isbn":"9780137081073",
                            "title":"Clean Coder: A Code of Conduct for Professional Programmers",
                            "authors":[
                                "Robert C. Martin"
                            ],
                            "numberOfPages":256,
                            "borrowed":{
                                "by":"Uncle Bob",
                                "on":"2017-08-20T12:34:56.789Z"
                            }
                        }
                    ]
                """

        RestAssured.`when`().get("/api/books").then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `new test`() {
        every { bookDataStore.findAll() } returns emptyList()

        RestAssured.`when`().get("/api/books").then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo("[]"))
    }

    private fun availableBook(id: BookId, book: Book) = BookRecord(id, book)
    private fun borrowedBook(id: BookId, book: Book, borrowedBy: String, borrowedOn: String) = availableBook(id, book)
            .borrow(Borrower(borrowedBy), OffsetDateTime.parse(borrowedOn))

    private fun List<Author>.toJson() = joinToString(separator = "\", \"", prefix = "[\"", postfix = "\"]")

}
