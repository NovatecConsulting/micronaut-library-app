package library.service.business.exceptions.handlers

import io.micronaut.context.annotation.Requires
import io.micronaut.http.*
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import library.service.api.ErrorDescription
import java.time.Clock
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [AccessDeniedException::class, ExceptionHandler::class])
class AccessDeniedExceptionHandler (private val clock: Clock) :
        BasicExceptionHandler<AccessDeniedException, MutableHttpResponse<ErrorDescription>> (clock) {

    override fun handle(request: HttpRequest<*>, exception: AccessDeniedException):
            MutableHttpResponse<ErrorDescription> {

        return HttpResponseFactory.INSTANCE.status(HttpStatus.FORBIDDEN,errorDescription(
                httpStatus = HttpStatus.FORBIDDEN,
                message = "You don't have the necessary rights to to this."))
    }
}
