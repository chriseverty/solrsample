package de.cheffe.solrsample;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedHSQLDBTestHarness;
import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

@FixMethodOrder
public class DynamicCategoriesTest {

    @ClassRule
    public static EmbeddedHSQLDBTestHarness hsqldb = new EmbeddedHSQLDBTestHarness();

    @ClassRule
    public static EmbeddedSolrServerResource<Document> solr = new EmbeddedSolrServerResource<>("dynamiccategories-core");

    private Connection connection;

    @BeforeClass
    public static void setupDatabase() throws Exception {
        // run liquibase to setup the database schema
        try (Connection tmpConnection = hsqldb.createConnection()) {
            File tmpChangeSet = new File("src/main/resources/database/createCategoriesDB.xml");
            Liquibase tmpLiquibase = new Liquibase(tmpChangeSet.getAbsolutePath(), new FileSystemResourceAccessor(), new JdbcConnection(tmpConnection));
            tmpLiquibase.update(null);

            tmpConnection.createStatement().executeUpdate("TRUNCATE SCHEMA public AND COMMIT");

            tmpConnection.setAutoCommit(false);

            // insert some docs, these are the entities that will be searchable later
            int tmpCountDocs = 100;
            PreparedStatement tmpDocStatement = tmpConnection.prepareStatement("INSERT INTO document(title) VALUES (?)");
            for (int i = 0; i < tmpCountDocs; i++) {
                tmpDocStatement.setString(1, "title " + i);
                tmpDocStatement.execute();
            }

            // insert some categories, these are the parents of category values
            // as such they are the name of the facets, to display to a user
            int tmpCountCategories = 10;
            PreparedStatement tmpCatStatement = tmpConnection.prepareStatement("INSERT INTO category(name) VALUES (?)");
            for (int i = 0; i < tmpCountCategories; i++) {
                tmpCatStatement.setString(1, "category " + i);
                tmpCatStatement.execute();
            }

            // insert the actual category values, these are assigned to a document
            int tmpCountCategoryValues = 20;
            PreparedStatement tmpCatValueStatement = tmpConnection.prepareStatement("INSERT INTO category_value(name, category_id) VALUES (?, ?)");
            for (int i = 0; i < tmpCountCategoryValues; i++) {
                tmpCatValueStatement.setString(1, "value " + i);
                tmpCatValueStatement.setInt(2, i % tmpCountCategories);
                tmpCatValueStatement.execute();
            }

            // create connections between documents and category values
            int tmpCountDocValues = 20;
            PreparedStatement tmpDocValueStatement = tmpConnection.prepareStatement("INSERT INTO document_category(category_value_id, document_id) VALUES (?, ?)");
            for (int i = 0; i < tmpCountDocValues; i++) {
                tmpDocValueStatement.setInt(1, i % tmpCountCategoryValues);
                tmpDocValueStatement.setInt(2, i % tmpCountDocs);
                tmpDocValueStatement.execute();
            }
            
            tmpConnection.commit();
        }
    }

    @Before
    public void connect() {
        connection = hsqldb.createConnection();
    }

    @Test
    public void selectDocument() throws Exception {
        ResultSet tmpResultSet = connection.createStatement().executeQuery("SELECT count(*) FROM document");
        Assert.assertTrue(tmpResultSet.next());

        int tmpCount = tmpResultSet.getInt(1);
        Assert.assertEquals(100, tmpCount);
    }

    @Test
    public void runImport() throws Exception {
        solr.runDataImportHandler("/dataimport");
        QueryResponse queryResponse = solr.query("*:*");
        Assert.assertEquals(100, queryResponse.getResults().getNumFound());
    }

    @Test
    public void facet() throws Exception {
        SolrQuery query = new SolrQuery("*:*");
        query.setFacet(true);
        query.setFacetPrefix("categories", "category 1");
        query.setFilterQueries("{!raw f=categories}category 1");
        QueryResponse queryResponse = solr.query(query);
        Assert.assertEquals(2, queryResponse.getResults().getNumFound());
    }
    
    @After
    public void disconnect() throws SQLException {
        connection.close();
    }

    public static class Document {
        @Field
        public int id;
        @Field
        public String title;
        @Field
        public String[] categories;
    }

}
