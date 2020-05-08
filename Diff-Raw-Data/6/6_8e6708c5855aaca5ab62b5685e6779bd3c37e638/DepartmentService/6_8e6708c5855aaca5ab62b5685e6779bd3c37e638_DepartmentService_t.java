 package com.appCore.personnel.Core.Service;
 
 import java.util.List;
 import javax.annotation.Resource;
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.apache.log4j.Logger;
 
import com.appCore.personnel.Core.Entity.Branch;
 import com.appCore.personnel.Core.Entity.BranchInfo;
 import com.appCore.personnel.Core.Entity.Department;
 import com.appCore.personnel.Core.Entity.DepartmentInfo;
 import com.appCore.personnel.Core.Entity.DepartmentSummary;
 import com.appCore.personnel.Core.Entity.Section;
 
 @Service("departmentService")
 @Transactional
 public class DepartmentService
 { 
 
 	@Resource(name="sessionFactory")
 	private SessionFactory sessionFactory;
 	
 	protected static Logger logger = Logger.getLogger("service");
 	
 	public DepartmentSummary getSummary(Integer companyId)
 	{
 		int summaryResultCount = 3;
 		Session session = sessionFactory.getCurrentSession();
 	
 		Query queryCount = session.createQuery("select count(*) from Department where companyId = :companyId");
 		queryCount.setParameter("companyId", companyId);
 		Long summaryCount = ((Long) queryCount.uniqueResult()).longValue();
 
 		Query query = session.createQuery("FROM Department where companyId = :companyId");
 		query.setParameter("companyId", companyId);
 
 		List<Department> list = query.setMaxResults(summaryResultCount).list();
 
 		DepartmentSummary summary = new DepartmentSummary();
 		summary.setCount(summaryCount);
 		summary.setListDepartment(list);
 		
 		return summary;
 	}
 	
 	
 	public List<Department> getAll() 
 	{	
 		Session session = sessionFactory.getCurrentSession();
 		
 		Query query = session.createQuery("FROM  Department");
 		List<Department> deptList = query.list();
 		return  query.list();
 	}
 
 	
 	public List<Department> getAllByCompany(Integer id) {
 		
 		Session session = sessionFactory.getCurrentSession();
 		Query query = session.createQuery("FROM  Department WHERE CompanyId = :id");
 		query.setParameter("id", id);
 		return  query.list();
 	}
 	
 	public Department get(Integer id) 
 	{
 		Session session = sessionFactory.getCurrentSession();
 		Department department = (Department) session.get(Department.class, id);
 
 		Query query = session.createQuery("FROM DepartmentInfo WHERE RefEntity = :id");
 		query.setParameter("id", id);
 		List<DepartmentInfo> info = query.list();
 		
 		department.setDepartmentInfo(info);
 		
 		return department;
 	}
 	
 	public List<Department> getByDivisionId(Integer id) 
 	{
 		Session session = sessionFactory.getCurrentSession();
 		Query query = session.createQuery("From Department where DivisionId = :divisionId");
 		query.setParameter("divisionId", id);
 		return query.list();
 	}
 	
 	public void add(Department department) 
 	{
 		Session session = sessionFactory.getCurrentSession();
 		Integer savedInstance = (Integer) session.save(department);
 		
 		for	(DepartmentInfo info : department.getDepartmentInfo())
 		{
 			info.setRefEntity(savedInstance);
 			session.save(info);
 		}
 		
 		/*for (Section section : department.getSection())
 		{
 			logger.debug("Traversing section code : " + section.getSectionCode());
 			section.setDepartmentId(savedInteger);
 			session.save(section);
 		}*/
 	}
 
 
 	public void saveOrUpdate(Department department) 
 	{
 		Session session = sessionFactory.getCurrentSession();
 		session.saveOrUpdate(department);
 	}
 	
 	public void save(Department department) 
 	{
 		Session session = sessionFactory.getCurrentSession();
 		session.save(department);
 	}
 
 
 	public void delete(Integer id) 
 	{
 		Session session = sessionFactory.getCurrentSession();
 		Department department = (Department) session.get(Department.class, id);
 		session.delete(department);
 	}
 
 	public void edit(Department department) 
 	{
 		Session session = sessionFactory.getCurrentSession();
 		Department target = (Department) session.get(Department.class, department.getNid());
 
 		target.setNid(department.getNid());
 		target.setDepartmentCode(department.getDepartmentCode());
 		target.setDepartmentInfo(department.getDepartmentInfo());
 		target.setDepartmentName(department.getDepartmentName());
 		target.setRemark(department.getRemark());
 		target.setDisabled(department.getDisabled());
 		
 		target.setLastUpdate(department.getLastUpdate());
 
 		session.save(target);
 	}
 
 
 	
 }
