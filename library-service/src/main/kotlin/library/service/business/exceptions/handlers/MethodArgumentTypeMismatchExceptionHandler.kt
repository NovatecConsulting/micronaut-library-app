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
import library.service.business.exceptions.MethodArgumentTypeMismatchException
import java.time.Clock
import java.time.OffsetDateTime
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [MethodArgumentTypeMismatchException::class, ExceptionHandler::class])
class MethodArgumentTypeMismatchExceptionHandler (private val clock: Clock) :
        ExceptionHandler<MethodArgumentTypeMismatchException, MutableHttpResponse<ErrorDescription>>{

    override fun handle(request: HttpRequest<*>, exception: MethodArgumentTypeMismatchException):
            MutableHttpResponse<ErrorDescription> {
        exception.printStackTrace()
        return HttpResponse.badRequest(errorDescription(
                httpStatus = HttpStatus.BAD_REQUEST,
                message = exception.message!!))
    }

    private fun errorDescription(
            httpStatus: HttpStatus,
            message: String,
            details: List<String> = emptyList()
    ) = ErrorDescription(
            status = httpStatus.code,
            error = httpStatus.reason,
            timestamp = OffsetDateTime.now(clock).toString(),
            //correlationId = correlationIdHolder.get(),
            message = message,
            details = details
    )
}
