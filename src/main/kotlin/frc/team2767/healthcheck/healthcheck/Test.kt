package frc.team2767.healthcheck.healthcheck

import kotlinx.html.TagConsumer

abstract class Test(protected var name: String) {
    abstract fun execute()
    abstract fun isFinished(): Boolean
    abstract fun reportHeader(tagConsumer: TagConsumer<Appendable>)
    abstract fun results(tagConsumer: TagConsumer<Appendable>)
}


private fun <T : Comparable<T>> ClosedRange<T>.withUnits(units: String) =
    "${this.start} - ${this.endInclusive} $units"
