package com.sageone.rxflex

import android.util.Log
import org.junit.Test
import rx.Observable
import rx.functions.Func2
import java.util.*
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
        val b = Observable.just("b").delay(2, TimeUnit.SECONDS)
        val c = Observable.just("c").delay(3, TimeUnit.SECONDS)
        val d = Observable.just("d").delay(4, TimeUnit.SECONDS)

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

    @Test
    @Throws(Exception::class)
    fun pagination() {
        val tickerNum = Observable.just(1, 2, 3, 4, 5, 0, 6, 7, 8, 9)
        val tickerChar = listOf(Observable.just("a"), Observable.just("b"), Observable.just("c"), Observable.just("d"), Observable.just("e"), Observable.just("f"), Observable.just("g"), Observable.just("h"), Observable.just("i"))

        tickerNum.takeWhile { it != 0 }.flatMap { num -> tickerChar[num] }.toBlocking().subscribe(
                { System.out.println(it) },
                { System.out.println("Error") },
                { System.out.println("Complete") })
    }

    @Test
    @Throws(Exception::class)
    fun paginationRaven() {
        Observable.range(1, Integer.MAX_VALUE) // emit pages
                .concatMap { fakeNetCall(it) } // call and concatenate the api calls...
                .takeUntil { it.next == null } // ..until it says there's no more pages
                .map { it.items } // convert the response to from ApiList to List<Int>
                .reduce(ArrayList<Int>(), { list, value -> // add all to array
                    list.addAll(value)
                    list
                })
                .subscribe(
                        { println(it) },
                        { println("error $it") },
                        { println("complete") })
    }

    fun fakeNetCall(page: Int): Observable<ApiList> {
        println("fake net call $page")
        when (page) {
            1 -> return Observable.just(ApiList("next", 1, listOf(1, 2, 3)))
            2 -> return Observable.just(ApiList("next", 2, listOf(10, 20, 30)))
            3 -> return Observable.just(ApiList(null, 3, listOf(100, 200, 300)))
            else -> throw IllegalStateException("Invalid page number $page")
        }
    }

    data class Result(val a1: String, val b1: String, val c1: String, val d1: String)

    data class ApiList(val next: String?, val page: Int, val items: List<Int>)

    companion object {
        val TAG = "Test"
    }
}

