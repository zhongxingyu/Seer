 [+ autogen5 template +]
 /* -*- Mode: C; indent-tabs-mode: t; c-basic-offset: 4; tab-width: 4 -*- */
 /*
 * main.cc
  * Copyright (C) [+Author+] [+(shell "date +%Y")+] <[+Email+]>
  * 
 [+CASE (get "License") +]
[+ == "BSD"  +][+(bsd  "main.cc" (get "Author") " * ")+]
[+ == "LGPL" +][+(lgpl "main.cc" (get "Author") " * ")+]
[+ == "GPL"  +][+(gpl  "main.cc"                " * ")+]
 [+ESAC+] */
 
 class [+MainClass+] {
     public static void main(String[] args) {
         System.out.println("Hello World!");
     }
 }
