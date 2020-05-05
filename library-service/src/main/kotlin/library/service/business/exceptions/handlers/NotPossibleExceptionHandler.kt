package library.service.business.exceptions.handlers

import io.micronaut.context.annotation.Requires
import io.micronaut.http.*
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import library.service.api.ErrorDescription
import library.service.business.exceptions.NotPossibleException
import java.time.Clock
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [NotPossibleException::class, ExceptionHandler::class])
class NotPossibleExceptionHandler (private val clock: Clock) :
        BasicExceptionHandler<NotPossibleException, MutableHttpResponse<ErrorDescription>> (clock) {

    override fun handle(request: HttpRequest<*>, exception: NotPossibleException):
            MutableHttpResponse<ErrorDescription> {

        return HttpResponseFactory.INSTANCE.status(HttpStatus.CONFLICT,errorDescription(
                httpStatus = HttpStatus.CONFLICT,
                message = exception.message!!))
    }
}
