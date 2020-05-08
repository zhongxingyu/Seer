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
 
 
 import android.graphics.Bitmap;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  * Image object to be included in a post to a user's stream.
  *
  * Create an image media object, fill in the object's fields, and add the object to the
  * JRActivityObject#media array in your JRActivityObject.  How the images get presented
  * and whether or not they are used, depend on the provider.
  *
  * Each image must contain a src URL, which maps to the photo's URL, and an href URL, which maps
  * to the URL where a user should be taken if he or she clicks the photo.
  *
  * Format and rules are identical to those described on the <a
  * href="http://developers.facebook.com/docs/guides/attachments">Facebook Developer page on
  * Attachments</a>.
  */
 public class
         JRImageMediaObject extends JRMediaObject {
 
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
     private String mSrc;
     private String mHref;
     private Bitmap mPreview;
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
     /**
      * Creates a <code>JRImageMediaObject</code> initialized with the given src and href.
      *
      * @param src
      *   The photo's URL.  This value cannot be <code>null</code>.
      *
      * @param href
      *   The URL where a user should be taken if he or she clicks the photo.  This value cannot
      *   be <code>null</code>.
      *
      * @throws IllegalArgumentException
     *   if swfSrc or imgSrc are null
      */
     public JRImageMediaObject(String src, String href) {
         // TODO: if null/empty should we throw?
         if (src == null || href == null) throw new IllegalArgumentException("illegal null src or href");
         mSrc = src;
         mHref = href;
     }
 
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     public String getSrc() {  /* (readonly) */
         return mSrc;
     }
 
     public String getHref() {  /* (readonly) */
         return mHref;
     }
 
     @JsonIgnore
     public Bitmap getPreview() {
         return mPreview;
     }
 
     public void setPreview(Bitmap preview) {
         mPreview = preview;
     }
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
     public boolean hasThumbnail() {
         return true;
     }
 
     public String getThumbnail() {
         return mSrc;
     }
 
     public String getType() {
         return "image";
     }
     
 }
