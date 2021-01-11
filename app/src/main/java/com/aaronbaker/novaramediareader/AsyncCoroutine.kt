package com.aaronbaker.novaramediareader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AsyncCoroutine<I, O> {
    var result: O? = null
    open fun onPreExecute() {}

    open fun onPostExecute(result: O?) {}
    abstract fun doInBackground(vararg params: I): O

    fun <T> execute(vararg input: I) {
        GlobalScope.launch(Dispatchers.Main) {
            onPreExecute()
            callAsync(*input)
        }
    }

    private suspend fun callAsync(vararg input: I) {
        withContext(Dispatchers.IO) {
            result = doInBackground(*input)
        }
        GlobalScope.launch(Dispatchers.Main) {
            onPostExecute(result)
        }
    }
}