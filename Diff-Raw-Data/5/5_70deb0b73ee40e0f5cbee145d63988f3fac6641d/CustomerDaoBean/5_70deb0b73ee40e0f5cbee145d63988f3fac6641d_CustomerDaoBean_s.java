 package com.acme.persistence;
 
 import com.acme.domain.Address;
 import com.acme.domain.Customer;
 import com.acme.persistence.exception.*;
 import org.slf4j.Logger;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 /**
  * Author: danny
  * Date: 4/8/12
  * Time: 2:39 PM
  */
 @Stateless
 public class CustomerDaoBean implements CustomerDao {
 
     @Inject
     private Logger logger;
 
     @PersistenceContext(unitName = "arqPU")
     private EntityManager em;
 
     @Override
     public void createCustomer(Customer customer) throws CustomerCreationException {
 
         StringBuffer errMsg = new StringBuffer();
         try {
             if (findCustomerByEmail(customer.getEmail()) != null) {
                 errMsg.append("Email: " + customer.getEmail() + ".\n");
             }
         } catch (UnknownEmailException e) {
             logger.info("Email available.");
         }
 
         try {
             // username may be null
             if (customer.getUsername() != null && findCustomerByUsername(customer.getUsername()) != null) {
                 errMsg.append("Username " + customer.getUsername() + ".\n");
             }
         } catch (UnknownUsernameException e) {
             logger.info("Username available.");
         }
 
         if (errMsg.length() > 0) {
             throw new CustomerCreationException(errMsg.append("Already in use.").toString());
         }
 
         if (customer.getAddressList().size() > 1) {
             removeDuplicates(customer.getAddressList());
         }
 
         em.persist(customer);
     }
 
     @Override
     public Customer findCustomerById(Long customerId) throws UnknownCustomerIdException {
         Customer customer = em.find(Customer.class, customerId);
         if (customer == null) {
             logger.debug("No customer with id: {}", customerId);
             throw new UnknownCustomerIdException("Unknown id: " + customerId);
         }
         return customer;
     }
 
     @Override
     public Customer findCustomerByEmail(String email) throws UnknownEmailException {
         Query query = em.createNamedQuery("findByEmail");
         query.setParameter("email", email);
         try {
             return (Customer) query.getSingleResult();
         } catch (NoResultException e) {
             logger.debug("No customer with email: {}", email, e);
            throw new UnknownEmailException("Unknown email: " + email);
         }
     }
 
     @Override
     public Customer findCustomerByUsername(String username) throws UnknownUsernameException {
         Query query = em.createNamedQuery("findByUsername");
         query.setParameter("username", username);
         try {
             return (Customer) query.getSingleResult();
         } catch (NoResultException e) {
             logger.debug("No customer with username: {}", username, e);
            throw new UnknownUsernameException("Unknown username: " + username);
         }
     }
 
     @Override
     public void updateCustomer(Customer customer) throws UnknownCustomerException {
         Customer foundCustomer = em.find(Customer.class, customer.getCustomerId());
         if (foundCustomer == null) {
             throw new UnknownCustomerException();
         } else {
             if (customer.getAddressList().size() > 1) {
                 removeDuplicates(customer.getAddressList());
             }
             foundCustomer = customer;
             em.merge(foundCustomer);
         }
     }
 
     @Override
     public void deleteCustomer(Customer customer) {
         Long id = customer.getCustomerId();
         Customer foundCustomer = em.find(Customer.class, id);
         if (foundCustomer == null) {
             logger.warn("Unable to delete customer. Couldn't find any record with id {}", id);
         } else {
             logger.debug("Deleting customer with id {}", id);
             em.remove(foundCustomer);
         }
     }
 
     private void removeDuplicates(List<Address> addressList) {
         Set<Address> uniqueAddresses = new HashSet<Address>();
 
         // first add persisted addresses
         for (Address adr : addressList) {
             if (adr.getAddressId() != null && !uniqueAddresses.add(adr)) {
                 logger.info("Removed duplicate address: {}", adr);
             }
         }
 
         // add new addresses
         for (Address adr : addressList) {
             if (!uniqueAddresses.add(adr)) {
                 logger.info("Removed duplicate address: {}", adr);
             }
         }
 
         addressList.clear();
         addressList.addAll(uniqueAddresses);
     }
 }
