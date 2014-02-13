package de.cheffe.solrsample;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.util.DateUtil;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class DateBoostTest {

    @ClassRule
    public static EmbeddedSolrServerResource<Document> solr = new EmbeddedSolrServerResource<>("dateboost-core");

    @BeforeClass
    public static void setupIndex() throws ParseException {
        //@formatter:off
	    solr.addBeansToIndex(
	        new Document(1, "A", DateUtil.parseDate("2012-12-31")),
	        new Document(2, "B", DateUtil.parseDate("2012-12-29")),
	        new Document(3, "C", DateUtil.parseDate("2013-01-30")),
	        new Document(4, "D", DateUtil.parseDate("2012-11-30")),
	        new Document(5, "E", DateUtil.parseDate("2012-12-01"))
	    );
        //@formatter:on
    }

    @Test
    public void boostCloseToNow() throws Exception {
        SolrQuery query = new SolrQuery("*:*");
        // TODO add boost parameter
        QueryResponse response = solr.query(query);
        solr.print(response);

        System.out.println(ClientUtils.toQueryString(query, false));

        StringBuilder actualOrder = new StringBuilder();
        for(Document  doc : response.getBeans(Document.class)) {
            actualOrder.append(doc.title);
            actualOrder.append(',');
        }
        assertEquals("A,C,B,E,D", actualOrder.toString());
    }

    public static class Document {
        @Field
        public int id;
        @Field
        public String title;
        @Field
        public Date date;
        
        public Document() {
            // for Solr that creates document by reflection
        }

        public Document(int id, String title, Date date) {
            super();
            this.id = id;
            this.title = title;
            this.date = date;
        }
    }

}
