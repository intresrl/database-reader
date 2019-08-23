package it.intre.code.database.reader.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColumnTest {

    @Test
    public void getOutName_returns_alias_if_present() {
        Column column = createColumn("NONNA PAPERA", "CICCIO");
        assertEquals("CICCIO", column.getOutName());
    }

    @Test
    public void getOutName_returns_name_if_null_alias() {
        Column column = createColumn("PAPERONE", null);
        assertEquals("PAPERONE", column.getOutName());
    }

    @Test
    public void getOutName_returns_name_if_empty_alias() {
        Column column = createColumn("PAPERONE", "");
        assertEquals("PAPERONE", column.getOutName());
    }

    @Test
    public void getNameWithAlias_returns_name_when_no_alias() {
        Column column = createColumn("PAPEROGA", null);
        assertEquals("PAPEROGA", column.getNameWithAlias());
    }

    @Test
    public void getNameWithAlias_returns_name_when_alias_is_the_same() {
        Column column = createColumn("PIPPO", "PIPPO");
        assertEquals("PIPPO", column.getNameWithAlias());
    }

    @Test
    public void getNameWithAlias_returns_name_and_alias_when_different() {
        Column column = createColumn("PAPERINO", "PAPERINIK");
        assertEquals("PAPERINO AS PAPERINIK", column.getNameWithAlias());
    }

    static Column createColumn(String name, String alias) {
        Column column = new Column(name);
        column.setAlias(alias);
        return column;
    }
}