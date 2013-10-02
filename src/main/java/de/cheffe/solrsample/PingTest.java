package de.cheffe.solrsample;

import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrTestHarness;


public class PingTest {

	@ClassRule
	public static EmbeddedSolrTestHarness<Object> h = new EmbeddedSolrTestHarness<>();

	@Test
	public void ping() throws Exception {
		SolrPingResponse tmpPingResponse = h.server.ping();
		Assert.assertEquals("OK", tmpPingResponse.getResponse().get("status"));
	}

}
