 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  Copyright (c) 2010, Janrain, Inc.
 
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation and/or
    other materials provided with the distribution.
  * Neither the name of the Janrain, Inc. nor the names of its
    contributors may be used to endorse or promote products derived from this
    software without specific prior written permission.
 
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 package com.janrain.android.engage.types;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * A link a user can use to take action on an activity update on the provider.
  *
  * Create an action link object, fill in the object's fields, and add the object
  * the JRActivityObject#action_links array of your JRActivityObject.
  *
  * Each action link must contain a link, \e href, and some \e text, describing what action
  * will happen if someone clicks the link.
  * Example:
  * <code>
  * action_links:
  * [
  *   {
  *     "text": "Rate this quiz result",
  *     "href": "http://example.com/quiz/12345/result/6789/rate"
  *   },
  *   {
  *     "text": "Take this quiz",
  *     "href": "http://example.com/quiz/12345/take"
  *   }
  * ]
  * </code>
  *
  */
 public class JRActionLink {
 
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
     /** The text describing the link */
     private String mText;
     /** A link a user can use to take action on an activity update on the provider */
     private String mHref;
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
     /**
      * Creates a <code>JRActionLink</code> initialized with the given text and href.
      *
      * @param text
      *   The text describing the link.  This value cannot be <code>null</code>.
      *
      * @param href
      *   A link a user can use to take action on an activity update on the provider.  This value
      *   cannot be <code>null</code>.
     * @throws IllegalArgumentException
     *   if text or href is null
      */
     public JRActionLink(String text, String href) {
        if (text == null || href == null) throw new IllegalArgumentException("illegal null text or null href");
         mText = text;
         mHref = href;
     }
 
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     public String getText() {
         return mText;
     }
 
     public void setText(String text) {
         mText = text;
     }
 
     public String getHref() {
         return mHref;
     }
 
     public void setHref(String href) {
         mHref = href;
     }
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
 
     /**
      * Returns a HashMap (Dictionary) representing the JRActionLink.
      *
      * @return
      *   An HashMap (Dictionary) of String objects representing the JRActionLink.
      *
      * NOTE: This function should not be used directly.  It is intended only for use by the
      * JREngage library.
      * 
      * TODO: revisit visibility/usage
      * specifically, is this the right jsonification
      */
     public Map<String, String> dictionaryForObject() {
         Map<String, String> map = new HashMap<String, String>(1);
         map.put(mText, mHref);
         return map;
     }
 }
