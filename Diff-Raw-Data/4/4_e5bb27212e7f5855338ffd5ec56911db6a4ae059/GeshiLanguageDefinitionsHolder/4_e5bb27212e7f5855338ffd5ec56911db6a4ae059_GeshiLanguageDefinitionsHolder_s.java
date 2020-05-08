 /**
  * Copyright (c) 2013, ciplogic.com - Bogdan Mustiata. All rights reserved.
  * For licensing, see the LICENSE file from the project's root folder.
  */
 package com.ciplogic.web.codeeditor.render.geshi;
 
 import com.ciplogic.gwtui.ProgrammingLanguage;
 import com.ciplogic.web.codeeditor.render.languages.LanguageDefinitionsHolder;
 
 public class GeshiLanguageDefinitionsHolder extends LanguageDefinitionsHolder {
     public GeshiLanguageDefinitionsHolder() {
         addLanguage("Text", "text", ProgrammingLanguage.TEXT);
         addLanguage("Visual Basic (.NET)", "vbnet", ProgrammingLanguage.VB_NET);
         addLanguage("C", "c", ProgrammingLanguage.C);
         addLanguage("C++", "cpp", ProgrammingLanguage.CPP);
         addLanguage("C#", "csharp", ProgrammingLanguage.CSHARP);
         addLanguage("CSS", "css", ProgrammingLanguage.CSS);
         addLanguage("Diff", "diff", ProgrammingLanguage.DIFF);
         addLanguage("Groovy", "groovy", ProgrammingLanguage.GROOVY);
         addLanguage("HTML 4", "html4strict", ProgrammingLanguage.HTML);
         addLanguage("INI", "ini", ProgrammingLanguage.PROPERTIES_FILE);
         addLanguage("Java", "java", ProgrammingLanguage.JAVA);
         addLanguage("JavaScript", "javascript", ProgrammingLanguage.JAVASCRIPT);
         addLanguage("MySql", "mysql", ProgrammingLanguage.MARIA_DB);
         addLanguage("Perl", "perl", ProgrammingLanguage.PERL);
         addLanguage("PHP", "php-brief", ProgrammingLanguage.PHP);
         addLanguage("PHP + HTML", "php", ProgrammingLanguage.PHP_HTML);
         addLanguage("Python", "python", ProgrammingLanguage.PYTHON);
         addLanguage("Ruby", "ruby", ProgrammingLanguage.RUBY);
         addLanguage("Shell", "bash", ProgrammingLanguage.SHELL);
         addLanguage("SQL", "sql", ProgrammingLanguage.SQL);
         addLanguage("XML", "xml", ProgrammingLanguage.XML);
     }
 }
