package de.cheffe.solrsample;

import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.JettySolrTestHarness;

public class ShardUnificationTest {

	@ClassRule
	public static JettySolrTestHarness<UnifiedDocument> solr = new JettySolrTestHarness<>("shard-unification");

	@BeforeClass
	public static void setupShard1() {
		solr.addBeanToIndex(new UnifiedDocument(1, "title 1", null, "description 1"), "shard-1");
	}

	@BeforeClass
	public static void setupShard2() {
		solr.addBeanToIndex(new UnifiedDocument(2, "title 2", "abstract 2", null), "shard-2");
	}

	@Test
	public void queryForUnifiedSchema() {
		QueryResponse response = solr.query("title");
		Assert.assertEquals(2, response.getResults().getNumFound());

		for (UnifiedDocument unifiedDoc : response.getBeans(UnifiedDocument.class)) {
			if (unifiedDoc.id == 1) {
				Assert.assertEquals("title 1", unifiedDoc.title);
				Assert.assertEquals(null, unifiedDoc.abstractText);
				Assert.assertEquals("description 1", unifiedDoc.description);
			} else if (unifiedDoc.id == 2) {
				Assert.assertEquals("title 2", unifiedDoc.title);
				Assert.assertEquals("abstract 2", unifiedDoc.abstractText);
				Assert.assertEquals(null, unifiedDoc.description);
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
