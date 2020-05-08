 /******************************************************************************
  *    FILE: IO.java                                                           *
  *    AUTHOR: David L Patrzeba                                                *
  *    E-MAIL: david.patrzeba@gmail.com                                        *
  *                                                                            *
  *    This file is not production code and is merely an academic exercise;    *
  *    that said, if you find something useful you may use this code in its    *
  *    entirety under the following license:                                   *
  *                                                                            *
  *    The MIT Liscense                                                        *
  *    Copyright (c) 2012 David L patrzeba                                     *
  *                                                                            *
  *    Permission is hereby granted, free of charge, to any person obtaining a *
  *    copy of this software and associated documentation files (the           *
  *    "Software"), to deal in the Software without restriction, including     *
  *    without limitation the rights to use, copy, modify, merge, publish,     *
  *    distribute, sublicense, and/or sell copies of the Software, and to      *
  *    permit persons to whom the Software is furnished to do so, subject to   *
  *    the following conditions:                                               *
  *                                                                            *
  *    The above copyright notice and this permission notice shall be included *
  *    in all copies or substantial portions of the Software.                  *
  *                                                                            *
  *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS *
  *    OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF              *
  *    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  *
  *    IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY    *
  *    CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,    *
  *    TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE       *
  *    SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                  *
  *                                                                            *
  ******************************************************************************/
 
 import java.io.Console;
 import java.io.InputStreamBuffer;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 public class IO {
 
    private static BufferedReader input = 
       new BufferedReader(new InputStreamBuffer(System.in));   
 
    private IO() {
       //should never be called
    }
 
    /***************************************************************************
     *NUMBERS - Imteger Types int, long, byte                                  *
     ***************************************************************************/
 
    public static int readInt(Sting prompt) {
 
        while(true) {
 
          try {
             System.out.print(prompt + " ");
             return Integer.parseInt(input.readLine());
          } catch(NumberFormatException e){
             System.err.print("Not a valid integer: ");
          } catch(IOException e) {
             //should never execute
             System.err.println("Unexpected IO Error " + e);
          }
       }
    }
 
     public static long readLong(String prompt) {
 
       while(true) {
 
          try {
             System.out.print(prompt + ": ");
             return Long.pareLong(input.readLine());
          } catch(NumberFormatException e) {
             System.err.print("Not a valid long: ");
          } catch (IOException e) {
             //should never execute
             System.err.println("Unexpected IO Error " + e);
          }
       }
    }
 
    public static byte readByte(String prompt) {
 
       while (true) {
          try {
             return Byte.parseByte(input.readLine());
          } catch(NumberFormatException e) {
             System.out.print("Not a valid byte: ");
          } catch(IOException e) {
             //should never execute
             System.err.println("Unexpected IO Error " + e);
          }
       }
    }
 
    public static int readHiddenInt(String prompt) {
 
       Console console = System.console();
       char[] chInput;
       String strInput;
 
       while(true) {
 
          try {
             chInput = console.readPassword(prompt + ": ");
             strInput = new String(chInput);
             return Integer.parseInt(strInput);
          } catch(Exception e) {
            System.err.println("Not a valid number: ");
          }
       }
    }
 
    /***************************************************************************
     *NUMBERS - Floating Point Types double, float                             *                                                              *
     ***************************************************************************/
 
    public static double readDouble(String prompt) {
 
       while(true) {
          try {
             System.out.print(prompt + " ");
             return Double.parseDouble(input.readLine());
          } catch(NumberFormatException e) {
             System.err.println("Not a valid double: ");
          } catch(IOException e) {
             //should never execute
             System.err.println("Unexpected IO Error: " + e);
          }
       }
    }
 
    public static float readFloat(String prompt) {
 
       while(true) {
 
          try {
             System.out.print(prompt + " ");
             return Float.valueOf(input.readLine());
          } catch(NumberFormatException e) {
             System.err.println("Not a valid double: ");
          } catch(IOException e) {
             //should never execute
             System.err.println("Unexpected IO Error: " + e);
          }
       }
    }
 
    /***************************************************************************
     *LETTERS - Types String, char                                             *                                                              *
     ***************************************************************************/
 
    public String readString(String prompt) {
 
       while(true) {
 
          try {
             System.out.print(prompt + " ");
             return input.readLine();
          } catch(IOException e) {
             //should never execute
             System.err.println("Unexpected IO Error: " + e);
          }
       }
    }
 
    public char readChar(String prompt) {
 
       String val;
 
       while(true) {
 
          try {
             val = input.readLine();
             if(val.length() != 1)
                continue;
             else
                return val.charAt(0);
          } catch(IOException e) {
             //should never execute
             System.err.println("Unexpected IO Error: " + e);
          }
          System.out.println("Not a valid char: ");
       }
    }
 
    public boolean readBool(String prompt) {
 
       String val;
 
       while(true) {
 
          try {
             val = input.readLine.toLowerCase();
 
             if(val.equals("yes") || val.equals("y") || 
                val.equals("t") || val.equals("true"))
                return true;
             else if(val.equals("no") || val.equals("n") ||
                val.equals("f") || val.equals("false"))
                return false;
             else
                continue;
          } catch(IOException e) {
             System.err.println("Unexpected IO error: " + e);
          }
 
          System.out.print("Not a valid boolean: ");
       }
    }
 }
