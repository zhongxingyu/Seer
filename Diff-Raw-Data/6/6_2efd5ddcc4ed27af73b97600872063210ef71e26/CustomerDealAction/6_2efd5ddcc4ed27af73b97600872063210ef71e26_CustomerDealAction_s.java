 package com.omartech.tdg.action.customer;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.omartech.tdg.exception.OutOfStockException;
 import com.omartech.tdg.mapper.CustomerAddressMapper;
 import com.omartech.tdg.model.Cart;
 import com.omartech.tdg.model.Customer;
 import com.omartech.tdg.model.CustomerAddress;
 import com.omartech.tdg.model.Item;
 import com.omartech.tdg.model.Order;
 import com.omartech.tdg.model.OrderItem;
 import com.omartech.tdg.service.CartService;
 import com.omartech.tdg.service.ItemService;
 import com.omartech.tdg.service.OrderService;
 import com.omartech.tdg.utils.OrderStatus;
 
 @Controller
 public class CustomerDealAction {
 	@Autowired
 	private ItemService itemService;
 	
 	@Autowired
 	private CustomerAddressMapper customerAddressMapper;
 	
 	@Autowired
 	private OrderService orderService;
 	
 	@Autowired
 	private CartService cartService;
 	
 	
 	@RequestMapping("/customer/paymoney")
 	public ModelAndView pay(@RequestParam int orderId){
 		Order order = orderService.getOrderById(orderId);
 		return new ModelAndView("/customer/order/pay").addObject("order", order);
 	}
 	
 	@RequestMapping("/customer/paymoney/callback")
 	public String paymoneyCallback(@RequestParam int orderId){
 		try{
 			orderService.updateOrderStatus(OrderStatus.PAID, orderId);
 		}catch(OutOfStockException e){
 			orderService.updateOrderStatus(OrderStatus.ERROR, orderId);//把订单置为错误状态
 			return "redirect:/customer/order/order-out-of-stock";
 		}
 		//扣钱
 		//TODO
 		return "redirect:/customer/order/show/"+orderId;
 	}
 	@RequestMapping("/customer/order/order-out-of-stock")
 	public ModelAndView outOfStock(){
 		return new ModelAndView("/customer/order/order-out-of-stock");
 	}
 	
 	
 	@RequestMapping(value="/order/create", method=RequestMethod.POST)
 	@ResponseBody
 	public Order createOrder(
 			@RequestParam int addressId,
 			@RequestParam String orderItems,
 			HttpSession session
 			){
 		Customer customer = (Customer)session.getAttribute("customer");
 		int customerId = customer.getId();
 		CustomerAddress customerAddress = customerAddressMapper.getCustomerAddressById(addressId);
 		Order order = new Order();
 		order.setName(customerAddress.getName());
 		order.setAddress(customerAddress.getAddress());
 		order.setCity(customerAddress.getCity());
 		order.setCountry(customerAddress.getCountry());
 		order.setPostCode(customerAddress.getPostCode());
 		order.setCustomerId(customerId);
 		order.setCreateAt(new Date());
 		int countryCode = customerAddress.getCountryCode();
 		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
 		Gson gson = new Gson();
 		if(orderItems!=null && orderItems.length()>1){
 			orderItemList = gson.fromJson(orderItems, new TypeToken<List<OrderItem>>(){}.getType());
 		}
 		List<OrderItem> newList = new ArrayList<OrderItem>();
 		for(OrderItem oi : orderItemList){
 			int itemId = oi.getItemId();
 			Item item = itemService.getItemById(itemId);
 			OrderItem noi = cartService.createOrderItemFromItem(item, oi.getNum());
 			judgeTransFee(noi, countryCode);
 			newList.add(noi);
 			cartService.deleteByCustomerIdAndItemId(customerId, itemId);
 		}
 		order.setOrderItems(newList);
 		order.setOrderStatus(OrderStatus.NOPAY);
 		orderService.insertOrder(order);
 		return order;
 	}
 	private void judgeTransFee(OrderItem item, int country){
 		int code = item.getCountryCode();
 		if(code == country){
 			item.setIfeeRMB(0);
 			item.setInternationalShippingFee(0);
 		}
 	}
 	
 	@RequestMapping("/cart")
 	public ModelAndView showCart(
 			HttpSession session,
 			Locale locale
 			){
 		Customer customer = (Customer)session.getAttribute("customer");
 		if(customer == null){
 			return new ModelAndView("/customer/auth/login");
 		}
 		int customerId = customer.getId();
 		List<CustomerAddress> addresses = customerAddressMapper.getCustomerAddressByCustomerId(customerId);
 		List<Cart> carts = cartService.getCartsByCustomerId(customerId);
 		List<OrderItem> orderItems = new ArrayList<OrderItem>();
 		for(Cart tmp : carts){
 			int itemId = tmp.getItemId();
 			Item item = itemService.getItemById(itemId);
 			OrderItem orderItem = cartService.createOrderItemFromItem(item, tmp.getNumber());
 			orderItems.add(orderItem);
 		}
 		return new ModelAndView("/customer/order/cart-list").addObject("orderItems", orderItems).addObject("addresses", addresses).addObject("locale", locale);
 	}
 	
 	
 	@ResponseBody
 	@RequestMapping("/deletefromcart")
 	public String deleteFromCart(
 			@RequestParam int sku,//Item.itemId
 			HttpSession session
 			){
 		Customer customer = (Customer)session.getAttribute("customer");
 		if(customer == null){
 			return "error";
 		}
 		int customerId = customer.getId();
 		cartService.deleteByCustomerIdAndItemId(customerId, sku);
 		return "success";
 	}
 	@ResponseBody
 	@RequestMapping("/addtocart")
 	public String addtoCart(
 			@RequestParam int sku,//若无单品则传productId
 			@RequestParam int number,
 			HttpSession session
 			){
 		Customer customer = (Customer)session.getAttribute("customer");
 		if(customer == null){
 			return "error";
 		}
 		int customerId = customer.getId();
 		List<Cart> carts = cartService.getCartsByCustomerId(customerId);
 		boolean existFlag = false;
 		if(carts.size() != 0){
 			for(Cart c : carts){
 				if(c.getItemId() == sku){
					number = c.getNumber()+number;
 					cartService.updateNumberByCustomerIdAndItemId(customerId, sku, number);
 					existFlag = true;
 				}
 			}
 		}
 		if(!existFlag){
 			Cart nc = new Cart();
 			nc.setNumber(number);
 			nc.setItemId(sku);
 			nc.setCustomerId(customerId);
 			cartService.insert(nc);
 		}
 		return "success";
 	}
 
 	public ItemService getItemService() {
 		return itemService;
 	}
 
 	public void setItemService(ItemService itemService) {
 		this.itemService = itemService;
 	}
 
 	public CustomerAddressMapper getCustomerAddressMapper() {
 		return customerAddressMapper;
 	}
 
 	public void setCustomerAddressMapper(CustomerAddressMapper customerAddressMapper) {
 		this.customerAddressMapper = customerAddressMapper;
 	}
 
 	public OrderService getOrderService() {
 		return orderService;
 	}
 
 	public void setOrderService(OrderService orderService) {
 		this.orderService = orderService;
 	}
 
 	public CartService getCartService() {
 		return cartService;
 	}
 
 	public void setCartService(CartService cartService) {
 		this.cartService = cartService;
 	}
 }
