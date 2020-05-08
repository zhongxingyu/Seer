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
 
 
 /**
  * Mp3 object to be included in a post to a user's stream.
  *
  * Create an mp3 media object, fill in the object's fields, and add the object to the
  * JRActivityObject#media array in your JRActivityObject.  How the mp3s get presented
  * and whether or not they are used, depend on the provider.
  *
  * Each mp3 must contain a src url, which is the URL of the MP3 file to be rendered.
  * The mp3 can also include a title, artist, and album.
  *
  * NOTE: You can only include one JRMp3MediaObject in the media array.  Any others
  * will be ignored.
  *
  * Format and rules are identical to those described on the <a
  * href="http://developers.facebook.com/docs/guides/attachments">Facebook Developer page on
  * Attachments</a>.
  */
 public class JRMp3MediaObject extends JRMediaObject {
 
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
     /** The URL of the MP3 file to be rendered */
     private String mSrc;
     /** The title of the song */
     private String mTitle;
     /** The artist */
     private String mArtist;
     /** The album */
     private String mAlbum;
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
     /**
      * Creates a <code>JRMp3MediaObject</code> initialized with the given src.
      *
      * @param src
      *   The URL of the MP3 file to be rendered.  This value cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *   if src is null
      */
     public JRMp3MediaObject(String src) {
         // TODO: if null/empty should we throw?
         if (src == null) throw new IllegalArgumentException("illegal src for JRMp3MediaObject constructor");
         mSrc = src;
     }
 
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     public String getSrc() {  /* (readonly) */
         return mSrc;
     }
 
     public String getTitle() {
         return mTitle;
     }
 
     public void setTitle(String title) {
         mTitle = title;
     }
 
     public String getArtist() {
         return mArtist;
     }
 
     public void setArtist(String artist) {
         mArtist = artist;
     }
 
     public String getAlbum() {
         return mAlbum;
     }
 
     public void setAlbum(String album) {
         mAlbum = album;
     }
 
     public String getType() {
         return "music";
     }
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
 }
