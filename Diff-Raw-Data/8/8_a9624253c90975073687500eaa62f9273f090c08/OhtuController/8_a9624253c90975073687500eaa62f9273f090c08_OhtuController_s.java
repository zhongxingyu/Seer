 package ohtu.controller;
 
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.domain.Article;
 import ohtu.domain.Book;
 import ohtu.domain.Inproceedings;
 import ohtu.domain.Reference;
 import ohtu.service.BibtexService;
 import ohtu.service.ReferenceService;
 import ohtu.service.UserService;
 import ohtu.validator.ReferenceValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class OhtuController {
 
     @Autowired
     ReferenceValidator validator;
     @Autowired
     BibtexService bibtex;
     @Autowired
     UserService users;
     @Autowired
     ReferenceService references;
 
     @RequestMapping(value = "login", method = RequestMethod.GET)
     public String login() {
         return "login";                 //Palauttaa WEB-INF/jsp/login.jsp:n 
     }
 
     @RequestMapping(value = "*")
     public String redir() {
         return "redirect:list";
     }
 
     @RequestMapping(value = "bibtex")
     public String pureBibtex(Model model) {
         model.addAttribute("bibtexs", bibtex.generate(references.listAll()));
         return "purebibtex";
     }
 
     @RequestMapping(value = "addArticle", method = RequestMethod.POST)
     public String createArticle(@ModelAttribute("article") @Valid Article article, BindingResult bindingresult, Model model) {
         return add("article", article, bindingresult, model);
     }
 
     @RequestMapping(value = "addBook", method = RequestMethod.POST)
     public String createBook(@ModelAttribute("book") @Valid Book book, BindingResult bindingresult, Model model) {
         return add("book", book, bindingresult, model);
     }
 
     @RequestMapping(value = "addInproc", method = RequestMethod.POST)
     public String createInproc(@ModelAttribute("inproc") @Valid Inproceedings inproc, BindingResult bindingresult, Model model) {
         return add("inproc", inproc, bindingresult, model);
     }
 
     @RequestMapping(value = "listBibtex")
     public String createBibtex(Model model) {
         model.addAttribute("bibtexs", bibtex.generate(references.listAll()));
         return "bibtex";
     }
 
     @RequestMapping(value = "addRef", method = RequestMethod.GET)
     public String addRef(Model model) {
         addAll(model);
         return "addRef";
     }
 
     @RequestMapping(value = "list", method = RequestMethod.GET)
     public String list(Model model) {
         model.addAttribute("references", references.listAll());
         return "listAll";
     }
 
     @RequestMapping(value = "downloadBibtex", method = RequestMethod.GET)
     public ResponseEntity<byte[]> downloadAttachment() {
         String ret = "";
         for (String string : bibtex.generate(references.listAll())) {
             ret += string;
         }
         byte[] content = ret.getBytes();
         HttpHeaders headers = new HttpHeaders();
         headers.setContentLength(content.length);
        headers.set("Content-Disposition", "attachment; filename=\"list" + System.currentTimeMillis() + ".BIBTEX\"");
         headers.setContentType(MediaType.parseMediaType("text/plain"));
         return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);
     }
 
     @RequestMapping(value = "listAll", method = RequestMethod.GET, produces = "application/json")
     @ResponseBody
     public List<Reference> listAll() {
         return references.listAll();
     }
 
     private void addAll(Model model) {
         addOne(model, "book", new Book());
         addOne(model, "article", new Article());
         addOne(model, "inproc", new Inproceedings());
     }
 
     private void addOne(Model model, String key, Reference reference) {
         if (!model.containsAttribute(key)) {
             model.addAttribute(key, reference);
         }
     }
 
     private String add(String name, Reference reference, BindingResult result, Model model) {
         if (references.containsRefId(reference.getRefId())) {
             result.addError(new FieldError(name, "refId", "ID must Be Unique!"));
         }
         if (result.hasErrors()) {
             model.addAttribute(name, reference);
             addAll(model);
             return "addRef";
         }

         references.add(reference);



         return "redirect:list";
     }
 }
