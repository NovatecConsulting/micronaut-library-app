package library.service.api.books.payload

import io.micronaut.core.annotation.Introspected
import library.service.business.books.domain.types.Borrower
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/** Request body used when borrowing a book. */
@Introspected
data class BorrowBookRequest(
        @field:NotNull
        @field:Size(min = 1, max = 50)
        @field:Pattern(regexp = Borrower.VALID_BORROWER_PATTERN)
        val borrower: String?
)
