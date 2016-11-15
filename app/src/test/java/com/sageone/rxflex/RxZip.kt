package com.sageone.rxflex

import org.junit.Test
import rx.Observable
import java.util.concurrent.TimeUnit

/**
 */
class RxZip {
    @Test
    @Throws(Exception::class)
    fun map() {
        val oneToEight = Observable.range(1, 8)
        val files = oneToEight.map { x -> 'a' + x - 1 }.doOnNext { System.out.println("  files $it") }
        val ranks = oneToEight.map { it.toString() }.doOnNext { System.out.println("  ranks $it") }

        val squares = files.flatMap { file -> ranks.map { file + it } }

        squares.subscribe {
            System.out.println(it)
        }
    }

    @Test
    @Throws(Exception::class)
    fun asyncCompletionMerge() {
        val a = Observable.just("a").delay(1, TimeUnit.SECONDS)
        val b = Observable.just("b").delay(1, TimeUnit.SECONDS)
        val c = Observable.just("c").delay(1, TimeUnit.SECONDS)
        val d = Observable.just("d").delay(1, TimeUnit.SECONDS)

        Observable.merge(a, b, c, d).toBlocking().subscribe({ System.out.println(it) }, { System.out.println("Error") }, { System.out.println("Complete") })
    }

    @Test
    @Throws(Exception::class)
    fun asyncCompletionMergeDependency() {
        val a = Observable.just("a").delay(1, TimeUnit.SECONDS)
        val b = Observable.just("b").delay(2, TimeUnit.SECONDS)
        val c = Observable.just("c").delay(3, TimeUnit.SECONDS)
        val d = Observable.just("d").delay(4, TimeUnit.SECONDS)
        val e = Observable.just("e").delay(100, TimeUnit.MILLISECONDS)
        val f = Observable.just("f").delay(200, TimeUnit.MILLISECONDS)
        val g = Observable.just("g").delay(300, TimeUnit.MILLISECONDS)
        val h = Observable.just("h").delay(400, TimeUnit.MILLISECONDS)

        Observable.merge(a, b, c, d).concatWith(Observable.merge(e, f, g, h)).toBlocking().subscribe({ System.out.println(it) }, { System.out.println("Error") }, { System.out.println("Complete") })
    }

    @Test
    @Throws(Exception::class)
    fun asyncCompletionZip() {
        val a = Observable.just("a").delay(1, TimeUnit.SECONDS).doOnNext { System.out.println(it) }
        val b = Observable.just("b").delay(1, TimeUnit.SECONDS).doOnNext { System.out.println(it) }
        val c = Observable.just("c").delay(1, TimeUnit.SECONDS).doOnNext { System.out.println(it) }
        val d = Observable.just("d").delay(1, TimeUnit.SECONDS).doOnNext { System.out.println(it) }

        Observable.zip(a, b, c, d) { a1, b1, c1, d1 -> Result(a1, b1, c1, d1) }.toBlocking().subscribe({ System.out.println(it) }, { System.out.println("Error") }, { System.out.println("Complete") })
    }
}

data class Result(val a1: String, val b1: String, val c1: String, val d1: String)
