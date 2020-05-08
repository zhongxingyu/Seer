 package ohtu.miniprojekti.controller;
 
 import java.util.List;
 import javax.validation.Valid;
 import ohtu.miniprojekti.domain.Viite;
 import ohtu.miniprojekti.domain.Viite.ViiteType;
 import ohtu.miniprojekti.formvalidation.ArticleValidationObject;
 import ohtu.miniprojekti.formvalidation.BookValidationObject;
 import ohtu.miniprojekti.formvalidation.InproceedingsValidationObject;
 import ohtu.miniprojekti.service.ViiteService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class MainController {
 
     @Autowired
     private ViiteService viiteService;
 
     @RequestMapping(value = "*")
     public String any() {
         return "redirect:home";
     }
 
     @RequestMapping(value = "home", method = RequestMethod.GET)
     public String home(Model model) {
         model.addAttribute("viitecount", viiteService.findAll().size());
         return "home";
     }
 
     @RequestMapping(value = "artikkeli", method = RequestMethod.GET)
     public String getArtikkeli(Model model) {
         model.addAttribute("viite", new ArticleValidationObject());
         return "artikkeli";
     }
 
     @RequestMapping(value = "edit/{id}", method = RequestMethod.GET)
     public String getEdit(Model model, @PathVariable Long id) {
         Viite viite = viiteService.findById(id);
         if (viite == null) {
             return "redirect:/home";
         }
 
         if (viite.getViiteType() == ViiteType.ARTICLE) {
             model.addAttribute("viite", new ArticleValidationObject(viite));
             return "artikkeli";
         } else if (viite.getViiteType() == ViiteType.BOOK) {
             model.addAttribute("viite", new BookValidationObject(viite));
             return "kirja";
         } else if (viite.getViiteType() == ViiteType.INPROCEEDINGS) {
             model.addAttribute("viite", new InproceedingsValidationObject(viite));
             return "inproceedings";
         };
         return "redirect:/home";
     }
 
     @RequestMapping(value = "artikkeli", method = RequestMethod.POST)
     public String postArtikkeli(@Valid @ModelAttribute("viite") ArticleValidationObject viiteValidationObj, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "artikkeli";
         }
 
         Viite viite = viiteService.getViite(viiteValidationObj);
         viite.updateFromValidationObj(viiteValidationObj);
 
         if (viite.getRefId() == null || !viiteService.refIdValid(viite.getRefId())) {
             List<String> authors = viite.getAuthors();
             String year = viite.getPublicationYear();
             viite.setRefId(viiteService.generateRefId(authors, year));
         }
 
         viite = viiteService.save(viite);
 
         return "redirect:home";
     }
 
     @RequestMapping(value = "kirja", method = RequestMethod.GET)
     public String getKirja(Model model) {
         model.addAttribute("viite", new BookValidationObject());
         return "kirja";
     }
 
     @RequestMapping(value = "kirja", method = RequestMethod.POST)
     public String postKirja(@Valid @ModelAttribute("viite") BookValidationObject viiteValidationObj, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "kirja";
         }
 
         Viite viite = viiteService.getViite(viiteValidationObj);
         viite.updateFromValidationObj(viiteValidationObj);
 
         if (viite.getRefId() == null || !viiteService.refIdValid(viite.getRefId())) {
             List<String> authors = viite.getAuthors();
             String year = viite.getPublicationYear();
             viite.setRefId(viiteService.generateRefId(authors, year));
         }
 
         viite = viiteService.save(viite);
 
         return "redirect:home";
     }
 
     @RequestMapping(value = "inproceedings", method = RequestMethod.GET)
     public String getInproceedings(Model model) {
         model.addAttribute("viite", new InproceedingsValidationObject());
         return "inproceedings";
     }
 
     @RequestMapping(value = "inproceedings", method = RequestMethod.POST)
     public String postInproceedings(@Valid @ModelAttribute("viite") InproceedingsValidationObject viiteValidationObj, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "inproceedings";
         }
 
         Viite viite = viiteService.getViite(viiteValidationObj);
         viite.updateFromValidationObj(viiteValidationObj);
 
         if (viite.getRefId() == null || !viiteService.refIdValid(viite.getRefId())) {
             List<String> authors = viite.getAuthors();
             String year = viite.getPublicationYear();
             viite.setRefId(viiteService.generateRefId(authors, year));
         }
 
         viite = viiteService.save(viite);
 
         return "redirect:home";
     }
 
     @RequestMapping(value = "listing/all", method = RequestMethod.GET)
     public String getListaaKaikki(Model model) {
         model.addAttribute("viitteet", viiteService.findAll());
        model.addAttribute("selectedAuthor", "all");
         return "listaus";
     }
 
     @RequestMapping(value = "listing/{author}", method = RequestMethod.GET)
     public String getListaaAuthor(Model model, @PathVariable String author) {
         model.addAttribute("viitteet", viiteService.findAllWithAuthor(author));
        model.addAttribute("selectedAuthor", author);
         return "listaus";
     }
 
     
     
    @RequestMapping(value = "bibtex/all", method = RequestMethod.GET)
     public String getBibtex(Model model) {
         model.addAttribute("viitteet", viiteService.findAll());
         return "bibtex";
     }
     
     
     @RequestMapping(value = "bibtex/{author}", method = RequestMethod.GET)
     public String getBibtexAuthor(Model model,@PathVariable String author) {
         model.addAttribute("viitteet", viiteService.findAllWithAuthor(author));
         return "bibtex";
     }
     
 }
