 // Copyright © 2009, Esko Luontola. All Rights Reserved.
 // This software is released under the MIT License. See LICENSE.txt
 
 package longcat;
 
 public class Cli {

     public static void main(String[] args) {
        try {
            System.out.print(build().parseArguments(args));
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
     }
 
     private static CliArgumentParser build() {
         LongcatFactory factory = new LongcatFactoryImpl();
         return new CliArgumentParser(factory);
     }
 }
