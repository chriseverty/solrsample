package de.cheffe.solrsample;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrTestHarness;


public class EdgeNGramTest {

	@ClassRule
	public static EmbeddedSolrTestHarness<Object> h = new EmbeddedSolrTestHarness<>();

	@Test
	public void analyse() throws Exception {
	    Assert.assertArrayEquals(
	            new String[] {"samp", "sampl", "sample"}, 
	            h.analyseQueryTime("ngram", "sample one").toArray());

	       Assert.assertArrayEquals(
	                new String[] {"jone", "jones"}, 
	                h.analyseQueryTime("ngram", "jones").toArray());
	}

}
