package org.fishnpotatoes.routine.util

val Collection<*>.indexRange: IntRange get() = 0..<size
val Array<out Any?>.indexRange: IntRange get() = 0..<size