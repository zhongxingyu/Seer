 package com.eli.web.action;
 
 
 
 import com.eli.index.controller.CacheMemberDao;
 import com.eli.index.controller.Member;
 import com.eli.index.document.DiscussionDoc;
 import com.eli.index.manager.MultiNRTSearcherAgent;
 import com.eli.index.manager.ZhihuIndexManager;
 import com.eli.web.BasicAction;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.cjk.CJKAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.*;
 import org.apache.lucene.search.highlight.*;
 import org.apache.lucene.util.Version;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class MainSearch extends BasicAction {
     private static final Logger logger = Logger.getLogger(MainSearch.class);
     private static Analyzer analyzer= new CJKAnalyzer(Version.LUCENE_36);
     private CacheMemberDao memberDao = CacheMemberDao.INSTANCE;
     private DateFormat weiredFormat = new SimpleDateFormat("yyyyMMddHHmmss");
     private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 
     @Override
     protected void execute() throws IOException {
         String token = super.getParam("q", "");
         String avatar_url = null;
         int offset  = Integer.parseInt(super.getParam("offset", "0"));
         int limit  = Integer.parseInt(super.getParam("limit", "10"));
 	    int _type   = Integer.parseInt(super.getParam("type", "0"));
         String adv = super.getParam("adv", "no");
         String author = super.getParam("author", "").trim();
         String reply  = super.getParam("reply", "no");
 
         super.put("query", token);
         super.put("offset", offset);
         super.put("limit", limit);
     	super.put("type", _type);
         super.put("total", 0);
         super.put("page", 0);
 
         QueryParser qp = new QueryParser(Version.LUCENE_36, "content.NGRAM", analyzer);
         QueryParser qp1 = new QueryParser(Version.LUCENE_36, "title.NGRAM", analyzer);
         List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
         MultiNRTSearcherAgent agent = ZhihuIndexManager.INSTANCE.acquire();
         try{
             IndexSearcher searcher = agent.getSearcher();
             Query sub = new TermQuery(new Term("content.NGRAM", ""));
             Query sub1 = new TermQuery(new Term("title.NGRAM", ""));
             if (token.trim().length() != 0) {
                 sub = qp.parse(token);
                 sub1 = qp1.parse(token);
             }
             Query sub2 = new TermQuery(new Term("name.None", token));
 
             BooleanQuery query = new BooleanQuery();
             if (_type == 0) {
                 query.add(sub, BooleanClause.Occur.SHOULD);
                 query.add(sub1, BooleanClause.Occur.SHOULD);
                 query.add(sub2, BooleanClause.Occur.SHOULD);
 
                 if (adv.equals("yes") && author.length() > 0) {
                     int memberId = getMemberId(author);
                     if (memberId > 0) {
                         TermQuery specialNameQuery = new TermQuery(new Term("author.None", memberId + ""));
                         BooleanQuery tmpQuery = new BooleanQuery();
                         tmpQuery.add(specialNameQuery, BooleanClause.Occur.MUST);
                         if (token.trim().length() > 0)
                             tmpQuery.add(query, BooleanClause.Occur.MUST);
                         query = tmpQuery;
                     }
                 }
 
                 if (adv.equals("no") || (adv.equals("yes") && reply.equals("no"))) {
                     BooleanQuery tmpQuery = new BooleanQuery();
                    TermQuery filterQuery = new TermQuery(new Term("seqOfThread.None",  "0"));
                     tmpQuery.add(filterQuery, BooleanClause.Occur.MUST);
                     tmpQuery.add(query, BooleanClause.Occur.MUST);
                     query = tmpQuery;
                 }
             } else {
                 Query sub3 = new TermQuery(new Term("type.None", _type == 1 ? "topic" : "member"));
                 BooleanQuery sub4 = new BooleanQuery();
 
                 sub4.add(sub, BooleanClause.Occur.SHOULD);
                 sub4.add(sub1, BooleanClause.Occur.SHOULD);
                 sub4.add(sub2, BooleanClause.Occur.SHOULD);
 
                 query.add(sub3, BooleanClause.Occur.MUST);
                 query.add(sub4, BooleanClause.Occur.MUST);
             }
 
             TopDocs hits = searcher.search(query, offset + limit);
             QueryScorer scorer = new QueryScorer(query);
             Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"search-red\">", "</span>"), new SimpleHTMLEncoder(), scorer);
             highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, 60));
 
             super.put("total", hits.totalHits);
 
             for (int i = offset; i < hits.scoreDocs.length && i < offset + limit; i++) {
                 int docId = hits.scoreDocs[i].doc;
                 Document doc = searcher.doc(docId);
                 String content =  doc.get("content.NGRAM");
                 String title =  doc.get("title.NGRAM");
                 String type  = doc.get("type.None");
 
                 Map<String,Object> map = new HashMap<String, Object>();
 
                 if (type.equals("member")) {
                     content = "以琳用户";
                     title   = doc.get("name.None");
                     avatar_url = doc.get("avatar.None");
                     if (avatar_url == null || avatar_url.length() == 0)
                         avatar_url =  "http://new.elimautism.org/images/face/0003.gif";
                 } else {
                     if (title == null)
                         title = "";
                     if  (content == null)
                         content = "";
 
                     String hContent = getFragmentsWithHighlightedTerms(analyzer, query, "content.NGRAM", content,10, 70);
                     String hTitle  = getFragmentsWithHighlightedTerms(analyzer, query, "title.NGRAM", title, 10, 200);
                     logger.info(hTitle);
                     logger.info(hContent);
 
 
                     if (hTitle == null && title.length() == 0)
                         hTitle = "无标题";
                     else if(hTitle == null)
                         hTitle = title.substring(0, Math.min(25, title.length()));
                     if  (hContent == null && (content == null || content.length() == 0))
                         hContent = "无内容";
                     else if(hContent == null)
                         hContent = content.substring(0, Math.min(80, content.length()));
 
                     if (type.equals("topic"))
                         hTitle = "论坛板块:" + hTitle;
 
                     content = hContent;
                     title   = hTitle;
                     avatar_url = null;
 
                     if (type.equals("discussion")) {
                         DiscussionDoc discussion = new DiscussionDoc(doc);
                         map.put("created", formatWeiredDate(discussion.getDate()));
                         map.put("hits", discussion.getHits());
                         Member member = memberDao.getMember(discussion.author);
                         if (member != null) {
                             map.put("author_name", member.getName());
                             map.put("author_url", member.getUrl());
                         } else {
                             map.put("author_name", "以琳用户");
                             map.put("author_url", "#");
                         }
                     }
 
                 }
                 String url     =  doc.get("url.None");
                 map.put("content", content);
                 map.put("title", title);
                 map.put("url", url);
                 map.put("type", type);
                 if (avatar_url != null)
                     map.put("avatar", avatar_url);
                 ret.add(map);
             }
         } catch (Exception e) {
             logger.error(e);
         } finally {
             ZhihuIndexManager.INSTANCE.release(agent);
         }
 
         super.put("reply", reply);
         super.put("author", author);
         super.put("adv", adv);
         super.put("ret", ret);
 
     }
 
     public static String getFragmentsWithHighlightedTerms(Analyzer analyzer, Query query,
                                                      String fieldName, String fieldContents, int fragmentNumber, int fragmentSize) throws IOException, InvalidTokenOffsetsException {
 
         fieldContents = StringUtils.replaceEach(fieldContents, new String[]{"&", "\"", "<", ">"}, new String[]{"&amp;", "&quot;", "&lt;", "&gt;"});
     
         TokenStream stream = TokenSources.getTokenStream(fieldName, fieldContents, analyzer);
         QueryScorer scorer = new QueryScorer(query);
         Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmentSize);
 
 //        Highlighter highlighter = new Highlighter(scorer);
         Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"search-red\">", "</span>"), scorer);
         highlighter.setTextFragmenter(fragmenter);
         highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);
 
         String fragments = highlighter.getBestFragment(stream, fieldContents);
 
 
         return fragments;
     }
 
 
     public String formatWeiredDate(String wd) {
         try {
             Date date = weiredFormat.parse(wd);
             wd = format.format(date);
         } catch (Exception e) {
             logger.error(e);
         } finally {
             return wd;
         }
     }
 
     public int getMemberId(String query) {
         if (query != null) {
             query = query.trim();
             return memberDao.getMemberId(query);
         }
         return 0;
     }
 }
