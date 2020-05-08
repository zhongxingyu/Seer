 /*
  * The MIT License
  *
  * Copyright 2013 Jason Unger <entityreborn@gmail.com>.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.entityreborn.socbot;
 
 /**
  *
  * @author Jason Unger <entityreborn@gmail.com>
  */
 public enum Colors {
     WHITE("0"),
     BLACK("1"),
     DARKBLUE("2"),
     DARKGREEN("3"),
     RED("4"),
     DARKRED("5"),
     DARKVIOLET("6"),
     ORANGE("7"),
     YELLOW("8"),
     LIGHTGREEN("9"),
     CYAN("10"),
     LIGHTCYAN("11"),
     BLUE("12"),
     VIOLET("13"),
     DARKGREY("14"),
     LIGHTGREY("15"),
     DEFAULT("");
 
     String foreground;
     Colors background;
 
     private Colors(String i) {
         foreground = i;
     }
 
     public String value() {
         return foreground;
     }
 
     public Colors setBackground(Colors col) {
         background = col;
 
         return this;
     }
 
     public Colors setBackground(String col) {
        return setBackground(Colors.valueOf(col.toUpperCase()));
     }
 
    public String toColor() {
         if (background != null) {
             return "\u0003" + value() + "," + background.value();
         } else {
             return "\u0003" + value();
         }
     }
 
     public static String removeAll(String line) {
         line = line.replaceAll("\u0003[0-15](,[0-15])?", line);
 
         return line;
     }
 }
