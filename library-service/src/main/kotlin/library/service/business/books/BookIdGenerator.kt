package library.service.business.books


import library.service.business.books.domain.types.BookId
import javax.inject.Singleton

@Singleton
open class BookIdGenerator(
        private val dataStore: BookDataStore
) {

    fun generate(): BookId {
        var bookId = BookId.generate()
        if (dataStore.existsById(bookId)) {
            bookId = generate()
        }
        return bookId
    }

}
