 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.db.domain;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 /**
  * Scrobble document.<p>
  * Represents Scrobble database model.
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 public class ScrobbleDocument extends CouchDBDocument {
     
     /**
      * @see #getUserId()
      */
     @JsonProperty("user_id")
     private String userId;
     
     /**
      * @see #getScrobbleStartDatetime()
      */
     @JsonProperty("scrobble_start_datetime")
     private int[] scrobbleStartDatetime;
     
     /**
      * @see #getScrobbleEndDatetime()
      */
     @JsonProperty("scrobble_end_datetime")
     private int[] scrobbleEndDatetime;
     
     /**
      * @see #getScrobbleDuration()
      */
     @JsonProperty("scrobble_duration")
     private Duration scrobbleDuration;
     
     /**
      * @see #getTrackId()
      */
     @JsonProperty
     private String trackId;
 
     /**
      * Returns identifier of user associated with scrobble event.
      * 
      * @return identifier of user associated with scrobble event
      */
     public String getUserId() {
         return userId;
     }
 
     /**
      * @param userId
      * @see #getUserId()
      */
     public void setUserId(String userId) {
         this.userId = userId;
     }
 
     /**
      * Returns date and time (UTC) when scrobble event was initiated.<p>
      * Array content is as follows: <code>[yyyy,MM,dd,HH,mm,ss,SSS]</code>.
      * 
      * @return date and time (UTC) when scrobble event was initiated
      */
     public int[] getScrobbleStartDatetime() {
         return scrobbleStartDatetime;
     }
 
     /**
      * @param scrobbleStartDatetime
      * @see #getScrobbleStartDatetime()
      */
     public void setScrobbleStartDatetime(int[] scrobbleStartDatetime) {
         this.scrobbleStartDatetime = scrobbleStartDatetime;
     }
 
     /**
      * Returns date and time (UTC) when scrobble event was finished.<p>
      * Array content is as follows: <code>[yyyy,MM,dd,HH,mm,ss,SSS]</code>.
      * 
      * @return date and time (UTC) when scrobble event was finished
      */
     public int[] getScrobbleEndDatetime() {
         return scrobbleEndDatetime;
     }
 
     /**
      * @param scrobbleEndDatetime
      * @see #getScrobbleEndDatetime()
      */
     public void setScrobbleEndDatetime(int[] scrobbleEndDatetime) {
         this.scrobbleEndDatetime = scrobbleEndDatetime;
     }
 
     /**
      * Returns scrobble length.
      * 
      * @return scrobble length
      */
     public Duration getScrobbleDuration() {
         return scrobbleDuration;
     }
 
     /**
      * @param scrobbleDuration
      * @see #getScrobbleDuration()
      */
     public void setScrobbleDuration(Duration scrobbleDuration) {
         this.scrobbleDuration = scrobbleDuration;
     }
 
     /**
      * Returns identifier of track associated with scrobble event.
      * 
      * @return identifier of track associated with scrobble event
      */
     public String getTrackId() {
         return trackId;
     }
 
     /**
      * @param trackId
      * @see #getTrackId()
      */
     public void setTrackId(String trackId) {
         this.trackId = trackId;
     }
 
     @Override
     public String toString() {
         return String.format("{id=%s, start=%s, userId=%s, trackId=%s}",
                getId(), scrobbleStartDatetime, userId, trackId);
     }
     
 }
