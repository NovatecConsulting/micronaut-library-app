package library.service.api

import io.micronaut.test.annotation.MicronautTest
import library.service.Application
import library.service.business.books.BookCollection
import library.service.business.books.BookIdGenerator
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest(application = Application::class)
class ApplicationTest {
    @Inject lateinit var bookCollection: BookCollection

    @Test
    fun `application starts`() = Unit
}
