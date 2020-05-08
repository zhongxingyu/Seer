 package me.lutea.iswho;
 
 import java.util.List;
 
 import me.lutea.iswho.intface.IData;
 import me.lutea.iswho.intface.ILog;
 import me.lutea.iswho.intface.IParse;
 import me.lutea.iswho.intface.IQuery;
 import me.lutea.iswho.intface.ILog.LEVEL;
 
 public class ISWHO {
 	public static final String	REGEXP_SLD_IDN	= "(xn--)?[a-z0-9](([a-z0-9-]+)?[a-z0-9])?";
 	public static final String	REGEXP_TLD		= "(\\.[a-z]{2,10})(\\.[a-z]{2,3})?";
 	public static final String	REGEXP_DOMAIN	= REGEXP_SLD_IDN + REGEXP_TLD;
 
 	private static ILog			iLog;
 	private static IData		iData;
 	private static IQuery		iQuery;
 	private static IParse		iParse;
 	private String				domain;
 	private WhoisMap			whoisMap;
 
 	public ISWHO(String domain) {
 		if (null != domain && !"".equals( domain.trim() ))
 			this.domain = domain.trim().toLowerCase();
 	}
 
 	public WhoisMap build() {
 		getLog().log( LEVEL.DEBUG, "[" + domain + "] ISWHO query start..." );
 
 		if (!validDomain( domain )) {
			getLog().log( LEVEL.ERROR, "[" + domain + "] not a valid domain name (or not an ASCII IDN domain name)" );
 			return null;
 		}
 
 		if (null == iQuery)
 			iQuery = new IWQuery();
 
 		if (null == iParse)
 			iParse = new IWParse();
 
 		whoisMap = new WhoisMap( domain );
 
 		// 1. get the query server
 		getLog().log( LEVEL.DEBUG, "[" + domain + "] get the query server..." );
 		WhoisServer server = getData().getServer( domain );
 		if (null == server) {
 			getLog().log( LEVEL.ERROR, "[" + domain + "] can not get server" );
			return whoisMap;
 		}
 
 		whoisMap.addWhoisServers( server.toString() );
 
 		// 2. look up and get the raw data
 		getLog().log( LEVEL.DEBUG, "[" + domain + "] get the raw data from <" + server + ">..." );
 		List<String> rawData = iQuery.query( domain, server );
 		if (null == rawData || rawData.isEmpty()) {
 			getLog().log( LEVEL.ERROR, "[" + domain + "] can not get rawdata from <" + server + ">" );
			return whoisMap;
 		}
 		else {
 			getLog().log( LEVEL.DEBUG, "[" + domain + "] rawdata from <" + server + ">", rawData );
 		}
 
 		whoisMap.setRawData( rawData );
 
 		// 3. parse the raw data into WhoisMap
 		getLog().log( LEVEL.DEBUG, "[" + domain + "] parse the raw data into WhoisMap..." );
 		WhoisServer deepServer = iParse.parse( whoisMap, rawData );
 
 		// 4. check if has a deep server and do deep query and parse
 		getLog().log( LEVEL.DEBUG, "[" + domain + "] check if has a deep server and do deep query and parse..." );
 		if (null != deepServer) {
 			getLog().log( LEVEL.DEBUG,
 					"[" + domain + "] yes, deep server exist, get the raw data from <" + server + ">..." );
 
 			whoisMap.addWhoisServers( deepServer.toString() );
 
 			rawData = iQuery.query( domain, deepServer );
 			if (null == rawData || rawData.isEmpty()) {
 				getLog().log( LEVEL.ERROR, "[" + domain + "] can not get rawdata from <" + deepServer + ">" );
 				return whoisMap;
 			}
 			else {
 				getLog().log( LEVEL.DEBUG, "[" + domain + "] rawdata from <" + deepServer + ">", rawData );
 			}
 
 			whoisMap.setRawData( rawData );
 
 			getLog().log( LEVEL.DEBUG, "[" + domain + "] parse the raw data into WhoisMap again..." );
 			iParse.parse( whoisMap, rawData );
 		}
 		else {
 			getLog().log( LEVEL.DEBUG, "[" + domain + "] no deep server exist" );
 		}
 
 		getLog().log( LEVEL.DEBUG, "[" + domain + "] ISWHO query done." );
 		return whoisMap;
 	}
 
 	public static IData getData() {
 		if (null == iData)
 			iData = new IWData();
 		return iData;
 	}
 
 	public static ILog getLog() {
 		if (null == iLog) {
 			iLog = new ILog() {
 				@Override
 				public void log(LEVEL lv, String info) {
 					System.out.println( lv + ":" + info );
 				}
 
 				@Override
 				public void log(LEVEL lv, String info, Exception e) {
 					System.out.println( lv + ":" + info + " - " + e.getMessage() );
 					e.printStackTrace();
 				}
 
 				@Override
 				public void log(LEVEL lv, String info, List<String> rawdata) {
 					System.out.println( lv + ":" + info + "\n" );
 					for (String line : rawdata)
 						System.out.println( line );
 					System.out.println();
 				}
 			};
 		}
 		return iLog;
 	}
 
 	public static void setIData(IData iData) {
 		ISWHO.iData = iData;
 	}
 
 	public static void setIQuery(IQuery iQuery) {
 		ISWHO.iQuery = iQuery;
 	}
 
 	public static void setIParse(IParse iParse) {
 		ISWHO.iParse = iParse;
 	}
 
 	public static void setILog(ILog iLog) {
 		ISWHO.iLog = iLog;
 	}
 
 	public static boolean validDomain(String domain) {
 		return ((null != domain) && domain.matches( REGEXP_DOMAIN ));
 	}
 }
