package dev.fishies.routine.util.geometry

/**
 * Data container that holds a position and a heading.
 */
data class Pose2(val xy: Vector2, val h: Radians) {
    constructor(x: Inches, y: Inches, heading: Radians) : this(Vector2(x, y), heading)

    val xyh get() = Triple(xy.x, xy.y, h)
    val x inline get() = xy.x
    val y inline get() = xy.y

    operator fun plus(other: Pose2) = Pose2(xy + other.xy, h.rotated(other.h))
    operator fun plus(other: Radians) = Pose2(xy, h.rotated(other))
    operator fun minus(other: Pose2) = Pose2(xy - other.xy, h.rotated(-other.h))

    /**
     * Simultaneously calculates the positional and **signed** angular distance between this and [other].
     *
     * @param other The other [Pose2] to calculate the distance to
     */
    fun distanceTo(other: Pose2) = Pair(xy.distanceTo(other.xy), h.angleTo(other.h))

    /**
     * Calculates the positional distance from this to [other].
     *
     * Heading is ignored for this calculation.
     * If you want to calculate the angular distance as well, use the other `distanceTo`
     *
     * @param other The other [Vector2] to calculate the distance to
     */
    fun distanceTo(other: Vector2) = xy.distanceTo(other)

    override fun toString() = "($xy x $h)"

    companion object {
        val ZERO = Pose2(Vector2.Factory.ZERO, Radians.Companion.ZERO)
    }
}