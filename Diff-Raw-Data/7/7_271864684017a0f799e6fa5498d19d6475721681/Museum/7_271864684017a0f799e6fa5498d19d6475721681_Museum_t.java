 package com.ngdb.entities;
 
 import java.math.BigInteger;
 import java.util.List;
 
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Session;
 
 import com.ngdb.entities.article.Article;
 
 @SuppressWarnings("unchecked")
 public class Museum {
 
 	@Inject
 	private Session session;
 
 	public int getRankOf(Article article) {
 		List<Object[]> list = session.createSQLQuery("SELECT article_id,COUNT(*) FROM CollectionObject GROUP BY article_id ORDER BY COUNT(*) DESC").list();
 		int rank = 1;
 		for (Object[] o : list) {
 			BigInteger articleId = (BigInteger) o[0];
 			if (article.getId().equals(articleId.longValue())) {
 				return rank;
 			}
			rank++;
 		}
 		return Integer.MAX_VALUE;
 	}
 
 }
