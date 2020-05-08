 /*
  * Copyright (c) 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.openinfinity.web.controller;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.ConstraintViolation;
 import javax.validation.Valid;
 import javax.validation.Validator;
 
 import org.apache.log4j.Logger;
 
 
 import org.openinfinity.core.annotation.AuditTrail;
 import org.openinfinity.core.annotation.Log;
 import org.openinfinity.core.aspect.ArgumentStrategy;
 import org.openinfinity.core.exception.AbstractCoreException;
 import org.openinfinity.core.exception.ApplicationException;
 import org.openinfinity.core.exception.BusinessViolationException;
 import org.openinfinity.core.exception.SystemException;
 import org.openinfinity.domain.entity.Catalogue;
 import org.openinfinity.domain.entity.Product;
 import org.openinfinity.domain.service.CatalogueService;
 import org.openinfinity.domain.service.ProductService;
 import org.openinfinity.web.model.ProductModel;
 import org.openinfinity.web.support.SerializerUtil;
 import org.openinfinity.web.support.ServletUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.*;
 
 /**
  * 
  * Product controller for m-v-c binding and service orchestration.
  * 
  * @author Ilkka Leinonen
  */
 @Controller
 @RequestMapping(value = "/productModel")
 public class ProductController {
 
     private static final Logger logger = Logger.getLogger(ProductController.class);
 
 
 	@Autowired
 	private ProductService productService;
 
     @Autowired
     private CatalogueService catalogueService;
 	
 	@Autowired
 	private Validator validator;
 	
 	@Autowired 
 	ApplicationContext applicationContext;
 	
 	//@ResponseStatus annotation is not working, bug in Spring?
 	//@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason="Domain service threw an exception.")
 	@Log
 	@ExceptionHandler({SystemException.class, ApplicationException.class, BusinessViolationException.class})
 	//public @ResponseBody Map<String, ? extends Object> exceptionOccurred(AbstractCoreException abstractCoreException, HttpServletResponse response) {
 	public void exceptionOccurred(AbstractCoreException abstractCoreException, HttpServletResponse response, Locale locale) {
 		ProductModel productModel = new ProductModel();
 		if (abstractCoreException.isErrorLevelExceptionMessagesIncluded()) {
 			Collection<String> localizedErrorMessages = getLocalizedExceptionMessages(abstractCoreException.getErrorLevelExceptionIds(), locale);
 			productModel.addErrorStatuses("errorLevelExceptions", localizedErrorMessages);
 		}
 		if (abstractCoreException.isWarningLevelExceptionMessagesIncluded())  {
 			Collection<String> localizedErrorMessages = getLocalizedExceptionMessages(abstractCoreException.getWarningLevelExceptionIds(), locale);
 			productModel.addErrorStatuses("warningLevelExceptions", localizedErrorMessages);
 		}
 		if (abstractCoreException.isInformativeLevelExceptionMessagesIncluded()) {
 			Collection<String> localizedErrorMessages = getLocalizedExceptionMessages(abstractCoreException.getInformativeLevelExceptionIds(), locale);
 			productModel.addErrorStatuses("informativeLevelExceptions", localizedErrorMessages);
 		}
 		//return productModel.getErrorStatuses();
 		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 		SerializerUtil.jsonSerialize(ServletUtil.getWriter(response), productModel.getErrorStatuses());
 	}
 
 	private Collection<String> getLocalizedExceptionMessages(Collection<String> localizedExceptionIds, Locale locale) {
 		Collection<String> localizedErrorMessages = new ArrayList<String>();
 		for (String uniqueId : localizedExceptionIds) {
 			String message = applicationContext.getMessage(uniqueId, null, locale);
 			localizedErrorMessages.add(message);	
 		}
 		return localizedErrorMessages;
 	}
 	
 	@Log
 	@AuditTrail(argumentStrategy=ArgumentStrategy.ALL)
 	@RequestMapping(method = RequestMethod.GET)
 	public String createNewProduct(Model model) {
 		model.addAttribute(new ProductModel());
         model.addAttribute("catalogs", catalogueService.loadAll());
         
         
         model.addAttribute("products", productService.loadAll());
 		return "product/editProduct";
 	}
 	
 	@Log
 	@AuditTrail(argumentStrategy=ArgumentStrategy.ALL) 
 	@RequestMapping(method = RequestMethod.POST)
 	public String create(@Valid @ModelAttribute ProductModel productModel) {
         String id = productService.create(productModel.getProduct());
         return "redirect:/productModel";
 	}
 
    @RequestMapping(value = "product/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") String itemId, Model model){
        productService.delete(productService.loadById(itemId));

        model.addAttribute(new ProductModel());
        return "redirect:/productModel";
    }

 	
 }
