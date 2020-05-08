 package ru.spbstu.students.dto;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class ItemInfo {
 	private int seller;
 	private String name;
 	private String description;
 	private String photo;
 	private double startBid;
 	private String type;
 	private Double min;
 	private Date startReg;
 	private Date finishReg;
 	private Date startAuc;
 	private Date finishAuc;
 	private int state;
 	private Date delivery;
 	private String category;
 	private int id;
 	private File image;
 	
 	public ItemInfo(int id, int seller, String name, String description, String photo,
 			double startBid, String type, Double min, Date startReg,
 			Date finishReg, Date startAuc, Date finishAuc, int state,
 			Date delivery, String category) {
 		this.id = id;
 		this.seller = seller;
 		this.name = name;
 		this.description = description;
 		this.photo = photo;
 		this.startBid = startBid;
 		this.type = type;
 		this.min = min;
 		this.startReg = startReg;
 		this.finishReg = finishReg;
 		this.startAuc = startAuc;
 		this.finishAuc = finishAuc;
 		this.state = state;
 		this.delivery = delivery;
 		this.category = category;
 	}
 
 	public ItemInfo() {	}
 
 	public int getSeller() {
 		return seller;
 	}
 
 	public void setSeller(int seller) {
 		this.seller = seller;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getPhoto() {
 		return photo;
 	}
 
 	public void setPhoto(String photo) {
 		this.photo = photo;
 	}
 
 	public double getStartBid() {
 		return startBid;
 	}
 
 	public void setStartBid(double startBid) {
 		this.startBid = startBid;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public Double getMin() {
 		return min;
 	}
 
 	public void setMin(Double min) {
 		this.min = min;
 	}
 
 	public Date getStartReg() {
 		return startReg;
 	}
 
 	public void setStartReg(String startReg) {
 		List<Integer> args = parseStringTokenizer(startReg);
 		Calendar c = Calendar.getInstance();
 		c.set(args.get(0), args.get(1)-1, args.get(2), args.get(3), args.get(4), args.get(5));
 		this.startReg = c.getTime();
 	}
 
 	public Date getFinishReg() {
 		return finishReg;
 	}
 
 	public void setFinishReg(String finishReg) {
 		List<Integer> args = parseStringTokenizer(finishReg);
 		Calendar c = Calendar.getInstance();
 		c.set(args.get(0), args.get(1)-1, args.get(2), args.get(3), args.get(4), args.get(5));
 		this.finishReg = c.getTime();
 	}
 
 	public Date getStartAuc() {
 		return startAuc;
 	}
 
 	public void setStartAuc(String startAuc) {
 		List<Integer> args = parseStringTokenizer(startAuc);
 		Calendar c = Calendar.getInstance();
 		c.set(args.get(0), args.get(1)-1, args.get(2), args.get(3), args.get(4), args.get(5));
 		this.startAuc = c.getTime();
 	}
 
 	public Date getFinishAuc() {
 		return finishAuc;
 	}
 
 	public void setFinishAuc(String finishAuc) {
 		List<Integer> args = parseStringTokenizer(finishAuc);
 		Calendar c = Calendar.getInstance();
 		c.set(args.get(0), args.get(1)-1, args.get(2), args.get(3), args.get(4), args.get(5));
 		this.finishAuc = c.getTime();
 	}
 
 	public int getState() {
 		return state;
 	}
 
 	public void setState(int state) {
 		this.state = state;
 	}
 
 	public Date getDelivery() {
 		return delivery;
 	}
 
	public void setDelivery(String delivery) {
		List<Integer> args = parseStringTokenizer(delivery);
		Calendar c = Calendar.getInstance();
		c.set(args.get(0), args.get(1)-1, args.get(2), args.get(3), args.get(4), args.get(5));
		this.delivery = c.getTime();
 	}
 
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public File getImage() {
 		return image;
 	}
 
 	public void setImage(File image) {
 		this.image = image;
 	}
 	
 	public static ArrayList<Integer> parseStringTokenizer(String date) {
 	      
 	      String szDelemiters = "-: "; 
 	      ArrayList<Integer> searchIds = new ArrayList<Integer>();
 	      StringTokenizer st = new StringTokenizer(date, szDelemiters, false);
 	      while(st.hasMoreTokens())
 	      {
 	        searchIds.add(Integer.parseInt(st.nextToken().trim()));     
 	      } 
 	      
 	      return searchIds;
 	      
 	  }
 	
 }
