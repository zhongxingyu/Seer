 package com.zarcode.data.dao;
 
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import com.zarcode.app.AppCommon;
 import com.zarcode.data.model.ReportDO;
 import com.zarcode.data.model.ReportRegionDO;
 import com.zarcode.data.model.UserDO;
 import com.zarcode.platform.dao.BaseDao;
 
 public class RegionDao extends BaseDao {
 	
 	private Logger logger = Logger.getLogger(RegionDao.class.getName());
 
 	
 	public List<ReportRegionDO> getAllRegions() {
 		List<ReportRegionDO> res = null;
 		StringBuilder sb = new StringBuilder();
 		Query query = pm.newQuery(ReportRegionDO.class);
 		res = (List<ReportRegionDO>)query.execute();
 		return res;
 	}
 	
 	public ReportRegionDO addRegion(ReportRegionDO region) {
 		ReportRegionDO newRegion = null;
 		Date now = new Date();
 		region.setLastUpdated(now);
 		region.setRegionId(null);
 		newRegion = pm.makePersistent(region); 
		logger.info("addRegion(): Added ReportRegion --> " + newRegion);
 		return newRegion;
 	}
 	
 	public ReportRegionDO getRegionByState(String state) {
 		int i = 0;
 		ReportRegionDO region = null;
 		List<ReportRegionDO> res = null;
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("state == ");
 		sb.append("'");
 		sb.append(state);
 		sb.append("'");
 		Query query = pm.newQuery(ReportRegionDO.class);
 		query.setFilter("state == stateParam");
 		query.setOrdering("keyword asc");
 		query.declareParameters("String stateParam");
 		res = (List<ReportRegionDO>)query.execute(state);
 		if (res.size() > 0) {
 			region = res.get(0);
 		}
 		return region;
 		
 	} // getRegionByState
 	
 	public void updateRegionByState(String state, Date reportDate) {
 		ReportRegionDO res = null;
 		Date now = new Date();
 		logger.info("updateRegionByState(): Updating state --> " + state + " " + reportDate);
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			res = getRegionByState(state);
 			if (res == null) {
 				res = new ReportRegionDO();
 				res.setState(state);
 				res.setLastReportDate(reportDate);
 				res = addRegion(res);
 			}
 			else {
 				res.setLastUpdated(now);
 				res.setLastReportDate(reportDate);
 			}
 			tx.commit();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 	}
 	
 	
 }
