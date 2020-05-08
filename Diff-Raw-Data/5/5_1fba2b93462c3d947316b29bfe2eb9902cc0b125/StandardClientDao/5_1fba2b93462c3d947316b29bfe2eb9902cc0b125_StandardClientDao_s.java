 /*
  * Copyright (c) 2005-2008 Grameen Foundation USA
  * All rights reserved.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  * 
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package org.mifos.client.repository;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.joda.time.LocalDate;
 import org.mifos.client.domain.Client;
 import org.mifos.core.MifosException;
 import org.springframework.transaction.annotation.Transactional;
 
 public class StandardClientDao implements ClientDao {
 
 	@PersistenceContext
 	private EntityManager entityManager;
 
 	@Override
 	@Transactional
 	public Client create(String firstName, String lastName, LocalDate dateOfBirth)
 			throws MifosException {
 		Client client = new Client(null, firstName, lastName, dateOfBirth);
 		entityManager.persist(client);
 		return entityManager.find(Client.class, client.getId());
 	}
 
 	@Override
 	@Transactional(readOnly=true)
 	public Client get(Integer id) {
 		return entityManager.find(Client.class, id);
 	}
 	
 	@Override
 	@Transactional(readOnly=true)
 	public List<Client> getAll() {
 		Query query = entityManager.createQuery("from Client");
 		return query.getResultList();
 	}
 
 	@Override
 	public List<Client> findClients(String clientName) {
		// TODO Auto-generated method stub
		return null;
 	}
 	
 }
