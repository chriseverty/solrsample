package de.cheffe.solrsample;

import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;


public class PingTest {

	@ClassRule
	public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>();

	@Test
	public void ping() throws Exception {
		SolrPingResponse tmpPingResponse = solr.ping();
		Assert.assertEquals("OK", tmpPingResponse.getResponse().get("status"));
	}

}
