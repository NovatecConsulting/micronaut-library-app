package library.service.api.books.payload

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotEmpty

/** Request body used when updating a book's title. */
@Introspected
data class UpdateAuthorsRequest(
        @get:NotEmpty
        val authors: List<String>?
)
