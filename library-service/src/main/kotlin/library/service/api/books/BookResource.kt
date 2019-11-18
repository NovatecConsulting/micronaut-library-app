package library.service.api.books

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import library.service.business.books.domain.BookRecord
import javax.inject.Singleton

/** Representation of a [BookRecord] as a REST resource. */
@JsonInclude(NON_NULL)
@Singleton
data class BookResource(
        val isbn: String,
        val title: String,
        val authors: List<String>?,
        val numberOfPages: Int?,
        val borrowed: Borrowed?
)

data class Borrowed(
        val by: String,
        val on: String
)
