package library.service.database

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.util.*
import javax.inject.Singleton

@Singleton
class BookRepository(
        private val mongoClient: MongoClient
) {

    fun find(): List<BookDocument> {
        return getCollection().find().toList()
    }

    fun find(_id: String): BookDocument {
        return getCollection().find(eq("_id", UUID.fromString(_id))).first()
    }

    fun existsById(_id: String): Boolean {
        return find(_id) != null
    }

    fun save(_document: BookDocument): BookDocument{
        val filter = eq("_id", _document.id)
        val options = UpdateOptions().upsert(true)
        val updateResult = getCollection().replaceOne(filter, _document, options)

        return if (updateResult.upsertedId == null) {
            find(_document.id.toString())
        } else {
            find(updateResult.upsertedId.toString())
        }
    }

    fun insert(_isbn: String, _title: String): BookDocument {
        val book = BookDocument(UUID.randomUUID(), _isbn, _title, emptyList(), 0, null)
        getCollection().insertOne(book)
        return book
    }

    fun delete(_id: String) {
        getCollection().deleteOne(eq("_id", UUID.fromString(_id)))
    }

    fun updateTitle(_id: String, _title: String): UpdateResult {
        val filter = eq("_id", UUID.fromString(_id))
        val update = Document("\$set", Document("title", "$_title"))
        return getCollection().updateOne(filter, update)
    }

    fun updateAuthors(_id: String, _authors: List<String>?): UpdateResult {
        val filter = eq("_id", UUID.fromString(_id))
        val update = Document("\$set", Document("authors", _authors))
        return getCollection().updateOne(filter, update)
    }

    fun updateNumberOfPages(_id: String, _numberOfPages: Int?): UpdateResult {
        val filter = eq("_id", UUID.fromString(_id))
        val update = Document("\$set", Document("numberOfPages", Integer.valueOf("$_numberOfPages")))
        return getCollection().updateOne(filter, update)
    }

    private fun getCollection(): MongoCollection<BookDocument> {
        val codecRegistry = fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )
        return mongoClient
                .getDatabase("library-service")
                .withCodecRegistry(codecRegistry)
                .getCollection("book", BookDocument::class.java)
    }
}
