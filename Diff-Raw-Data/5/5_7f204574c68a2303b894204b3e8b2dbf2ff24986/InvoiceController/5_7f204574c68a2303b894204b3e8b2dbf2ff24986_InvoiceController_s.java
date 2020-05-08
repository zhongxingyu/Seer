 package kabbadi.controller;
 
 import kabbadi.domain.Invoice;
 import kabbadi.service.InvoiceService;
 import kabbadi.spring.util.NullSafeDatePropertyEditor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Date;
 import java.util.List;
 
 @Controller
public class InvoiceController {
 
     private final InvoiceService invoiceService;
 
     @Autowired
     public InvoiceController(InvoiceService invoiceService) {
         this.invoiceService = invoiceService;
     }
 
     @InitBinder
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         binder.registerCustomEditor(Date.class, new NullSafeDatePropertyEditor());
     }
 
     @RequestMapping(value = "invoice/save", method = RequestMethod.POST)
     public ModelAndView add(@ModelAttribute Invoice invoice) {
         if (invoice.valid()) {
             invoiceService.saveOrUpdate(invoice);
             return new ModelAndView(new RedirectView("/invoice/list", true));
         }
         return new ModelAndView(new RedirectView("/invoice/create", true));
     }
 
     @RequestMapping(value = "invoice/create", method = RequestMethod.GET)
     public ModelAndView create() {
         return new ModelAndView("invoice/edit", "invoice", new Invoice());
     }
 
     @RequestMapping(value = "invoice/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable Integer id) {
         return new ModelAndView("invoice/edit", "invoice", invoiceService.get(id));
     }
 
     @RequestMapping(value = "invoice/list", method = RequestMethod.GET)
     public ModelAndView list() {
         ModelAndView modelAndView = new ModelAndView("invoice/list");
         List<Invoice> invoices = invoiceService.list();
         modelAndView.addObject("invoices", invoices);
         return modelAndView;
     }
 
     @RequestMapping(value = "invoice/{id}", method = RequestMethod.GET)
     public ModelAndView viewDetails(@PathVariable("id") Integer id) {
         ModelAndView modelAndView = new ModelAndView("invoice/view");
         modelAndView.addObject("invoice", invoiceService.get(id));
         return modelAndView;
     }
 
 }
