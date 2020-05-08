 /* 
  *  Copyright 2012 Zachary Richey <zr.public@gmail.com>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 /* Useful static methods for using the library quickly. */
 package com.github.zachuorice.brainfuh;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import com.github.zachuorice.brainfuh.*;
 
 public class Brainfuh
 {
     static void executeFile(File code) throws IOException
     {
         if(!code.canRead() || !code.isFile())
             throw new IOException();
         FileReader code_reader = new FileReader(code);
         int data = code_reader.read();
         Interpreter interpreter = new Interpreter();
         while(data != -1)
             interpreter.feed((char ) data);
         interpreter.execute();
     }
 
     static void executeString(String code)
     {
         Interpreter interpreter = new Interpreter();
         interpreter.feed(code);
         interpreter.execute();
     }
 }
