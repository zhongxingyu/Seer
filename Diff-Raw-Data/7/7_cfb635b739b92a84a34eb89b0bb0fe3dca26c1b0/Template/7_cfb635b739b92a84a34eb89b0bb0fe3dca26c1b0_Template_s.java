 /***************************************************************************
     begin........: January 2012
     copyright....: Sebastian Fedrau
     email........: lord-kefir@arcor.de
  ***************************************************************************/
 
 /***************************************************************************
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
     General Public License for more details.
  ***************************************************************************/
 package accounting.application;
 
 import java.util.Date;
 import java.util.Calendar;
 
 import org.picocontainer.annotations.Inject;
 
 import accounting.Injection;
 import accounting.application.annotation.*;
 import accounting.data.IProvider;
 import accounting.data.ProviderException;
 
 @SuppressWarnings("unused")
 public class Template extends AEntity<Long> implements Comparable<Template>
 {
 	@Attribute(name="Name", readable=true, writeable=true)
 	@StringValidator(allowNull=false, minLength=0, maxLength=32)
 	private String name;
 	@Attribute(name="Category", readable=true, writeable=true)
 	@ObjectValidator(allowNull=false)
 	private Category category;
 	@Attribute(name="Amount", readable=true, writeable=true)
 	@DoubleValidator(allowNull=false, min=-Float.MAX_VALUE, max=Float.MAX_VALUE)
 	double amount;
 	@Attribute(name="Remarks", readable=true, writeable=true)
 	@StringValidator(allowNull=true, minLength=0, maxLength=512)
 	private String remarks;
 	@Attribute(name="Currency", readable=true, writeable=true)
 	@ObjectValidator(allowNull=false)
 	private Currency currency;
 
 	@Inject protected ExchangeUtil exchangeUtil;
 
 	@Override
 	protected void update() throws ProviderException
 	{
 		provider.updateTemplate(this);
 	}
 
 	@Override
 	protected void remove() throws ProviderException, ReferenceException
 	{
 		provider.deleteTemplate(getId());
 	}
 
 	@Override
 	public String toString()
 	{
 		return getName();
 	}
 
 	public String getName()
 	{
 		return getString("Name");
 	}
 
 	public void setName(String name) throws AttributeException
 	{
 		setAttribute("Name", name);
 	}
 
 	public Category getCategory()
 	{
 		return (Category)getObject("Category");
 	}
 
 	public void setCategory(Category category) throws AttributeException
 	{
 		if(this.category != null && category.isExpenditure() != this.category.isExpenditure())
 		{
 			amount *= -1;
 		}
 
 		setAttribute("Category", category);
 	}
 
 	public String getRemarks()
 	{
 		return getString("Remarks");
 	}
 
 	public void setRemarks(String remarks) throws AttributeException
 	{
 		setAttribute("Remarks", remarks);
 	}
 
 	public void setAmount(double amount) throws AttributeException
 	{
 		if(category != null)
 		{
 			setAttribute("Amount", category.isExpenditure() ? amount * -1 : amount);
 		}
 		else
 		{
 			setAttribute("Amount", amount);
 		}
 	}
 
 	public double getIncome()
 	{
 		if(!category.isExpenditure())
 		{
 			return amount;
 		}
 
 		return 0;
 	}
 
 	public double getRebate()
 	{
 		if(category.isExpenditure())
 		{
 			return Math.abs(amount);
 		}
 
 		return 0;
 	}
 	
 	public void setCurrency(Currency currency) throws AttributeException
 	{
 		setAttribute("Currency", currency);
 	}
 	
 	public Currency getCurrency()
 	{
 		try
 		{
 			return (Currency)getAttribute("Currency");
 		}
 		catch(AttributeException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	@Override
 	public int compareTo(Template template)
 	{
 		return getId().compareTo(template.getId());
 	}
 
 	public Transaction createTransaction(Account account) throws ProviderException
 	{
 		String remarks = this.remarks;
 		double amount;
 		
 		if(remarks.isEmpty())
 		{
 			remarks = name;
 		}
 
 		try
 		{
 			amount = exchangeUtil.exchange(currency, account.getCurrency(), this.amount);
 		}
 		catch(ExchangeRateUtilException e)
 		{
 			e.printStackTrace();
 			amount = this.amount;
 		}
 
		return provider.createTransaction(account, category, Calendar.getInstance().getTime(), amount, account.nextNo(), remarks);
 	}
 }
