 package com.cnnic.whois.dao.query.cache;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.stereotype.Repository;
 
 import com.cnnic.whois.bean.DomainQueryParam;
 import com.cnnic.whois.bean.QueryParam;
 import com.cnnic.whois.bean.QueryType;
 import com.cnnic.whois.dao.query.db.AbstractDomainQueryDao;
 @Repository("cacheDnrDomainQueryDao")
 public class DnrDomainQueryDao extends AbstractCacheQueryDao {
 	@Override
 	protected List<String> getCacheKeySplits(QueryParam param) {
		String q = param.getQ();
		if(param instanceof DomainQueryParam){
			DomainQueryParam domainParam = (DomainQueryParam)param;
			q = domainParam.getDomainPuny();
		}
 		List<String> keySplits = new ArrayList<String>();
 		keySplits.add(QueryType.DNRDOMAIN.toString());
 		keySplits.add("ldhName");
		keySplits.add(q);
 		return keySplits;
 	}
 
 	@Override
 	public QueryType getQueryType() {
 		return QueryType.DNRDOMAIN;
 	}
 
 	@Override
 	public boolean supportType(QueryType queryType) {
 		return QueryType.DNRDOMAIN.equals(queryType);
 	}
 
 	@Override
 	protected boolean needInitCache() {
 		return true;
 	}
 
 	@Override
 	protected void initCache() {
 		super.initCacheWithOneKey(AbstractDomainQueryDao.QUERY_KEY, "Ldh_Name");
 	}
 }
