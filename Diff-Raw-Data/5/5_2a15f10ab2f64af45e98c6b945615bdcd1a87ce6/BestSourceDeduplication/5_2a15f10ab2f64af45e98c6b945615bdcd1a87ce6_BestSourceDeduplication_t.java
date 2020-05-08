 /**
 *
 * Copyright 2002 - 2005 NCHELP
 *
 * Author:	Priority Technologies, Inc.
 *
 * This code is part of the Meteor system as defined and specified 
 * by the National Council of Higher Education Loan Programs, Inc. 
 * (NCHELP) and the Meteor Sponsors, and developed by Priority 
 * Technologies, Inc. (PTI). 
 *
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *	
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *	
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 ********************************************************************************/
 
 package org.nchelp.meteor.aggregation;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.NoSuchElementException;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.xpath.XPathAPI;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.nchelp.meteor.util.XMLDataTypes;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.ParsingException;
 
 
 /**
 * This is the class that aggregates all of the loans received from the data
 * providers. Those messages were in the form of MeteorDataResponse objects, but
 * they were stripped and parsed to provide this class with DOM Document
 * objects, instead.
 * 
 * @since Meteor1.0
 *
 */
 
 public class BestSourceDeduplication implements Serializable
 {
 	private static transient final Log log = LogFactory.getLog(BestSourceDeduplication.class);
 
 	// LoanList will contain all of the loan records being manipulated
 	// by this instance. Basically, this is organized as a group of
 	// buckets, each bucket containing all of those records that have
 	// been deemed to be duplicate info about one particular loan.
 	private LoanList theLoanList;
 
 	// internally, everything is looked up and stored
 	// by the hashcode() method of Object. This Hashtable
 	// is a cross reference between the APSUniqueAwardID
 	// and the Document.hashcode()
 	private Hashtable awardIdReference = new Hashtable();
 
 
 	public BestSourceDeduplication ()
 	{
 		theLoanList = new LoanList();
 	}
 
 	/**
 	 * Perform aggregation for the loans that have been added. This is simply the
 	 * process of looking among duplicate loan detail records for the "best
 	 * data" based on rules encoded from XML in the theBestSource object.
 	 * 
 	 * @return The result of this method is merely a reorganization of each set of
 	 * duplicates so that the "best" is now "first" in the set and can be
 	 * extracted by other means.
 	 */
 	synchronized public void aggregateLoans ()
 	{
 		for (int iter = theLoanList.numberOfDuplicateLists(); iter > 0; iter--)
 		{
 			arrangeBestOrder();
 		}
 	}
 
 	// First we dedupe the list, storing a hash boolean value with each loan
 	// determining if the loan was a duplicate
 
 	protected void arrangeBestOrder ()
 	{
 		Object[] duplLoans = null;
 		try
 		{
 			duplLoans = theLoanList.popFirstDuplicates();
 		}
 		catch (NoSuchElementException ex)
 		{
 			log.error("The list of duplicate loan lists is empty");
 			return;
 		}
 
 		DuplicateLoanDataVO bestSourceLoan = null;
 		DuplicateLoanDataVO firstDataVO = null;
 		DuplicateLoanDataVO nextDataVO = null;
 		String xmlValue = null;
 		String apsId1 = null;
 		String providerType1 = null;
 		String apsId2 = null;
 		String providerType2 = null;
 
 		for (int ndx = 1; ndx < duplLoans.length; ndx++)
 		{
 			firstDataVO = (DuplicateLoanDataVO)duplLoans[0];
 			nextDataVO = (DuplicateLoanDataVO)duplLoans[ndx];
 
 			apsId1 = firstDataVO.getAwardId();
 			providerType1 = firstDataVO.getProviderType();
 			apsId2 = nextDataVO.getAwardId();
 			providerType2 = nextDataVO.getProviderType();
 
 			if (apsId1 == null)
 			{
 				apsId1 = XMLParser.getNodeValue(firstDataVO.getDocument(), "APSUniqueAwardID");
 				firstDataVO.setAwardId(apsId1);
 			}
 
 			if (providerType1 == null)
 			{
 				providerType1 = XMLParser.getNodeValue(firstDataVO.getDocument(), "DataProviderType");
 				firstDataVO.setProviderType(providerType1);
 			}
 
 			if (apsId2 == null)
 			{
 				apsId2 = XMLParser.getNodeValue(nextDataVO.getDocument(), "APSUniqueAwardID");
 				nextDataVO.setAwardId(apsId2);
 			}
 
 			if (providerType2 == null)
 			{
 				providerType2 = XMLParser.getNodeValue(nextDataVO.getDocument(), "DataProviderType");
 				nextDataVO.setProviderType(providerType2);
 			}
 
 			if (log.isDebugEnabled())
 			{
 				log.debug("Comparing Award ID: " + apsId1 + " with provider type '" + providerType1 +
 				          "' to Award ID: " + apsId2 + " with provider type '" + providerType2 + "' ");
 			}
 
 			bestSourceLoan = determineBestSource(firstDataVO, nextDataVO);
 			if (bestSourceLoan != null && bestSourceLoan == nextDataVO)
 			{
 				// we need to rearrange things so that the "best" is at
 				// the "head" of the array (element 0).
 
 				DuplicateLoanDataVO best = (DuplicateLoanDataVO)duplLoans[ndx];
 				duplLoans[ndx] = duplLoans[0];
 				duplLoans[0] = best;
 
 				if (log.isDebugEnabled())
 				{
 					String apsId = nextDataVO.getAwardId();
 					String providerType = nextDataVO.getProviderType();
 					log.debug("Award ID: " + apsId + " with provider type '" + providerType + "' is now the 'best' source");
 				}
 			}
 			else
 			{
 				if (log.isDebugEnabled())
 				{
 					String apsId = firstDataVO.getAwardId();
 					String providerType = firstDataVO.getProviderType();
 					log.debug("Award ID: " + apsId + " with provider type '" + providerType + "' is still the 'best' source");
 				}
 			}
 		}
 		theLoanList.pushLastDuplicates(duplLoans);
 	}
 
 		/************************************************************************
 		 * PRE-GUARANTEE
 		 * 		IF
 		 * 			GuarDt == NULL
 		 * 			&&
 		 * 			loanDisbStatus == NOT DISBURSED
 		 * 		THEN
 		 * 			Preguarantee
 		 * 				Priority = LO, SBS, G, LRS
 		 * 		
 		 * PRE-DEFAULT
 		 * 		IF
 		 * 			GuarDt != null
 		 * 			||
 		 * 			loanDisbStatus == DISBURSED or PARTIALLY DISBURSED
 		 * 		THEN
 		 * 			IF
 		 * 				ClaimPaidDate != null
 		 * 			THEN
 		 * 				Goto DEFAULT
 		 * 			IF
 		 * 				loanDisbStatus == FULLY CANCELLED || UNDISBURSED || PARTIALLY DISBURSED
 		 * 			THEN
 		 * 				priority = LO, LRS, G, SBS
 		 * 			IF
 		 * 				loanDisbStatus == FULLY DISBURSED
 		 * 			THEN
 		 * 				priority = LRS, G, LO, SBS
 		 * 			
 		 * DEFAULT
 		 * 		IF
 		 * 			ClaimPdDate != null
 		 * 		THEN
 		 * 			priority = G, LRS, LO
 		 * 
 		 * 			
 		 * 		2. Pre-Default
 		 * 
 		 * 		The following fields are needed to determine this status.
 		 * 		Guarantee Date, Loan Disbursement Status, Claim Paid Date
 		 * 		If the Guarantee Date field is populated, or it?s not but
 		 * 		the 1st disbursement has been made (assumed under BCG), then
 		 * 		check the Claim Paid Date field. If the Claim Paid Date
 		 * 		field is not populated the loan is in the Pre-Default
 		 * 		Meteor Status Category and the following rules should be
 		 * 		applied to it (this assumes that the field is reported
 		 * 		without information if a claim has not been paid).
 		 * 			If Claim Paid Date field is populated, then the loan is
 		 * 		in the Default Meteor Status Category.
 		 * 		If the Loan Disbursement Status is not ?Fully Disbursed?
 		 * 		(includes the following three Loan Disbursement Statuses;
 		 * 		Fully Cancelled, Undisbursed, and Partially Disbursed),
 		 * 		then best source are first the LO, then the LRS, then
 		 * 		the G, and then the SBS.
 		 * 		If the Loan Disbursement Status is ?Fully Disbursed,?
 		 * 		then best source is first the LRS, then the G, then the
 		 * 		LO, and then the SBS.
 		 * 		If duplicate loans are reported with conflicting loan
 		 * 		disbursement statuses, then the software assumes that
 		 * 		the loan(s) possessing a higher stage of disbursement
 		 * 		(see Issue B-041) is better and does not use any loans
 		 * 		reported with an earlier loan disbursement status in
 		 * 		determining the best source data provider. For example,
 		 * 		if two duplicate loans are reported and the Meteor derived
 		 * 		Loan Disbursement Status is ?Partially Disbursed? for
 		 * 		Loan 1 from a ?LRS? and ?Fully Disbursed? for Loan 2 from
 		 * 		an ?G?, then Meteor looks at the Stage of Disbursement
 		 * 		outlined in Issue B-041 and determines that Loan 2 is
 		 * 		further along in the stage of the disbursement process
 		 * 		and should be used for determining best source. Therefore,
 		 * 		since Loan 2 is Fully Disbursed the Meteor software will
 		 * 		follow the rules above for Fully Disbursed loans which
 		 * 		says to pick 1st the LRS, then G, then LO, and then the
 		 * 		SBS. The best source would be the loan reported by
 		 * 		the ?G,? which is Loan 2.
 		 * 
 		 * 		3. Default
 		 * 
 		 * 		The following fields are needed to determine this status.
 		 * 		Claim Paid Date
 		 * 		If the Claim Paid Date is populated, then the loan is
 		 * 		determined to be in the Default Meteor Status Category
 		 * 		and the best source is first the G, then the LRS, and
 		 * 		then the LO.
 		 ************************************************************************/
 	protected DuplicateLoanDataVO determineBestSource (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String dataProviderType1 = loan1.getProviderType();
 		String dataProviderType2 = loan2.getProviderType();
 		if (dataProviderType1 == null)
 		{
 			dataProviderType1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/DataProviderType");
 			loan1.setProviderType(dataProviderType1);
 		}
 		if (dataProviderType2 == null)
 		{
 			dataProviderType2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/DataProviderType");
 			loan2.setProviderType(dataProviderType2);
 		}
 
 		String guarDate1 = loan1.getGuaranteeDate();
 		String guarDate2 = loan2.getGuaranteeDate();
 		if (guarDate1 == null)
 		{
 			guarDate1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/GuarDt");
 			loan1.setGuaranteeDate(guarDate1);
 		}
 		if (guarDate2 == null)
 		{
 			guarDate2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/GuarDt");
 			loan2.setGuaranteeDate(guarDate2);
 		}
 
 		String claimPaidDate1 = loan1.getClaimPaidDate();
 		String claimPaidDate2 = loan2.getClaimPaidDate();
 		if (claimPaidDate1 == null)
 		{
 			claimPaidDate1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/Default/ClaimPdDt");
 			loan1.setClaimPaidDate(claimPaidDate1);
 		}
 		if (claimPaidDate2 == null)
 		{
 			claimPaidDate2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/Default/ClaimPdDt");
 			loan2.setClaimPaidDate(claimPaidDate2);
 		}
 
 		String loanDisbStatus1 = loan1.getAwardStatus();
 		String loanDisbStatus2 = loan2.getAwardStatus();
 		try
 		{
 			if (loanDisbStatus1 == null)
 			{
 				loanDisbStatus1 = determineLoanDisbursementStatus(loan1);
 				loan1.setLoanDisbStatus(loanDisbStatus1);
 			}
 			if (loanDisbStatus2 == null)
 			{
 				loanDisbStatus2 = determineLoanDisbursementStatus(loan2);
 				loan2.setLoanDisbStatus(loanDisbStatus2);
 			}
 		}
 		catch (ParsingException e)
 		{
 			log.debug("Transforming Error", e);
 			return loan1;
 		}
 
 		String guarDate = guarDate1;
 		if ((guarDate1 == null || guarDate1.trim().length() <= 0 || guarDate1.equals(XMLDataTypes.BLANK_XML_DATE)) &&
 		     guarDate2 != null && guarDate2.trim().length() > 0 && !guarDate2.equals(XMLDataTypes.BLANK_XML_DATE))
 		{
 			guarDate = guarDate2;
 		}
 
 		String claimPaidDate = claimPaidDate1;
 		if ((claimPaidDate1 == null || claimPaidDate1.trim().length() <= 0 || claimPaidDate1.equals(XMLDataTypes.BLANK_XML_DATE)) &&
 		     claimPaidDate2 != null && claimPaidDate2.trim().length() > 0 && !claimPaidDate2.equals(XMLDataTypes.BLANK_XML_DATE))
 		{
 			claimPaidDate = claimPaidDate2;
 		}
 
 		String loanDisbStatus = "";
 		if (loanDisbStatus1.equals(AggregateConstants.UNDISBURSED) ||
 		    loanDisbStatus2.equals(AggregateConstants.UNDISBURSED))
 		{
 			loanDisbStatus = AggregateConstants.UNDISBURSED;
 		}
 
 		if (loanDisbStatus1.equals(AggregateConstants.PARTIALLY_DISBURSED) ||
 		    loanDisbStatus2.equals(AggregateConstants.PARTIALLY_DISBURSED))
 		{
 			loanDisbStatus = AggregateConstants.PARTIALLY_DISBURSED;
 		}
 
 		if (loanDisbStatus1.equals(AggregateConstants.FULLY_DISBURSED) ||
 		    loanDisbStatus2.equals(AggregateConstants.FULLY_DISBURSED))
 		{
 			loanDisbStatus = AggregateConstants.FULLY_DISBURSED;
 		}
 
 		if (loanDisbStatus1.equals(AggregateConstants.FULLY_CANCELLED) ||
 		    loanDisbStatus2.equals(AggregateConstants.FULLY_CANCELLED))
 		{
 			loanDisbStatus = AggregateConstants.FULLY_CANCELLED;
 		}
 
 		// Determine category
 
 		String[] priority = new String[4];
 
 		// Pre-Guarantee
 		if ((guarDate == null || guarDate.trim().length() == 0 || guarDate.equals(XMLDataTypes.BLANK_XML_DATE)) &&
 		     loanDisbStatus.equals(AggregateConstants.UNDISBURSED))
 		{
 			priority[0] = AggregateConstants.LENDER_ORIGINATOR;
 			priority[1] = AggregateConstants.SCHOOL_BASED_SOFTWARE;
 			priority[2] = AggregateConstants.GUARANTOR;
 			priority[3] = AggregateConstants.LENDER_SERVICER;
 		}
 		else
 		{
 			// Default
 			if (claimPaidDate != null && claimPaidDate.trim().length() > 0 && !claimPaidDate.equals(XMLDataTypes.BLANK_XML_DATE))
 			{
 				priority[0] = AggregateConstants.GUARANTOR;
 				priority[1] = AggregateConstants.LENDER_SERVICER;
 				priority[2] = AggregateConstants.LENDER_ORIGINATOR;
 				priority[3] = "";
 			}
 			else
 			{
 				// Pre-Default
 				if (loanDisbStatus.equals(AggregateConstants.FULLY_CANCELLED) ||
 				    loanDisbStatus.equals(AggregateConstants.UNDISBURSED) ||
 				    loanDisbStatus.equals(AggregateConstants.PARTIALLY_DISBURSED))
 				{
 					priority[0] = AggregateConstants.LENDER_ORIGINATOR;
 					priority[1] = AggregateConstants.LENDER_SERVICER;
 					priority[2] = AggregateConstants.GUARANTOR;
 					priority[3] = AggregateConstants.SCHOOL_BASED_SOFTWARE;
 				}
 				else
 				{
 					priority[0] = AggregateConstants.LENDER_SERVICER;
 					priority[1] = AggregateConstants.GUARANTOR;
 					priority[2] = AggregateConstants.LENDER_ORIGINATOR;
 					priority[3] = AggregateConstants.SCHOOL_BASED_SOFTWARE;
 				}
 			}
 		}
 
 		DuplicateLoanDataVO returnLoan = null;
 		for (int i = 0; i < priority.length; i++)
 		{
 			if (dataProviderType1 != null && dataProviderType1.equals(priority[i]))
 			{
 				returnLoan = loan1;
 				break;
 			}
 			if (dataProviderType2 != null && dataProviderType2.equals(priority[i]))
 			{
 				returnLoan = loan2;
 				break;
 			}
 		}
 
 		return returnLoan;
 	}
 
 	protected String determineLoanDisbursementStatus (DuplicateLoanDataVO loanVO) throws ParsingException
 	{
 		NodeList disbs;
 		Node disbNodes = null;
 
 		try
 		{
 			disbs = XPathAPI.selectNodeList(loanVO.getDocument(), "//Award/Disbursement");
 		}
 		catch (TransformerException e)
 		{
 			log.debug("Transforming Error", e);
 			throw new ParsingException("Unable to locate any elements matching the expression '//Award/Disbursement': " + e.getMessage());
 		}
 
 		String disbStatus = null;
 		int disbCancelled = 0;
 		int disbNotDisbursed = 0;
 		int disbDisbursed = 0;
 
 		try
 		{
 			for (int i = 0; i < disbs.getLength(); i++)
 			{
 				Node disbNode = disbs.item(i);
 				Node statCdNode = XPathAPI.selectSingleNode(disbNode, "DisbStatCd");
 				if (statCdNode == null)
 				{
 					continue;
 				}
 				Node textNode = statCdNode.getFirstChild();
 				if (textNode == null)
 				{
 					continue;
 				}
 				disbStatus = textNode.getNodeValue();
 
 				if (disbStatus != null)
 				{
 					if (disbStatus.equals(AggregateConstants.DISBURSED))
 					{
 						disbDisbursed++;
 					}
 					else
 					{
 						if (disbStatus.equals(AggregateConstants.CANCELLED))
 						{
 							disbCancelled++;
 						}
 						else
 						{
 							if (disbStatus.equals(AggregateConstants.ACTIVE))
 							{
 								disbNotDisbursed++;
 							}
 						}
 					}
 				}
 			}
 		}
 		catch (TransformerException e)
 		{
 			log.debug("Transforming Error", e);
 			throw new ParsingException("Unable to locate any elements matching the expression '//Award/DisbStatCd': " + e.getMessage());
 		}
 
 		String returnValue = null;
 		if (disbCancelled == disbs.getLength())
 		{
 			returnValue = AggregateConstants.FULLY_CANCELLED;
 		}
 		else
 		{
 			if ((disbDisbursed + disbCancelled) == disbs.getLength())
 			{
 				returnValue = AggregateConstants.FULLY_DISBURSED;
 			}
 			else
 			{
 				if (disbNotDisbursed == disbs.getLength())
 				{
 					returnValue = AggregateConstants.UNDISBURSED;
 				}
 				else
 				{
 					returnValue = AggregateConstants.PARTIALLY_DISBURSED;
 				}
 			}
 		}
 
 		return returnValue;
 	}
 
 	protected String dedupLoans (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		return applyTier0(loan1, loan2);
 	}
 
 	protected String applyTier0 (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String awardType1 = loan1.getAwardType();
 		String awardType2 = loan2.getAwardType();
 		boolean loan1Consol = false;
 		boolean loan2Consol = false;
 
 		if (awardType1 == null)
 		{
 			awardType1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/AwardType");
 			loan1.setAwardType(awardType1);
 		}
 
 		if (awardType2 == null)
 		{
 			awardType2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/AwardType");
 			loan2.setAwardType(awardType2);
 		}
 
 		if (awardType1 != null &&
 		   (awardType1.equalsIgnoreCase(AggregateConstants.CONSOL) ||
 		    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION) ||
 		    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_SUBSIDIZED) ||
 		    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_UNSUBSIDIZED) ||
 		    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_HEAL) ||
 		    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_OTHER)))
 		{
 			loan1Consol = true;
 		}
 
 		if (awardType2 != null &&
 		   (awardType2.equalsIgnoreCase(AggregateConstants.CONSOL) ||
 		    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION) ||
 		    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_SUBSIDIZED) ||
 		    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_UNSUBSIDIZED) ||
 		    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_HEAL) ||
 		    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_OTHER)))
 		{
 			loan2Consol = true;
 		}
 
 		if (loan1Consol && loan2Consol)
 		{
 			results = applyTier1(loan1, loan2);
 		}
 		else
 		{
 			String dataProviderType1 = loan1.getProviderType();
 			String dataProviderType2 = loan2.getProviderType();
 
 			if (dataProviderType1 == null)
 			{
 				dataProviderType1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/DataProviderType");
 				loan1.setProviderType(dataProviderType1);
 			}
 
 			if (dataProviderType2 == null)
 			{
 				dataProviderType2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/DataProviderType");
 				loan2.setProviderType(dataProviderType2);
 			}
 
 			if ((dataProviderType1 == null && dataProviderType2 == null) ||
 			    (dataProviderType1 != null && dataProviderType1.equals(dataProviderType2)))
 			{
 				results = applyTier0B(loan1, loan2);
 			}
 			else
 			{
 				results = applyTier1(loan1, loan2);
 			}
 		}
 
 		return results;
 	}
 
 	protected String applyTier0B (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String entityId1 = loan1.getProviderEntityId();
 		String entityId2 = loan2.getProviderEntityId();
 
 		if (entityId1 == null)
 		{
 			entityId1 = determineProviderId(loan1.getDocument());
 			loan1.setProviderEntityId(entityId1);
 		}
 
 		if (entityId2 == null)
 		{
 			entityId2 = determineProviderId(loan2.getDocument());
 			loan2.setProviderEntityId(entityId2);
 		}
 
 		if ((entityId1 == null && entityId2 == null) ||
 		    (entityId1 != null && entityId1.equals(entityId2)))
 		{
 			results = AggregateConstants.NON_DUPLICATE;
 		}
 		else
 		{
 			results = applyTier1(loan1, loan2);
 		}
 
 		return results;
 	}
 
 	protected String applyTier1 (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String awardType1 = loan1.getAwardType();
 		String awardType2 = loan2.getAwardType();
 
 		if (awardType1 == null)
 		{
 			awardType1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/AwardType");
 			loan1.setAwardType(awardType1);
 		}
 
 		if (awardType2 == null)
 		{
 			awardType2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/AwardType");
 			loan2.setAwardType(awardType2);
 		}
 
 		if (awardType1 != null &&
 		 (!(awardType1.equalsIgnoreCase(AggregateConstants.SUBSIDIZED)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.UNSUBSIDIZED)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.PLUS)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.SLS)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.ALTERNATIVE)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.HEAL)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.CONSOL)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_SUBSIDIZED)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_UNSUBSIDIZED)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_HEAL)) &&
 		  !(awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_OTHER))))
 		{
 			if (awardType2 != null &&
 			  (!awardType2.equalsIgnoreCase(AggregateConstants.SUBSIDIZED) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.UNSUBSIDIZED) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.PLUS) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.SLS) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.ALTERNATIVE) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.HEAL) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.CONSOL) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_SUBSIDIZED) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_UNSUBSIDIZED) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_HEAL) &&
 			   !awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_OTHER)))
 			{
 				return AggregateConstants.NON_DUPLICATE;
 			}
 		}
 
 		if ((awardType1 == null && awardType2 == null) || awardType1 != null && awardType1.equals(awardType2))
 		{
 			results = applyTier2(loan1, loan2);
 		}
 		else
 		{
 			boolean loan1Consol = false;
 			boolean loan2Consol = false;
 
 			if (awardType1 != null &&
 			   (awardType1.equalsIgnoreCase(AggregateConstants.CONSOL) ||
 			    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION) ||
 			    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_SUBSIDIZED) ||
 			    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_UNSUBSIDIZED) ||
 			    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_HEAL) ||
 			    awardType1.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_OTHER)))
 			{
 				loan1Consol = true;
 			}
 
 			if (awardType2 != null &&
 			   (awardType2.equalsIgnoreCase(AggregateConstants.CONSOL) ||
 			    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION) ||
 			    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_SUBSIDIZED) ||
 			    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_UNSUBSIDIZED) ||
 			    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_HEAL) ||
 			    awardType2.equalsIgnoreCase(AggregateConstants.CONSOLIDATION_OTHER)))
 			{
 				loan2Consol = true;
 			}
 
 			if (loan1Consol && loan2Consol)
 			{
 				results = applyTier1B(loan1, loan2);
 			}
 			else
 			{
 				results = AggregateConstants.NON_DUPLICATE;
 			}
 		}
 
 		return results;
 	}
 
 	protected String applyTier1B (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String actualDisbDate1 = loan1.getActualDisbDate();
 		String actualDisbDate2 = loan2.getActualDisbDate();
 
 		if (actualDisbDate1 == null)
 		{
 			actualDisbDate1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/Disbursement/ActualDisbDt");
 			loan1.setActualDisbDate(actualDisbDate1);
 		}
 
 		if (actualDisbDate2 == null)
 		{
 			actualDisbDate2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/Disbursement/ActualDisbDt");
 			loan2.setActualDisbDate(actualDisbDate2);
 		}
 
 		if (actualDisbDate1 != null && actualDisbDate1.trim().length() > 0 && !actualDisbDate1.equals(XMLDataTypes.BLANK_XML_DATE) &&
 		    actualDisbDate2 != null && actualDisbDate2.trim().length() > 0 && !actualDisbDate2.equals(XMLDataTypes.BLANK_XML_DATE))
 		{
 			if (actualDisbDate1.equals(actualDisbDate2))
 			{
 				results = AggregateConstants.DUPLICATE;
 			}
 			else
 			{
 				results = AggregateConstants.NON_DUPLICATE;
 			}
 		}
 		else
 		{
 			results = AggregateConstants.NON_DUPLICATE;
 		}
 
 		return results;
 	}
 
 	protected String applyTier2 (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String awardID1 = loan1.getAwardId();
 		String awardID2 = loan2.getAwardId();
 
 		if (awardID1 == null)
 		{
 			awardID1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/AwardId");
 			loan1.setAwardId(awardID1);
 		}
 
 		if (awardID2 == null)
 		{
 			awardID2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/AwardId");
 			loan2.setAwardId(awardID2);
 		}
 
 		if (awardID1 != null && awardID1.trim().length() > 0 &&
 		    awardID2 != null && awardID2.trim().length() > 0)
 		{
 			int size = awardID1.trim().length();
 			if (size > 17)
 			{
 				size = 17;
 			}
 
 			String CLUID1 = awardID1.substring(0, size);
 			size = awardID2.trim().length();
 
 			if (size > 17)
 			{
 				size = 17;
 			}
 
 			String CLUID2 = awardID2.substring(0, size);
 			if (CLUID1.equals(CLUID2))
 			{
 				return AggregateConstants.DUPLICATE;
 			}
 			else
 			{
 				return AggregateConstants.NON_DUPLICATE;
 			}
 		}
 		else
 		{
 			results = applyTier3(loan1, loan2);
 		}
 
 		return results;
 	}
 
 	protected String applyTier3 (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String guarDate1 = loan1.getGuaranteeDate();
 		String guarDate2 = loan2.getGuaranteeDate();
 
 		if (guarDate1 == null)
 		{
 			guarDate1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/GuarDt");
 			loan1.setGuaranteeDate(guarDate1);
 		}
 
 		if (guarDate2 == null)
 		{
 			guarDate2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/GuarDt");
 			loan2.setGuaranteeDate(guarDate2);
 		}
 
 		if (guarDate1 != null && guarDate1.trim().length() > 0 && !guarDate1.equals(XMLDataTypes.BLANK_XML_DATE) &&
 		    guarDate2 != null && guarDate2.trim().length() > 0 && !guarDate2.equals(XMLDataTypes.BLANK_XML_DATE))
 		{
 			if (guarDate1.equals(guarDate2))
 			{
 				results = applyTier3B(loan1, loan2);
 			}
 			else
 			{
 				results = AggregateConstants.NON_DUPLICATE;
 			}
 		}
 		else
 		{
 			results = applyTier4(loan1, loan2);
 		}
 
 		return results;
 	}
 
 	protected String applyTier3B (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String entityId1 = loan1.getSchoolEntityId();
 		String entityId2 = loan2.getSchoolEntityId();
 
 		if (entityId1 == null)
 		{
 			entityId1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/School/EntityID");
 			loan1.setSchoolEntityId(entityId1);
 		}
 
 		if (entityId2 == null)
 		{
 			entityId2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/School/EntityID");
 			loan2.setSchoolEntityId(entityId2);
 		}
 
 		if (entityId1 != null && entityId1.trim().length() > 0 &&
 		    entityId2 != null && entityId2.trim().length() > 0)
 		{
 			if (entityId1.equals(entityId2))
 			{
 				results = applyTier5(loan1, loan2);
 			}
 			else
 			{
 				results = AggregateConstants.NON_DUPLICATE;
 			}
 		}
 		else
 		{
 			results = applyTier4(loan1, loan2);
 		}
 
 		return results;
 	}
 
 	protected String applyTier4 (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 
 		String actualDisbDate1 = loan1.getActualDisbDate();
 		String actualDisbDate2 = loan2.getActualDisbDate();
 
 		if (actualDisbDate1 == null)
 		{
 			actualDisbDate1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/Disbursement/ActualDisbDt");
 			loan1.setActualDisbDate(actualDisbDate1);
 		}
 
 		if (actualDisbDate2 == null)
 		{
 			actualDisbDate2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/Disbursement/ActualDisbDt");
 			loan2.setActualDisbDate(actualDisbDate2);
 		}
 
 		if (actualDisbDate1 != null && actualDisbDate1.trim().length() > 0 && !actualDisbDate1.equals(XMLDataTypes.BLANK_XML_DATE) &&
 		    actualDisbDate2 != null && actualDisbDate2.trim().length() > 0 && !actualDisbDate2.equals(XMLDataTypes.BLANK_XML_DATE))
 		{
 			int oneDay = 24 * 60 * 60;
 			long difference = 0;
 			boolean overTenDays = false;
 			Date actualDate1 = null;
 			long actualTime1 = 0;
 			Date actualDate2 = null;
 			long actualTime2 = 0;
 
 			try
 			{
 				// Format the actual date
 				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 				actualDate1 = formatter.parse(actualDisbDate1);
 				actualTime1 = actualDate1.getTime();
 				actualDate2 = formatter.parse(actualDisbDate2);
 				actualTime2 = actualDate1.getTime();
 			}
 			catch (Exception e)
 			{
 				overTenDays = true;
 			}
 
 			if (actualTime1 > actualTime2)
 			{
 				difference = actualTime1 - actualTime2;
 			}
 			else
 			{
 				difference = actualTime2 - actualTime1;
 			}
 
 			if ((difference / oneDay) > 10)
 			{
 				overTenDays = true;
 			}
 
 			if (actualDisbDate1.equals(actualDisbDate2) || !overTenDays)
 			{
 				results = applyTier4B(loan1, loan2);
 			}
 			else
 			{
 				results = AggregateConstants.NON_DUPLICATE;
 			}
 		}
 		else
 		{
 			results = applyTier4C(loan1, loan2);
 		}
 
 		return results;
 	}
 
 	protected String applyTier4B (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String entityId1 = loan1.getSchoolEntityId();
 		String entityId2 = loan2.getSchoolEntityId();
 
 		if (entityId1 == null)
 		{
 			entityId1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/School/EntityID");
 			loan1.setSchoolEntityId(entityId1);
 		}
 
 		if (entityId2 == null)
 		{
 			entityId2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/School/EntityID");
 			loan2.setSchoolEntityId(entityId2);
 		}
 
 		if ((entityId1 != null && entityId1.trim().length() > 0) &&
 		    (entityId2 != null && entityId2.trim().length() > 0))
 		{
 			if (entityId1.equals(entityId2))
 			{
 				results = applyTier5(loan1, loan2);
 			}
 			else
 			{
 				results = AggregateConstants.NON_DUPLICATE;
 			}
 		}
 		else
 		{
 			results = applyTier5(loan1, loan2);
 		}
 
 		return results;
 	}
 
 	protected String applyTier4C (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String scheduledDisbDate1 = loan1.getSchedDisbDate();
 		String scheduledDisbDate2 = loan2.getSchedDisbDate();
 
 		if (scheduledDisbDate1 == null)
 		{
 			scheduledDisbDate1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/Disbursement/SchedDisbDt");
 			loan1.setActualDisbDate(scheduledDisbDate1);
 		}
 
 		if (scheduledDisbDate2 == null)
 		{
 			scheduledDisbDate2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/Disbursement/SchedDisbDt");
 			loan2.setSchedDisbDate(scheduledDisbDate2);
 		}
 
 		if (scheduledDisbDate1 != null && scheduledDisbDate1.trim().length() > 0 && !scheduledDisbDate1.equals(XMLDataTypes.BLANK_XML_DATE) &&
 		    scheduledDisbDate2 != null && scheduledDisbDate2.trim().length() > 0 && !scheduledDisbDate2.equals(XMLDataTypes.BLANK_XML_DATE))
 		{
 			int oneDay = 24 * 60 * 60;
 			long difference = 0;
 			boolean overTenDays = false;
 			Date scheduledDate1 = null;
 			long scheduledTime1 = 0;
 			Date scheduledDate2 = null;
 			long scheduledTime2 = 0;
 
 			try
 			{
 				// Format the actual date
 				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 				scheduledDate1 = formatter.parse(scheduledDisbDate1);
 				scheduledTime1 = scheduledDate1.getTime();
 				scheduledDate2 = formatter.parse(scheduledDisbDate2);
 				scheduledTime2 = scheduledDate1.getTime();
 			}
 			catch (Exception e)
 			{
 				overTenDays = true;
 			}
 
 			if (scheduledTime1 > scheduledTime2)
 			{
 				difference = scheduledTime1 - scheduledTime2;
 			}
 			else
 			{
 				difference = scheduledTime2 - scheduledTime1;
 			}
 
 			if ((difference / oneDay) > 10)
 			{
 				overTenDays = true;
 			}
 
 			if (scheduledDisbDate1.equals(scheduledDisbDate2) || !overTenDays)
 			{
 				results = applyTier4B(loan1, loan2);
 			}
 			else
 			{
 				results = AggregateConstants.NON_DUPLICATE;
 			}
 		}
 		else
 		{
 			results = AggregateConstants.NON_DUPLICATE;
 		}
 
 		return results;
 	}
 
 	protected String applyTier5 (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String awardType1 = loan1.getAwardType();
 		String awardType2 = loan2.getAwardType();
 
 		if (awardType1 == null)
 		{
 			awardType1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/AwardType");
 			loan1.setAwardType(awardType1);
 		}
 
 		if (awardType2 == null)
 		{
 			awardType2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/AwardType");
 			loan2.setAwardType(awardType2);
 		}
 
 		if ((awardType1 != null && awardType1.equalsIgnoreCase(AggregateConstants.PLUS)) ||
 		    (awardType2 != null && awardType1.equalsIgnoreCase(AggregateConstants.PLUS)))
 		{
 			results = applyTier5B(loan1, loan2);
 		}
 		else
 		{
 			results = AggregateConstants.DUPLICATE;
 		}
 
 		return results;
 	}
 
 	protected String applyTier5B (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String studentSsn1 = loan1.getStudentSSN();
 		String studentSsn2 = loan2.getStudentSSN();
 
 		if (studentSsn1 == null)
 		{
 			studentSsn1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/Student/SSNum");
 			loan1.setStudentSSN(studentSsn1);
 		}
 
 		if (studentSsn2 == null)
 		{
 			studentSsn2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/Student/SSNum");
 			loan2.setStudentSSN(studentSsn2);
 		}
 
 		if (studentSsn1 != null && studentSsn1.trim().length() > 0 &&
 		    studentSsn2 != null && studentSsn2.trim().length() > 0)
 		{
 			if (studentSsn1.equals(studentSsn2))
 			{
 				results = applyTier5C(loan1, loan2);
 			}
 			else
 			{
 				results = AggregateConstants.NON_DUPLICATE;
 			}
 		}
 		else
 		{
 			results = AggregateConstants.NON_DUPLICATE;
 		}
 
 		return results;
 	}
 
 	protected String applyTier5C (DuplicateLoanDataVO loan1, DuplicateLoanDataVO loan2)
 	{
 		String results = null;
 		String borrowerSsn1 = loan1.getBorrowerSSN();
 		String borrowerSsn2 = loan2.getBorrowerSSN();
 
 		if (borrowerSsn1 == null)
 		{
 			borrowerSsn1 = XMLParser.getNodeValue(loan1.getDocument(), "/Award/Borrower/SSNum");
			loan1.setBorrowerSSN(borrowerSsn1);
 		}
 
 		if (borrowerSsn2 == null)
 		{
 			borrowerSsn2 = XMLParser.getNodeValue(loan2.getDocument(), "/Award/Borrower/SSNum");
			loan2.setBorrowerSSN(borrowerSsn2);
 		}
 
 		if (borrowerSsn1 != null && borrowerSsn1.trim().length() > 0 &&
 		    borrowerSsn2 != null && borrowerSsn2.trim().length() > 0)
 		{
 			if (borrowerSsn1.equals(borrowerSsn2))
 			{
 				results = AggregateConstants.DUPLICATE;
 			}
 			else
 			{
 				results = AggregateConstants.NON_DUPLICATE;
 			}
 		}
 		else
 		{
 			results = AggregateConstants.NON_DUPLICATE;
 		}
 
 		return results;
 	}
 
 	protected String determineProviderId (Document document)
 	{
 		String providerId = null;
 		String providerType = XMLParser.getNodeValue(document, "/Award/DataProviderType");
 
 		if (providerType.equals(AggregateConstants.GUARANTOR))
 		{
 			providerId = XMLParser.getNodeValue(document, "/Award/Guarantor/EntityID");
 		}
 		else
 		{
 			if (providerType.equals(AggregateConstants.LENDER_ORIGINATOR))
 			{
 				providerId = XMLParser.getNodeValue(document, "/Award/Lender/EntityID");
 			}
 			else
 			{
 				if (providerType.equals(AggregateConstants.LENDER_SERVICER))
 				{
 					providerId = XMLParser.getNodeValue(document, "/Award/Servicer/EntityID");
 				}
 				else
 				{
 					if (providerType.equals(AggregateConstants.SCHOOL_BASED_SOFTWARE))
 					{
 						providerId = XMLParser.getNodeValue(document, "/Award/School/EntityID");
 					}
 				}
 			}
 		}
 
 		return providerId;
 	}
 
 	/**
 	 * Add a Loan Detail Record (as a DOM document) to the list of records. The
 	 * record will be examined to determine whether it is a duplicate of an
 	 * existing loan detail.
 	 * 
 	 * @param newLoan
 	 * is a DOM Document representation of the loan detail record.
 	 */
 	public void add (Document inLoan)
 	{
 		DuplicateLoanDataVO existingLoan;
 		DuplicateLoanDataVO newLoan = new DuplicateLoanDataVO();
 		String results = null;
 
 		newLoan.setDocument(inLoan);
 
 		String key = XMLParser.getNodeValue(newLoan.getDocument(), "APSUniqueAwardID");
 		if (key == null)
 		{
 			key = "0";
 		}
 
 		Integer intKey = new Integer(key);
 		while (awardIdReference.containsKey(intKey))
 		{
 			intKey = new Integer(intKey.intValue() + 1);
 		}
 		awardIdReference.put(intKey, new Integer(newLoan.hashCode()));
 
 		String dataProviderType = XMLParser.getNodeValue(newLoan.getDocument(), "/Award/DataProviderType");
 		newLoan.setProviderType(dataProviderType);
 
 		theLoanList.reset();
 		while (theLoanList.hasNext())
 		{
 			existingLoan = (DuplicateLoanDataVO)theLoanList.next();
 			results = dedupLoans(newLoan, existingLoan);
 
 			if (results != null && results.equals(AggregateConstants.DUPLICATE))
 			{
 				log.debug("Adding a duplicate loan");
 				theLoanList.addDuplicate(existingLoan, newLoan);
 				return;
 			}
 		}
 		theLoanList.add(newLoan);
 	}
 
 	/**
 	 * If aggregateLoans has been called previously and since additional loan
 	 * detail records have been added to the list, then this method will return an
 	 * array containing the "best data" for each loan.
 	 * 
 	 * @return an array of Object references. Elements can be safely downcast to
 	 * DOM document references.
 	 */
 	public Object[] getBest ()
 	{
 		return theLoanList.getHeaders();
 	}
 
 	/**
 	 * If aggregateLoans has been called previously and since additional loan
 	 * detail records have been added to the list, then this method will return
 	 * the "best data" record for the set of duplicate loans containing the record
 	 * specified by the unique hashCode.
 	 * 
 	 * @param hashCode
 	 * is the result of invoking hashCode() on an object instance.
 	 * @throws NoSuchElementException
 	 * if the hashCode does not reference a contained loan detail
 	 * record.
 	 */
 	public Document getBest (int awardID)
 	{
 		Integer hashCode = (Integer)awardIdReference.get(new Integer(awardID));
 		Object[] dupls = theLoanList.getDupls(hashCode.intValue());
 		DuplicateLoanDataVO dataVO = (DuplicateLoanDataVO)dupls[0];
 		return dataVO.getDocument();
 	}
 
 	/**
 	 * Returns an array of Object references for each record in the set of
 	 * duplicate loan detail records that includes the record specified by the
 	 * unique hashCode.
 	 * 
 	 * @param hashCode
 	 * is the result of invoking hashCode() on an object instance.
 	 * @throws NoSuchElementException
 	 * if the hashCode does not reference a contained loan detail record.
 	 * @return an array of Object references. Elements can be safely downcast to
 	 * DOM document references.
 	 */
 	public Object[] getDuplicates (int awardID)
 	{
 		Integer hashCode = (Integer)awardIdReference.get(new Integer(awardID));
 		if (hashCode == null)
 		{
 			return null;
 		}
 
 		Object[] retVal = theLoanList.getDupls(hashCode.intValue());
 
 		return retVal;
 	}
 
 	/**
 	 * Provides an array of DOM Document references to each loan detail record
 	 * that has been added and not removed.
 	 */
 	synchronized public Document[] get ()
 	{
 		int ndx = 0;
 		Document[] rslt = new Document[theLoanList.numberOfRecords()];
 		DuplicateLoanDataVO dataVO = null;
 
 		theLoanList.reset();
 		while (theLoanList.hasNext())
 		{
 			dataVO = (DuplicateLoanDataVO)theLoanList.next();
 			rslt[ndx++] = (Document)dataVO.getDocument();
 		}
 
 		return rslt;
 	}
 
 	/**
 	 * This method is used to locate a specific DOM Document instance based on its
 	 * unique hashCode.
 	 * 
 	 * @param hashCode
 	 * is the result of invoking hashCode() on an object instance.
 	 * @throws NoSuchElementException
 	 * if the hashCode does not reference a contained loan detail
 	 * record.
 	 */
 	public Document get (int awardID) throws NoSuchElementException
 	{
 		Integer hashCode = (Integer)awardIdReference.get(new Integer(awardID));
 		if (hashCode != null)
 		{
 			DuplicateLoanDataVO dataVO = null;
 			Object[] dupls = theLoanList.getDupls(hashCode.intValue());
 
 			for (int ndx = 0; ndx < dupls.length; ndx++)
 			{
 				if (dupls[ndx].hashCode() == hashCode.intValue())
 				{
 					dataVO = (DuplicateLoanDataVO)dupls[ndx];
 					return (Document)dataVO.getDocument();
 				}
 			}
 			throw new NoSuchElementException();
 		}
 
 		return null;
 	}
 
 	/**
 	 * TEST HARNESS
 	 */
 
 	public String toString ()
 	{
 		return theLoanList.toString();
 	}
 
 }
 
