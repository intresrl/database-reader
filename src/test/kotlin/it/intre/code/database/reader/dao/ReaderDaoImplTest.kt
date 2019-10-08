package it.intre.code.database.reader.dao

import it.intre.code.database.reader.config.ProfileLoader
import it.intre.code.database.reader.config.ProfileUrlResolver
import it.intre.code.database.reader.config.ReadProfile
import it.intre.code.database.reader.filter.FilterContainer
import it.intre.code.database.reader.filter.QueryStringFilter
import it.intre.code.database.reader.filter.generic.TextFilter
import it.intre.code.database.reader.resultset.ReaderResultSet
import it.intre.code.database.reader.sql.SqlHelperTest.Companion.createQueryProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.net.URL
import java.sql.*
import java.util.Arrays.asList
import javax.sql.DataSource

class ReaderDaoImplTest {

    private val sut: ReaderDaoImpl = ReaderDaoImpl()

    private val profileLoader: ProfileLoader = mock(ProfileLoader::class.java)

    private var dataSource: DataSource = mock(DataSource::class.java)

    private val mockConnection: Connection = mock(Connection::class.java)

    private val stm: Statement = mock(Statement::class.java)

    private val meta: ResultSetMetaData = mock(ResultSetMetaData::class.java)

    private val rs: ResultSet = mock(ResultSet::class.java)

    private var mockProfile: ReadProfile? = null

    val profileUrlResolver = object : ProfileUrlResolver {
        override fun toUrl(path: String) = with("$path.json") {
            Thread.currentThread().contextClassLoader?.getResource(this) ?: ClassLoader.getSystemResource(this)
        }
    }

    @BeforeEach
    fun setUp() {
        sut.setProfileLoader(profileLoader)
        sut.setGenericDataSource(object : GenericDataSource {
            override val connection: Connection
                get() = mockConnection
        })
        sut.setProfileUrlResolver(profileUrlResolver)

        mockProfile = ReadProfile()
        mockProfile!!.name = PROFILE_NAME
        mockProfile!!.queries = listOf(createQueryProfile())
    }

    @Throws(SQLException::class)
    private fun setUpMocks() {
        `when`(dataSource.connection).thenReturn(mockConnection)
        `when`(mockConnection.createStatement()).thenReturn(stm)
        `when`(stm.executeQuery(ArgumentMatchers.any())).thenReturn(rs)
        `when`(rs.metaData).thenReturn(meta)
        val oneRow = object : Answer<Boolean> {
            private var count = 0

            override fun answer(invocation: InvocationOnMock): Boolean {
                return count++ <= 0
            }
        }
        `when`(rs.next()).thenAnswer(oneRow)
        `when`<Any>(rs.getObject(ArgumentMatchers.anyInt())).thenReturn(MOCK_ROW_COUNT, MOCK_LAST_VALUE)
        `when`(meta.getColumnName(ArgumentMatchers.anyInt())).thenReturn("rowcount", "the_last")
        `when`(meta.columnCount).thenReturn(2)
    }

    @Test
    fun getColumns() {
        `when`(profileLoader.loadProfile(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mockProfile)

        val actual = sut.getColumns(FilterContainer())

        val expected = ReaderResultSet()
        expected.columns = mockProfile!!.columns

        assertEquals(expected, actual)
    }

    @Test
    @Throws(SQLException::class)
    fun findLast() {
        val filterContainer = FilterContainer()
        filterContainer.profile = PROFILE_NAME
        filterContainer.customFilters = listOf<TextFilter>(TextFilter("Y", false, false, asList<String>("BAR", "BAZ"), false))
        val queryStringFilter = QueryStringFilter()
        queryStringFilter.last = "the_last"
        filterContainer.queryStringFilter = queryStringFilter
        `when`(profileLoader.loadProfile(filterContainer, profileUrlResolver::toUrl)).thenReturn(mockProfile)

        setUpMocks()

        val actual = sut.find(filterContainer)

        val expected = ReaderResultSet()
        expected.totalRows = MOCK_ROW_COUNT
        expected.last = MOCK_LAST_VALUE

        assertEquals(expected, actual)
    }

    companion object {
        private val PROFILE_NAME = "FOO"
        private val MOCK_ROW_COUNT = 2048
        private val MOCK_LAST_VALUE = "987654"
    }

}