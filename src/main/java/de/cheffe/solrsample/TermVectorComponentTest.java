package de.cheffe.solrsample;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;


public class TermVectorComponentTest {

	@ClassRule
	public static EmbeddedSolrServerResource<Document> solr = new EmbeddedSolrServerResource<>("term-vector");

	@BeforeClass
	public static void setupShards() throws SolrServerException, IOException {
		solr.addBeanToIndex(new Document(1, "title 1", "description 1"));
		solr.addBeanToIndex(new Document(2, "title 2", "description 2"));
	}

	@Test
	public void asdf() {
		SolrQuery query = new SolrQuery("title");
		query.set("tv", true);
		query.set("tv.positions", true);
		solr.query(query);
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
