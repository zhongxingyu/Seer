 package org.xezz.timeregistration.service;
 
 import org.xezz.timeregistration.dao.CoworkerDAO;
 import org.xezz.timeregistration.dao.CustomerDAO;
 import org.xezz.timeregistration.dao.ProjectDAO;
 import org.xezz.timeregistration.dao.TimeSpanDAO;
 
 /**
  * User: Xezz
  * Date: 29.04.13
  * Time: 14:36
  */
 public interface CustomerService {
 
     /**
      * Receive all Customers
      *
      * @return List of all Customers
      */
 
     Iterable<CustomerDAO> customersAll();
 
     /**
      * Get a Customer by its id
      *
      * @param id Long the id of a given Customer
      * @return Customer that has the id
      */
     CustomerDAO customerById(Long id);
 
     /**
      * Persist a new Customer
      *
      * @param c Customer to persist
      * @return Persisted Customer
      */
     CustomerDAO addNewCustomer(CustomerDAO c);
 
     /**
      * Update an existing Customer
      *
      * @param c Customer that has been updated
      * @return Customer after persisting
      */
     CustomerDAO updateCustomer(CustomerDAO c);
 
     /**
      * Receive Customers by their name
      *
      * @param name String the name of the Customer
      * @return List of Customers that matches a give name
      */
     Iterable<CustomerDAO> customerByName(String name);
 
     /**
      * Receive all Customers where a part of its name matches the given name
      *
      * @param name String part of the name to match
      * @return List of Customers that match a name partially
      */
     Iterable<CustomerDAO> customerByNameMatch(String name);
 
     /**
      * Get the Customer of a Project
      *
      * @param p Project to get a Customer of
      * @return Customer that owns the Project
      */
     CustomerDAO customerByProject(ProjectDAO p);
 
     /**
      * Get a Customer that is associated to a timeframe
      *
      * @param t TimeSpan of concern
      * @return Customer that gets charged for the timeframe
      */
    CustomerDAO customerByTimeFrame(TimeSpanDAO t);
 
     /**
      * Get all Customers the given Coworker worked for
      *
      * @param c Coworker in concern
      * @return List of Customers the given Coworker was involved with
      */
    Iterable<CustomerDAO> customerByCoworker(CoworkerDAO c);
 
     /**
      * Delete an existing Customer
      *
      * @param customerDAO Customer to delete
      */
     void deleteCustomer(CustomerDAO customerDAO);
 }
