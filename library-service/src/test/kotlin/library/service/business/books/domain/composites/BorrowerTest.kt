package library.service.business.books.domain.composites

import contracts.ValueTypeContract
import library.service.business.books.domain.types.Borrower
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.classification.UnitTest

@UnitTest
internal class BorrowerTest : ValueTypeContract<Borrower, String>() {

    override fun getValueExample() = "Rob Stark"
    override fun getAnotherValueExample() = "Ned Stark"
    override fun createNewInstance(value: String) = Borrower(value)

    @Test fun `toString() returns Borrower's value as a String`() {
        val borrower = Borrower("slu")
        assertThat(borrower.toString()).isEqualTo("slu")
    }

}
