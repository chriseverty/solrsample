package de.cheffe.solrsample;


import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;


public class RussianTranslitarationTest {

	@ClassRule
	public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("cyrillic");

	@BeforeClass
	public static void setupDocuments() {
	    SolrInputDocument sampleDoc = new SolrInputDocument();
	    sampleDoc.addField("id", Integer.valueOf(1));
	    sampleDoc.addField("description", "телевидение");
	    sampleDoc.addField("spellcheck", "телевидение");
	    solr.addToIndex(sampleDoc);
	}
	
	@Test
	public void transliteration() throws Exception {
	    String analysisResult = solr.analyseQueryTime("spell_text", "televidenieee").get(0);
	    Assert.assertEquals("телевидениеее", analysisResult);
	}

	@Test
	public void spellcheck() {
	    SolrQuery query = new SolrQuery("televidenieee");
	    query.set("qt", "/spellCheckCompRH");
	    query.set("q", "televidenieee");
	    query.set("spellcheck", "on");
	    query.set("spellcheck.build", "true");
	    query.set("df", "description");

	    QueryResponse response = solr.query(query);
	    System.out.println("response = " + response);
	}
	
}