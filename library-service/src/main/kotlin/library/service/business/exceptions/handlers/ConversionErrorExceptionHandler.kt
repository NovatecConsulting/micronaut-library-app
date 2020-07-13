package library.service.business.exceptions.handlers

import io.micronaut.context.annotation.Replaces
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Produces
import library.service.api.ErrorDescription
import library.service.correlation.CorrelationIdHolder
import java.time.Clock
import javax.inject.Singleton

@Produces
@Singleton
@Replaces(io.micronaut.http.server.exceptions.ConversionErrorHandler::class)
class ConversionErrorExceptionHandler(private val clock: Clock, correlationIdHolder: CorrelationIdHolder) :
    BasicExceptionHandler<ConversionErrorException, MutableHttpResponse<ErrorDescription>>(
        clock,
        correlationIdHolder
    ) {

    override fun handle(request: HttpRequest<*>, exception: ConversionErrorException):
            MutableHttpResponse<ErrorDescription> {
        val parameter = exception.message!!.substringAfter("[").substringBefore("]")

        return HttpResponse.badRequest(
            errorDescription(
                httpStatus = HttpStatus.BAD_REQUEST,
                message = """The request's '${parameter}' parameter is malformed."""
            )
        )
    }
}
