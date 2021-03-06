 
 package pairPackage;
 import java.util.*;
 import java.io.*;
 
 public class Person
 {
    String  name = "Agent Smith";
    int     age  = 999;
    char    gender = 'M';
    String  title = "Mr. Smith";
     //String major in subclass
 
     String MrSmith = "Agent Smith,999,M,Mr. Smith";
     
     public void initialize()
     {
         createPerson( MrSmith );
     }
     
     public void createPerson( String aStr )
     {
         initialize();
         findName(   aStr );
         findAge(    aStr );
         findGend(   aStr );
         findTitle(  aStr );
     }
 
     public String parseComma( String ini, int c  )
     {
         String tmpStr;
         for(int i = 0; c >= 0; i++)
         {
             if( c == 0 )
                 tmpStr += ini.charAt(i);            
            
             if( ini.charAt(i) == ',' )
                 c--;
         }
         return tmpStr;
     }
 
     public void findName( String ini )
     {
         name = parseComma( ini, 0 );
     }
 
     public void findAge( String ini )
     {
         String tmpStr = parseComma( ini, 1 );
         age = Integer.valueOf( tmpStr );   
     }
 
     public void findGend( String ini )
     {
         String tmpStr;
         tmpStr = parseComma( ini, 2 );
         gender = tmpStr.charAt(0);
     }
     
     public void findTitle( String ini )
     {
         title = parseComma( ini, 3 );    
     } 
 }
