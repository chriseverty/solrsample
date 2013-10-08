package de.cheffe.solrsample;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedHSQLDBTestHarness;

public class HSQLImportTest {

    @ClassRule
    public static EmbeddedHSQLDBTestHarness hsqldb = new EmbeddedHSQLDBTestHarness();

    private Connection connection;

    @Before
    public void connect() {
        connection = hsqldb.createConnection();
    }

    @Test
    public void fetchDate() throws Exception {
        ResultSet tmpResultSet = connection.createStatement().executeQuery("VALUES (NOW)");
        tmpResultSet.next();
        long tmpDBTime = tmpResultSet.getTimestamp(1).getTime();
        long tmpCurrent = System.currentTimeMillis();

        assertDifferenceLessThan(20, tmpDBTime, tmpCurrent);
    }

    @After
    public void disconnect() throws SQLException {
        connection.close();
    }

    private static void assertDifferenceLessThan(long expectedDiff, long expected, long actual) {
        long actualDiff = Math.abs(expected - actual);
        if (actualDiff > expectedDiff) {
            throw new AssertionError("expected less than:<" + expectedDiff + "> but was:<" + actualDiff + ">");
        }
    }

}
