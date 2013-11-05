package de.cheffe.solrsample;


import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;


public class RussianTranslitarationTest {

	@ClassRule
	public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>("cyrillic");

	@Test
	public void transliteration() throws Exception {
	    String analysisResult = solr.analyseQueryTime("spell_text", "televidenieee").get(0);
	    Assert.assertEquals("телевидениеее", analysisResult);
	}

}