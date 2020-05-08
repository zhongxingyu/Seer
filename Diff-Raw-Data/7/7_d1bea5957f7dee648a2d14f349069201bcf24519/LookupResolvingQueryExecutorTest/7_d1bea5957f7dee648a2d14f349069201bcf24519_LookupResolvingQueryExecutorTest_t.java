 package org.atlasapi.query.content;
 
 import static org.hamcrest.Matchers.hasItems;
 
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.content.criteria.MatchesNothing;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.LookupRef;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.KnownTypeContentResolver;
 import org.atlasapi.persistence.content.ResolvedContent;
 import org.atlasapi.persistence.lookup.InMemoryLookupEntryStore;
 import org.atlasapi.persistence.lookup.entry.LookupEntry;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 
 @RunWith(JMock.class)
 public class LookupResolvingQueryExecutorTest extends TestCase {
     private final Mockery context = new Mockery();
     
     private KnownTypeContentResolver cassandraContentResolver = context.mock(KnownTypeContentResolver.class, "cassandraContentResolver");
     private KnownTypeContentResolver mongoContentResolver = context.mock(KnownTypeContentResolver.class, "mongoContentResolver");
     
     private final InMemoryLookupEntryStore lookupStore = new InMemoryLookupEntryStore();
     
     private final LookupResolvingQueryExecutor executor = new LookupResolvingQueryExecutor(cassandraContentResolver, mongoContentResolver, lookupStore);
 
     @Test
     public void testSetsSameAs() {
         final String query = "query";
         final Item queryItem = new Item(query, "qcurie", Publisher.BBC);
         final Item equivItem = new Item("equiv", "ecurie", Publisher.YOUTUBE);
         
         lookupStore.store(lookupEntryWithEquivalents(query, LookupRef.from(queryItem), LookupRef.from(equivItem)));
         
         context.checking(new Expectations(){{
             one(mongoContentResolver).findByLookupRefs(with(hasItems(LookupRef.from(queryItem), LookupRef.from(equivItem))));
             will(returnValue(ResolvedContent.builder().put(queryItem.getCanonicalUri(), queryItem).put(equivItem.getCanonicalUri(), equivItem).build()));
         }});
         context.checking(new Expectations(){{
             never(cassandraContentResolver).findByLookupRefs(with(Expectations.<Iterable<LookupRef>>anything()));
         }});
         
         Map<String, List<Identified>> result = executor.executeUriQuery(ImmutableList.of(query), MatchesNothing.asQuery());
         
         assertEquals(2, result.get(query).size());
         for (Identified resolved : result.get(query)) {
             if(resolved.getCanonicalUri().equals(query)) {
                assertEquals(ImmutableSet.of(LookupRef.from(equivItem)), resolved.getEquivalentTo());
             } else if(resolved.getCanonicalUri().equals("equiv")) {
                assertEquals(ImmutableSet.of(LookupRef.from(queryItem)), resolved.getEquivalentTo());
             }
         }
         
         context.assertIsSatisfied();
     }
     
     @Test
     public void testCassandraIsNotCalledIfMongoReturnsSomething() {
         final String query = "query";
         final Item queryItem = new Item(query, "qcurie", Publisher.BBC);
         
         lookupStore.store(LookupEntry.lookupEntryFrom(queryItem));
 
         context.checking(new Expectations(){{
             one(mongoContentResolver).findByLookupRefs(with(Expectations.<Iterable<LookupRef>>anything()));
             will(returnValue(ResolvedContent.builder().put(queryItem.getCanonicalUri(), queryItem).build()));
         }});
         context.checking(new Expectations(){{
             never(cassandraContentResolver).findByLookupRefs(with(Expectations.<Iterable<LookupRef>>anything()));
         }});
         
         Map<String, List<Identified>> result = executor.executeUriQuery(ImmutableList.of(query), MatchesNothing.asQuery());
         
         assertEquals(1, result.get(query).size());
         
         context.assertIsSatisfied();
     }
     
     @Test
     public void testCassandraIsCalledIfMongoReturnsNothing() {
         final String query = "query";
         final Item queryItem = new Item(query, "qcurie", Publisher.BBC);
 
         context.checking(new Expectations(){{
             never(mongoContentResolver).findByLookupRefs(with(Expectations.<Iterable<LookupRef>>anything()));
         }});
         context.checking(new Expectations(){{
             one(cassandraContentResolver).findByLookupRefs(with(Expectations.<Iterable<LookupRef>>anything()));
             will(returnValue(ResolvedContent.builder().put(queryItem.getCanonicalUri(), queryItem).build()));
         }});
         
         Map<String, List<Identified>> result = executor.executeUriQuery(ImmutableList.of(query), MatchesNothing.asQuery());
         
         assertEquals(1, result.get(query).size());
         
         context.assertIsSatisfied();
     }
 
     private LookupEntry lookupEntryWithEquivalents(String uri, LookupRef... equiv) {
         return new LookupEntry(uri, null, LookupRef.from(new Item("uri","curie",Publisher.BBC)), ImmutableSet.<String>of(), ImmutableSet.<LookupRef>of(), ImmutableSet.<LookupRef>of(), ImmutableSet.copyOf(equiv), null, null);
     }
 }
