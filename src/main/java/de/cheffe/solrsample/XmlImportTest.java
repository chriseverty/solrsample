package de.cheffe.solrsample;

import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class XmlImportTest {

	@ClassRule
	public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("xml-import");
	
	@Test
	public void runImport() throws Exception {
		solr.runDataImportHandler("/dataimport");
	}
}
