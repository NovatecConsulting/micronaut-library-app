package utils

import io.micronaut.context.annotation.Replaces
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Singleton

@Replaces(Clock::class)
@Singleton
class MutableClock : Clock() {

    private var delegate: Clock = Clock.systemUTC()

    override fun withZone(zone: ZoneId?): Clock = delegate.withZone(zone)
    override fun getZone(): ZoneId = delegate.getZone()
    override fun instant(): Instant = delegate.instant()

    fun setFixedTime(isoTimestamp: String) {
        delegate = clockWithFixedTime(isoTimestamp)
    }

    fun reset() {
        delegate = Clock.systemUTC()
    }

}
