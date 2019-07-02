package library.service.database

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*

@Controller("/books")
class BookRestController(
        private val repository: BookRepository
) {

    @Get("/")
    fun getAll(): HttpResponse<List<Book>> {
        return HttpResponse.ok(repository.find())
    }

    @Get("/{_id}")
    fun getOne(_id: String): HttpResponse<Book?>? {
        val book = repository.find(_id)
        book?.let {
            return HttpResponse.ok(repository.find(_id))
        } ?: return HttpResponse.notFound()

    }

    @Post("/")
    fun postOne(@Body book: Book): HttpResponse<Book> {
        val insertedBook = repository.insert(book)
        return HttpResponse.created(insertedBook)
    }

    @Delete("/{_id}")
    fun deleteOne(_id: String): HttpResponse<Book> {
        repository.delete(_id)
        return HttpResponse.noContent()
    }
}
