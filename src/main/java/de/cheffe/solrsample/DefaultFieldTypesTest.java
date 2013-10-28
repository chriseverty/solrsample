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

        // show the default values
        for (Entry<String, SchemaField> field : fields.entrySet()) {
            System.out.println(field.getValue());
        }

        // now print the nice table
        System.out.println("type\t\tindexed\tstored\tomitNorms\tomitTermFreqAndPositions\ttokenized");
        for (Entry<String, SchemaField> field : fields.entrySet()) {
            if(!field.getKey().equals("textField")) {
                System.out.print(field.getKey() + "\t\t");
            } else {
                System.out.print(field.getKey() + "\t");
            }
            String properties = FieldHelper.fieldPropertiesToString(field.getValue().getProperties());
            printProperty(properties, "indexed", 1);
            printProperty(properties, "stored", 1);
            printProperty(properties, "omitNorms", 2);
            printProperty(properties, "omitTermFreqAndPositions", 4);
            printProperty(properties, "tokenized", 1);
            System.out.println();
        }
    }

    private void printProperty(String aProperties, String aProperty, int aNumTabs) {
        if(aProperties.contains(aProperty)) {
            System.out.print("true");
        } else {
            System.out.print("false");
        }
        for(int i=0; i<aNumTabs; i++) {
            System.out.print("\t");
        }
    }
}
