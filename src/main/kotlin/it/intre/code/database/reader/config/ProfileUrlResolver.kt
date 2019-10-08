package it.intre.code.database.reader.config

import java.net.URL

interface ProfileUrlResolver {
    fun toUrl(profileName: String): URL
}
