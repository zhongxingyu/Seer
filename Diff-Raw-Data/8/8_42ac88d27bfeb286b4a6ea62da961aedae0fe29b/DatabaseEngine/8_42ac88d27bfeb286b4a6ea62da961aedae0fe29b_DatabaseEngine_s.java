 package org.publicmain.sql;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import org.publicmain.common.Config;
 import org.publicmain.common.LogEngine;
 import org.publicmain.common.MSG;
 import org.publicmain.common.Node;
 import org.publicmain.nodeengine.NodeEngine;
 
 public class DatabaseEngine {
 	
 	private LocalDBConnection localDB;
 	private BackupDBConnection backupDB;
 	
 	private BlockingQueue<MSG> msg2Store;
 	private BlockingQueue<Node> node2Store;
 	private BlockingQueue<Map.Entry<Long, Long>> routes2Store;
 	private BlockingQueue<String> groups2Store; 
 	
 	private HashSet<MSG> failed_msgs;
 	private List<Node> failed_node;
 	private BlockingQueue<Map.Entry<Long, Long>> failed_routes;
 //	private HashSet<Node> stored_nodes;
 //	private HashSet<Node> stored_routes;
  
 	private static DatabaseEngine me;
 	Thread transporter = new Thread(new DPTransportBot());
 	
 	private DatabaseEngine() {
 		me=this;
 		localDB = LocalDBConnection.getDBConnection(this);
 		backupDB = BackupDBConnection.getBackupDBConnection();
 		
 		msg2Store = new LinkedBlockingQueue<MSG>();
 		node2Store = new LinkedBlockingQueue<Node>();
 		routes2Store = new LinkedBlockingQueue<Map.Entry<Long,Long>>();
 		groups2Store = new LinkedBlockingQueue<String>();
 		failed_msgs = new HashSet<MSG>();
 		failed_node = new ArrayList<Node>();
 		failed_routes = new LinkedBlockingQueue<Map.Entry<Long,Long>>();
 		
 		Config.registerDatabaseEngine(this);
 		transporter.start();
 	}
 	
 	public void writeConfig(){
 				if(localDB.getStatus())localDB.writeAllSettingsToDB(Config.getConfig());
 		}
 	
 	public synchronized static DatabaseEngine getDatabaseEngine() {
 		if(me==null) new DatabaseEngine();
 		return me;
 	}
 	
 	public void put(MSG x){
 		msg2Store.offer(x);
 	}
 	
 	public void put(Node x){
 		node2Store.offer(x);
 	}
 
 	public void put(Collection<Node> x){
 		for (Node node : x) {
 			node2Store.offer(node);
 		}
 	}
 	
 	public void put(String group){
 		groups2Store.add(group);
 	}
 	
 
 	public void put(long target, long gateway){
 		routes2Store.offer(new AbstractMap.SimpleEntry(target, gateway));
 	}
 	
 	public void deleteLocalHistory() {
 		localDB.deleteAllMsgs();
 	}
 	
 	public void deleteBackup() {
 //		TODO:Tell BackupServer to Drop users data
 	}
 	
 	public void push(){
 		if(localDB.getStatus()&&backupDB.getStatus()){
 		ResultSet tmp_users 	= localDB.pull_users();
 		backupDB.push_users(tmp_users);
 		ResultSet tmp_messages 	= localDB.pull_msgs();
 		backupDB.push_msgs(tmp_messages);
 		ResultSet tmp_settings 	= localDB.pull_settings();
		
 		backupDB.push_settings(tmp_settings);
 		}
 	}
 	
 	public void pull(){
 		//TODO: HILFE
 	}
 	
 	private final class DPTransportBot implements Runnable {
 		@Override
 		public void run() {
 			boolean locDBWasConnectetBefore = false;
 			while (true) {
 				if (!localDB.getStatus()) {
 					synchronized (transporter) {
 						try {
 							if (locDBWasConnectetBefore) localDB.reconnectToLocDBServer();
 							transporter.wait();
 							locDBWasConnectetBefore = true;
 						} catch (InterruptedException e) {
 						}
 					}
 				}
 				while (true) {
 					// kopiere msgs
 					List<MSG> tmp_msg = new ArrayList<MSG>(msg2Store);
 					msg2Store.removeAll(tmp_msg);
 					
 					// kopiere groups
 					List<String> tmp_groups = new ArrayList<String>(groups2Store);
 					groups2Store.removeAll(tmp_groups);
 					
 					// kopiere nodes
 					List<Node> tmp_nodes = new ArrayList<Node>(node2Store);
 					node2Store.removeAll(tmp_nodes);
 
 					// kopiere routen
 					List<Map.Entry<Long, Long>> tmp_routes = new ArrayList<Map.Entry<Long, Long>>(routes2Store);
 					routes2Store.removeAll(tmp_routes);
 					
 					// schreibe nodes
 					if(!localDB.writeAllUsersToDB(tmp_nodes)){
 						failed_node.addAll(tmp_nodes);
 						if(!localDB.getStatus()) break;
 					}
 					
 					// schreibe groups
 					localDB.writeAllGroupsToDB(tmp_groups);
 					
 					// schreibe msgs
 					boolean all_msgs_written=true;
 					for (MSG current : tmp_msg) {
 						if(!localDB.writeMsgToDB(current)){
 							all_msgs_written=false;
 							failed_msgs.add(current);
 						}
 					}
 					if(!all_msgs_written){
 						if(!localDB.getStatus()){
 							break;
 						}
 					}
 					
 					//schreibe alle routen
 					for (Entry<Long, Long> tmp_route : tmp_routes){
 						if(!localDB.writeRoutingTableToDB(tmp_route.getKey(),NodeEngine.getNE().getNode(tmp_route.getKey()).getHostname(), NodeEngine.getNE().getUIDforNID(tmp_route.getKey()), tmp_route.getValue())){
 							failed_routes.add(tmp_route);
 							break;
 						}
 					}
 					
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Queries the local Database for Messages of a selected user (<code>uid</code>) which have been send after the begin date but before the end date. 
 	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by giving a search text.
 	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
 	 * @param uid UserId of User whose messages should be retrieved
 	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
 	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
 	 * @param text only messages containing this text will be shown. text will be ignored if empty string
 	 * @return a JTable listing all the messages fitting the given attributes (Maybe the Database Engine will update the Datamodel of the HistoryWindow later)
 	 */
 	public JTable selectMSGsByUser(long uid,GregorianCalendar begin, GregorianCalendar end,String text) {
 
 		ResultSet tmpRS;
 		
 		String para_uid	=(uid>=0)?String.valueOf(uid):"%";
 		String para_alias	=null;
 		String para_group	=null;
 		String para_text	=(text.trim().length()==0)?"%":"%"+text.trim()+"%";
 		long para_begin 	= (begin!=null)?begin.getTimeInMillis():0;
 		long para_end 	= (end!=null)?end.getTimeInMillis():Long.MAX_VALUE;
 		
 //		System.out.println(para_uid+para_alias+para_group+"<"+para_begin+":"+para_end+">"+para_text);
 
 		if (para_begin<para_end) tmpRS =localDB.searchInHistory(para_uid,para_alias,para_group,para_begin,para_end,para_text);
 		else tmpRS =localDB.searchInHistory(para_uid,para_alias,para_group,para_end,para_begin,para_text);
 
 		
 		if(tmpRS!=null){
 			try {
 				
 //				new SimpleTableDemo(new JTable(buildTableModel(tmpRS)));
 				return new JTable(buildTableModel(tmpRS));
 			} catch (SQLException e) {
 				LogEngine.log(this, "Error while building JTable: " + e.getMessage(), LogEngine.ERROR );
 			}
 		}
 		return allMSGs();
 	}
 
 	/**
 	 * Queries the local Database for Messages which have been send with the given <code>alias</code> if send after the begin date but before the end date. 
 	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by providing a search <code>text</code>.
 	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
 	 * @param alias used in message
 	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
 	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
 	 * @param text only messages containing this text will be shown. text will be ignored if empty string
 	 * @return a JTable listing all the messages fitting the given attributes
 	 */	
 	public JTable selectMSGsByAlias(String alias,GregorianCalendar begin, GregorianCalendar end,String text) {
 		ResultSet tmpRS;
 
 		
 		String para_uid		= null;
 		String para_alias	= (alias.trim().length()==0)?"%":"%"+alias.trim()+"%";
 		String para_group	= null;
 		String para_text	= (text.trim().length()==0)?"%":"%"+text.trim()+"%";
 		long para_begin 	= (begin!=null)?begin.getTimeInMillis():0;
 		long para_end 		= (end!=null)?end.getTimeInMillis():Long.MAX_VALUE;
 		
 		System.out.println(para_uid+para_alias+para_group+"<"+para_begin+":"+para_end+">"+para_text);
 
 		if (para_begin<para_end) tmpRS =localDB.searchInHistory(para_uid,para_alias,para_group,para_begin,para_end,para_text);
 		else tmpRS =localDB.searchInHistory(para_uid,para_alias,para_group,para_end,para_begin,para_text);
 
 		if(tmpRS!=null){
 
 //			getResultInStringArray(tmpRS);
 			
 			try {
 //				return new JTable(buildTableModel(tmp));
 				return buildTable(tmpRS);
 			} catch (SQLException e) {
 				System.out.println(e.getMessage());
 
 			}
 		}
 		System.out.println("Abfrage hat nicht geklappt");
 		return allMSGs();
 
 	}
 	
 	public void printberschriften(DatabaseDaten parameterObject){
 		for (String ber: parameterObject.spaltenberschriften) {				
 		System.out.println(ber);	
 		}
 		
 	}
 
 
 	private static DatabaseDaten getResultData(ResultSet rs) throws SQLException{
 		
 		 ResultSetMetaData metaData = rs.getMetaData();
 
 		    // names of columns
 		 int columnCount = metaData.getColumnCount();
 		 String[] spab= new String[columnCount];
 		    for (int column = 1; column <= columnCount; column++) {
 		        spab[column-1]= metaData.getColumnName(column);
 		    }
 
 		    // data of the table
 		    rs.last();
 		    int rows = rs.getRow();
 		    rs.beforeFirst();
 		    
 		    String[][] stringdata = new String[rows][columnCount];
 		    while (rs.next()) {
 		        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 		        	Object zelle = rs.getObject(columnIndex);
 					stringdata[rs.getRow()-1][columnIndex-1]=(zelle!=null)?zelle.toString():"";
 		        }
 		    }
 		    
 		    return new DatabaseDaten(spab, stringdata);
 	}
 	
 	/**
 	 * Queries the local Database for Messages which have been send within the given <code>group</code> if send after the begin date but before the end date. 
 	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by providing a search <code>text</code>.
 	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
 	 * @param group the message was send to
 	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
 	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
 	 * @param text only messages containing this text will be shown. text will be ignored if empty string
 	 * @return a JTable listing all the messages fitting the given attributes
 	 */	
 	public JTable selectMSGsByGroup(String group,GregorianCalendar begin, GregorianCalendar end,String text) {
 		ResultSet tmp;
 
 		String para_uid	=null;
 		String para_alias	=null;
 		String para_group	=(group.trim().length()==0)?"%":"%"+group.trim()+"%";
 		String para_text	=(text.trim().length()==0)?"%":"%"+text.trim()+"%";
 		long para_begin 	= (begin!=null)?begin.getTimeInMillis():0;
 		long para_end 	= (end!=null)?end.getTimeInMillis():Long.MAX_VALUE;
 		
 //		System.out.println(para_uid+para_alias+para_group+"<"+para_begin+":"+para_end+">"+para_text);
 
 		if (para_begin<para_end) tmp =localDB.searchInHistory(para_uid,para_alias,para_group,para_begin,para_end,para_text);else
 		tmp =localDB.searchInHistory(para_uid,para_alias,para_group,para_end,para_begin,para_text);
 
 		if(tmp!=null){
 			try {
 				return new JTable(buildTableModel(tmp));
 			} catch (SQLException e) {
 				System.out.println(e.getMessage());
 
 			}
 		}
 		System.out.println("Abfrage hat nicht geklappt");
 		return allMSGs();
 
 	}
 
 	public JTable allMSGs() {
 		ResultSet tmp =localDB.pull_msgs();
 		if(tmp!=null){
 
 
 			try {
 				return new JTable(buildTableModel(tmp));
 			} catch (SQLException e) {
 				System.out.println(e.getMessage());
 			}
 		}
 		return new JTable();
 
 	}
 
 	public JComboBox<Node> getUsers(){
 		try {
 
 			ResultSet tmp = localDB.pull_users();
 			if(tmp!=null){
 //				ResultSetMetaData meta = tmp.getMetaData();
 
 //				Vector<String> cols_title = new Vector<String>();
 //				int columnCount = meta.getColumnCount();
 //				for (int column = 1; column <= columnCount; column++) {
 //					cols_title.add(meta.getColumnName(column));
 //				}
 //				System.out.println(cols_title);
 //				
 				Vector<Node> nodes = new Vector<Node>();
 				Set <Node> testdata = new HashSet<Node>();
 
 				while (tmp.next()) {
 					Node tmp_node = new Node(tmp.getLong(4),tmp.getLong(1),tmp.getString(3),tmp.getString(2),tmp.getString(5));
 					nodes.add(tmp_node);
 					testdata.add(tmp_node);
 					System.out.println(tmp_node.toString2());
 				}
 				DefaultComboBoxModel<Node> tobi = new DefaultComboBoxModel<Node>(nodes);
 				JComboBox<Node> tmp_combo = new JComboBox<Node>(tobi);
 				tmp_combo.insertItemAt(null, 0);
 				tmp_combo.setSelectedItem(null);
 				return tmp_combo;
 
 
 			}else{
 //				System.out.println("tmp war null" );
 			}
 
 		} catch (SQLException e) {
 			System.out.println(e.getMessage() );
 		}
 		return new JComboBox<Node>();
 
 	}
 	
 	
 	public static JTable buildTable(ResultSet rs) throws SQLException{
 
 //		    ResultSetMetaData metaData = rs.getMetaData();
 //
 //		    // names of columns
 //		    int columnCount = metaData.getColumnCount();
 //		    String[] columnNames = new String[columnCount];
 //		    for (int column = 1; column <= columnCount; column++) {
 //		        columnNames[column-1]=metaData.getColumnName(column);
 //		    }
 //		    System.out.println(columnNames);
 //		    
 //
 //		    // data of the table
 //		    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
 //		    while (rs.next()) {
 //		        Vector<Object> vector = new Vector<Object>();
 //		        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 //		            vector.add(rs.getObject(columnIndex));
 //		        }
 //		        data.add(vector);
 //		    }
 ////		    DefaultTableModel tblModel = new DefaultTableModel(nmbrRows, colHdrs.size());
 ////		    tblModel.setColumnIdentifiers(colHdrs);
 //		    
 //		    data.toArray();
 //		    Object [][] array = new Object[data.size()][columnCount];
 //		    int i  =0;
 //		    for (Vector<Object> tmp : data) {
 //			    array[i++]=tmp.toArray();
 //		}
 //		  
 //		    DefaultTableModel tmod = new DefaultTableModel(data.size(),columnCount);
 //		    tmod.setColumnIdentifiers(columnNames);
 //		    JTable tmp = new JTable(tmod);
 //		    
 //		    return tmp;
 		return new JTable(buildTableModel(rs));
 		
 	}
 	public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
 		DatabaseDaten tmp = getResultData(rs);
 		
 		//	    ResultSetMetaData metaData = rs.getMetaData();
 //
 //	    // names of columns
 //	    Vector<String> columnNames = new Vector<String>();
 //	    int columnCount = metaData.getColumnCount();
 //	    for (int column = 1; column <= columnCount; column++) {
 //	        columnNames.add(metaData.getColumnName(column));
 //	    }
 //
 //	    // data of the table
 //	    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
 //	    while (rs.next()) {
 //	        Vector<Object> vector = new Vector<Object>();
 //	        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 //	            vector.add(rs.getObject(columnIndex));
 //	        }
 //	        data.add(vector);
 //	    }
 //
 	    return new DefaultTableModel(tmp.zelleninhalt,tmp.spaltenberschriften);
 
 	}
 
 	public synchronized void go() {
 		synchronized (transporter) {
 			transporter.notify();
 		}
 	}
 
 	public int createUser(String username, String password) {
 		if(backupDB.getStatus()){
 			if(backupDB.createUser(username, password)){
 				return 2;
 			}
 			return 1;
 		}
 		return 0;
 		
 	}
 	
 
 }
