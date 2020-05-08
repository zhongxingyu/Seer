 package no.dusken.annonseweb.control;
 
 import no.dusken.annonseweb.models.Customer;
 import no.dusken.annonseweb.models.Sale;
 import no.dusken.annonseweb.service.CustomerService;
 import no.dusken.annonseweb.service.SalesService;
 import no.dusken.common.editor.BindByIdEditor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 
 import javax.validation.Valid;
 import java.beans.PropertyEditor;
 
 /**
  * Under Dusken - underdusken.no - https://github.com/dusken/
  * Magnus Kirø - magnuskiro@underdusken.no
  * 08.11.11
  */
 
 @Controller
 @RequestMapping("/sale")
 public class SalesController{
 
     @Autowired
     private SalesService salesService;
 
     @Autowired
     private CustomerService customerService;
 
     @RequestMapping()
     public String viewSaleHome(){
         return "sale/home";
     }
 
     /**
      * creates a new sale to be viewed and managed through edit
      * @param model the model to view
      * @return the place to view it
      */
     @RequestMapping("/new")
     public String newSale(Model model){
         return viewEdit(new Sale(), model);
     }
 
    @RequestMapping("/new/{customer}")
    public String newSaleCustomer(@PathVariable Customer customer, Model model) {
        return viewEdit(new Sale(customer), model);
    }

     @RequestMapping("edit/{sale}")
     public String viewEdit(@PathVariable Sale sale, Model model){
         model.addAttribute("customerList", customerService.findAll());
         model.addAttribute("sale", sale);
         return "sale/edit";
     }
 
     @RequestMapping(value="/edit", method = RequestMethod.POST)
     public String editSale(@Valid @ModelAttribute Sale sale){
         Customer customer = customerService.findOne(sale.getCustomer().getId());
         if (sale.getEditNumber() != null) {
             Sale s = salesService.findOne(Long.valueOf(sale.getEditNumber()));
             s.cloneFrom(sale);
             sale = s;
         }
         sale.setCustomer(customer);
         salesService.saveAndFlush(sale);
         customerService.saveAndFlush(customer);
         return "redirect:/annonse/sale/" + sale.getId();
     }
 
     @RequestMapping("/{sale}")
     public String viewSale(@PathVariable Sale sale, Model model){
         model.addAttribute("sale", sale);
         return "sale/sale";
     }
     
     @RequestMapping("/all")
     public String viewSalesList(Model model){
         model.addAttribute("saleList", salesService.findAll());
         return "sale/all";
     }
 
     @InitBinder
     public void initBinder(WebDataBinder binder){
         binder.registerCustomEditor(Sale.class, new BindByIdEditor(salesService));
         binder.registerCustomEditor(Customer.class, new BindByIdEditor(customerService));
     }
 }
