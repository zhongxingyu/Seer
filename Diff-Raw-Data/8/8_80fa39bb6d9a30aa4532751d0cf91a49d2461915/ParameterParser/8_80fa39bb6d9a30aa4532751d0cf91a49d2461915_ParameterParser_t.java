 package org.dt.japper;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /*
  * Copyright (c) 2012, David Sykes and Tomasz Orzechowski 
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  * 
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * - Neither the name David Sykes nor Tomasz Orzechowski may be used to endorse
  * or promote products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE. * @author Administrator
  * 
  * 
  */
 
 
 public class ParameterParser {
 
   private String originalSql;
   private String resultSql;
   
   private Map<String, List<Integer>> paramMap = new HashMap<String, List<Integer>>();
   
   public ParameterParser(String query) {
     this.originalSql = query;
   }
   
   public ParameterParser parse() {
     
     for (int index = 0; index < originalSql.length(); ) {
       int ch = originalSql.charAt(index);
       boolean consume = handleChar(ch);
       
       if (consume) index++;
     }
     handleChar(EOS);
     
     resultSql = sql.toString();
     
     return this;
   }
   
  public List<Integer> getIndexes(String name) {
    List<Integer> indexes = paramMap.get(name.toLowerCase());
    if (indexes == null) {
      return null;
    }
    return Collections.unmodifiableList(indexes); 
  }
   
   public String getSql() { return resultSql; }
   
   private static enum State { IN_SQL, PARAM_START, PARAM_NAME, IN_SINGLE, IN_DOUBLE }
   private State state = State.IN_SQL;
   
   private static final int EOS = -1;
   private StringBuffer sql = new StringBuffer();
   private StringBuffer paramName = new StringBuffer();
   private int paramIndex = 1;
   
   private boolean handleChar(int ch) {
     switch(state) {
       case IN_SQL:
         return handleInSQL(ch);
         
       case PARAM_START:
         return handleParamStart(ch);
         
       case PARAM_NAME:
         return handleParamName(ch);
         
       case IN_DOUBLE:
         return handleInDouble(ch);
         
       case IN_SINGLE:
         return handleInSingle(ch);
     }
     
     throw new IllegalStateException("In state "+state+", could not process char "+((char) ch));
   }
   
   private boolean handleInSQL(int ch) {
     if (ch == EOS) {
       return true;
     }
     
     if (ch == ':') {
       state = State.PARAM_START;
       return true;
     }
     
     addToResult(ch);
     
     if (ch == '\'') {
       state = State.IN_SINGLE;
     }
     else if (ch == '"') {
       state = State.IN_DOUBLE;
     }
     
     return true;
   }
 
   private boolean handleParamStart(int ch) {
     if (Character.isLetter((char) ch)) {
       paramName.setLength(0);
       paramName.append((char) ch);
       state = State.PARAM_NAME;
       return true;
     }
     
     // This is basically an unknown use of the colon - just add it to the output
     // and return to the base state
     addToResult(':');
     addToResult(ch);
     state = State.IN_SQL;
     return true;
   }
   
   private boolean handleParamName(int ch) {
     if (Character.isLetterOrDigit((char) ch) || ch == '_') {
       paramName.append((char) ch);
       return true;
     }
     
     addParamRef();
     state = State.IN_SQL;
     return false;       // we need to process this character again, but from the IN_SQL state
   }
   
   private void addParamRef() {
     String name = paramName.toString().toLowerCase();
     List<Integer> refs = paramMap.get(name);
     if (refs == null) {
       refs = new ArrayList<Integer>();
       paramMap.put(name, refs);
     }
     refs.add(paramIndex++);
     
     addToResult('?');
   }
   
   private boolean handleInDouble(int ch) {
     if (ch == '"') {
       state = State.IN_SQL;
     }
     
     addToResult(ch);
     return true;
   }
   
   private boolean handleInSingle(int ch) {
     if (ch == '\'') {
       state = State.IN_SQL;
     }
     
     addToResult(ch);
     return true;
   }
   
   private void addToResult(int ch) {
     if (ch != EOS) sql.append((char) ch);
   }
 }
