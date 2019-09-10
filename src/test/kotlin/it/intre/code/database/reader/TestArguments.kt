package it.intre.code.database.reader

import org.junit.jupiter.params.provider.Arguments

data class TestArguments(var args: Array<out Any>):Arguments {

    override fun get() = args

    companion object {
        fun of(vararg args:Any) = TestArguments(args)
    }

}
