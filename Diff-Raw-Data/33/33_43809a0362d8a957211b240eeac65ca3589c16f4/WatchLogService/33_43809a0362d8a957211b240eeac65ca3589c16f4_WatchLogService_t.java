 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cn.orz.pascal.gfmonitor.service;
 
 import cn.orz.pascal.gfmonitor.model.monitor.ServerMonitor;
 import cn.orz.pascal.gfmonitor.model.monitor.SystemMonitor;
 import cn.orz.pascal.gfmonitor.model.monitor.RequestMonitor;
 import cn.orz.pascal.gfmonitor.model.monitor.SessionMonitor;
 import cn.orz.pascal.gfmonitor.model.dao.requestmonitor.CountrequestsFacade;
 import cn.orz.pascal.gfmonitor.model.dao.requestmonitor.ErrorcountFacade;
 import cn.orz.pascal.gfmonitor.model.dao.requestmonitor.MaxtimeFacade;
 import cn.orz.pascal.gfmonitor.model.dao.requestmonitor.ProcessingtimeFacade;
 import cn.orz.pascal.gfmonitor.model.dao.sessionmonitor.ActivatedsessionstotalFacade;
 import cn.orz.pascal.gfmonitor.model.dao.sessionmonitor.ActivesessionscurrentFacade;
 import cn.orz.pascal.gfmonitor.model.dao.sessionmonitor.ExpiredsessionstotalFacade;
 import cn.orz.pascal.gfmonitor.model.dao.sessionmonitor.PassivatedsessionstotalFacade;
 import cn.orz.pascal.gfmonitor.model.dao.sessionmonitor.PersistedsessionstotalFacade;
 import cn.orz.pascal.gfmonitor.model.dao.sessionmonitor.RejectedsessionstotalFacade;
 import cn.orz.pascal.gfmonitor.model.dao.severmonitor.ActiveservletsloadedcountFacade;
 import cn.orz.pascal.gfmonitor.model.dao.severmonitor.ServletprocessingtimesFacade;
 import cn.orz.pascal.gfmonitor.model.dao.severmonitor.TotalservletsloadedcountFacade;
 import cn.orz.pascal.gfmonitor.model.dao.system.SystemLogFacade;
import java.util.Date;
 import javax.ejb.EJB;
import javax.ejb.Schedule;
 import javax.jws.WebService;
 import javax.ejb.Stateless;
 
 /**
  *
  * @author koduki
  */
 @WebService(serviceName = "WatchLogService")
 @Stateless()
 public class WatchLogService {
     @EJB
     ServerMonitor serverMonitor;
     @EJB
     SessionMonitor sessionMonitor;
     @EJB
     RequestMonitor requestMonitor;
     @EJB
     SystemMonitor systemMonitor;
     
     @EJB
     ActiveservletsloadedcountFacade activeservletsloadedcountFacade;
     @EJB
     ServletprocessingtimesFacade servletprocessingtimesFacade;
     @EJB
     TotalservletsloadedcountFacade totalservletsloadedcountFacade;
     @EJB
     ActivatedsessionstotalFacade activatedsessionstotalFacade;
     @EJB
     ActivesessionscurrentFacade activesessionscurrentFacade;
     @EJB
     ExpiredsessionstotalFacade expiredsessionstotalFacade;
     @EJB
     PassivatedsessionstotalFacade passivatedsessionstotalFacade;
     @EJB
     PersistedsessionstotalFacade persistedsessionstotalFacade;
     @EJB
     RejectedsessionstotalFacade rejectedsessionstotalFacade;
     @EJB
     CountrequestsFacade countrequestsFacade;
     @EJB
     ErrorcountFacade errorcountFacade;
     @EJB
     MaxtimeFacade maxtimeFacade;
     @EJB
     ProcessingtimeFacade processingtimeFacade;
     @EJB
     SystemLogFacade systemLogFacade;
 
    @Schedule(minute="*/3", hour="*")
    public void writeLog() throws Exception {
         // server-mon
         activeservletsloadedcountFacade.create(serverMonitor.getActiveservletsloadedcount());
         servletprocessingtimesFacade.create(serverMonitor.getServletprocessingtimes());
         totalservletsloadedcountFacade.create(serverMonitor.getTotalservletsloadedcount());
 
         // session-mon
         activatedsessionstotalFacade.create(sessionMonitor.getActivatedsessionstotal());
         activesessionscurrentFacade.create(sessionMonitor.getActivesessionscurrent());
         expiredsessionstotalFacade.create(sessionMonitor.getExpiredsessionstotal());
         passivatedsessionstotalFacade.create(sessionMonitor.getPassivatedsessionstotal());
         persistedsessionstotalFacade.create(sessionMonitor.getPersistedsessionstotal());
         rejectedsessionstotalFacade.create(sessionMonitor.getRejectedsessionstotal());
         
         // request-mon
         countrequestsFacade.create(requestMonitor.getCountrequests());
         errorcountFacade.create(requestMonitor.getErrorcount());
         maxtimeFacade.create(requestMonitor.getMaxtime());
         processingtimeFacade.create(requestMonitor.getProcessingtime());
         
         // system-mon
         systemLogFacade.create(systemMonitor.getSystemLog());
           
        System.out.println(new Date(System.currentTimeMillis()));
     }
 }
