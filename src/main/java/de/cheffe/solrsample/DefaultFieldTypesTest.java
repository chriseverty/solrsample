package de.cheffe.solrsample;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.solr.schema.FieldHelper;
import org.apache.solr.schema.SchemaField;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.MLTTest.Document;
import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class DefaultFieldTypesTest {

    @ClassRule
    public static EmbeddedSolrServerResource<Document> solr = new EmbeddedSolrServerResource<>("defaultFieldTypes");

    @Test
    public void printDefaults() {
        Map<String, SchemaField> fields = new TreeMap<>(solr.getDefaultCoreSchema().getFields());
        System.out.println("type\tindexed\tstored\tomitNorms\tomitTermFreqAndPositions\ttokenized");
        for(Entry<String, SchemaField> field : fields.entrySet()) {
            System.out.print(field.getKey() + "\t");
            String properties = FieldHelper.fieldPropertiesToString(field.getValue().getProperties());
            printProperty(properties, "indexed");
            printProperty(properties, "stored");
            printProperty(properties, "omitNorms");
            printProperty(properties, "omitTermFreqAndPositions");
            printProperty(properties, "tokenized");
            System.out.println();
        }
    }

    private void printProperty(String properties, String tmpProperty) {
        if(properties.contains(tmpProperty)) {
            System.out.print("true\t");
        } else {
            System.out.print("false\t");
        }
    }
    
}
