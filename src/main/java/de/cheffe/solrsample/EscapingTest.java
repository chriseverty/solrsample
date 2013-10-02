package de.cheffe.solrsample;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.junit.Assert;
import org.junit.Test;


public class EscapingTest {

	@Test
	public void escape() throws Exception {
	    Assert.assertEquals("\\:\\)", ClientUtils.escapeQueryChars(":)"));
	}

}
