package de.cheffe.solrsample;

import static java.math.BigDecimal.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class StringVsTrieIntTest {
   
    private static final Logger LOG = LoggerFactory.getLogger(StringVsTrieIntTest.class);

    @ClassRule
    public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("string-vs-trieint");

    int batchSize = 25 * 1000;
    int documentCount = 1000 * 1000;

    @Test
    public void checkString() {
        setupIndex("id_string");
        runBenchmark("id_string");
    }

    @Test
    public void checkTriInt() {
        setupIndex("id_tint");
        runBenchmark("id_tint");
    }
    
    @Test
    public void checkInt() {
        setupIndex("id_int");
        runBenchmark("id_int");
    }

    private void setupIndex(String aField) {
        LOG.info("setting up index for: {}", aField);
        solr.clearIndex();
        
        solr.setCommitImmediateAfterAdd(false);

        List<SolrInputDocument> docs = new ArrayList<>(batchSize);
        for (int i = 1; i <= documentCount; i++) {

            SolrInputDocument document = new SolrInputDocument();
            document.setField(aField, i);
            docs.add(document);

            if (i % batchSize == 0) {
                solr.addToIndex(docs);
                LOG.debug("done with {} docs", i);
                docs = new ArrayList<>(batchSize);
            }
        }
        solr.commit();
        solr.optimize();
    }

    private void runBenchmark(String aField) {
        LOG.info("running benchmark for: {}", aField);
        long querySum = 0;
        long filterSum = 0;
        int testQueryCount = 1000;
        for (int i = 1; i <= testQueryCount; i++) {
            solr.commit();
            querySum += solr.query(aField + ":" + i).getQTime();
            
            solr.commit();
            SolrQuery query = new SolrQuery("*:*");
            query.setFilterQueries(aField + ":" + (i + testQueryCount));
            filterSum += solr.query(query).getQTime();
        }

        LOG.info("+--------------------------------------------------------+");
        LOG.info("| field under test: {}", aField);
        LOG.info("| size of index (bytes): {}", solr.getDefaultIndexSize());
        String avgQueryTime = valueOf(querySum).divide(valueOf(testQueryCount)).toPlainString();
        LOG.info("| average response time (ms) for query: {} ", avgQueryTime);
        String avgFilterTime = valueOf(filterSum).divide(valueOf(testQueryCount)).toPlainString();
        LOG.info("| average response time (ms) for filter: {} ", avgFilterTime);
        LOG.info("+--------------------------------------------------------+");
    }

}
