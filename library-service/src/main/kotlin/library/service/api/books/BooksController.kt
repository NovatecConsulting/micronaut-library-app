package library.service.api.books

import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import library.service.api.books.payload.*
import library.service.business.books.BookCollection
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.types.*
import java.util.*
import javax.validation.Valid


@Controller("/api/books")
class BooksController(
        private val collection: BookCollection,
        private val assembler: BookResourceAssembler
) {

    @Get("/")
    fun getBooks(): MutableHttpResponse<List<BookResource>> {
        val allBookRecords = collection.getAllBooks()
        val bookResources = mutableListOf<BookResource>()
        for (record in allBookRecords){
            bookResources.add(assembler.toResource(record))
        }
        return HttpResponse.ok(bookResources)
    }

    @Post("/")
    fun postBook(@Body body: CreateBookRequest): MutableHttpResponse<BookResource> {
        val book = Book(
                isbn = Isbn13.parse(body.isbn!!),
                title = Title(body.title!!),
                authors = emptyList(),
                numberOfPages = null
        )
        val bookRecord = collection.addBook(book)
        return HttpResponse.ok(assembler.toResource(bookRecord))
    }

    @Put("/{id}/title")
    fun putBookTitle(@PathVariable id: UUID, @Valid @Body body: UpdateTitleRequest): MutableHttpResponse<BookResource> {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeTitle(Title(body.title!!))
        }
        return HttpResponse.ok(assembler.toResource(bookRecord))
    }

    @Put("/{id}/authors")
    fun putBookAuthors(@PathVariable id: UUID, @Valid @Body body: UpdateAuthorsRequest):
            MutableHttpResponse<BookResource> {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeAuthors(body.authors!!.map { Author(it) })
        }
        return HttpResponse.ok(assembler.toResource(bookRecord))
    }


    @Delete("/{id}/authors")
    fun deleteBookAuthors(@PathVariable id: UUID): MutableHttpResponse<BookResource> {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeAuthors(emptyList())
        }
        return HttpResponse.ok(assembler.toResource(bookRecord))
    }

    @Put("/{id}/numberOfPages")
    fun putBookNumberOfPages(@PathVariable id: UUID, @Valid @Body body: UpdateNumberOfPagesRequest):
            MutableHttpResponse<BookResource> {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeNumberOfPages(body.numberOfPages)
        }
        return HttpResponse.ok(assembler.toResource(bookRecord))
    }

    @Delete("/{id}/numberOfPages")
    fun deleteBookNumberOfPages(@PathVariable id: UUID): MutableHttpResponse<BookResource> {
        val bookRecord = collection.updateBook(BookId(id)) {
            it.changeNumberOfPages(null)
        }
        return HttpResponse.ok(assembler.toResource(bookRecord))
    }


    @Get("/{id}")
    fun getBook(@PathVariable id: UUID): MutableHttpResponse<BookResource> {
        val bookRecord = collection.getBook(BookId(id))
        return HttpResponse.ok(assembler.toResource(bookRecord))
    }

    @Delete("/{id}")
    fun deleteBook(@PathVariable id: UUID): MutableHttpResponse<BookResource> {
        collection.removeBook(BookId(id))
        return HttpResponse.noContent()
    }

    @Post("/{id}/borrow")
    fun postBorrowBook(@PathVariable id: UUID, @Valid @Body body: BorrowBookRequest):
            MutableHttpResponse<BookResource> {
        val bookRecord = collection.borrowBook(BookId(id), Borrower(body.borrower!!))
        return HttpResponse.ok(assembler.toResource(bookRecord))
    }

    @Post("/{id}/return")
    fun postReturnBook(@PathVariable id: UUID):  MutableHttpResponse<BookResource> {
        val bookRecord = collection.returnBook(BookId(id))
        return HttpResponse.ok(assembler.toResource(bookRecord))
    }

}
