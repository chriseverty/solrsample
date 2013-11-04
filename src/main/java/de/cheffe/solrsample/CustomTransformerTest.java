package de.cheffe.solrsample;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class CustomTransformerTest {
	@ClassRule
	public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("core-1");
	
	@BeforeClass
	public static void setupDocuments() {
		SolrInputDocument inputDocument = new SolrInputDocument();
		inputDocument.addField("title", "some title");
		solr.addToIndex(inputDocument);
	}
	
	@Test
	public void fetchTransformerValue() {
		SolrQuery query = new SolrQuery("*:*");
		query.addField("title");
		query.addField("[core]");
		QueryResponse response = solr.query(query);
		System.out.println(response.getResponse());
		Assert.assertTrue(response.getResponse().toString().contains("[core]=core-1"));
	}
	
	@Test
	public void doNotFetchWithTransformer() {
		SolrQuery query = new SolrQuery("*:*");
		query.addField("title");
		QueryResponse response = solr.query(query);
		System.out.println(response.getResponse());
		Assert.assertFalse(response.getResponse().toString().contains("[core]=core-1"));		
	}
}
