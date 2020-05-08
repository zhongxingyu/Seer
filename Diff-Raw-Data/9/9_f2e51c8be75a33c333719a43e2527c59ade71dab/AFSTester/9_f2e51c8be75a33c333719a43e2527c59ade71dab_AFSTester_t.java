 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 
 /**
  * TODO Put here a description of what this class does.
  *
  * @author schepedw.
  *         Created Mar 23, 2013.
  */
 public class AFSTester {
 

 	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, SQLException {
 		AFSCommand command= new AFSCommand("testName","","");
 		command.execute();
 		AFSCommand command2=new AFSCommand("2013-04-10_13:54","","testFilterApplier");
 		command2.execute();
 		AFSCommand command3=new AFSCommand("2013-03-23_15:28","Examples/output.txt","");
 		command3.execute();
 	}
 
 }
