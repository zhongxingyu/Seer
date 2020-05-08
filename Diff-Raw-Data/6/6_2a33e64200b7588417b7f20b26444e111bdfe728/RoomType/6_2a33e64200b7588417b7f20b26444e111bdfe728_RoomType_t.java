 package com.burda.scraper.model;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 
 public class RoomType
 {
 	public String name;
 	public String promoDesc;
 	public String bookItUrl;
 	public BigDecimal avgNightlyRate;
 	public BigDecimal avgNightlyOriginalRate;
 	public BigDecimal totalPrice;
 	public List<DailyRate> dailyRates = new ArrayList<DailyRate>();
 	
 	public String getName()
 	{
 		return name;
 	}
 
 	public String getPromoDesc()
 	{
 		return promoDesc;
 	}
 
 	public String getBookItUrl()
 	{
 		return bookItUrl;
 	}
 
 	public BigDecimal getAvgNightlyRate()
 	{
 		return avgNightlyRate;
 	}
 
 	public BigDecimal getAvgNightlyOriginalRate()
 	{
 		return avgNightlyOriginalRate;
 	}
 	
 	public BigDecimal getTotalPrice()
 	{
 		return totalPrice;
 	}
 
 	public List<DailyRate> getDailyRates()
 	{
 		return dailyRates;
 	}
 	
 	public boolean isPromoRate()
 	{
		boolean isPromoRate = false;
		if (avgNightlyOriginalRate != null)
			if (!getAvgNightlyRate().equals(getAvgNightlyOriginalRate()))
				isPromoRate = true;
		return isPromoRate;
 	}
 
 	public String toString()
 	{
 		return ToStringBuilder.reflectionToString(this);
 	}
 }
