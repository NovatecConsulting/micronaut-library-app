package library.service.api

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import library.service.business.exceptions.MalformedValueException
import library.service.business.exceptions.NotFoundException
import library.service.business.exceptions.NotPossibleException
import java.time.Clock
import java.time.OffsetDateTime
import javax.inject.Singleton

/**
 * Defines a number of commonly used exception handlers for REST endpoints.
 *
 * This includes basic handlers for common business exceptions like:
 * - [NotFoundException]
 * - [NotPossibleException]
 * - [MalformedValueException]
 *
 * As well as a number of framework exceptions related to bad user input.
 *
 * This class should _not_ contain any domain specific exception handlers.
 * Those need to be defined in the corresponding controller!
 */
@Produces
@Singleton
class ErrorHandlers(
        private val clock: Clock
        //,private val correlationIdHolder: CorrelationIdHolder
) {

    inner class NotFoundExceptionHandler():
            ExceptionHandler<NotFoundException, MutableHttpResponse<ErrorDescription>>
    {
        override fun handle(request: HttpRequest<Any>, exception: NotFoundException):
                MutableHttpResponse<ErrorDescription>? {
            return HttpResponse.badRequest(errorDescription(
                    httpStatus = HttpStatus.NOT_FOUND,
                    message = exception.message!!))
        }
    }

    inner class NotPossibleExceptionHandler():
            ExceptionHandler<NotPossibleException, MutableHttpResponse<ErrorDescription>>
    {
        override fun handle(request: HttpRequest<Any>, exception: NotPossibleException):
                MutableHttpResponse<ErrorDescription>? {
            return HttpResponse.badRequest(errorDescription(
                    httpStatus = HttpStatus.CONFLICT,
                    message = exception.message!!))
        }
    }

    inner class AccessDeniedExceptionHandler():
            ExceptionHandler<AccessDeniedException, MutableHttpResponse<ErrorDescription>>
    {
        override fun handle(request: HttpRequest<Any>, exception: AccessDeniedException):
                MutableHttpResponse<ErrorDescription>? {
            return HttpResponse.badRequest(errorDescription(
                    httpStatus = HttpStatus.FORBIDDEN,
                    message = "You don't have the necessary rights to to this."))
        }
    }

    inner class MalformedValueExceptionHandler():
            ExceptionHandler<MalformedValueException, MutableHttpResponse<ErrorDescription>>
    {
        override fun handle(request: HttpRequest<Any>, exception: MalformedValueException):
                MutableHttpResponse<ErrorDescription>? {
            return HttpResponse.badRequest(errorDescription(
                    httpStatus = HttpStatus.BAD_REQUEST,
                    message = exception.message!!))
        }
    }

    inner class GenericExceptionHandler():
            ExceptionHandler<Exception, MutableHttpResponse<ErrorDescription>>
    {
        override fun handle(request: HttpRequest<Any>, exception: Exception):
                MutableHttpResponse<ErrorDescription>? {
            return HttpResponse.badRequest(errorDescription(
                    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
                    message = "An internal server error occurred, see server logs for more information."))
        }
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
