 package com.seo.webapp;
 
 import java.util.*;
 import java.lang.*;
 
 public class Recommendation {
 
 	/*
 	 * Member variables 
 	 */
 	private ArrayList<SiteMetrics> m_sites;
 	private SiteMetrics m_compareSite;
 
 	private double m_avgKeywordPos;
 	private double m_avgNumKeyword;
 	
 	private double m_avgNumH1Tag;
 	private double m_avgNumH2Tag;
 	private double m_avgNumH3Tag;
 	private double m_avgNumH4Tag;
 	private double m_avgNumH5Tag;
 	private double m_avgNumH6Tag;
 	
 	private double m_avgKeywordH1Tag;
 	private double m_avgKeywordH2Tag;
 	private double m_avgKeywordH3Tag;
 	private double m_avgKeywordH4Tag;
 	private double m_avgKeywordH5Tag;
 	private double m_avgKeywordH6Tag;
 
 	private double m_percentKeywordExistsDesc;
 	private double m_percentKeywordExistsTitle;
 
 	// Constructor. Initializes variables
	public Recommendation(ArrayList<SiteMetrics> sites, String siteToCompare)
 	{
 		m_sites = sites;
 		for(int i = 0; i < m_sites.size(); i++)
 		{
			if(m_sites.get(i).getDomain().equalsIgnoreCase(siteToCompare))
 				m_compareSite = m_sites.get(i);
 		}
		
 		m_avgKeywordPos = 0;
 		m_avgNumKeyword = 0;
 		
 		m_avgNumH1Tag = 0;
 		m_avgNumH2Tag = 0;
 		m_avgNumH3Tag = 0;
 		m_avgNumH4Tag = 0;
 		m_avgNumH5Tag = 0;
 		m_avgNumH6Tag = 0;
 
 		m_avgKeywordH1Tag = 0;
 		m_avgKeywordH2Tag = 0;
 		m_avgKeywordH3Tag = 0;
 		m_avgKeywordH4Tag = 0;
 		m_avgKeywordH5Tag = 0;
 		m_avgKeywordH6Tag = 0;
 
 		m_percentKeywordExistsDesc = 0;
 		m_percentKeywordExistsTitle = 0;
 		
 	}
 
 	// Calculates the averages for each of the metrics gathered.
 	public void calculateDifferenceValues()
 	{
 		int numOtherSites = m_sites.size()-1;
 		for(int i = 0; i < numOtherSites+1; i++)
 		{
 			SiteMetrics currSite = m_sites.get(i);
 			
 			if(currSite != m_compareSite)
 			{	
 				m_avgKeywordPos += Double.parseDouble(currSite.getKeywordPos());
 				m_avgNumKeyword += Double.parseDouble(currSite.getNumKeyword());
 				
 				m_avgNumH1Tag += Double.parseDouble(currSite.getNumH1Tag());
 				m_avgNumH2Tag += Double.parseDouble(currSite.getNumH2Tag());
 				m_avgNumH3Tag += Double.parseDouble(currSite.getNumH3Tag());
 				m_avgNumH4Tag += Double.parseDouble(currSite.getNumH4Tag());
 				m_avgNumH5Tag += Double.parseDouble(currSite.getNumH5Tag());
 				m_avgNumH6Tag += Double.parseDouble(currSite.getNumH6Tag());
 
 				m_avgKeywordH1Tag += Double.parseDouble(currSite.getKeywordH1Tag());
 				m_avgKeywordH2Tag += Double.parseDouble(currSite.getKeywordH2Tag());
 				m_avgKeywordH3Tag += Double.parseDouble(currSite.getKeywordH3Tag());
 				m_avgKeywordH4Tag += Double.parseDouble(currSite.getKeywordH4Tag());
 				m_avgKeywordH5Tag += Double.parseDouble(currSite.getKeywordH5Tag());
 				m_avgKeywordH6Tag += Double.parseDouble(currSite.getKeywordH6Tag());
 
 				if(currSite.getKeywordExistsDesc().equalsIgnoreCase("true"))
 					m_percentKeywordExistsDesc += 1;
 				if(currSite.getKeywordExistsTitle().equalsIgnoreCase("true"))
 					m_percentKeywordExistsTitle += 1;
 			}
 		}
 		m_avgKeywordPos = Math.ceil(m_avgKeywordPos / numOtherSites); 
 		m_avgNumKeyword = Math.ceil(m_avgNumKeyword / numOtherSites);
 		
 		m_avgNumH1Tag = Math.ceil(m_avgNumH1Tag / numOtherSites);
 		m_avgNumH2Tag = Math.ceil(m_avgNumH2Tag / numOtherSites);
 		m_avgNumH3Tag = Math.ceil(m_avgNumH3Tag / numOtherSites);
 		m_avgNumH4Tag = Math.ceil(m_avgNumH4Tag / numOtherSites);
 		m_avgNumH5Tag = Math.ceil(m_avgNumH5Tag / numOtherSites);
 		m_avgNumH6Tag = Math.ceil(m_avgNumH6Tag / numOtherSites);
 
 		m_avgKeywordH1Tag = Math.ceil(m_avgKeywordH1Tag / numOtherSites);
 		m_avgKeywordH2Tag = Math.ceil(m_avgKeywordH2Tag / numOtherSites);
 		m_avgKeywordH3Tag = Math.ceil(m_avgKeywordH3Tag / numOtherSites);
 		m_avgKeywordH4Tag = Math.ceil(m_avgKeywordH4Tag / numOtherSites);
 		m_avgKeywordH5Tag = Math.ceil(m_avgKeywordH5Tag / numOtherSites);
 		m_avgKeywordH6Tag = Math.ceil(m_avgKeywordH6Tag / numOtherSites);
 
 		m_percentKeywordExistsDesc = Math.ceil(m_percentKeywordExistsDesc / numOtherSites);
 		m_percentKeywordExistsTitle = Math.ceil(m_percentKeywordExistsTitle / numOtherSites);
 	}
 
 	// Average keyword position recommendation
 	public String getKeywordPosRec()
 	{
 		if(m_avgKeywordPos < Double.parseDouble(m_compareSite.getKeywordPos()))
 			return "Have a keyword within " + m_avgKeywordPos + " positions from the top of the page";
 		else
 			return "The first keyword appears near the average position.";
 	}
 
 	// Average keyword number recommendation
 	public String getNumKeywordRec()
 	{
 		double numKeywordCompSite = Double.parseDouble(m_compareSite.getNumKeyword());
 		if(m_avgNumKeyword > numKeywordCompSite)
 			return "Add " + (m_avgNumKeyword - numKeywordCompSite) + " more keywords to the page.";
 		else
 			return "The number of keywords on the page match the average number.";
 	}
 
 	/*
 	 * Number of H# tag recommendations
 	 */
 	public String getNumH1TagRec()
 	{
 		double numTagCompSite = Double.parseDouble(m_compareSite.getNumH1Tag());
 		if(m_avgNumH1Tag > numTagCompSite)
 			return "Add " + (m_avgNumH1Tag - numTagCompSite) + " more H1 tags.";
 		else
 			return "The number of H1 tags are sufficient.";
 	}
 
 	public String getNumH2TagRec()
 	{
 		double numTagCompSite = Double.parseDouble(m_compareSite.getNumH2Tag());
 		if(m_avgNumH2Tag > numTagCompSite)
 			return "Add " + (m_avgNumH2Tag - numTagCompSite) + " more H2 tags.";
 		else
 			return "The number of H2 tags are sufficient.";
 	}
 
 	public String getNumH3TagRec()
 	{
 		double numTagCompSite = Double.parseDouble(m_compareSite.getNumH3Tag());
 		if(m_avgNumH3Tag > numTagCompSite)
 			return "Add " + (m_avgNumH3Tag - numTagCompSite) + " more H3 tags.";
 		else
 			return "The number of H3 tags are sufficient.";
 	}
 
 	public String getNumH4TagRec()
 	{
 		double numTagCompSite = Double.parseDouble(m_compareSite.getNumH4Tag());
 		if(m_avgNumH4Tag > numTagCompSite)
 			return "Add " + (m_avgNumH4Tag - numTagCompSite) + " more H4 tags.";
 		else
 			return "The number of H4 tags are sufficient.";
 	}
 
 	public String getNumH5TagRec()
 	{
 		double numTagCompSite = Double.parseDouble(m_compareSite.getNumH5Tag());
 		if(m_avgNumH5Tag > numTagCompSite)
 			return "Add " + (m_avgNumH5Tag - numTagCompSite) + " more H5 tags.";
 		else
 			return "The number of H5 tags are sufficient.";
 	}
 
 	public String getNumH6TagRec()
 	{
 		double numTagCompSite = Double.parseDouble(m_compareSite.getNumH6Tag());
 		if(m_avgNumH6Tag > numTagCompSite)
 			return "Add " + (m_avgNumH6Tag - numTagCompSite) + " more H6 tags.";
 		else
 			return "The number of H6 tags are sufficient.";
 	}
 
 	/*
 	 * Number of keywords in H# tags recommendations
 	 */
 	public String getKeywordH1TagRec()
 	{
 		double numKeywordCompSite = Double.parseDouble(m_compareSite.getKeywordH1Tag());
 		if(m_avgKeywordH1Tag > numKeywordCompSite)
 			return "Add " + (m_avgKeywordH1Tag - numKeywordCompSite) + " keywords to H1 tags.";
 		else
 			return "The number of keywords in H1 tags are sufficient.";
 	}
 
 	public String getKeywordH2TagRec()
 	{
 		double numKeywordCompSite = Double.parseDouble(m_compareSite.getKeywordH2Tag());
 		if(m_avgKeywordH2Tag > numKeywordCompSite)
 			return "Add " + (m_avgKeywordH2Tag - numKeywordCompSite) + " keywords to H2 tags.";
 		else
 			return "The number of keywords in H2 tags are sufficient.";
 	}
 
 	public String getKeywordH3TagRec()
 	{
 		double numKeywordCompSite = Double.parseDouble(m_compareSite.getKeywordH3Tag());
 		if(m_avgKeywordH3Tag > numKeywordCompSite)
 			return "Add " + (m_avgKeywordH3Tag - numKeywordCompSite) + " keywords to H3 tags.";
 		else
 			return "The number of keywords in H3 tags are sufficient.";
 	}
 
 	public String getKeywordH4TagRec()
 	{
 		double numKeywordCompSite = Double.parseDouble(m_compareSite.getKeywordH4Tag());
 		if(m_avgKeywordH4Tag > numKeywordCompSite)
 			return "Add " + (m_avgKeywordH4Tag - numKeywordCompSite) + " keywords to H4 tags.";
 		else
 			return "The number of keywords in H4 tags are sufficient.";
 	}
 
 	public String getKeywordH5TagRec()
 	{
 		double numKeywordCompSite = Double.parseDouble(m_compareSite.getKeywordH5Tag());
 		if(m_avgKeywordH5Tag > numKeywordCompSite)
 			return "Add " + (m_avgKeywordH5Tag - numKeywordCompSite) + " keywords to H5 tags.";
 		else
 			return "The number of keywords in H5 tags are sufficient.";
 	}
 
 	public String getKeywordH6TagRec()
 	{
 		double numKeywordCompSite = Double.parseDouble(m_compareSite.getKeywordH6Tag());
 		if(m_avgKeywordH6Tag > numKeywordCompSite)
 			return "Add " + (m_avgKeywordH6Tag - numKeywordCompSite) + " keywords to H6 tags.";
 		else
 			return "The number of keywords in H6 tags are sufficient.";
 	}
 	
 	// Keyword in description recommendation
 	public String getKeywordExistsDescRec()
 	{
 		String keywordExists = m_compareSite.getKeywordExistsDesc();
 		if(m_percentKeywordExistsDesc >= .5 && keywordExists.equalsIgnoreCase("false"))
 			return "The majority of the top sites have the keyword in the description. Please add some.";
 		else if(m_percentKeywordExistsDesc < .5 && keywordExists.equalsIgnoreCase("true"))
 			return "The majority of the top sites do not have the keyword in the description. Remove the keywords.";
 		else
 			return "The description follows format used by most of the top sites.";
 	}
 
 	// Keyword in title recommendation
 	public String getKeywordExistsTitleRec()
 	{
 		String keywordExists = m_compareSite.getKeywordExistsTitle();
 		if(m_percentKeywordExistsTitle >= .5 && keywordExists.equalsIgnoreCase("false"))
 			return "The majority of the top sites have the keyword in the title Please add some.";
 		else if(m_percentKeywordExistsTitle < .5 && keywordExists.equalsIgnoreCase("true"))
 			return "The majority of the top sites do not have the keyword in the title. Remove the keywords.";
 		else
 			return "The title follows format used by most of the top sites.";
 	}
 
 
}
