 package com.xone.action.back.person;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.xone.action.base.Action;
 import com.xone.action.utils.ReflectUtils;
 import com.xone.model.hibernate.entity.Person;
 import com.xone.service.app.PersonService;
 
 public class PersonAction extends Action {
 
     private static final long serialVersionUID = -5998499942790505506L;
     protected PersonService personService;
     protected Person person = new Person();
     protected List<Person> list = new ArrayList<Person>();
     
     public String dispatch() {
         return SUCCESS;
     }
     
     public String list() {
         list = personService.findAll();
         return SUCCESS;
     }
     
     public String get() {
         Long id = person.getId();
         if(id != null)
             person = personService.findById(id);
         return SUCCESS;
     }
     
     public String create() {
         personService.save(person);
         return SUCCESS;
     }
     
     public String save() {
         Long id = person.getId();
        Person p = personService.findById(id);
         if(p == null){
             p = new Person();
         }
         ReflectUtils.copyPropertiesSafely(p, person);
         personService.saveOrUpdate(p);
         return SUCCESS;
     }
     
     public String deleted() {
         Long id = person.getId();
         personService.deleteById(id);
         return SUCCESS;
     }
     
     public Person getPerson() {
         return person;
     }
     public List<Person> getList() {
         return list;
     }
     public void setPersonService(PersonService personService) {
         this.personService = personService;
     }
     
 }
