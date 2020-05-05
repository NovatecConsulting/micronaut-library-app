package library.service.business.exceptions.handlers

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.web.router.exceptions.UnsatisfiedBodyRouteException
import library.service.api.ErrorDescription
import java.time.Clock
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [UnsatisfiedBodyRouteException::class, ExceptionHandler::class])
class UnsatisfiedBodyRouteExceptionHandler (private val clock: Clock) :
        BasicExceptionHandler<UnsatisfiedBodyRouteException, MutableHttpResponse<ErrorDescription>> (clock) {

    override fun handle(request: HttpRequest<*>, exception: UnsatisfiedBodyRouteException):
            MutableHttpResponse<ErrorDescription> {

        return HttpResponse.badRequest(errorDescription(
                httpStatus = HttpStatus.BAD_REQUEST,
                message = "The request's body could not be read. It is either empty or malformed."))
    }
}
