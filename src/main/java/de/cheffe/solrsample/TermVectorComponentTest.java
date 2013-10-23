package de.cheffe.solrsample;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import de.cheffe.solrsample.rule.JettySolrServerResource;

public class TermVectorComponentTest {

	@ClassRule
	public static JettySolrServerResource<Document> solr = new JettySolrServerResource<>("term-vector");

	@BeforeClass
	public static void setupShards() throws SolrServerException, IOException {
		solr.addBeanToIndex(new Document(1, "title 1", "description 1"));
		solr.addBeanToIndex(new Document(2, "title 2", "description 2"));
	}

	@Test
	public void asdf() throws ResourceException, IOException {
		// use the solr query as builder
		SolrQuery query = new SolrQuery("title");
		query.set("tv", true);
		query.set("tv.positions", true);
		Assert.assertEquals(2, solr.query(query).getResults().getNumFound());
		
		// combine solr parameter with base url
		String tmpQueryString = solr.getURL() + ClientUtils.toQueryString(query, false);
		new ClientResource(tmpQueryString).get().write(System.out);  
	}

	public static class Document {
		@Field
		public int id;
		@Field
		public String title;
		@Field
		public String description;

		public Document() {
			super();
		}

		public Document(int id, String title, String description) {
			super();
			this.id = id;
			this.title = title;
			this.description = description;
		}

	}

}
