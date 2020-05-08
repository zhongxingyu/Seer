 package com.tsuyu.dao;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import com.tsuyu.model.*;
 
 import com.tsuyu.persistance.HibernateUtil;
 import com.tsuyu.util.ExtJSFilter;
 import com.tsuyu.util.JsonUtil;
 import com.tsuyu.util.Util;
 
 public class MenuDAO  {
 
 	private Session session;
 	private Util util;
 	
 	public MenuDAO(){
 		this.util = new Util();
 	}
 
 	public ArrayList<Accordian> accordianList(int signinId) {
 		
 		session = HibernateUtil.getSessionFactory().openSession();
 		
 		ArrayList<Accordian> accordianAll = new ArrayList<Accordian>();
 		
 		String hql = "select b.accordianId as accordianId, b.accordianSequence as accordianSequence, b.accordianName as accordianName," +
 				" b.accordianDescription as accordianDescription, b.accordianIcon as accordianIcon from AccessLevel a join a.accordians b "
          	+"where a.accessLevelId = (select c.accessLevelId from AccessLevel c " +
          			"join c.signIns d where d.signinId = :signinId) order by b.accordianSequence";      
     	 List data = session.createQuery(hql).setParameter("signinId", signinId).setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
   		.list();
      	 for (Object object : data){
   		  Map row = (Map)object;
   		  Accordian accordianMap = new Accordian();
   		  accordianMap.setAccordianId(Integer.parseInt(row.get("accordianId").toString()));
   		  accordianMap.setAccordianSequence(Integer.parseInt(row.get("accordianSequence").toString()));
   		  accordianMap.setAccordianName(row.get("accordianName").toString());
   		  accordianMap.setAccordianDescription(row.get("accordianDescription").toString());
   		  accordianMap.setAccordianIcon(row.get("accordianIcon").toString());
   		  accordianAll.add(accordianMap);
   		 }
      	 
 		session.close();
 		return accordianAll;
 	}
 
 	public ArrayList<Children> childrenList(int accordianId) {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 		Query query = session.createQuery(
 				"from Accordian a join a.childrens i where "
 						+ "a.accordianId = :accordianId "
 						+ "order by i.childrenSequence").setParameter(
 				"accordianId", accordianId);
 		query.setCacheable(true);
 
 		Iterator<?> ite = query.list().iterator();
 		ArrayList<Children> childAll = new ArrayList<Children>();
 		Object[] pair = null;
 		while (ite.hasNext()) {
 			pair = (Object[]) ite.next();
 			childAll.add((Children) pair[1]);
 		}
 		session.close();
 		return childAll;
 	}
 
 	public ArrayList<Leaf> leafList(int childrenId) {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 		Query query = session.createQuery(
 				"from Children a join a.leafs i where "
 						+ "a.childrenId = :childrenId order by i.leafSequence")
 				.setParameter("childrenId", childrenId);
 		query.setCacheable(true);
 		Iterator<?> ite = query.list().iterator();
 		ArrayList<Leaf> leafAll = new ArrayList<Leaf>();
 		Object[] pair = null;
 		while (ite.hasNext()) {
 			pair = (Object[]) ite.next();
 			leafAll.add((Leaf) pair[1]);
 		}
 		
 		session.close();
 		return leafAll;
 	}
 
 	public ArrayList<Leaf> showLeafGrid(HttpServletRequest request)
 			throws HibernateException, SecurityException, NoSuchFieldException,
 			ParseException {
 		session = HibernateUtil.getSessionFactory().openSession();
 
 		Object filter = null;
 		List<ExtJSFilter> filters = null;
 		filter = request.getParameter("filter");
 		ArrayList<Leaf> leafAll = new ArrayList<Leaf>();
 		if (filter != null) { // filter is optional
 			
 			filters = JsonUtil.getExtJSFiltersFromRequest(filter);
 			// List catalog =
 			// session.getNamedQuery("filterNativeSQL").setParameter("filtered",
 			// Util.filterBuilder(filters)).list();
 			List data = session.createSQLQuery("select leafId,leafSequence, leafName, COALESCE(leafDescription,'') as leafDescription," +
 					" leafMapper, COALESCE(leafIcon,'') as leafIcon ,childrenName " +
 					" from leaf join children using(childrenId) WHERE 1 " + Util.filterBuilder(filters))
 			.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
 			
 			for (Object object : data)
 			{
 				Map row = (Map)object;
 				Leaf leafMap = new Leaf();
 			    leafMap.setLeafId(Integer.parseInt(row.get("leafId").toString()));
 			    leafMap.setLeafSequence(Integer.parseInt(row.get("leafSequence").toString()));
 				leafMap.setLeafName(row.get("leafName").toString());
 				leafMap.setLeafDescription(row.get("leafDescription").toString());
 				leafMap.setLeafMapper(row.get("leafMapper").toString());
 				leafMap.setLeafIcon(row.get("leafIcon").toString());
 				leafMap.setChildrenName(row.get("childrenName").toString());
 				leafAll.add(leafMap);
 			}
 			
 			session.close();
 			return leafAll;
 
 		} else {
 			return showLeafList();
 		}
 	}
 	
 	public ArrayList<Children> showChildrenGrid(HttpServletRequest request)
 			throws HibernateException, SecurityException, NoSuchFieldException,
 			ParseException {
 		session = HibernateUtil.getSessionFactory().openSession();
 
 		Object filter = null;
 		List<ExtJSFilter> filters = null;
 		filter = request.getParameter("filter");
 		ArrayList<Children> childrenAll = new ArrayList<Children>();
 		if (filter != null) { // filter is optional
 
 			filters = JsonUtil.getExtJSFiltersFromRequest(filter);
 			List data = session
 					.createSQLQuery(
							"select childrenId,childrenSequence, childrenName, COALESCE(childrenDescription,'') as leafDescription,"
 									+ "COALESCE(childrenIcon,'') as childrenIcon, childrenMapper, accordianName "
 									+ " from children join accordian using(accordianId) WHERE 1 "
 									+ Util.filterBuilder(filters))
 					.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
 
 			for (Object object : data) {
 				Map row = (Map) object;
 				Children childrenMap = new Children();
 				childrenMap.setChildrenId(Integer.parseInt(row
 						.get("childrenId").toString()));
 				childrenMap.setChildrenSequence(Integer.parseInt(row.get(
 						"childrenSequence").toString()));
 				childrenMap.setChildrenName(row.get("childrenName").toString());
 				childrenMap.setChildrenDescription(row.get(
 						"childrenDescription").toString());
 				childrenMap.setChildrenIcon(row.get("childrenIcon").toString());
 				childrenMap.setChildrenMapper(row.get("childrenMapper").toString());
 				childrenMap.setAccordianName(row.get("accordianName")
 						.toString());
 				childrenAll.add(childrenMap);
 			}
 
 			session.close();
 			return childrenAll;
 
 		} else {
 			return showChildrenList();
			//showChildrenAll();
 		}
 	}
 	
 	public ArrayList<Accordian> showAccordianGrid(HttpServletRequest request)
 			throws HibernateException, SecurityException, NoSuchFieldException,
 			ParseException {
 		session = HibernateUtil.getSessionFactory().openSession();
 
 		Object filter = null;
 		List<ExtJSFilter> filters = null;
 		filter = request.getParameter("filter");
 		ArrayList<Accordian> accordianAll = new ArrayList<Accordian>();
 		if (filter != null) { // filter is optional
 
 			filters = JsonUtil.getExtJSFiltersFromRequest(filter);
 			// List catalog =
 			// session.getNamedQuery("filterNativeSQL").setParameter("filtered",
 			// Util.filterBuilder(filters)).list();
 			List data = session
 					.createSQLQuery(
 							"select accordianId,accordianSequence, accordianName, COALESCE(accordianDescription,'') as accordianDescription,"
 									+ "COALESCE(accordianIcon,'') as accordianIcon from accordian WHERE 1 "
 									+ Util.filterBuilder(filters))
 					.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
 
 			for (Object object : data) {
 				Map row = (Map) object;
 				Accordian accordianMap = new Accordian();
 				accordianMap.setAccordianId(Integer.parseInt(row.get("accordianId").toString()));
 				accordianMap.setAccordianSequence(Integer.parseInt(row.get("accordianSequence").toString()));
 				accordianMap.setAccordianName(row.get("accordianName").toString());
 				accordianMap.setAccordianDescription(row.get("accordianDescription").toString());
 				accordianMap.setAccordianIcon(row.get("accordianIcon").toString());
 				accordianAll.add(accordianMap);
 			}
 
 			session.close();
 			return accordianAll;
 
 		} else {
 			return showAccordianList();
 		}
 	}
 	
 	public ArrayList<Icon> showIcon() {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 
 		Query query = session.createQuery("from Icon");
 		List<Icon> icon = query.list();
 		query.setCacheable(true);
 		query.setCacheRegion("query.Icon");
 		session.close();
 
 		return (ArrayList<Icon>) icon;
 	}
 
 	public ArrayList<Accordian> showAccordianList() {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 
 		ArrayList<Accordian> accordianAll = new ArrayList<Accordian>();
 		
 		String hql = "select b.accordianId as accordianId, b.accordianSequence as accordianSequence, b.accordianName as accordianName," +
 				" b.accordianDescription as accordianDescription, b.accordianIcon as accordianIcon, a.accessLevelId as accessLevelId from AccessLevel a join a.accordians b "
          	+"order by b.accordianSequence";      
     	 List data = session.createQuery(hql).setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
   		.list();
      	 for (Object object : data){
   		  Map row = (Map)object;
   		  Accordian accordianMap = new Accordian();
   		  accordianMap.setAccordianId(Integer.parseInt(row.get("accordianId").toString()));
   		  accordianMap.setAccordianSequence(Integer.parseInt(row.get("accordianSequence").toString()));
   		  accordianMap.setAccordianName(row.get("accordianName").toString());
   		  accordianMap.setAccordianDescription(row.get("accordianDescription").toString());
   		  accordianMap.setAccordianIcon(row.get("accordianIcon").toString());
   		  accordianMap.setAccessLevelId(Integer.parseInt(row.get("accessLevelId").toString()));
   		  accordianAll.add(accordianMap);
   		 }
      	 
 		session.close();
 		return accordianAll;
 	}
 	
 	public ArrayList<Children> showChildrenList() {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 
 		ArrayList<Children> childrenAll = new ArrayList<Children>();
 
 		Query query = session
 				.createQuery("from Accordian a join a.childrens c order by c.childrenId");
 
 		query.setCacheable(true);
 		Iterator<?> ite = query.list().iterator();
 
 		Object[] pair = null;
 		Accordian accordian = null;
 		Children childrenMap = null;
 		Children childrenData = null;
 		while (ite.hasNext()) {
 			accordian = new Accordian();
 			childrenMap = new Children();
 			pair = (Object[]) ite.next();
 			accordian = (Accordian) pair[0];
 			childrenData = (Children) pair[1];
 			childrenMap.setChildrenSequence(childrenData.getChildrenSequence());
 			childrenMap.setChildrenName(childrenData.getChildrenName());
 			childrenMap.setChildrenDescription(childrenData
 					.getChildrenDescription());
 			childrenMap.setChildrenIcon(childrenData.getChildrenIcon());
 			childrenMap.setChildrenId(childrenData.getChildrenId());
 			childrenMap.setChildrenMapper(childrenData.getChildrenMapper());
 			childrenMap.setAccordianName(accordian.getAccordianName());
 			childrenAll.add(childrenMap);
 		}
 		session.close();
 		return childrenAll;
 	}
 
 	public ArrayList<Leaf> showLeafList() throws HibernateException,
 	SecurityException, NoSuchFieldException, ParseException {
 		session = HibernateUtil.getSessionFactory().openSession();
 
 		ArrayList<Leaf> leafAll = new ArrayList<Leaf>();
 
 		Query query = session
 				.createQuery("from Children c join c.leafs d order by d.leafId");
 
 		query.setCacheable(true);
 		Iterator<?> ite = query.list().iterator();
 
 		Object[] pair = null;
 		Children children = null;
 		Leaf leafMap = null;
 		Leaf leafData = null;
 		while (ite.hasNext()) {
 			children = new Children();
 			leafMap = new Leaf();
 			pair = (Object[]) ite.next();
 			children = (Children) pair[0];
 			leafData = (Leaf) pair[1];
 			leafMap.setLeafSequence(leafData.getLeafSequence());
 			leafMap.setLeafName(leafData.getLeafName());
 			leafMap.setLeafMapper(leafData.getLeafMapper());
 			leafMap.setLeafDescription(leafData.getLeafDescription());
 			leafMap.setLeafIcon(leafData.getLeafIcon());
 			leafMap.setLeafId(leafData.getLeafId());
 			leafMap.setChildrenName(children.getChildrenName());
 			leafAll.add(leafMap);
 		}
 		session.close();
 		return leafAll;
 }
 	
 	public ArrayList<AccessLevel> showAccessLevelList() {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 
 		ArrayList<AccessLevel> accessLevelAll = new ArrayList<AccessLevel>();
 		Query query = session
 				.createQuery("from AccessLevel a");
 		
 		Iterator iter = query.list().iterator();
 		AccessLevel accessLevelMap = null; 
 		while (iter.hasNext()) {
 			AccessLevel accessLevel = (AccessLevel) iter.next();
 			accessLevelMap =  new AccessLevel();
 			accessLevelMap.setAccessLevelId(accessLevel.getAccessLevelId());
 			accessLevelMap.setAccessLevelName(accessLevel.getAccessLevelName());
 			accessLevelAll.add(accessLevelMap);
 		}
 		
 		session.close();
 		return (ArrayList<AccessLevel>) accessLevelAll;
 	}
 	
 	public ArrayList<Leaf> updateLeaf(HttpServletRequest request) throws HibernateException, SecurityException, NoSuchFieldException, ParseException {
 		
 		session = HibernateUtil.getSessionFactory().openSession();
 		
 		ArrayList<Leaf> leafAll = new ArrayList<Leaf>();
 		
 		List data = session.createSQLQuery("select leafId,leafSequence, leafName, COALESCE(leafDescription,'') as leafDescription," +
 				" leafMapper, COALESCE(leafIcon,'') as leafIcon ,childrenId " +
 				" FROM leaf WHERE 1 AND leafId = :leafId LIMIT 1").setParameter("leafId", Integer.parseInt(request.getParameter("leafId")))
 		.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
 		Leaf leafMap = new Leaf();
 		for (Object object : data)
 		{
 			Map row = (Map)object;
 			leafMap.setLeafId(Integer.parseInt(row.get("leafId").toString()));
 		    leafMap.setLeafSequence(Integer.parseInt(row.get("leafSequence").toString()));
 			leafMap.setLeafName(row.get("leafName").toString());
 			leafMap.setLeafDescription(row.get("leafDescription").toString());
 			leafMap.setLeafMapper(row.get("leafMapper").toString());
 			leafMap.setLeafIcon(row.get("leafIcon").toString());
 			leafMap.setChildrenId(Integer.parseInt(row.get("childrenId").toString()));
 		}
 		
 		leafAll.add(leafMap);
 
 		session.close();
 		
 		return leafAll;
 
 	}
 	
 	public ArrayList<Children> updateChildren(HttpServletRequest request)
 			throws HibernateException, SecurityException, NoSuchFieldException,
 			ParseException {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 
 		ArrayList<Children> childrenAll = new ArrayList<Children>();
 
 		List data = session
 				.createSQLQuery(
 						"select childrenId,childrenSequence,childrenName,COALESCE(childrenDescription,'') as childrenDescription,"
 								+ "COALESCE(childrenIcon,'') as childrenIcon, childrenMapper, accordianId "
 								+ " FROM children WHERE 1 AND childrenId = :childrenId LIMIT 1")
 				.setParameter("childrenId",
 						Integer.parseInt(request.getParameter("childrenId")))
 				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
 		for (Object object : data) {
 			Map row = (Map) object;
 			Children childrenMap = new Children();
 			childrenMap.setChildrenId(Integer.parseInt(row.get("childrenId").toString()));
 			childrenMap.setChildrenSequence(Integer.parseInt(row.get("childrenSequence").toString()));
 			childrenMap.setChildrenName(row.get("childrenName").toString());
 			childrenMap.setChildrenDescription(row.get("childrenDescription").toString());
 			childrenMap.setChildrenIcon(row.get("childrenIcon").toString());
 			childrenMap.setChildrenMapper(row.get("childrenMapper").toString());
 			childrenMap.setAccordianId(Integer.parseInt(row.get("accordianId").toString()));
 			Set<Leaf> leaf= new HashSet<Leaf>(0);
 	        leaf.add(null);
 	        childrenMap.setLeafs(leaf);
 			childrenMap.setAccordian(null);
 			childrenAll.add(childrenMap);
 		}
 
 		session.close();
 
 		return childrenAll;
 
 	}
 	
 	public ArrayList<Accordian> updateAccordian(HttpServletRequest request)
 			throws HibernateException, SecurityException, NoSuchFieldException,
 			ParseException {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 		
 		ArrayList<Accordian> accordianAll = new ArrayList<Accordian>();
 
 		List data = session
 				.createSQLQuery(
 						"select accordianId,accordianSequence,accordianName,COALESCE(accordianDescription,'') as accordianDescription,"
 								+ "COALESCE(accordianIcon,'') as accordianIcon, accessLevelId "
 								+ " FROM accordian WHERE 1 AND accordianId = :accordianId LIMIT 1")
 				.setParameter("accordianId",
 						Integer.parseInt(request.getParameter("accordianId")))
 				.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
 		for (Object object : data) {
 			Map row = (Map) object;
 			Accordian accordianMap = new Accordian();
 			accordianMap.setAccordianId(Integer.parseInt(row.get("accordianId").toString()));
 			accordianMap.setAccordianSequence(Integer.parseInt(row.get("accordianSequence").toString()));
 			accordianMap.setAccordianName(row.get("accordianName").toString());
 			accordianMap.setAccordianDescription(row.get("accordianDescription").toString());
 			accordianMap.setAccordianIcon(row.get("accordianIcon").toString());
 			Set<Children> children= new HashSet<Children>(0);
 			children.add(null);
 	        accordianMap.setChildrens(children);
 	        accordianMap.setAccessLevelId(Integer.parseInt(row.get("accessLevelId").toString()));
 			accordianAll.add(accordianMap);
 		}
 		
 		
 		session.close();
 
 		return accordianAll;
 
 	}
 
 	public ArrayList<Leaf> addLeaf(HttpServletRequest request) throws HibernateException, SecurityException, NoSuchFieldException, ParseException {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 		
 		if (request.getParameter("leafId").toString().length() == 0 ) {
 
 			session.beginTransaction();
 
 			Children child = (Children) session
 					.createQuery(
 							"from Children where " + "childrenId = :childrenId")
 					.setParameter(
 							"childrenId",
 							Integer.parseInt(request.getParameter("childrenId")))
 					.list().get(0);
 
 			Leaf leaf = new Leaf();
 			leaf.setLeafId(null);
 			leaf.setLeafSequence(Integer.parseInt(request.getParameter("leafSequence")));
 			leaf.setLeafName(request.getParameter("leafName"));
 			leaf.setLeafDescription(request.getParameter("leafDescription"));
 			leaf.setLeafMapper(request.getParameter("leafMapper"));
 			leaf.setLeafIcon(request.getParameter("leafIcon"));
 			leaf.setCreateTime(new Date());
 
 			leaf.setChildren(child);
 			child.getLeafs().add(leaf);
 			session.save(child);
 
 			session.getTransaction().commit();
 			session.close();
 			return showLeafList();
 
 		} else {
 
 			session.beginTransaction();
 
 			Children child = (Children) session
 					.createQuery(
 							"from Children where " + "childrenId = :childrenId")
 					.setParameter(
 							"childrenId",
 							Integer.parseInt(request.getParameter("childrenId")))
 					.list().get(0);
 
 			Leaf leaf = (Leaf) session
 					.createQuery("from Leaf where " + "leafId = :leafId")
 					.setParameter("leafId",
 							Integer.parseInt(request.getParameter("leafId")))
 					.list().get(0);
 
 			leaf.setLeafSequence(Integer.parseInt(request
 					.getParameter("leafSequence")));
 			leaf.setLeafName(request.getParameter("leafName"));
 			leaf.setLeafDescription(request.getParameter("leafDescription"));
 			leaf.setLeafMapper(request.getParameter("leafMapper"));
 			leaf.setLeafIcon(request.getParameter("leafIcon"));
 			leaf.setUpdatedTime(new Date());
 
 			leaf.setChildren(child);
 			child.getLeafs().add(leaf);
 			session.saveOrUpdate(child);
 
 			session.getTransaction().commit();
 			return showLeafList();
 		}
 		}
 	
 	public ArrayList<Children> addChildren(HttpServletRequest request) throws HibernateException, SecurityException, NoSuchFieldException, ParseException {
 
 		session = HibernateUtil.getSessionFactory().openSession();
 		
 		if (request.getParameter("childrenId").toString().length() == 0 ) {
 
 			session.beginTransaction();
 
 			Accordian accordian = (Accordian) session
 					.createQuery(
 							"from Accordian where " + "accordianId = :accordianId")
 					.setParameter(
 							"accordianId",
 							Integer.parseInt(request.getParameter("accordianId")))
 					.list().get(0);
 
 			Children children = new Children();
 			children.setChildrenId(null);
 			children.setChildrenSequence(Integer.parseInt(request.getParameter("childrenSequence")));
 			children.setChildrenName(request.getParameter("childrenName"));
 			children.setChildrenDescription(request.getParameter("childrenDescription"));
 			children.setChildrenIcon(request.getParameter("childrenIcon"));
 			children.setChildrenMapper(request.getParameter("childrenMapper"));
 			children.setCreateTime(new Date());
 
 			children.setAccordian(accordian);
 			accordian.getChildrens().add(children);
 			session.save(accordian);
 
 			session.getTransaction().commit();
 			session.close();
 			return showChildrenList();
 
 		} else {
 
 			session.beginTransaction();
 
 			Accordian accordian = (Accordian) session
 					.createQuery(
 							"from Accordian where " + "accordianId = :accordianId")
 					.setParameter(
 							"accordianId",
 							Integer.parseInt(request.getParameter("accordianId")))
 					.list().get(0);
 
 			Children children = (Children) session
 					.createQuery("from Children where " + "childrenId = :childrenId")
 					.setParameter("childrenId",
 							Integer.parseInt(request.getParameter("childrenId")))
 					.list().get(0);
 
 			children.setChildrenSequence(Integer.parseInt(request.getParameter("childrenSequence")));
 			children.setChildrenName(request.getParameter("childrenName"));
 			children.setChildrenDescription(request.getParameter("childrenDescription"));
 			children.setChildrenIcon(request.getParameter("childrenIcon"));
 			children.setChildrenMapper(request.getParameter("childrenMapper"));
 			children.setUpdatedTime(new Date());
 			children.setAccordian(accordian);
 			accordian.getChildrens().add(children);
 			session.saveOrUpdate(accordian);
 
 			session.getTransaction().commit();
 			return showChildrenList();
 		}
 		}
 	
 	public ArrayList<Accordian> addAccordian(HttpServletRequest request) {
 		
 		session = HibernateUtil.getSessionFactory().openSession();
 		
 		if (request.getParameter("accordianId").toString().length() == 0 ) {
 
 			session.beginTransaction();
 			
 			AccessLevel accessLevel =  (AccessLevel) session.createQuery("from AccessLevel where" +
 					" accessLevelId = :accessLevelId").setParameter("accessLevelId",Integer.parseInt(request.getParameter("accessLevelId"))).list().get(0);
 			
 		    Accordian accordian = new Accordian();
 			accordian.setAccordianId(null);
 			accordian.setAccordianSequence(Integer.parseInt(request.getParameter("accordianSequence")));
 			accordian.setAccordianName(request.getParameter("accordianName"));
 			accordian.setAccordianDescription(request.getParameter("accordianDescription"));
 			accordian.setAccordianIcon(request.getParameter("accordianIcon"));
 			accordian.setCreateTime(new Date());
 			accordian.setAccessLevel(accessLevel);
 			accessLevel.getAccordians().add(accordian);
 			session.save(accordian);
 
 			session.getTransaction().commit();
 			session.close();
 			return showAccordianList();
 
 		} else {
 
 			session.beginTransaction();
 
 			AccessLevel accessLevel =  (AccessLevel) session.createQuery("from AccessLevel where" +
 			" accessLevelId = :accessLevelId").setParameter("accessLevelId",Integer.parseInt(request.getParameter("accessLevelId"))).list().get(0);
 	
 			Accordian accordian = (Accordian) session
 					.createQuery(
 							"from Accordian where " + "accordianId = :accordianId")
 					.setParameter(
 							"accordianId",
 							Integer.parseInt(request.getParameter("accordianId")))
 					.list().get(0);
 
 			accordian.setAccordianSequence(Integer.parseInt(request.getParameter("accordianSequence")));
 			accordian.setAccordianName(request.getParameter("accordianName"));
 			accordian.setAccordianDescription(request.getParameter("accordianDescription"));
 			accordian.setAccordianIcon(request.getParameter("accordianIcon"));
 			accordian.setUpdatedTime(new Date());
 			accordian.setAccessLevel(accessLevel);
 			accessLevel.getAccordians().add(accordian);
 			session.saveOrUpdate(accordian);
 
 			session.getTransaction().commit();
 			return showAccordianList();
 		}
 	}
 
 	public ArrayList<Leaf> deleteLeaf(int leafId) throws HibernateException,
 			SecurityException, NoSuchFieldException, ParseException {
 		session = HibernateUtil.getSessionFactory().openSession();
 		session.beginTransaction();
 		Leaf leaf = (Leaf) session.get(Leaf.class, new Integer(leafId));
 		session.delete(leaf);
 		session.getTransaction().commit();
 		session.close();
 		return showLeafList();
 	}
 	
 	public ArrayList<Children> deleteChildren(int childrenId) throws HibernateException,
 			SecurityException, NoSuchFieldException, ParseException {
 		session = HibernateUtil.getSessionFactory().openSession();
 		session.beginTransaction();
 		Children children = (Children) session.get(Children.class, new Integer(childrenId));
 		session.delete(children);
 		session.getTransaction().commit();
 		session.close();
 		return showChildrenList();
 	}
 	
 	public ArrayList<Accordian> deleteAccordian(int accordianId)
 			throws HibernateException, SecurityException, NoSuchFieldException,
 			ParseException {
 		session = HibernateUtil.getSessionFactory().openSession();
 		session.beginTransaction();
 		Query query = session.createQuery("from Accordian where accordianId = :accordianId ");
 		query.setParameter("accordianId", accordianId);
 		Accordian accordian = (Accordian)query.list().get(0);
 		/*Accordian accordian = (Accordian) session.get(Accordian.class, new Integer(
 				accordianId));*/
 		session.delete(accordian);
 		session.getTransaction().commit();
 		session.close();
 		return showAccordianList();
 	}
 }
