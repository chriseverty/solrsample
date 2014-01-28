package de.cheffe.solrsample;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class RollbackTest {

    @ClassRule
    public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("rollback");
    
    private AtomicInteger id = new AtomicInteger();

    @Test
    public void rollback() {
        solr.addToIndex(createDocument()); // does also a hard-commit, each time
        solr.addToIndex(createDocument());
        solr.addToIndex(createDocument());
        solr.addToIndex(createDocument());
        solr.addToIndex(createDocument());
        solr.addToIndex(createDocument());
        printTitleOfDocsInIndex();
        
        solr.requestRecovery();
        printTitleOfDocsInIndex();
        
        solr.requestRecovery();
        printTitleOfDocsInIndex();
        
        solr.requestRecovery();
        printTitleOfDocsInIndex();
        
        solr.requestRecovery();
        printTitleOfDocsInIndex();
        
        solr.requestRecovery();
        printTitleOfDocsInIndex();
        
        solr.requestRecovery();
        printTitleOfDocsInIndex();
    }

    private void printTitleOfDocsInIndex() {
        Iterator<SolrDocument> tmpDocs = solr.query("*:*").getResults().iterator();
        System.out.println("title");
        while(tmpDocs.hasNext()) {
            System.out.println(tmpDocs.next().getFirstValue("title"));
        }
    }

    private SolrInputDocument createDocument() {
        SolrInputDocument tmpDoc = new SolrInputDocument();
        tmpDoc.setField("id", Integer.valueOf(id.getAndIncrement()));
        tmpDoc.setField("title", "title " + id.intValue());
        return tmpDoc;
    }

}
