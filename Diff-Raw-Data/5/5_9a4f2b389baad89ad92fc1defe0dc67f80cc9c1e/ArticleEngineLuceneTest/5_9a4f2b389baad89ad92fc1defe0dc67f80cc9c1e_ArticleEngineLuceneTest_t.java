 package com.celements.blog.article;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.celements.search.lucene.ILuceneSearchService;
 import com.celements.search.lucene.LuceneSearchException;
 import com.celements.search.lucene.LuceneSearchResult;
 import com.celements.search.lucene.query.LuceneQuery;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.web.Utils;
 
 public class ArticleEngineLuceneTest extends AbstractBridgedComponentTestCase {
   
   private ArticleEngineLucene engine;
 
   private ILuceneSearchService searchServiceMock;
   private IArticleLuceneQueryBuilderRole queryBuilderMock;
 
   @Before
   public void setUp_ArticleEngineLuceneTest() {
     engine = (ArticleEngineLucene) Utils.getComponent(IArticleEngineRole.class, "lucene");
     searchServiceMock = createMockAndAddToDefault(ILuceneSearchService.class);
     engine.injectSearchService(searchServiceMock);
     queryBuilderMock = createMockAndAddToDefault(IArticleLuceneQueryBuilderRole.class);
     engine.injectQueryBuilder(queryBuilderMock);
   }
   
   @Test
   public void testGetArticles() throws Exception {
     ArticleLoadParameter param = new ArticleLoadParameter();
     List<String> sortFields = Arrays.asList("field1", "field2");
     param.setSortFields(sortFields);
     String language = "de";
     param.setLanguage(language);
     int offset = 5;
     param.setOffset(offset);
     int limit = 10;
     param.setLimit(limit);
     LuceneQuery query = new LuceneQuery("db");    
     LuceneSearchResult resultMock = createMockAndAddToDefault(LuceneSearchResult.class);
     
     expect(queryBuilderMock.build(same(param))).andReturn(query).once();
     expect(searchServiceMock.searchWithoutChecks(same(query), eq(sortFields), 
        eq(Arrays.asList("default", language)))).andReturn(resultMock).once();
     expect(resultMock.setOffset(eq(offset))).andReturn(resultMock).once();
     expect(resultMock.setLimit(eq(limit))).andReturn(resultMock).once();
     expect(resultMock.getResults()).andReturn(Collections.<DocumentReference>emptyList()
         ).once();
     
     replayDefault();
     List<Article> ret = engine.getArticles(param);
     verifyDefault();
     
     assertNotNull(ret);
     assertEquals(0, ret.size());
   }
   
   @Test
   public void testGetArticles_XWE() throws Exception {
     ArticleLoadParameter param = new ArticleLoadParameter();
     XWikiException cause = new XWikiException();
     
     expect(queryBuilderMock.build(same(param))).andThrow(cause).once();
     
     replayDefault();
     try {
       engine.getArticles(param);
       fail("expecting ALE");
     } catch (ArticleLoadException ale) {
       assertSame(cause, ale.getCause());
     }
     verifyDefault();
   }
   
   @Test
   public void testGetArticles_LSE() throws Exception {
     ArticleLoadParameter param = new ArticleLoadParameter();
     List<String> sortFields = Arrays.asList("field1", "field2");
     param.setSortFields(sortFields);
     String language = "de";
     param.setLanguage(language);
     int offset = 5;
     param.setOffset(offset);
     int limit = 10;
     param.setLimit(limit);
     LuceneQuery query = new LuceneQuery("db");    
     LuceneSearchResult resultMock = createMockAndAddToDefault(LuceneSearchResult.class);
     Throwable cause = createMockAndAddToDefault(LuceneSearchException.class);
     
     expect(queryBuilderMock.build(same(param))).andReturn(query).once();
     expect(searchServiceMock.searchWithoutChecks(same(query), eq(sortFields), 
        eq(Arrays.asList("default", language)))).andReturn(resultMock).once();
     expect(resultMock.setOffset(eq(offset))).andReturn(resultMock).once();
     expect(resultMock.setLimit(eq(limit))).andReturn(resultMock).once();
     expect(resultMock.getResults()).andThrow(cause).once();
     
     replayDefault();
     try {
       engine.getArticles(param);
       fail("expecting ALE");
     } catch (ArticleLoadException ale) {
       assertSame(cause, ale.getCause());
     }
     verifyDefault();
   }
 
 }
