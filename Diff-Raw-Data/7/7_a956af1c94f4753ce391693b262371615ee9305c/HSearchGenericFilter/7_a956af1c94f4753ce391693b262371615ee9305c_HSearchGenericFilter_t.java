 /*
 * Copyright 2010 Bizosys Technologies Limited
 *
 * Licensed to the Bizosys Technologies Limited (Bizosys) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Bizosys licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.bizosys.hsearch.treetable.storage;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.filter.Filter;
 
 import com.bizosys.hsearch.federate.QueryPart;
 import com.bizosys.hsearch.hbase.HbaseLog;
 import com.bizosys.hsearch.treetable.client.HSearchTableMultiQueryExecutor;
 import com.bizosys.hsearch.treetable.client.HSearchTableParts;
 import com.bizosys.hsearch.treetable.client.IHSearchPlugin;
 
 /**
  * @author abinash
  *
  */
 public abstract class HSearchGenericFilter implements Filter {
 
 	public static boolean DEBUG_ENABLED = HbaseLog.l.isDebugEnabled();
 
 	String multiQuery = null;
 	Map<String, String> queryFilters = null;
 	Map<String,QueryPart> queryPayload = new HashMap<String, QueryPart>();
 	Map<String, String> colIdWithType = new HashMap<String, String>();
 
 	public HSearchGenericFilter(){
 	}
 	
 	public HSearchGenericFilter(String query, Map<String, String> details){
 		this.multiQuery = query;
 		this.queryFilters = details;
 	}
 	
 	public abstract HSearchTableMultiQueryExecutor createExector();
 	public abstract IHSearchPlugin createPlugIn(String type) throws IOException ;
 
 	/**
 	 * structured:A OR unstructured:B
 	 * structured:A=f|1|1|1|c|*|*
 	 * unstructured:B=*|*|*|*|*|*
 	 */
 	@Override
 	public void write(DataOutput out) throws IOException {
 		StringBuilder sb = new StringBuilder();
 		sb.append(this.multiQuery);
 		
 		if ( null != queryFilters) {
 			for (String queryP : queryFilters.keySet()) {
 				String input = queryFilters.get(queryP);
 				sb.append('\n').append(queryP).append('=').append(input.toString());
 			}
 		}
 		
 		if ( DEBUG_ENABLED ) {
 			HbaseLog.l.debug("Sending to HBase : " + sb.toString());
 		}
 		
 		byte[] ser = sb.toString().getBytes();
 		out.writeInt(ser.length);
 		out.write(ser);
 	}	
 
 	/**
 	 * structured:A OR unstructured:B
 	 * structured:A=f|1|1|1|c|*|*
 	 * unstructured:B=*|*|*|*|*|*
 	 * TODO:// Replace with Fast Split.
 	 */
 	@Override
 	public void readFields(DataInput in) throws IOException {
 		int length = in.readInt();
 		if ( 0 == length) throw new IOException("Invalid Query");
 		
 		byte[] ser = new byte[length];
 		in.readFully(ser, 0, length);
 
 		StringTokenizer stk = new StringTokenizer(new String(ser), "\n");
 		
 		boolean isFirst = true;
 		while ( stk.hasMoreTokens() ) {
 			if ( isFirst ) {
 				this.multiQuery = stk.nextToken();
 				this.queryPayload = new HashMap<String, QueryPart>();
 				isFirst = false;
 
 				if ( DEBUG_ENABLED ) {
 					HbaseLog.l.debug("HBase Region Server: Multi Query" +  this.multiQuery);
 				}
 				
 			} else {
 				String line = stk.nextToken();
 				int splitIndex = line.indexOf('=');
 				if ( -1 == splitIndex) throw new IOException("Expecting [=] in line " + line);
 				
 				String colNameQuolonId = line.substring(0,splitIndex);
 				String filtersPipeSeparated =  line.substring(splitIndex+1);
 				
 				int colNameAndQIdSplitIndex = colNameQuolonId.indexOf(':');
 				if ( -1 == colNameAndQIdSplitIndex || colNameQuolonId.length() - 1 == colNameAndQIdSplitIndex) {
 					throw new IOException("Sub queries expected as  X:Y eg.\n" + 
 							 "structured:A OR unstructured:B\nstructured:A=f|1|1|1|c|*|*\nunstructured:B=*|*|*|*|*|*");
 				}
 				String colName = colNameQuolonId.substring(0,colNameAndQIdSplitIndex);
 				String qId =  colNameQuolonId.substring(colNameAndQIdSplitIndex+1);
 				
 				if ( DEBUG_ENABLED ) {
 					HbaseLog.l.debug("colName:qId = " + colName + "/" + qId);
 				}
 				
 				colIdWithType.put(qId, colName);
 				
 				this.queryPayload.put(
 						colNameQuolonId, new QueryPart(filtersPipeSeparated, HSearchTableMultiQueryExecutor.PLUGIN,createPlugIn(colName) ) );
 
 				if ( DEBUG_ENABLED ) {
 					HbaseLog.l.debug("HBase Region Server: Query Payload" +  line);
 				}
 			
 			}
 		}
 	}
 	
 	@Override
 	public void filterRow(List<KeyValue> kvL) {
 		if ( null == kvL) return;
 		int kvT = kvL.size();
 		if ( 0 == kvT) return;
 		
 		if ( DEBUG_ENABLED ) {
 			HbaseLog.l.debug("Processing @ Region Server : filterRow" );
 		}
 		
 		
 		try {
 			byte[] row = null;
 			byte[] firstFamily = null;
 			byte[] firstCol = null;
 			
 			Map<String, HSearchTableParts> colParts = new HashMap<String, HSearchTableParts>();
 			//colParts.put("structured:A", bytes);
 			
 			HSearchTableMultiQueryExecutor intersector = createExector();
 
 			for (KeyValue kv : kvL) {
 				if ( null == kv) continue;
 
 				byte[] inputData = kv.getValue();
 				if ( null == inputData) continue;
 				
 				String fName = new String(kv.getFamily());
				
				int fNameI = fName.indexOf('_');
				fName = fName.substring(0, fNameI - 1);
				
 				HSearchTableParts parts =  null;
				if ( colParts.containsKey(fName)) {
 					parts = colParts.get(fName);
 				} else {
 					parts = new HSearchTableParts();
 					colParts.put(fName, parts);
 				}
 
 				parts.collect(inputData);
 				if ( null == row ) {
 					firstFamily = kv.getFamily();
 					firstCol = kv.getQualifier();
 					row = kv.getRow();
 				}
 			}
 			
 			if ( DEBUG_ENABLED ) {
 				HbaseLog.l.debug("queryData HSearchTableParts creation. ");
 			}
 			
 			Map<String, HSearchTableParts> queryData = new HashMap<String, HSearchTableParts>();
 			for (String queryId : colIdWithType.keySet()) { //A
 				String queryType = colIdWithType.get(queryId); //structured
 				HSearchTableParts parts = colParts.get(queryType);
 				queryData.put((queryType + ":" + queryId), parts);
 			}
 			colParts.clear();
 			colParts = null;
 
 			if ( DEBUG_ENABLED ) {
 				HbaseLog.l.debug("intersector.executeForCols ");
 			}
 			
 			byte[] intersectedIds = intersector.executeForCols(
 					queryData, this.multiQuery, this.queryPayload);
 			
 			kvL.clear();
 			kvL.add(new KeyValue(row, firstFamily, firstCol, intersectedIds));
 		} catch (Exception ex) {
 			ex.printStackTrace(System.err);
 		} 
 	}
 
 	@Override
 	public void reset() {
 	}	
 	
 	@Override
 	public boolean hasFilterRow() {
 		return true;
 	}	
 	
 	@Override
 	public KeyValue getNextKeyHint(KeyValue arg0) {
 		return null;
 	}	
 	
 	@Override
 	public boolean filterRowKey(byte[] rowKey, int offset, int length) {
 		return false;
 	}
 	
 	@Override
 	public boolean filterAllRemaining() {
 		return false;
 	}
 	
 	@Override
 	public boolean filterRow() {
 		return false;
 	}
 	
 	@Override
 	public ReturnCode filterKeyValue(KeyValue arg0) {
 		return ReturnCode.INCLUDE;
 	}	
 }
