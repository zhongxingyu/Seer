 [+ autogen5 template +]
 /* -*- Mode: C; indent-tabs-mode: t; c-basic-offset: 4; tab-width: 4 -*- */
 /*
 * [+MainClass+].java
  * Copyright (C) [+Author+] [+(shell "date +%Y")+] <[+Email+]>
  * 
 [+CASE (get "License") +]
[+ == "BSD"  +][+(bsd  "main.java" (get "Author") " * ")+]
[+ == "LGPL" +][+(lgpl "main.java" (get "Author") " * ")+]
[+ == "GPL"  +][+(gpl  "main.java"                " * ")+]
 [+ESAC+] */
 
 class [+MainClass+] {
     public static void main(String[] args) {
         System.out.println("Hello World!");
     }
 }
