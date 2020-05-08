 package com.cnnic.whois.dao.db;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.cnnic.whois.bean.PageBean;
 import com.cnnic.whois.bean.QueryParam;
 import com.cnnic.whois.bean.QueryType;
 import com.cnnic.whois.dao.QueryExecutor;
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.execption.RedirectExecption;
 
 public class DbQueryExecutor implements QueryExecutor {
 	private static DbQueryExecutor executor = new DbQueryExecutor();
 
 	public static DbQueryExecutor getExecutor() {
 		return executor;
 	}
 
 	private List<AbstractDbQueryDao> dbQueryDaos = new ArrayList<AbstractDbQueryDao>();
 
 	private void init() {
 		dbQueryDaos.add(new AsQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new DelegationKeysQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new DnrDomainQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new DnrEntityQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new AbstractDomainQueryDao(dbQueryDaos) {
 		});// for getAll
 		dbQueryDaos.add(new DsDataQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new EntityQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new ErrorMsgQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new EventsQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new SearchDomainQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new SearchEntityQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new HelpQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new IpQueryDao(dbQueryDaos));// TODO:
 		dbQueryDaos.add(new IpRedirectionQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new KeyDataQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new LinksQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new NoticesQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new NsQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new PhonesQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new PostalAddressQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new PublicIdsQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new RefirectionQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new RegistrarQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new RemarksQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new RirDomainQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new RirEntityQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new SecureDnsQueryDao(dbQueryDaos));
 		dbQueryDaos.add(new VariantsQueryDao(dbQueryDaos));
 	}
 
 	public DbQueryExecutor() {
 		super();
 		init();
 	}
 
 	@Override
 	public Map<String, Object> query(QueryType queryType, QueryParam param,
 			PageBean... pageParam) throws QueryException, RedirectExecption {
 		for (AbstractDbQueryDao queryDao : dbQueryDaos) {
 			if (queryDao.supportType(queryType)) {
 				return queryDao.query(param, pageParam);
 			}
 		}
 		return null;
 	}
 
 	public Map<String, Object> getAll(QueryType queryType, String role)
 			throws QueryException {
 		for (AbstractDbQueryDao queryDao : dbQueryDaos) {
 			if (queryDao.supportType(queryType)) {
 				return queryDao.getAll(role);
 			}
 		}
 		return null;
 	}
 
 	public List<AbstractDbQueryDao> getDbQueryDaos() {
 		return dbQueryDaos;
 	}
 
 	public void setDbQueryDaos(List<AbstractDbQueryDao> dbQueryDaos) {
 		this.dbQueryDaos = dbQueryDaos;
 	}
 
 	public List<String> getKeyFields(QueryType queryType,String role) {
 		for (AbstractDbQueryDao queryDao : dbQueryDaos) {
 			if (queryDao.supportType(queryType)) {
 				return queryDao.getKeyFields(role);
 			}
 		}
 		return new ArrayList<String>();
 	}
 }
