 package org.atlasapi.persistence.media.entity;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.stub;
 
 import java.util.Map;
 
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Described;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.RelatedLink;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.Answer;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ContentTranslatorTest {
 
     private final DescribedTranslator describedTranslator = mock(DescribedTranslator.class);
     
     @Test
     public void testHashCodeSymmetry() {
         
         stub(describedTranslator.toDBObject((DBObject)any(), (Described)any())).toAnswer(new Answer<DBObject>() {
             public DBObject answer(InvocationOnMock invocation) {
                 return (DBObject) invocation.getArguments()[0];
             }
         });
         
         BasicDBList list = new BasicDBList();
         list.add(ImmutableMap.of("url", "http://example.com/", "type", "unknown"));
         list.add(ImmutableMap.of("url", "http://another.com/", "type", "unknown"));
         
         Map<String, Object> m = ImmutableMap.of("links", (Object)list);
                 
         DBObject dbo = new BasicDBObject(m);
         int hashCodeFromDbo = dbo.hashCode();
         
         Content content = new Item();
         
         content.setRelatedLinks(ImmutableSet.of(
                 RelatedLink.unknownTypeLink("http://example.com/").build(),
                 RelatedLink.unknownTypeLink("http://another.com/").build()
                 ));
         ContentTranslator translator = new ContentTranslator(describedTranslator, null);
         
         BasicDBObject dboFromContent = new BasicDBObject();
         translator.toDBObject(dboFromContent, content);
         int hashCodeFromContent = dboFromContent.hashCode();
         
         assertEquals(hashCodeFromContent, hashCodeFromDbo);
     }
 }
