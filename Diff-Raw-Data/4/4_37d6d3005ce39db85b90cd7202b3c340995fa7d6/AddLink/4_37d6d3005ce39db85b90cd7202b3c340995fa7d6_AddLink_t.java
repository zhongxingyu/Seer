 /*
 Copyright (c) 2005-2006, Lucas Holt
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
 
 import com.justjournal.db.UserLinkDao;
 import com.justjournal.db.UserLinkTo;
 import org.apache.log4j.Category;
 
 /**
  * User: laffer1
  * Date: Dec 28, 2005
  * Time: 2:02:10 PM
  */
 public class AddLink extends Protected {
     private static Category log = Category.getInstance(AddLink.class.getName());
     protected String title;
     protected String uri;
 
     public String getMyLogin() {
         return this.currentLoginName();
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getUri() {
         return uri;
     }
 
     public void setUri(String uri) {
         this.uri = uri;
     }
 
     protected String insidePerform() throws Exception {
 
         if (log.isDebugEnabled())
             log.debug("insidePerform(): Attempting to add link.");
 
         if (this.currentLoginId() < 1)
             addError("login", "The login timed out or is invalid.");
 
         if (title == null || title.length() < 1)
             addError("title", "The link title is required.");
 
         if (uri == null || uri.length() < 11)
             addError("uri", "The URI is the website address and must be similar to http://www.justjournal.com/");
 
         if (!this.hasErrors()) {
             try {
                 UserLinkDao dao = new UserLinkDao();
                 UserLinkTo ul = new UserLinkTo();
 
                 ul.setTitle(title);
                 ul.setUri(uri);
                 ul.setUserId(this.currentLoginId());
 
                if (!dao.add(ul))
                    addError("Add Link", "Error adding link.");
             }
             catch (Exception e) {
                 addError("Add Link", "Could not add the link.");
                 if (log.isDebugEnabled())
                     log.debug("insidePerform(): " + e.getMessage());
             }
         }
 
         if (this.hasErrors())
             return ERROR;
         else
             return SUCCESS;
     }
 }
