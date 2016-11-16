package com.sageone.rxflex

import org.junit.Test
import rx.Observable
import java.util.concurrent.TimeUnit

/**
 * Flat map/map examples.
 */
class RxFlatMap {
    @Test
    @Throws(Exception::class)
    fun flatMap() {
        query("fm").flatMap { urls -> Observable.from(urls) }
                .subscribe { url -> println(url) }
    }

    @Test
    @Throws(Exception::class)
    fun flatMap2() {
        query("fm2-").flatMap { urls -> Observable.from(urls) }
                .flatMap { url -> title(url) }
                .toBlocking()
                .subscribe { url -> println(url) }
    }

    @Test
    @Throws(Exception::class)
    fun map() {
        query("m").subscribe { url -> println(url) }
    }

    companion object {
        var count : Int = 0
        fun query(text: String): Observable<List<String>> {
            return Observable.just(listOf("${text}1", "${text}2", "${text}3"))
        }
        fun title(url: String): Observable<String> {
            return Observable.just("$url-title").delay((++count).toLong(), TimeUnit.SECONDS)
        }
    }
}