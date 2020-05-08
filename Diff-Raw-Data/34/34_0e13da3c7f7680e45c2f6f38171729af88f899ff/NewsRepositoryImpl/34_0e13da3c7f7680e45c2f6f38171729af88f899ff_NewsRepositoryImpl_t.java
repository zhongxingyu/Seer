 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.actor.domain.misc;
 import static zeroth.actor.domain.misc.Notice_.*;
 import javax.enterprise.inject.Default;
 import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
 import zeroth.actor.domain.misc.PersistenceServiceAnnotation.NewsPersistenceService;
 import zeroth.framework.enterprise.domain.AbstractQueryRepositoryImpl;
 import zeroth.framework.enterprise.infra.persistence.QueryPersistenceService;
 /**
 * お知らせリポジトリ
  * @author nilcy
  */
 @Default
public  class NewsRepositoryImpl extends AbstractQueryRepositoryImpl<News, Long, NoticeFilter>
    implements NewsRepository {
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
     * 検索条件は、表題、説明、カテゴリとする。
      * </p>
      */
     @Override
    protected Predicate expression(final NoticeFilter filter) {
        final CriteriaBuilder b = service.builder();
        final Root<News> r = service.root();
        return b.and(b.equal(r.get(title), filter.getTitle()),
            b.equal(r.get(description), filter.getDescription()),
            b.equal(r.get(category), filter.getCategory()));
     }
 }
