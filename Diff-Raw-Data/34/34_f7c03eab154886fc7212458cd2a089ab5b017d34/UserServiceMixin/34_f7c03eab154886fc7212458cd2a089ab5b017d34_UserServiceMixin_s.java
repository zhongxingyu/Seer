 /*
  * Copyright (c) 2008, Muhd Kamil Mohd Baki. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.chronos.service.impl;
 
 import java.util.Collection;
 import java.util.Set;
 import org.qi4j.chronos.model.Account;
 import org.qi4j.chronos.model.Admin;
 import org.qi4j.chronos.model.ContactPerson;
 import org.qi4j.chronos.model.Customer;
 import org.qi4j.chronos.model.Login;
 import org.qi4j.chronos.model.Staff;
 import org.qi4j.chronos.model.User;
 import org.qi4j.chronos.model.associations.HasLogin;
 import org.qi4j.chronos.service.UserAuthenticationFailException;
 import org.qi4j.chronos.service.UserService;
 import org.qi4j.entity.UnitOfWork;
 import org.qi4j.entity.association.SetAssociation;
import org.qi4j.library.constraints.annotation.NotNull;
 import org.qi4j.library.validation.ValidationException;
 import org.qi4j.property.Property;
 import org.qi4j.query.Query;
 import org.qi4j.query.QueryBuilder;
 import org.qi4j.query.QueryBuilderFactory;
 import static org.qi4j.query.QueryExpressions.and;
 import static org.qi4j.query.QueryExpressions.eq;
 import static org.qi4j.query.QueryExpressions.templateFor;
 import static org.qi4j.query.QueryExpressions.variable;
 import org.qi4j.query.grammar.BooleanExpression;
 import org.qi4j.query.grammar.Conjunction;
 import org.qi4j.query.grammar.EqualsPredicate;
 import org.qi4j.query.grammar.VariableValueExpression;
 
 /**
  * TODO user encode password
  */
 public abstract class UserServiceMixin extends AbstractServiceMixin
     implements UserService
 {
     private static final long serialVersionUID = 1L;
 
     private static Conjunction ADMIN_QUERY_WHERE_CLAUSE;
 
     static
     {
         Admin admin = templateFor( Admin.class );
 
         Login userLogin = admin.login().get();
         Property<String> userPasswordProperty = userLogin.password();
 
         Property<String> userNameProperty = userLogin.name();
         VariableValueExpression<String> userName = variable( "username" );
         BooleanExpression userNameEquals = eq( userNameProperty, userName );
 
         VariableValueExpression<String> password = variable( "password" );
         EqualsPredicate<String> passwordEquals = eq( userPasswordProperty, password );
         ADMIN_QUERY_WHERE_CLAUSE = and( userNameEquals, passwordEquals );
     }
 
 
     public final void changePassword( User aUser, String anOldPassword, String aNewPassword )
         throws ValidationException
     {
         Login userLogin = aUser.login().get();
         Property<String> passwordProperty = userLogin.password();
         String userPassword = passwordProperty.get();
         if( !userPassword.equals( anOldPassword ) )
         {
             throw new ValidationException( "The old password is incorrect!" );
         }
 
         passwordProperty.set( aNewPassword );
     }
 
     public final User authenticate( Account anAccount, String aUserName, final String aPassword )
         throws UserAuthenticationFailException
     {
 
         //user attempting to login as super admin
         if( anAccount == null )
         {
             return authenticateAdminUser( aUserName, aPassword );
         }
         else
         {
             UnitOfWork unitOfWork = getUnitOfWork();
             Account referencedAccount = unitOfWork.dereference( anAccount );
 
             // is user a staff
             Set<Staff> staffs = referencedAccount.staffs();
             User user = authenticateUsersFrom( aUserName, aPassword, staffs );
             if( user != null )
             {
                 return user;
             }
 
             // is user a customer
             Set<Customer> customers = referencedAccount.customers();
             for( Customer customer : customers )
             {
                 SetAssociation<ContactPerson> contactPersons = customer.contactPersons();
 
                 user = authenticateUsersFrom( aUserName, aPassword, contactPersons );
                 if( user != null )
                 {
                     return user;
                 }
             }
 
         }
 
         throw new UserAuthenticationFailException( "Invalid password or username" );
     }
 
     private User authenticateUsersFrom( String aUserName, String aPassword, Collection<? extends User> candidates )
     {
         for( User user : candidates )
         {
             if( isUserCredentialsMatch( aUserName, aPassword, user ) )
             {
                 return user;
             }
         }
 
         return null;
     }
 
     private boolean isUserCredentialsMatch( String aUserName, String aPassword, HasLogin hasLogin )
     {
         Login userLogin = hasLogin.login().get();
         String userName = userLogin.name().get();
         String userPassword = userLogin.password().get();
 
         return userName.equals( aUserName ) && userPassword.equals( aPassword );
     }
 
     private Admin authenticateAdminUser( String aUserName, String aPassword )
         throws UserAuthenticationFailException
     {
         Query<Admin> adminQuery = createAdminQuery();
         adminQuery.setVariable( "username", aUserName );
         adminQuery.setVariable( "password", aPassword );
         Admin admin = adminQuery.find();
 
         if( admin == null )
         {
             throw new UserAuthenticationFailException( "Invalid username or password." );
         }
 
         return admin;
     }
 
     private Query<Admin> createAdminQuery()
     {
         UnitOfWork work = getUnitOfWork();
         QueryBuilderFactory builderFactory = work.queryBuilderFactory();
         QueryBuilder<Admin> adminQueryBuilder = builderFactory.newQueryBuilder( Admin.class );
         return adminQueryBuilder.where( ADMIN_QUERY_WHERE_CLAUSE ).newQuery();
     }
 
 }
