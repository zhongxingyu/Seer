 package com.jboss.forge.scaffold.vraptor.app.view;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import com.jboss.forge.scaffold.vraptor.app.model.Customer;
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 @Resource
 public class CustomerController {
 
     private EntityManager entityManager;
     private final Result result;
 
     public CustomerController(EntityManager entityManager, Result result) {
         this.entityManager = entityManager;
         this.result = result;
     }
 
     @Get
     public void search() {
         result.include("customers", entityManager.createQuery("from Customer c").getResultList());
     }
 
     @Post
     public void search(Customer customer) {
         CriteriaBuilder builder = entityManager.getCriteriaBuilder();
         CriteriaQuery<Customer> query = builder.createQuery(Customer.class);
         Root<Customer> from = query.from(Customer.class);
         CriteriaQuery<Customer> select = query.select(from);
         
         if (customer.getFirstName() != null && !"".equals(customer.getFirstName())) {
             query.where(builder.like(from.<String>get("firstName"), "%" + customer.getFirstName() + "%"));
         }
 
         if (customer.getLastName() != null && !"".equals(customer.getLastName())) {
             query.where(builder.like(from.<String>get("lastName"), "%" + customer.getLastName() + "%"));
         }
         
         result.include("customers", entityManager.createQuery(select).getResultList());
     }
 
     public void create() {
     }
 
     @Path("/customer/view/{customer.id}")
     @Get
     public void view(Customer customer) {
         result.include("customer", entityManager.find(Customer.class, customer.getId()));
     }
 
     @Path("/customer/edit/{customer.id}")
     @Get
     public void edit(Customer customer) {
         result.include("customer", entityManager.find(Customer.class, customer.getId()));
     }
 
     public void save(Customer customer) {

         if (customer.getId() == null) {
             entityManager.persist(customer);
         } else {
             entityManager.merge(customer);
         }

         result.redirectTo(CustomerController.class).search();
     }
 }
