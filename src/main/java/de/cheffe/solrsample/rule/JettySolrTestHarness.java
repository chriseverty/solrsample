package de.cheffe.solrsample.rule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.JettySolrRunner;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.FieldAnalysisRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.rules.ExternalResource;
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
public class JettySolrTestHarness<T extends Object> extends ExternalResource {

	private static final Logger LOG = LoggerFactory.getLogger(JettySolrTestHarness.class);

	public HttpSolrServer server;
	private JettySolrRunner jettySolr;

	private String pathToSolrXml;
	private String defaultCore;
	private String baseUrl;

	/**
	 * Defines that the solr.xml is to be found within resource package 'solr'.
	 */
	public JettySolrTestHarness() {
		this(null, "/solr/solr.xml");
	}

	/**
	 * @param aDefaultCore
	 *            the core to use as default
	 */
	public JettySolrTestHarness(String aDefaultCore) {
		this(aDefaultCore, "/solr/solr.xml");
	}

	/**
	 * @para aDefaultCore the core to use as default
	 * @param aPathToSolrXml
	 *            the path to the solr.xml within your resources - e.g.
	 *            "/solr/solr.xml" if a solr.xml is to be found within a package
	 *            named 'solr'
	 */
	public JettySolrTestHarness(String aDefaultCore, String aPathToSolrXml) {
		super();
		defaultCore = aDefaultCore;
		pathToSolrXml = aPathToSolrXml;
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		LOG.info("start embedded solr");
		String tmpSolrXmlPath = JettySolrTestHarness.class.getResource(pathToSolrXml).getFile();
		File tmpSolrXml = new File(tmpSolrXmlPath);
		File tmpSolrHomeDir = tmpSolrXml.getParentFile();

		int tmpPort = 8080;
		String tmpContext = "/solr";
		jettySolr = new JettySolrRunner(tmpSolrHomeDir.getAbsolutePath(), tmpContext, tmpPort);
		jettySolr.start(true);

		baseUrl = "http://localhost:8080/solr/";
		
		if (defaultCore != null) {
			server = new HttpSolrServer(baseUrl + defaultCore);
		} else {
			server = new HttpSolrServer(baseUrl);
		}

		if(server.ping().getStatus() != 0) {
			LOG.warn("Solr server has a problem");
		}
		
		clearIndexAll();
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

	/**
	 * Adds a given list of beans to the index and commits them.
	 * 
	 * @param aBeans
	 *            the beans to add
	 */
	public void addBeanToIndex(T aBean) {
		LOG.info("adding document to index " + aBean);
		try {
			server.addBean(aBean);
			server.commit();
		} catch (IOException | SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	public void addBeanToIndex(T aBean, String aCoreName) {
		LOG.info("adding document " + aBean + " to core " + aCoreName);
		try {
			HttpSolrServer tmpServer = getServer(aCoreName);
			tmpServer.addBean(aBean);
			tmpServer.commit();
		} catch (IOException | SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	private HttpSolrServer getServer(String aCoreName) {
		HttpSolrServer tmpServer = server;
		if (!aCoreName.equals(defaultCore)) {
			tmpServer = new HttpSolrServer("http://localhost:8080/solr/" + aCoreName);
		}
		return tmpServer;
	}
	
	/**
	 * Adds a given list of beans to the index and commits them.
	 * 
	 * @param aBeans
	 *            the beans to add
	 */
	public void addBeansToIndex(List<T> aBeans) {
		LOG.info("adding documents to index " + aBeans);
		try {
			server.addBeans(aBeans);
			server.commit();
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Delete all documents from the index of the {@link #defaultCore}.
	 */
	public void clearIndex() {
		LOG.info("clearing index");
		try {
			server.deleteByQuery("*:*");
			server.commit();
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Delete all documents from the index of the given core.
	 */
	public void clearIndex(String aCore) {
		LOG.info("clearing index of core " + aCore);
		try {
			HttpSolrServer tmpServer = getServer(aCore);
			tmpServer.deleteByQuery("*:*");
			tmpServer.commit();
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Delete all documents from the index of the all cores.
	 */
	public void clearIndexAll() {
		LOG.info("clearing all indexes");
		try {
			// Request core list
			CoreAdminRequest tmpCoreRequest = new CoreAdminRequest();
			tmpCoreRequest.setAction(CoreAdminAction.STATUS);
			HttpSolrServer tmpServer = new HttpSolrServer(baseUrl);
			CoreAdminResponse tmpCores = tmpCoreRequest.process(tmpServer);

			// List of the cores
			for (int i = 0; i < tmpCores.getCoreStatus().size(); i++) {
				clearIndex(tmpCores.getCoreStatus().getName(i));
			}
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param aQuery
	 *            the query to ask the server for
	 * @return the list of T that are contained in the response
	 */
	public List<T> query(String aQuery, Class<T> aClass) {
		SolrQuery tmpQuery = new SolrQuery(aQuery);
		try {
			LOG.info("query: " + ClientUtils.toQueryString(tmpQuery, false));
			QueryResponse tmpResponse = server.query(tmpQuery);
			return (List<T>) tmpResponse.getBeans(aClass);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

    public QueryResponse query(SolrQuery aQuery) {
        LOG.info("query: " + ClientUtils.toQueryString(aQuery, false));
        try {
            return server.query(aQuery);
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }
    }
	
	/**
	 * @param aQuery
	 *            the query to ask the server for
	 * @return the list of T that are contained in the response
	 */
	public List<T> query(SolrQuery aQuery, Class<T> aClass) {
		try {
			LOG.info("query: " + ClientUtils.toQueryString(aQuery, false));
			QueryResponse tmpResponse = server.query(aQuery);
			return (List<T>) tmpResponse.getBeans(aClass);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param aQuery
	 *            the query to ask the server for
	 * @return the QueryResponse
	 */
	public QueryResponse query(String aQuery) {
		SolrQuery tmpQuery = new SolrQuery(aQuery);
		try {
			LOG.info("query: " + ClientUtils.toQueryString(tmpQuery, false));
			return server.query(tmpQuery);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param aFieldType
	 *            the name of the field type whose analyser shall be taken to
	 *            compute the given raw text
	 * @param aRawText
	 *            the raw text to analyse
	 * @return the tokens that would be placed in the index
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> analyseIndexTime(String aFieldType, String aRawText) throws SolrServerException, IOException {
		FieldAnalysisRequest tmpRequest = new FieldAnalysisRequest();
		tmpRequest.setFieldTypes(Arrays.asList(aFieldType));
		tmpRequest.setFieldValue(aRawText);

		NamedList<Object> tmpResponse = server.request(tmpRequest);
		tmpResponse = (NamedList<Object>) tmpResponse.get("analysis");
		tmpResponse = (NamedList<Object>) tmpResponse.get("field_types");
		tmpResponse = (NamedList<Object>) tmpResponse.get(aFieldType);
		tmpResponse = (NamedList<Object>) tmpResponse.get("index");
		int tmpSize = tmpResponse.size() - 1;
		ArrayList<SimpleOrderedMap> tmpList = (ArrayList<SimpleOrderedMap>) tmpResponse.getVal(tmpSize);
		ArrayList<String> tmpResult = new ArrayList<String>(tmpList.size());
		for (SimpleOrderedMap tmpObj : tmpList) {
			tmpResult.add(tmpObj.get("text").toString());
		}
		return tmpResult;
	}

	/**
	 * @param aFieldType
	 *            the name of the field type whose analyser shall be taken to
	 *            compute the given raw text
	 * @param aRawText
	 *            the raw text to analyse
	 * @return the tokens that would used to query against the index
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> analyseQueryTime(String aFieldType, String aRawText) throws SolrServerException, IOException {
		FieldAnalysisRequest tmpRequest = new FieldAnalysisRequest();
		tmpRequest.setFieldTypes(Arrays.asList(aFieldType));
		tmpRequest.setQuery(aRawText);

		NamedList<Object> tmpResponse = server.request(tmpRequest);
		tmpResponse = (NamedList<Object>) tmpResponse.get("analysis");
		tmpResponse = (NamedList<Object>) tmpResponse.get("field_types");
		tmpResponse = (NamedList<Object>) tmpResponse.get(aFieldType);
		tmpResponse = (NamedList<Object>) tmpResponse.get("query");
		int tmpSize = tmpResponse.size() - 1;
		ArrayList<SimpleOrderedMap> tmpList = (ArrayList<SimpleOrderedMap>) tmpResponse.getVal(tmpSize);
		ArrayList<String> tmpResult = new ArrayList<String>(tmpList.size());
		for (SimpleOrderedMap tmpObj : tmpList) {
			tmpResult.add(tmpObj.get("text").toString());
		}
		return tmpResult;
	}

	public void runDataImportHandler(String aHandlerName) throws Exception {
		ModifiableSolrParams tmpParams = new ModifiableSolrParams();
		tmpParams.set("command", "full-import");
		tmpParams.set("clean", true);
		tmpParams.set("commit", true);
		tmpParams.set("optimize", false);

		UpdateRequest tmpRequest = new UpdateRequest(aHandlerName);
		tmpRequest.setParams(tmpParams);
		tmpRequest.process(server);

		ModifiableSolrParams tmpStatusParams = new ModifiableSolrParams();
		tmpStatusParams.set("command", "status");
		String tmpStatus = "";
		do {
			LOG.info("waiting for import to finish, status was " + tmpStatus);
			Thread.sleep(500);
			UpdateRequest tmpStatusRequest = new UpdateRequest(aHandlerName);
			tmpStatusRequest.setParams(tmpParams);
			UpdateResponse tmpStatusResponse = tmpStatusRequest.process(server);
			tmpStatus = tmpStatusResponse.getResponse().get("status").toString();
		} while ("busy".equals(tmpStatus));
		LOG.info("import done");
	}

}