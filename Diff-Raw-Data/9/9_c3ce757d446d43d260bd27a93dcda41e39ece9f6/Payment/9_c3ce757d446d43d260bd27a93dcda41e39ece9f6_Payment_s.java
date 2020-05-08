 package com.infonam.film.store.model;
 
 import java.sql.Timestamp;
 import java.sql.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 @Entity
 @Table(name="payment", catalog="sakila")
 public class Payment {
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	@Column(name="payment_id", unique=true,nullable=false)
 	private int payment_id;
 	private Customer customer;
 	private Staff taff;
 	private Rental rental;
 	private float amount;
 	private Date payment_date;
 	private Timestamp last_update;
	
 	public int getPayment_id() {
 		return payment_id;
 	}
 	public void setPayment_id(int payment_id) {
 		this.payment_id = payment_id;
 	}
 	public Customer getCustomer() {
 		return customer;
 	}
 	public void setCustomer(Customer customer) {
 		this.customer = customer;
 	}
 	public Staff getTaff() {
 		return taff;
 	}
 	public void setTaff(Staff taff) {
 		this.taff = taff;
 	}
 	public Rental getRental() {
 		return rental;
 	}
 	public void setRental(Rental rental) {
 		this.rental = rental;
 	}
 	public float getAmount() {
 		return amount;
 	}
 	public void setAmount(float amount) {
 		this.amount = amount;
 	}
 	public Date getPayment_date() {
 		return payment_date;
 	}
 	public void setPayment_date(Date payment_date) {
 		this.payment_date = payment_date;
 	}
 	public Timestamp getLast_update() {
 		return last_update;
 	}
 	public void setLast_update(Timestamp last_update) {
 		this.last_update = last_update;
 	}
 }
