 /**
  * Copyright 2007 The Apache Software Foundation
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
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
 package com.intel.hadoop.hbase.dot;
 
 /**
  * All constants for DOT
  *
  */
 public class DotConstants {
   // Table related configuration items and its default values
   public static final String HBASE_TABLE_IS_A_DOT_TABLE = "hbase.dot.enable";
   public static final byte[] HBASE_TABLE_IS_A_DOT_TABLE_BYTES = HBASE_TABLE_IS_A_DOT_TABLE.getBytes();
   public static final boolean HBASE_TABLE_IS_A_DOT_TABLE_DEFAULT = true;
   public static final byte[] HBASE_TABLE_IS_A_DOT_TABLE_DEFAULT_BYTES = String.valueOf(HBASE_TABLE_IS_A_DOT_TABLE_DEFAULT).getBytes();
 
   public static final String HBASE_DOT_TABLE_TYPE = "hbase.dot.type";
   public static final String HBASE_DOT_TABLE_TYPE_DEFAULT = "ANALYTICAL";
 
   // Column family related configuration items and its default values
   public static final String HBASE_DOT_COLUMNFAMILY_DOC_ELEMENT = "hbase.dot.columnfamily.doc.element";
   public static final String HBASE_DOT_COLUMNFAMILY_DOC_SCHEMA_PREFIX = "hbase.dot.columnfamily.doc.schema.";
   public static final String HBASE_DOT_COLUMNFAMILY_DOC_SERIALIZER_CLASS = "hbase.dot.columnfamily.doc.serializer.class";
  public static final String HBASE_DOT_COLUMNFAMILY_DOC_SERIALIZER_CLASS_DEFAULT = "com.intel.hadoop.hbase.dot.doc.serializer.AvroDoc";
 
   public static final byte HBASE_DOT_DOC_AND_FIELD_SEPERATOR = '.';
   public static final String HBASE_DOT_COLUMNFAMILY_AND_DOC_SEPERATOR = ":";
   public static final String HBASE_DOR_ROW_AND_COLUMNFAMILY_SEPERATOR = "/";
 
   // encode null value into certain format
   public static final String SERIALIZATION_NUL_FORMAT = "serialization.null.format";
   public static final String DEFAULT_SERIALIZATION_NUL_FORMAT = "\\N";
 
   public static final int INT_SIZE = 4;
   public static final int CHAR_SIZE = 2;
 
 }
