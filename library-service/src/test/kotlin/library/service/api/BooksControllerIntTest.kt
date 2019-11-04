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
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import utils.MutableClock
import utils.ResetMocksAfterEachTest
import utils.classification.IntegrationTest
import java.time.OffsetDateTime
import java.util.*
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
    fun bookDataStore(): BookDataStore = mockk()
    @MockBean(BookIdGenerator::class)
    fun bookIdGenerator(): BookIdGenerator = mockk()
    @MockBean(EventDispatcher::class)
    fun eventDispatcher(): EventDispatcher<BookEvent> = mockk()

    val correlationId = UUID.randomUUID().toString()

    @BeforeEach fun setTime() {
        clock.setFixedTime("2017-08-20T12:34:56.789Z")
    }

    @BeforeEach fun initMocks() {
        every { bookDataStore.findById(any()) } returns null
        every { bookDataStore.createOrUpdate(any()) } answers { firstArg() }
    }

    @Test fun `when there are no books, the response only contains a self link`() {
        every { bookDataStore.findAll() } returns emptyList()

        RestAssured.`when`().get("/api/books").then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("_links.self.href", Matchers.equalTo("http://localhost/books"))
    }

    @MicronautTest
    @DisplayName("/api/books")
    @Nested inner class BooksEndpoint {

        @MicronautTest
        @DisplayName("GET")
        @Nested inner class GetMethod {

            @Test fun `when there are no books, the response only contains a self link`() {
                every { bookDataStore.findAll() } returns emptyList()

                RestAssured.`when`().get("/api/books").then()
                        .statusCode(200)
                        .contentType(MediaType.APPLICATION_HAL_JSON)
                        .body("_links.self.href", Matchers.equalTo("http://localhost/api/books"))
            }

            /*@Test fun `when there are books, the response contains them with all relevant links`() {
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

                val request = get("/api/books")
                val expectedResponse = """
                    {
                      "_embedded": {
                        "books": [
                          {
                            "isbn": "${Books.CLEAN_CODE.isbn}",
                            "title": "${Books.CLEAN_CODE.title}",
                            "authors": ${Books.CLEAN_CODE.authors.toJson()},
                            "numberOfPages": ${Books.CLEAN_CODE.numberOfPages},
                            "_links": {
                              "self": {
                                "href": "http://localhost/api/books/883a2931-325b-4482-8972-8cb6f7d33816"
                              },
                              "delete": {
                                "href": "http://localhost/api/books/883a2931-325b-4482-8972-8cb6f7d33816"
                              },
                              "borrow": {
                                "href": "http://localhost/api/books/883a2931-325b-4482-8972-8cb6f7d33816/borrow"
                              }
                            }
                          },
                          {
                            "isbn": "${Books.CLEAN_CODER.isbn}",
                            "title": "${Books.CLEAN_CODER.title}",
                            "authors": ${Books.CLEAN_CODER.authors.toJson()},
                            "numberOfPages": ${Books.CLEAN_CODER.numberOfPages},
                            "borrowed": {
                              "by": "Uncle Bob",
                              "on": "2017-08-20T12:34:56.789Z"
                            },
                            "_links": {
                              "self": {
                                "href": "http://localhost/api/books/53397dc0-932d-4198-801a-3e00b2742ba7"
                              },
                              "delete": {
                                "href": "http://localhost/api/books/53397dc0-932d-4198-801a-3e00b2742ba7"
                              },
                              "return": {
                                "href": "http://localhost/api/books/53397dc0-932d-4198-801a-3e00b2742ba7/return"
                              }
                            }
                          }
                        ]
                      },
                      "_links": {
                        "self": {
                          "href": "http://localhost/api/books"
                        }
                      }
                    }
                """
                mockMvc.perform(request)
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_HAL_JSON))
                        .andExpect(content().json(expectedResponse, true))
            }*/

        }

        @DisplayName("POST")
        @Nested inner class PostMethod {
            // !TODO
        }

        @DisplayName("/api/books/{id}")
        @Nested inner class BookByIdEndpoint
         // !TODO

    }

    private fun availableBook(id: BookId, book: Book) = BookRecord(id, book)
    private fun borrowedBook(id: BookId, book: Book, borrowedBy: String, borrowedOn: String) = availableBook(id, book)
            .borrow(Borrower(borrowedBy), OffsetDateTime.parse(borrowedOn))

    private fun List<Author>.toJson() = joinToString(separator = "\", \"", prefix = "[\"", postfix = "\"]")

}
