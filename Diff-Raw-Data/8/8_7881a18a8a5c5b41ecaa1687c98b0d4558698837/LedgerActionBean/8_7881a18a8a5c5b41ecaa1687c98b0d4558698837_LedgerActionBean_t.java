 package com.osc.web.action.ledger;
 
 
 import java.util.Date;
 import java.util.List;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.validation.SimpleError;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import net.sourceforge.stripes.validation.ValidationMethod;
 import net.sourceforge.stripes.validation.ValidationState;
 
 import org.springframework.dao.DataAccessException;
 
 import com.osc.biz.bean.account.AccountBean;
 import com.osc.biz.bean.ledger.EntryBean;
 import com.osc.web.action.AccountAwareActionBean;
 
 
 public class LedgerActionBean extends AccountAwareActionBean {
 
 
 	private int accountId;
 	
 	private Date endDate = new Date();
 	
 	private AccountBean account;
 	
 	private String accountName;
 	
 	private List<EntryBean> entryList;
 	
 	
 	@ValidationMethod(on="view", when=ValidationState.NO_ERRORS) 
 	public void validateAccountName(ValidationErrors errors) {
 		if(accountName == null || accountName.length() == 0)
 			return;
 		if(!isAccountNameValid(accountName)) {
 			errors.add("accountName", new SimpleError("Invalid account selected: '"+accountName+"'"));
 		}
 	}
 
 	@DefaultHandler
 	public Resolution view() {
 		if(accountName == null || accountName.length() == 0) {
 			accountName = "";
	        return new ForwardResolution("/ledger/ledger.jsp");
 		}
 		
 		try {			
 			String[] items = accountName.split("\\s*-\\s*");	
 			accountId = Integer.parseInt(items[0]);			
 			entryList = ledgerService.getEntryList(endDate, accountId);			
 			
 			List<AccountBean> accountList = accountService.getAccountBalances(endDate, accountId);
 			if(accountList==null || accountList.size()==0)
 				account = null;
 			else
 				account = accountList.get(0);
 	    }
 	    catch (DataAccessException e) {
 	    	return handleException(e);
 	    }
 		
        return new ForwardResolution("/ledger/ledger.jsp");
     }
 
 	
 	public Resolution directView() {
 		if(accountId == 0)
 			return new ForwardResolution(LedgerActionBean.class);
 		
 		try {			
 			endDate = new Date();
 			entryList = ledgerService.getEntryList(endDate, accountId);	
 
 			
 			List<AccountBean> accountList = accountService.getAccountBalances(endDate, accountId);
 			if(accountList==null || accountList.size()==0) {
 				account = null;
 				accountName = "";
 			} else {
 				account = accountList.get(0);
 				accountName = account.getAccountId() + " - " + account.getName();
 			}
 	    }
 	    catch (DataAccessException e) {
 	    	return handleException(e);
 	    }
 		
        return new ForwardResolution("/ledger/ledger.jsp");
     }
 
 	
 	public String getAccountName() {
 		return accountName;
 	}
 
 	public void setAccountName(String accountName) {
 		this.accountName = accountName;
 	}
 
 	public List<EntryBean> getEntryList() {
 		return entryList;
 	}
 
 	public AccountBean getAccount() {
 		return account;
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(Date endDate) {
 		this.endDate = endDate;
 	}
 
 	public int getAccountId() {
 		return accountId;
 	}
 
 	public void setAccountId(int accountId) {
 		this.accountId = accountId;
 	}
 
 }
