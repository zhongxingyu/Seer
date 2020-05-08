 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.evinceframework.membership.authentication;
 
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.ParameterExpression;
 import javax.persistence.criteria.Root;
 
 import com.evinceframework.jpa.query.AbstractJpaQuery;
 import com.evinceframework.membership.model.User;
 import com.evinceframework.membership.model.User_;
 
 /**
  * A query class that returns a user and their rights based on an email address.
  * 
  * @author Craig Swing
  */
 public class UserQuery extends AbstractJpaQuery<User> {
 	
 	private ParameterExpression<String> emailParameter; 
 	
 	public UserQuery() {
 		super(User.class);
 	}
 
 	@Override
 	protected void createCriteria(CriteriaBuilder builder,
 			CriteriaQuery<User> criteria, Root<User> root) {
 		
 		emailParameter = builder.parameter(String.class);
 		
 		criteria.where(
 			builder.equal(builder.lower(root.get(User_.emailAddress)), emailParameter));
 		
		root.join(User_.rights);
		root.fetch(User_.rights);
 	}
 
 	/**
 	 * Query for a user based on an email address. The user's rights are also fetched as part of
 	 * the query.
 	 * 
 	 * @param emailAddress
 	 * @return the user.
 	 */
 	public User findByEmailAddress(String emailAddress) {
 		TypedQuery<User> q = createQuery();
 		q.setParameter(emailParameter, emailAddress.toLowerCase());
 		return executeSingleResult(q, false);
 	}
 
 }
