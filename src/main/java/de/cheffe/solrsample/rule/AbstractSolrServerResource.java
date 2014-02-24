package de.cheffe.solrsample.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.FieldAnalysisRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSolrServerResource<T extends Object> extends ExternalResource {

	static final Logger LOG = LoggerFactory.getLogger(AbstractSolrServerResource.class);

	protected String defaultCore;

	protected abstract SolrServer getServer();

	protected abstract SolrServer switchCore(String aCoreName);

	protected abstract SolrServer getRootServer();

	/**
	 * Adds a given list of beans to the index and commits them.
	 * 
	 * @param aBeans
	 *            the beans to add
	 */
	public void addBeanToIndex(T aBean) {
		LOG.info("adding document to index " + aBean);
		try {
			SolrServer tmpServer = getServer();
			tmpServer.addBean(aBean);
			commit(tmpServer);
		} catch (IOException | SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	/**
     * Adds the given beans to the index and commits them.
     * 
     * @param aBeans
     *            the beans to add
     */
    @SuppressWarnings("unchecked")
    public void addBeansToIndex(T ... aBeans) {
        LOG.info("adding documents to index " + aBeans);
        try {
            SolrServer tmpServer = getServer();
            tmpServer.addBeans(Arrays.asList(aBeans));
            commit(tmpServer);
        } catch (IOException | SolrServerException e) {
            throw new RuntimeException(e);
        }
    }
	
	public void addBeanToIndex(T aBean, String aCoreName) {
		LOG.info("adding document " + aBean + " to core " + aCoreName);
		try {
			SolrServer tmpServer = switchCore(aCoreName);
			tmpServer.addBean(aBean);
			commit(tmpServer);
			switchCore(defaultCore);
		} catch (IOException | SolrServerException e) {
			throw new RuntimeException(e);
		}
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
			SolrServer tmpServer = getServer();
			tmpServer.addBeans(aBeans);
			commit(tmpServer);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

    private void commit(SolrServer aServer) throws SolrServerException, IOException {
        LOG.info("commit");
        aServer.commit();
    }

	public void addToIndex(SolrInputDocument aInputDocument) {
		LOG.info("adding document to index " + aInputDocument);
		try {
			SolrServer tmpServer = getServer();
			tmpServer.add(aInputDocument);
			commit(tmpServer);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}		
	}

    public void addToIndex(SolrInputDocument ... aInputDocuments) {
        LOG.info("adding documents to index " + StringUtils.join(aInputDocuments, "; "));
        try {
            SolrServer tmpServer = getServer();
            tmpServer.add(Arrays.asList(aInputDocuments));
            commit(tmpServer);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }       
    }
    
    public void addToIndex(Collection<SolrInputDocument> aInputDocuments) {
        LOG.info("adding " + aInputDocuments.size() + " documents to index");
        try {
            SolrServer tmpServer = getServer();
            tmpServer.add(aInputDocuments);
            commit(tmpServer);
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
			SolrServer tmpServer = getServer();
			tmpServer.deleteByQuery("*:*");
			commit(tmpServer);
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
			SolrServer tmpServer = switchCore(aCore);
			tmpServer.deleteByQuery("*:*");
			commit(tmpServer);
			switchCore(defaultCore);
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
			CoreAdminResponse tmpCores = tmpCoreRequest.process(getRootServer());

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
			QueryResponse tmpResponse = getServer().query(tmpQuery);
			return (List<T>) tmpResponse.getBeans(aClass);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	public QueryResponse query(SolrQuery aQuery) {
		LOG.info("query: " + ClientUtils.toQueryString(aQuery, false));
		try {
			return getServer().query(aQuery);
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
			QueryResponse tmpResponse = getServer().query(aQuery);
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
			return getServer().query(tmpQuery);
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
	public List<String> analyseIndexTime(String aFieldType, String aRawText) {
		FieldAnalysisRequest tmpRequest = new FieldAnalysisRequest();
		tmpRequest.setFieldTypes(Arrays.asList(aFieldType));
		tmpRequest.setFieldValue(aRawText);

		NamedList<Object> tmpResponse;
        try {
            tmpResponse = getServer().request(tmpRequest);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
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
	public List<String> analyseQueryTime(String aFieldType, String aRawText) {
		FieldAnalysisRequest tmpRequest = new FieldAnalysisRequest();
		tmpRequest.setFieldTypes(Arrays.asList(aFieldType));
		tmpRequest.setQuery(aRawText);

		NamedList<Object> tmpResponse;
		try {
			tmpResponse = getServer().request(tmpRequest);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
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

	public int runDataImportHandler(String aHandlerName) throws Exception {
		ModifiableSolrParams tmpParams = new ModifiableSolrParams();
		tmpParams.set("command", "full-import");
		tmpParams.set("clean", false);
		tmpParams.set("commit", false);
		tmpParams.set("optimize", false);

		UpdateRequest tmpRequest = new UpdateRequest(aHandlerName);
		tmpRequest.setParams(tmpParams);

		SolrServer tmpServer = getServer();
		tmpRequest.process(tmpServer);

		ModifiableSolrParams tmpStatusParams = new ModifiableSolrParams();
		tmpStatusParams.set("command", "status");
		String tmpStatus = "busy";
		int tmpProcessed = 0;
		do {
			LOG.info("waiting for import to finish, status was busy");
			Thread.sleep(500);
			UpdateRequest tmpStatusRequest = new UpdateRequest(aHandlerName);
			tmpStatusRequest.setParams(tmpStatusParams);
			UpdateResponse tmpStatusResponse = tmpStatusRequest.process(tmpServer);
			tmpStatus = tmpStatusResponse.getResponse().get("status").toString();
			@SuppressWarnings("rawtypes")
			Map tmpMessages = (Map) tmpStatusResponse.getResponse().get("statusMessages");
			LOG.info("import status is " + tmpStatus);
			if (tmpMessages.get("Total Documents Processed") != null) {
				tmpProcessed = Integer.valueOf(tmpMessages.get("Total Documents Processed").toString());
			}
		} while ("busy".equals(tmpStatus));
		commit(tmpServer);
		LOG.info("import done");
		return tmpProcessed;
	}


    public void rollback() {
        LOG.info("rollback");
        try {
            getServer().rollback();
        } catch(SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
	
    public void requestRecovery() {
        LOG.info("requesting recovery");
        try {
            CoreAdminRequest tmpRequest = new CoreAdminRequest();
            tmpRequest.setAction(CoreAdminAction.REQUESTRECOVERY);
            tmpRequest.setCoreName(defaultCore);
            CoreAdminResponse tmpResponse = tmpRequest.process(getServer());
            System.out.println(tmpResponse);
        } catch(SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void optimize() {
        LOG.info("optimize");
        try {
            getServer().optimize();
        } catch(SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
	public SolrPingResponse ping() {
		try {
			return getServer().ping();
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

    public void print(QueryResponse response) {
        LOG.info("print results");
        SolrDocumentList results = response.getResults();
        if(results.getNumFound() == 0) {
            System.out.println("+---------------+");
            System.out.println("| nothing found |");
            System.out.println("+---------------+");
            return;
        }
        
        // determine max field width
        Map<String, Integer> fieldWidth = new HashMap<>();
        for(int i = 0; i < results.size(); i++) {
            for(String fieldName : results.get(i).getFieldNames()) {
                Integer maxWidth = fieldWidth.get(fieldName);
                Integer currentWidth = results.get(i).getFieldValue(fieldName).toString().length();
                if(maxWidth == null) {
                    fieldWidth.put(fieldName, fieldName.length());
                    maxWidth = fieldName.length();
                }
                if(maxWidth.intValue() < currentWidth.intValue()) {
                    fieldWidth.put(fieldName, currentWidth);
                }
            }
        }

        printLine(results, fieldWidth);
        
        // print header
        System.out.print("|");
        for(String fieldName : results.get(0).getFieldNames()) {
            System.out.print(StringUtils.rightPad(fieldName, fieldWidth.get(fieldName)));
            System.out.print("|");
        }
        System.out.println("");

        printLine(results, fieldWidth);
        
        // print body
        for(int i = 0; i < results.size(); i++) {
            System.out.print("|");
            for(String fieldName : results.get(i).getFieldNames()) {
                String value = results.get(i).getFieldValue(fieldName).toString();
                System.out.print(StringUtils.rightPad(value, fieldWidth.get(fieldName)));
                System.out.print("|");                
            }
            System.out.println("");
            printLine(results, fieldWidth);
        }
    }

    private void printLine(SolrDocumentList results, Map<String, Integer> fieldWidth) {
        System.out.print("+");
        for(String fieldName : results.get(0).getFieldNames()) {
            System.out.print(StringUtils.rightPad("", fieldWidth.get(fieldName), "-"));
            System.out.print("+");
        }
        System.out.println("");
    }
	
}