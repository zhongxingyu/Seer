 package com.arlandis;
 
 public class CommandLineParser {
 
     private String[] argsToParse;
     
     public CommandLineParser(String[] args){
        argsToParse = args;
     }
 
     public Integer portNum() {
 
         Integer port;
 
         if ( (argsToParse.length > 0) &&
               argsToParse[0].equals("-p")){
            port = Integer.parseInt(argsToParse[1]);
         } else {
             port = 8000;
         }
         
         return port;
     }
 }
