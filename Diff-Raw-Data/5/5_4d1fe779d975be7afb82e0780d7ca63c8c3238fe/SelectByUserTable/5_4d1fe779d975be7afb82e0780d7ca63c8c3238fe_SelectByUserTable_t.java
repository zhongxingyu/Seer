 package org.sakaiproject.tool.mailtool;
 
 import org.sakaiproject.tool.mailtool.EmailUser;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 
 public class SelectByUserTable {
 	public class TableEntry
 	{
 		/* Two names per row */
 		protected EmailUser m_user1 = null;
 		protected EmailUser m_user2 = null;
 		
 		protected boolean m_selected1 = false;
 		protected boolean m_selected2 = false;
 		
 		public TableEntry() {	}
 		
 		public EmailUser getUser1() { return m_user1; }
 		public EmailUser getUser2() { return m_user2; }
 		public boolean isSelected1() { return m_selected1; }
 		public boolean isSelected2() { return m_selected2; }
 		
 		public void setUser1(EmailUser user) { m_user1 = user; }
 		public void setUser2(EmailUser user) { m_user2 = user; }
 		public void setSelected1(boolean selected) { m_selected1 = selected; }
 		public void setSelected2(boolean selected) { m_selected2 = selected; }
 		
 		public boolean isRender2()
 		{
 			if (m_user2 == null)
 				return false;
 			else
 				return true;
 		}
 	}
 	
 	List /** TableEntry **/ m_tablerows = new ArrayList();
 	
 	public SelectByUserTable(List /* EmailUsers */ users)
 	{
 		m_tablerows.clear();
		Collections.sort(users);
 		for (Iterator i = users.iterator(); i.hasNext();)
 		{
 			EmailUser euser1 = (EmailUser) i.next();
 			TableEntry te = new TableEntry();
 			te.setUser1(euser1);
 			
 			if (i.hasNext())
 			{
 				EmailUser euser2 = (EmailUser) i.next();
 				te.setUser2(euser2);
 			}
 			
			
 			m_tablerows.add(te);
 		}
 	}
 	
 	public DataModel getUserRows()
 	{
 		DataModel returnmodel = new ListDataModel();
 		returnmodel.setWrappedData(m_tablerows);
 		return returnmodel;
 	}
 
 	public List /* EmailUsers */ getSelectedUsers()
 	{
 		List returnusers = new ArrayList();
 		for (Iterator i = m_tablerows.iterator(); i.hasNext();)
 		{
 			TableEntry te = (TableEntry) i.next();
 			
 			if (te.isSelected1())
 				returnusers.add(te.getUser1());
 			
 			if (te.isSelected2())
 				returnusers.add(te.getUser2());
 		}
 		
 		return returnusers;
 	}
 }
