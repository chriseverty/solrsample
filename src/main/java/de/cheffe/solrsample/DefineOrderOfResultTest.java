package de.cheffe.solrsample;


import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class DefineOrderOfResultTest {

	@ClassRule
	public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("core-1");

	@BeforeClass
	public static void setupDocuments() {
	    solr.addToIndex(
            createDocument(3),
	        createDocument(5),
	        createDocument(6),
	        createDocument(10)
		);
	}

	private static SolrInputDocument createDocument(int tmpId) {
		SolrInputDocument inputDocument = new SolrInputDocument();
		inputDocument.addField("id", Integer.valueOf(tmpId));
		return inputDocument;
	}

	@Test
	public void sortWithBoostQuery() {
		SolrQuery query = new SolrQuery("id:5 OR id:3 OR id:10 OR id:6");
		query.set("bq", "id:5^4 id:3^3 id:10^2");

		QueryResponse response = solr.query(query);
		Assert.assertEquals(4, response.getResults().getNumFound());
		Assert.assertEquals(5, response.getResults().get(0).getFieldValue("id"));
		Assert.assertEquals(3, response.getResults().get(1).getFieldValue("id"));
		Assert.assertEquals(10, response.getResults().get(2).getFieldValue("id"));
		Assert.assertEquals(6, response.getResults().get(3).getFieldValue("id"));
	}

    @Test
    public void sortWithTermBoosting() {
        SolrQuery query = new SolrQuery("id:5^4 OR id:3^3 OR id:10^2 OR id:6");

        QueryResponse response = solr.query(query);
        Assert.assertEquals(4, response.getResults().getNumFound());
        Assert.assertEquals(5, response.getResults().get(0).getFieldValue("id"));
        Assert.assertEquals(3, response.getResults().get(1).getFieldValue("id"));
        Assert.assertEquals(10, response.getResults().get(2).getFieldValue("id"));
        Assert.assertEquals(6, response.getResults().get(3).getFieldValue("id"));
    }
}
