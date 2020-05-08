 package com.lebk.dto;
 
 import java.util.Date;
 
 import com.lebk.dao.BusinessTypeDao;
 import com.lebk.dao.ProductDao;
 import com.lebk.dao.UserDao;
 import com.lebk.dao.impl.BusinessTypeDaoImpl;
 import com.lebk.dao.impl.ProductDaoImpl;
 import com.lebk.dao.impl.UserDaoImpl;
 import com.lebk.enumType.BusinessEnumType;
 import com.lebk.po.Ptdetails;
 
 /**
  * Copyright: All Right Reserved.
  * 
  * @author Lei Bo(lebk.lei@gmail.com)
  * @contact: qq 87535204
  * @date 2013-11-18
  */
 
 public class PtDetailsDTO {
 
 	private Integer id;
 	private Integer poId;
 	private String prdName;
 	private String businessType;
 	private Integer ptNumber;
 	private String opUser;
 	private Date date;
 	UserDao ud = new UserDaoImpl();
 	BusinessTypeDao btd = new BusinessTypeDaoImpl();
 	ProductDao pd = new ProductDaoImpl();
 
 	public PtDetailsDTO() {
 
 	}
 
 	public PtDetailsDTO(Ptdetails ptd) {
 		this.id = ptd.getId();
 		this.poId = ptd.getPoId();
 		this.prdName = pd.getNameByProductId(poId);
		this.ptNumber=ptd.getNum();
 		this.date = ptd.getDate();
 		this.businessType = btd.getTypeById(ptd.getBtId());
 		this.opUser = ud.getUsernamebyUserid(ptd.getOpUserId());
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public Integer getPoId() {
 		return poId;
 	}
 
 	public void setPoId(Integer poId) {
 		this.poId = poId;
 	}
 
 	public String getPrdName() {
 		return prdName;
 	}
 
 	public void setPrdName(String prdName) {
 		this.prdName = prdName;
 	}
 	public String getBusinessType() {
 		return businessType;
 	}
 
 	public void setBusinessType(String businessType) {
 		this.businessType = businessType;
 	}
 
 	public Integer getPtNumber() {
 		return ptNumber;
 	}
 
 	public void setPtNumber(Integer ptNumber) {
 		this.ptNumber = ptNumber;
 	}
 
 	public String getOpUser() {
 		return opUser;
 	}
 
 	public void setOpUser(String opUser) {
 		this.opUser = opUser;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public String toString() {
		return this.getId() + ":" + this.getPoId() + ":" + this.getPtNumber() + ":" + this.getPrdName()
 				+ ":" + this.getDate();
 	}
 }
