package it.intre.code.database.reader.config;

import org.junit.Before;
import org.junit.Test;

import static it.intre.code.database.reader.config.Column.NameGenerator.*;
import static it.intre.code.database.reader.config.ColumnTest.createColumn;
import static org.junit.Assert.assertEquals;

public class NameGeneratorTest {

    private Column column;

    @Before
    public void setUp() throws Exception {
        column = createColumn("ETA", "BETA");
    }

    @Test
    public void nameGenerator_ONLY_NAME_NO_DERIVED_for_derived_column() {
        column.setDerived(true);
        assertEquals("", ONLY_NAME_NO_DERIVED.generate(column));
    }

    @Test
    public void nameGenerator_ONLY_NAME_NO_DERIVED_for_standard_column() {
        assertEquals("ETA", ONLY_NAME_NO_DERIVED.generate(column));
    }

    @Test
    public void nameGenerator_ONLY_ALIAS() {
        assertEquals("BETA", ONLY_ALIAS.generate(column));
    }

    @Test
    public void nameGenerator_NAME_WITH_ALIAS() {
        assertEquals("ETA AS BETA", NAME_WITH_ALIAS.generate(column));
    }
}