 package org.homebudget.controllers;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import javax.annotation.Resource;
 import javax.validation.Valid;
 import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormat;
 import org.homebudget.model.BinaryResource;
 import org.homebudget.model.Category;
 import org.homebudget.model.Transaction;
 import org.homebudget.model.Transaction.TransactionType;
 import org.homebudget.services.AccountManagementService;
 import org.homebudget.services.CategoryEditor;
 import org.homebudget.services.CategoryManagementService;
 import org.homebudget.services.ResourceManagementService;
 import org.homebudget.services.TransactionManagementService;
 import org.homebudget.services.TransactionValidationService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.multipart.MultipartFile;
 
 @Controller
 @RequestMapping("/accounts")
 public class TransactionController extends AbstractController {
 
    private static final Logger logger = Logger.getLogger(TransactionController.class);
 
    @Autowired
    private TransactionValidationService transactionValidation;
 
    @Resource
    private TransactionManagementService transactionManagementService;
 
    @Resource
    private AccountManagementService accountManagementService;
 
    @Resource
    private CategoryManagementService categoryManagementService;
 
    @Resource
    private ResourceManagementService resourceManagementService;
 
    @RequestMapping(value = "/{name}/transactions", method = RequestMethod.GET)
 
    public String getAllTransactions(@PathVariable("name") String accountName,
          @RequestParam(value = "start", required = false)  @DateTimeFormat(pattern="mm/dd/yyyy") Date start,
          @RequestParam(value = "end", required = false)   @DateTimeFormat(pattern="mm/dd/yyyy") Date end, Model model) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
           .getUsername());
       if (!isAuthorized) {
          return "redirect:";
       }
 
       List<Transaction> transactions = new ArrayList<Transaction>();
       if (start != null && end != null) {
          transactions = transactionManagementService.getAllAccountTransactionsBetween(accountName,
                start, end);
       }
       else {
          transactions = transactionManagementService.getAllAccountTransactions(accountName);
       }
 
       model.addAttribute(transactions);
 
       return "transaction/listTransactions";
    }
 
    @RequestMapping(value = "{name}/transactions/{id}", method = RequestMethod.GET)
    public String getTransaction(@PathVariable("name") String accountName,
        @PathVariable("id") Long transactionId, Model model) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
           .getUsername(), transactionId);
       if (!isAuthorized) {
          return "redirect:";
       }
       final Transaction transaction = transactionManagementService.getTransaction(transactionId);
       final List<TransactionType> transactionTypeList = new ArrayList<TransactionType>(
           Arrays.asList(TransactionType.values()));
       BinaryResource attachment = transaction.getAttachment();
       String attachmentString = null;
       if (attachment != null) {
          attachmentString = resourceManagementService.getBase64ImageString(attachment);
       }
       final List<Category> categories = categoryManagementService.getAllCategories(getSessionUser()
           .getUsername());
 
       model.addAttribute(categories);
       model.addAttribute(transactionTypeList);
       model.addAttribute(transaction);
       model.addAttribute("attachment", attachmentString);
       return "transaction/editTransaction";
    }
 
    @RequestMapping(value = "{name}/transactions/new", method = RequestMethod.GET)
    public String createTransaction(@PathVariable("name") String accountName, Model model) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
           .getUsername());
       if (!isAuthorized) {
          return "redirect:";
       }
 
       final List<TransactionType> transactionTypeList = new ArrayList<TransactionType>(
           Arrays.asList(TransactionType.values()));
 
       final List<Category> categories = categoryManagementService.getAllCategories(getSessionUser()
           .getUsername());
 
       model.addAttribute(categories);
       model.addAttribute(transactionTypeList);
       model.addAttribute(new Transaction());
       return "transaction/newTransaction";
    }
 
    @RequestMapping(value = "{name}/transactions/{id}", method = RequestMethod.DELETE)
    public String deleteTransaction(@PathVariable("name") String accountName,
        @PathVariable("id") Long transactionId) {
 
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
           .getUsername(), transactionId);
       if (!isAuthorized) {
          return "redirect:";
       }
       final Transaction transaction = transactionManagementService.getTransaction(transactionId);
 
       transaction.getParent().getTransactions().remove(transaction);
 
       accountManagementService.updateAccount(transaction.getParent());
 
       if (transaction == null) {
          return "redirect:";
       }
 
       return "redirect:";
    }
 
    @RequestMapping(value = "{name}/transactions/new", method = RequestMethod.POST)
    public String postTransaction(@PathVariable("name") String accountName,
        @ModelAttribute("transaction") @Valid Transaction transaction, BindingResult result,
        @RequestParam(value = "file", required = false) MultipartFile file) {
       if (file == null) {
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!File is null!!!!!!!!!!!!!!!!!!!!!!!!!!!");
       }
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
           .getUsername());
       if (!isAuthorized) {
          return "redirect:";
       }
       logger.info("Processing save transacton: " + transaction);
       if (file != null) {
          BinaryResource resource = resourceManagementService.getResource(file);
          transaction.setAttachment(resource);
       }
       transactionManagementService.saveTransaction(transaction, accountName);
 
       return "redirect:";
    }
 
    @RequestMapping(value = "{name}/transactions", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
 
    public String updateTransactionDetails(
        @RequestParam(value = "file", required = false) MultipartFile file,
        @PathVariable("name") String accountName, Transaction transaction, BindingResult result,
        Model model) {
       if (file == null) {
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!File is null!!!!!!!!!!!!!!!!!!!!!!!!!!!");
       }
       boolean isAuthorized = accountManagementService.isAuthorized(accountName, getSessionUser()
           .getUsername(), transaction.getId());
       if (!isAuthorized) {
          return "redirect:";
       }
 
       Transaction oldTransaction = transactionManagementService.getTransaction(transaction.getId(),
           accountName);
 
       if (oldTransaction == null) {

       }
       transactionManagementService.updateTransactionDetails(oldTransaction, transaction, file);
 
 
 
    }
 
    public TransactionManagementService getTransactionManagementService() {
 
       return transactionManagementService;
    }
 
    public void setTransactionManagementService(
        TransactionManagementService transactionManagementService) {
 
       this.transactionManagementService = transactionManagementService;
    }
 
    public TransactionValidationService getTransactionValidation() {
 
       return transactionValidation;
    }
 
    public void setTransactionValidation(TransactionValidationService transactionValidation) {
 
       this.transactionValidation = transactionValidation;
    }
 
    @InitBinder
    public void initBinder(WebDataBinder binder) {
 
       SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
       binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
 
       binder.registerCustomEditor(Category.class, new CategoryEditor(categoryManagementService,
           getSessionUser().getUsername()));
    }
 
 }
