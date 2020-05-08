 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.hbase;
 
 import javax.annotation.Nonnull;
 
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.Put;
 
 import com.google.common.base.Preconditions;
 
 /**
 * Convenience class for anything that wants to implements {@link HBaseStrategy} but only cares about either encoding or decoding.
  *
 * the {@link HBaseStrategy#key(Object)} method must always be implemented.
  */
 public abstract class AbstractHBaseStrategy<T> implements HBaseStrategy<T>
 {
     @Override
     public Put encode(@Nonnull final T obj) throws IllegalArgumentException
     {
         Preconditions.checkNotNull(obj, "encode does not accept null values!");
         throw new UnsupportedOperationException("encode is not implemented.");
     }
 
     @Override
     public T decode(@Nonnull final Get row) throws IllegalArgumentException
     {
         Preconditions.checkNotNull(row, "decode does not accept null values!");
         throw new UnsupportedOperationException("decode is not implemented.");
     }
 }
