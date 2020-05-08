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
 import javax.persistence.EntityTransaction;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 import org.slf4j.Logger;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.SetMultimap;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.prealpha.dispatch.server.ActionHandler;
 import com.prealpha.dispatch.shared.ActionException;
 import com.prealpha.dispatch.shared.Dispatcher;
 import com.subitarius.action.MutationResult;
 import com.subitarius.action.ParseArticles;
 import com.subitarius.domain.Article;
 import com.subitarius.domain.ArticleUrl;
 import com.subitarius.domain.DeletedEntity;
 import com.subitarius.domain.DistributedEntity;
 import com.subitarius.domain.Source;
 import com.subitarius.domain.Team;
 import com.subitarius.instance.server.parse.ArticleParseException;
 import com.subitarius.instance.server.parse.ArticleParser;
 import com.subitarius.util.http.StatusCodeException;
 import com.subitarius.util.logging.InjectLogger;
 
 final class ParseArticlesHandler implements
 		ActionHandler<ParseArticles, MutationResult> {
 	@InjectLogger
 	private Logger log;
 
 	private final Provider<EntityManager> entityManagerProvider;
 
 	private final ArticleParser articleParser;
 
 	private final Provider<Team> teamProvider;
 
 	private CountDownLatch latch;
 
 	@Inject
 	private ParseArticlesHandler(Provider<EntityManager> entityManagerProvider,
 			ArticleParser articleParser, Provider<Team> teamProvider) {
 		this.entityManagerProvider = entityManagerProvider;
 		this.articleParser = articleParser;
 		this.teamProvider = teamProvider;
 	}
 
 	@Override
 	public MutationResult execute(ParseArticles action, Dispatcher dispatcher)
 			throws ActionException {
 		EntityManager entityManager = entityManagerProvider.get();
 		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
 		CriteriaQuery<ArticleUrl> criteria = builder
 				.createQuery(ArticleUrl.class);
 		Root<ArticleUrl> urlRoot = criteria.from(ArticleUrl.class);
 		criteria.select(urlRoot);
		criteria.where(builder.isEmpty(urlRoot
				.<Set<DistributedEntity>> get("children")));
 		criteria.distinct(true);
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
 			EntityManager entityManager = entityManagerProvider.get();
 			EntityTransaction transaction = entityManager.getTransaction();
 			transaction.begin();
 			try {
 				log.trace("attempting to parse {}", url);
 				Article article = articleParser.parse(url);
 				if (article != null) {
 					entityManager.persist(article);
 					log.trace("persisted article: {}", article);
 				} else {
 					log.trace("no valid article at URL {}", url);
 				}
 				transaction.commit();
 			} catch (ArticleParseException apx) {
 				if (apx.getCause() instanceof StatusCodeException) {
 					int statusCode = ((StatusCodeException) apx.getCause())
 							.getStatusCode();
 					log.debug("parse failed due to status code {}: {}",
 							statusCode, url);
 					if (statusCode == 404) {
 						log.trace("marking URL as deleted: {}", url);
 						DeletedEntity deleted = new DeletedEntity(
 								teamProvider.get(), url);
 						entityManager.persist(deleted);
 					}
 				} else {
 					log.warn("exception while parsing article at URL {}", url,
 							apx);
 				}
 			} catch (RuntimeException rx) {
 				log.warn(
 						"unexpected runtime exception while parsing article at URL {}",
 						url, rx);
 			} finally {
 				if (transaction.isActive()) {
 					transaction.rollback();
 				}
 			}
 		}
 	}
 }
