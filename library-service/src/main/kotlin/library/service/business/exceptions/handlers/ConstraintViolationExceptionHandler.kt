package library.service.business.exceptions.handlers

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Produces
import library.service.api.ErrorDescription
import library.service.correlation.CorrelationIdHolder
import java.time.Clock
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Produces
@Singleton
@Replaces(io.micronaut.validation.exceptions.ConstraintExceptionHandler::class)
class ConstraintViolationExceptionHandler(
    private val clock: Clock,
    private val correlationIdHolder: CorrelationIdHolder
) :
    BasicExceptionHandler<ConstraintViolationException, MutableHttpResponse<ErrorDescription>>(
        clock,
        correlationIdHolder
    ) {

    override fun handle(request: HttpRequest<*>, exception: ConstraintViolationException):
            MutableHttpResponse<ErrorDescription> {

        // Customize ConstraintViolationMessage to match Spring MethodArgumentTypeMismatchException for comparability
        val detailsList = exception.message!!.split(", ").toMutableList()
        val it = detailsList.listIterator()
        while (it.hasNext()) {
            val message = it.next()
                .replaceBeforeLast(".", "")
                .replaceFirst(".", "")
                .replaceFirst(":", "")
            it.set(
                """The field '${message.substringBefore(" ")}' ${message.substringAfter(" ")}."""
            )
        }

        return HttpResponse.badRequest(
            errorDescription(
                httpStatus = HttpStatus.BAD_REQUEST,
                message = "The request's body is invalid. See details...",
                details = detailsList.toList()
            )
        )
    }
}
