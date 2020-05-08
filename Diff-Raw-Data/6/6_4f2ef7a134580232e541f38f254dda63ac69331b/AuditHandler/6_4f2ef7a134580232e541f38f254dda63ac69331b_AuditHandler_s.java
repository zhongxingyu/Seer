 /* 
  *   This file is part of the Open Database Audit Project (ODAP).
  *
  *   ODAP is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Foobar is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  *
  *   The code was developed by Rob Williams
  */
 
 
 package com.odap.server.audit;
 
 import com.odap.common.thrift.*;
 import com.odap.common.util.LogUtil;
 import com.odap.common.hbase.EventHBaseHandler;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.log4j.Logger;
 import org.apache.thrift.TException;
 
 public class AuditHandler implements AuditEvent.Iface {
 
 	//TODO ADD AUDITHANDLER ID TO KEY TO ENSURE MULTIPLE PROCESSORS DON'T COLLIDE
 	private static int count = 1;
 	private static short spin_value =0;
 	private static int prev_timestamp = 0;
 	
 	static Logger logger = LogUtil.getInstance();
 	
 	private static Map<Short,Integer> heartBeat = new ConcurrentHashMap<>();
 	
 	public void store(Message msg) throws TException {
 
 		System.out.println("Here I am" + count++);
 		
 		System.out.println("MSG Server ID: " + msg.getServer_id());
 		System.out.println("MSG Authentication Token: " + msg.getToken()); 
 		
 		//TODO NEED TO KEEP TRACK OF SPIN PER SERVER!!!!!
 
 		
 		for (Event e:msg.events){
 				System.out.println("Source IP: " + e.getSrc_ip());
 				System.out.println("Source Port: " + e.getSrc_port());
 				System.out.println("Source Time:" + e.getTimestamp());
 				System.out.println("Statement: " + e.getStatement());
 				System.out.println("DB Name: " + e.getDbname());
 				System.out.println("Application Name: " + e.getAppname());
 				System.out.println("Row Count: " + e.getRow_count());
 				System.out.println("Username: " + e.getUsername());
 				
 				byte[] c = new byte[Bytes.toBytes(msg.getServer_id()).length +Bytes.toBytes(e.getTimestamp()).length+3];
 				System.arraycopy(Bytes.toBytes(msg.getServer_id()), 0, c, 0, Bytes.toBytes(msg.getServer_id()).length);
 				System.arraycopy(Bytes.toBytes(Integer.MAX_VALUE-e.getTimestamp()), 0, c, Bytes.toBytes(msg.getServer_id()).length, Bytes.toBytes(Integer.MAX_VALUE-e.getTimestamp()).length);
 				
 				System.out.println("prev_time:" + prev_timestamp + " etime:" + e.getTimestamp());
 				if(prev_timestamp == e.getTimestamp()){
 					spin_value++;
 
 					System.out.println("spin value:" + spin_value);
 				}else{
 					prev_timestamp = e.getTimestamp();
 					spin_value = 0;
 				}
 				c[c.length-3] = AuditServer.audit_server_id;
 				c[c.length-2] = (byte)(spin_value >>> 8);
 				c[c.length-1] = (byte)spin_value;
 
 				System.out.println("INSERTING KEY:" + EventHBaseHandler.byteArrayToHexString(c));
 				Put put = new Put( c); 
 				//put.add(Bytes.toBytes("colfam1"), Bytes.toBytes(e.getOperation()), Bytes.toBytes(e.getValue()));
 				put.add(Bytes.toBytes("core"), Bytes.toBytes("src"), Bytes.toBytes(e.getSrc_ip()));
				put.add(Bytes.toBytes("core"), Bytes.toBytes("port"), Bytes.toBytes(e.getSrc_port()));
 				put.add(Bytes.toBytes("core"), Bytes.toBytes("statement"), Bytes.toBytes(e.getStatement()));
 				put.add(Bytes.toBytes("core"), Bytes.toBytes("dbname"), Bytes.toBytes(e.getDbname().toUpperCase()));
 				put.add(Bytes.toBytes("core"), Bytes.toBytes("username"), Bytes.toBytes(e.getUsername().toUpperCase()));
 				put.add(Bytes.toBytes("core"), Bytes.toBytes("application"), Bytes.toBytes(e.getAppname().toUpperCase()));
 				
 				try{
 					AuditServer.table.put(put);
 					AuditServer.table.flushCommits();
 				}catch(Exception ex){
 					System.err.println("Exception occured!!!!!");
 					ex.printStackTrace();
 				}
 		}
 	}
 	
 	public static void checkHeartBeat(){
 
 		int check_time = (int)(System.currentTimeMillis()/1000);
 		for(Map.Entry<Short, Integer> entry : heartBeat.entrySet()){
 			if( (check_time - entry.getValue()) > 60*7 ){
 				// Need to check if we need send out alert
 				heartBeat.remove(entry.getKey());
 			}
 		}
 	}
 	
 	public void heartbeat(short server_id, String token) throws TException {
 		logger.info("Enter heartbeat(" + server_id + ")");
 		heartBeat.put(new Short(server_id), new Integer((int)(System.currentTimeMillis()/1000)));
 		logger.info("Exit heartbeat(" + server_id + ")");
 	}
 }
