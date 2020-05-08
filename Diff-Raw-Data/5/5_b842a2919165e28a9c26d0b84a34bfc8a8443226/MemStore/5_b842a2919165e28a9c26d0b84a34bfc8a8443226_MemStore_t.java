 /*******************************************************************************
  * Copyright (c) 2013, Minor Gordon
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in
  *       the documentation and/or other materials provided with the
  *       distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGE.
  ******************************************************************************/
 
 package org.thryft.web.server.store;
 
 import org.apache.thrift.TBase;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Table;
 import com.google.common.collect.TreeBasedTable;
 
 public final class MemStore<ModelT extends TBase<?, ?>> extends Store<ModelT> {
     public MemStore(final Class<ModelT> modelClass) {
         super(modelClass);
     }
 
     @Override
     protected synchronized boolean _deleteModelById(final String modelId,
             final String username) {
         return models.remove(username, modelId) != null;
     }
 
     @Override
     protected synchronized void _deleteModels(final String username) {
        final ImmutableSet<String> columnKeys = ImmutableSet.copyOf(models.row(
                username).keySet());
        for (final String columnKey : columnKeys) {
             models.remove(username, columnKey);
         }
     }
 
     @Override
     protected synchronized ModelT _getModelById(final String modelId,
             final String username) throws NoSuchModelException {
         final ModelT model = models.get(username, modelId);
         if (model != null) {
             return model;
         } else {
             throw new NoSuchModelException(modelId);
         }
     }
 
     @Override
     protected synchronized int _getModelCount(final String username) {
         return models.row(username).size();
     }
 
     @Override
     protected synchronized ImmutableSet<String> _getModelIds(
             final String username) {
         return ImmutableSet.copyOf(models.row(username).keySet());
     }
 
     @Override
     protected synchronized ImmutableMap<String, ModelT> _getModels(
             final String username) {
         return ImmutableMap.copyOf(models.row(username));
     }
 
     @Override
     protected synchronized ImmutableMap<String, ModelT> _getModelsByIds(
             final ImmutableSet<String> modelIds, final String username)
             throws NoSuchModelException {
         final ImmutableMap.Builder<String, ModelT> models = ImmutableMap
                 .builder();
         for (final String modelId : modelIds) {
             models.put(modelId, getModelById(modelId, username));
         }
         return models.build();
     }
 
     @Override
     protected ImmutableSet<String> _getUsernames() {
         return ImmutableSet.copyOf(models.rowKeySet());
     }
 
     @Override
     protected synchronized boolean _headModelById(final String modelId,
             final String username) {
         return models.get(username, modelId) != null;
     }
 
     @Override
     protected synchronized void _putModel(final ModelT model,
             final String modelId, final String username) {
         models.put(username, modelId, model);
     }
 
     @Override
     protected synchronized void _putModels(
             final ImmutableMap<String, ModelT> models, final String username) {
         for (final ImmutableMap.Entry<String, ModelT> model : models.entrySet()) {
             this.models.put(username, model.getKey(), model.getValue());
         }
     }
 
     private final Table<String, String, ModelT> models = TreeBasedTable
             .create(); // Username, model ID, model
 }
