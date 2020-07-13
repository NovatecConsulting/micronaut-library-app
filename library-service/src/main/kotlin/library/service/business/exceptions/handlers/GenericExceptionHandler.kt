package library.service.business.exceptions.handlers

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import library.service.api.ErrorDescription
import library.service.correlation.CorrelationIdHolder
import java.time.Clock
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [Exception::class, ExceptionHandler::class])
class GenericExceptionHandler(private val clock: Clock, correlationIdHolder: CorrelationIdHolder) :
    BasicExceptionHandler<Exception, MutableHttpResponse<ErrorDescription>>(clock, correlationIdHolder) {

    override fun handle(request: HttpRequest<Any>, exception: Exception):
            MutableHttpResponse<ErrorDescription>? {

        return HttpResponse.serverError(
            errorDescription(
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
                message = "An internal server error occurred, see server logs for more information."
            )
        )
    }
}
