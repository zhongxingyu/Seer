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
 
 import javax.persistence.AttributeOverride;
 import javax.persistence.AttributeOverrides;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import lombok.Getter;
 import lombok.Setter;
 import lombok.ToString;
 
 /**
  * @author Michal Bocek
  * @since 1.0.0
  */
 @Entity
 @ToString
 @Table(name="CONTACT")
 public class Contact implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Getter
 	private Long id;
 
 	@Getter @Setter
 	@Embedded
 	private EmailAddress email;
 	
 	@Getter @Setter
 	@Embedded
 	@AttributeOverrides({ 
 		@AttributeOverride(name = "countryCode", column = @Column(name = "MOBILE_COUNTRY_CODE", length = 5)),
		@AttributeOverride(name = "phoneNumber", column = @Column(name = "MOBILE_PHONE_NUMBER", length = 50)) })	
 	private PhoneNumber mobilePhone;
 	
 	@Getter @Setter
 	@Embedded
 	@AttributeOverrides({ 
 		@AttributeOverride(name = "countryCode", column = @Column(name = "LNDL_COUNTRY_CODE", length = 5)),
		@AttributeOverride(name = "phoneNumber", column = @Column(name = "LNDL_PHONE_NUMBER", length = 50)) })	
 	private PhoneNumber landLine;
 	
 	@Getter @Setter
 	@OneToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "ADDRESS_ID")
 	private Address address;
 }
