 /*
 Copyright (c) 2005, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.justjournal.ctl;
 
 import com.justjournal.db.CommentDao;
 import com.justjournal.db.EntryDAO;
 import com.justjournal.db.EntryTo;
 
 import java.util.Collection;
 
 /**
  * Created by IntelliJ IDEA.
  * User: laffer1
  * Date: Dec 31, 2003
  * Time: 3:25:21 PM
 * To change this template use Options | File Templates.
  */
 public class ViewComment extends ControllerAuth {
 
     protected EntryTo entry;
     protected Collection comments;
     protected int entryId;
 
 
     public EntryTo getEntry() {
         return this.entry;
     }
 
     public Collection getComments() {
         return this.comments;
     }
 
 
     public String getMyLogin() {
         return this.currentLoginName();
     }
 
     public int getEntryId() {
         return this.entryId;
     }
 
     public void setEntryId(int entryId) {
         this.entryId = entryId;
     }
 
 
     public String perform() throws Exception {
         CommentDao cdao = new CommentDao();
         EntryDAO edao = new EntryDAO();
 
         this.comments = cdao.view(entryId);
         this.entry = edao.viewSingle(entryId, false);
 
         return SUCCESS;
     }
 }
