 package org.nchelp.meteor.provider.access;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.nchelp.meteor.aggregation.AggregatedLoanData;
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.message.MeteorDataResponse;
 import org.nchelp.meteor.message.MeteorIndexResponse;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.IndexProvider;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.registry.DistributedRegistry;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.Cache;
 import org.nchelp.meteor.util.ProviderError;
 import org.nchelp.meteor.util.exception.DataException;
 import org.nchelp.meteor.util.exception.IndexException;
 
 public class IndexQueryService {
 
 	private final Logger log = Logger.create(this.getClass());
 
 	private static Cache iProviderCache = new Cache();
 	private static Cache dProviderCache = new Cache();
 	private AggregatedLoanData aggregatedData = new AggregatedLoanData();
 	private ProviderError errors = new ProviderError();
 
 	/**
 	 * Main method for Meteor queries.  This will return an XML document
 	 * that represents the summary information
 	 * @param ssn Social Security Number
 	 * @param dob Date Of Birth for this SSN
 	 * @return String
 	 */
 	public String query(String ssn, Date dob) {
 		MeteorParameters params = new MeteorParameters();
 		params.setSsn(ssn);
 		params.setDob(dob);
 	
 		return this.query(params);
 	}
 	
 	public String query(MeteorParameters params){
 		// check dataProviderCache
 		// if no providers in cache should 
 		// look for index providers in index provider cache
 		// if not found should  look up and cache 
 
 		List dProviders = null;
 		List iProviders = null;
 
 		DistributedRegistry registry = DistributedRegistry.singleton();
 		SecurityToken token = registry.getAuthentication();
 
 		dProviders = (List)dProviderCache.cached(params.getSsn());
 
 		if (dProviders.isEmpty()) {
 
 			iProviders = (List)iProviderCache.cached("");
 
 			if (iProviders.isEmpty()) {
 
 				iProviders = registry.getIndexProviders();
 				iProviderCache.add("", iProviders);
 			}
 
 			// now request a list of data providers from each index provider.
 			Iterator iterator = iProviders.iterator();
 			IndexProvider iProvider = null;
 
 			while (iterator.hasNext()) {
 				
 				iProvider = (IndexProvider) iterator.next();
 				MeteorIndexResponse ipResp = null;
 				try{
 					log.assert(iProvider != null, "Ack! the IndexProvider object is null");
 					ipResp = iProvider.getDataProviders(token, params);
 				} catch(IndexException e){
 					//TODO!!! THIS IS A NASTY HACK!!!
 					errors.setError((MeteorIndexResponse)null, e);
 				}
				dProviders = this.aggregateList(dProviders, ipResp.getDataProviderList());
 			}
 			
 			// Now interate through each of these and make the real call
 			iterator = dProviders.iterator();
 			DataProvider dProvider = null;
 			
 			while(iterator.hasNext()){
 				dProvider = (DataProvider) iterator.next();
 				
 				MeteorDataResponse mdr = null;
 				try{
 					mdr = dProvider.getData(token, params);
 					
 					this.aggregatedData.add(mdr);
 					
 				} catch(DataException e){
 					errors.setError(mdr, e);
 				}
 			}
 			
 		}
 		
 		return aggregatedData.toXML();
 	}
 	
 	/**
 	 * View loan detail information
 	 * @param loanID 
 	 * @return String
 	 */
 	public String viewDetail(String loanID){
 		return aggregatedData.toXML(loanID);
 	}
 	
 	/**
 	 * As each of the calls to AccessProvider.getDataProviders() returns
 	 * call this method to eliminate any of the duplicate Data
 	 * Providers
 	 * @param dataProviders
 	 * @return List
 	 */
 	private List aggregateList(List dataProviders, List newDataProviders){
 		// easiest way to do this is to cast this to a Set and add them then turn it back into a List
 
 		if(newDataProviders == null){
 			return dataProviders;
 		}
 		
 		if(dataProviders == null){
 			dataProviders = new Vector();
 		}
 		dataProviders.addAll(newDataProviders);
 		return dataProviders;
 	}
 
 }
