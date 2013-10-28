package de.cheffe.solrsample;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.JettySolrServerResource;

/**
 * @author cheffe
 */
public class SelfImportTest {

	//private static final Logger LOG = LoggerFactory.getLogger(SelfImportTest.class);

	@ClassRule
	public static JettySolrServerResource<Document> solr = new JettySolrServerResource<>("self-import");

	@BeforeClass
	public static void setupShards() throws SolrServerException, IOException {
		solr.addBeanToIndex(new Document(1, 12, null));
		solr.addBeanToIndex(new Document(2, 16, null));
	}

	@Test
	public void selfImport() throws Exception {
		// fetch all docs and show that update_field is empty
		List<Document> docs = solr.query("*:*", Document.class);
		Assert.assertEquals(2, docs.size());
		for (Document document : docs) {
			Assert.assertNull(document.update_field);
		}
		
		// run the import as configured
		int tmpProcessed = solr.runDataImportHandler("/dataimport");
		Assert.assertEquals(2, tmpProcessed);
		
		// check that the docs got updated
		List<Document> updatedDocs = solr.query("*:*", Document.class);
		Assert.assertEquals(2, updatedDocs.size());
		for (Document document : updatedDocs) {
			Assert.assertEquals("The age is: " + document.age, document.update_field);
		}
	}
	
	public static class Document {
		@Field
		public int id;
		@Field
		public int age;
		@Field
		public String update_field;

		public Document() {
			super();
		}

		Document(int id, int age, String update_field) {
			super();
			this.id = id;
			this.age = age;
			this.update_field = update_field;
		}
		
	}
}
