 package com.omartech.tdg.action.seller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 @RequestMapping("/seller/error")
 public class SellerErrorAction {
 
 	@RequestMapping("/productHasNoChildren")
 	public String productNoChildren(){
 		return "/seller/error/productNoChildren";
 	}
 	@RequestMapping("/carrierisnull")
 	public String carrierisnull(){
 		return "/seller/error/order-carrier-null";
 	}
 	@RequestMapping("/item-with-different-feature")
 	public String differentFeature(){
 		return "/seller/error/item-with-different-feature";
 	}
 	@RequestMapping("/cancel-order-without-reason")
 	public String cancelOrderWithoutReason(){
 		return "/seller/error/cancel-order-without-reason";
 	}
 }
