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
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.hbase.client.coprocessor.Batch;
 
 import com.bizosys.hsearch.hbase.ColumnFamName;
 import com.bizosys.hsearch.hbase.HTableWrapper;
 import com.bizosys.hsearch.hbase.HbaseLog;
 
 public final class HSearchBytesCoProcessorProxy {
 	
 	public static boolean INFO_ENABLED = HbaseLog.l.isInfoEnabled();
 	
 	HSearchBytesFilter filter = null;
 	byte[][] families = null;
 	byte[][] cols = null;
 	
 	public HSearchBytesCoProcessorProxy(final List<ColumnFamName> family_cols , final HSearchBytesFilter filter) throws IOException {
 		this.filter = filter;
 		
 		if (null == family_cols) throw new IOException("Please provide family details. Scan on all cols are not allowed");
 		this.families = new byte[family_cols.size()][];
 		this.cols = new byte[family_cols.size()][];
 		
 		int seq = -1;
 		for (ColumnFamName columnFamName : family_cols) {
 			seq++;
 			this.families[seq] = columnFamName.family;
 			this.cols[seq] = columnFamName.name;
 		}
 
 	}
 	
 	public final Map<byte[], byte[]> execCoprocessorRows(final HTableWrapper table) throws IOException, Throwable  {
 
 		Map<byte[], byte[]> output = table.table.coprocessorExec(
                HSearchBytesCoprocessorI.class, null, null,
                 
                 
                new Batch.Call<HSearchBytesCoprocessorI, byte[]>() {
                     @Override
                    public final byte[] call(HSearchBytesCoprocessorI counter) throws IOException {
                         return counter.getRows(families, cols, filter);
                  }
          } );
 		
 		return output;
 	}
 }
