package de.cheffe.solrsample;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;
import org.apache.solr.client.solrj.beans.Field;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SolrMappingTest {

    @ClassRule
    public static EmbeddedSolrServerResource<SampleDocument> solr = new EmbeddedSolrServerResource<>("core-1");

    @Test
    public void add() throws Exception {
        solr.clearIndex();

        // adding a single document
        SampleDocument document = new SampleDocument(1, "title 1");
        solr.addBeanToIndex(document);

        // adding multiple documents
        List<SampleDocument> documents = Arrays.asList(
                new SampleDocument(2, "title 2"), 
                new SampleDocument(3, "title 3"));
        solr.addBeansToIndex(documents);
        
        // get the response as List of POJO type
        List<SampleDocument> foundDocuments = solr.query("*:*", SampleDocument.class);
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
