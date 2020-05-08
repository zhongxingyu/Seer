 package com.omartech.tdg.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.omartech.tdg.model.Customer;
 import com.omartech.tdg.model.Order;
 import com.omartech.tdg.model.PasswordKey;
 import com.omartech.tdg.model.Seller;
 import com.omartech.tdg.service.customer.CustomerAuthService;
 import com.omartech.tdg.service.seller.SellerAuthService;
 import com.omartech.tdg.utils.EmailSender;
 import com.omartech.tdg.utils.EmailTemplate;
 @Service
 public class EmailService {
 
 	private EmailSender sender = new EmailSender();
 	@Autowired
 	private CustomerAuthService customerAuthService;
 	@Autowired
 	private SellerAuthService sellerAuthService;
 	
 	public void sendEmailWhenSellerRegisterSuccess(Seller seller){
 		sender.sendEmail(seller.getEmail(), "Register success", EmailTemplate.createSellerSuccessRegister(seller));
 	}
 	
 	public void sendEmailWhenSellerForgetPassword(String email, PasswordKey key){
 		sender.sendEmail(email, "Pass word Assistance", EmailTemplate.createSellerForgetPassword(key));
 	}
 	
 	public void sendEmailWhenProductWillSoldOut(int productId, Seller seller){
 		sender.sendEmail(seller.getEmail(), "Product stock warning", EmailTemplate.unsafeProductStock(productId));
 	}
 
 	public void sendEmailWhenCustomerClaimOrder(String customerEmail, String sellerEmail,  int orderId, int claimId, int status){
 		sender.sendEmail(customerEmail, "Claim notice letter ", EmailTemplate.claimLettertoCustomer(claimId, status));
 		sender.sendEmail(sellerEmail, "Claim notice letter ", EmailTemplate.claimLetterToSeller(claimId, status));
 	}
 	
 	//下单成功
 	public void sendEmailWhenMakeOrderOk(Order order){
 		
 		int sellerId = order.getSellerId();
 		if(sellerId != 0){
 			int customerId = order.getCustomerId();
 			Customer customer = customerAuthService.getCustomerById(customerId);
 			Seller seller = sellerAuthService.getSellerById(sellerId);
 			
 			String customerEmail = customer.getEmail();
 			String sellerEmail = seller.getEmail();
 			
 			sender.sendEmail(customerEmail, "order received From TDG", EmailTemplate.makeDealToCustomer());
 			sender.sendEmail(sellerEmail, "order received From TDG", EmailTemplate.makeDealToSeller());
 		}
 	}
	
 	public void sendEmailWhenSellerUpdateCarrierInformation(Order order){
 		int customerId = order.getCustomerId();
 		Customer customer = customerAuthService.getCustomerById(customerId);
 		String customerEmail = customer.getEmail();
 		sender.sendEmail(customerEmail, "Order shipping information changed", EmailTemplate.carrierInformationChanged(order.getId()));
 	}
 }
