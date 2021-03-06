package de.cheffe.solrsample;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BitMaskTest {

    @ClassRule
    public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("bitmask");

    private int numberOfCampaigns = 6;

    @Test
    public void bitmask() throws Exception {
        int numberOfDocs = (int) Math.pow(2, numberOfCampaigns);
        List<SolrInputDocument> docs = new ArrayList<>(1000);

        for (int i = 0; i <= numberOfDocs; i++) {

            List<Integer> integers = new ArrayList<>();
            List<String> strings = new ArrayList<>();

            String binaryString = Integer.toBinaryString(i);

            for (int c = 0; c < binaryString.length(); c++) {
                if (binaryString.charAt(c) == '1') {
                    integers.add(c);
                    strings.add(c + "_1");
                } else {
                    strings.add(c + "_0");
                }
            }

            SolrInputDocument inputDocument = new SolrInputDocument();
            inputDocument.setField("id", Integer.valueOf(i));
            inputDocument.setField("ints", integers);
            inputDocument.setField("strings", strings);

            docs.add(inputDocument);

            if (docs.size() == 1000) {
                solr.addToIndex(docs);
                docs.clear();
            }
        }
        solr.addToIndex(docs);

        System.out.println(solr.query("ints:(1 OR 2)").getResults().getNumFound());
        System.out.println(solr.query("strings:(1_1 OR 2_1)").getResults().getNumFound());
        System.out.println(solr.query("strings:((1_1 OR 2_1) AND 4_0)").getResults().getNumFound());
    }

    @Test
    public void hashField() {
        // setup
        SolrInputDocument document = new SolrInputDocument();
        document.setField("id", 1);
        document.setField("hash", "004143737f7f7f7f");
        solr.addToIndex(document);

        SolrInputDocument document2 = new SolrInputDocument();
        document2.setField("id", 2);
        document2.setField("hash", "0041417f7f7f7f7f");
        solr.addToIndex(document2);

        SolrQuery query = new SolrQuery("hash:0041417f7f7f7f7f~0.8");
        query.setFields("hash", "strdist(\"0041417f7f7f7f7f\", hash, edit)");
        solr.print(solr.query(query));
    }

}
