package com.googlecode.paradox.data;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.paradox.ParadoxConnection;
import com.googlecode.paradox.data.table.value.FieldValue;
import com.googlecode.paradox.integration.MainTest;
import com.googlecode.paradox.metadata.ParadoxField;
import com.googlecode.paradox.metadata.ParadoxTable;

public class TableDataTest {

    private ParadoxConnection conn;

    @BeforeClass
    public static void setUp() throws Exception {
        Class.forName(Driver.class.getName());
    }

    @After
    public void closeConnection() throws Exception {
        if (conn != null) {
            conn.close();
        }
    }

    @Before
    public void connect() throws SQLException {
        conn = (ParadoxConnection) DriverManager.getConnection(MainTest.CONNECTION_STRING + "db");
    }

    @Test
    public void testInvalidTable() throws SQLException {
        Assert.assertEquals(0, TableData.listTables(conn, "not found.db").size());
    }

    @Test
    public void testLoadAreaCodes() throws SQLException {
        final List<ParadoxTable> tables = TableData.listTables(conn, "areacodes.db");
        Assert.assertNotNull("List tables is null", tables);
        Assert.assertTrue("List tables is empty", tables.size() > 0);
        final ParadoxTable table = tables.get(0);
        final List<List<FieldValue>> data = TableData.loadData(table, table.getFields());
        Assert.assertEquals(table.getRowCount(), data.size());
    }

    @Test
    public void testLoadContacts() throws SQLException {
        final ParadoxTable table = TableData.listTables(conn, "contacts.db").get(0);
        final ArrayList<ParadoxField> fields = new ArrayList<ParadoxField>();
        fields.add(table.getFields().get(0));
        TableData.loadData(table, fields);
    }

    @Test
    public void testLoadCustomer() throws SQLException {
        final ParadoxTable table = TableData.listTables(conn, "customer.db").get(0);
        final ArrayList<ParadoxField> fields = new ArrayList<ParadoxField>();
        fields.add(table.getFields().get(0));
        TableData.loadData(table, fields);
    }

    @Test
    public void testLoadHercules() throws SQLException {
        final ParadoxTable table = TableData.listTables(conn, "hercules.db").get(0);
        TableData.loadData(table, table.getFields());
    }

    @Test
    public void testLoadOrders() throws SQLException {
        final ParadoxTable table = TableData.listTables(conn, "orders.db").get(0);
        final ArrayList<ParadoxField> fields = new ArrayList<ParadoxField>();
        fields.add(table.getFields().get(0));
        TableData.loadData(table, fields);
    }

    @Test
    public void testLoadServer() throws SQLException {
        final ParadoxTable table = TableData.listTables(conn, "server.db").get(0);
        final ArrayList<ParadoxField> fields = new ArrayList<ParadoxField>();
        fields.add(table.getFields().get(0));
        TableData.loadData(table, fields);
    }
}
