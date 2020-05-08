 package com.smes.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import com.smes.dao.BankDao;
 import com.smes.dao.DepositLogDao;
 import com.smes.dao.WithdrawalLogDao;
 import com.smes.domain.DomainAuditUtil;
 import com.smes.domain.hibernate.Bank;
 import com.smes.domain.hibernate.DepositLog;
 import com.smes.domain.hibernate.User;
 import com.smes.domain.hibernate.WithdrawalLog;
 import com.smes.view.frm.BankTransaction;
 import com.smes.view.frm.Credential;
 
 /**
  * TODO: Must consider paging!!!
  *
  */
 public class BankTransactionService {
 	private WithdrawalLogDao withdrawalLogDao;
 	private DepositLogDao depositLogDao;
 	private BankDao bankDao;
 	
 	public void setBankDao(BankDao bankDao) {
 		this.bankDao = bankDao;
 	}
 
 	public void setDepositLogDao(DepositLogDao depositLogDao) {
 		this.depositLogDao = depositLogDao;
 	}
 	
 	public void setWithdrawalLogDao(WithdrawalLogDao withdrawalLogDao) {
 		this.withdrawalLogDao = withdrawalLogDao;
 	}
 
 	public Collection<Bank> getBanks (){
 		return bankDao.getAllBanks();
 	}
 	
 	public Collection<BankTransaction> getBankTransactions (int bankId){
 		List<BankTransaction> bankTransactions = new ArrayList<BankTransaction>();
 		Collection<WithdrawalLog> withdrawalLogs = withdrawalLogDao.getWithdrawals (bankId);
 		Collection<DepositLog> depositLogs = depositLogDao.getDeposits (bankId);
 		//Populate first the bank transaction without the total.
 		for (DepositLog dl : depositLogs)
 			bankTransactions.add(BankTransaction.getInstanceOf(dl.getDepositLogId(), bankId, 
 					dl.getDepositDate(), dl.getRemarks(), 0, dl.getAmount(), 0));
 		for (WithdrawalLog wl : withdrawalLogs)
 			bankTransactions.add(BankTransaction.getInstanceOf(wl.getWithdrawalLogId(), bankId,
					wl.getWithdrawalDate(), wl.getRemarks(), wl.getAmount(), 0, 0));
 		System.out.println(withdrawalLogs);
 		Collections.sort (bankTransactions);
 		setAvailableBalance (bankTransactions);
 		return bankTransactions;
 	}
 	
 	private void setAvailableBalance (Collection<BankTransaction> bankTransactions){
 		BankTransaction prev = null;
 		for (BankTransaction bt : bankTransactions){
 			double amount = bt.getDepositAmount () - bt.getWithdrawalAmount();
 			if (prev == null){
 				bt.setAvailableAmount (amount);
 				prev = bt;
 			}
 			bt.setAvailableAmount (prev.getAvailableAmount() + amount);
 		}
 	}
 	
 	public void saveBankTransaction (BankTransaction bankTransaction, Credential credential){
 		switch (bankTransaction.getBankTransactionType()){
 			case Deposit :
 				depositLogDao.saveDeposit(convertAndProcessToDepositLog(bankTransaction, credential));
 				break;
 			case Withdrawal:
 				withdrawalLogDao.saveWithdrawal(convertAndProcessToWithdrawalLog(bankTransaction, credential));
 				break;
 		}
 	}
 	
 	private DepositLog convertAndProcessToDepositLog (BankTransaction bt, Credential credential){
 		DepositLog dl = DepositLog.getInstaceOf(bt.getBankTransactionId(),
 				bt.getDate(), bt.getRemarks(), 
 				bt.getDepositAmount(), bt.getBankId());
 		User user = credential.getUser();
 		Date currentDate = new Date ();
 		if (dl.getDepositLogId() < 1) {
 			depositLogDao.persistObject(dl);
 			DomainAuditUtil.updateAudit(dl, user, currentDate);
 		} else {
 			DomainAuditUtil.addAudit(dl, user, currentDate);
 		}
 		return dl;
 	}
 	
 	private WithdrawalLog convertAndProcessToWithdrawalLog (BankTransaction bt, Credential credential){
 		WithdrawalLog wl = WithdrawalLog.getInstanceOf(bt.getBankTransactionId(), bt.getDate(), 
 				bt.getRemarks(), bt.getWithdrawalAmount(), bt.getBankId());
 		User user = credential.getUser();
 		Date currentDate = new Date ();
 		if (wl.getWithdrawalLogId() < 1){
 			withdrawalLogDao.persistObject(wl);
 			DomainAuditUtil.updateAudit(wl, user, currentDate);
 		} else {
 			DomainAuditUtil.addAudit(wl, user, currentDate);
 		}
 		return wl;
 	}
 }
