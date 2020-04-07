package library.service

import library.service.business.books.domain.events.BookEvent
import library.service.business.events.EventDispatcher
import javax.inject.Singleton

@Singleton
//@Validated
class BookEventDispatcher:EventDispatcher<BookEvent>{
    override fun dispatch(event: BookEvent) {
        // no op
    }

}
