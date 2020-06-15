package library.service.api.books

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


private val bookDataStore: BookDataStore = mockk()
private val bookIdGenerator: BookIdGenerator = mockk()
private val eventDispatcher: EventDispatcher<BookEvent> = mockk(relaxed=true)

@MicronautTest
@IntegrationTest
@ResetMocksAfterEachTest
class BooksControllerIntTest {
    @MockBean(BookDataStore::class)
    fun bookDataStore(): BookDataStore = bookDataStore
    @MockBean(BookIdGenerator::class)
    fun bookIdGenerator(): BookIdGenerator = bookIdGenerator
    @MockBean(EventDispatcher::class)
    fun eventDispatcher(): EventDispatcher<BookEvent> = eventDispatcher

    @Inject lateinit var server: EmbeddedServer
    @Inject lateinit var clock: MutableClock

    @BeforeEach fun setTime() {
        clock.setFixedTime("2017-08-20T12:34:56.789Z")
        RestAssured.port = server.url.port
    }

    @BeforeEach fun initMocks() {
        every { bookDataStore.findById(any()) } returns null
        every { bookDataStore.createOrUpdate(any()) } answers { firstArg() }
        every { bookDataStore.existsById(any()) } returns false
    }

    // BooksEndpoint
    @Test fun `when there are no books, the response is empty`() {
        every { bookDataStore.findAll() } returns emptyList()

        RestAssured.`when`()
                .get("/api/books")
                .then()
                .body(JsonMatcher.jsonEqualTo("[]"))
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .statusCode(HttpStatus.OK.code)
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

        RestAssured.given().`when`().get("/api/books").then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    // PostMethod
    @Test fun `creates a book and responds with its resource representation`() {
        val bookId = BookId.generate()
        every { bookIdGenerator.generate() } returns bookId

        val requestBody = """
                    {
                      "isbn": "9780132350884",
                      "title": "Clean Code: A Handbook of Agile Software Craftsmanship"
                    }
                """

        val expectedResponse = """{
                      "isbn": "9780132350884",
                      "title": "Clean Code: A Handbook of Agile Software Craftsmanship",
                      "authors": []                      
                    }
                """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(requestBody)
                .`when`().post("/api/books").then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for invalid ISBN`() {
        val requestBody = """
                    {
                      "isbn": "abcdefghij",
                      "title": "Clean Code: A Handbook of Agile Software Craftsmanship"
                    }
                """
        val expectedResponse = """{"status": 400,
                      "error": "Bad Request",
                      "timestamp": "2017-08-20T12:34:56.789Z",
                      "message": "The request's body is invalid. See details...",
                      "details": ["The field 'isbn' must match \"(\\d{3}-?)?\\d{10}\"."]
                    }
                """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(requestBody)
                .`when`().post("/api/books").then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for missing required properties`() {
        val requestBody = """ { } """
        val expectedResponse = """
                    {
                      "status": 400,
                      "error": "Bad Request",
                      "timestamp": "2017-08-20T12:34:56.789Z",
                      "message": "The request's body is invalid. See details...",
                      "details": [
                        "The field 'isbn' must not be blank.",
                        "The field 'title' must not be blank."
                      ]
                    }
                """
        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(requestBody)
                .`when`().post("/api/books").then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `415 UNSUPPORTED MEDIA for missing content type in header request`() {
        val requestBody = """ """
        val expectedResponse = """
                    {
                      "message":"Content Type [text/plain;charset=ISO-8859-1] not allowed. Allowed types: [application/json]","_links":{"self":{"href":"/api/books","templated":false}}}
                    }
                """
        RestAssured.given().body(requestBody)
                .`when`().post("/api/books").then()
                .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.code)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    // BookByIdEndpoint
    private val id = BookId.generate()
    val book = Books.CLEAN_CODE
    private val availableBookRecord = availableBook(id, book)
    private val borrowedBookRecord = borrowedBook(id, book, "Uncle Bob", "2017-08-20T12:34:56.789Z")

    @Test fun `responds with book's resource representation for existing available book`() {
        every { bookDataStore.findById(id) } returns availableBookRecord

        val request = "/api/books/$id"
        val expectedResponse = """
                        {
                          "isbn": "${Books.CLEAN_CODE.isbn}",
                          "title": "${Books.CLEAN_CODE.title}",
                          "authors": ${Books.CLEAN_CODE.authors.toJson()},
                          "numberOfPages": ${Books.CLEAN_CODE.numberOfPages}
                          }
                        }
                    """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().get(request).then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `responds with book's resource representation for existing borrowed book`() {
        every { bookDataStore.findById(id) } returns borrowedBookRecord

        val request = "/api/books/$id"
        val expectedResponse = """
                        {
                          "isbn": "${Books.CLEAN_CODE.isbn}",
                          "title": "${Books.CLEAN_CODE.title}",
                          "authors": ${Books.CLEAN_CODE.authors.toJson()},
                          "numberOfPages": ${Books.CLEAN_CODE.numberOfPages},
                          "borrowed": {
                            "by": "Uncle Bob",
                            "on": "2017-08-20T12:34:56.789Z"
                          }
                        }
                    """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().get(request).then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `404 NOT FOUND for non-existing book`() {
        val request = "/api/books/$id"
        val expectedResponse = """
                        {
                          "status": 404,
                          "error": "Not Found",
                          "timestamp": "2017-08-20T12:34:56.789Z",
                          "message": "The book with ID: $id does not exist!"
                        }
                    """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().get(request).then()
                .statusCode(HttpStatus.NOT_FOUND.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for malformed ID`() {
        val request = "/api/books/malformed-id"
        val expectedResponse = """
                        {
                          "status": 400,
                          "error": "Bad Request",
                          "timestamp": "2017-08-20T12:34:56.789Z",
                          "message": "The request's 'id' parameter is malformed."
                        }
                    """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().get(request).then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `existing book is deleted and response is empty 204 NO CONTENT`() {
        every { bookDataStore.findById(id) } returns availableBookRecord
        every { bookDataStore.delete(availableBookRecord) } returns Unit

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().delete("/api/books/$id").then()
                .statusCode(HttpStatus.NO_CONTENT.code)
                .contentType("")
    }

    @Test fun `404 NOT FOUND for non-existing book to delete`() {
        val request = "/api/books/$id"
        val expectedResponse = """
                        {
                          "status": 404,
                          "error": "Not Found",
                          "timestamp": "2017-08-20T12:34:56.789Z",
                          "message": "The book with ID: $id does not exist!"
                        }
                    """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().delete(request).then()
                .statusCode(HttpStatus.NOT_FOUND.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for book with malformed ID to delete`() {
        val request = "/api/books/malformed-id"
        val expectedResponse = """
                        {
                          "status": 400,
                          "error": "Bad Request",
                          "timestamp": "2017-08-20T12:34:56.789Z",
                          "message": "The request's 'id' parameter is malformed."
                        }
                    """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().delete(request).then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    // BookByIdAuthorsEndpoint
    @Test fun `replaces authors of book and responds with its resource representation`() {
        every { bookDataStore.findById(id) } returns availableBookRecord

        val request = "/api/books/$id/authors"
        val expectedResponse = """
                            {
                              "isbn": "${book.isbn}",
                              "title": "${book.title}",
                              "authors": ["Foo", "Bar"],
                              "numberOfPages": ${book.numberOfPages}
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { "authors": ["Foo", "Bar"] } """)
                .`when`().put(request).then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `404 NOT FOUND for non-existing book where authors should be replaced`() {
        val request = "/api/books/$id/authors"
        val expectedResponse = """
                        {
                          "status": 404,
                          "error": "Not Found",
                          "timestamp": "2017-08-20T12:34:56.789Z",
                          "message": "The book with ID: $id does not exist!"
                        }
                    """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { "authors": ["Foo", "Bar"] } """)
                .`when`().put(request).then()
                .statusCode(HttpStatus.NOT_FOUND.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for missing required properties of book where authors should be replaced`() {
        val request = "/api/books/$id/authors"
        val expectedResponse = """
                    {
                      "status": 400,
                      "error": "Bad Request",
                      "timestamp": "2017-08-20T12:34:56.789Z",
                      "message": "The request's body is invalid. See details...",
                      "details": [ "The field 'authors' must not be empty." ]
                    }
                """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { } """)
                .`when`().put(request).then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `removes authors from book and responds with its resource representation`() {
        every { bookDataStore.findById(id) } returns availableBookRecord

        val request = "/api/books/$id/authors"
        val expectedResponse = """
                            {
                              "isbn": "${book.isbn}",
                              "title": "${book.title}",
                              "authors": [],
                              "numberOfPages": ${book.numberOfPages}
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().delete(request).then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `404 NOT FOUND for non-existing book where authors should be deleted`() {
        val request = "/api/books/$id/authors"
        val expectedResponse = """
                            {
                              "status": 404,
                              "error": "Not Found",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The book with ID: $id does not exist!"
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().delete(request).then()
                .statusCode(HttpStatus.NOT_FOUND.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    // BookByIdBorrowEndpoint
    @Test fun `borrows book and responds with its updated resource representation`() {
        every { bookDataStore.findById(id) } returns availableBookRecord

        val request = "/api/books/$id/borrow"
        val expectedResponse = """
                            {
                              "isbn": "${Books.CLEAN_CODE.isbn}",
                              "title": "${Books.CLEAN_CODE.title}",
                              "authors": ${Books.CLEAN_CODE.authors.toJson()},
                              "numberOfPages": ${Books.CLEAN_CODE.numberOfPages},
                              "borrowed": {
                                "by": "Uncle Bob",
                                "on": "2017-08-20T12:34:56.789Z"
                              }
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { "borrower": "Uncle Bob" } """)
                .`when`().post(request).then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `409 CONFLICT for already borrowed book`() {
        every { bookDataStore.findById(id) } returns borrowedBookRecord

        val request = "/api/books/$id/borrow"
        val expectedResponse = """
                            {
                              "status": 409,
                              "error": "Conflict",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The book with ID: $id is already borrowed!"
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { "borrower": "Uncle Bob" } """)
                .`when`().post(request).then()
                .statusCode(HttpStatus.CONFLICT.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `404 NOT FOUND for non-existing book to be borrowed`() {
        val request = "/api/books/$id/borrow"
        val expectedResponse = """
                            {
                              "status": 404,
                              "error": "Not Found",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The book with ID: $id does not exist!"
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { "borrower": "Uncle Bob" } """)
                .`when`().post(request).then()
                .statusCode(HttpStatus.NOT_FOUND.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for missing required properties of book to be borrowed`() {
        val request = "/api/books/$id/borrow"
        val expectedResponse = """
                            {
                              "status": 400,
                              "error": "Bad Request",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The request's body is invalid. See details...",
                              "details": [ "The field 'borrower' must not be null." ]
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(" { } ")
                .`when`().post(request).then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for malformed request of book to be borrowed`() {
        val request = "/api/books/$id/borrow"
        val expectedResponse = """
                            {
                              "status": 400,
                              "error": "Bad Request",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The request's body could not be read. It is either empty or malformed."
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body("")
                .`when`().post(request).then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for malformed ID of book to be borrowed`() {
        val request = "/api/books/malformed-id/borrow"
        val expectedResponse = """
                            {
                              "status": 400,
                              "error": "Bad Request",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The request's 'id' parameter is malformed."
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body("")
                .`when`().post(request).then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    // BookByIdNumberOfPagesEndpoint
    @Test fun `replaces number of pages of book and responds with its resource representation`() {
        every { bookDataStore.findById(id) } returns availableBookRecord

        val request = "/api/books/$id/numberOfPages"
        val expectedResponse = """
                            {
                              "isbn": "${book.isbn}",
                              "title": "${book.title}",
                              "authors": ${book.authors.toJson()},
                              "numberOfPages": 128
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { "numberOfPages": 128 } """)
                .`when`().put(request).then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `404 NOT FOUND for non-existing book where number of pages should be replaced`() {
        val request = "/api/books/$id/numberOfPages"
        val expectedResponse = """
                            {
                              "status": 404,
                              "error": "Not Found",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The book with ID: $id does not exist!"
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { "numberOfPages": 128 } """)
                .`when`().put(request).then()
                .statusCode(HttpStatus.NOT_FOUND.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for missing required properties for book where number of pages should be replaced`() {
        val idValue = BookId.generate().toString()
        val request = "/api/books/$idValue/numberOfPages"
        val expectedResponse = """
                            {
                              "status": 400,
                              "error": "Bad Request",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The request's body is invalid. See details...",
                              "details": [ "The field 'numberOfPages' must not be null." ]
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { } """)
                .`when`().put(request).then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `removes number of pages from book and responds with its resource representation`() {
        every { bookDataStore.findById(id) } returns availableBookRecord

        val request = "/api/books/$id/numberOfPages"
        val expectedResponse = """
                            {
                              "isbn": "${book.isbn}",
                              "title": "${book.title}",
                              "authors": ${book.authors.toJson()}
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().delete(request).then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `404 NOT FOUND for non-existing book where number of pages should be removed`() {
        val request = "/api/books/$id/numberOfPages"
        val expectedResponse = """
                            {
                              "status": 404,
                              "error": "Not Found",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The book with ID: $id does not exist!"
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().delete(request).then()
                .statusCode(HttpStatus.NOT_FOUND.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    // BookByIdReturnEndpoint
    @Test fun `returns book and responds with its updated resource representation`() {
        every { bookDataStore.findById(id) } returns borrowedBookRecord

        val request = "/api/books/$id/return"
        val expectedResponse = """
                            {
                              "isbn": "${Books.CLEAN_CODE.isbn}",
                              "title": "${Books.CLEAN_CODE.title}",
                              "authors": ${Books.CLEAN_CODE.authors.toJson()},
                              "numberOfPages": ${Books.CLEAN_CODE.numberOfPages}                              
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().post(request).then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `409 CONFLICT for already returned book`() {
        every { bookDataStore.findById(id) } returns availableBookRecord

        val request = "/api/books/$id/return"
        val expectedResponse = """
                            {
                              "status": 409,
                              "error": "Conflict",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The book with ID: $id was already returned!"
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().post(request).then()
                .statusCode(HttpStatus.CONFLICT.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `404 NOT FOUND for non-existing book to be returned`() {
        val request = "/api/books/$id/return"
        val expectedResponse = """
                            {
                              "status": 404,
                              "error": "Not Found",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The book with ID: $id does not exist!"
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().post(request).then()
                .statusCode(HttpStatus.NOT_FOUND.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for malformed ID of book to be returned`() {
        val request = "/api/books/malformed-id/return"
        val expectedResponse = """
                            {
                              "status": 400,
                              "error": "Bad Request",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The request's 'id' parameter is malformed."
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON)
                .`when`().post(request).then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    // BookByIdTitleEndpoint
    @Test fun `replaces title of book and responds with its resource representation`() {
        every { bookDataStore.findById(id) } returns availableBookRecord

        val request = "/api/books/$id/title"
        val expectedResponse = """
                            {
                              "isbn": "${book.isbn}",
                              "title": "New Title",
                              "authors": ${book.authors.toJson()},
                              "numberOfPages": ${book.numberOfPages}
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { "title": "New Title" } """)
                .`when`().put(request).then()
                .statusCode(HttpStatus.OK.code)
                .contentType(MediaType.APPLICATION_HAL_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `404 NOT FOUND for non-existing book where title should be replaced`() {
        val request = "/api/books/$id/title"
        val expectedResponse = """
                            {
                              "status": 404,
                              "error": "Not Found",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The book with ID: $id does not exist!"
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { "title": "New Title" } """)
                .`when`().put(request).then()
                .statusCode(HttpStatus.NOT_FOUND.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    @Test fun `400 BAD REQUEST for missing required properties for book where title should be replaced`() {
        val idValue = BookId.generate().toString()
        val request = "/api/books/$idValue/title"
        val expectedResponse = """
                            {
                              "status": 400,
                              "error": "Bad Request",
                              "timestamp": "2017-08-20T12:34:56.789Z",
                              "message": "The request's body is invalid. See details...",
                              "details": [ "The field 'title' must not be blank." ]
                            }
                        """

        RestAssured.given().contentType(MediaType.APPLICATION_JSON).body(""" { } """)
                .`when`().put(request).then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JsonMatcher.jsonEqualTo(expectedResponse))
    }

    private fun availableBook(id: BookId, book: Book) = BookRecord(id, book)
    private fun borrowedBook(id: BookId, book: Book, borrowedBy: String, borrowedOn: String) = availableBook(id, book)
            .borrow(Borrower(borrowedBy), OffsetDateTime.parse(borrowedOn))

    private fun List<Author>.toJson() = joinToString(separator = "\", \"", prefix = "[\"", postfix = "\"]")

}
