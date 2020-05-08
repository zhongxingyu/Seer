 /**                                                                                                                                                                                
  * Copyright (c) 2010 Yahoo! Inc. All rights reserved.                                                                                                                             
  *                                                                                                                                                                                 
  * Licensed under the Apache License, Version 2.0 (the "License"); you                                                                                                             
  * may not use this file except in compliance with the License. You                                                                                                                
  * may obtain a copy of the License at                                                                                                                                             
  *                                                                                                                                                                                 
  * http://www.apache.org/licenses/LICENSE-2.0                                                                                                                                      
  *                                                                                                                                                                                 
  * Unless required by applicable law or agreed to in writing, software                                                                                                             
  * distributed under the License is distributed on an "AS IS" BASIS,                                                                                                               
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or                                                                                                                 
  * implied. See the License for the specific language governing                                                                                                                    
  * permissions and limitations under the License. See accompanying                                                                                                                 
  * LICENSE file.                                                                                                                                                                   
  */
 
 package com.yahoo.ycsb;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Enumeration;
 import java.util.Vector;
 import org.apache.zookeeper.*;
 import org.apache.zookeeper.KeeperException.ConnectionLossException;
 import org.apache.zookeeper.ZooDefs.Ids;
 import org.apache.zookeeper.ZooKeeper.States;
 
 /**
  * Basic DB that just prints out the requested operations, instead of doing them against a database.
  */
 public class ZKDB extends DB implements Watcher
 {
 	public BufferedWriter logger = null;
 	public ArrayList<Integer> partitions = new ArrayList<Integer>();
 	public String[] machines = new String[3];
 	public String[] portsPerPartition = new String[3];
 	public String[] znodes = {"/db1", "/db2", "/db3"};
 	
 	int rrCount = 0;
 	
 	String logFileName;
 	/**
 	 * 2D array of dimensions 3*3
 	 * each row stands for the partition for which client wants to send read/write request
 	 * 1st column in each row stands for the replica which is on SSD
 	 * 2nd and 3rd columns are for HDD replicas
 	 * 2nd stands for leader replica
 	 */
 	private ZooKeeper[][] paxosInstances = new ZooKeeper[3][3];
 	// naiveMode = 0		:	complex client
 	// naiveMode = 1		: 	rr client
 	// naiveMode = 2		:	random client
 	private int naiveMode = 0;
 	public static final String DEFAULT_NAIVE_MODE = "0";
 	
 	//int randPartition;
 	//public static final String DEFAULT_RANDOM_PARTITION = "1";
 	
 	public long maxKey = 268435456;		//hex - 10000000
 	
 	public static final String VERBOSE="zkdb.verbose";
 	public static final String VERBOSE_DEFAULT="true";
 	
 	public static final String SIMULATE_DELAY="zkdb.simulatedelay";
 	public static final String SIMULATE_DELAY_DEFAULT="0";
 	
 	
 	boolean verbose;
 	int todelay;
 
 	public ZKDB()
 	{
 		todelay=0;
 	}
 
 	protected void finalize() {
 		long initTime = System.currentTimeMillis();
 		logger = getLoggerHandle();
 		try {
 			logger.write("********************** INIT TIME: " + initTime + " ***********************\n");
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 		closeLoggerHandle(logger);
 	}
 	
 	private BufferedWriter getLoggerHandle() {
 		
 		File logFile = new File(this.logFileName);
 		FileWriter fop = null;
 		BufferedWriter out = null;
 		try {
 			// if file doesn't exists, then create it
 			if (!logFile.exists()) {
 				logFile.createNewFile();
 			}
 			fop = new FileWriter(logFile, false);
 			out = new BufferedWriter(fop);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return out;		
 	}
 	
 
 	private void closeLoggerHandle(BufferedWriter logger) {
 		
 		try {
 			logger.flush();
 			logger.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 	void delay()
 	{
 		if (todelay>0)
 		{
 			try
 			{
 				Thread.sleep((long)Utils.random().nextInt(todelay));
 			}
 			catch (InterruptedException e)
 			{
 				//do nothing
 			}
 		}
 	}
 
 	//@Override
 	public void process(WatchedEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Initialize any state for this DB.
 	 * Called once per DB instance; there is one DB instance per client thread.
 	 */
 	@SuppressWarnings("unchecked")
 	public void init()
 	{
 		logFileName = getProperties().getProperty("logFileName");
 		long startTime = System.currentTimeMillis();
 		logger = getLoggerHandle();
 		try {
 			logger.write("********************** INIT TIME: " + startTime + " ***********************\n");
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 		
 		partitions.add(0, new Integer(1));
 		partitions.add(1, new Integer(2));
 		partitions.add(2, new Integer(3));
 		
 		verbose=Boolean.parseBoolean(getProperties().getProperty(VERBOSE, VERBOSE_DEFAULT));
 		todelay=Integer.parseInt(getProperties().getProperty(SIMULATE_DELAY, SIMULATE_DELAY_DEFAULT));
 		
 		for ( int i = 1; i <= 3; i++ ) {
 			machines[i-1] = getProperties().getProperty("machine"+i);
 			portsPerPartition[i-1] = getProperties().getProperty("portForPartition"+i);			
 		}
 		maxKey = Long.parseLong(getProperties().getProperty("recordcount"));
 			
 		naiveMode = Integer.parseInt(getProperties().getProperty("naiveMode", DEFAULT_NAIVE_MODE));
 		
 		if (verbose)
 		{
 			System.out.println("***************** properties *****************");
 			Properties p=getProperties();
 			if (p!=null)
 			{
 				for (Enumeration e=p.propertyNames(); e.hasMoreElements(); )
 				{
 					String k=(String)e.nextElement();
 					System.out.println("\""+k+"\"=\""+p.getProperty(k)+"\"");
 				}
 			}
 			System.out.println("**********************************************");
 		}
 		
 		ZooKeeper read, write = null;
 		int i = 0, j = 0, k = 1;
 		String hostPort;
 		try {
 			// i - partition number
 			// j - zk connections per replica
 			/*
 			 * 		j=0			j=1				j=2
 			 * i=0	read[ssd]	write(L)/read	read
 			 * i=1	read[ssd]	write(L)/read	read
 			 * i=2	read[ssd]	wrute(L)/read	read
 			 */
 			/*
 			 * 		read[ssd]	write[L]/read	read
 			 * 
 			 * db1	m1:p1		m2:p1			m3:p1
 			 * 
 			 * db2	m2:p2		m3:p2			m1:p2
 			 * 
 			 * db3	m3:p3		m1:p3			m2:p3
 			 * 
 			 */
 			
 			for(i = 0; i < 3; i++) {
 				k = 1;
 				hostPort = machines[i]+":"+portsPerPartition[i];
 				read = new ZooKeeper(hostPort, 30000, this); 
 				while( read.getState() != States.CONNECTED ) {
 					System.out.println("ZK connection state: "+read.getState());
 				}
 				byte[] tmp = new String(znodes[i]).getBytes("UTF-16");
 				if (read.exists(znodes[i], null) == null) {
 					read.create(znodes[i], tmp, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
 				}
 				paxosInstances[i][0] = read;
 				System.out.println("ZooKeeper connection for - (" + i +", "+ 0 + "), established with :" + hostPort);
 				
 				for(j = 0; j < 3; j++) {
 					if(j != i) {
 						hostPort = machines[j]+":"+portsPerPartition[i];
 						write = new ZooKeeper(hostPort, 30000, this);
 						while( write.getState() != States.CONNECTED ) {
 							System.out.println("ZK connection state: "+write.getState());
 						}
 						paxosInstances[i][k] = write;
 						k++;
 						System.out.println("ZooKeeper connection for - (" + i +", "+ String.valueOf(k-1) + "), established with :" + hostPort);
 					}
 				}		//TEMP
 			}
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (KeeperException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	
 	public void cleanup() { 
 		System.out.println("********* CLEANUP called **********");
 		int i = 0;
 		int j = 0;
 		for(; i < 3; i++) {
 			j = 0;
 			for(; j < 3; j++) {
 				try {
 					paxosInstances[i][j].close();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					//throw DBException;
 				} catch (NullPointerException e) {
 					e.printStackTrace();
 					//throw DBException;
 				}
 			}
 		}	
 	}
 	
 	/**
 	 * Read a record from the database. Each field/value pair from the result will be stored in a HashMap.
 	 *
 	 * @param table The name of the table
 	 * @param key The record key of the record to read.
 	 * @param fields The list of fields to read, or null for all of them
 	 * @param result A HashMap of field/value pairs for the result
 	 * @return Zero on success, a non-zero error code on error
 	 */
 	public int read(String table, String key, Set<String> fields, HashMap<String,ByteIterator> result)
 	{
 		byte[] retData = null;
 		int partitionNo = Integer.parseInt(table);
 		assert( partitionNo >= 1 && partitionNo <= 3 );
 		
 		byte[] keybytes = key.getBytes();
 		byte[] forGetData = new byte[16];
 		Arrays.fill(forGetData, (byte)'0');
 		System.arraycopy(keybytes, 0, forGetData, 16-keybytes.length, keybytes.length);
 		
 		delay();
 
 		if (verbose)
 		{
 			System.out.print("READ "+table+" "+key+" [ ");
 			if (fields!=null)
 			{
 				for (String f : fields)
 				{
 					System.out.print(f+" ");
 				}
 			}
 			else
 			{
 				System.out.print("<all fields>");
 			}
 
 			System.out.println("]");
 		}
 		ZooKeeper zkFinal = null;
 		if ( naiveMode == 0 ) {
 			zkFinal = paxosInstances[partitionNo-1][0];
 		}
 		//naive mode: RR
 		else if ( naiveMode == 1 ) {
 			// RR
 			zkFinal = paxosInstances[partitionNo-1][rrCount%3];
 			rrCount++;
 		}
 		//naive mode: Random Replica
 		else if ( naiveMode == 2 ) {	
 			// Random Replica
 			int ranRep = (int)Math.floor( Math.random() * 3 );
 			zkFinal = paxosInstances[partitionNo-1][ranRep];
 		}
 
 		try {
 			retData = zkFinal.getDataByKey(znodes[partitionNo-1], new String(forGetData));
 			if(retData == null) {
 				System.err.println("******************** retData is null ****************");
 				return -1;
 			}
			System.out.println("READ CALLED FOR "+this);
 		} catch (ConnectionLossException e) {
 			System.out.println("ConnectionLossException recved in getData for PaxosInstance - "+this);
 			e.printStackTrace();
 		} catch (Exception e) {
             e.printStackTrace();
         }
 		return 0;
 	}
 	
 	/**
 	 * Perform a range scan for a set of records in the database. Each field/value pair from the result will be stored in a HashMap.
 	 *
 	 * @param table The name of the table
 	 * @param startkey The record key of the first record to read.
 	 * @param recordcount The number of records to read
 	 * @param fields The list of fields to read, or null for all of them
 	 * @param result A Vector of HashMaps, where each HashMap is a set field/value pairs for one record
 	 * @return Zero on success, a non-zero error code on error
 	 */
 	public int scan(String table, String startkey, int recordcount, Set<String> fields, Vector<HashMap<String,ByteIterator>> result)
 	{
 		delay();
 
 		if (verbose)
 		{
 			System.out.print("SCAN "+table+" "+startkey+" "+recordcount+" [ ");
 			if (fields!=null)
 			{
 				for (String f : fields)
 				{
 					System.out.print(f+" ");
 				}
 			}
 			else
 			{
 				System.out.print("<all fields>");
 			}
 
 			System.out.println("]");
 		}
 
 		return 0;
 	}
 
 	/**
 	 * Update a record in the database. Any field/value pairs in the specified values HashMap will be written into the record with the specified
 	 * record key, overwriting any existing values with the same field name.
 	 *
 	 * @param table The name of the table
 	 * @param key The record key of the record to write.
 	 * @param values A HashMap of field/value pairs to update in the record
 	 * @return Zero on success, a non-zero error code on error
 	 */
 	public int update(String table, String randKey, HashMap<String,ByteIterator> values)
 	{
 		int partitionNo = Integer.parseInt(table);
 		assert( partitionNo >= 1 && partitionNo <= 3 );
 		
 		ByteArrayOutputStream bOut = new ByteArrayOutputStream(100);
 		bOut.reset();
 		try {
 			bOut.write(randKey.getBytes());
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		byte[] key = randKey.getBytes();
 		byte[] value = bOut.toByteArray();
 		byte[] forSetData = new byte[116];
 		Arrays.fill(forSetData, (byte)'0');
 		
 		System.arraycopy(key, 0, forSetData, 16-key.length, key.length);
 		System.arraycopy(value, 0, forSetData, 116-value.length, value.length);
 
 		delay();
 
 		if (verbose)
 		{
 			System.out.print("UPDATE "+table+" "+key+" [ ");
 			if (values!=null)
 			{
 				for (String k : values.keySet())
 				{
 					System.out.print(k+"="+values.get(k)+" ");
 				}
 			}
 			System.out.println("]");
 		}
 
 		ZooKeeper zkFinal = null;
 		
 		zkFinal = paxosInstances[partitionNo-1][1];
 		try {
 			zkFinal.setData(znodes[partitionNo-1], forSetData, -1);
 		} catch (ConnectionLossException e) {
			System.err.println("ConnectionLossException recved in setData for PaxosInstance - ");
 			e.printStackTrace();
 		} catch (Exception e) {
             e.printStackTrace();
         }
 		
 		return 0;
 	}
 
 	/**
 	 * Insert a record in the database. Any field/value pairs in the specified values HashMap will be written into the record with the specified
 	 * record key.
 	 *
 	 * @param table The name of the table
 	 * @param key The record key of the record to insert.
 	 * @param values A HashMap of field/value pairs to insert in the record
 	 * @return Zero on success, a non-zero error code on error
 	 */
 	public int insert(String table, String key, HashMap<String,ByteIterator> values)
 	{
 		delay();
 
 		if (verbose)
 		{
 			System.out.print("INSERT "+table+" "+key+" [ ");
 			if (values!=null)
 			{
 				for (String k : values.keySet())
 				{
 					System.out.print(k+"="+values.get(k)+" ");
 				}
 			}
 
 			System.out.println("]");
 		}
 
 		update( table, key, values );
 		
 		return 0;
 	}
 
 
 	/**
 	 * Delete a record from the database. 
 	 *
 	 * @param table The name of the table
 	 * @param key The record key of the record to delete.
 	 * @return Zero on success, a non-zero error code on error
 	 */
 	public int delete(String table, String key)
 	{
 		delay();
 
 		if (verbose)
 		{
 			System.out.println("DELETE "+table+" "+key);
 		}
 
 		return 0;
 	}
 
 	/**
 	 * Short test of ZKDB
 	 */
 	/*
 	public static void main(String[] args)
 	{
 		ZKDB bdb=new ZKDB();
 
 		Properties p=new Properties();
 		p.setProperty("Sky","Blue");
 		p.setProperty("Ocean","Wet");
 
 		bdb.setProperties(p);
 
 		bdb.init();
 
 		HashMap<String,String> fields=new HashMap<String,String>();
 		fields.put("A","X");
 		fields.put("B","Y");
 
 		bdb.read("table","key",null,null);
 		bdb.insert("table","key",fields);
 
 		fields=new HashMap<String,String>();
 		fields.put("C","Z");
 
 		bdb.update("table","key",fields);
 
 		bdb.delete("table","key");
 	}*/
 }
