 
 /*
  * Copyright (c) Members of the EGEE Collaboration. 2004.
  * See http://eu-egee.org/partners/ for details on the copyright holders.
  * For license conditions see the license file or http://eu-egee.org/license.html
  */
 
 package org.glite.wms.wmproxy.outputlist ;
 
 import org.glite.wms.wmproxy.WMProxyAPI ;
 import org.glite.wms.wmproxy.StringAndLongList ;
 import org.glite.wms.wmproxy.StringAndLongType ;
 
 
 /*
 	Test of  "getOutputFileList" method in org.glite.wms.wmproxy.WMProxyAPI
 
 */
 
 public class WMProxyOutputListTest {
 	/*
 	* Default constructor
 	*/
 	public WMProxyOutputListTest ( ) { }
 	/*
 	*	Starts the test
 	*	@param url service URL
 	*  	@param jobID the id to identify the job
 	*	@param proxyFile  the path location of the user proxy file
 	*	@throws.Exception if any error occurs
 	*/
 	private static void runTest ( String url, String proxyFile, String jobId ) throws java.lang.Exception {
 		StringAndLongList result = null;
 		StringAndLongType[ ] list = null;
 		int size = 0;
 		// Print out the input parameters
 		System.out.println ("TEST : OutputList");
 		System.out.println ("************************************************************************************************************************************");
 		System.out.println ("WS URL		= [" + url + "]" );
 		System.out.println ("--------------------------------------------------------------------------------------------------------------------------------");
 		System.out.println ("proxy		= [" + proxyFile + "]" );
 		System.out.println ("--------------------------------------------------------------------------------------------------------------------------------");
 		System.out.println ("JOB-ID		= [" + jobId + "]" );
 		System.out.println ("--------------------------------------------------------------------------------------------------------------------------------");
 		// Testing ...
 		WMProxyAPI client = new WMProxyAPI ( url, proxyFile ) ;
 		System.out.println ("Testing ....");
 		result = client.getOutputFileList(jobId);
 		System.out.println ("End of the test\n" );
 		// test results
 		System.out.println ("Result:");
 		System.out.println ("=======================================================================");
 		if ( result != null ) {
 			// list of files+their size
 			list = (StringAndLongType[ ] ) result.getFile ( );
 			if ( list != null ){
 				for (int i = 0; i < size ; i++){
 					System.out.println ("file n. " + (i+1) );
 					System.out.println ("--------------------------------------------");
 					System.out.println ("name		= [" + list[i].getName ( ) + "]" );
 					System.out.println ("size		= [" + list[i].getSize ( ) + "]" );
 				}
 			} else {
 				System.out.println ( "No output files for this job!");
 			}
 		} else {
 			System.out.println ( "NULLL!");
 		}
 		System.out.println("=======================================================================");
 	}
 	/*
 	* main
 	*/
 	public static void main(String[] args) throws Exception {
 		String url = "" ;
 		String jobId = "" ;
 		String proxyFile = "";
 		// Reads the input arguments
 		if ((args == null) || (args.length < 3))
 			throw new Exception ("error: some mandatory input parameters are missing (<WebServices URL> <proxyFile> <JobId> )");
 		url = args[0];
 		proxyFile = args[1];
 		jobId = args[2];
 		// Launches the test
 		runTest(url, proxyFile, jobId);
 	}
}
