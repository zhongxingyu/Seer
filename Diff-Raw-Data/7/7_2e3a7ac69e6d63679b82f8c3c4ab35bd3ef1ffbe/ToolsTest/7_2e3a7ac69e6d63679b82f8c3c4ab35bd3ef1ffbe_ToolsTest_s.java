 /*
  *    FILE: ToolsTest.java
  *    AUTHOR: David L Patrzeba
  *    E-MAIL: david.patrzeba@gmail.com
  *
  *    This is the code to test the files in the JavaTools folder.
  *    This code may be used under the following liscense:
  *
  *    The MIT Liscense
  *    Copyright (c) 2012 David L Patrzeba
  *
  *    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
  *    associated documentation files (the "Software"), to deal in the Software without restriction, including
  *    without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *    copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
  *    following conditions:
  *
  *    The above copyright notice and this permission notice shall be included in all
  *    copies or substantial portions of the Software.
  *
  *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
  *    LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
  *    EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  *    IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
  *    THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  */
 public class ToolsTest{
 
    public static void main(String[] args){
 
       //test insertion sorts
       Integer[] array = {8, 10, 5, 87, 56, 12, 54, 23, 100, 1, 7, 99};
      DavesSearch.insertionSort(array);
       for(Integer x : array){
          System.out.printf("%d ",x);
       }
 
       System.out.println();
 
      DavesSearch.insertionSortD(array);
       for(Integer x : array){
          System.out.printf("%d ",x);
       }
 
       System.out.println();
 
       //end insertion sort tests
 
 
 
       StarChar star = new StarChar();
       star.getString("Hello World Encyclopedia and then i went to georgia the quick brown fox jumps over the lazy dog");
 
 
    }//end of method main
 }//end class ToolsTest
