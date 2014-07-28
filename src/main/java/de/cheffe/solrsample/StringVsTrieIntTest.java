package de.cheffe.solrsample;

import static java.math.BigDecimal.valueOf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class StringVsTrieIntTest {

	private static final Logger LOG = LoggerFactory
			.getLogger(StringVsTrieIntTest.class);

	@ClassRule
	public static EmbeddedSolrServerResource<Object> solr = new EmbeddedSolrServerResource<>(
			"string-vs-trieint");

	@BeforeClass
	public static void setup() throws Exception {
		solr.setCommitImmediateAfterAdd(false);

		File reportFile = new File("report.log");
		if (reportFile.exists()) {
			reportFile.delete();
		}
		reportFile.createNewFile();
		reportWriter = new FileWriter(reportFile);
		reportWriter.write("field under test; document count; size of index (bytes); avg q time (ms); avg fq time (ms);avg facet time (ms); avg q elapsed time (ms); avg fq elapsed time (ms);avg facet elapsed time (ms);\n"); 
	}

	@AfterClass
	public static void teardown() throws Exception {
		reportWriter.flush();
		reportWriter.close();
	}
	
	static FileWriter reportWriter;
	int batchSize = 25 * 1000;
	int testRuns[] = { 
			50000, 
			100000, 
			500000, 
			1000000, 
			2000000, 
			4000000,
			8000000, 
			16000000, 
			32000000, 
			64000000 
	};

	@Test
	public void checkString() {
		solr.clearIndex();

		int from = 1;
		for(int to : testRuns) {
			addDocumentsToIndex("id_string", from, to);
			from = to + 1;
			runBenchmark("id_string", to);
		}
	}

	@Test
	public void checkTriInt() {
		solr.clearIndex();

		int from = 1;
		for(int to : testRuns) {
			addDocumentsToIndex("id_tint", from, to);
			from = to + 1;
			runBenchmark("id_tint", to);
		}
	}

	@Test
	public void checkInt() {
		solr.clearIndex();

		int from = 1;
		for(int to : testRuns) {
			addDocumentsToIndex("id_int", from, to);
			from = to + 1;
			runBenchmark("id_int", to);
		}
	}

	private void addDocumentsToIndex(String aField, int aFrom, int aTo) {
		LOG.info("setting up index for '{}' from {} to {}", new Object[] {aField, aFrom, aTo});

		List<SolrInputDocument> docs = new ArrayList<>(batchSize);
		for (int i = aFrom; i <= aTo; i++) {

			SolrInputDocument document = new SolrInputDocument();
			Object[] values = new Object[5];
			for(int j = 0; j < 5; j++) {
				values[j] = (i + j) % 100000;
			}
			document.setField(aField, values);
			docs.add(document);

			if (i % batchSize == 0) {
				solr.addToIndex(docs);
				LOG.debug("done with {} docs", i);
				docs = new ArrayList<>(batchSize);
			}
		}
		solr.commit();
		solr.optimize();
	}

	private void runBenchmark(String aField, int documentCount) {
		LOG.info("running benchmark for: {}", aField);
		long querySum = 0;
		long queryElapsedSum = 0;
		long filterSum = 0;
		long filterElapsedSum = 0;
		long facetSum = 0;
		long facetElapsedSum = 0;
		
		int testQueryCount = 100;
		for (int i = 1; i <= testQueryCount; i++) {
			solr.commit();
			QueryResponse response = solr.query(aField + ":" + i);
			querySum += response.getQTime();
			queryElapsedSum += response.getElapsedTime();

			solr.commit();
			SolrQuery query = new SolrQuery("*:*");
			query.setFilterQueries(aField + ":" + (i + testQueryCount));
			response = solr.query(query);
			filterSum += response.getQTime();
			filterElapsedSum += response.getElapsedTime();
			
			solr.commit();
			query = new SolrQuery("*:*");
			query.setFacet(true);
			query.setFields(aField);
			response = solr.query(query);
			facetSum += response.getQTime();
			facetElapsedSum += response.getElapsedTime();
		}

		String avgQueryTime = valueOf(querySum).divide(valueOf(testQueryCount)).toPlainString();
		String avgQueryElapsedTime = valueOf(queryElapsedSum).divide(valueOf(testQueryCount)).toPlainString();
		String avgFilterTime = valueOf(filterSum).divide(valueOf(testQueryCount)).toPlainString();
		String avgFilterElapsedTime = valueOf(filterElapsedSum).divide(valueOf(testQueryCount)).toPlainString();
		String avgFacetTime = valueOf(facetSum).divide(valueOf(testQueryCount)).toPlainString();
		String avgFacetElapsedTime = valueOf(facetElapsedSum).divide(valueOf(testQueryCount)).toPlainString();
		
		writeLine(aField + ";" + documentCount + ";" + solr.getDefaultIndexSize() + ";" + avgQueryTime + ";" + avgFilterTime + ";" + avgFacetTime + ";"
				+ avgQueryElapsedTime + ";" + avgFilterElapsedTime + ";" + avgFacetElapsedTime);
	}

	private void writeLine(String aLine) {
		try {
			reportWriter.write(aLine + "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
