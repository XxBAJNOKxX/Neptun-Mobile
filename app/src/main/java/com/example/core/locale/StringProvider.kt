package com.example.core.locale

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * Erőforrás-szövegek elérése nem-Compose rétegekből (ViewModel, repository,
 * ApiClient). Az app-contextet használja, így az app nyelvi beállítását követi.
 */
interface StringProvider {
    fun getString(@StringRes id: Int): String
    fun getString(@StringRes id: Int, vararg args: Any?): String
    fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg args: Any?): String
}

class AndroidStringProvider(context: Context) : StringProvider {
    private val appContext = context.applicationContext

    override fun getString(@StringRes id: Int): String = appContext.getString(id)

    override fun getString(@StringRes id: Int, vararg args: Any?): String =
        appContext.getString(id, *args)

    override fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg args: Any?): String =
        appContext.resources.getQuantityString(id, quantity, *args)
}
