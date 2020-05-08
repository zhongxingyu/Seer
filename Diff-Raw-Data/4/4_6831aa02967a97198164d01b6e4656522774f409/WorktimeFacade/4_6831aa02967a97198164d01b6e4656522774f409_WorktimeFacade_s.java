 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.courses.whpp.session;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.courses.whpp.entity.Employee;
 import org.courses.whpp.entity.Worktime;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Roman Kostyrko <nubaseg@gmail.com>
  *         Created on Jun 13, 2012, 8:11:44 PM
  */
 @Stateless
 public class WorktimeFacade extends AbstractFacade<Worktime>
 {
     protected EmployeeFacade employeeFacade = null;
 
     @PersistenceContext(unitName = "org.courses_WHPP-ejb_ejb_1.0-SNAPSHOTPU")
     private EntityManager em;
 
     @Override
     protected EntityManager getEntityManager()
     {
         return em;
     }
 
     public WorktimeFacade()
     {
         super(Worktime.class);
         employeeFacade = new EmployeeFacade();
     }
 
     public Worktime findOpenedByEmployeeId(Integer EmployeeId)
     {
         return (Worktime) em.createNamedQuery("Worktime.findOpenedByEmployeeId").setParameter("EmployeeId", EmployeeId).getSingleResult();
     }
 
     public Boolean logIn(Integer EmployeeId)
     {
         Boolean result = true;
 
         Employee employeeForId = employeeFacade.findById(EmployeeId);
         if(employeeForId == null)
             result = false;
 
         Worktime worktimeForId = findOpenedByEmployeeId(EmployeeId);
         if(worktimeForId != null )
             result = result && logOut(worktimeForId);
 
         this.create(new Worktime(new Date(), null, employeeForId));
 
         return result;
 
     }
 
     public Boolean logOut(Integer EmployeeId)
     {
         Boolean result = true;
 
         Worktime worktimeForId = findOpenedByEmployeeId(EmployeeId);
         if(worktimeForId == null )
             result = false;
         else
             result = result && logOut(worktimeForId);
 
         return result;
     }
 
     protected Boolean logOut(Worktime tl)
     {
         Date curTime = new Date();
         if((curTime.getTime() - tl.getIntime().getTime()) > 43200)
         {
             tl.setOuttime(new Date(tl.getIntime().getTime()+43200));
         }
         else
         {
             tl.setOuttime(curTime);
         }
 
         this.edit(tl);
         return true;         // working ideal by default
     }
 }
