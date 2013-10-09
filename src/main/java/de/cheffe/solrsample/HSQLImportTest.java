package de.cheffe.solrsample;

import java.io.File;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

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
    public void connect() throws LiquibaseException {
        connection = hsqldb.createConnection();
        
        File tmpChangeSet = new File("src/main/resources/database/createDatabase.xml");
        ResourceAccessor tmpAccessor = new FileSystemResourceAccessor();
        Liquibase tmpLiquibase = new Liquibase(tmpChangeSet.getAbsolutePath(), tmpAccessor, new JdbcConnection(connection));
        tmpLiquibase.update(null, new OutputStreamWriter(System.out));
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
