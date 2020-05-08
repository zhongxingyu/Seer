 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.blog.article;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 
import com.celements.blog.article.Article;
 import com.celements.blog.plugin.BlogClasses;
 import com.celements.blog.plugin.EmptyArticleException;
 import com.celements.blog.service.BlogService;
 import com.celements.blog.service.IBlogServiceRole;
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 public class ArticleTest extends AbstractBridgedComponentTestCase {
 
   private XWikiContext context;
   private XWikiDocument articleDoc;
   private Article article;
   private XWiki xwiki;
   private IBlogServiceRole blogServiceMock;
 
   @Before
   public void setUp_ArticleTest() throws Exception {
     context = getContext();
     xwiki = createMock(XWiki.class);
     context.setWiki(xwiki);
     article = new Article(context);
     blogServiceMock = createMock(IBlogServiceRole.class);
     article.injected_blogService = blogServiceMock;
     articleDoc = createMock(XWikiDocument.class);
   }
 
   @Test
   public void testGetBlogService_default() {
     article.injected_blogService = null;
     assertNotNull(article.getBlogService());
   }
 
   @Test
   public void testGetBlogService_inject() {
     BlogService testBlogService = new BlogService();
     article.injected_blogService = testBlogService;
     assertSame(testBlogService, article.getBlogService());
   }
 
   @Test
   public void testGetTitleDetailed() throws XWikiException, EmptyArticleException {
     BaseObject bObj = new BaseObject();
     bObj.setStringValue("title", "Article Title");
     bObj.setStringValue("lang", "de");
     com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(bObj, context);
     List<com.xpn.xwiki.api.Object> list = new ArrayList<com.xpn.xwiki.api.Object>();
     list.add(obj);
     expect(xwiki.getSpacePreference(eq("default_language"), eq("space"), eq(""),
         same(context))).andReturn("").anyTimes();
     replayAll();
     article = new Article(list, "space", context);
     String[] result = article.getTitleDetailed("de");
     verifyAll();
     assertEquals("de", result[0]);
     assertEquals("Article Title", result[1]);
   }
   
   @Test
   public void testGetTitleDetailed_noTranslation() throws XWikiException, 
       EmptyArticleException {
     BaseObject bObj = new BaseObject();
     bObj.setStringValue("title", "Article Title");
     bObj.setStringValue("lang", "de");
     com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(bObj, context);
     BaseObject frbObj = new BaseObject();
     frbObj.setStringValue("title", "");
     frbObj.setStringValue("lang", "fr");
     com.xpn.xwiki.api.Object frObj = new com.xpn.xwiki.api.Object(frbObj, context);
     List<com.xpn.xwiki.api.Object> list = new ArrayList<com.xpn.xwiki.api.Object>();
     list.add(obj);
     list.add(frObj);
     expect(xwiki.getSpacePreference(eq("default_language"), eq("space"), eq(""), 
         same(context))).andReturn("de");
     replayAll();
     article = new Article(list, "space", context);
     String[] result = article.getTitleDetailed("fr");
     assertEquals("de", result[0]);
     assertEquals("Article Title", result[1]);
     verifyAll();
   }
   
   @Test
   public void getTitle() throws Exception {
     BaseObject bObj = new BaseObject();
     bObj.setStringValue("title", "Article Title");
     bObj.setStringValue("lang", "de");
     com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(bObj, context);
     List<com.xpn.xwiki.api.Object> list = new ArrayList<com.xpn.xwiki.api.Object>();
     list.add(obj);
     article = new Article(list, "space", context);
     assertEquals("Article Title", article.getTitle("de"));
   }
 
   @Test
   public void testGetDocumentReference() throws Exception {
     BaseObject bObj = new BaseObject();
     bObj.setStringValue("title", "Article Title");
     bObj.setStringValue("lang", "de");
     DocumentReference expectedDocRef = new DocumentReference(getContext().getDatabase(),
         "MyBlog", "Article1");
     bObj.setDocumentReference(expectedDocRef);
     com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(bObj, context);
     List<com.xpn.xwiki.api.Object> list = new ArrayList<com.xpn.xwiki.api.Object>();
     list.add(obj);
     article = new Article(list, "space", context);
     assertEquals(expectedDocRef, article.getDocumentReference());
   }
 
   @Test
   public void testGetDocumentReference_nullObject() throws Exception {
     BaseObject bObj = new BaseObject();
     bObj.setStringValue("title", "Article Title");
     bObj.setStringValue("lang", "de");
     DocumentReference expectedDocRef = new DocumentReference(getContext().getDatabase(),
         "MyBlog", "Article1");
     bObj.setDocumentReference(expectedDocRef);
     com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(bObj, context);
     List<com.xpn.xwiki.api.Object> list = new ArrayList<com.xpn.xwiki.api.Object>();
     list.add(null);
     list.add(obj);
     article = new Article(list, "space", context);
     assertEquals(expectedDocRef, article.getDocumentReference());
   }
 
   @Test
   public void getTitle_noTranslation() throws XWikiException, EmptyArticleException {
     BaseObject bObj = new BaseObject();
     bObj.setStringValue("title", "Article Title");
     bObj.setStringValue("lang", "de");
     com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(bObj, context);
     BaseObject frbObj = new BaseObject();
     frbObj.setStringValue("title", "");
     frbObj.setStringValue("lang", "fr");
     com.xpn.xwiki.api.Object frObj = new com.xpn.xwiki.api.Object(frbObj, context);
     List<com.xpn.xwiki.api.Object> list = new ArrayList<com.xpn.xwiki.api.Object>();
     list.add(obj);
     list.add(frObj);
     expect(xwiki.getSpacePreference(eq("default_language"), eq("space"), eq(""), 
         same(context))).andReturn("de");
     replayAll();
     article = new Article(list, "space", context);
     assertEquals("Article Title", article.getTitle("fr"));
     verifyAll();
   }
 
   @Test 
   public void getStringProperty() throws XWikiException, EmptyArticleException {
     BaseObject bObj = new BaseObject();
     bObj.setStringValue("field", "value");
     com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(bObj, context);
     List<com.xpn.xwiki.api.Object> list = new ArrayList<com.xpn.xwiki.api.Object>();
     list.add(obj);
     article = new Article(list, "space", context);
     assertEquals("value", article.getStringProperty(obj, "field"));
   }
   
   @Test
   public void testHasMoreLink_in_translation_translation_empty(
       ) throws XWikiException, EmptyArticleException {
     Document articleApiDoc = new Document(articleDoc, context);
     expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
     Vector<BaseObject> articleObjs = new Vector<BaseObject>();
     BaseObject articleDe = new BaseObject();
     articleDe.setLargeStringValue("extract", "deutscher extract");
     articleDe.setLargeStringValue("content", "deutscher extract");
     articleDe.setStringValue("lang", "de");
     articleObjs.add(articleDe);
     BaseObject articleFr = new BaseObject();
     articleFr.setStringValue("lang", "fr");
     articleObjs.add(articleFr);
     BaseObject articleIt = new BaseObject();
     articleIt.setStringValue("lang", "it");
     articleObjs.add(articleIt);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
         "ArticleClass");
     expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
     expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
     expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("News"), eq(""),
         same(context))).andReturn("de");
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Main"))).andReturn(null).anyTimes();
     replayAll();
     article = new Article(articleDoc , context);
     article.injected_blogService = blogServiceMock;
     assertTrue("No translation in it but details in de.", article.hasMoreLink("it",
         false));
     verifyAll();
   }
 
   @Test
   public void testHasMoreLink_in_translation_translation_not_empty(
       ) throws XWikiException, EmptyArticleException {
     Document articleApiDoc = new Document(articleDoc, context);
     expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
     Vector<BaseObject> articleObjs = new Vector<BaseObject>();
     BaseObject articleDe = new BaseObject();
     articleDe.setLargeStringValue("extract", "deutscher extract");
     articleDe.setLargeStringValue("content", "deutscher content");
     articleDe.setStringValue("lang", "de");
     articleObjs.add(articleDe);
     BaseObject articleFr = new BaseObject();
     articleFr.setStringValue("lang", "fr");
     articleObjs.add(articleFr);
     BaseObject articleIt = new BaseObject();
     articleIt.setStringValue("lang", "it");
     articleIt.setLargeStringValue("extract", "Ital extract");
     articleIt.setLargeStringValue("content", "Ital content");
     articleObjs.add(articleIt);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
         "ArticleClass");
     expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
     expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
     expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("News"), eq(""),
         same(context))).andReturn("de");
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Main"))).andReturn(null).anyTimes();
     replayAll();
     article = new Article(articleDoc , context);
     article.injected_blogService = blogServiceMock;
     assertTrue("Has translation in it.", article.hasMoreLink("it",
         false));
     verifyAll();
   }
 
   @Test
   public void testHasMoreLink_in_default_lang(
       ) throws XWikiException, EmptyArticleException {
     Document articleApiDoc = new Document(articleDoc, context);
     expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
     Vector<BaseObject> articleObjs = new Vector<BaseObject>();
     BaseObject articleDe = new BaseObject();
     articleDe.setLargeStringValue("extract", "deutscher extract");
     articleDe.setLargeStringValue("content", "deutscher content");
     articleDe.setStringValue("lang", "de");
     articleObjs.add(articleDe);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
         "ArticleClass");
     expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
     expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
     expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("News"), eq(""),
         same(context))).andReturn("de");
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Main"))).andReturn(null).anyTimes();
     replayAll();
     article = new Article(articleDoc , context);
     article.injected_blogService = blogServiceMock;
     assertTrue("Details in de.", article.hasMoreLink("de",
         false));
     verifyAll();
   }
 
   @Test
   public void testHasMoreLink_in_default_lang_no_extract(
       ) throws XWikiException, EmptyArticleException {
     Document articleApiDoc = new Document(articleDoc, context);
     expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
     Vector<BaseObject> articleObjs = new Vector<BaseObject>();
     BaseObject articleDe = new BaseObject();
     articleDe.setLargeStringValue("content", "deutscher content");
     articleDe.setStringValue("lang", "de");
     articleObjs.add(articleDe);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
         "ArticleClass");
     expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
     expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
     expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("News"), eq(""),
         same(context))).andReturn("de");
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Main"))).andReturn(null).anyTimes();
     replayAll();
     article = new Article(articleDoc , context);
     article.injected_blogService = blogServiceMock;
     assertFalse("Only details (short) but no extract in de.", article.hasMoreLink("de",
         false));
     verifyAll();
   }
 
   @Test
   public void testHasMoreLink_in_default_lang_no_extract_long_content(
       ) throws XWikiException, EmptyArticleException {
     Document articleApiDoc = new Document(articleDoc, context);
     expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
     Vector<BaseObject> articleObjs = new Vector<BaseObject>();
     BaseObject articleDe = new BaseObject();
     articleDe.setLargeStringValue("content", getLoremIpsum());
     articleDe.setStringValue("lang", "de");
     articleObjs.add(articleDe);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
         "ArticleClass");
     expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
     expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
     expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("News"), eq(""),
         same(context))).andReturn("de");
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Main"))).andReturn(null).anyTimes();
     replayAll();
     article = new Article(articleDoc , context);
     article.injected_blogService = blogServiceMock;
     assertTrue("Only details (to long for extract) but no extract in de.", 
         article.hasMoreLink("de", false));
     verifyAll();
   }
   
   @Test
   public void testHasMoreLink_in_translation_no_extract(
       ) throws XWikiException, EmptyArticleException {
 //    expect(articleDoc.getSpace()).andReturn("News");
     Document articleApiDoc = new Document(articleDoc, context);
     expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
     Vector<BaseObject> articleObjs = new Vector<BaseObject>();
     BaseObject articleDe = new BaseObject();
     articleDe.setLargeStringValue("content", "deutscher content");
     articleDe.setStringValue("lang", "de");
     articleObjs.add(articleDe);
     BaseObject articleIt = new BaseObject();
     articleIt.setStringValue("lang", "it");
     articleObjs.add(articleIt);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
         "ArticleClass");
     expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
     expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
     expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("News"), eq(""),
         same(context))).andReturn("de");
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Main"))).andReturn(null).anyTimes();
     replayAll();
     article = new Article(articleDoc , context);
     article.injected_blogService = blogServiceMock;
     assertFalse("Only details (short) but no extract in de.", 
         article.hasMoreLink("it", false));
     verifyAll();
   }
   
   @Test
   public void testHasMoreLink_in_translation_no_extract_long_content(
       ) throws XWikiException, EmptyArticleException {
     Document articleApiDoc = new Document(articleDoc, context);
     expect(articleDoc.newDocument(same(context))).andReturn(articleApiDoc);
     Vector<BaseObject> articleObjs = new Vector<BaseObject>();
     BaseObject articleDe = new BaseObject();
     articleDe.setLargeStringValue("content", getLoremIpsum());
     articleDe.setStringValue("lang", "de");
     articleObjs.add(articleDe);
     BaseObject articleIt = new BaseObject();
     articleIt.setStringValue("lang", "it");
     articleObjs.add(articleIt);
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
         "ArticleClass");
     expect(articleDoc.clone()).andReturn(articleDoc).atLeastOnce();
     expect(articleDoc.resolveClassReference(eq("XWiki.ArticleClass"))).andReturn(
         articleClassRef);
     expect(articleDoc.getXObjects(eq(articleClassRef))).andReturn(articleObjs);
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
     expect(articleDoc.getDocumentReference()).andReturn(articleDocRef).atLeastOnce();
     expect(xwiki.getSpacePreference(eq("default_language"), eq("News"), eq(""),
         same(context))).andReturn("de");
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("Main"))).andReturn(null).anyTimes();
     replayAll();
     article = new Article(articleDoc, context);
     article.injected_blogService = blogServiceMock;
     assertTrue("Only details (to long for extract) but no extract in de.", 
         article.hasMoreLink("it", false));
     verifyAll();
   }
 
   @Test
   public void testGetMaxNumChars_default() throws Exception {
     BaseObject articleObj = new BaseObject();
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
         "ArticleClass");
     articleObj.setXClassReference(articleClassRef);
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
     articleObj.setDocumentReference(articleDocRef);
     com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(articleObj, context);
     expect(xwiki.getSpacePreference(eq("default_language"), eq("News"), eq(""),
         same(context))).andReturn("de");
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("News"))).andReturn(null).anyTimes();
     replayAll();
     article.init(obj, "News");
     int maxNumChars = article.getMaxNumChars();
     assertEquals(250, maxNumChars);
     verifyAll();
   }
 
   @Test
   public void testGetMaxNumChars_blogConfig() throws Exception {
     BaseObject articleObj = new BaseObject();
     DocumentReference articleClassRef = new DocumentReference("xwikidb", "XWiki", 
         "ArticleClass");
     articleObj.setXClassReference(articleClassRef);
     DocumentReference articleDocRef = new DocumentReference("xwikidb", "News", "Bla");
     articleObj.setDocumentReference(articleDocRef);
     com.xpn.xwiki.api.Object obj = new com.xpn.xwiki.api.Object(articleObj, context);
     expect(xwiki.getSpacePreference(eq("default_language"), eq("News"), eq(""),
         same(context))).andReturn("de");
     DocumentReference blogDocRef = new DocumentReference(context.getDatabase(), "Content",
         "news");
     XWikiDocument blogDoc = new XWikiDocument(blogDocRef);
     BaseObject blogConfigObj = new BaseObject();
     DocumentReference blogConfigClassRef = new DocumentReference("xwikidb",
         BlogClasses.BLOG_CONFIG_CLASS_SPACE, BlogClasses.BLOG_CONFIG_CLASS_DOC);
     blogConfigObj.setXClassReference(blogConfigClassRef);
    blogConfigObj.setIntValue(BlogClasses.MAX_NUM_CHARS_FIELD, 1000);
     blogDoc.addXObject(blogConfigObj);
     expect(blogServiceMock.getBlogPageByBlogSpace(eq("News"))).andReturn(blogDoc
         ).anyTimes();
     replayAll();
     article.init(obj, "News");
     int maxNumChars = article.getMaxNumChars();
     assertEquals(1000, maxNumChars);
     verifyAll();
   }
 
   // Helper
   
   private void replayAll(Object ... mocks) {
     replay(xwiki, articleDoc, blogServiceMock);
     replay(mocks);
   }
 
   private void verifyAll(Object ... mocks) {
     verify(xwiki, articleDoc, blogServiceMock);
     verify(mocks);
   }
 
   String getLoremIpsum() {
     return "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo " +
     		"ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis " +
     		"parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, " +
     		"pellentesque eu, pretiumsquis, sem. Nulla consequat massa quis enim. Donec " +
     		"pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, " +
     		"rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede " +
     		"mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper " +
     		"nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, " +
     		"consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra " +
     		"quis, feugiat a, tellus.";
   }
 }
