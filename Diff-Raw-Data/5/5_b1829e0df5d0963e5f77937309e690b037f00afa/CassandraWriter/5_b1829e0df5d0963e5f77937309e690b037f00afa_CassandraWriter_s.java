 /*
  * roomstore - an irc journaller using cassandra.
  * 
  * Copyright 2011 MeBigFatGuy.com
  * Copyright 2011 Dave Brosius
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations
  * under the License.
  */
 package com.mebigfatguy.roomstore;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.cassandra.thrift.Cassandra;
 import org.apache.cassandra.thrift.CfDef;
 import org.apache.cassandra.thrift.Column;
 import org.apache.cassandra.thrift.ColumnOrSuperColumn;
 import org.apache.cassandra.thrift.ColumnParent;
 import org.apache.cassandra.thrift.ConsistencyLevel;
 import org.apache.cassandra.thrift.InvalidRequestException;
 import org.apache.cassandra.thrift.KsDef;
 import org.apache.cassandra.thrift.NotFoundException;
 import org.apache.cassandra.thrift.SchemaDisagreementException;
 import org.apache.cassandra.thrift.SlicePredicate;
 import org.apache.cassandra.thrift.SliceRange;
 import org.apache.cassandra.thrift.TimedOutException;
 import org.apache.cassandra.thrift.UnavailableException;
 import org.apache.thrift.TException;
 import org.apache.thrift.protocol.TBinaryProtocol;
 import org.apache.thrift.protocol.TProtocol;
 import org.apache.thrift.transport.TFramedTransport;
 import org.apache.thrift.transport.TSocket;
 import org.apache.thrift.transport.TTransport;
 import org.apache.thrift.transport.TTransportException;
 
 public class CassandraWriter {
 
     private static final String KEY_SPACE_NAME = "roomstore";
     private static final String COLUMN_FAMILY_NAME = "messages";
     private static final String STRATEGY_NAME = "SimpleStrategy";
     private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);
     
     private TTransport transport;
     private Cassandra.Client client; 
     
     public CassandraWriter(String cassandraServer) throws TTransportException, TException, InvalidRequestException, SchemaDisagreementException {
         setupClient();
         setupKeyspace();
     }
 
     public void addMessage(String channel, String sender, String hostname, String message) throws TException, UnknownHostException, InvalidRequestException, TimedOutException, UnavailableException {
         
         client.set_keyspace(KEY_SPACE_NAME);
 
         long timestamp = System.currentTimeMillis();
         
         ByteBuffer key = generateKey(channel, sender); 
         Column column = generateColumn(timestamp, hostname, message);
         
         client.insert(key, new ColumnParent(COLUMN_FAMILY_NAME), column, ConsistencyLevel.ONE);
     }
     
     public Message getLastMessage(String channel, String sender) throws TException, UnknownHostException, InvalidRequestException, TimedOutException, UnavailableException {
         
         try {
             ByteBuffer key = generateKey(channel, sender); 
             ColumnParent parent = new ColumnParent(COLUMN_FAMILY_NAME);
             SlicePredicate slice = new SlicePredicate();
             SliceRange range = new SliceRange(EMPTY_BUFFER, EMPTY_BUFFER, true, 1);
             slice.setSlice_range(range);
             List<ColumnOrSuperColumn> columns = client.get_slice(key, parent, slice, ConsistencyLevel.ONE);
             if (columns.size() == 0) {
                 return null;
             }
             
             Column c = columns.get(0).column;
             
             return new Message(channel, sender, new Date(c.getTimestamp()), new String(c.getValue(), "UTF-8"));
         } catch (UnsupportedEncodingException uee) {
             return null;
         }
     }
     
     private void setupClient() throws TTransportException {
         transport = new TFramedTransport(new TSocket("localhost", 9160));
         TProtocol proto = new TBinaryProtocol(transport);
         client = new Cassandra.Client(proto);
         transport.open();
     }
     
     private void setupKeyspace() throws TException, InvalidRequestException, SchemaDisagreementException {
         try {
             client.describe_keyspace(KEY_SPACE_NAME);
         } catch (NotFoundException nfe) {
             List<CfDef> columnDefs = new ArrayList<CfDef>();
             CfDef columnFamily = new CfDef(KEY_SPACE_NAME, COLUMN_FAMILY_NAME);
             columnFamily.setKey_validation_class("CompositeType(UTF8Type,UTF8Type)");
             columnFamily.setComparator_type("CompositeType(LongType,UTF8Type)");
             columnFamily.setDefault_validation_class("UTF8Type");
             columnDefs.add(columnFamily);
            
             KsDef ksdef = new KsDef(KEY_SPACE_NAME, STRATEGY_NAME, columnDefs);
             Map<String, String> options = new HashMap<String, String>();
             options.put("replication_factor", "1");
             ksdef.setStrategy_options(options);
             client.system_add_keyspace(ksdef);
         }
     }
     
     private ByteBuffer generateKey(String channel, String sender) {
         try {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             
            int len = sender.length();
             baos.write((byte) ((len >> 8) & 0xFF));
             baos.write((byte) ((len & 0xFF)));
            baos.write(sender.getBytes("UTF-8"));
             baos.write((byte) 0);
             
             len = sender.length();
             baos.write((byte) ((len >> 8) & 0xFF));
             baos.write((byte) ((len & 0xFF)));
             baos.write(sender.getBytes("UTF-8"));
             baos.write((byte) 0);
             
             return ByteBuffer.wrap(baos.toByteArray());
         } catch (Exception e) {
             throw new Error("Bad Error trying to generate key", e);
         }
     }
     
     private Column generateColumn(long timestamp, String hostName, String message) {
         
         try {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             baos.write((byte)0);
             baos.write((byte)8);
             
             long ts = timestamp;
             byte[] longBytes = new byte[8];
             for (int i = 0; i < 8; i++) {
                 longBytes[7 - i] = (byte)(ts & 0xFF);
                 ts >>>= 8;
             }
             baos.write(longBytes);
             baos.write((byte) 0);
             
             int mLen = hostName.length();
             baos.write((byte) ((mLen >> 8) & 0xFF));
             baos.write((byte) ((mLen & 0xFF)));
             baos.write(hostName.toLowerCase().getBytes("UTF-8"));
             baos.write((byte) 0);
             
             Column c = new Column(ByteBuffer.wrap(baos.toByteArray()));
             c.setValue(message.getBytes("UTF-8"));
             c.setTimestamp(timestamp);
             c.setTtl(365 * 24 * 60 * 60);
             
             return c;
         } catch (IOException ioe) {
             return new Column();
         }
     }
 }
