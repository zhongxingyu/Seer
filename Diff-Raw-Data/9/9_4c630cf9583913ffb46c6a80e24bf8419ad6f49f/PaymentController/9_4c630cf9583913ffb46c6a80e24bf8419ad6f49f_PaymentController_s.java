 package org.twuni.money.exchange.web.controller;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.twuni.money.common.Bank;
 import org.twuni.money.common.Repository;
 import org.twuni.money.common.SimpleToken;
 import org.twuni.money.common.Token;
 import org.twuni.money.common.exception.InsufficientFundsException;
 import org.twuni.money.exchange.Application;
 import org.twuni.money.exchange.anet.command.ClaimCommand;
 import org.twuni.money.exchange.anet.command.PayCommand;
 import org.twuni.money.exchange.exception.PaymentException;
 import org.twuni.money.exchange.exception.ValidationException;
 import org.twuni.money.exchange.model.Payment;
 import org.twuni.money.exchange.util.Validator;
 import org.twuni.money.exchange.web.command.BuyCommand;
 import org.twuni.money.exchange.web.command.SellCommand;
 import org.twuni.money.exchange.web.model.BuyContext;
 import org.twuni.money.exchange.web.model.ClaimContext;
 import org.twuni.money.exchange.web.model.Context;
 import org.twuni.money.exchange.web.model.SellContext;
 
 import com.google.gson.Gson;
 
 @Controller
 public class PaymentController {
 
 	private class Path {
 
 		static final String FORM = "/*";
 		static final String BUY = "/buy";
 		static final String PAY = "/pay";
 		static final String SELL = "/sell";
 		static final String CLAIM = "/claim";
 
 	}
 
 	private final Logger log = LoggerFactory.getLogger( getClass() );
 
 	@Autowired
 	private PayCommand payCommand;
 
 	@Autowired
 	private ClaimCommand claimCommand;
 
 	@Autowired
 	private Application application;
 
 	@Autowired
 	private Repository<String, Payment> paymentRepository;
 
 	@Autowired
 	private Validator<String> treasuryValidator;
 
 	@Autowired
 	private Bank bank;
 
 	@RequestMapping( value = Path.FORM, method = RequestMethod.GET )
 	public String showForm() {
 		return "form";
 	}
 
 	@RequestMapping( value = Path.BUY, method = RequestMethod.POST )
 	public ModelAndView buy( @ModelAttribute BuyCommand command ) {
 
 		BuyContext context = new BuyContext( command );
 
 		context.setAmount( fromTokenValue( command.getAmount() ) );
 
 		return new ModelAndView( "buy", Context.NAME, context );
 
 	}
 
 	@RequestMapping( value = Path.PAY, method = RequestMethod.POST )
 	public void pay( @RequestParam String accountNumber, @RequestParam String expirationDate, @RequestParam float amount, HttpServletResponse response ) throws IOException {
 
 		String relayUrl = application.getUrl( Path.CLAIM );
 		long invoiceNumber = System.currentTimeMillis();
 		String notes = "";
 
 		String result = payCommand.execute( accountNumber, expirationDate, amount, relayUrl, invoiceNumber, notes );
 
 		response.setContentType( "text/html" );
 
 		response.getWriter().write( result );
 		response.getWriter().flush();
 		response.getWriter().close();
 
 	}
 
 	@RequestMapping( value = Path.CLAIM, method = RequestMethod.POST )
	public ModelAndView claim( @RequestParam( "x_amount" ) float amount, @RequestParam( "x_trans_id" ) String transactionId, @RequestParam( "x_MD5_Hash" ) String signature ) {
 
 		ClaimContext context = new ClaimContext( new org.twuni.money.exchange.web.command.ClaimCommand( amount, signature, transactionId ) );
 
 		try {
 
 			claimCommand.execute( amount, transactionId, signature );
 
 			validate( bank );
 
 			Token token = bank.withdraw( toTokenValue( amount ) );
 
 			Payment payment = new Payment( amount, transactionId, token );
 
 			paymentRepository.save( payment );
 
 			context.setPayment( payment );
 
 		} catch( ValidationException exception ) {
 			context.getErrors().put( "validation", "The transaction could not be processed due to a validation error." );
 		} catch( InsufficientFundsException exception ) {
			// TODO: Request more funds from the treasury or communicate the transaction failure to
			// Authorize.net.
			context.getErrors().put( "funds", "This exchange does not have the funds necessary to complete the transaction." );
 		}
 
 		return new ModelAndView( "claim", Context.NAME, context );
 
 	}
 
 	@RequestMapping( value = Path.SELL, method = RequestMethod.POST )
 	public ModelAndView sell( @ModelAttribute SellCommand command ) {
 
 		SellContext context = new SellContext( command );
 
 		try {
 
 			Token token = new Gson().fromJson( command.getToken(), SimpleToken.class );
 
 			treasuryValidator.validate( token.getTreasury() );
 
 			bank.deposit( token );
 
 			try {
 				throw new PaymentException( new UnsupportedOperationException() );
 			} catch( PaymentException exception ) {
 				context.setToken( bank.withdraw( token.getValue() ) );
 				throw exception;
 			}
 
 		} catch( Exception exception ) {
 			context.getErrors().put( "token", exception.getMessage() );
 		}
 
 		return new ModelAndView( "sell", Context.NAME, context );
 
 	}
 
 	private void validate( Bank bank ) {
 		try {
 			bank.validate();
 		} catch( Exception exception ) {
 			log.info( exception.getMessage() );
 		}
 	}
 
 	private float fromTokenValue( int tokenValue ) {
 		return tokenValue * 0.01f;
 	}
 
 	private int toTokenValue( float paymentAmount ) {
 		return Float.valueOf( paymentAmount / 0.01f ).intValue();
 	}
 
 	public void setPayCommand( PayCommand payCommand ) {
 		this.payCommand = payCommand;
 	}
 
 	public void setClaimCommand( ClaimCommand claimCommand ) {
 		this.claimCommand = claimCommand;
 	}
 
 	public void setApplication( Application application ) {
 		this.application = application;
 	}
 
 	public void setPaymentRepository( Repository<String, Payment> paymentRepository ) {
 		this.paymentRepository = paymentRepository;
 	}
 
 	public void setBank( Bank bank ) {
 		this.bank = bank;
 	}
 
 	public void setTransactionRepository( Repository<String, Payment> transactionRepository ) {
 		this.paymentRepository = transactionRepository;
 	}
 
 	public void setTreasuryValidator( Validator<String> treasuryValidator ) {
 		this.treasuryValidator = treasuryValidator;
 	}
 
 }
