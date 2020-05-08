 package no.dusken.annonseweb.control;
 
 import no.dusken.annonseweb.models.ContactNote;
 import no.dusken.annonseweb.models.ContactPerson;
 import no.dusken.annonseweb.service.ContactNoteService;
 import no.dusken.annonseweb.service.ContactPersonService;
 import no.dusken.annonseweb.service.CustomerService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.validation.Valid;
 import java.util.ArrayList;
 import java.util.List;
 
 
 @Controller
 @RequestMapping("/contact")
 public class ContactController{
 
     @Autowired
     private ContactPersonService contactPersonService;
 
     @Autowired
     private ContactNoteService contactNoteService;
 
     @Autowired
     private CustomerService customerService;
 
     @RequestMapping("")
     public String viewContactsHome(){
         return "contact/home";
     }
 
     @RequestMapping("/note/all")
     public String allContacts(Model model){
         List<ContactNote> contactNotes = contactNoteService.findAll();
         model.addAttribute("contactPersonList", contactNotes);
         return "contact/note/all";
     }
 
     @RequestMapping("/person/active")
     public String allActive(Model model){
         List<ContactPerson> contactPersonList = contactPersonService.findAll();
         for(ContactPerson contactPerson: contactPersonList){
             if(!contactPerson.getActive()) contactPersonList.remove(contactPerson);
         }
         model.addAttribute("contactPersonList", contactPersonList);
         return "contact/person/all";
     }
 
     @RequestMapping("/person/all")
     public String all(Model model){
         List<ContactPerson> contactPersonList = contactPersonService.findAll();
         model.addAttribute("contactPersonList", contactPersonList);
         return "contact/person/all";
     }
 
     @RequestMapping(value="/note/{Id}")
     public String viewContactNote(@PathVariable Long Id, Model model){
         model.addAttribute("contact", contactNoteService.findOne(Id));
         return "contact/note/note";
     }
 
     @RequestMapping("/note/new")
     public String newContact(){
         return "contact/note/new";
     }
 
     @RequestMapping("/note/new/{Id}")
     public String newContactWithCustomerID(@PathVariable Long Id,Model model){
         model.addAttribute("customer", customerService.findOne(Id));
         return "contact/note/new";
     }
 
     @RequestMapping(value="/note/add", method = RequestMethod.POST)
     public String addContactNote(@Valid @ModelAttribute("contactNote")ContactNote contactNote){
         contactNoteService.save(contactNote);
         return "contact/note";
     }
 
     @RequestMapping(value="/person/{Id}")
     public String viewContactPerson(@PathVariable Long Id, Model model){
         ContactPerson contactPerson = contactPersonService.findOne(Id);
         model.addAttribute("contact", contactPerson);
         return "contact/person/person";
     }
 
     @RequestMapping("/person/new")
     public String newContactPerson(){
         return "contact/person/new";
     }
 
     @RequestMapping(value="/person/add", method = RequestMethod.POST)
     public String addContactPerson(@Valid @ModelAttribute("newContactPerson")ContactPerson contactPerson){
         contactPersonService.save(contactPerson);
         return "contact/person/person";
     }
 
     @RequestMapping("/person/emailList")
    public String viewEmailsForCustomersContactPersons(Model model){
         List<String> emailList = new ArrayList<String>();
         for(ContactPerson contactPerson: contactPersonService.findAll()){
             emailList.add(contactPerson.getEmail());
         }
         model.addAttribute("emailList", emailList);
        return "contact/person/emailList";
     }
 
 }
