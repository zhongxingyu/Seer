 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 
 package org.lafayette.server.core.config;
 
 import java.io.File;
 import java.net.URISyntaxException;
 
 /**
  * Configuration loader strategy for unit tests.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class TestStrategy implements LoaderStrategie {
 
     /**
      * Configuration file for unit tests.
     *
     * TODO USe this for configs of HSQLDEB.
      */
    private static final String FILE = "/org/lafayette/server/config/server.properties";
 
     @Override
     public boolean hasFoundConfig() {
         return true;
     }
 
     @Override
     public File getFoundConfig() {
         try {
             return new File(getClass().getResource(FILE).toURI());
         } catch (URISyntaxException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     @Override
     public void findConfig() {
         // Nothing to do.
     }
 
 }
