 package com.test.hibernate.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.FileCopyUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.test.hibernate.form.Address;
 import com.test.hibernate.form.Category;
 import com.test.hibernate.form.Product;
 import com.test.hibernate.service.AddressService;
 import com.test.hibernate.service.CategoryService;
 import com.test.hibernate.service.ProductService;
 import com.test.hibernate.xmlForm.Products;
 import com.test.hibernate.xmlService.MarshallerService;
 import com.test.hibernate.xmlService.TransformationService;
 
 @Controller
 public class ProductController {
 	
 	private static final Logger LOGGER_INFO= Logger.getLogger(ProductController.class);
 	private static final double MAX_RECORDS_PER_PAGE=5.0;
 	private static final String PRODUCT_LIST="productList";
 	private static final String USERMODE="usermode";
 	private static final String MAX="max";
 	private static final String PRODUCT="product";
 	private static final String PAGE_NO="pageNo";
 	
 	
 	private static int pageNo;
 	private static Long totalRecords;
 	
 	@Autowired
 	private ProductService productService;
 	@Autowired
 	private AddressService addressService;
 	
 	@Autowired
 	private CategoryService categoryService;
 	
 	@Autowired
 	private MarshallerService marshallerService;
 	
 	@Autowired
 	private TransformationService transformationService;
 	
 	public static int getPageNo() {
 		return pageNo;
 	}
 	public static void setPageNo() {
 		ProductController.pageNo = (int) Math.ceil(totalRecords/MAX_RECORDS_PER_PAGE);
 	}
 	public static Long getTotalRecords() {
 		return totalRecords;
 	}
 	public static void setTotalRecords(Long totalRecords) {
 		ProductController.totalRecords = totalRecords;
 	}
 	
 	@RequestMapping(value="/")
 	public String home(){
 		
 		setTotalRecords(productService.getCount());
 		setPageNo();
 		return "Main";
 		
 	}
 	
 
 	@RequestMapping(value="{usermode}/feed", method = RequestMethod.POST)
 	public String addRecord(@ModelAttribute ("product")Product product,BindingResult rs,@RequestParam("cityName") String cityName,
 			@RequestParam("category") String categoryName){
 		 
 		
 		
 		Address address=new Address();
 		Category category=new Category();
 		if(addressService.getAddressByCityName(cityName) != null)
 			{
 			address.setId(addressService.getAddressByCityName(cityName).getId());
 			address.setCname(addressService.getAddressByCityName(cityName).getCname());
 			}
 			else
 		{
 			address.setCname(cityName);
 			addressService.createAddress(address);
 		}
 		
 		if(categoryService.getCategoryByName(categoryName) != null)
 		{
 			category=categoryService.getCategoryByName(categoryName);
 		}
 		
 	else
 	{
 		category.setCategory(categoryName);
 		categoryService.createCategory(category);
 	}
 		product.setAddress(address);
 		product.setCategory(category);
 		productService.createRecord(product);
 		return "redirect:/";
 		
 	}
 	
 	@RequestMapping(value="{usermode}/products/order" , method= RequestMethod.POST) 
 	public String getOrder(@RequestParam("choice") String choice,@RequestParam("start") int startPage,@RequestParam(MAX) int max, Map <String , Object> map){
 		
 	
 		map.put(PAGE_NO,pageNo);
 		map.put(PRODUCT, new Product());
 		map.put(MAX,getTotalRecords().intValue());
 		map.put(PRODUCT_LIST,productService.orderBy(choice,startPage,max));
 		return "products";
 		
 	}
 	@RequestMapping(value="{usermode}/products/page/{page}/order" , method= RequestMethod.POST) 
 	public String getOrderPaginate(@PathVariable(USERMODE)String usermode,@PathVariable("page")int startPageUrl,@RequestParam("choice") String choice,@RequestParam("start") int startPage,@RequestParam(MAX) int max, Map <String , Object> map){
 		
 		map.put("pageNo",pageNo);
 		map.put("product", new Product());
 		map.put(MAX,max);
 		map.put("startPage", startPageUrl);
 		map.put(USERMODE,usermode);
 		map.put(PRODUCT_LIST,productService.orderBy(choice,startPage,max));
 		return "paginate";
 		
 	}
 	
 	@RequestMapping(value="fetch")
 	public String getProductById(@RequestParam("id")String idS , ModelMap map){
 		
 		Product result=productService.getProductById(Integer.parseInt(idS));
 		map.addAttribute("Product", result);
 		return "productPage";
 				
 	}
 	
 	@RequestMapping(value="{usermode}/products/page/{startPage}")
 	public String pagiNation(@PathVariable(USERMODE)String usermode,@PathVariable("startPage") int startPage ,Map <String , Object> map){
 		
 		map.put(PAGE_NO,pageNo);
 		map.put("startPage",startPage);
 		map.put(USERMODE,usermode);
 		map.put(PRODUCT, new Product());
 		map.put(PRODUCT_LIST,productService.pagiNate(startPage));
 		
 		return "paginate";
 	}
 
 	@RequestMapping(value="{usermode}/insertLocation")
 	public String addLocationDB(@RequestParam("location") String cityName ){
 
 		Address address = new Address();
 		if(addressService.getAddressByCityName(cityName) != null)
 			{
 			return "redirect:/";
 			}
 			
 		else
 		{
 			address.setCname(cityName);
 			addressService.createAddress(address);
 		}
 		return "redirect:/";
 
 	}
 	
 	@RequestMapping(value="admin/GetXml")
 	@ResponseBody
 	public byte[] createXmlReport(HttpSession session,HttpServletResponse response)  throws IOException, TransformerException{
 		
 		@SuppressWarnings("unchecked")
 		List<Product> productList= (List<Product>) session.getAttribute("object_Xml");
 		Products xmlProducts=new Products();
 		xmlProducts.setProducts(productList);
 		String result=marshallerService.doMarshalling(xmlProducts);
 		try {
 			transformationService.doTransform(result);
 		} catch (TransformerConfigurationException e) {
 			// TODO Auto-generated catch block
 			LOGGER_INFO.error(e.toString());
 		}
 		LOGGER_INFO.info("You are here !"+productList.size());  
		File xmlFile= new File("./GeneratedXML.xml");
 		byte bytes[]=FileCopyUtils.copyToByteArray(xmlFile);
 	    response.setHeader("Content-Disposition", "attachment; filename=\"" + xmlFile.getName() + "\"");
 		response.setContentLength(bytes.length);
 		response.setContentType("application/xml");
 		return bytes;
 	}
 	
 }
