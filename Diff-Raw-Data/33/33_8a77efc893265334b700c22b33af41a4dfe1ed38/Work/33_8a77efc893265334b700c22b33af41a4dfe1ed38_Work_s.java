 /*
  * Copyright (C) 2010 deNormans
  * http://www.denormans.com/
  * All rights reserved.
  *
  * This software is the confidential and proprietary information of deNormans ("Confidential Information"). You 
  * shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license
  * agreement you entered into with deNormans.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  * DENORMANS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.denormans.facebookgwt.api.client.graph.js;
 
 import com.denormans.facebookgwt.api.client.common.js.FBJSObject;
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 
 import java.util.Date;
 
 public class Work extends FBJSObject {
  public static final DateTimeFormat TimePeriodFormat = DateTimeFormat.getFormat("yyyy-MM");
 
   protected Work() {
   }
 
   public final native FBGraphObject getEmployer() /*-{
     return this.employer;
   }-*/;
 
   public final native FBGraphObject getLocation() /*-{
     return this.location;
   }-*/;
 
   public final native FBGraphObject getPosition() /*-{
     return this.position;
   }-*/;
 
   public final Date getStartDate() {
     return parseTimePeriodDate(getStartDateJS());
   }
 
   private native String getStartDateJS() /*-{
     return this.start_date;
   }-*/;
 
   public final Date getEndDate() {
     return parseTimePeriodDate(getEndDateJS());
   }
 
   private native String getEndDateJS() /*-{
     return this.end_date;
   }-*/;
 
   private Date parseTimePeriodDate(final String dateText) {
     if (dateText == null || "0000-00".equals(dateText)) {
       return null;
     }
 
    return TimePeriodFormat.parse(dateText);
   }
 }
