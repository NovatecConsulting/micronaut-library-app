package library.service.security

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.convert.format.MapFormat

@ConfigurationProperties("credentials")
class UsersStore {
    @MapFormat
    var users: Map<String, String>? = null
    @MapFormat
    var roles: Map<String, String>? = null

    fun getUserPassword(username: String): String? {
        return users!![username]
    }

    fun getUserRole(username: String): String? {
        return roles!![username]
    }
}
