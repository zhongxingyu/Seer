 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.evidence.entity;
 
 import java.io.Serializable;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 import lombok.ToString;
 
 /**
  * @author Michal Bocek
  * @since 1.0.0
  */
 @Entity
 @ToString
 @NoArgsConstructor
 @Table(name = "KINDERGARTEN")
 @NamedQueries(value = { 
 		@NamedQuery(name = Kindergarten.QUERY_NAME_FIND_ALL_BY_DELETED_FLAG, query = Kindergarten.QUERY_FIND_ALL_BY_DELETED_FLAG) 
 	})
 public class Kindergarten implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	public static final String QUERY_NAME_FIND_ALL_BY_DELETED_FLAG = "Kindergarten.finaAllByDeletedFlag";   
 	public static final String QUERY_FIND_ALL_BY_DELETED_FLAG = "SELECT k FROM Kindergarten k WHERE k.deleted = :deleted";   
 
 	@Id	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Getter
 	private Long id;
 
 	@Getter	@Setter
 	@Column(name = "NAME", length = 200, nullable = false)
 	private String name;
 	
 	@Getter @Setter
 	@OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
 	@JoinColumn(name = "CONTACT_ID")
 	private Contact contact;
 	
 	@Getter	@Setter
 	@Column(name = "DELETED", nullable = false)
	private Boolean deleted = false;
 
 	public Kindergarten(final Long id) {
 		this.id = id;
 	}
 }
