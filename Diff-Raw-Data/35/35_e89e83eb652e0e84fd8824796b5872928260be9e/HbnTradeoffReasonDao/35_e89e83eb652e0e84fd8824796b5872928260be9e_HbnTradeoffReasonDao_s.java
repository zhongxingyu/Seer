 /**
  * 
  */
 package edu.cmu.square.server.dao.implementation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.springframework.stereotype.Repository;
 
 
 import edu.cmu.square.client.model.GwtTradeoffReason;
 import edu.cmu.square.server.dao.interfaces.ProjectPackageRequirementRatingDao;
 import edu.cmu.square.server.dao.interfaces.TradeoffReasonDao;
 import edu.cmu.square.server.dao.model.Project;
 import edu.cmu.square.server.dao.model.ProjectPackageTradeoffreason;
 import edu.cmu.square.server.dao.model.QualityAttribute;
 
 /**
  * @author Nan
  *
  */
 @Repository
 @SuppressWarnings("unchecked")
 public class HbnTradeoffReasonDao extends HbnAbstractDao<ProjectPackageTradeoffreason, Integer> implements TradeoffReasonDao 
 {
 
 	@Override
 	public List<GwtTradeoffReason> getAllTradeoffReasons(Project project)
 	{
 		if(project == null) 
 			return null;
 		
 		List<ProjectPackageTradeoffreason> lines = null;
 		
 		String query = "select s from ProjectPackageTradeoffreason s where s.project.id=:project";
 		
 		Query q = getSession().createQuery(query);
 		q.setParameter("project", project.getId());
 		lines = (List<ProjectPackageTradeoffreason>)q.list();
 		
 		List<GwtTradeoffReason> result = new ArrayList<GwtTradeoffReason>();
 		GwtTradeoffReason current = null;
 		for(int i=0 ;i<lines.size(); ++i)
 		{
 			current = new GwtTradeoffReason();
 			current.setProjectId(lines.get(i).getId().getProjectId());
 			current.setPackageId(lines.get(i).getId().getPackageId());
 			current.setTradeoffreason(lines.get(i).getTradeoffreason());
 			current.setPriority(lines.get(i).getPriority());
 			result.add(current);
 		}
 		
 		return result;
 	}
 
 	@Override
 	public void setTradeoffReason(int projectID, int packageId, String tradeoffreason)
 	{
 		System.out.println("here...."+projectID+"   "+packageId+"  "+tradeoffreason);
 		
 		String query = "Update ProjectPackageTradeoffreason r set r.tradeoffreason=:tradeoffreason where r.project.id=:projectId and r.softwarePackage.id=:packageId";
 		Query q = getSession().createQuery(query);
 		q.setParameter("projectId", projectID);
 		q.setParameter("packageId", packageId);
 		q.setParameter("tradeoffreason", tradeoffreason);
 		q.executeUpdate();
 	}
 
 	@Override
 	public String getTradeoffReason(int projectID, int packageId)
 	{
 		String tradeoffreason = "";
 		String query = "Select s from ProjectPackageTradeoffreason s where s.project.id=:projectId and s.softwarePackage.id=:packageId)";
 		
 		Query q = getSession().createQuery(query);
 		
 		q.setParameter("projectId", projectID);
 		q.setParameter("packageId", packageId);
 		
 		List<ProjectPackageTradeoffreason> list = q.list(); 
 		if(list.size() == 1)
 			tradeoffreason = list.get(0).getTradeoffreason();
 			
 		return tradeoffreason;
 	}
 
 	@Override
 	public void updateTradeoffReason(int projectID, int packageId, String tradeoffreason)
 	{
 		System.out.println("update here...."+projectID+"   "+packageId+"  "+tradeoffreason);
 			
 		String query = "Update ProjectPackageTradeoffreason r set r.tradeoffreason=:tradeoffreason where r.project.id=:projectId and r.softwarePackage.id=:packageId";
 		Query q = getSession().createQuery(query);
 		q.setParameter("projectId", projectID);
 		q.setParameter("packageId", packageId);
 		q.setParameter("tradeoffreason", tradeoffreason);
 		q.executeUpdate();
 	}
 
 	@Override
 	public void setPriority(int projectID, int packageId, int priority)
 	{
 		//System.out.println("here...."+projectID+"   "+packageId+"  "+tradeoffreason);
 		
 
		System.out.println("here priority...."+projectID+"   "+packageId+"  "+priority);		
 		
 		String query = "Update ProjectPackageTradeoffreason r set r.priority=:priority where r.project.id=:projectId and r.softwarePackage.id=:packageId";
 		Query q = getSession().createQuery(query);
 		q.setParameter("projectId", projectID);
 		q.setParameter("packageId", packageId);
 		q.setParameter("priority", priority);
 		q.executeUpdate();
 		
 		System.out.println("completed");
 	}
 
 	@Override
 	public Integer getPriority(int projectID, int packageId)
 	{
 		int priority = -1;
 		String query = "Select s from ProjectPackageTradeoffreason s where s.project.id=:projectId and s.softwarePackage.id=:packageId)";		
 		Query q = getSession().createQuery(query);
 		
 		q.setParameter("projectId", projectID);
 		q.setParameter("packageId", packageId);
 		
 		List<ProjectPackageTradeoffreason> list = q.list(); 
 		if(list.size() == 1)
 			priority = list.get(0).getPriority();
 			
 		return priority;
 	}
 
 }
