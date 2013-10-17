package de.cheffe.solrsample.runner;

import java.io.File;

import org.apache.solr.client.solrj.embedded.JettySolrRunner;

public class SolrRunner {

    public static void main(String[] args) {
        try {
            String tmpSolrHome = SolrRunner.class.getResource("/solr").getFile();
            File tmpSolrHomeDir = new File(tmpSolrHome);
            JettySolrRunner jetty = new JettySolrRunner(tmpSolrHomeDir.getAbsolutePath(), "/solr", 8080);
            jetty.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
