 /*
  * Copyright (C) 2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package testprogress2;
 
 import com.sun.javadoc.AnnotationDesc;
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.MethodDoc;
 
 /**
  *
  */
 public class MethodOriginator implements Originator {
     private final MethodDoc mMethod;
 
     private final String mComment;
 
     private final ClassDoc mClass;
 
     private String knownFailure = null;
 
     private String brokenTest = null;
 
     private String toBeFixed = null;
 
     /**
      * @param testMethod
      * @param clazz we need to provide the clazz since junit collects the
      *            testMethods from all super classes - and thus MethodDoc's
      *            class can point to a superclass. However, we want to know the
      *            class where the TestTargetClass is pointing to the API class.
      * @param comment
      */
     MethodOriginator(MethodDoc testMethod, ClassDoc clazz, String comment) {
         mMethod = testMethod;
         mComment = comment;
         mClass = clazz;
 
         AnnotationDesc[] annots = testMethod.annotations();
         for (AnnotationDesc annot : annots) {
             if (annot.annotationType().qualifiedName().equals(
                     "dalvik.annotation.KnownFailure")) {
                 knownFailure = "<b>@KnownFailure:</b>"
                         + (String)annot.elementValues()[0].value().value();
             } else if (annot.annotationType().qualifiedName().equals(
                     "dalvik.annotation.BrokenTest")) {
                 brokenTest = "<b>@BrokenTest:</b>"
                         + (String)annot.elementValues()[0].value().value();
             } else if (annot.annotationType().qualifiedName().equals(
                     "dalvik.annotation.ToBeFixed")) {
                toBeFixed = "<b>@ToBeFixed:</b>"
                        + (String)annot.elementValues()[0].value().value();
             }
             // else some other annotation - ignore
         }
     }
 
     /*
      * (non-Javadoc)
      * @see testprogress.Originator#asString()
      */
     public String asString() {
         return (mComment != null ? "comment:" + mComment + " - " : "")
                 + mClass.qualifiedName() + ": <b>" + mMethod.name() + "</b>"
                 + mMethod.signature()
                 + (brokenTest != null ? " [" + brokenTest + "]" : "")
                 + (toBeFixed != null ? " [" + toBeFixed + "]" : "")
                 + (knownFailure != null ? " [" + knownFailure + "]" : "");
     }
 
     public boolean isDisabled() {
         return mMethod.name().startsWith("_test");
     }
 
     public String getBrokenTest() {
         return brokenTest;
     }
 
     public String getToBeFixed() {
         return toBeFixed;
     }
 
     public String getKnownFailure() {
         return knownFailure;
     }
 
 }
