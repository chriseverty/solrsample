package org.apache.solr.response.transform;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

public class CoreAugmenterFactory extends TransformerFactory {

	@Override
	public DocTransformer create(String field, SolrParams params, SolrQueryRequest req) {
		String v = req.getCore().getName();
		return new ValueAugmenter(field, v);
	}

}
