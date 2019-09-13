package utils.testconfiguration

import io.micronaut.context.annotation.Bean
import utils.MutableClock

class BaseTestConfiguration {
    @Bean
    fun clock() = MutableClock()

}
