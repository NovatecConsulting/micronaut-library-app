package library.service.database

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.UpdateResult
import org.bson.BsonBinary
import org.bson.BsonBinarySubType
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.codecs.UuidCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.internal.UuidHelper
import java.util.*
import javax.inject.Singleton


@Singleton
class BookRepository(
    private val mongoClient: MongoClient
) {

    fun find(): List<BookDocument> {
        return getCollection().find().toList()
    }

    fun find(_id: String): BookDocument? {
        return getCollection().find(eq("_id", UUID.fromString(_id))).first()
    }

    fun existsById(_id: String): Boolean {
        return find(_id) != null
    }

    fun save(_document: BookDocument): BookDocument? {
        val filter = eq("_id", _document.id)
        val options = ReplaceOptions().upsert(true)
        val updateResult = getCollection().replaceOne(filter, _document, options)

        return if (updateResult.upsertedId == null) {
            find(_document.id.toString())
        } else {
            val upsertedId = updateResult.upsertedId as BsonBinary
            val uuid = UuidHelper.decodeBinaryToUuid(
                upsertedId.data,
                BsonBinarySubType.UUID_STANDARD.value,
                UuidRepresentation.STANDARD
            )
            find(uuid.toString())
        }
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
            CodecRegistries.fromCodecs(UuidCodec(UuidRepresentation.STANDARD)),
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())

        )

        return mongoClient
            .getDatabase("library-service")
            .withCodecRegistry(codecRegistry)
            .getCollection("book", BookDocument::class.java)
    }
}
