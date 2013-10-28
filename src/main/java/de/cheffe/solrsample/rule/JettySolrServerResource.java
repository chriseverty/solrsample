package de.cheffe.solrsample.rule;

import java.io.File;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
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
public class JettySolrServerResource<T extends Object> extends AbstractSolrServerResource<T> {

	static final Logger LOG = LoggerFactory.getLogger(JettySolrServerResource.class);

	private HttpSolrServer server;
	private HttpSolrServer rootServer;
	private JettySolrRunner jettySolr;

	private String pathToSolrXml;
	private String baseUrl;

	/**
	 * Defines that the solr.xml is to be found within resource package 'solr'.
	 */
	public JettySolrServerResource() {
		this(null, "/solr/solr.xml");
	}

	/**
	 * @param aDefaultCore
	 *            the core to use as default
	 */
	public JettySolrServerResource(String aDefaultCore) {
		this(aDefaultCore, "/solr/solr.xml");
	}

	/**
	 * @para aDefaultCore the core to use as default
	 * @param aPathToSolrXml
	 *            the path to the solr.xml within your resources - e.g.
	 *            "/solr/solr.xml" if a solr.xml is to be found within a package
	 *            named 'solr'
	 */
	public JettySolrServerResource(String aDefaultCore, String aPathToSolrXml) {
		super();
		defaultCore = aDefaultCore;
		pathToSolrXml = aPathToSolrXml;
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		LOG.info("start embedded solr");
		String tmpSolrXmlPath = JettySolrServerResource.class.getResource(pathToSolrXml).getFile();
		File tmpSolrXml = new File(tmpSolrXmlPath);
		File tmpSolrHomeDir = tmpSolrXml.getParentFile();

		int tmpPort = 8080;
		String tmpContext = "/solr";
		jettySolr = new JettySolrRunner(tmpSolrHomeDir.getAbsolutePath(), tmpContext, tmpPort);
		jettySolr.start(true);

		baseUrl = "http://localhost:8080/solr/";
		if (defaultCore != null) {
			baseUrl += defaultCore;
		}
		server = new HttpSolrServer(baseUrl);

		rootServer = new HttpSolrServer("http://localhost:8080/solr");

		if (server.ping().getStatus() != 0) {
			LOG.warn("Solr server has a problem");
		}

		clearIndexAll();
	}

	public String getURL() {
		return baseUrl;
	}

	@Override
	protected void after() {
		super.after();
		LOG.info("shutdown embedded solr");
		server.shutdown();
		try {
			jettySolr.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected SolrServer switchCore(String aCoreName) {
		if (!server.getBaseURL().endsWith(aCoreName)) {
			server = new HttpSolrServer("http://localhost:8080/solr/" + aCoreName);
		}
		return server;
	}

	@Override
	protected SolrServer getRootServer() {
		return rootServer;
	}

	@Override
	protected SolrServer getServer() {
		return server;
	}

}