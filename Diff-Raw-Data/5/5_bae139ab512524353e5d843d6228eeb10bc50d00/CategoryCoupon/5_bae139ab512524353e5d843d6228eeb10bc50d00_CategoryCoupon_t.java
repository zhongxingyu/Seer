 package com.sk.domain.coupon;
 
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
import javax.persistence.Transient;
 
 import com.sk.domain.Category;
 import com.sk.domain.Order;
 import com.sk.service.coupon.CategoryCouponChecker;
 
 @Entity
 @DiscriminatorValue("CategoryCoupon")
 public class CategoryCoupon extends Coupon {
 
 	private static final long serialVersionUID = -4580681695558831088L;
 
	@Transient
	private int subtask24;
	
 	@ManyToOne
 	private Category category;
 
 	@Override
 	public CouponHolder getCouponHolder() {
 		return category;
 	}
 	
 	@Override
 	public void setCouponHolder(CouponHolder couponHolder) {
 		category = (Category)couponHolder;
 	}
 
 	@Override
 	public Boolean canUseCoupon(Order order) {
 		return new CategoryCouponChecker(category).canUseCoupon(order);
 	}
 	
 }
