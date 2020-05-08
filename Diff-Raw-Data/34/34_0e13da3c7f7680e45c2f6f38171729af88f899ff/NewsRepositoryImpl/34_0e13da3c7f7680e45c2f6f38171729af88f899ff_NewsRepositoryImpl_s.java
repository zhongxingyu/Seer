 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.actor.domain.misc;
 import static zeroth.actor.domain.misc.Notice_.*;
 import javax.enterprise.inject.Default;
 import javax.inject.Inject;
 import javax.persistence.criteria.Predicate;
 import zeroth.actor.domain.misc.PersistenceServiceAnnotation.NewsPersistenceService;
 import zeroth.framework.enterprise.domain.AbstractQueryRepositoryImpl;
 import zeroth.framework.enterprise.infra.persistence.QueryPersistenceService;
 /**
 * News repository implementation.
  * @author nilcy
  */
 @Default
public class NewsRepositoryImpl extends AbstractQueryRepositoryImpl<News, Long, News> implements
    NewsRepository {
     /** 製品番号 */
     private static final long serialVersionUID = 8151517797978098457L;
     /** 先進データ永続化サービス */
     @Inject
     @NewsPersistenceService
     private QueryPersistenceService<News, Long> service;
     /** {@inheritDoc} */
     @Override
     protected QueryPersistenceService<News, Long> getPersistenceService() {
         return service;
     }
     /**
      * {@inheritDoc}
      * <p>
     * 検索条件は、表題とする。
      * </p>
      */
     @Override
    protected Predicate expression(final News filter) {
        return getPersistenceService().builder().equal(getPersistenceService().root().get(title),
            filter.getTitle());
     }
 }
