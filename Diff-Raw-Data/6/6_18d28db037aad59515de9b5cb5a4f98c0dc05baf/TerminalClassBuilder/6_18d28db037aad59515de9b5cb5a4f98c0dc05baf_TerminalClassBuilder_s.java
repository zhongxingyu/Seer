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
 
 package com.betterjargs.classbuilders;
 
 import com.betterjargs.BetterJargs;
 import com.betterjargs.output.CodeFormatter;
 import com.betterjargs.output.ImportOutput;
 import com.betterjargs.output.Output;
 import com.betterjargs.elements.ArgumentsElement;
 import com.betterjargs.elements.ArgumentElement;
 import java.util.Iterator;
 
 
 /**
  *
  * @author Joseph Spencer
  */
 public class TerminalClassBuilder {
 
    public static Object buildTerminalArguments(ArgumentsElement arguments){
       String indent = arguments.getIndent();
       String className = arguments.getTerminalClassName();
 
       BetterJargs.out("Building "+className+".java");
 
       CodeFormatter output = new CodeFormatter(indent);
       ImportOutput importOutput = new ImportOutput();
       CodeFormatter privateFieldOutput = new CodeFormatter(indent).addIndent(1);
       CodeFormatter variableOutput = new CodeFormatter(indent).addIndent(2);
       CodeFormatter testOutput = new CodeFormatter(indent).addIndent(3);
       CodeFormatter argumentsConstructorParamsOutput = new CodeFormatter(indent).addIndent(4);
       CodeFormatter getterOutput = new CodeFormatter(indent).addIndent(1);
 
       FileBuilderUtilities.buildPackage(output, arguments);
 
       output.
          add(importOutput).
          
          addLine("public class "+className+" {").
             add(privateFieldOutput).
             addLine().
             addIndent().
             addLine("public static "+arguments.getArgumentsClassName()+" getArguments(String[] args) throws IllegalArgumentException {").
             addIndent().
                add(variableOutput).
                addLine("if(__showHelpOnNoArgs && args.length == 0){").addIndent().
                   addLine("System.out.print("+arguments.getClassName()+"Help.getHelpMenu());").
                   addLine("System.exit(0);").removeIndent().
                addLine("}").
                addLine("int len = args.length;").
                addLine("int i=0;").
                addLine("for(;i+1<len;i+=2){").
                addIndent().
                   addLine("String key = args[i];").
                   addLine("String val = args[i+1];").
                add(testOutput).
                removeIndent().
                addLine("}").
                addLine("if(i - len != 0){").
                addIndent().
                   addLine("throw new IllegalArgumentException(\"An even number of arguments must be given.\");").
                removeIndent().
                addLine("}").
               addLine("return new "+arguments.getArgumentsClassName()+"(").addIndent().
                   add(argumentsConstructorParamsOutput).
                addLine(");").
                removeIndent().
             addLine("}").
             add(getterOutput).
             add(FileBuilderUtilities.getPathMethod(indent, 1)).
             add(FileBuilderUtilities.getGetBooleanMethod(indent, 1)).
             removeIndent().
          addLine("}");
 
       if(arguments.getHelp()){
          privateFieldOutput.addLine("private static final boolean __showHelpOnNoArgs=true;");
       } else {
          privateFieldOutput.addLine("private static final boolean __showHelpOnNoArgs;");
       }
 
       Iterator<ArgumentElement> args = arguments.getArgumentIterator();
       while(args.hasNext()){
          ArgumentElement arg = args.next();
 
          FileBuilderUtilities.buildImport(importOutput, arg);
          FileBuilderUtilities.buildVariable(variableOutput, arg, "", ";");
          buildAssignment(testOutput, arg);
          buildConstructorParam(argumentsConstructorParamsOutput, arg, args.hasNext() ? "," : "");
       }
       return output;
 
    }
 
 
    public static void buildAssignment(CodeFormatter out, ArgumentElement arg){
       String fieldType = arg.getFieldType();
       String fieldName = arg.getFieldName();
 
       out.addLine("if(\""+arg.getName()+"\".equals(key)){").
                addIndent();
          switch(arg.getType()){
          case "directory":
          case "file":
             out.
                addLine("String newPath = getPath(val);").
                addLine(fieldName+" = new "+fieldType+"(newPath);");
             break;
          case "boolean":
             out.addLine(fieldName+" = getBoolean(val);");
             break;
          }
 
       out.addLine("continue;").
          removeIndent().
          addLine("}");
    }
    public static void buildConstructorParam(CodeFormatter out, ArgumentElement arg, String seperator){
       String fieldName = arg.getFieldName();
       out.addLine(fieldName + seperator);
    }
 
 
 
 }
