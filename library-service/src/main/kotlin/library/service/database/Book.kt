package library.service.database

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class Book @BsonCreator constructor(
        @JsonIgnore @BsonId var id: ObjectId?,
        @param:BsonProperty("isbn") val isbn: String?,
        @param:BsonProperty("title") val title: String?,
        @param:BsonProperty("authors") val authors: List<String>?,
        @param:BsonProperty("numberOfPages") val numberOfPages: Int?,
        @param:BsonProperty("borrowed") val borrowed: BorrowedState?
) {
    val _id: String
        get() = id.toString()
}

data class BorrowedState @BsonCreator constructor(
        @JsonIgnore @BsonId var id: ObjectId?,
        @param:BsonProperty("by") val by: String,
        @param:BsonProperty("on") val on: String
) {
    val _id: String
        get() = id.toString()
}
