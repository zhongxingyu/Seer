 /*
  * ParseArticlesHandler.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.subitarius.instance.server.action;
 
 import static com.google.common.base.Preconditions.*;
 
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 
 import org.slf4j.Logger;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.SetMultimap;
 import com.google.inject.Inject;
 import com.prealpha.dispatch.server.ActionHandler;
 import com.prealpha.dispatch.shared.ActionException;
 import com.prealpha.dispatch.shared.Dispatcher;
 import com.subitarius.action.MutationResult;
 import com.subitarius.action.ParseArticles;
 import com.subitarius.domain.Article;
 import com.subitarius.domain.ArticleUrl;
 import com.subitarius.domain.Source;
 import com.subitarius.instance.server.parse.ArticleParseException;
 import com.subitarius.instance.server.parse.ArticleParser;
 import com.subitarius.util.http.StatusCodeException;
 import com.subitarius.util.logging.InjectLogger;
 
 final class ParseArticlesHandler implements
 		ActionHandler<ParseArticles, MutationResult> {
 	@InjectLogger
 	private Logger log;
 
 	private final EntityManager entityManager;
 
 	private final ArticleParser articleParser;
 
 	private CountDownLatch latch;
 
 	@Inject
 	private ParseArticlesHandler(EntityManager entityManager,
 			ArticleParser articleParser) {
 		this.entityManager = entityManager;
 		this.articleParser = articleParser;
 	}
 
 	@Override
 	public MutationResult execute(ParseArticles action, Dispatcher dispatcher)
 			throws ActionException {
 		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
 		CriteriaQuery<ArticleUrl> criteria = builder
 				.createQuery(ArticleUrl.class);
 		criteria.select(criteria.from(ArticleUrl.class));
 		List<ArticleUrl> allUrls = entityManager.createQuery(criteria)
 				.getResultList();
 
 		SetMultimap<Source, ArticleUrl> urlMap = HashMultimap.create();
 		for (ArticleUrl url : allUrls) {
 			Source source = Source.fromUrl(url.getUrl());
 			urlMap.put(source, url);
 		}
 
 		latch = new CountDownLatch(Source.values().length);
 		for (Source source : Source.values()) {
 			String name = String.format("parse/%s", source.toString());
 			Set<ArticleUrl> urls = urlMap.get(source);
 			Runnable task = new SourceTask(urls);
 			Thread thread = new Thread(task, name);
 			thread.start();
 			log.debug("queued {} articles from {}", urls.size(), source);
 		}
 
 		try {
 			latch.await();
 		} catch (InterruptedException ix) {
 			log.warn("interrupt on request thread", ix);
 		}
 		return MutationResult.SUCCESS;
 	}
 
 	private final class SourceTask implements Runnable {
 		private final Set<ArticleUrl> urls;
 
 		public SourceTask(Set<ArticleUrl> urls) {
 			checkNotNull(urls);
 			this.urls = urls;
 		}
 
 		@Override
 		public void run() {
 			try {
 				for (ArticleUrl url : urls) {
 					parseArticle(url);
 					Thread.sleep(1000);
 				}
 			} catch (InterruptedException ix) {
 				log.warn("interrupt on source thread", ix);
 			}
 			latch.countDown();
 		}
 
 		private void parseArticle(ArticleUrl url) {
 			entityManager.getTransaction().begin();
 			try {
 				log.trace("attempting to parse {}", url);
 				Article article = articleParser.parse(url);
 				if (article != null) {
 					entityManager.persist(article);
 					log.trace("persisted article: {}", article);
 				} else {
 					log.trace("no valid article at URL {}", url);
 				}
 				entityManager.getTransaction().commit();
 			} catch (ArticleParseException apx) {
 				if (apx.getCause() instanceof StatusCodeException) {
 					int statusCode = ((StatusCodeException) apx.getCause())
 							.getStatusCode();
 					log.debug("parse failed due to status code {}: {}",
 							statusCode, url);
 				} else {
					log.warn("exception while parsing article at URL {}", url,
							apx);
 				}
 			} catch (RuntimeException rx) {
				log.warn(
						"unexpected runtime exception while parsing article at URL {}",
						url, rx);
 			} finally {
 				if (entityManager.getTransaction().isActive()) {
 					entityManager.getTransaction().rollback();
 				}
 			}
 		}
 	}
 }
