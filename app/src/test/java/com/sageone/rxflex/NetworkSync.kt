package com.sageone.rxflex

import org.junit.Test
import rx.Observable

/**
 * Network sync examples.
 */
class NetworkSync {

    @Test
    fun combineEndpointData() {
        syncTransactions().doOnNext { t ->
            DbTransaction(t.id, t.title, DbAccount(t.account.id, t.account.name, DbAccountType(t.account.accountType.id)), t.amount)
        }.subscribe({
            System.out.println(it)
        })
    }

    fun syncTransactions(): Observable<Transaction> {
        return Observable.zip(transactions(), accounts(), accountTypes(), {
            t, a, at ->
            t.map { transaction ->
                Transaction(transaction.id,
                        transaction.title,
                        a.findId(transaction.accountId).let {
                            account ->
                            Account(account.id, account.name, at.findId(account.typeId).let {
                                AccountType(it.id)
                            })
                        }, transaction.amount)
            }
        }).flatMap { Observable.from(it) }
    }

    companion object {
        fun transactions(): Observable<List<ApiTransaction>> {
            return Observable.just(listOf(
                    ApiTransaction("t1", "Apple", "a1", 10.0),
                    ApiTransaction("t2", "Banana", "a2", 8.88)))
        }

        fun accounts(): Observable<List<ApiAccount>> {
            return Observable.just(listOf(ApiAccount("a1", "Abacus Bank", "at1"), ApiAccount("a2", "Bingley Bank", "at2")))
        }

        fun accountTypes(): Observable<List<ApiAccountType>> {
            return Observable.just(listOf(ApiAccountType("at1"), ApiAccountType("at2")))
        }
    }

    data class ApiAccount(val id: String, val name: String, val typeId: String)
    data class ApiAccountType(val id: String)
    data class ApiTransaction(val id: String, val title: String, val accountId: String, val amount: Double)

    data class Account(val id: String, val name: String, val accountType: AccountType)
    data class AccountType(val id: String)
    data class Transaction(val id: String, val title: String, val account: Account, val amount: Double)

    data class DbTransaction(val id: String, val title: String, val account: DbAccount, val amount: Double)
    data class DbAccount(val id: String, val name: String, val accountType: DbAccountType)
    data class DbAccountType(val id: String)
}

private fun List<NetworkSync.ApiAccount>.findId(id: String): NetworkSync.ApiAccount {
    return find { id == it.id } ?: throw IllegalStateException("Unknown account id $id")
}

private fun List<NetworkSync.ApiAccountType>.findId(id: String): NetworkSync.ApiAccountType {
    return find { id == it.id } ?: throw IllegalStateException("Unknown account id $id")
}
