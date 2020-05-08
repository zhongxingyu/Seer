 package module.dashBoard.domain;
 
 import pt.ist.fenixframework.pstm.Transaction;
 
 public class Note extends Note_Base {
 
     public Note() {
 	super();
 	setDashBoardController(DashBoardController.getInstance());
     }
 
     public void delete() {
 	removeDashBoardController();
	deleteDomainObject();
     }
 
 }
