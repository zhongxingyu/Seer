 /**
  *        Licensed to the Apache Software Foundation (ASF) under one
  *        or more contributor license agreements.  See the NOTICE file
  *        distributed with this work for additional information
  *        regarding copyright ownership.  The ASF licenses this file
  *        to you under the Apache License, Version 2.0 (the
  *        "License"); you may not use this file except in compliance
  *        with the License.  You may obtain a copy of the License at
  *
  *          http://www.apache.org/licenses/LICENSE-2.0
  *
  *        Unless required by applicable law or agreed to in writing,
  *        software distributed under the License is distributed on an
  *        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *        KIND, either express or implied.  See the License for the
  *        specific language governing permissions and limitations
  *        under the License.
  *
  */
 package com.intelligentsia.dowsers.entity.store;
 
 import java.util.Collection;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Sets;
 import com.intelligentsia.dowsers.entity.meta.MetaEntity;
 import com.intelligentsia.dowsers.entity.reference.Reference;
 
 /**
  * MetaEntityStoreSupport.
  * 
  * @author <a href="mailto:jguibert@intelligents-ia.com" >Jerome Guibert</a>
  * 
  */
 public class MetaEntityStoreSupport implements MetaEntityStore {
 
 	/**
 	 * Underlying {@link EntityStore}.
 	 */
 	private final EntityStore entityStore;
 
 	/**
 	 * Build a new instance of MetaEntityStoreSupport.java.
 	 * 
 	 * @param entityStore
 	 * @throws NullPointerException
 	 *             if entityStore is null
 	 */
 	public MetaEntityStoreSupport(final EntityStore entityStore) throws NullPointerException {
 		super();
 		this.entityStore = Preconditions.checkNotNull(entityStore);
 	}
 
 	@Override
 	public Collection<MetaEntity> find(final Reference reference) throws NullPointerException {
 		final Collection<MetaEntity> result = Sets.newLinkedHashSet();
		// we are looking for MetaEntity with attribute 'name' equals to
		// EntityClassName in reference parameter.
		Reference target = new Reference(MetaEntity.class, "name", Preconditions.checkNotNull(reference).getEntityClassName());
 		try {
			for (Reference ref : entityStore.find(target)) {
				result.add(entityStore.find(MetaEntity.class, ref));
			}
 		} catch (final EntityNotFoundException entityNotFoundException) {
 			// oups
 		}
 		return result;
 	}
 
 	@Override
 	public void store(final MetaEntity entity) throws NullPointerException, ConcurrencyException, IllegalArgumentException {
 		entityStore.store(entity);
 	}
 
 	@Override
 	public void remove(final MetaEntity entity) throws NullPointerException, IllegalArgumentException {
 		entityStore.remove(entity);
 	}
 
 	@Override
 	public void remove(final Reference reference) throws NullPointerException, IllegalArgumentException {
 		Preconditions.checkArgument(MetaEntity.class.getSimpleName().equals(Preconditions.checkNotNull(reference).getEntityClassName()));
 		entityStore.remove(reference);
 	}
 
 }
