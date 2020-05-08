 package org.atlasapi.remotesite.bbc;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Topic;
 import org.atlasapi.media.entity.Topic.Type;
 import org.atlasapi.media.entity.TopicRef;
 import org.atlasapi.persistence.logging.NullAdapterLog;
 import org.atlasapi.persistence.topic.TopicStore;
 import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesDescription;
 import org.junit.Test;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.base.Maybe;
 
 public class BbcSlashProgrammesRdfTopicExtractorTest extends TestCase {
 
     private final String topicUri = "http://dbpedia.org/resource/Religion";
     private final DummyTopicStore topicStore = new DummyTopicStore(topicUri);
     private final BbcSlashProgrammesRdfTopicExtractor extractor = new BbcSlashProgrammesRdfTopicExtractor(topicStore , new NullAdapterLog());
 
     @Test
     public void testExtractsTopicFromValidSlashProgrammesTopic() {
         
         String typeUri = "http://purl.org/ontology/po/Person";
         
         SlashProgrammesRdf rdf = new SlashProgrammesRdf().withDescription(new SlashProgrammesDescription().withSameAs(
                 ImmutableSet.of(new SlashProgrammesRdf.SlashProgrammesSameAs().withResourceUri(topicUri))
         ).withTypes(
                 ImmutableSet.of(new SlashProgrammesRdf.SlashProgrammesType().withResourceUri(typeUri))
         ));
         
         Maybe<TopicRef> extractedTopicRef = extractor.extract(rdf);
         Topic storedTopic = topicStore.getStoredTopic();
         
         assertTrue(extractedTopicRef.hasValue());
         assertThat(extractedTopicRef.requireValue().getTopic(), is(equalTo(storedTopic.getId())));
         assertThat(extractedTopicRef.requireValue().getWeighting(), is(equalTo(1f)));
         assertThat(extractedTopicRef.requireValue().isSupervised(), is(true));
         assertThat(extractedTopicRef.requireValue().getRelationship(), is(TopicRef.Relationship.ABOUT));
         
         assertThat(storedTopic.getValue(), is(equalTo(topicUri)));
         assertThat(storedTopic.getType(), is(equalTo(Type.PERSON)));
         assertThat(storedTopic.getPublisher(), is(equalTo(Publisher.DBPEDIA)));
         assertThat(storedTopic.getNamespace(), is(equalTo(Publisher.DBPEDIA.name().toLowerCase())));
         assertThat(storedTopic.getTitle(), is(equalTo("Religion")));
         
     }
     
     private static class DummyTopicStore implements TopicStore {
     	private Topic storedTopic;
 		private String topicUri;
 
     	public DummyTopicStore(String topicUri) {
     		this.topicUri = topicUri;
     	}
 		@Override
 		public Maybe<Topic> topicFor(String namespace, String value) {
			Preconditions.checkArgument(namespace.equals("dbpedia"), "Unexpected namespace");
			Preconditions.checkArgument(value.equals(topicUri), "Unexpected URI");
 			
 			return Maybe.just(new Topic(100l));
 		}
 		
 		@Override
 		public Maybe<Topic> topicFor(Publisher publisher, String namespace, String value) {
 		    throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public void write(Topic topic) {
 			Preconditions.checkState(topicUri != null, "Already stored a topic");
 			this.storedTopic = topic;
 		}
     	
     	public Topic getStoredTopic() {
     		return storedTopic;
     	}
     }
     
 }
