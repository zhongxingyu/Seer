 /*
  * Copyright 2012 Joseph Spencer.
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
 
 package com.xforj.productions;
 
 import com.xforj.Output;
 import com.xforj.CharWrapper;
 import com.sun.org.apache.xalan.internal.xsltc.compiler.Template;
 import java.util.regex.Matcher;
 
 /**
  *
  * @author Joseph Spencer
  */
 public class TemplateDeclaration extends Production {
    Output templateBodyOutput = new Output();
    public TemplateDeclaration(Output output) {
       super(output);
    }
 
    private boolean isOpened;
    private boolean expectingTemplateBody;
    private boolean allowVariableDeclarations;
 
    @Override
    public void execute(CharWrapper characters, ProductionContext context) throws Exception {
      String extraExcMsg="";
       characters.removeSpace();
 
       if(characters.charAt(0) == open){
          if(!isOpened){
             isOpened=true;
             characters.shift(1);
             context.addVaribleOutput();
 
             Matcher template = characters.match(TEMPLATE);
             if(template.find()){
                characters.shift(template.group(1).length());
 
                if(characters.removeSpace()){
                   Matcher name = characters.match(NAME);
                   if(name.find()){
                      String nm = name.group(1);
 
                      characters.shift(nm.length());
                      output.
                         prepend(
                            js_currentNS+"."+nm+"=function("+js__data+", "+js__params+"){"+
                               "var "+js_context+"="+js__data+"||{},"+
                                  js_params+"="+js__params+"||{},"+
                                  js_bld+"="+js_StringBuffer+"(),"+
                                  js_last+"=''/0,"+
                                  js_position+"="+js_last+";"
                         ).
                         prepend(context.getCurrentVariableOutput()).
                         prepend(templateBodyOutput).
                         prepend("return "+js_bld+".toString()};");
 
                      if(characters.charAt(0) == close){
                         characters.shift(1);
                         context.addProduction(new ParamDeclarations(context.getCurrentVariableOutput()));                           
 
                         allowVariableDeclarations=true;
                         expectingTemplateBody=true;
                         return;
                      }
                   }
                }
             }
          } else if(allowVariableDeclarations && characters.charAt(1) == v){
             context.addProduction(new VariableDeclarations(context.getCurrentVariableOutput()));
             allowVariableDeclarations=false;
             expectingTemplateBody=true;
             return;
          } else if(characters.charAt(1) == forward){
            Matcher template = characters.match(TEMPLATE_CLOSING);
             if(template.find()){
                characters.shift(template.group(1).length());
               context.removeProduction();
               context.removeVariableOutput();
               return;
            } else {
               extraExcMsg="  Template Declarations must be followed by '{/template}.";
             }
          } else if(expectingTemplateBody){
             evaluateTemplateBody(context);
             return;
          }
 
       } else if(expectingTemplateBody){
          evaluateTemplateBody(context);
          return;
       }
 
      throw new Exception("Invalid TemplateDeclaration." + extraExcMsg);
    }
 
    private void evaluateTemplateBody(ProductionContext context){
       expectingTemplateBody=false;
       context.addProduction(new TemplateBody(templateBodyOutput));
    }
 }
