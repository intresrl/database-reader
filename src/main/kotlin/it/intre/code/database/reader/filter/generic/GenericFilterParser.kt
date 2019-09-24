package it.intre.code.database.reader.filter.generic

//import java.util.stream.RefStreams
import java8.util.Optional
import java8.util.stream.RefStreams
import java.util.regex.Pattern.quote
import javax.ws.rs.core.MultivaluedMap

/**
 * Parser for custom filters, i.e. the additional filter fields
 * that are not among properties of [it.intre.code.database.reader.filter.QueryStringFilter].
 *
 *
 * Examples:
 * - ` f.foo=1&f.foo=2 ` means `foo=1 OR foo=2`
 * - ` f.bar=abc* ` means `bar LIKE 'abc%'`
 * - ` f.baz=xyz&f.baz.negate=true ` means `NOT (baz='xyz')`
 * - ` f.fizz.blank=true&f.fizz=0 ` means `fizz IS NULL OR fizz=0`
 */
class GenericFilterParser(private val params: MultivaluedMap<String, String>) {

    fun parse(): List<GenericFilter> = params.keys
            .filter { this.isCustomFilter(it) }
            .map { noSuffix(it) }
            .distinct()
            .filter { this.isSingleWord(it) }
            .map { this.toFilter(it) }
            .filter { it.isPresent }
            .map { it.get() }

    /** Complex expression are filtered as a safeguard against sql injection  */
    private fun isSingleWord(it: String) = noPrefix(it).matches("\\w+".toRegex())

    private fun isCustomFilter(k: String) = k.startsWith(PREFIX)

    private fun toFilter(name: String): Optional<GenericFilter> {
        // try all handlers in priority order
        val handlerStream = RefStreams.of(
                ComparisonHandler(params),
                TextHandler(params)
        )
        return handlerStream
                .map { handler -> handler.toFilter(name) }
                .filter { it.hasCondition() }
                .findFirst()
    }

    companion object {

        /**
         * Keys in provided params that start with this string can be used to create a [GenericFilter].
         * This prefix will be stripped away in the resulting [GenericFilter].
         * Keys ending with any of the suffixes [ParseHandler.NEGATE_SUFFIX], [TextHandler.BLANK_SUFFIX], [ParseHandler.ALL_SUFFIX]
         * do not result in values for a [GenericFilter], but may set options on other filters.
         *
         *
         * Example: ` f.foo=["1", "2"] ` leads to a filter with name = "foo" and values = ["1","2"]
         */
        const val PREFIX = "f."

        internal fun noPrefix(key: String): String {
            return key.replace(("^" + quote(PREFIX)).toRegex(), "")
        }

        internal fun noSuffix(key: String): String {
            val k = noPrefix(key)
            val pre = if (key == k) "" else PREFIX
            val regex = "[.][^.]+$"
            return pre + k.replace(regex.toRegex(), "")
        }
    }

}
