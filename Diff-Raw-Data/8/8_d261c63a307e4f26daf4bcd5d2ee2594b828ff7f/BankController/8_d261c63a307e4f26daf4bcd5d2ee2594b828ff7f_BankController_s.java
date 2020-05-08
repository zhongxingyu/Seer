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
 
 import static br.octahedron.cotopaxi.controller.Converter.Builder.date;
 import static br.octahedron.figgo.util.DateUtil.SHORT;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import br.octahedron.cotopaxi.CotopaxiProperty;
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
  */
 @AuthenticationRequired
 @AuthorizationRequired
 @NamespaceRequired
 @OnlyForNamespace
 public class BankController extends AbstractBankController {
 
 	/*
 	 * TODO public bank pages should be in other controller
 	 */
 	private static final String INVALID = CotopaxiProperty.getProperty(CotopaxiProperty.INVALID_PROPERTY);
 
 	private static final String BASE_DIR_TPL = "bank/";
 	private static final String INDEX_TPL = BASE_DIR_TPL + "index.vm";
 	private static final String STATEMENT_TPL = BASE_DIR_TPL + "statement.vm";
 	private static final String TRANSFER_TPL = BASE_DIR_TPL + "transfer.vm";
 	private static final String BASE_URL = "/bank";
 	private static final String STATS_TPL = BASE_DIR_TPL + "stats.vm";
 
 	@Inject
 	private AccountManager accountManager;
 
 	/**
 	 * Method used by the Injector
 	 */
 	public void setAccountManager(AccountManager accountManager) {
 		this.accountManager = accountManager;
 	}
 
 	/**
 	 * Handler method for disabled accounts.
 	 */
 	private void handleDisabledAccount(String accountID, String template) {
 		this.out(INVALID, "ACCOUNT_INVALID");
 		this.out("INVALID_ACCOUNT_ID", accountID);
 		this.echo();
 		this.invalid(template);
 	}
 
 	/**
 	 * Shows the initial bank page for a user
 	 */
 	public void getIndexBank() {
 		try {
 			this.out("balance", this.accountManager.getBalance(this.currentUser()));
 			this.out("transactions", this.accountManager.getLastNTransactions(this.currentUser(), 5));
 			this.success(INDEX_TPL);
 		} catch (DisabledBankAccountException ex) {
 			this.handleDisabledAccount(ex.getMessage(), INDEX_TPL);
 		}
 	}
 
 	/**
 	 * Shows the page for transfers between users
 	 */
 	public void getTransferBank() {
 		try {
 			this.out("balance", this.accountManager.getBalance(this.currentUser()));
 			this.out("transactions", this.accountManager.getLastNTransactions(this.currentUser(), 5));
 			this.success(TRANSFER_TPL);
 		} catch (DisabledBankAccountException ex) {
 			this.handleDisabledAccount(ex.getMessage(), TRANSFER_TPL);
 		}
 	}
 
 	/**
 	 * Posts a transfer between users
 	 */
	public void postTransferBank() {
 		Validator input = BankValidators.getTransferValidator();
 		Validator amountValidator = BankValidators.getAmountValidator();
 		try {
 			if (input.isValid() && amountValidator.isValid()) {
 				this.accountManager.transact(this.currentUser(), this.in("userId"), new BigDecimal(this.in("amount")), this.in("comment"),
 						TransactionType.valueOf(this.in("type")));
 				this.redirect(BASE_URL);
 			} else {
 				this.out("balance", this.accountManager.getBalance(this.currentUser()));
 				this.echo();
 				this.invalid(TRANSFER_TPL);
 			}
 		} catch (DisabledBankAccountException ex) {
 			this.handleDisabledAccount(ex.getMessage(), TRANSFER_TPL);
 		} catch (InsufficientBalanceException e) {
			this.out(INVALID, "INSUFFICIENT_FUNDS");
 			this.echo();
 			this.invalid(TRANSFER_TPL);
 		}
 	}
 
 	/**
 	 * Gets the current user current balance
 	 */
 	public void getStatementBank() {
 		try {
 			this.out("balance", this.accountManager.getBalance(this.currentUser()));
 			this.success(STATEMENT_TPL);
 		} catch (DisabledBankAccountException ex) {
 			this.handleDisabledAccount(ex.getMessage(), STATEMENT_TPL);
 		}
 	}
 
 	/**
 	 * Posts parameters to get user's statement by transactions
 	 * 
 	 * JSON method
 	 */
 	public void postStatementBank() {
 		Validator dateValidator = BankValidators.getDateValidator();
 		if (dateValidator.isValid()) {
 			this.out("transactions",
 					this.accountManager.getTransactions(this.currentUser(), this.in("startDate", date(SHORT)), this.in("endDate", date(SHORT))));
 			jsonSuccess();
 		} else {
 			jsonInvalid();
 		}
 	}
 
 	/**
 	 * Gets some important information about the bank
 	 * 
 	 * @throws DisabledBankAccountException
 	 *             If the bank account is disabled - This kind of account can't be disabled, if this
 	 *             occurs, indicates an DEFECT
 	 */
 	public void getStatsBank() throws DisabledBankAccountException {
 		BigDecimal balance = this.accountManager.getBalance(this.domainBankAccount());
 		BigDecimal ballast = this.accountManager.getBallast();
 		this.out("balance", balance);
 		this.out("ballast", ballast);
 		this.out("inCirculation", ballast.subtract(balance)); // TODO move this from here
 		this.out("monthCirculation", this.accountManager.getCurrentAmountTransactions());
 		this.out("creditAmount", this.accountManager.getCurrentAmountCredit());
 		this.success(STATS_TPL);
 	}
 
 	/**
 	 * Posts parameters to get bank's static information
 	 */
 	public void postStatsBank() {
 		Validator dateValidator = BankValidators.getDateValidator();
 		if (dateValidator.isValid()) {
 			Date startDate = this.in("startDate", date(SHORT));
 			Date endDate = this.in("endDate", date(SHORT));
 			this.out("circulation", this.accountManager.getAmountTransactions(startDate, endDate));
 			this.out("creditAmount", this.accountManager.getAmountCredit(startDate, endDate));
 			jsonSuccess();
 		} else {
 			jsonInvalid();
 		}
 	}
 }
