package de.cheffe.solrsample;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Rule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class IpAdressTest {

    @Rule
    public EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("ip-adress");

    @Test
    public void tokens() {
        System.out.println(solr.analyseIndexTime("ip_adress", "192.168.1.2"));
    }
    
    @Test
    public void query() throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", Integer.valueOf(1));
        doc.setField("ip_adress", "192.168.1.2");
        solr.addToIndex(doc);
        
        SolrQuery query = new SolrQuery("ip_adress:192.168");
        solr.print(solr.query(query));
    }

}
