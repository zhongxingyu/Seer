 /**
  * The MIT License
  *
  * Copyright (c) 2011 Kevin Birch <kevin.birch@gmail.com>. Some rights reserved.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * Created: 9/25/11 12:00 AM
  */
 
 package com.webguys.djinn.marid;
 
 import java.io.IOException;
 
 import com.webguys.djinn.ifrit.model.Executable;
 import com.webguys.djinn.marid.runtime.*;
import org.apache.commons.lang3.StringUtils;
 import scala.tools.jline.console.ConsoleReader;
 
 public class REPL
 {
     private ConsoleReader reader;
     private Dictionary dictionary;
     private Context context;
     private Version version;
     private Runtime runtime;
 
     private boolean done;
 
     public REPL() throws Exception
     {
         this.version = new Version();
         this.reader = new ConsoleReader();
         Dictionary root = Dictionary.getRootDictionary();
 
         this.runtime = new Runtime();
         this.runtime.loadSourceFile("djinn/prelude.djinn", root);
 
         this.dictionary = root.newChild();
         this.context = new Context(new StandardStack(), this.dictionary, this.reader.getInput(), this.reader.getOutput());
     }
 
     private void run() throws Exception
     {
         this.reader.setPrompt("User> ");
 
         this.reader.println("Welcome to Djinn.");
         this.reader.println("Type \":quit\" or \":exit\" to end your session.  Type \":help\" for instructions.");
         this.reader.println();
 
         while(!this.done)
         {
             String input = this.read();
             String result = this.eval(input);
             this.print(result);
         }
     }
 
     private String read() throws IOException
     {
         return this.reader.readLine();
     }
 
     private String eval(String input) throws Exception
     {
         if(null == input)
         {
             this.reader.println("^D");
             return this.evalCommand(":exit");
         }
        else if(StringUtils.isBlank(input))
        {
            return input;
        }
         else if(input.startsWith(":"))
         {
             return this.evalCommand(input);
         }
         else
         {
             return this.evalStatement(input);
         }
     }
 
     private String evalCommand(String input) throws Exception
     {
         if(":quit".equalsIgnoreCase(input) || ":exit".equalsIgnoreCase(input))
         {
             this.done = true;
             return "goodbye.";
         }
         else if(":help".equalsIgnoreCase(input))
         {
             return "coming soon.";
         }
         else if(":version".equalsIgnoreCase(input))
         {
             return String.format("%s%n%s", this.version.getVersionDetails(), this.version.getBuildEnvironment());
         }
         else if(":no-warranty".equalsIgnoreCase(input))
         {
             return "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                    "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                    "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                    "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                    "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                    "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                    "SOFTWARE.";
         }
         else if(":stack".equalsIgnoreCase(input))
         {
             return "stack: " + this.context.getStack();
         }
         else if(":depth".equalsIgnoreCase(input))
         {
             return String.valueOf(this.context.getStack().depth());
         }
         else
         {
             return "error: command \"" + input.substring(1) + "\" was not understood";
         }
     }
 
     private String evalStatement(String input) throws Exception
     {
         try
         {
             Executable result = this.runtime.parseStatement(input, this.dictionary);
             if(result instanceof ImmediateStatement)
             {
                 result.execute(this.context);
                 return "stack: " + this.context.getStack();
             }
             else
             {
                 return "=> " + result.toString();
             }
         }
         catch(Exception e)
         {
             return "error: " + e.getMessage();
         }
     }
 
     private void print(String result) throws Exception
     {
         this.reader.println(result);
         this.reader.flush();
         this.reader.println();
     }
 
     public static void main(String[] args)
     {
         try
         {
             REPL repl = new REPL();
             repl.run();
         }
         catch(Throwable e)
         {
             System.err.println("error: Unhandled fatal exception.  No restarts available, exiting.");
             e.printStackTrace();
         }
     }
 }
