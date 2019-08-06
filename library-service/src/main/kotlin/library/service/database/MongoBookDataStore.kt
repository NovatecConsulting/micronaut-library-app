package library.service.database

import library.service.business.books.BookDataStore
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.types.BookId
import javax.inject.Singleton

@Singleton
class MongoBookDataStore(
        private val repository: BookRepository,
        private val bookRecordToDocumentMapper: Mapper<BookRecord, BookDocument>,
        private val bookDocumentToRecordMapper: Mapper<BookDocument, BookRecord>
) : BookDataStore {

    override fun createOrUpdate(bookRecord: BookRecord): BookRecord {
        val document = bookRecordToDocumentMapper.map(bookRecord)
        val updatedDocument = repository.save(document)
        return bookDocumentToRecordMapper.map(updatedDocument)
    }

    override fun delete(bookRecord: BookRecord) {
        repository.delete(bookRecord.id.toString())
    }

    override fun findById(id: BookId): BookRecord? {
        val document = repository.find(id.toString())
        var bookRecord: BookRecord? = null
        if(document != null){
            bookRecord = bookDocumentToRecordMapper.map(document)
        }
        return bookRecord
    }

    override fun findAll(): List<BookRecord> {
        val documents = repository.find()
        val bookRecords = mutableListOf<BookRecord>()
        for(document in documents){
            bookRecords.add(bookDocumentToRecordMapper.map(document))
        }
        return bookRecords
    }

    override fun existsById(bookId: BookId): Boolean {
        return repository.existsById(bookId.toString())
    }

}
