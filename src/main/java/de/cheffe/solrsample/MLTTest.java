package de.cheffe.solrsample;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.Field;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class MLTTest {

    @ClassRule
    public static EmbeddedSolrServerResource<Document> solr = new EmbeddedSolrServerResource<>("mlt");

    @BeforeClass
    public static void setupDocuments() {
        List<Document> docs = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            docs.add(new Document(i, "title " + i, "description to title " + i));
        }
        solr.addBeansToIndex(docs);
    }

    /**
     * Shows a currently pending solr bug.
     */
    @Test
    public void queryMoreLikeThisTerms() throws Exception {
        SolrQuery query = new SolrQuery("title");
        query.set("mlt", true);
        query.set("mlt.fl", "title description");
        query.set("mlt.count", 100);
        query.setRows(5);
        try {
            solr.query(query);
        } catch(RuntimeException e) {
            Assert.assertTrue(e.getCause().getMessage().endsWith("EarlyTerminatingCollectorException"));
            return;
        }
        Assert.fail();
    }

    public static class Document {
        @Field
        public int id;
        @Field
        public String title;
        @Field
        public String description;

        public Document() {
            super();
        }

        public Document(int id, String title, String description) {
            super();
            this.id = id;
            this.title = title;
            this.description = description;
        }

    }

}
