 package main.java.browniepoints.model;
 
 public class CompositeQuestion implements Cloneable {
 	private Question q = new Question();
 	private Coupon c = new Coupon();
 	private Restaurant r = new Restaurant();
 
 	private String answer_status = "N";
 	private String voucher_code = "";
 
 	public String getVoucher_code() {
 		return voucher_code;
 	}
 
 	public void setVoucher_code(String voucher_code) {
 		this.voucher_code = voucher_code;
 	}
 
 	public String getAnswer_status() {
 		return answer_status;
 	}
 
 	public void setAnswer_status(String answer_status) {
 		this.answer_status = answer_status;
 	}
 
 	public Question getQ() {
 		return q;
 	}
 
 	@Override
 	public String toString() {
 		return "CompositeQuestion [q=" + q + ", c=" + c + ", r=" + r
 				+ ", answer_status=" + answer_status + ", voucher_code="
 				+ voucher_code + "]";
 	}
 
 	public void setQ(Question q) {
 		this.q = q;
 	}
 
 	public Coupon getC() {
 		return c;
 	}
 
 	public void setC(Coupon c) {
 		this.c = c;
 	}
 
 	public Restaurant getR() {
 		return r;
 	}
 
 	public void setR(Restaurant r) {
 		this.r = r;
 	}
 
 	public CompositeQuestion copyOf() throws CloneNotSupportedException {
 		return (CompositeQuestion) this.clone();
 	}
 
 	@Override
 	protected Object clone() throws CloneNotSupportedException {
		CompositeQuestion obj = (CompositeQuestion) super.clone();
		obj.setQ((Question) this.q.clone());
		return obj;
 	}
 }
