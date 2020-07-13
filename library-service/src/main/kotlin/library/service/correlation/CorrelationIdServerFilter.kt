package library.service.correlation

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import org.reactivestreams.Publisher

private const val CORRELATION_ID_HEADER = "X-Correlation-ID"

/**
 * HTTP servlet filter responsible for processing optional external correlation
 * IDs or generating them, if none are provided by the caller.
 *
 * Consumer of the library's API can provide a custom correlation ID by setting
 * the `X-Correlation-ID` header. For requests without this header a random
 * [UUID] is generated. The correlation ID is then given to a
 * [CorrelationIdHolder] in order to remember it for the duration of the
 * request. The correlation ID is also added to each response generated by the
 * library.
 */
@Filter("/**")
class CorrelationIdServerFilter(
    private val correlationIdHolder: CorrelationIdHolder
) : HttpServerFilter {
    override fun doFilter(request: HttpRequest<*>?, chain: ServerFilterChain?): Publisher<MutableHttpResponse<*>> {

        val correlationId = request?.headers?.get(CORRELATION_ID_HEADER) ?: CorrelationId.generate()
        correlationIdHolder.set(correlationId)

        return correlationIdHolder.ioSchedule(request!!)
            .switchMap { chain?.proceed(request) }
            .doOnNext { res ->
                res.headers.add(CORRELATION_ID_HEADER, correlationId)
            }


    }
}
