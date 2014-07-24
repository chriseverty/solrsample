package de.cheffe.solrsample.rule;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.lucene.store.Directory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrException;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.DirectoryFactory;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.DirectoryFactory.DirContext;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Resource for solr integration tests. It will use the configuration stored
 * within a given solr.xml to start up.
 * </p>
 * <p>
 * To manage the content of the index use
 * </p>
 * <ul>
 * <li>{@link #addBeanToIndex(Object)}
 * <li>{@link #addBeansToIndex(List)}
 * <li>{@link #clearIndex()}
 * </ul>
 * <p>
 * To query for documents use
 * </p>
 * <ul>
 * <li> {@link #query(String)}
 * </ul>
 * <p>
 * To test analyzers use
 * </p>
 * <ul>
 * <li>analysis at index time {@link #analyseIndexTime(String, String)}
 * <li>analysis at query time {@link #analyseQueryTime(String, String)}
 * </ul>
 * 
 * @author cheffe
 * 
 * @param <T>
 */
public class EmbeddedSolrServerResource<T extends Object> extends AbstractSolrServerResource<T> {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedSolrServerResource.class);

    private EmbeddedSolrServer server;
    private CoreContainer container;
    private String pathToSolrXml;

    static {
        DOMConfigurator.configure("src/main/java/log4j.xml");
    }

    /**
     * Defines that the solr.xml is to be found within resource package 'solr'.
     */
    public EmbeddedSolrServerResource() {
        this(null, "/solr/solr.xml");
    }

    /**
     * @param aDefaultCore
     *            the core to use as default
     */
    public EmbeddedSolrServerResource(String aDefaultCore) {
        this(aDefaultCore, "/solr/solr.xml");
    }

    /**
     * @para aDefaultCore the core to use as default
     * @param aPathToSolrXml
     *            the path to the solr.xml within your resources - e.g.
     *            "/solr/solr.xml" if a solr.xml is to be found within a package
     *            named 'solr'
     */
    public EmbeddedSolrServerResource(String aDefaultCore, String aPathToSolrXml) {
        super();
        defaultCore = aDefaultCore;
        pathToSolrXml = aPathToSolrXml;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        LOG.info("start embedded solr");
        String tmpSolrXmlPath = EmbeddedSolrServerResource.class.getResource(pathToSolrXml).getFile();
        File tmpSolrXml = new File(tmpSolrXmlPath);
        File tmpSolrHomeDir = tmpSolrXml.getParentFile();
        container = CoreContainer.createAndLoad(tmpSolrHomeDir.getAbsolutePath(), tmpSolrXml);
        if (defaultCore == null) {
            defaultCore = container.getDefaultCoreName();
        }
        server = new EmbeddedSolrServer(container, defaultCore);
        clearIndex();
    }

    @Override
    protected void after() {
        super.after();
        LOG.info("shutdown embedded solr");
        server.shutdown();
        container.shutdown();
    }

    @Override
    protected SolrServer getServer() {
        return server;
    }

    @Override
    protected SolrServer switchCore(String aCoreName) {
        container.swap(defaultCore, aCoreName);
        return server;
    }

    @Override
    protected SolrServer getRootServer() {
        return server;
    }

    private SolrCore getDefaultCore() {
        return container.getCore(defaultCore);
    }

    public IndexSchema getDefaultCoreSchema() {
        return getDefaultCore().getLatestSchema();
    }

    /**
     * @return the size of the default index in bytes
     */
    public long getDefaultIndexSize() {
        try {
            SolrCore core = getDefaultCore();
            Directory dir = core.getDirectoryFactory().get(core.getIndexDir(), DirContext.DEFAULT, core.getSolrConfig().indexConfig.lockType);
            try {
                return DirectoryFactory.sizeOfDirectory(dir);
            } finally {
                core.getDirectoryFactory().release(dir);
            }
        } catch (IOException e) {
            SolrException.log(LOG, "IO error while trying to get the size of the Directory", e);
        }
        return 0;
    }

}