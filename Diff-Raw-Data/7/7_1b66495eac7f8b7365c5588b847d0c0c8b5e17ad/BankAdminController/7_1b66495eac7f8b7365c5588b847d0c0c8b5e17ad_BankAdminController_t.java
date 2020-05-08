 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.bank.controller;
 
 import static br.octahedron.cotopaxi.controller.Converter.Builder.safeString;
 
 import java.math.BigDecimal;
 import java.util.HashMap;
 
 import br.octahedron.cotopaxi.auth.AuthenticationRequired;
 import br.octahedron.cotopaxi.auth.AuthorizationRequired;
 import br.octahedron.cotopaxi.datastore.namespace.NamespaceRequired;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.cotopaxi.validation.Validator;
 import br.octahedron.figgo.OnlyForNamespaceControllerInterceptor.OnlyForNamespace;
 import br.octahedron.figgo.modules.bank.controller.validation.BankValidators;
 import br.octahedron.figgo.modules.bank.data.BankTransaction.TransactionType;
 import br.octahedron.figgo.modules.bank.manager.AccountManager;
 import br.octahedron.figgo.modules.bank.manager.DisabledBankAccountException;
 import br.octahedron.figgo.modules.bank.manager.InsufficientBalanceException;
 
 /**
  * @author Vítor Avelino
  * 
  */
 @AuthenticationRequired
 @AuthorizationRequired
 @NamespaceRequired
 @OnlyForNamespace
 public class BankAdminController extends AbstractBankController {
 
 	/*
 	 * TODO public bank pages should be in other controller
 	 */
 	private static final String BASE_DIR_TPL = "bank/";
 	private static final String BALLAST_TPL = BASE_DIR_TPL + "ballast.vm";
 	private static final String SHARE_TPL = BASE_DIR_TPL + "share.vm";
 	private static final String COLLECT_TPL = BASE_DIR_TPL + "collect.vm";
 	private static final String BALANCES_TPL = BASE_DIR_TPL + "balances.vm";
 	private static final String BASE_URL = "/bank";
 	private static final String BALLAST_URL = BASE_URL + "/ballast";
 	private static final String SHARE_URL = BASE_URL + "/share";
 
 	@Inject
 	private AccountManager accountManager;
 
 	/**
 	 * Method used by the Injector
 	 */
 	public void setAccountManager(AccountManager accountManager) {
 		this.accountManager = accountManager;
 	}
 
 	/**
 	 * Gets the bank share interface
 	 * 
 	 * @throws DisabledBankAccountException
 	 *             If the bank account is disabled - This kind of account can't be disabled, if this
 	 *             occurs, indicates an DEFECT
 	 */
 	public void getShareBank() throws DisabledBankAccountException {
 		this.out("balance", this.accountManager.getBalance(this.domainBankAccount()));
 		this.success(SHARE_TPL);
 	}
 
 	/**
 	 * Transfer from bank account to a user account
 	 * 
 	 * @throws DisabledBankAccountException
 	 *             If the bank account is disabled - This kind of account can't be disabled, if this
 	 *             occurs, indicates an DEFECT
 	 */
 	public void postShareBank() throws DisabledBankAccountException {
 		try {
 			Validator requiredValidator = BankValidators.getShareValidator();
 			Validator amountValidator = BankValidators.getAmountValidator();
 			if (requiredValidator.isValid() && amountValidator.isValid()) {
 				this.accountManager.transact(this.domainBankAccount(), this.in("userId", safeString()), new BigDecimal(this.in("amount")),
 						this.in("comment", safeString()), TransactionType.valueOf(this.in("type")));
 				this.redirect(SHARE_URL);
 			} else {
 				this.echo();
 				this.out("balance", this.accountManager.getBalance(this.domainBankAccount()));
 				this.invalid(SHARE_TPL);
 			}
 		} catch (InsufficientBalanceException e) {
 			this.out("balance", this.accountManager.getBalance(this.domainBankAccount()));
 			this.out("insufficient", "INSUFFICIENT_FUNDS");
 			this.echo();
 			this.invalid(SHARE_TPL);
 		}
 	}
 
 	/**
 	 * Gets the bank share interface
 	 * 
 	 * @throws DisabledBankAccountException
 	 *             If the bank account is disabled - This kind of account can't be disabled, if this
 	 *             occurs, indicates an DEFECT
 	 */
 	public void getCollectBank() throws DisabledBankAccountException {
 		this.success(COLLECT_TPL);
 	}
 
 	/**
 	 * Transfer from bank account to a user account
 	 * 
 	 * @throws DisabledBankAccountException
 	 *             If the bank account is disabled - This kind of account can't be disabled, if this
 	 *             occurs, indicates an DEFECT
 	 */
 	public void postCollectBank() throws DisabledBankAccountException {
 		try {
 			Validator requiredValidator = BankValidators.getCollectValidator();
 			Validator amountValidator = BankValidators.getAmountValidator();
 			if (requiredValidator.isValid() && amountValidator.isValid()) {
 				// transfer from user to bank
 				this.accountManager.transact(this.in("userId", safeString()), this.domainBankAccount(), new BigDecimal(this.in("amount")),
 						this.in("comment", safeString()), TransactionType.PAYMENT);
 				this.redirect(this.relativeRequestedUrl());
 			} else {
 				this.echo();
 				this.invalid(COLLECT_TPL);
 			}
 		} catch (InsufficientBalanceException e) {
 			this.out("insufficient", "USER_INSUFFICIENT_FUNDS");
 			this.echo();
 			this.invalid(COLLECT_TPL);
 		}
 	}
 
 	/**
 	 * Gets the bank ballast interface
 	 * 
 	 * @throws DisabledBankAccountException
 	 *             If the bank account is disabled - This kind of account can't be disabled, if this
 	 *             occurs, indicates an DEFECT
 	 */
 	public void getBallastBank() throws DisabledBankAccountException {
 		this.out("balance", this.accountManager.getBalance(this.domainBankAccount()));
 		this.success(BALLAST_TPL);
 	}
 
 	/**
 	 * Transfer from system account to bank account
 	 * 
 	 * @throws DisabledBankAccountException
 	 *             If the bank account is disabled - This kind of account can't be disabled, if this
 	 *             occurs, indicates a DEFECT
 	 * @throws InsufficientBalanceException
 	 *             This transfer is from SYSTEM account, this kind of account has infinity funds, if
 	 *             this occurs, indicates a DEFECT
 	 */
 	public void postBallastBank() throws InsufficientBalanceException, DisabledBankAccountException {
 		Validator amount = BankValidators.getAmountValidator();
 		if (amount.isValid()) {
 			this.accountManager.insertBallast(this.domainBankAccount(), new BigDecimal(this.in("amount")), this.in("comment", safeString()));
 			this.redirect(BALLAST_URL);
 		} else {
 			this.echo();
 			this.out("balance", this.accountManager.getBalance(this.domainBankAccount()));
 			this.invalid(BALLAST_TPL);
 		}
 	}
 
 	/**
 	 * Gets balances from all user accounts.
 	 * 
 	 * @throws DisabledBankAccountException
 	 */
 	public void getAllBalancesBank() throws DisabledBankAccountException {
 		HashMap<String, BigDecimal> balances = accountManager.getAllBalances();
 		this.out("balances", balances);
 		this.success(BALANCES_TPL);
 	}
 }
