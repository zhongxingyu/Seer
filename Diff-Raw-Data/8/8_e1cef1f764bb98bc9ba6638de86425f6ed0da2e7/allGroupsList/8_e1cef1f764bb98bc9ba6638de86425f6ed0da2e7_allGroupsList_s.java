 package server.listModel;
 
 import java.rmi.RemoteException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
import common.allGroupsListI;

 import server.model.Group;
 import server.model.Model;
 import server.system.StorageServer;
 
 public class allGroupsList extends ListModel implements allGroupsListI {
 
 	ArrayList<Group> list = new ArrayList<Group>();
 
 	public allGroupsList() throws RemoteException {
 		super();
 		refresh();
 	}
 
 	public ArrayList<Group> getList() throws RemoteException {
 		return list;
 	}
 
 	private void refresh() {
 		ArrayList<Group> oldList = (ArrayList<Group>) list.clone();
 
 		list = new ArrayList<Group>();
 
		String query = "SELECT groupID FROM Group;";
 		ResultSet result;
 		try {
 			result = Model.getDB().readQuery(query);
 			while (result.next()) {
 				this.list.add(StorageServer.groupStorage.get(result
 						.getInt("groupID")));
 			}
 
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			throw new RuntimeException(e);
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 
 		pcs.firePropertyChange("list", oldList, list);
 	}
 }
