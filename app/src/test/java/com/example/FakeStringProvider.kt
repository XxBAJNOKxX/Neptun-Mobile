package com.example

import com.example.core.locale.StringProvider

/** Egyszerű StringProvider-helyettesítő a tisztán JVM-alapú tesztekhez. */
class FakeStringProvider : StringProvider {
    override fun getString(id: Int): String = "res-$id"

    override fun getString(id: Int, vararg args: Any?): String =
        "res-$id:" + args.joinToString(",") { it.toString() }

    override fun getQuantityString(id: Int, quantity: Int, vararg args: Any?): String =
        "plur-$id:$quantity"
}
