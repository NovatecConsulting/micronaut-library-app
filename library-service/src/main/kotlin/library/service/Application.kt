package library.service

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("library.service")
                .mainClass(Application.javaClass)
                .start()
    }
}