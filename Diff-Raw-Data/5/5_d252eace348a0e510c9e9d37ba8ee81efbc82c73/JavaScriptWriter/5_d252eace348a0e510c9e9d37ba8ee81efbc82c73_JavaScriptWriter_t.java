 package fi.vincit.jmobster.processor.languages.javascript.writer;/*
  * Copyright 2012 Juha Siponen
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
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 import fi.vincit.jmobster.processor.languages.BaseDataWriter;
 import fi.vincit.jmobster.util.itemprocessor.ItemHandler;
 import fi.vincit.jmobster.util.itemprocessor.ItemProcessor;
 import fi.vincit.jmobster.util.itemprocessor.ItemStatus;
 import fi.vincit.jmobster.util.writer.DataWriter;
 
 /**
  * Higher level abstraction for DataWriter that can write
  * JavaScript to DataWriter. By default the writer checks that
  * all functions and blocks are closed when the writer is closed.
  * This feature can be turned of with {@link JavaScriptWriter#lenientModeOn}.
  */
 @SuppressWarnings( "UnusedReturnValue" )
 public class JavaScriptWriter extends BaseDataWriter<JavaScriptWriter> {
 
     private static final String COMMENT_START = "/*";
     private static final String COMMENT_LINE_START = " * ";
     private static final String COMMENT_END = " */";
     private static final String VARIABLE = "var";
     private static final String BLOCK_START = "{";
     private static final String BLOCK_END = "}";
 
     private static final String ARRAY_START = "[";
     private static final String ARRAY_END = "]";
     private static final String ARRAY_SEPARATOR = ", ";
 
     private static final String FUNCTION_ARG_START = "(";
     private static final String FUNCTION_ARG_END = ")";
 
     private static final String KEY_VALUE_SEPARATOR = ": ";
     private static final String LIST_SEPARATOR = ",";
 
     private static final String FUNCTION_DEF = "function";
     private static final String STATEMENT_END = ";";
     private static final String QUOTE = "\"";
     private static final String ASSIGN = " = ";
     private static final char KEYWORD_SEPARATOR = ' ';
 
     private String space = " ";
 
     private boolean lenientModeOn = false;
 
     // Sanity checks.
     private int functionsOpen = 0;
     private int blocksOpen = 0;
     private int functionCallsOpen = 0;
     private boolean JSONmode = false;
 
     private abstract static class ItemWriter<T> implements ItemHandler<T> {
         private final JavaScriptWriter writer;
 
         ItemWriter( JavaScriptWriter writer ) {
             this.writer = writer;
         }
 
         JavaScriptWriter getWriter() {
             return writer;
         }
     }
 
     // TODO: Test that this works now as it should. Used to be as static variable which is VERY wrong
     private final ItemWriter<Object> arrayWriter = new ItemWriter<Object>(this) {
         @Override
         public void process( Object item, ItemStatus status ) {
             getWriter().write(item.toString(), ARRAY_SEPARATOR, !status.isLastItem());
         }
     };
 
 
 
     public JavaScriptWriter(DataWriter writer) {
         super(writer);
     }
 
     /**
      * Start named function and starts a new block.
      * @param name Function name
      * @param arguments Function arguments
      * @return Writer itself for chaining writes
      */
     public JavaScriptWriter startNamedFunction(String name, String... arguments) {
         return write(FUNCTION_DEF + space).writeFunctionArgsAndStartBlock(name, arguments);
     }
 
     /**
      * Start anonymous function and starts a new block.
      * @param arguments Function arguments
      * @return Writer itself for chaining writes
      */
     public JavaScriptWriter startAnonFunction(String... arguments) {
         return write(FUNCTION_DEF).writeFunctionArgsAndStartBlock( "", arguments );
     }
 
     /**
      * Writes function arguments and starts block
      * @param arguments Function arguments
      * @return Writer itself for chaining writes
      */
     private JavaScriptWriter writeFunctionArgsAndStartBlock(String name, String... arguments) {
         startFunctionCall(name);
         ItemHandler<String> argumentProcessor = new ItemHandler<String>() {
             @Override
             public void process(String argument, ItemStatus status) {
                 write(argument, LIST_SEPARATOR + space, status.isNotLastItem());
             }
         };
         ItemProcessor.process(argumentProcessor, arguments);
         endFunctionCall().writeSpace();
         return startBlock();
     }
 
     /**
      * Ends function and blocks.
      * @return Writer itself for chaining writes
      * @param status Writes list separator item is last
      */
     public JavaScriptWriter endFunction( ItemStatus status ) {
         --functionsOpen;
         return endBlock(status);
     }
 
     /**
      * Stars a new block. Following lines will be indented.
      * @return Writer itself for chaining writes
      */
     public JavaScriptWriter startBlock() {
         ++blocksOpen;
         return writeLine( BLOCK_START ).indent();
     }
 
     /**
      * Ends block. Indents back. Writes list separator (default ",") if isLast is false.
      * @param status Writes list separator if item is last
      * @return Writer itself for chaining writes
      */
     public JavaScriptWriter endBlock(ItemStatus status) {
         --blocksOpen;
         return indentBack().write( BLOCK_END ).writeLine("", LIST_SEPARATOR, status.isNotLastItem());
     }
 
     /**
      * Start function call with block statement inside
      * @param name Name of the function
      * @return Writer itself for chaining writes
      */
     public JavaScriptWriter startFunctionCallBlock(String name) {
         return startFunctionCall(name).startBlock();
     }
 
     /**
      * End function call with block statement
      * @param status Writes list separator if item is last
      * @return Writer itself for chaining writes
      */
     public JavaScriptWriter endFunctionCallBlock(ItemStatus status) {
         --blocksOpen;
         --functionCallsOpen;
         return indentBack().write( BLOCK_END + ")" ).writeLine( "", LIST_SEPARATOR, status.isNotLastItem() );
     }
 
     public JavaScriptWriter endBlockStatement() {
         write(BLOCK_END).endStatement();
         return this;
     }
 
     /**
      * Writes object key (also the separator, default ":")
      * @param key Key name
      * @return Writer itself for chaining writes
      */
     public JavaScriptWriter writeKey(String key) {
         return write("", QUOTE, JSONmode).write( key, QUOTE, JSONmode ).write( KEY_VALUE_SEPARATOR );
     }
 
     /**
      * Writes object key and value. Writes list separator (default ",") if isLast is false.
      * @param key Key name
      * @param value Value
      * @param status Writes list separator item is last
      * @return Writer itself for chaining writes
      */
     public JavaScriptWriter writeKeyValue(String key, String value, ItemStatus status) {
         return writeKey(key).write(value).writeLine("", LIST_SEPARATOR, status.isNotLastItem());
     }
 
     /**
      * Writes array of objects. Objects must have {@link Object#toString()} method
      * implemented.
      * @param status Writes list separator item is last
      * @param objects Objects to write
      * @return Writer itself for chaining writes
      */
     public JavaScriptWriter writeArray(ItemStatus status, Object... objects) {
         write(ARRAY_START);
         ItemProcessor.process(arrayWriter, objects);
         write(ARRAY_END);
         write("", LIST_SEPARATOR, status.isNotLastItem());
         writeLine("");
         return this;
     }
 
     public JavaScriptWriter writeVariable(String name, String value) {
         return writeVariable(name, value, VariableType.STRING);
     }
 
     public JavaScriptWriter endStatement() {
         return writeLine(STATEMENT_END);
     }
 
     public static enum VariableType {
         STRING, BLOCK, OTHER
     }
     public JavaScriptWriter writeVariable(String name, String value, VariableType type) {
         final String quoteMark = type == VariableType.STRING ? QUOTE : "";
         write(VARIABLE).write(KEYWORD_SEPARATOR).write(name).write(ASSIGN);
 
         if( type != VariableType.BLOCK ) {
             write(quoteMark).write(value).write(quoteMark).endStatement();
         } else {
             writeLine(BLOCK_START);
         }
 
         return this;
     }
 
     public JavaScriptWriter startFunctionCall(String functionName) {
         ++functionCallsOpen;
         write(functionName).write(FUNCTION_ARG_START);
         return this;
     }
 
     public JavaScriptWriter endFunctionCall() {
         --functionCallsOpen;
         write(FUNCTION_ARG_END);
         return this;
     }
 
     public JavaScriptWriter writeComment(String comment) {
         writeLine(COMMENT_START);
         write(COMMENT_LINE_START);
         writeLine(comment);
         writeLine(COMMENT_END);
         return this;
     }
 
     /**
      * Writes space. Use this to enable easy to setup compact writing that
      * can ignore spaces. To disable spaces use {@link JavaScriptWriter#setSpace(String)}
      * method.
      * @return Writer itself for chaining writes.
      */
     public JavaScriptWriter writeSpace() {
         write(space);
         return this;
     }
 
     /**
      * @throws IllegalStateException If lenient mode is on and the writer has unclosed functions or blocks.
      */
     @Override
     public void close() {
         super.close();
         if( !lenientModeOn ) {
             if( functionsOpen > 0 ) {
                throw new IllegalStateException("There are still " + functionsOpen + " unclosed functions");
             } else if( functionsOpen < 0 ) {
                 throw new IllegalStateException("Too many functions closed. " + Math.abs(functionsOpen) + " times too many.");
             }
 
             if( blocksOpen > 0 ) {
                throw new IllegalStateException("There are still " + blocksOpen + " unclosed blocks");
             } else if( blocksOpen < 0 ) {
                 throw new IllegalStateException("Too many blocks closed. " + Math.abs(blocksOpen) + " times too many.");
             }
 
             if( functionCallsOpen > 0 ) {
                 throw new IllegalStateException("There are still " + functionCallsOpen + " unclosed function calls");
             } else if( functionCallsOpen < 0 ) {
                 throw new IllegalStateException("Too many function calls closed. " + Math.abs(functionCallsOpen) + " times too many.");
             }
         }
     }
 
     // Setters and getters
 
     /**
      * If set to false (default) {@link JavaScriptWriter#close()} will
      * check that functions and blocks have been closed properly. If errors are found,
      * exception will be thrown. If set to true, no checks are made and no exceptions
      * are thrown.
      * @param lenientModeOn Should the writer ignore obvious errors.
      */
     public void setLenientMode( boolean lenientModeOn ) {
         this.lenientModeOn = lenientModeOn;
     }
 
     /**
      * Sets space sequence.
      * @param space Space sequence
      */
     public void setSpace(String space) {
         this.space = space;
     }
 
     public void setJSONmode(boolean JSONmode) {
         this.JSONmode = JSONmode;
     }
 }
