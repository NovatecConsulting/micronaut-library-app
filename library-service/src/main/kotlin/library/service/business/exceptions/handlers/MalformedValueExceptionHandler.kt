package library.service.business.exceptions.handlers

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import library.service.api.ErrorDescription
import library.service.business.exceptions.MalformedValueException
import java.time.Clock
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [MalformedValueException::class, ExceptionHandler::class])
class MalformedValueExceptionHandler (private val clock: Clock) :
        BasicExceptionHandler<MalformedValueException, MutableHttpResponse<ErrorDescription>> (clock) {

    override fun handle(request: HttpRequest<*>, exception: MalformedValueException):
            MutableHttpResponse<ErrorDescription> {

        return HttpResponse.badRequest(errorDescription(
                httpStatus = HttpStatus.BAD_REQUEST,
                message = exception.message!!))
    }
}
