package de.cheffe.solrsample;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class FileImportTest {

    @ClassRule
    public static EmbeddedSolrServerResource<Object> h = new EmbeddedSolrServerResource<>();

    @Test
    public void runFileImport() throws Exception {
        h.runDataImportHandler("/file-import");

        Assert.assertEquals("sample,one", StringUtils.join(h.analyseIndexTime("text", "sample one"), ","));

        System.out.println(h.query("*:*").toString());
        System.out.println(h.query("rawLine:sample").toString());
    }
}
