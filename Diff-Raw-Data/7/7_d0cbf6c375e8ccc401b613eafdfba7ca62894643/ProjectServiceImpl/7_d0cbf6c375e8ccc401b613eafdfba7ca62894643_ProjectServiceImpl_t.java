 package org.xezz.timeregistration.service.impl;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.xezz.timeregistration.dao.CustomerDAO;
 import org.xezz.timeregistration.dao.ProjectDAO;
 import org.xezz.timeregistration.dao.TimeSpanDAO;
 import org.xezz.timeregistration.model.Customer;
 import org.xezz.timeregistration.model.Project;
 import org.xezz.timeregistration.repository.CustomerRepository;
 import org.xezz.timeregistration.repository.ProjectRepository;
 import org.xezz.timeregistration.repository.TimeSpanRepository;
 import org.xezz.timeregistration.service.ProjectService;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Xezz
  * Date: 05.05.13
  * Time: 11:14
  */
 @Service
 @Transactional
 public class ProjectServiceImpl implements ProjectService {
 
     @Autowired
     ProjectRepository repo;
     @Autowired
     CustomerRepository customerRepository;
     @Autowired
     TimeSpanRepository timeSpanRepository;
 
     @Override
     public Iterable<ProjectDAO> getAll() {
         final Iterable<Project> all = repo.findAll();
         final List<ProjectDAO> daoList = new ArrayList<ProjectDAO>();
         for (Project p : all) {
             daoList.add(new ProjectDAO(p));
         }
         return daoList;
     }
 
     @Override
     public Iterable<ProjectDAO> getByName(String name) {
         final Iterable<Project> byNameLike = repo.findByNameLike("%" + name + "%");
         final List<ProjectDAO> daoList = new ArrayList<ProjectDAO>();
         for (Project p : byNameLike) {
             daoList.add(new ProjectDAO(p));
         }
         return daoList;
     }
 
     @Override
     public Iterable<ProjectDAO> getByCustomer(CustomerDAO c) {
         if (c == null || c.getCustomerId() == null) {
             return new ArrayList<ProjectDAO>();
         }
         final Customer customer = customerRepository.findOne(c.getCustomerId());
         if (customer == null) {
             return new ArrayList<ProjectDAO>();
         }
         final Iterable<Project> byCustomer = repo.findByCustomer(customer);
         final List<ProjectDAO> daoList = new ArrayList<ProjectDAO>();
         for (Project p : byCustomer) {
             daoList.add(new ProjectDAO(p));
         }
         return daoList;
     }
 
     // FIXME: NULL CHECK
     @Override
     public ProjectDAO getByTimeFrame(TimeSpanDAO t) {
         if (t == null || t.getProjectId() == null) {
             return null;
         }
         final Project project = repo.findOne(t.getProjectId());
         return project != null ? new ProjectDAO(project) : null;
     }
 
     @Override
     public ProjectDAO getById(Long id) {
         if (id == null) {
             return null;
         }
         final Project project = repo.findOne(id);
         return project != null ? new ProjectDAO(project) : null;
     }
 
     @Transactional
     @Override
     public ProjectDAO addNew(ProjectDAO p) {
         if (p == null) {
             throw new IllegalArgumentException("NULL not allowed here");
         }
         final Project save = repo.save(new Project(p));
         if (save == null) throw new IllegalArgumentException("SAVE RETURNED NULL!");
         return new ProjectDAO(save);
     }
 
     @Transactional
     @Override
     public ProjectDAO update(ProjectDAO p) {
        if (p == null) {
            throw new IllegalArgumentException("NULL not allowed here");
        }
        final Project save = repo.save(new Project(p));
        return new ProjectDAO(save);
     }
 
     @Override
     public void delete(ProjectDAO p) {
         if (p != null) {
             repo.delete(new Project(p));
         }
     }
 }
