package de.cheffe.solrsample;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;


public class EdgeNGramTest {

	@ClassRule
	public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>();

	@Test
	public void analyse() {
	    Assert.assertArrayEquals(
	            new String[] {"samp", "sampl", "sample"}, 
	            solr.analyseQueryTime("ngram", "sample one").toArray());

	       Assert.assertArrayEquals(
	                new String[] {"jone", "jones"}, 
	                solr.analyseQueryTime("ngram", "jones").toArray());
	}
	
	@Test
	public void analyseTitleSuggestion() {
	    System.out.println(solr.analyseQueryTime("title_suggestion", "TDK iClassic iPod Dock"));
	}

}
