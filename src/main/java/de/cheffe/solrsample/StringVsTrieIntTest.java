package de.cheffe.solrsample;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

import static java.math.BigDecimal.valueOf;

public class StringVsTrieIntTest {

    private static final Logger LOG = LoggerFactory.getLogger(StringVsTrieIntTest.class);

    @ClassRule
    public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("string-vs-trieint");

    int batchSize = 25 * 1000;
    int documentCount = 50 * 1000;

    @Test
    public void checkInt() {
        setupIndex("id_int");
        runBenchmark("id_int");
    }

    @Test
    public void checkTriInt() {
        setupIndex("id_tint");
        runBenchmark("id_tint");
    }

    @Test
    public void checkString() {
        setupIndex("id_string");
        runBenchmark("id_string");
    }

    private void setupIndex(String aField) {
        LOG.info("setting up index for: {}", aField);
        solr.setCommitImmediateAfterAdd(false);

        List<SolrInputDocument> docs = new ArrayList<>(batchSize);
        for (int i = 1; i <= documentCount; i++) {

            SolrInputDocument document = new SolrInputDocument();
            document.setField(aField, i);
            docs.add(document);

            if (i % batchSize == 0) {
                solr.addToIndex(docs);
                LOG.debug("done with " + i + " docs");
                docs = new ArrayList<>(batchSize);
            }
        }
        solr.commit();
        solr.optimize();
    }

    private void runBenchmark(String aField) {
        LOG.info("running benchmark for: {}", aField);
        long sum = 0;
        for (int i = 1; i <= (documentCount / batchSize) * 10; i++) {
            QueryResponse response = solr.query(aField + ":" + i);
            sum += response.getQTime();
        }

        LOG.info("+--------------------------------------------------------+");
        LOG.info("| field under test: {}", aField);
        LOG.info("| size of index (bytes): {}", solr.getDefaultIndexSize());
        String avgTime = valueOf(sum).divide(valueOf(documentCount / batchSize)).toPlainString();
        LOG.info("| average response time (ms): {} ", avgTime);
        LOG.info("+--------------------------------------------------------+");
    }

}
