 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Eclipse Public License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.eclipse.org/org/documents/epl-v10.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.ide.eclipse.adt.internal.build;
 
 /**
  * Base exception class containing the error code and output of an external tool failed exec.
  *
  */
 class ExecResultException extends Exception {
     private static final long serialVersionUID = 1L;
 
     private final int mErrorCode;
     private final String[] mOutput;
 
     protected ExecResultException(int errorCode, String[] output) {
         mErrorCode = errorCode;
         mOutput = output;
     }
 
     /**
      * Returns the full output of aapt.
      */
     public String[] getOutput() {
         return mOutput;
     }
 
     /**
      * Returns the aapt return code.
      */
     public int getErrorCode() {
         return mErrorCode;
     }
 }
