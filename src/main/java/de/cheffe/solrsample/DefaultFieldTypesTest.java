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
        String newLineMark = System.getProperty("line.separator");
        System.out.println("+-----------+---------+--------+-----------+------------------------+-----------+");
        System.out.println("|   Type    | indexed | stored | omitNorms |omitTermFreqAndPositions| tokenized |");
        System.out.println("+-----------+---------+--------+-----------+------------------------+-----------+");
        String leftAlignFormat = "| %-9s | %-7s | %-6s | %-9s | %-22s | %-9s |" + newLineMark;
        for (Entry<String, SchemaField> field : fields.entrySet()) {
            String properties = FieldHelper.fieldPropertiesToString(field.getValue().getProperties());
            //@formatter:off
            System.out.format(leftAlignFormat, field.getKey(), 
                    printProperty(properties, "indexed"),
                    printProperty(properties, "stored"),
                    printProperty(properties, "omitNorms"),
                    printProperty(properties, "omitTermFreqAndPositions"),
                    printProperty(properties, "tokenized"));
            //@formatter:on
        }
        System.out.format("+-----------+---------+--------+-----------+------------------------+-----------+" + newLineMark);
    }

    private String printProperty(String aProperties, String aProperty) {
        if (aProperties.contains(aProperty)) {
            return "true";
        }
        return "false";
    }
}
