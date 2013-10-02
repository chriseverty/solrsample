package de.cheffe.solrsample;

import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.beans.Field;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrTestHarness;

public class SolrMappingTest {

    @ClassRule
    public static EmbeddedSolrTestHarness<SampleDocument> h = new EmbeddedSolrTestHarness<>();

    @Test
    public void add() throws Exception {
        h.clearIndex();

        // adding a single document
        SampleDocument document = new SampleDocument(1, "title 1");
        h.addBeanToIndex(document);

        // adding multiple documents
        List<SampleDocument> documents = Arrays.asList(
                new SampleDocument(2, "title 2"), 
                new SampleDocument(3, "title 3"));
        h.addBeansToIndex(documents);
        
        // get the response as List of POJO type
        List<SampleDocument> foundDocuments = h.query("*:*", SampleDocument.class);
        Assert.assertEquals("title 1", foundDocuments.get(0).getTitle());
    }

    
    public static class SampleDocument {

        @Field
        public int id;
        
        private String title;

            public SampleDocument() {
                // required for solrj to make an instance
            }
            
            public SampleDocument(int id, String title) {
                this.id = id;
                this.title = title;
            }
        
            public String getTitle() {
                return title;
            }
            
            @Field("title")
            public void setTitle(String title) {
                this.title = title;
            }
        
    }
    
}
