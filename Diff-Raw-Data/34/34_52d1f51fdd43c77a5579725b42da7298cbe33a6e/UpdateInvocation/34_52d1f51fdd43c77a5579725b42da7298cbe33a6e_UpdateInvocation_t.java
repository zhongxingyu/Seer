 /**
  * Copyright 2010 Marko Lavikainen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.contextfw.web.application.internal.service;
 
 public class UpdateInvocation {
 
    public static final UpdateInvocation DELAYED = new UpdateInvocation(true, false, false,null);
    public static final UpdateInvocation NOT_DELAYED = new UpdateInvocation(false, false, false, null);
    public static final UpdateInvocation NONE = new UpdateInvocation(false, false, true, null);
     
     private final boolean delayed;
    private final boolean cancelled;
     private final boolean resource;
     private final Object retVal;
     
    private UpdateInvocation(boolean delayed, boolean resource, boolean cancelled, Object retVal) {
         this.resource = resource;
         this.retVal = retVal;
         this.delayed = delayed;
        this.cancelled = cancelled;
     }
 
     public UpdateInvocation(boolean resource, Object retVal) {
        this(false, resource, false, retVal);
     }
     
     public boolean isResource() {
         return resource;
     }
 
     public Object getRetVal() {
         return retVal;
     }
 
     public boolean isDelayed() {
         return delayed;
     }

    public boolean isCancelled() {
        return cancelled;
    }
 }
