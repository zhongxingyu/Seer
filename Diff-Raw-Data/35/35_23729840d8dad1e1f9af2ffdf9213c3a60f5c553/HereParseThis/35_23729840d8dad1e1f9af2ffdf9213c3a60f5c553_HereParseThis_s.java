 /*
  * HereParseThis.java
  * Copyright (C) 2011 Ty Overby
  * All rights reserved.
  */
 
 package com.subitarius.instance.server.parse;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.subitarius.domain.Article;
 import com.subitarius.domain.ArticleUrl;
 import com.subitarius.util.http.HttpModule;
 import com.subitarius.util.logging.TestLoggingModule;
 
 /**
  * @author ty
  * 
  */
 public class HereParseThis {
 	
 	/**
 	 * @param args
 	 * @throws ArticleParseException
 	 */
 	public static void main(String[] args) throws ArticleParseException {
		String earl = "http://www.economist.com/node/21526415";
 		
 		printArticle(parseThisMotherFucker(earl));
 	}
 	
 	private static Article parseThisMotherFucker(String earl) throws ArticleParseException {
 		Injector inj = Guice.createInjector(new HttpModule(), new TestLoggingModule(), new AbstractModule() {
 			@Override
 			protected void configure() {
 				bind(ArticleParser.class).to(MasterArticleParser.class);
 			}
 		});
 		ArticleParser ap = inj.getInstance(ArticleParser.class);
 		return ap.parse(new ArticleUrl(earl));
 	}
 	
 	private static void printArticle(Article article) {
 		System.out.println("TITLE:    " + article.getTitle());
 		System.out.println("EARL: " + article.getUrl());
 		System.out.println("AUTHOR: " + article.getByline());
 		System.out.println("DATE: " + article.getDate());
 		System.out.println("PARAGRAPHS:");
 		for (String s : article.getParagraphs()) {
			System.out.println("\t" + s + "\n");
 		}
 	}
 }
