package library.service.api.books

import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.states.Available
import library.service.business.books.domain.states.Borrowed
import javax.inject.Singleton

/**
 * Component responsible for converting a [BookRecord] into a [BookResource].
 *
 * This includes transforming the data from one class to another and adding the
 * correct links depending on the [BookRecord] state.
 */
@Singleton
class BookResourceAssembler () {
    fun toResource(bookRecord: BookRecord): BookResource {
        return instantiateResource(bookRecord)
    }

    private fun instantiateResource(bookRecord: BookRecord): BookResource {
        val bookState = bookRecord.state
        return BookResource(
                isbn = bookRecord.book.isbn.toString(),
                title = bookRecord.book.title.toString(),
                authors = bookRecord.book.authors.map { it.toString() },
                numberOfPages = bookRecord.book.numberOfPages,
                borrowed = when (bookState) {
                    is Available -> null
                    is Borrowed -> Borrowed(by = "${bookState.by}", on = "${bookState.on}")
                }
        )
    }
}
