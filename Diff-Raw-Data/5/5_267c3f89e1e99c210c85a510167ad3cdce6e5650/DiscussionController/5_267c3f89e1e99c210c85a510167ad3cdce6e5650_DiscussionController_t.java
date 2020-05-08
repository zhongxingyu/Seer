 package com.eli.index.controller;
 
 import com.eli.index.document.DiscussionDoc;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: shenchen
  * Date: 8/13/12
  * Time: 2:44 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DiscussionController {
     private static final Logger logger = Logger.getLogger(DiscussionController.class);
     private DiscussionDao discussionDao;
 
     public DiscussionController() {
         try {
             this.discussionDao = new FileDiscussionDao();
         } catch (IOException e) {
             logger.error(e);
         }
     }
 
     public Set<Integer> getTopicIds() {
         return this.discussionDao.getTopicIds();
     }
 
     public List<DiscussionDoc> getDiscussionDocs(int topicId) {
         List<DiscussionDoc> ret = new ArrayList<DiscussionDoc>();
         List<Discussion> ids = this.discussionDao.getDiscussion(topicId);
         for (int i = 0; i * 20 < ids.size(); i ++ ) {
             for (int j = 0; j < 20 && i * 20 + j < ids.size(); j ++) {
                 DiscussionDoc doc = new DiscussionDoc();
                 Discussion dis= ids.get(i * 20 + j);
                doc.url = "http://new.elimautism.org/a/a.asp?ID="+ dis.id + "&Ar=" + dis.topicSort +"&Aq=1" ;
                 doc.hits = dis.readCount;
                 doc.date = dis.date + "";
                 doc.author = dis.author;
                 if (dis.topicId == dis.id) {
                     doc.setBoost(1.2f);
                     doc.setSeqOfThread(0);
                 } else {
                    doc.setSeqOfThread(1);
                 }
 
                 if (dis.boardId == 78 || dis.boardId == 50 || dis.boardId == 444)
                     continue;
 
                 if (dis.title != null)
                     doc.title = dis.title;
                 doc.content = dis.content;
                 ret.add(doc);
             }
         }
         return ret;
     }
 }
