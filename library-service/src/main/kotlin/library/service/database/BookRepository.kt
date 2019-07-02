package library.service.database

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
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

    fun insert(book: Book): Book {
        book.id = ObjectId()
        getCollection().insertOne(book)
        return book
    }

    fun delete(_id: String) {
        getCollection().deleteOne(eq("_id", ObjectId(_id)))
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
