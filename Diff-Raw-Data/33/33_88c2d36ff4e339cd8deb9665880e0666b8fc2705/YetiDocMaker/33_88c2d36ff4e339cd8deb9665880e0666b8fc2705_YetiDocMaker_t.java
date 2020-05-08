 /*
  * Copyright (c) 2010,2011 --chris--
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. The name of the author may not be used to endorse or promote products
  *    derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package yeti.lang.compiler;
 
import java.io.File;
 import org.yetidoc.YetiDocModuleType;
 import org.yetidoc.YetiDocFieldType;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  *
  * @author Christian
  */
 public class YetiDocMaker {
     static public YetiDocModuleType[] docForSrcs(String[] basedirs, String[] classpath, String[] srcs) {
        //SourceReader reader = new DelegateSourceReader(new FileSourceReader(sourcepath),new ClassPathSourceReader(null));
        YetiC reader = new YetiC();
        reader.basedirs = basedirs;
        CompileCtx compileCtx = new CompileCtx(reader,null,new String[]{"yeti/lang/std","yeti/lang/io"}, new ClassFinder(classpath));
         YetiDocModuleType[] ret = new YetiDocModuleType[srcs.length];
         for(int i=0;i < srcs.length;i++){
             CompileCtx old = CompileCtx.current();
             CompileCtx.currentCompileCtx.set(compileCtx);
             try{
                 ret[i] = docFor(srcs[i],reader);
             }finally{
                 CompileCtx.currentCompileCtx.set(old);
             }
         }
         return ret;
     }
 
     static private YetiDocModuleType docFor(String fileName,SourceReader reader) {
         YetiDocModuleType ymt = null;
 
         Exception exception = null;
         String source = "";
         String doc = "";
         String name = "";
         String typeString ="";
         boolean struct = false;
 
         try{
             try{
                 String[] nameA = new String[]{fileName};
                 char[] sourceBytes = reader.getSource(nameA, false);
                 source = new String(sourceBytes);
             }catch(IOException ex) {
                 source = "";
             }
 
             ModuleType mt = YetiTypeVisitor.getType(null, fileName, true);
 
             doc = mt.topDoc == null ? "" : mt.topDoc;
            name = fileName.substring(0,fileName.lastIndexOf('.')).replace('/','.').replace(File.separatorChar, '.');
             //name = fileName.replace('/', '.');
 
             typeString = mt.type.toString();
             struct = mt.type.type == YetiType.STRUCT;
             
             ymt = new YetiDocModuleType(doc, source, typeString, name, fileName, struct, exception);
             if (struct) {
                 readStruct(mt.type,ymt);
             }
         }catch(Exception ex) {
             exception = ex;
             ymt = new YetiDocModuleType(doc, source, typeString, name, fileName, struct, exception);
         }
 
         return ymt;
     }
 
     static private void readStruct(YType tt,YetiDocModuleType ymt) {
         Map m = new java.util.TreeMap();
         if(tt.partialMembers != null)
             m.putAll(tt.partialMembers);
         if(tt.finalMembers != null)
             m.putAll(tt.finalMembers);
         Iterator i = m.entrySet().iterator();
 
         while(i.hasNext()) {
             Map.Entry e = (Map.Entry) i.next();
             YType t = (YType) e.getValue();
             
             String doc = t.doc == null ? "" : t.doc;
             String name = e.getKey().toString();
             String type = t.toString();
             boolean varField = t.field == YetiType.FIELD_MUTABLE;
             String finalPartialMember = "";
             if (tt.finalMembers == null || !tt.finalMembers.containsKey(e.getKey()))
                 finalPartialMember = ".";
             else if(tt.partialMembers != null && tt.partialMembers.containsKey(e.getKey()))
                 finalPartialMember = "`";
 
             boolean isFunction = t.type == YetiType.FUN;
             YetiDocFieldType yft = new YetiDocFieldType(doc, name, type, varField, finalPartialMember,isFunction);
             ymt.getFields().add(yft);
         }
     }
 
 
 }
 
 
 
 
