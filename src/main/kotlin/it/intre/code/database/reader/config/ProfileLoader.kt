package it.intre.code.database.reader.config

import it.intre.code.database.reader.filter.FilterContainer
import org.codehaus.jackson.map.ObjectMapper
import java.io.IOException
import java.net.URL

/**
 * Loader of read profiles
 */
open class ProfileLoader {

    fun loadProfile(filter: FilterContainer, toUrl : (String) -> URL?): ReadProfile? {
        val profileName = filter.profile
        return if (!profileName.isNullOrBlank()) {
            loadProfile(profileName, toUrl)
        } else null
    }

    private fun loadProfile(profileName: String, toUrl : (String) -> URL?): ReadProfile {
        val url = toUrl(profileName) ?: throw ProfileNotFoundException("Profile not found: $profileName")
        try {
            val mapper = ObjectMapper()
            val readProfile = mapper.readValue(url, ReadProfile::class.java)
            loadIncludes(readProfile, toUrl)
            return readProfile
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    @Throws(IOException::class)
    private fun loadIncludes(readProfile: ReadProfile, toUrl : (String) -> URL?) {
        val mapper = ObjectMapper()
        for (query in readProfile.queries!!) {
            val include = query.include
            if (!include.isNullOrBlank()) {
                val includeProfile = mapper.readValue(toUrl(include), QueryProfile::class.java)
                query.copyFrom(includeProfile)
            }
        }
    }
}
