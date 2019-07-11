package library.service.database

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.result.UpdateResult
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.ObjectId
import javax.inject.Singleton

@Singleton
class BookRepository(
        private val mongoClient: MongoClient
) {

    fun find(): List<Book> {
        return getCollection().find().toList()
    }

    fun find(_id: String): Book? {
        return getCollection().find(eq("_id", ObjectId(_id))).first()
    }

    fun insert(_isbn: String?, _title: String?): Book {
        val book = Book(ObjectId(), _isbn, _title, emptyList(), 0, null)
        getCollection().insertOne(book)
        return book
    }

    fun delete(_id: String) {
        getCollection().deleteOne(eq("_id", ObjectId(_id)))
    }

    fun updateTitle(_id: String, _title: String): UpdateResult {
        val filter = eq("_id", ObjectId(_id))
        val update = Document("\$set", Document("title", "$_title"))
        return getCollection().updateOne(filter, update)
    }

    fun updateAuthors(_id: String, _authors: List<String>?): UpdateResult {
        val filter = eq("_id", ObjectId(_id))
        val update = Document("\$set", Document("authors", "$_authors"))
        return getCollection().updateOne(filter, update)
    }

    fun updateNumberOfPages(_id: String, _fieldValue: Int?): UpdateResult {
        val filter = eq("_id", ObjectId(_id))
        val update = Document("\$set", Document("numberOfPages", "$_fieldValue"))
        return getCollection().updateOne(filter, update)
    }

    private fun getCollection(): MongoCollection<Book> {
        val codecRegistry = fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )
        return mongoClient
                .getDatabase("library-service")
                .withCodecRegistry(codecRegistry)
                .getCollection("book", Book::class.java)
    }
}
