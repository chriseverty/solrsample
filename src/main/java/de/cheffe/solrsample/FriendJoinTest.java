package de.cheffe.solrsample;

import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import de.cheffe.solrsample.rule.EmbeddedSolrServerResource;

public class FriendJoinTest {

    @ClassRule
    public static EmbeddedSolrServerResource<Friend> solr = new EmbeddedSolrServerResource<>("friend-core");

    @BeforeClass
    public static void setup() {
        solr.clearIndex();
        //@formatter:off
        List<Friend> friends = Arrays.asList(
          new Friend(1, "George", "nice american guy", 35, "USA", "New York", new Integer[] {2,3}),
          new Friend(2, "Peter", "nice french guy", 30, "France", "Paris", new Integer[] {1}),
          new Friend(3, "Thomas", "nice german guy", 25, "Germany", "Berlin", new Integer[] {1}),
          new Friend(4, "Martin", "has only young friends", 25, "Germany", "Berlin", new Integer[] {3,5}),
          new Friend(5, "Nick", "has only young friends", 27, "Germany", "Berlin", new Integer[] {3,4})
        );
        //@formatter:on
        solr.addBeansToIndex(friends);
    }

    @Test
    public void listCountriesOfFriendsOfGeorge() throws SolrServerException {
        System.out.println("list countries of friends of george");
        SolrQuery friendOfGeorgeQuery = new SolrQuery("friends:1");
        friendOfGeorgeQuery.setFields("countryRaw").setRows(0);
        friendOfGeorgeQuery.setFacet(true).addFacetField("countryRaw").setFacetMinCount(1);
        
        QueryResponse response = solr.query(friendOfGeorgeQuery);
        for(FacetField.Count count : response.getFacetField("countryRaw").getValues()) {
            System.out.print("  ");
            System.out.println(count);
        }
    }

    @Test
    public void listPersonsWithFriendsOfCertainAge() throws SolrServerException {
        System.out.println("list persons that have friends that are of age 30 or older");
        SolrQuery query = new SolrQuery("{!join from=id to=friends}age:[30 TO *]");
        printFriendResult(solr.query(query).getBeans(Friend.class));        
    }
    
    private void printFriendResult(List<Friend> foundFriends) {
        for (Friend friend : foundFriends) {
            System.out.print("  ");
            System.out.print(friend.name);
            System.out.print(", ");
            System.out.print(friend.age);
            System.out.print(", ");
            System.out.print(friend.town);
            System.out.print(", ");
            System.out.println(friend.country);
        }
    }

    /**
     * POJO for automatic mapping.
     * 
     * @author cheffe
     */
    public static class Friend {
        @Field
        public int id;
        @Field
        public String name;
        @Field
        public String description;
        @Field
        public int age;
        @Field
        public String town;
        @Field
        public String townRaw;
        @Field
        public String country;
        @Field
        public String countryRaw;
        @Field
        public Integer[] friends;

        /**
         * Empty default constructor for solr's mapping mechanism.
         */
        public Friend() {
            super();
        }

        /**
         * Convenience constructor for setup.
         */
        public Friend(int id, String name, String description, int age, String country, String town, Integer[] friends) {
            super();
            this.id = id;
            this.name = name;
            this.description = description;
            this.age = age;
            this.country = country;
            this.town = town;
            this.friends = friends;
        }

    }

}
