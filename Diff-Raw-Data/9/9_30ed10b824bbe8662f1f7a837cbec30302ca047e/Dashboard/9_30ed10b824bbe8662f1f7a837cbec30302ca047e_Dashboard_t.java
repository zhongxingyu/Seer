 package timeSheet;
 
 import timeSheet.database.entity.Employee;
 
 import javax.servlet.http.HttpSession;
 
 /**
  * User: John Lawrence
  * Date: 12/10/10
  * Time: 12:43 AM
  */
 public class Dashboard {
     private HttpSession session;
 
     public Dashboard(HttpSession session) {
         this.session = session;
     }
 
     public String getMenu() {
         StringBuilder sortedMenu = new StringBuilder();
         if (session.getAttribute(SessionConst.employee.toString()) == null) {
             return "";
         }
         Employee currentEmployee = (Employee) session.getAttribute(SessionConst.employee.toString());
         switch (currentEmployee.getRole()) {
             case Administrator:
                 sortedMenu.append("<a href=\"manageGroups.jsp\">Manage Groups</a><br />");
                 sortedMenu.append("<a href=\"manageEmployees.jsp\">Manage Employees</a><br />");
                 sortedMenu.append("<a href=\"manageSettings.jsp\">Manage Settings</a><br />");
                 sortedMenu.append("<a href=\"manageHourTypes.jsp\">Manage Hour Types</a><br />");
             case Executive:
                 sortedMenu.append("<a href=\"reports/reports.jsp\">Reports</a><br />");
             case Manager:
             case AssistantManager:
             case TimeSheetApproval:
                sortedMenu.insert(0, "<a href=\"manageTime.jsp\">Manage Time</a><br />");
             case Employee:
                sortedMenu.insert(0, "<a href=\"manageUser.jsp\">Manage Account</a><br />");
                if (!currentEmployee.getSalary()) {
                    sortedMenu.insert(0, "<a href=\"timeEntering.jsp\">Enter Time</a><br />");
                }
         }
         return sortedMenu.toString();
     }
 
     public String getName() {
         Object attribute = session.getAttribute(SessionConst.employee.toString());
         if (attribute != null) {
             return ((Employee) attribute).getName();
         }
         return "";
     }
 }
