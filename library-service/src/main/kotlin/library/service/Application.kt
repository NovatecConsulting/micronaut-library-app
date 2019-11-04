package library.service

import io.micronaut.context.annotation.Factory
import io.micronaut.runtime.Micronaut
import java.time.Clock
import javax.inject.Singleton


object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("library.service")
                .mainClass(Application.javaClass)
                .start()
    }
}
@Factory
internal class ClockFactory {

    @Singleton
    fun clock(): Clock {
        return Clock.systemUTC()
    }
}
