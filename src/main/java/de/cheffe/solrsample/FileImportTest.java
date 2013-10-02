package de.cheffe.solrsample;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrTestHarness;

public class FileImportTest {

    @ClassRule
    public static EmbeddedSolrTestHarness<Object> h = new EmbeddedSolrTestHarness<>();

    @Test
    public void runFileImport() throws Exception {
        h.runDataImportHandler("/file-import");

        Assert.assertEquals("sample,one", StringUtils.join(h.analyseIndexTime("text", "sample one"), ","));

        System.out.println(h.query("*:*").toString());
        System.out.println(h.query("rawLine:sample").toString());
    }
}
