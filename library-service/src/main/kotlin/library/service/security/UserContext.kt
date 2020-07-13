package library.service.security

import io.micronaut.security.utils.SecurityService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class UserContext() {
    @Inject
    lateinit var securityService: SecurityService

    fun isCurator() = currentUserHasRole(Authorizations.CURATOR_ROLE)

    private fun currentUserHasRole(role: String) = securityService?.hasRole(role)?: true
}
