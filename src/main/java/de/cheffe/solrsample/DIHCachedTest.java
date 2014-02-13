package de.cheffe.solrsample;

import de.cheffe.solrsample.rule.EmbeddedHSQLDBTestHarness;
import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;

import org.apache.solr.client.solrj.beans.Field;
import org.junit.*;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DIHCachedTest {

    @ClassRule
    public static EmbeddedHSQLDBTestHarness hsqldb = new EmbeddedHSQLDBTestHarness();

    @ClassRule
    public static EmbeddedSolrServerResource<Publication> solr = new EmbeddedSolrServerResource<>("dih-cached");

    private Connection connection;

    @BeforeClass
    public static void setupDatabase() throws Exception {
        // run liquibase to setup the database schema
        Connection tmpConnection = hsqldb.createConnection();
        File tmpChangeSet = new File("src/main/resources/database/createPublicationsDB.xml");
        Liquibase tmpLiquibase = new Liquibase(tmpChangeSet.getAbsolutePath(), new FileSystemResourceAccessor(), new JdbcConnection(tmpConnection));
        tmpLiquibase.update(null);
        
        // insert some sample data
        Statement tmpStatement = tmpConnection.createStatement();
        tmpStatement.executeUpdate("INSERT INTO sm_publications (id, conference_id, journal_id, title, title_hash, abstract, publ_year, doi, created, modified) values "
                                                             + "(1, 1, 1, 'title 1', 1234, 'abstract 1', 2014, '1234-5678', NOW(), NOW())");

        tmpStatement.executeUpdate("INSERT INTO sm_conferences (id, full_name, full_name_hash, short_name, created, modified) values "
                                                            + "(1, 'full name 1', 5678, 'short name 1', NOW(), NOW())");
        tmpConnection.commit();
        tmpConnection.close();
    }

    @Before
    public void connect() {
        connection = hsqldb.createConnection();
    }

    @Test
    public void selectPublication() throws Exception {
        // just check that the database has been set up correctly
        ResultSet tmpResultSet = connection
                .createStatement()
                .executeQuery("SELECT conference_id, journal_id, title, title_hash, abstract, publ_year, doi, created, modified  FROM sm_publications");
        Assert.assertTrue(tmpResultSet.next());

        tmpResultSet = connection
                .createStatement()
                .executeQuery("SELECT full_name, full_name_hash,short_name, created, modified FROM sm_conferences");
        Assert.assertTrue(tmpResultSet.next());

        tmpResultSet = connection
                .createStatement()
                .executeQuery("SELECT * FROM sm_conferences WHERE sm_conferences.id = 1");
        Assert.assertTrue(tmpResultSet.next());
    }
    
    @Test
    public void runImport() throws Exception {
        solr.runDataImportHandler("/dataimport");
        Assert.assertEquals(1, solr.query("*:*").getResults().getNumFound());
    }

    @After
    public void disconnect() throws SQLException {
        connection.close();
    }

    public static class Publication {
        @Field
        public int id;
        @Field
        public String conference;
        @Field
        public int year;
        @Field
        public String doi;

        public Publication() {
            super();
        }
        
    }

}
