package library.service.database

import io.micronaut.test.annotation.MicronautTest
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.types.BookId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.Books
import javax.inject.Inject

@MicronautTest
internal class MongoBookDataStoreIntTest {
    @Inject
    lateinit var repository: BookRepository

    @Inject
    lateinit var cut: MongoBookDataStore


    @BeforeEach
    fun resetDatabase() = with(repository) { // !TODO Delete all
    }

    // creating or updating book records
    @Test
    fun `non-existing book records are created`() {
        val bookId = BookId.generate()
        val inputRecord = BookRecord(bookId, Books.THE_DARK_TOWER_I)
        val createdRecord = cut.createOrUpdate(inputRecord)

        assertThat(createdRecord).isEqualTo(inputRecord)
        assertThat(cut.findById(bookId)).isEqualTo(createdRecord)
    }

}
