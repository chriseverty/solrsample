package de.cheffe.solrsample;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cheffe.solrsample.rule.JettySolrTestHarness;

public class ShardUnificationTest {

	private static final Logger LOG = LoggerFactory.getLogger(ShardUnificationTest.class);

	// start a jetty server, so that we can use solr's shard feature
	// set the unification shard as default core
	@ClassRule
	public static JettySolrTestHarness<UnifiedDocument> solr = new JettySolrTestHarness<>("shard-unification");

	@BeforeClass
	public static void setupShards() throws SolrServerException, IOException {
		// add a document to the core shard-1
		solr.addBeanToIndex(new UnifiedDocument(1, "title 1", null, "description 1"), "shard-1");
		
		// add another document to the core shard-2
		solr.addBeanToIndex(new UnifiedDocument(2, "title 2", "abstract 2", null), "shard-2");
	}

	@Test
	public void queryForUnifiedSchema() {
		LOG.info("starting test queryForUnifiedSchema");

		// check that the core shard-unification itself is empty
		Assert.assertEquals(0, solr.query("*:*").getResults().getNumFound());

		// fetch all docs via *:* query, so the two added docs should be fetched
		// now add the two cores that hold data via the shards param
		SolrQuery tmpQuery = new SolrQuery("*:*");
		tmpQuery.set("shards", "localhost:8080/solr/shard-1,localhost:8080/solr/shard-2");
		tmpQuery.set("indent", true);
		List<UnifiedDocument> response = solr.query(tmpQuery, UnifiedDocument.class);
		
		// assure that we got two docs
		Assert.assertEquals(2, response.size());

		// check that the attributes are set
		boolean tmpCheckedDoc1 = false;
		boolean tmpCheckedDoc2 = false;
		for (UnifiedDocument unifiedDoc : response) {
			if (unifiedDoc.id == 1) {
				Assert.assertEquals("title 1", unifiedDoc.title);
				Assert.assertEquals(null, unifiedDoc.abstractText);
				Assert.assertEquals("description 1", unifiedDoc.description);
				Assert.assertEquals("localhost:8080/solr/shard-1", unifiedDoc.shard);
				Assert.assertFalse(tmpCheckedDoc1);
				tmpCheckedDoc1 = true;
			} else if (unifiedDoc.id == 2) {
				Assert.assertEquals("title 2", unifiedDoc.title);
				Assert.assertEquals("abstract 2", unifiedDoc.abstractText);
				Assert.assertEquals(null, unifiedDoc.description);
				Assert.assertFalse(tmpCheckedDoc2);
				Assert.assertEquals("localhost:8080/solr/shard-2", unifiedDoc.shard);
				tmpCheckedDoc2 = true;
			} else {
				Assert.fail("unexpected doc");
			}
		}
	}
    
	public static class UnifiedDocument {
		@Field
		public int id;
		@Field
		public String title;
		@Field
		public String abstractText;
		@Field
		public String description;
		@Field("[shard]")
		public String shard;

		public UnifiedDocument() {
			super();
		}

		public UnifiedDocument(int id, String title, String abstractText, String description) {
			super();
			this.id = id;
			this.title = title;
			this.abstractText = abstractText;
			this.description = description;
		}

	}
}
