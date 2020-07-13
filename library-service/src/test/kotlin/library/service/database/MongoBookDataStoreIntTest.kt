package library.service.database

import io.micronaut.test.annotation.MicronautTest
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.states.Borrowed
import library.service.business.books.domain.types.BookId
import library.service.business.books.domain.types.Borrower
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.Books
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@MicronautTest
internal class MongoBookDataStoreIntTest {
    @Inject
    lateinit var repository: BookRepository

    @Inject
    lateinit var cut: MongoBookDataStore

    @BeforeEach
    fun resetDatabase() = with(repository) {
        val allBooks = cut.findAll()
        allBooks.forEach {
            cut.delete(it)
        }
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

    @Test
    fun `existing book records are updated`() {
        val bookId = BookId.generate()
        val inputRecord = BookRecord(bookId, Books.THE_DARK_TOWER_II)
        val createdRecord = cut.createOrUpdate(inputRecord)

        val borrowedBy = Borrower("Frodo")
        val borrowedOn = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC)
        val modifiedRecord = BookRecord(bookId, Books.THE_LORD_OF_THE_RINGS_1, Borrowed(borrowedBy, borrowedOn))

        val updatedRecord = cut.createOrUpdate(modifiedRecord)

        assertThat(updatedRecord).isEqualTo(modifiedRecord)
        assertThat(updatedRecord).isNotEqualTo(createdRecord)
        assertThat(cut.findById(bookId)).isEqualTo(updatedRecord)
    }

    // looking up book records
    @Test
    fun `book records can be looked up by their ID`() {
        val bookRecord = create(Books.THE_DARK_TOWER_II)
        assertThat(cut.findById(bookRecord.id)).isEqualTo(bookRecord)
    }

    @Test
    fun `looking up book records by their ID might return null`() {
        assertThat(cut.findById(BookId.generate())).isNull()
    }

    @Test
    fun `all book records can be looked up at once`() {
        val bookRecord1 = create(Books.THE_DARK_TOWER_IV)
        val bookRecord2 = create(Books.THE_DARK_TOWER_V)
        val bookRecord3 = create(Books.THE_DARK_TOWER_VI)

        val allBooks = cut.findAll()

        assertThat(allBooks).containsOnly(bookRecord1, bookRecord2, bookRecord3)
    }


    // checking existence of records by Id
    @Test
    fun `returns true for exiting records`() {
        val bookRecord = create(Books.THE_DARK_TOWER_II)
        assertThat(cut.existsById(bookRecord.id)).isTrue()
    }

    @Test
    fun `returns false for non-existing records`() {
        assertThat(cut.existsById(BookId.generate())).isFalse()
    }

    // deleting book records
    @Test
    fun `book records can be deleted`() {
        val bookRecord = create(Books.THE_DARK_TOWER_III)
        cut.delete(bookRecord)
        assertThat(cut.findById(bookRecord.id)).isNull()
    }

    private fun create(book: Book): BookRecord {
        return cut.createOrUpdate(BookRecord(BookId.generate(), book))
    }

}
