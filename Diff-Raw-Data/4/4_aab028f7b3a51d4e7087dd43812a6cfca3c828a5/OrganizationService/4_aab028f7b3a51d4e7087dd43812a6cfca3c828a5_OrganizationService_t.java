 package org.CommunityService.Services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.CommunityService.EntitiesMapped.Organization;
 import org.hibernate.HibernateException;
 
 public class OrganizationService {
 
 	public static Organization getOrganizationById(int orgId) throws HibernateException {
		String hql = "from Organization as o where o.orgId=?";
 		ArrayList<Integer> params = new ArrayList<Integer>();
 		params.add(orgId);
 		try {
 			@SuppressWarnings("unchecked")
 			Organization o = (Organization) (((List<Organization>) DBConnection.query(hql, params)).get(0));
 			return o;
 		} catch (HibernateException e) {
 			return null;
 		} catch (IndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 	
 	public static void addOrganization(Organization o) throws Exception {
 		DBConnection.persist(o);
 		return;
 	}	
 }
