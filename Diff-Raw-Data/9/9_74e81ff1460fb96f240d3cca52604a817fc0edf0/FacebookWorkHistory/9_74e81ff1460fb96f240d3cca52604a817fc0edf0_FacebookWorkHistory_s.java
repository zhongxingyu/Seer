 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Joëll Portier
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.dare2date.domein.facebook;
 
 /**
  * Facebook work history.
  */
 public class FacebookWorkHistory {
     private String id;
     private String name;
 
     /**
      * Set Facebook work ID.
      * @param id Facebook ID.
      */
     public void setId(String id) {
         this.id = id;
     }
 
     /**
      * Get Facebook work ID.
      *
      * @return Facebook work ID.
      */
     public String getId() {
         return id;
     }
 
     /**
      * Name of the company department.
      *
      * @param name Name
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Company name.
      *
      * @return Name.
      */
     public String getName() {
         return name;
     }
 
     @Override
     public boolean equals(Object obj) {
        if (obj instanceof FacebookEvent) {
             FacebookWorkHistory other = (FacebookWorkHistory)obj;
 
             // Look only to the id.
             return other.id.equals(id);
         }
         return super.equals(obj);
     }
 }
