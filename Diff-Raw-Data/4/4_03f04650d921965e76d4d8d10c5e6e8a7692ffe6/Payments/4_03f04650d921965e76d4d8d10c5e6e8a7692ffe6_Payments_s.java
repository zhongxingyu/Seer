 package controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import models.Payment;
 import models.User;
 import models.Receipt;
 import play.*;
 import play.mvc.*;
 
 /**
  * For CRUD-interface
  * 
  * @author Peksa
  */
 @With(Secure.class) // Require login for controller access
 public class Payments extends CRUD
 {
 	// Helpers for show
 	static void increment(HashMap<User,Integer> map, User key, Integer value) {
 		if(map.containsKey(key)) map.put(key, map.get(key) + value);
 		else map.put(key, value);
 	}
 	static void addset(HashMap<User,HashSet<Receipt>> map, User key, Receipt receipt) {
 		if(!map.containsKey(key)) map.put(key, new HashSet<Receipt>());
 		map.get(key).add(receipt);
 	}
 	static void addPayment(ArrayList<Payment> active, ArrayList<Payment> settled, Payment payment) {
 		if(payment.accepted == null) active.add(payment);
 		else settled.add(payment);
 	}
 	
 	public static void index() {
 		User connected = Security.connectedUser();
 		if (connected != null)
 			show(connected.id);
 	}
 	
 	/**
 	 * Provides a list of all the users debts per person, as well as incoming debts
 	 * This matches incoming and outgoing debts over time and settles any difference
 	 * @param userId
 	 */
 	public static void show(Long userId) {
 		validation.required(userId);
 		if(!validate(userId)) return;
 		
 		//TODO(dschlyter) This operation is pretty expensive, but premature optimization is the root of all evil etc
 		// Opt Idea 1: Cache result and invalidate on new receipt with user involved
 		// Opt Idea 2: Cache result and date and only use new data to re-calculate (harder)
 		
 		User user = User.findById(userId);
 		HashMap<User, Integer> debt = new HashMap<User, Integer>();
 		// Track debt that is linked to fresh receipts - this is to enable user to verify debt correctness
 		HashMap<User, Integer> freshDebt = new HashMap<User, Integer>();
 		HashMap<User, HashSet<Receipt>> freshReceipts = new HashMap<User, HashSet<Receipt>>();
 		
 		ArrayList<Payment> liabilities = new ArrayList<Payment>(); // Outgoing unpayed
 		ArrayList<Payment> pending = new ArrayList<Payment>(); // Outgoing payed, not accepted
 		ArrayList<Payment> securities = new ArrayList<Payment>(); // Incomming unpayed
 		ArrayList<Payment> accept = new ArrayList<Payment>(); // Incomming payed, not accepted
 		ArrayList<Payment> settled = new ArrayList<Payment>(); // All accepted payments
 		
 		// Sum all receipts where you owe money, subtract sum of all receipts owned
 		for(Receipt r : user.incomingReceipts) {
 			int total = r.getTotal(user);
 			increment(debt, r.owner, total);
 			if(!r.hasPayment(user)) {
 				increment(freshDebt, r.owner, total);
 				addset(freshReceipts, r.owner, r);
 			}
 		}
 		for(Receipt r : user.receipts) {
 			for(User u : r.members) {
 				int total = r.getTotal(u);
 				increment(debt, u, -total);
 				if(!r.hasPayment(u)) { 
 					increment(freshDebt, u, -total);
 					addset(freshReceipts, u, r);
 				}
 			}
 		}
 		
 		// Sum all payments received, subtract sum of all payments made
 		for(Payment p : user.incomingPayments) {
 			increment(debt, p.payer, p.amount);
 			addPayment(accept,settled,p);
 		}
 		for(Payment p : user.payments) {
 			increment(debt, p.receiver, -p.amount);
 			addPayment(pending,settled,p);
 		}
 		
 		// TODO break up into payment objects
		int paymentCounter = user.payments.size();
 		for(User u : debt.keySet()) {
 			int userDebt = debt.get(u);
 			if(userDebt != 0) {
 				int missing = Math.abs(userDebt - freshDebt.get(u));
 				
 				ArrayList<Receipt> receipts = new ArrayList<Receipt>();
 				if(freshReceipts.containsKey(u)) receipts.addAll(freshReceipts.get(u));
 				
 				if(userDebt > 0) {
					paymentCounter = (paymentCounter + 1) % 10000;
 					String paymentId = user.username.substring(0,Math.min(6,user.username.length())) 
 						+ Integer.toString(paymentCounter);
 					liabilities.add(new Payment(user, u, paymentId, userDebt, missing, receipts));
 				}
 				else if(userDebt < 0) securities.add(new Payment(u, user, "", -userDebt, missing, receipts));
 			}	
 		}
 		
 		render(liabilities, pending, securities, accept, settled, user);
 	}
 
 	
 	/**
 	 * Creates new payment
 	 * @param senderId
 	 * @param receiverId
 	 * @param identifier a 10 char identifier for this payment
 	 * @param amount
 	 * @param unsourced money not trackable to related receipts
 	 * @param receipts List of receipts this payment is covering
 	 */
 	public static void add(Long senderId, Long receiverId, String identifier, int amount, int unsourced, List<Long> receiptId)
 	{
 		validation.required(senderId);
 		validation.required(receiverId);
 		validation.required(amount);
 		if(!validate(senderId)) return;
 		
 		List<Receipt> receipts = new ArrayList<Receipt>();
 		for (Long id : receiptId) 
 		{
 			Receipt r = Receipt.findById(id);
 			receipts.add(r);
 		}
 		
 		User receiver = User.findById(receiverId);
 		Payment payment = new Payment(Security.connectedUser(), receiver, identifier, amount, unsourced, receipts);
 		payment.save();
 
 		index();
 	}
 	
 	/**
 	 * Marks a payment as accepted
 	 * @param userId
 	 * @param paymentId
 	 */
 	public static void accept(Long paymentId) {
 		validation.required(paymentId);
 		Payment payment = Payment.findById(paymentId);
 		if(!validate(payment.receiver.id)) return;
 		
 		payment.accepted = new Date();
 		payment.save();
 		
 		index();
 	}
 	
 	/**
 	 * Verify that user ID is same as authorized ID and input has no errors
 	 * @param userId Id of user
 	 * @return true iff validation is successful
 	 */
 	static boolean validate(Long userId) {
 		if (validation.hasErrors()) {
 			error("Params failed validation.");
 			return false;
 		}
 		
 		User user = User.findById(userId);
 		if (user == null || !Security.isAuthorized(user)) {
 			error("User not authorized");
 			return false;
 		}
 			
 		return true;
 	}
 }
