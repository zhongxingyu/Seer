 package com.centaurean.clmax.schema.platforms;
 
 import com.centaurean.clmax.schema.CL;
 import com.centaurean.commons.utilities.Transform;
 
 import java.util.Hashtable;
 import java.util.NoSuchElementException;
 
 /*
  * Copyright (c) 2013, Centaurean
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of Centaurean nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL Centaurean BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * CLmax
  *
  * 23/03/13 21:44
  * @author gpnuma
  */
 public class CLPlatforms extends Hashtable<Long, CLPlatform> {
     private static CLPlatforms platforms = null;
 
     public static CLPlatforms getPlatforms() {
         if(platforms == null) {
             long[] pointers = CL.getPlatformsNative();
             platforms = new CLPlatforms();
             for(long pointer : pointers)
                 platforms.add(new CLPlatform(pointer));
         }
         return platforms;
     }
 
     private CLPlatforms() {
         super();
     }
 
     public long[] getPointers() {
        return Transform.toLongArray(keySet());
     }
 
     public boolean add(CLPlatform platform) {
         CLPlatform found = put(platform.getPointer(), platform);
         return found == null;
     }
 
     public CLPlatform getFirst() {
         if(size() > 0)
             return values().iterator().next();
         else
             throw new NoSuchElementException();
     }
 }
