package de.cheffe.solrsample;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedHSQLDBTestHarness;

public class HSQLImportTest {

    @ClassRule
    public static EmbeddedHSQLDBTestHarness hsqldb = new EmbeddedHSQLDBTestHarness();

    private Connection connection;

    @BeforeClass
    public static void setupDatabase() throws Exception {
        // generate a SQL file with loads of persons
        File tmpInsertPersonsSQL = new File("src/main/resources/database/insertPersons.sql");
        if (tmpInsertPersonsSQL.exists()) {
            RandomAccessFile tmpFile = new RandomAccessFile(tmpInsertPersonsSQL, "rw");
            tmpFile.setLength(0);
            tmpFile.close();
        } else {
            tmpInsertPersonsSQL.createNewFile();
        }
        FileWriter tmpWriter = new FileWriter(tmpInsertPersonsSQL);
        tmpWriter.write("truncate table person;\n");
        for (int i = 0; i < 500; i++) {
            tmpWriter.write("INSERT INTO person (firstname, lastname, state) VALUES ('firstname-" + i + "', 'lastname-" + i + "', '" + (i % 2) + "');\n");
            if (i % 100 == 0) {
                tmpWriter.write("commit;\n");
                tmpWriter.flush();
            }
        }
        tmpWriter.write("commit;\n");
        tmpWriter.close();

        // run liquibase to setup the database schema and load the generated persons
        Connection tmpConnection = hsqldb.createConnection();
        File tmpChangeSet = new File("src/main/resources/database/createDatabase.xml");
        Liquibase tmpLiquibase = new Liquibase(tmpChangeSet.getAbsolutePath(), new FileSystemResourceAccessor(), new JdbcConnection(tmpConnection));
        tmpLiquibase.update(null);
        tmpConnection.close();
    }

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

    @Test
    public void selectPerson() throws Exception {
        ResultSet tmpResultSet = connection.createStatement().executeQuery("SELECT firstname, lastname FROM person WHERE id = 1;");
        
        Assert.assertTrue(tmpResultSet.next());
        Assert.assertEquals("firstname-1", tmpResultSet.getString("firstname"));
        Assert.assertEquals("lastname-1", tmpResultSet.getString("lastname"));
    }
    
    @After
    public void disconnect() throws SQLException {
        connection.close();
    }
    
    @AfterClass
    public static void tearDownDatabase() throws SQLException {
        File tmpInsertPersonsSQL = new File("src/main/resources/database/insertPersons.sql");
        tmpInsertPersonsSQL.delete();
    }

    private static void assertDifferenceLessThan(long expectedDiff, long expected, long actual) {
        long actualDiff = Math.abs(expected - actual);
        if (actualDiff > expectedDiff) {
            throw new AssertionError("expected less than:<" + expectedDiff + "> but was:<" + actualDiff + ">");
        }
    }

}
