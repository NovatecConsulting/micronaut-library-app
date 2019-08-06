package library.service.api.books

import com.mongodb.client.result.UpdateResult
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import library.service.api.books.payload.*
import library.service.database.BookDocument
import library.service.database.BookRepository


@Controller("/books")
class BooksController(
        private val repository: BookRepository
) {

    @Get("/")
    fun getBooks(): HttpResponse<List<BookDocument>> {
        return HttpResponse.ok(repository.find())
    }

    @Get("/{_id}")
    fun getBook(_id: String): HttpResponse<BookDocument?>? {
        val book = repository.find(_id)
        book?.let {
            return HttpResponse.ok(repository.find(_id))
        } ?: return HttpResponse.notFound()

    }

    @Post("/")
    fun postBook(@Body body: CreateBookRequest): HttpResponse<BookDocument> {
        val insertedBook = repository.insert(body.isbn.toString(), body.title.toString())
        return HttpResponse.created(insertedBook)
    }

    @Delete("/{_id}")
    fun deleteOne(_id: String): HttpResponse<BookDocument> {
        repository.delete(_id)
        return HttpResponse.noContent()
    }

    @Put("/{_id}/title")
    fun putBookTitle(_id: String, @Body body: UpdateTitleRequest): HttpResponse<UpdateResult> {
        val updateResult = repository.updateTitle(_id, body.title!!)
        return HttpResponse.ok(updateResult)
    }

    @Put("/{_id}/authors")
    fun putBookAuthors(_id: String, @Body body: UpdateAuthorsRequest): HttpResponse<UpdateResult> {
        val updateResult = repository.updateAuthors(_id, body.authors)
        return HttpResponse.ok(updateResult)
    }

    @Delete("/{_id}/authors")
    fun deleteBookAuthors(_id: String): HttpResponse<UpdateResult> {
        val updateResult = repository.updateAuthors(_id, emptyList())
        return HttpResponse.ok(updateResult)
    }

    @Put("/{_id}/numberOfPages")
    fun putBookNumberOfPages(_id: String, @Body body: UpdateNumberOfPagesRequest): HttpResponse<UpdateResult> {
        val updateResult = repository.updateNumberOfPages(_id, body.numberOfPages)
        return HttpResponse.ok(updateResult)
    }

    @Delete("/{_id}/numberOfPages")
    fun deleteBookNumberOfPages(_id: String): HttpResponse<UpdateResult> {
        val updateResult = repository.updateNumberOfPages(_id, null)
        return HttpResponse.ok(updateResult)
    }

    @Post("/{_id}/borrow")
    fun postBorrowBook(_id: String, @Body body: BorrowBookRequest): HttpResponse<UpdateResult> {
        // !todo IMPLEMENT
        return HttpResponse.ok()
    }

    @Post("/{_id}/return")
    fun postReturnBook(_id: String, @Body body: BorrowBookRequest): HttpResponse<UpdateResult> {
        // !todo IMPLEMENT
        return HttpResponse.ok()
    }

}
