 package org.yarquen.web.account;
 
 import java.beans.PropertyEditorSupport;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.annotation.Resource;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.yarquen.account.Account;
 import org.yarquen.account.AccountRepository;
 import org.yarquen.account.AccountService;
 import org.yarquen.category.CategoryBranch;
 import org.yarquen.category.CategoryService;
 import org.yarquen.validation.BeanValidationException;
 import org.yarquen.web.enricher.CategoryTreeBuilder;
 
 @Controller
 @RequestMapping("/account")
 public class AccountController {
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(AccountController.class);
 
 	@Resource
 	private AccountService accountService;
 	@Resource
 	private AccountRepository accountRepository;
 	@Resource
 	private CategoryTreeBuilder categoryTreeBuilder;
 	
 	@Resource
 	private CategoryService categoryService;
 	
 	@InitBinder
 	public void initBinder(WebDataBinder binder) {
 		binder.registerCustomEditor(CategoryBranch.class,
 				new PropertyEditorSupport() {
 					@Override
 					public void setAsText(String branch) {
 						try {
 							LOGGER.trace(
 									"converting {} to a CategoryBranch object",
 									branch);
 							final CategoryBranch categoryBranch = new CategoryBranch();
 							final StringTokenizer tokenizer = new StringTokenizer(
 									branch, ".");
 							while (tokenizer.hasMoreTokens()) {
 								final String code = tokenizer.nextToken();
 								categoryBranch.addSubCategory(code, null);
 							}
 							// fill names
 							categoryService
 									.completeCategoryBranchNodeNames(categoryBranch);
 							setValue(categoryBranch);
 						} catch (RuntimeException e) {
 							LOGGER.error(":(", e);
 							throw e;
 						}
 					}
 				});
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public String save(@Valid Account account, BindingResult result, Model model) {
 
 		LOGGER.debug("saving user: {} \n {}", account, result);
 
 		
 		if (result.hasErrors()) {
 			LOGGER.trace("errors!: {}", result.getAllErrors());
 			return "account/register";
 		}
 		
 		try{
 			accountService.register(account);
 		}catch(BeanValidationException e){
 			ObjectError error=new ObjectError("account",e.getMessage());
 			result.addError(error);
 			LOGGER.trace("errors!: {}", result.getAllErrors());
 			return "account/register";
 		}
 
 		
 		model.addAttribute("message", "account created successfully");
 		return "message";
 	}
 
 	@RequestMapping(value = "/update", method = RequestMethod.POST)
 	public String update(@Valid Account account, BindingResult result,
 			Model model) {
 
 		LOGGER.debug("updating user: {} \n {}", account, result);
 
 		if (result.hasErrors()) {
 			LOGGER.trace("errors!: {}", result.getAllErrors());
 			return "account/edit";
 		}
 
 		try{
 			accountService.register(account);
 		}catch(BeanValidationException e){
 			ObjectError error=new ObjectError("account",e.getMessage());
 			result.addError(error);
 			LOGGER.trace("errors!: {}", result.getAllErrors());
 			return "account/edit";
 		}
 		model.addAttribute("message", "account updated successfully");
 		return "message";
 	}
 
 	@RequestMapping("register.html")
 	public String register(Account newAccount, Model model) {
 		model.addAttribute("account", newAccount);
 		return "account/register";
 	}
 
 	@RequestMapping("current.html")
 	public String register(Model model) {
 		Account userDetails = (Account) SecurityContextHolder.getContext()
 				.getAuthentication().getDetails();
 		LOGGER.debug("userDetail: {}", userDetails);
 		Account account = accountRepository.findOne(userDetails.getId());
 		model.addAttribute("account", account);
 		return "account/show";
 	}
 
 	@RequestMapping(value = "/edit/{accountId}", method = RequestMethod.GET)
 	public String edit(@PathVariable("accountId") String accountId, Model model) {
 		LOGGER.debug("accountId to edit: {}", accountId);
 		Account account = accountRepository.findOne(accountId);
 		if (account != null) {
 			model.addAttribute("account", account);
 			return "account/edit";
 		} else
 			return "error";
 
 	}
 
 	@RequestMapping(value = "/setupSkills/{accountId}", method = RequestMethod.GET)
 	public String setupSkills(@PathVariable("accountId") String accountId,
 			Model model) {
 		LOGGER.debug("setuping skills for accountId {}", accountId);
 		Account account = accountRepository.findOne(accountId);
 		if (account != null) {
 			// categories
 			final List<Map<String, Object>> categoryTree = categoryTreeBuilder
 					.buildTree();
 			model.addAttribute("categories", categoryTree);
 			model.addAttribute("account", account);
 			return "account/editSkills";
 		} else
 			return "error";
 
 	}
 	
 	@RequestMapping(value = "/skills/", method = RequestMethod.POST)
 	public String updateSkills(@Valid Account account, BindingResult result,
 			Model model) {
 		LOGGER.debug("skills for accountId {} : {}", account.getId(),account.getSkills());
 		final List<Map<String, Object>> categoryTree = categoryTreeBuilder
 				.buildTree();
 		model.addAttribute("categories", categoryTree);
 		model.addAttribute("account", account);
 		if (result.hasErrors()) {
 			LOGGER.trace("errors!: {}", result.getAllErrors());
 			return "account/editSkills";
 		}
 		try{
			accountService.updateSkills(account);
 		}catch(BeanValidationException e){
 			ObjectError error=new ObjectError("account",e.getMessage());
 			result.addError(error);
 			LOGGER.trace("errors!: {}", result.getAllErrors());
 			return "account/editSkills";
 		}
 		model.addAttribute("account", account);
 		return "account/show";
 
 	}
 }
