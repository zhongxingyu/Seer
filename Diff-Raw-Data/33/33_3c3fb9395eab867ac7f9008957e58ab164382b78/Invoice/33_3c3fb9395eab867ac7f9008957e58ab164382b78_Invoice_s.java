 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.softlysoftware.jxero.core;
 
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.List;
 import java.util.LinkedList;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 import java.text.NumberFormat;
 import java.text.DecimalFormat;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlSchemaType;
 
 @XmlRootElement(name = "Invoice")
 @XmlAccessorType(XmlAccessType.NONE)
 public class Invoice {
 
 	// TODO - Parse using the Xero Account's timezone
 	private static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T00:00:00'");
 	static {DF.setTimeZone(TimeZone.getTimeZone("UTC"));}
 
 	private static NumberFormat MONEY_FORMAT = new DecimalFormat("0.00");
 
 	public enum Type {ACCPAY, ACCREC};
 
 	@XmlElement(name = "Type")
 	private String type;
 	public Type getType() {
 		if (type == null) return null;
 		if (type.equals("ACCPAY")) return Type.ACCPAY;
 		if (type.equals("ACCREC")) return Type.ACCREC;
 		throw new RuntimeException("Bad Invoice type : " + type);
 	}
 	public void setType(Type type) {this.type = type.toString();}
 
 	@XmlElement(name = "Contact")
 	private Contact contact;
 	public Contact getContact(){return contact;}
 	public void setContact(Contact contact){this.contact = contact;}
 
 	@XmlElement(name = "Date")
 	private String date;
 	public Date getDate(){
 		try {return DF.parse(date);}
 		catch (ParseException e) {throw new RuntimeException(e);}
 	}
 	public void setDate(Date date){this.date = DF.format(date);}
 
 	@XmlElement(name = "DueDate")
 	private String dueDate;
 	public Date getDueDate(){
 		try {return DF.parse(dueDate);}
 		catch (ParseException e) {throw new RuntimeException(e);}
 	}
	public void setDueDate(Date dueDate){this.date = DF.format(dueDate);}
 
 	@XmlElement(name = "InvoiceNumber")
 	private String invoiceNumber;
 	public String getInvoiceNumber(){return invoiceNumber;}
 	public void setInvoiceNumber(String invoiceNumber){this.invoiceNumber = invoiceNumber;}
 
 	@XmlElement(name = "Reference")
 	private String reference;
 	public String getReference(){return reference;}
 	public void setReference(String reference){this.reference = reference;}
 
 	@XmlElement(name = "BrandingThemeID")
 	private String brandingThemeID;
 	public String getBrandingThemeID(){return brandingThemeID;}
 	public void setBrandingThemeID(String brandingThemeID){this.brandingThemeID = brandingThemeID;}
 
 	@XmlElement(name = "Url")
 	private String url;
 	public String getUrl(){return url;}
 	public void setUrl(String url){this.url = url;}
 
 	@XmlElement(name = "CurrencyCode")
 	private String currencyCode;
 	public String getCurrencyCode(){return currencyCode;}
 	public void setCurrencyCode(String currencyCode){this.currencyCode = currencyCode;}
 
 	public enum Status {DRAFT, SUBMITTED, AUTHORISED};
 
 	@XmlElement(name = "Status")
 	private String status;
 	public Status getStatus(){
 		if (status == null) return null;
 		if (status.equals("DRAFT")) return Status.DRAFT;
 		if (status.equals("SUBMITTED")) return Status.SUBMITTED;
 		if (status.equals("AUTHORISED")) return Status.AUTHORISED;
 		throw new RuntimeException("Bad status : " + status);
 	}
 	public void setStatus(Status status){this.status = status.toString();}
 
 	@XmlElement(name = "LineAmountTypes")
 	private String lineAmountTypes;
 	public String getLineAmountTypes(){return lineAmountTypes;}
 	public void setLineAmountTypes(String lineAmountTypes){this.lineAmountTypes = lineAmountTypes;}
 
 	@XmlElement(name = "SubTotal")
 	private String subTotal;
 	public double getSubTotal(){
 		try {return MONEY_FORMAT.parse(subTotal).doubleValue();}
 		catch (ParseException e) {throw new RuntimeException(e);}
 	}
 	public void setSubTotal(double subTotal){this.subTotal = MONEY_FORMAT.format(subTotal);} 
 
 	@XmlElement(name = "TotalTax")
 	private String totalTax;
 	public double getTotalTax(){
 		try {return MONEY_FORMAT.parse(totalTax).doubleValue();}
 		catch (ParseException e) {throw new RuntimeException(e);}
 	}
 	public void setTotalTax(double totalTax){this.totalTax = MONEY_FORMAT.format(totalTax);} 
 
 	@XmlElement(name = "Total")
 	private String total;
 	public double getTotal(){
 		try {return MONEY_FORMAT.parse(total).doubleValue();}
 		catch (ParseException e) {throw new RuntimeException(e);}
 	}
 	public void setTotal(double total){this.total = MONEY_FORMAT.format(total);}
 
 	@XmlElementWrapper(name = "LineItems")
 	@XmlElement(name = "LineItem")
 	private List<LineItem> lineItems = new LinkedList<LineItem>();
 	public List<LineItem> getLineItems(){return lineItems;}
 	public void setLineItems(List<LineItem> lineItems){this.lineItems = lineItems;}
 
 }
