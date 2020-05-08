 package springapp.web.sale;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import springapp.domain.Sale;
 import springapp.service.SaleService;
 
 @Controller
 @RequestMapping("/createsale")
 @SessionAttributes("saleBean")
 public class CreateSaleController {
 
 	private SaleService saleService;
 
 	@Autowired
 	public CreateSaleController(SaleService saleService) {
 		this.saleService = saleService;
 	}
 
 	@ModelAttribute("saleBean")
 	public SaleBean createFormBean() {
 		return new SaleBean();
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public String setupForm(ModelMap model) {
		return "createsale";
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public String processSubmit(SaleBean saleBean, BindingResult result,
 			Model model, RedirectAttributes redirectAttributes) {
 
 		if (result.hasErrors()) {
 			return null;
 		}
 
 		Sale sale = saleFromSaleBean(saleBean);
 		saleService.openSale(sale);
 		
 		String message = "Form submitted successfully.  Bound " + saleBean;
 
 		redirectAttributes.addFlashAttribute("message", message);
 		return "redirect:/createsale.htm";
 	}
 
 	public Sale saleFromSaleBean(SaleBean saleBean) {
 		return new Sale(saleBean.getSalesAssistant(),saleBean.getTimeStamp(),saleBean.getTillId());
 	}
 }
