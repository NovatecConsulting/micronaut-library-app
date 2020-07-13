package library.service.security

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.*
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
open class UserPasswordAuthProvider : AuthenticationProvider {
    @Inject
    lateinit var store: UsersStore

    override fun authenticate(
        httpRequest: HttpRequest<*>?,
        authenticationRequest: AuthenticationRequest<*, *>?
    ): Publisher<AuthenticationResponse> {
        val username = authenticationRequest?.identity.toString()
        val password = authenticationRequest?.secret.toString()
        return if (password == store.getUserPassword(username)) {
            val details = UserDetails(username, Collections.singletonList(store.getUserRole(username)))
            Flowable.just(details)
        } else {
            Flowable.just(AuthenticationFailed())
        }
    }
}
