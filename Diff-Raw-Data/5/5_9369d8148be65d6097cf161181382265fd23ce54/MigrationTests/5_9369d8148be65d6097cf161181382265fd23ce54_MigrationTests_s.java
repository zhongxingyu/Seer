 package rhsm.cli.tests;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.bugzilla.BlockedByBzBug;
 import com.redhat.qe.auto.bugzilla.BzChecker;
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import rhsm.base.CandlepinType;
 import rhsm.base.SubscriptionManagerCLITestScript;
 import rhsm.cli.tasks.CandlepinTasks;
 import rhsm.data.ProductCert;
 import rhsm.data.ProductSubscription;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 /**
  * @author jsefler
  *
  *	References:
  *		http://documentation-stage.bne.redhat.com/docs/en-US/Red_Hat_Enterprise_Linux/5/html/Deployment_Guide/rhn-migration.html
  *		https://engineering.redhat.com/trac/PBUPM/browser/trunk/documents/Releases/RHEL6/Variants/RHEL6-Variants.rst
  *		http://linuxczar.net/articles/rhel-installation-numbers
  *		https://docspace.corp.redhat.com/docs/DOC-71135 (PRODUCT CERTS)
  *		https://engineering.redhat.com/trac/rcm/wiki/Projects/CDNBaseline
  *
  *	// OLD LOCATION
  *	git clone git://git.app.eng.bos.redhat.com/rcm/rhn-definitions.git
  *  http://git.app.eng.bos.redhat.com/?p=rcm/rhn-definitions.git;a=tree
  *  
  *  git clone git://git.app.eng.bos.redhat.com/rcm/rcm-metadata.git
  *  http://git.app.eng.bos.redhat.com/?p=rcm/rcm-metadata.git;a=tree
  *  
  *  product 150 is at
  *  http://git.app.eng.bos.redhat.com/?p=rcm/rhn-definitions.git;a=tree;f=product_ids/rhev-3.0;hb=HEAD
  *
  */
 @Test(groups={"MigrationTests"})
 public class MigrationTests extends SubscriptionManagerCLITestScript {
 
 	// Test methods ***********************************************************************
 	
 	@Test(	description="Verify that the channel-cert-mapping.txt exists",
 			groups={"AcceptanceTests"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyChannelCertMappingFileExists_Test() {
 		Assert.assertTrue(RemoteFileTasks.testExists(client, channelCertMappingFilename),"The expected channel cert mapping file '"+channelCertMappingFilename+"' exists.");
 	}
 	
 	
 	@Test(	description="Verify that the channel-cert-mapping.txt contains a unique map of channels to product certs",
 			groups={"AcceptanceTests"},
 			dependsOnMethods={"VerifyChannelCertMappingFileExists_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyChannelCertMapping_Test() throws FileNotFoundException, IOException {
 		Assert.assertTrue(RemoteFileTasks.testExists(client, channelCertMappingFilename),"The expected channel cert mapping file '"+channelCertMappingFilename+"' exists.");
 		
 		// Read the channelCertMappingFilename as if they were properties (Warning! this will mask non-unique mappings)
 		// [root@jsefler-onprem-5client ~]# cat /usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt
 		// rhn-tools-rhel-x86_64-server-5-beta: none
 		// rhn-tools-rhel-x86_64-server-5: Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem
 		// rhn-tools-rhel-x86_64-client-5-beta: none
 		// rhn-tools-rhel-x86_64-client-5: Client-Client-x86_64-efe91c1c-78d7-4d19-b2fb-3c88cfc2da35-68.pem
 		SSHCommandResult result = client.runCommandAndWait("cat "+channelCertMappingFilename);
 		Properties p = new Properties();
 		p.load(new ByteArrayInputStream(result.getStdout().getBytes("UTF-8")));
 		for (Object key: p.keySet()){
 			// load the channelsToProductCertFilesMap
 			channelsToProductCertFilenamesMap.put((String)key, p.getProperty((String)(key)));
 			// load the mappedProductCertFiles
 			if (!channelsToProductCertFilenamesMap.get(key).equalsIgnoreCase("none"))
 				mappedProductCertFilenames.add(channelsToProductCertFilenamesMap.get(key));
 		}
 		
 		// Read the channelCertMappingFilename line by line asserting unique mappings
 		boolean uniqueChannelsToProductCertFilenamesMap = true;
 		for (String line: result.getStdout().trim().split("\\n")){
 			if (line.trim().equals("")) continue; // skip blank lines
 			if (line.trim().startsWith("#")) continue; // skip comments
 			String channel = line.split(":")[0].trim();
 			String productCertFilename = line.split(":")[1].trim();
 			if (channelsToProductCertFilenamesMap.containsKey(channel)) {
 				if (!channelsToProductCertFilenamesMap.get(channel).equals(productCertFilename)) {
 					log.warning("RHN Channel '"+channel+"' is already mapped to productFilename '"+productCertFilename+"' while parsing "+channelCertMappingFilename+" line: "+line);
 					uniqueChannelsToProductCertFilenamesMap = false;
 				}
 			} else {
 				Assert.fail("Having trouble parsing the following channel:product map from "+channelCertMappingFilename+": "+line);
 			}
 		}
 		Assert.assertTrue(uniqueChannelsToProductCertFilenamesMap, "Each channel in "+channelCertMappingFilename+" maps to a unique product cert filename. (See above warnings for offenders.)");
 	}
 	
 	
 	@Test(	description="Verify that all product cert files mapped in channel-cert-mapping.txt exist",
 			groups={"AcceptanceTests","blockedByBug-771615"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyAllMappedProductCertFilesExists_Test() {
 
 		boolean allMappedProductCertFilesExist = true;
 		for (String mappedProductCertFilename : mappedProductCertFilenames) {
 			String mappedProductCertFile = baseProductsDir+"/"+mappedProductCertFilename;
 			if (RemoteFileTasks.testExists(client, mappedProductCertFile)) {
 				log.info("Mapped productCert file '"+mappedProductCertFile+"' exists.");		
 			} else {
 				log.warning("Mapped productCert file '"+mappedProductCertFile+"' does NOT exist.");
 				allMappedProductCertFilesExist = false;
 			}
 		}
 		Assert.assertTrue(allMappedProductCertFilesExist,"All of the productCert files mapped in '"+channelCertMappingFilename+"' exist.");
 	}
 	
 	
 	@Test(	description="Verify that all existing product cert files are mapped in channel-cert-mapping.txt",
 			groups={"AcceptanceTests","blockedByBug-799103","blockedByBug-849274"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyAllExistingProductCertFilesAreMapped_Test() {
 		
 		// get a list of all the existing product cert files
 		SSHCommandResult result = client.runCommandAndWait("ls "+baseProductsDir+"/*.pem");
 		List<String> existingProductCertFiles = Arrays.asList(result.getStdout().split("\\n"));
 		boolean allExitingProductCertFilesAreMapped = true;
 		for (String existingProductCertFile : existingProductCertFiles) {
 			if (mappedProductCertFilenames.contains(new File(existingProductCertFile).getName())) {
 				log.info("Existing productCert file '"+existingProductCertFile+"' is mapped in '"+channelCertMappingFilename+"'.");
 			} else {
 				log.warning("Existing productCert file '"+existingProductCertFile+"' is NOT mapped in '"+channelCertMappingFilename+"'.");
 				
 				// TEMPORARY WORKAROUND FOR BUG
 				/* Notes: http://entitlement.etherpad.corp.redhat.com/Entitlement02MAY12 
 			    /product_ids/rhel-6.3/ComputeNode-ScalableFileSystem-x86_64-21b36280d242-175.pem  is not mapped to any RHN Channels in /cdn/product-baseline.json  (SEEMS  WRONG)
 			    (dgregor) channel won't exist until 6.3 GA.  suggest we pick this up in 6.4
 			    (jsefler) TODO update automated test with pre-6.3GA work-around
 			    /product_ids/rhel-6.3/Server-HPN-ppc64-fff6dded9725-173.pem  is not mapped to  any RHN Channels in /cdn/product-baseline.json   (SEEMS WRONG)
 			    (dgregor) channel won't exist until 6.3 GA.  suggest we pick this up in 6.4
 			    (jsefler) TODO update automated test with pre-6.3GA work-around
 			    */
 				if (existingProductCertFile.endsWith("-173.pem") && clienttasks.redhatReleaseXY.equals("6.3")) {
 					log.warning("Ignoring that existing productCert file '"+existingProductCertFile+"' is NOT mapped in '"+channelCertMappingFilename+"' until release 6.4 as recommended by dgregor.");
 				} else
 				if (existingProductCertFile.endsWith("-175.pem") && clienttasks.redhatReleaseXY.equals("6.3")) {
 					log.warning("Ignoring that existing productCert file '"+existingProductCertFile+"' is NOT mapped in '"+channelCertMappingFilename+"' until release 6.4 as recommended by dgregor.");
 				} else
 				// END OF WORKAROUND
 				allExitingProductCertFilesAreMapped = false;
 				
 			}
 		}
 		Assert.assertTrue(allExitingProductCertFilesAreMapped,"All of the existing productCert files in directory '"+baseProductsDir+"' are mapped to a channel in '"+channelCertMappingFilename+"'.");
 	}
 	
 	
 	@Test(	description="Verify that the migration product certs support this system's RHEL release version",
 			groups={"AcceptanceTests","blockedByBug-782208"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130940)
 	public void VerifyMigrationProductCertsSupportThisSystemsRhelVersion_Test() {
 		
 		// process all the migration product cert files into ProductCerts and assert their version
 		boolean verifiedVersionOfAllMigrationProductCertFiles = true;
 		for (ProductCert productCert : clienttasks.getProductCerts(baseProductsDir)) {
 			if (!productCert.productNamespace.providedTags.toLowerCase().contains("rhel")) {
 				log.warning("Migration productCert '"+productCert+"' does not provide RHEL tags.  Skipping assertion that its version matches this system's RHEL version.");
 				continue;
 			}
 			if (productCert.productNamespace.version.equals(clienttasks.redhatReleaseXY)) {
 				log.info("Migration productCert '"+productCert+"' supports this version of RHEL '"+clienttasks.redhatReleaseXY+"'.");
 
 			} else {
 				log.warning("Migration productCert '"+productCert+"' does NOT support this version of RHEL '"+clienttasks.redhatReleaseXY+"'.");
 				verifiedVersionOfAllMigrationProductCertFiles = false;
 			}
 		}
 		Assert.assertTrue(verifiedVersionOfAllMigrationProductCertFiles,"All of the migration productCerts in directory '"+baseProductsDir+"' support this version of RHEL '"+clienttasks.redhatReleaseXY+"'.");
 	}
 	
 	
 	@Test(	description="Verify that the migration product certs match those from rhn definitions",
 			groups={"AcceptanceTests","blockedByBug-799152","blockedByBug-814360","blockedByBug-861420","blockedByBug-861470","blockedByBug-872959"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyMigrationProductCertsMatchThoseFromRhnDefinitions_Test() {
 		
 		// process all the migration product cert files into ProductCerts and assert they match those from the RHN Definitions
 
 		// get all of the rhnDefnition product certs
 		List<ProductCert> rhnDefnitionProductCerts = new ArrayList<ProductCert>();
 		for (String rhnDefinitionsProductCertsDir : sm_rhnDefinitionsProductCertsDirs) {
 			String tmpRhnDefinitionsProductCertsDir = clienttasks.rhnDefinitionsDir+rhnDefinitionsProductCertsDir;
 			Assert.assertTrue(RemoteFileTasks.testExists(client, tmpRhnDefinitionsProductCertsDir),"The rhn definitions product certs dir '"+rhnDefinitionsProductCertsDir+"' has been locally cloned to '"+tmpRhnDefinitionsProductCertsDir+"'.");
 			rhnDefnitionProductCerts.addAll(clienttasks.getProductCerts(tmpRhnDefinitionsProductCertsDir));
 		}
 		/* ALTERNATIVE WAY OF GETTING ALL rhnDefnition PRODUCT CERTS FROM ALL DIRECTORIES
 		SSHCommandResult result = client.runCommandAndWait("find "+clienttasks.rhnDefinitionsDir+"/product_ids/ -name '*.pem'");
 		String[] rhnDefnitionProductCertPaths = result.getStdout().trim().split("\\n");
 		if (rhnDefnitionProductCertPaths.length==1 && rhnDefnitionProductCertPaths[0].equals("")) rhnDefnitionProductCertPaths = new String[]{};
 		for (String rhnDefnitionProductCertPath : rhnDefnitionProductCertPaths) {
 			rhnDefnitionProductCerts.add(clienttasks.getProductCertFromProductCertFile(new File(rhnDefnitionProductCertPath)));
 		}
 		*/
 		
 		// get the local migration product certs available for install
 		List<ProductCert> migrationProductCerts = clienttasks.getProductCerts(baseProductsDir);
 
 		// test that these local migration product certs came from the current rhnDefinitions structure
 		boolean verifiedMatchForAllMigrationProductCertFiles = true;
 		for (ProductCert migrationProductCert : migrationProductCerts) {
 			if (rhnDefnitionProductCerts.contains(migrationProductCert)) {
 				Assert.assertTrue(true, "Migration product cert '"+migrationProductCert.file+"' was found among the product certs declared for this release from ["+sm_rhnDefinitionsGitRepository+"] "+sm_rhnDefinitionsProductCertsDirs);
 			} else {
 				log.warning("Migration product cert '"+migrationProductCert.file+"' was NOT found among the product certs declared for this release from ["+sm_rhnDefinitionsGitRepository+"] "+sm_rhnDefinitionsProductCertsDirs+".  It may have been re-generated by release engineering.");
 				verifiedMatchForAllMigrationProductCertFiles = false;
 			}
 		}
 		
 		// now assert that all of product certs from the current rhnDefinitions structure are locally available for install
 		for (ProductCert rhnDefinitionProductCert : rhnDefnitionProductCerts) {
 			if (migrationProductCerts.contains(rhnDefinitionProductCert)) {
 				Assert.assertTrue(true, "CDN product cert ["+sm_rhnDefinitionsGitRepository+"] "+rhnDefinitionProductCert.file.getPath().replaceFirst(clienttasks.rhnDefinitionsDir, "")+" was found among the local migration product certs available for installation.");
 			} else {
 				
 				// determine if the rhnDefinitionProductCert is not mapped to any RHEL [5|6] RHN Channels defined in the product baseline file
 				List<String> rhnChannels = cdnProductBaselineProductIdMap.get(rhnDefinitionProductCert.productId);
 				if (rhnChannels==null) {
 					log.warning("CDN Product Baseline has an empty list of RHN Channels for Product ID '"+rhnDefinitionProductCert.productId+"' Name '"+rhnDefinitionProductCert.productName+"'.  This could be a rel-eng defect.");
 					rhnChannels = new ArrayList<String>();
 				}
 				Set<String> rhnChannelsFilteredForRhelRelease = new HashSet<String>();
 				for (String rhnChannel : rhnChannels) {
 					// filter out all RHN Channels not associated with this release  (e.g., assume that an rhn channel containing "-5-" or ends in "-5" is only applicable to rhel5 
 					if (!(rhnChannel.contains("-"+clienttasks.redhatReleaseX+"-") || rhnChannel.endsWith("-"+clienttasks.redhatReleaseX))) continue;
 					rhnChannelsFilteredForRhelRelease.add(rhnChannel);
 				}
 				if (rhnChannelsFilteredForRhelRelease.isEmpty()) {
 					log.info("CDN product cert ["+sm_rhnDefinitionsGitRepository+"] "+rhnDefinitionProductCert.file.getPath().replaceFirst(clienttasks.rhnDefinitionsDir, "")+" was NOT found among the current migration product certs.  No RHEL '"+clienttasks.redhatReleaseX+"' RHN Channels in '"+sm_rhnDefinitionsProductBaselineFile+"' map to Product ID '"+rhnDefinitionProductCert.productId+"' Name '"+rhnDefinitionProductCert.productName+"'.");	
 				} else {
 					log.warning("CDN product cert ["+sm_rhnDefinitionsGitRepository+"] "+rhnDefinitionProductCert.file.getPath().replaceFirst(clienttasks.rhnDefinitionsDir, "")+" was NOT found among the current migration product certs.  It is probably a new product cert generated by release engineering and therefore subscription-manager-migration-data needs a regeneration.");
 					verifiedMatchForAllMigrationProductCertFiles = false;
 				}
 			}
 		}
 		
 		Assert.assertTrue(verifiedMatchForAllMigrationProductCertFiles,"All of the migration productCerts in directory '"+baseProductsDir+"' match the current ["+sm_rhnDefinitionsGitRepository+"] product certs for this RHEL release '"+clienttasks.redhatReleaseXY+"' ");
 	}
 	
 	
 	@Test(	description="Verify that all of the required RHN Channels in the ProductBaseline file are accounted for in channel-cert-mapping.txt",
 			groups={},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="RhnChannelFromProductBaselineData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyChannelCertMappingFileSupportsRhnChannelFromProductBaseline_Test(Object bugzilla, String productBaselineRhnChannel, String productBaselineProductId) throws JSONException {
 		
 		// does the cdn indicate that this channel maps to more than one product?
 		if (cdnProductBaselineChannelMap.get(productBaselineRhnChannel).size()>1) {
 			log.warning("According to the CDN Product Baseline, RHN Channel '"+productBaselineRhnChannel+"' maps to more than one product id: "+cdnProductBaselineChannelMap.get(productBaselineRhnChannel));
 			// handle special cases to decide what productId should be mapped (see bug https://bugzilla.redhat.com/show_bug.cgi?id=786257)
 			// SPECIAL CASE 1:	productId:68  productName:Red Hat Enterprise Linux Desktop
 			if (Arrays.asList(
 					"rhel-x86_64-client-5",
 					"rhel-x86_64-client-5-debuginfo",
 					"rhel-x86_64-client-5-beta",
 					"rhel-x86_64-client-5-beta-debuginfo",
 					"rhel-x86_64-client-supplementary-5",
 					"rhel-x86_64-client-supplementary-5-debuginfo",
 					"rhel-x86_64-client-supplementary-5-beta",
 					"rhel-x86_64-client-supplementary-5-beta-debuginfo",
 					"rhel-i386-client-5",
 					"rhel-i386-client-5-debuginfo",
 					"rhel-i386-client-5-beta",
 					"rhel-i386-client-5-beta-debuginfo",
 					"rhel-i386-client-supplementary-5",
 					"rhel-i386-client-supplementary-5-debuginfo",
 					"rhel-i386-client-supplementary-5-beta",
 					"rhel-i386-client-supplementary-5-beta-debuginfo").contains(productBaselineRhnChannel)) {
 				log.warning("However, RHN Channel '"+productBaselineRhnChannel+"' is a special case.  See https://bugzilla.redhat.com/show_bug.cgi?id=786257#c1 for more details.");
 				Set<String> productIdsForDesktopAndWorkstation = new HashSet<String>();
 				productIdsForDesktopAndWorkstation.add("68");	// rhel-5,rhel-5-client							Red Hat Enterprise Linux Desktop
 				productIdsForDesktopAndWorkstation.add("71");	// rhel-5-client-workstation,rhel-5-workstation	Red Hat Enterprise Linux Workstation
 				Assert.assertTrue(cdnProductBaselineChannelMap.get(productBaselineRhnChannel).containsAll(productIdsForDesktopAndWorkstation) && productIdsForDesktopAndWorkstation.containsAll(cdnProductBaselineChannelMap.get(productBaselineRhnChannel)),
 						"Expecting RHN Channel '"+productBaselineRhnChannel+"' on the CDN Product Baseline to map only to productIds "+productIdsForDesktopAndWorkstation);
 				Assert.assertEquals(getProductIdFromProductCertFilename(channelsToProductCertFilenamesMap.get(productBaselineRhnChannel)),"68",
 						"As dictated in the comments of https://bugzilla.redhat.com/show_bug.cgi?id=786257 subscription-manager-migration-data file '"+channelCertMappingFilename+"' should only map RHN Channel '"+productBaselineRhnChannel+"' to productId 68.");
 				return;
 
 			// SPECIAL CASE 2:	productId:180  productName:Red Hat Beta rhnChannels:
 			} else if (Arrays.asList(	
 					"rhel-i386-client-dts-5-beta", 
 					"rhel-i386-client-dts-5-beta-debuginfo", 
 					"rhel-i386-client-dts-6-beta", 
 					"rhel-i386-client-dts-6-beta-debuginfo", 
 					"rhel-i386-server-dts-5-beta", 
 					"rhel-i386-server-dts-5-beta-debuginfo", 
 					"rhel-i386-server-dts-6-beta", 
 					"rhel-i386-server-dts-6-beta-debuginfo", 
 					"rhel-i386-workstation-dts-6-beta", 
 					"rhel-i386-workstation-dts-6-beta-debuginfo", 
 					"rhel-x86_64-client-dts-5-beta", 
 					"rhel-x86_64-client-dts-5-beta-debuginfo", 
 					"rhel-x86_64-client-dts-6-beta", 
 					"rhel-x86_64-client-dts-6-beta-debuginfo", 
 					"rhel-x86_64-hpc-node-dts-6-beta", 
 					"rhel-x86_64-hpc-node-dts-6-beta-debuginfo", 
 					"rhel-x86_64-server-dts-5-beta", 
 					"rhel-x86_64-server-dts-5-beta-debuginfo", 
 					"rhel-x86_64-server-dts-6-beta", 
 					"rhel-x86_64-server-dts-6-beta-debuginfo", 
 					"rhel-x86_64-workstation-dts-6-beta", 
 					"rhel-x86_64-workstation-dts-6-beta-debuginfo").contains(productBaselineRhnChannel)) {
 				log.warning("However, RHN Channel '"+productBaselineRhnChannel+"' is a special case.  See https://bugzilla.redhat.com/show_bug.cgi?id=820749#c4 for more details.");
 				Assert.assertEquals(getProductIdFromProductCertFilename(channelsToProductCertFilenamesMap.get(productBaselineRhnChannel)),"180",
 						"As dictated in the comments of https://bugzilla.redhat.com/show_bug.cgi?id=820749 subscription-manager-migration-data file '"+channelCertMappingFilename+"' should only map RHN Channel '"+productBaselineRhnChannel+"' to productId 180.");
 				return;
 
 			// SPECIAL CASE:	placeholder for next special case
 			} else if (false) {
 				
 			} else {
 				Assert.fail("Encountered an unexpected case in the CDN Product Baseline where RHN Channel '"+productBaselineRhnChannel+"' maps to more than one product id: "+cdnProductBaselineChannelMap.get(productBaselineRhnChannel)+".  Do not know how to choose which productId channel '"+productBaselineRhnChannel+"' maps to in the subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			}
 		}
 		
 		// Special case for High Touch Beta productId 135  reference: https://bugzilla.redhat.com/show_bug.cgi?id=799152#c4
 		if (productBaselineProductId.equals("135")) {
 			log.warning("For product id 135 (Red Hat Enterprise Linux Server HTB), we actually do NOT want a channel cert mapping as instructed in https://bugzilla.redhat.com/show_bug.cgi?id=799152#c4");
 			Assert.assertTrue(!channelsToProductCertFilenamesMap.containsKey(productBaselineRhnChannel),
 					"CDN Product Baseline RHN Channel '"+productBaselineRhnChannel+"' supporting productId="+productBaselineProductId+" was NOT mapped to a product certificate in the subscription-manager-migration-data file '"+channelCertMappingFilename+"'.  This is a special case (Bugzilla 799152#c4).");
 			return;
 		}
 		
 		// assert that the subscription-manager-migration-data file has a mapping for this RHN Channel found in the CDN Product Baseline
 		Assert.assertTrue(channelsToProductCertFilenamesMap.containsKey(productBaselineRhnChannel),
 				"CDN Product Baseline RHN Channel '"+productBaselineRhnChannel+"' is accounted for in the subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 		
 		// now assert that the subscription-manager-migration-data mapping for the RHN Channel is to the same productId as mapped in the CDN Product Baseline
 		Assert.assertEquals(getProductIdFromProductCertFilename(channelsToProductCertFilenamesMap.get(productBaselineRhnChannel)), productBaselineProductId,
 				"The subscription-manager-migration-data file '"+channelCertMappingFilename+"' maps RHN Channel '"+productBaselineRhnChannel+"' to the same productId as dictated in the CDN Product Baseline.");
 	}
 	
 	
 	@Test(	description="Verify that all of the classic RHN Channels available to a classically registered consumer are accounted for in the in the channel-cert-mapping.txt or is a known exception",
 			groups={"AcceptanceTests"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="getRhnClassicBaseAndAvailableChildChannelsData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyChannelCertMappingFileSupportsRhnClassicBaseAndAvailableChildChannel_Test(Object bugzilla, String classicRhnChannel) {
 		
 		// SPECIAL CASES.....
 		
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-cf-ae-1' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-cf-ae-1-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-cf-ae-1-beta-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-cf-ae-1-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// (degregor 5/4/2012) CloudForms Application Engine isn't shipping anytime soon, so we decided to remove the CDN repos.  While the channels are there in RHN, no one has access to them.
 		if (classicRhnChannel.matches("rhel-.+-6-cf-ae-1(-.*|$)")) {
 			log.warning("(degregor 5/4/2012) CloudForms Application Engine isn't shipping anytime soon, so we decided to remove the CDN repos.  While the channels are there in RHN, no one has access to them.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-htb' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.817 - WARNING: RHN Classic channel 'rhel-x86_64-server-6-htb-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'sam-rhel-x86_64-server-6-htb' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.828 - WARNING: RHN Classic channel 'sam-rhel-x86_64-server-6-htb-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// (degregor 5/4/2012) We intentionally exclude HTB channels from the migration script.  It's not a supported use case.
 		if (classicRhnChannel.matches(".+-htb(-.*|$)")) {
 			log.warning("(degregor 5/4/2012) We intentionally exclude HTB channels from the migration script.  It's not a supported use case.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		// 201205032049:22.819 - WARNING: RHN Classic channel 'rhel-x86_64-server-clusteredstorage-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.820 - WARNING: RHN Classic channel 'rhel-x86_64-server-ei-replication-6' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.820 - WARNING: RHN Classic channel 'rhel-x86_64-server-ei-replication-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.820 - WARNING: RHN Classic channel 'rhel-x86_64-server-ei-replication-6-beta-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.820 - WARNING: RHN Classic channel 'rhel-x86_64-server-ei-replication-6-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'. 
 		// (degregor 5/4/2012) The above channels aren't used.
 		if (classicRhnChannel.matches("rhel-.+-ei-replication-6(-.*|$)")  || classicRhnChannel.matches("rhel-.+-clusteredstorage-6(-.*|$)")) {
 			log.warning("(degregor 5/4/2012) The above channels aren't used.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'rhn-tools-rhel-x86_64-server-6' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'rhn-tools-rhel-x86_64-server-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'rhn-tools-rhel-x86_64-server-6-beta-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205032049:22.827 - WARNING: RHN Classic channel 'rhn-tools-rhel-x86_64-server-6-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// (degregor 5/4/2012) RHN Tools content doesn't get delivered through CDN.
 		if (classicRhnChannel.startsWith("rhn-tools-rhel-")) {
 			log.warning("(degregor 5/4/2012) RHN Tools content doesn't get delivered through CDN.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		// 201205080442:43.007 - WARNING: RHN Classic channel 'rhel-x86_64-server-highavailability-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205080442:43.008 - WARNING: RHN Classic channel 'rhel-x86_64-server-largefilesystem-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205080442:43.010 - WARNING: RHN Classic channel 'rhel-x86_64-server-loadbalance-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// (degregor 5/8/2012) These channels are not used and can be ignored.
 		if (classicRhnChannel.matches("rhel-.+-highavailability-6-beta") || classicRhnChannel.matches("rhel-.+-largefilesystem-6-beta") || classicRhnChannel.matches("rhel-.+-loadbalance-6-beta")) {
 			log.warning("(degregor 5/8/2012) These channels are not used and can be ignored.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		// 201205080556:10.326 - WARNING: RHN Classic channel 'rhel-x86_64-server-hts-6' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		// 201205080556:10.326 - WARNING: RHN Classic channel 'rhel-x86_64-server-hts-6-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-6/channel-cert-mapping.txt'.
 		//	RHN Classic channel 'rhel-x86_64-server-hts-5' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt'.
 		//	RHN Classic channel 'rhel-x86_64-server-hts-5-beta' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt'.
 		//	RHN Classic channel 'rhel-x86_64-server-hts-5-beta-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt'.
 		//	RHN Classic channel 'rhel-x86_64-server-hts-5-debuginfo' is NOT mapped in the file '/usr/share/rhsm/product/RHEL-5/channel-cert-mapping.txt'.
 		// (degregor 5/8/2012) We're not delivering Hardware Certification (aka hts) bits through the CDN at this point.
 		if (classicRhnChannel.matches("rhel-.+-hts-"+clienttasks.redhatReleaseX+"(-.*|$)")) {
 			log.warning("(degregor 5/8/2012) We're not delivering Hardware Certification (aka hts) bits through the CDN at this point.");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		
 		if (classicRhnChannel.matches("rhel-.+-server-5-mrg-.*")) {	// rhel-x86_64-server-5-mrg-grid-1 rhel-x86_64-server-5-mrg-grid-1-beta rhel-x86_64-server-5-mrg-grid-2 rhel-x86_64-server-5-mrg-grid-execute-1 rhel-x86_64-server-5-mrg-grid-execute-1-beta rhel-x86_64-server-5-mrg-grid-execute-2 etc.
 			// Bug 840102 - channels for rhel-<ARCH>-server-5-mrg-* are not yet mapped to product certs in rcm/rcm-metadata.git
 			log.warning("(degregor 8/4/2012) RHEL 5 MRG isn't currently supported in CDN (outside of RHUI) - https://bugzilla.redhat.com/show_bug.cgi?id=840102#c1");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.matches("rhel-.+-server-hpc-5(-.*|$)")) {	// rhel-x86_64-server-hpc-5-beta
 			// Bug 840103 - channel for rhel-x86_64-server-hpc-5-beta is not yet mapped to product cert in rcm/rcm-metadata.git
 			log.warning("(degregor 8/4/2012) The RHEL 5 HPC products is not currently supported in CDN - https://bugzilla.redhat.com/show_bug.cgi?id=840103#c1");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.matches("rhel-.+-server-rhev-hdk-2-5(-.+|$)")) {	// rhel-x86_64-server-rhev-hdk-2-5 rhel-x86_64-server-rhev-hdk-2-5-beta
 			// Bug 840108 - channels for rhel-<ARCH>-rhev-hdk-2-5-* are not yet mapped to product certs in rcm/rhn-definitions.git
 			log.warning("(degregor 8/4/2012) RHEV H Dev Kit is not currently supported in CDN - https://bugzilla.redhat.com/show_bug.cgi?id=840108#c1");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		if (classicRhnChannel.startsWith("rhx-")) {	// rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5 rhx-amanda-enterprise-backup-2.6-rhel-x86_64-server-5 etcetera
 			// Bug 840111 - various rhx channels are not yet mapped to product certs in rcm/rcm-metadata.git 
 			log.warning("(degregor 8/4/2012) RHX products are not currently supported in CDN - https://bugzilla.redhat.com/show_bug.cgi?id=840111#c2");
 			Assert.assertFalse(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 			return;
 		}
 		
 		Assert.assertTrue(channelsToProductCertFilenamesMap.containsKey(classicRhnChannel), "RHN Classic channel '"+classicRhnChannel+"' is accounted for in subscription-manager-migration-data file '"+channelCertMappingFilename+"'.");
 	}
 	
 	
 	
 	// install-num-migrate-to-rhsm Test methods ***********************************************************************
 	
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with a known instnumber and assert the expected productCerts are copied",
 			groups={"blockedByBug-853187","AcceptanceTests","InstallNumMigrateToRhsmWithInstNumber_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="InstallNumMigrateToRhsmData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=131567)
 	//@ImplementsNitrateTest(caseId=130760)
 	//@ImplementsNitrateTest(caseId=130758)
 	public void InstallNumMigrateToRhsmWithInstNumber_Test(Object bugzilla, String instNumber) throws JSONException {
 		InstallNumMigrateToRhsmWithInstNumber(instNumber);
 	}
 	protected SSHCommandResult InstallNumMigrateToRhsmWithInstNumber(String instNumber) throws JSONException {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		String command;
 		SSHCommandResult result;
 		
 		// deleting the currently installed product certs
 		clienttasks.removeAllCerts(false, false, true);
 		clienttasks.removeAllFacts();
 		
 		// get the product cert filenames that we should expect install-num-migrate-to-rhsm to copy
 		List<String> expectedMigrationProductCertFilenames = getExpectedMappedProductCertFilenamesCorrespondingToInstnumberUsingInstnumTool(instNumber);
 
 		// test --dryrun --instnumber ................................................
 		log.info("Testing with the dryrun option...");
 		command = installNumTool+" --dryrun --instnumber="+instNumber;
 		result = RemoteFileTasks.runCommandAndAssert(client,command,0);
 		//[root@jsefler-onprem-5server ~]# install-num-migrate-to-rhsm --dryrun --instnumber 0000000e0017fc01
 		//Copying /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-f812997e0eda-71.pem to /etc/pki/product/71.pem
 		//Copying /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-6587edcf1c03-68.pem to /etc/pki/product/68.pem
 
 		// assert the dryrun
 		for (String expectedMigrationProductCertFilename : expectedMigrationProductCertFilenames) {
 			String pemFilename = getPemFileNameFromProductCertFilename(expectedMigrationProductCertFilename);
 			//String expectedStdoutString = "Copying "+baseProductsDir+"/"+expectedMigrationProductCertFilename+" to "+clienttasks.productCertDir+"/"+pemFilename;	// valid prior to Bug 853187 - String Update: install-num-migrate-to-rhsm output
 			String expectedStdoutString = "Installing "+baseProductsDir+"/"+expectedMigrationProductCertFilename+" to "+clienttasks.productCertDir+"/"+pemFilename;
 			Assert.assertTrue(result.getStdout().contains(expectedStdoutString),"The dryrun output from "+installNumTool+" contains the expected message: "+expectedStdoutString);
 		}
 		int numProductCertFilenamesToBeCopied=0;
 		//for (int fromIndex=0; result.getStdout().indexOf("Copying", fromIndex)>=0&&fromIndex>-1; fromIndex=result.getStdout().indexOf("Copying", fromIndex+1)) numProductCertFilenamesToBeCopied++;	// valid prior to Bug 853187 - String Update: install-num-migrate-to-rhsm output
 		for (int fromIndex=0; result.getStdout().indexOf("Installing", fromIndex)>=0&&fromIndex>-1; fromIndex=result.getStdout().indexOf("Installing", fromIndex+1)) numProductCertFilenamesToBeCopied++;	
 		Assert.assertEquals(numProductCertFilenamesToBeCopied, expectedMigrationProductCertFilenames.size(),"The number of product certs to be copied.");
 		Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), 0, "A dryrun should NOT install any product certs.");
 		Map<String,String> factMap = clienttasks.getFacts();
 		
 //		// TEMPORARY WORKAROUND FOR BUG
 //		String bugId = "840415"; boolean invokeWorkaroundWhileBugIsOpen = true;
 //		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 //		if (invokeWorkaroundWhileBugIsOpen) {
 //			if (clienttasks.productCertDir.equals(nonDefaultProductCertDir))
 //			log.warning("Skipping the removal of the non default productCertDir '"+nonDefaultProductCertDir+"' before Testing without the dryrun option...");
 //		} else
 //		// END OF WORKAROUND
 // TODO This test path is not yet complete - depends on the outcome of bug 840415
 		// when testing with the non-default productCertDir, make sure it does not exist (the list --installed call above will create it as a side affect)
 		// Note: this if block help reveal bug 840415 - Install-num migration throws traceback for invalid product cert location.
 		if (clienttasks.productCertDir.equals(nonDefaultProductCertDir)) {
 			client.runCommandAndWait("rm -rf "+clienttasks.productCertDir);
 			Assert.assertTrue(!RemoteFileTasks.testExists(client, clienttasks.productCertDir),"The configured rhsm.productCertDir does not exist.");
 		}
 		
 		// TEMPORARY WORKAROUND FOR BUG
 		String bugId = "783278"; boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		String bugPkg = "subscription-manager-migration";
 		String bugVer = "subscription-manager-migration-0.98";	// RHEL58
 		try {if (clienttasks.installedPackageVersion.get(bugPkg).contains(bugVer) && !invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+" which has NOT been fixed in this installed version of "+bugVer+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")"); invokeWorkaroundWhileBugIsOpen=true;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			log.warning("Skipping the assertion of the fact '"+migrationFromFact+"' fact.");
 		} else
 		// END OF WORKAROUND
 		Assert.assertNull(factMap.get(migrationFromFact), "The migration fact '"+migrationFromFact+"' should NOT be set after running command: "+command);
 		Assert.assertNull(factMap.get(migrationSystemIdFact), "The migration fact '"+migrationSystemIdFact+"' should NOT be set after running command: "+command);
 		Assert.assertNull(factMap.get(migrationDateFact), "The migration fact '"+migrationDateFact+"' should NOT be set after running command: "+command);
 		
 		// test --instnumber ................................................
 		log.info("Testing without the dryrun option...");
 		command = installNumTool+" --instnumber="+instNumber;
 		result = RemoteFileTasks.runCommandAndAssert(client,command,0);
 		//[root@jsefler-onprem-5server ~]# install-num-migrate-to-rhsm --instnumber 0000000e0017fc01
 		//Copying /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-f812997e0eda-71.pem to /etc/pki/product/71.pem
 		//Copying /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-6587edcf1c03-68.pem to /etc/pki/product/68.pem
 		List<ProductCert> migratedProductCerts = clienttasks.getCurrentProductCerts();
 		Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), expectedMigrationProductCertFilenames.size(), "The number of productCerts installed after running migration command: "+command);
 		for (String expectedMigrationProductCertFilename : expectedMigrationProductCertFilenames) {
 			ProductCert expectedMigrationProductCert = clienttasks.getProductCertFromProductCertFile(new File(baseProductsDir+"/"+expectedMigrationProductCertFilename));
 			Assert.assertTrue(migratedProductCerts.contains(expectedMigrationProductCert),"The newly installed product certs includes the expected migration productCert: "+expectedMigrationProductCert);
 		}
 		//	[root@jsefler-rhel59 ~]# subscription-manager facts --list | grep migration
 		//	migration.install_number: 0000000e0017fc01
 		//	migration.migrated_from: install_number
 		//	migration.migration_date: 2012-08-08T11:11:15.818782
 		factMap = clienttasks.getFacts();
 		Assert.assertEquals(factMap.get(migrationFromFact), "install_number", "The migration fact '"+migrationFromFact+"' should be set after running command: "+command);
 		Assert.assertNull(factMap.get(migrationSystemIdFact), "The migration fact '"+migrationSystemIdFact+"' should NOT be set after running command: "+command);
 		Assert.assertNotNull(factMap.get(migrationDateFact), "The migration fact '"+migrationDateFact+"' should be set after running command: "+command);
 		
 		// assert that the migrationDateFact was set within the last few seconds
 		int tol = 60; // tolerance in seconds
 		Calendar migrationDate = parseDateStringUsingDatePattern(factMap.get(migrationDateFact), "yyyy-MM-dd'T'HH:mm:ss", null);	// NOTE: The .SSS milliseconds was dropped from the date pattern because it was getting confused as seconds from the six digit value in migration.migration_date: 2012-08-08T11:11:15.818782
 		long systemTimeInSeconds = Long.valueOf(client.runCommandAndWait("date +%s").getStdout().trim());	// seconds since 1970-01-01 00:00:00 UTC
 		long migratTimeInSeconds = migrationDate.getTimeInMillis()/1000;
 		Assert.assertTrue(systemTimeInSeconds-tol < migratTimeInSeconds && migratTimeInSeconds < systemTimeInSeconds+tol, "The migration date fact '"+factMap.get(migrationDateFact)+"' was set within the last '"+tol+"' seconds.");
 		
 		return result;
 	}
 	
 	
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with install-num used to provision this machine",
 			groups={"AcceptanceTests","InstallNumMigrateToRhsm_Test","blockedByBug-854879"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130760)
 	public void InstallNumMigrateToRhsm_Test() throws JSONException {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		if (!RemoteFileTasks.testExists(client, machineInstNumberFile) &&
 			RemoteFileTasks.testExists(client, backupMachineInstNumberFile)	) {
 			log.info("Restoring backup of the rhn install-num file...");
 			client.runCommandAndWait("mv -f "+backupMachineInstNumberFile+" "+machineInstNumberFile);
 		}
 		if (!RemoteFileTasks.testExists(client, machineInstNumberFile)) throw new SkipException("This system was NOT provisioned with an install number.");
 		
 		// get the install number used to provision this machine
 		SSHCommandResult result = client.runCommandAndWait("cat "+machineInstNumberFile);
 		String installNumber = result.getStdout().trim();
 		
 		// test this install number explicitly (specifying --instnumber option)
 		SSHCommandResult explicitResult = InstallNumMigrateToRhsmWithInstNumber(installNumber);
 		
 		// now test this install number implicitly (without specifying any options)
 		clienttasks.removeAllCerts(false, false, true);
 		clienttasks.removeAllFacts();
 		SSHCommandResult implicitResult = client.runCommandAndWait(installNumTool);
 		// compare implicit to explicit results for verification
 		Assert.assertEquals(implicitResult.getStdout().trim(), explicitResult.getStdout().trim(), "Stdout from running :"+installNumTool);
 		Assert.assertEquals(implicitResult.getStderr().trim(), explicitResult.getStderr().trim(), "Stderr from running :"+installNumTool);
 		Assert.assertEquals(implicitResult.getExitCode(), explicitResult.getExitCode(), "ExitCode from running :"+installNumTool);
 		Assert.assertEquals(clienttasks.getFactValue(migrationFromFact), "install_number", "The migration fact '"+migrationFromFact+"' should be set after running command: "+installNumTool);
 		Assert.assertNull(clienttasks.getFactValue(migrationSystemIdFact), "The migration fact '"+migrationSystemIdFact+"' should NOT be set after running command: "+installNumTool);
 		
 		// assert that the migrated product certs provide (at least) the same product tags as originally installed with the install number
 		List<ProductCert> migratedProductCerts = clienttasks.getCurrentProductCerts();
 		log.info("The following productCerts were originally installed on this machine prior to this migration test:");
 		for (ProductCert originalProductCert : originallyInstalledRedHatProductCerts) log.info(originalProductCert.toString());
 		log.info("The following productCerts were migrated to the product install directory after running the migration test:");
 		for (ProductCert migratedProductCert : migratedProductCerts) log.info(migratedProductCert.toString());
 		log.info("Will now verify that all of the productTags from the originally installed productCerts are found among the providedTags of the migrated productCerts...");
 		for (ProductCert originalProductCert : originallyInstalledRedHatProductCerts) {
 			if (originalProductCert.productNamespace.providedTags==null) continue;
 			List<String> originalProvidedTags = Arrays.asList(originalProductCert.productNamespace.providedTags.trim().split(" *, *"));
 			for (ProductCert migratedProductCert : migratedProductCerts) {
 				List<String> migratedProvidedTags = Arrays.asList(migratedProductCert.productNamespace.providedTags.trim().split(" *, *"));
 				if (migratedProvidedTags.containsAll(originalProvidedTags)) {
 					Assert.assertTrue(true,"This migrated productCert provides all the same tags as one of the originally installed product certs.\nMigrated productCert: "+migratedProductCert);
 					originalProvidedTags = new ArrayList<String>();//originalProvidedTags.clear();
 					break;
 				}
 			}
 			if (!originalProvidedTags.isEmpty()) {
 				Assert.fail("Failed to find the providedTags from the originally installed productCert among the migrated productCerts.\nOriginal productCert: "+originalProductCert);
 			}
 		}
 	}
 	
 	
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with a non-default rhsm.productcertdir configured",
 			groups={"blockedByBug-773707","blockedByBug-840415","InstallNumMigrateToRhsmWithNonDefaultProductCertDir_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="InstallNumMigrateToRhsmData",
 			enabled=true)
 	public void InstallNumMigrateToRhsmWithNonDefaultProductCertDir_Test(Object bugzilla, String instNumber) throws JSONException {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		
 		// TEMPORARY WORKAROUND FOR BUG
 		String bugId = "773707"; boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		String bugPkg = "subscription-manager-migration";
 		String bugVer = "subscription-manager-migration-0.98";	// RHEL58
 		try {if (clienttasks.installedPackageVersion.get(bugPkg).contains(bugVer) && !invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+" which has NOT been fixed in this installed version of "+bugVer+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")"); invokeWorkaroundWhileBugIsOpen=true;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			throw new SkipException("There is no workaround for this installed version of "+bugVer+".  Blocked by Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");
 		}
 		// END OF WORKAROUND
 		
 		//if (clienttasks.redhatReleaseXY.equals("5.9")) {
 		//if (Arrays.asList("5.7","5.8","5.9").contains(clienttasks.redhatReleaseXY)) {
 		if (Float.valueOf(clienttasks.redhatReleaseXY) <= 5.9f) {
 			throw new SkipException("Blocking bugzilla 840415 was fixed in a subsequent release.  Skipping this test since we already know it will fail in RHEL release '"+clienttasks.redhatReleaseXY+"'.");
 		}
 		
 		// NOTE: The configNonDefaultRhsmProductCertDir will handle the configuration setting
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm", "productCertDir"), nonDefaultProductCertDir,"A non-default rhsm.productCertDir has been configured.");
 		InstallNumMigrateToRhsmWithInstNumber_Test(bugzilla,instNumber);
 	}
 	
 	
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with a bad length install-num (expecting 16 chars long)",
 			groups={},
 			dependsOnMethods={},
 			dataProvider="InstallNumMigrateToRhsmWithInvalidInstNumberData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130760)
 	public void InstallNumMigrateToRhsmWithInvalidInstNumber_Test(Object bugzilla, String command, Integer expectedExitCode, String expectedStdout, String expectedStderr) {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		SSHCommandResult result = client.runCommandAndWait(command);
 		if (expectedStdout!=null) Assert.assertEquals(result.getStdout().trim(), expectedStdout, "Stdout from running :"+command);
 		if (expectedStderr!=null) Assert.assertEquals(result.getStderr().trim(), expectedStderr, "Stderr from running :"+command);
 		// TEMPORARY WORKAROUND FOR BUG
 		String bugId="783542"; boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		String bugPkg = "subscription-manager-migration";
 		String bugVer = "subscription-manager-migration-0.98";	// RHEL58
 		try {if (clienttasks.installedPackageVersion.get(bugPkg).contains(bugVer) && !invokeWorkaroundWhileBugIsOpen) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+" which has NOT been fixed in this installed version of "+bugVer+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")"); invokeWorkaroundWhileBugIsOpen=true;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			log.warning("Skipping the exitCode assertion from running: "+command);
 		} else
 		// END OF WORKAROUND
 		if (expectedExitCode!=null) Assert.assertEquals(result.getExitCode(), expectedExitCode, "ExitCode from running :"+command);
 	}
 
 
 	@Test(	description="Execute migration tool install-num-migrate-to-rhsm with no install-num found on machine",
 			groups={},
 			dependsOnMethods={},
 			enabled=true)
 	public void InstallNumMigrateToRhsmWithMissingInstNumber_Test() {
 		if (!clienttasks.redhatReleaseX.equals("5")) throw new SkipException("This test is applicable to RHEL5 only.");
 		if (RemoteFileTasks.testExists(client, machineInstNumberFile)) {
 			log.info("Backing up the rhn install-num file...");
 			client.runCommandAndWait("mv -f "+machineInstNumberFile+" "+backupMachineInstNumberFile);
 		}
 		InstallNumMigrateToRhsmWithInvalidInstNumber_Test(null, installNumTool,1,"Could not read installation number from "+machineInstNumberFile+".  Aborting.","");
 	}
 	
 	
 	@Test(	description="Assert that install-num-migrate-to-rhsm is only installed on RHEL5",
 			groups={"blockedByBug-790205"},
 			dependsOnMethods={},
 			enabled=true)
 	public void InstallNumMigrateToRhsmShouldOnlyBeInstalledOnRHEL5_Test() {
 		// make sure subscription-manager-migration is installed on RHEL5
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client, "rpm -ql "+clienttasks.command+"-migration", 0);
 		Assert.assertEquals(result.getStdout().contains(installNumTool), clienttasks.redhatReleaseX.equals("5"), "The "+clienttasks.command+"-migration package should only provide '"+installNumTool+"' on RHEL5.");
 	}
 	
 	
 	
 	// rhn-migrate-classic-to-rhsm Test methods ***********************************************************************
 	
 	@Test(	description="Register system using RHN Classic and then Execute migration tool rhn-migrate-classic-to-rhsm with options after adding RHN Channels",
 			groups={"AcceptanceTests","RhnMigrateClassicToRhsm_Test","blockedByBug-840169"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="RhnMigrateClassicToRhsmData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=130764,130762) // TODO some expected yum repo assertions are not yet automated
 	public void RhnMigrateClassicToRhsm_Test(Object bugzilla, String rhnUsername, String rhnPassword, String rhnHostname, List<String> rhnChannelsToAdd, String options, String regUsername, String regPassword, String regOrg, Integer serviceLevelIndex, String serviceLevelExpected) {
 		if (sm_rhnHostname.equals("")) throw new SkipException("This test requires access to RHN Classic.");
 
 		// make sure our serverUrl is configured to it's original good value
 		// TODO maybe this should go after the unregister and removeAll commands
 		restoreOriginallyConfiguredServerUrl();
 		
 		// make sure we are NOT registered to RHSM
 		clienttasks.unregister(null,null,null);
 
 		// deleting the currently installed product certs
 		clienttasks.removeAllCerts(false, false, true);
 		clienttasks.removeAllFacts();
 		
 		// register to RHN Classic
 		String rhnSystemId = clienttasks.registerToRhnClassic(rhnUsername, rhnPassword, rhnHostname);
 		Assert.assertTrue(clienttasks.isRhnSystemIdRegistered(rhnUsername, rhnPassword, rhnHostname, rhnSystemId),"Confirmed that rhn systemId '"+rhnSystemId+"' is currently registered.");
 		
 		// subscribe to more RHN Classic channels
 		if (rhnChannelsToAdd.size()>0) addRhnClassicChannels(rhnUsername, rhnPassword, rhnChannelsToAdd);
 		
 		// get a list of the consumed RHN Classic channels
 		List<String> rhnChannelsConsumed = getCurrentRhnClassicChannels();
 		if (rhnChannelsToAdd.size()>0) Assert.assertTrue(rhnChannelsConsumed.containsAll(rhnChannelsToAdd), "All of the RHN Classic channels added appear to be consumed.");
 
 		// get the product cert filenames that we should expect rhn-migrate-classic-to-rhsm to copy (or use the ones supplied to the @Test)
 		Set<String> expectedMigrationProductCertFilenames = getExpectedMappedProductCertFilenamesCorrespondingToChannels(rhnChannelsConsumed);
 		
 		// screw up the currently configured serverUrl when the input options specify a new one
 		if (options.contains("--serverurl")) {
 			log.info("Configuring a bad server hostname:port/prefix to test that the specified --serverurl can override it...");
 			List<String[]> listOfSectionNameValues = new ArrayList<String[]>();
 			listOfSectionNameValues.add(new String[]{"server","hostname","bad-hostname.com"});
 			listOfSectionNameValues.add(new String[]{"server","port","000"});
 			listOfSectionNameValues.add(new String[]{"server","prefix","/bad-prefix"});
 			clienttasks.config(null, null, true, listOfSectionNameValues);
 		}
 		
 		// execute rhn-migrate-classic-to-rhsm with options
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsm(options,rhnUsername,rhnPassword,regUsername,regPassword,regOrg,serviceLevelIndex);
 		
 		// assert the exit code
 		String expectedMsg;
 		if (!areAllChannelsMapped(rhnChannelsConsumed) && !options.contains("-f")/*--force*/) {	// when not all of the rhnChannelsConsumed have been mapped to a productCert and no --force has been specified.
 			log.warning("Not all of the channels are mapped to a product cert.  Therefore, the "+rhnMigrateTool+" command should have exited with code 1.");
 			expectedMsg = "Use --force to ignore these channels and continue the migration.";
 			Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+expectedMsg);	
 			Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "ExitCode from call to '"+rhnMigrateTool+" "+options+"' when any of the channels are not mapped to a productCert.");
 			Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' exists.  This indicates this system is still registered using RHN Classic when rhn-migrate-classic-to-rhsm requires --force to continue.");
 			
 			// assert that no product certs have been copied yet
 			Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), 0, "No productCerts have been migrated when "+rhnMigrateTool+" requires --force to continue.");
 
 			// assert that we are not yet registered to RHSM
 			Assert.assertNull(clienttasks.getCurrentConsumerCert(),"We should NOT be registered to RHSM when "+rhnMigrateTool+" requires --force to continue.");
 			
 			// assert that we are still registered to RHN
 			Assert.assertTrue(clienttasks.isRhnSystemIdRegistered(rhnUsername, rhnPassword, rhnHostname, rhnSystemId),"Confirmed that rhn systemId '"+rhnSystemId+"' is still registered since our migration attempt requires --force to continue.");
 
 			return;
 		}
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(0), "ExitCode from call to '"+rhnMigrateTool+" "+options+"' when all of the channels are mapped.");
 		
 		// assert the expected migration.* facts are set
 		//	[root@ibm-x3620m3-01 ~]# subscription-manager facts --list | grep migration
 		//	migration.classic_system_id: 1023061526
 		//	migration.migrated_from: rhn_hosted_classic
 		//	migration.migration_date: 2012-07-13T18:51:44.254543
 		Map<String,String> factMap = clienttasks.getFacts();
 		Assert.assertEquals(factMap.get(migrationFromFact), "rhn_hosted_classic", "The migration fact '"+migrationFromFact+"' should be set after running "+rhnMigrateTool+" with "+options+".");
 		Assert.assertEquals(factMap.get(migrationSystemIdFact), rhnSystemId, "The migration fact '"+migrationSystemIdFact+"' should be set after running "+rhnMigrateTool+" with "+options+".");
 		Assert.assertNotNull(factMap.get(migrationDateFact), "The migration fact '"+migrationDateFact+"' should be set after running "+rhnMigrateTool+" with "+options+".");
 		int tol = 180; // tolerance in seconds to assert that the migration_date facts was set within the last few seconds
 		Calendar migrationDate = parseDateStringUsingDatePattern(factMap.get(migrationDateFact), "yyyy-MM-dd'T'HH:mm:ss", null);	// NOTE: The .SSS milliseconds was dropped from the date pattern because it was getting confused as seconds from the six digit value in migration.migration_date: 2012-08-08T11:11:15.818782
 		long systemTimeInSeconds = Long.valueOf(client.runCommandAndWait("date +%s").getStdout().trim());	// seconds since 1970-01-01 00:00:00 UTC
 		long migratTimeInSeconds = migrationDate.getTimeInMillis()/1000;
 		Assert.assertTrue(systemTimeInSeconds-tol < migratTimeInSeconds && migratTimeInSeconds < systemTimeInSeconds+tol, "The migration date fact '"+factMap.get(migrationDateFact)+"' was set within the last '"+tol+"' seconds.");
 		
 		// assert we are no longer registered to RHN Classic
 		expectedMsg = "System successfully unregistered from RHN Classic.";
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+expectedMsg);
 		Assert.assertTrue(!RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' is abscent.  This indicates this system is not registered using RHN Classic.");
 		Assert.assertTrue(!clienttasks.isRhnSystemIdRegistered(rhnUsername, rhnPassword, rhnHostname, rhnSystemId), "Confirmed that rhn systemId '"+rhnSystemId+"' is no longer registered.");
 
 		// assert products are copied
 		expectedMsg = String.format("Product certificates copied successfully to %s !",	clienttasks.productCertDir);
 		expectedMsg = String.format("Product certificates copied successfully to %s",	clienttasks.productCertDir);
 		expectedMsg = String.format("Product certificates installed successfully to %s.",	clienttasks.productCertDir);	// Bug 852107 - String Update: rhn-migrate-classic-to-rhsm output
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+expectedMsg);
 		
 		// assert that the expected product certs mapped from the consumed RHN Classic channels are now installed
 		List<ProductCert> migratedProductCerts = clienttasks.getCurrentProductCerts();
 		Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), expectedMigrationProductCertFilenames.size(), "The number of productCerts installed after running "+rhnMigrateTool+" with "+options+".");
 		for (String expectedMigrationProductCertFilename : expectedMigrationProductCertFilenames) {
 			ProductCert expectedMigrationProductCert = clienttasks.getProductCertFromProductCertFile(new File(baseProductsDir+"/"+expectedMigrationProductCertFilename));
 			Assert.assertTrue(migratedProductCerts.contains(expectedMigrationProductCert),"The newly installed product certs includes the expected migration productCert: "+expectedMigrationProductCert);
 		}
 		
 		// assert that when --serverurl is specified, its hostname:port/prefix are preserved into rhsm.conf
 		if (options.contains("--serverurl")) {
 			// comparing to original configuration values because these are the ones I am using in the dataProvider
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "hostname"),originalServerHostname,"The value of the [server]hostname newly configured in "+clienttasks.rhsmConfFile+" was extracted from the --serverurl option specified in rhn-migrated-classic-to-rhsm options '"+options+"'.");
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "port"),originalServerPort,"The value of the [server]port newly configured in "+clienttasks.rhsmConfFile+" was extracted from the --serverurl option specified in rhn-migrated-classic-to-rhsm options '"+options+"'.");
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "prefix"),originalServerPrefix,"The value of the [server]prefix newly configured in "+clienttasks.rhsmConfFile+" was extracted from the --serverurl option specified in rhn-migrated-classic-to-rhsm options '"+options+"'.");
 		}
 		
 		// assert that we are newly registered using rhsm
 		clienttasks.identity(null, null, null, null, null, null, null);
 		Assert.assertNotNull(clienttasks.getCurrentConsumerId(),"The existance of a consumer cert indicates that the system is currently registered using RHSM.");
 		expectedMsg = String.format("System '%s' successfully registered to Red Hat Subscription Management.",	clienttasks.hostname);
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+expectedMsg);
 
 		// assert the the expected service level was set as a preference on the registered consumer
 		if (serviceLevelExpected!=null) {
 			String serviceLevel = clienttasks.getCurrentServiceLevel();
 			Assert.assertTrue(serviceLevelExpected.equalsIgnoreCase(serviceLevel), "Regardless of case, the serviceLevel requested during migration (or possibly the org's defaultServiceLevel) was set as the system's service level preference (serviceLevelExpected='"+serviceLevelExpected+"').");
 		}
 		
 		// assert that when --no-auto is specified, no entitlements were granted during the rhsm registration
 		String autosubscribeAttemptedMsg = "Attempting to auto-subscribe to appropriate subscriptions ...";
 		String autosubscribeFailedMsg = "Unable to auto-subscribe.  Do your existing subscriptions match the products installed on this system?";
 		if (options.contains("-n")) { // -n, --no-auto   Do not autosubscribe when registering with subscription-manager
 
 			// assert that autosubscribe was NOT attempted
 			Assert.assertTrue(!sshCommandResult.getStdout().contains(autosubscribeAttemptedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' does NOT contain message: "+autosubscribeAttemptedMsg);			
 			Assert.assertTrue(!sshCommandResult.getStdout().contains(autosubscribeFailedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' does NOT contain message: "+autosubscribeFailedMsg);			
 
 			// assert that we are NOT registered using rhsm
 			/* THIS ASSERTION IS WRONG! DON'T DO IT!  BUG 849644
 			clienttasks.identity_(null, null, null, null, null, null, null);
 			Assert.assertNull(clienttasks.getCurrentConsumerCert(),"We should NOT be registered to RHSM after a call to "+rhnMigrateTool+" with options "+options+".");
 			*/
 			
 			// assert that we are NOT consuming any entitlements
 			Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().isEmpty(),"We should NOT be consuming any RHSM entitlements after call to "+rhnMigrateTool+" with options ("+options+") that indicate no autosubscribe.");
 			
 			// since no autosubscribing took place, there is nothing left to assert, just return
 			return;
 		}
 
 		// assert that autosubscribe was attempted
 		Assert.assertTrue(sshCommandResult.getStdout().contains(autosubscribeAttemptedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+autosubscribeAttemptedMsg);			
 
 		// assert that the migrated productCert corresponding to the base channel has been autosubscribed by checking the status on the installedProduct
 		// FIXME This assertion is wrong when there are no available subscriptions that provide for the migrated product certs' providesTags; however since we register as qa@redhat.com, I think we have access to all base rhel subscriptions
 		// FIXME if a service-level is provided that is not available, then this product may NOT be subscribed
 		/* DECIDED NOT TO FIXME SINCE THIS ASSERTION IS THE JOB OF DEDICATED AUTOSUBSCRIBE TESTS IN SubscribeTests.java
 		InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToProductCert(clienttasks.getProductCertFromProductCertFile(new File(clienttasks.productCertDir+"/"+getPemFileNameFromProductCertFilename(channelsToProductCertFilenamesMap.get(rhnBaseChannel)))));
 		Assert.assertEquals(installedProduct.status, "Subscribed","The migrated product cert corresponding to the RHN Classic base channel '"+rhnBaseChannel+"' was autosubscribed: "+installedProduct);
 		*/
 		
 		// assert that autosubscribe feedback was a success (or not)
 		List<ProductSubscription> consumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		if (consumedProductSubscriptions.isEmpty()) {
 			Assert.assertTrue(sshCommandResult.getStdout().contains(autosubscribeFailedMsg), "When no entitlements have been granted, stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+autosubscribeFailedMsg);			
 		} else {
 			Assert.assertTrue(!sshCommandResult.getStdout().contains(autosubscribeFailedMsg), "When autosubscribe is successful and entitlements have been granted, stdout from call to '"+rhnMigrateTool+" "+options+"' does NOT contains message: "+autosubscribeFailedMsg);				
 		}
 		
 		// assert that when no --servicelevel is specified, then no service level preference will be set on the registered consumer
 		if (!options.contains("-s ") && !options.contains("--servicelevel") && (serviceLevelExpected==null||serviceLevelExpected.isEmpty())) {
 			// assert no service level preference was set
 			Assert.assertEquals(clienttasks.getCurrentServiceLevel(), "", "No servicelevel preference should be set on the consumer when no service level was requested.");
 		}
 		
 		// assert the service levels consumed from autosubscribe match the requested serviceLevel
 		if (serviceLevelExpected!=null && !serviceLevelExpected.isEmpty()) {
 
 			// when a valid servicelevel was either specified or chosen
 			expectedMsg = String.format("Service level set to: %s",serviceLevelExpected);
 			Assert.assertTrue(sshCommandResult.getStdout().toUpperCase().contains(expectedMsg.toUpperCase()), "Regardless of service level case, the stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+expectedMsg);
 			
 			for (ProductSubscription productSubscription : consumedProductSubscriptions) {
 				Assert.assertNotNull(productSubscription.serviceLevel, "When migrating from RHN Classic with a specified service level '"+serviceLevelExpected+"', this auto consumed product subscription's service level should not be null: "+productSubscription);
 				if (sm_exemptServiceLevelsInUpperCase.contains(productSubscription.serviceLevel.toUpperCase())) {
 					log.info("Exempt service levels: "+sm_exemptServiceLevelsInUpperCase);
 					Assert.assertTrue(sm_exemptServiceLevelsInUpperCase.contains(productSubscription.serviceLevel.toUpperCase()),"This auto consumed product subscription's service level is among the exempt service levels: "+productSubscription);
 				} else {
 					Assert.assertTrue(productSubscription.serviceLevel.equalsIgnoreCase(serviceLevelExpected),"When migrating from RHN Classic with a specified service level '"+serviceLevelExpected+"', this auto consumed product subscription's service level should match: "+productSubscription);
 				}
 			}
 		}
 	}
 	
 	
 	@Test(	description="With a proxy configured in rhn/up2date, register system using RHN Classic and then Execute migration tool rhn-migrate-classic-to-rhsm with options after adding RHN Channels",
 			groups={"AcceptanceTests","RhnMigrateClassicToRhsm_Test","RhnMigrateClassicToRhsmUsingProxyServer_Test","blockedbyBug-798015","blockedbyBug-861693"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="RhnMigrateClassicToRhsmUsingProxyServerData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130763)
 	public void RhnMigrateClassicToRhsmUsingProxyServer_Test(Object bugzilla, String rhnUsername, String rhnPassword, String rhnHostname, List<String> rhnChannelsToAdd, String options, String regUsername, String regPassword, String regOrg, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex) {
 		if (sm_rhnHostname.equals("")) throw new SkipException("This test requires access to RHN Classic.");
 
 		// make sure we are NOT registered to RHSM
 		clienttasks.unregister_(null,null,null);
 		clienttasks.removeAllCerts(true, true, true);
 		clienttasks.removeAllFacts();
 		
 //		// reset all of the proxy server configuration (RHN and RHSM)
 //		removeProxyServerConfigurations();
 		// remove proxy settings from up2date
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxy", "0");		// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "httpProxy", "");			// httpProxy[comment]=HTTP proxy in host:port format, e.g. squid.redhat.com:3128
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth", "0");	// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyUser", "");			// proxyUser[comment]=The username for an authenticated proxy
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyPassword", "");		// proxyPassword[comment]=The password to use for an authenticated proxy
 		iptablesAcceptPort(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "port"));
 
 
 		// enable/set proxy settings for RHN up2date
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxy",		"1");											// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "httpProxy",		proxy_hostnameConfig+":"+proxy_portConfig);		// httpProxy[comment]=HTTP proxy in host:port format, e.g. squid.redhat.com:3128
 		if (proxy_userConfig.equals("") && proxy_passwordConfig.equals("")) {
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth", "0");	// enableProxyAuth[comment]=To use an authenticated proxy or not
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyUser",		"disabled-proxy-user");								// proxyUser[comment]=The username for an authenticated proxy
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyPassword",	"disabled-proxy-password");							// proxyPassword[comment]=The password to use for an authenticated proxy
 		} else {
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth", "1");	// enableProxyAuth[comment]=To use an authenticated proxy or not
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyUser",		proxy_userConfig);								// proxyUser[comment]=The username for an authenticated proxy
 			clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyPassword",	proxy_passwordConfig);							// proxyPassword[comment]=The password to use for an authenticated proxy
 		}
 		
 		// mark the tail of proxyLog with a message
 		String proxyLogMarker = System.currentTimeMillis()+" Testing RhnMigrateClassicToRhsmUsingProxyServer_Test.registerToRhnClassic from "+clienttasks.hostname+"...";
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 
 		// register to RHN Classic
 		String rhnSystemId = clienttasks.registerToRhnClassic(rhnUsername, rhnPassword, rhnHostname);
 		Assert.assertTrue(clienttasks.isRhnSystemIdRegistered(rhnUsername, rhnPassword, rhnHostname, rhnSystemId),"Confirmed that rhn systemId '"+rhnSystemId+"' is currently registered.");
 
 		// assert that traffic to RHN is went through the proxy
 		String proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 		Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to RHN.");
 		
 		// subscribe to more RHN Classic channels
 		if (!rhnChannelsToAdd.isEmpty()) addRhnClassicChannels(rhnUsername, rhnPassword, rhnChannelsToAdd);
 		
 		// get a list of the consumed RHN Classic channels
 		List<String> rhnChannelsConsumed = getCurrentRhnClassicChannels();
 		if (!rhnChannelsToAdd.isEmpty()) Assert.assertTrue(rhnChannelsConsumed.containsAll(rhnChannelsToAdd), "All of the RHN Classic channels added appear to be consumed.");
 
 		// reject traffic through the server.port
 		iptablesRejectPort(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "port"));
 
 		// mark the tail of proxyLog with a message
 		proxyLogMarker = System.currentTimeMillis()+" Testing RhnMigrateClassicToRhsmUsingProxyServer_Test.executeRhnMigrateClassicToRhsmWithOptions from "+clienttasks.hostname+"...";
 		RemoteFileTasks.markFile(proxyRunner, proxyLog, proxyLogMarker);
 
 		// execute rhn-migrate-classic-to-rhsm with options
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsm(options,rhnUsername,rhnPassword,regUsername,regPassword,regOrg,null);
 		
 		// assert that traffic to RHSM went through the proxy
 		proxyLogResult = RemoteFileTasks.getTailFromMarkedFile(proxyRunner, proxyLog, proxyLogMarker, clienttasks.ipaddr);	// accounts for multiple tests hitting the same proxy server simultaneously
 		Assert.assertContainsMatch(proxyLogResult, proxyLogRegex, "The proxy server appears to be logging the expected connection attempts to RHN.");
 
 		// assert the exit code
 		String expectedMsg;
 		if (!areAllChannelsMapped(rhnChannelsConsumed) && !options.contains("-f")/*--force*/) {	// when not all of the rhnChannelsConsumed have been mapped to a productCert and no --force has been specified.
 			log.warning("Not all of the channels are mapped to a product cert.  Therefore, the "+rhnMigrateTool+" command should have exited with code 1.");
 			expectedMsg = "Use --force to ignore these channels and continue the migration.";
 			Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+expectedMsg);	
 			Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "ExitCode from call to '"+rhnMigrateTool+" "+options+"' when any of the channels are not mapped to a productCert.");
 			Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' indicates this system is still registered using RHN Classic when rhn-migrate-classic-to-rhsm requires --force to continue.");
 			
 			// assert that we are not yet registered to RHSM
 			Assert.assertNull(clienttasks.getCurrentConsumerCert(),"We should NOT be registered to RHSM when "+rhnMigrateTool+" requires --force to continue.");
 			
 			// assert that we are still registered to RHN
 			Assert.assertTrue(clienttasks.isRhnSystemIdRegistered(rhnUsername, rhnPassword, rhnHostname, rhnSystemId),"Confirmed that rhn systemId '"+rhnSystemId+"' is still registered since our migration attempt requires --force to continue.");
 
 			return;
 		}
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(0), "ExitCode from call to '"+rhnMigrateTool+" "+options+"' when all of the channels are mapped.");
 
 		// assert that proxy configurations from RHN up2date have been copied to RHSM rhsm.conf
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_hostname"), proxy_hostnameConfig.replace("http://", ""), "The RHN hostname component from the httpProxy configuration in "+clienttasks.rhnUp2dateFile+" has been copied to the RHSM server.proxy_hostname configuration in "+clienttasks.rhsmConfFile+" (with prefix \"http://\" removed; reference bug 798015).");
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_port"), proxy_portConfig, "The RHN port component from the httpProxy configuration in "+clienttasks.rhnUp2dateFile+" has been copied to the RHSM server.proxy_port configuration in "+clienttasks.rhsmConfFile+".");
 		if (clienttasks.getConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth").equals("0") || clienttasks.getConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth").equalsIgnoreCase("false")) {
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_user"), "", "The RHSM server.proxy_user configuration in "+clienttasks.rhsmConfFile+" is removed when RHN configuration enableProxyAuth is false.");
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_password"), "", "The RHSM server.proxy_password configuration in "+clienttasks.rhsmConfFile+" is removed when RHN configuration enableProxyAuth is false.");
 		} else {
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_user"), proxy_userConfig, "The RHN proxyUser configuration in "+clienttasks.rhnUp2dateFile+" has been copied to the RHSM server.proxy_user configuration in "+clienttasks.rhsmConfFile+" when RHN configuration enableProxyAuth is true.");
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "proxy_password"), proxy_passwordConfig, "The RHN proxyPassword configuration in "+clienttasks.rhnUp2dateFile+" has been copied to the RHSM server.proxy_password configuration in "+clienttasks.rhsmConfFile+" when RHN configuration enableProxyAuth is true.");
 		}
 		
 		// assert we are no longer registered to RHN Classic
 		expectedMsg = "System successfully unregistered from RHN Classic.";
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+expectedMsg);
 		Assert.assertTrue(!RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' is absent.  This indicates this system is not registered using RHN Classic.");
 		Assert.assertTrue(!clienttasks.isRhnSystemIdRegistered(rhnUsername, rhnPassword, rhnHostname, rhnSystemId), "Confirmed that rhn systemId '"+rhnSystemId+"' is no longer registered.");
 
 		// assert that we are newly registered using rhsm
 		clienttasks.identity(null, null, null, null, null, null, null);
 		Assert.assertNotNull(clienttasks.getCurrentConsumerId(),"The existance of a consumer cert indicates that the system is currently registered using RHSM.");
 		expectedMsg = String.format("System '%s' successfully registered to Red Hat Subscription Management.",	clienttasks.hostname);
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to '"+rhnMigrateTool+" "+options+"' contains message: "+expectedMsg);
 
 		log.info("No need to assert any more details of the migration since this test proxy test since they are covered in the non-proxy test.");
 	}
 	
 	
 	@Test(	description="Execute migration tool rhn-migrate-classic-to-rhsm with a non-default rhsm.productcertdir configured",
 			groups={"RhnMigrateClassicToRhsmWithNonDefaultProductCertDir_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			dataProvider="RhnMigrateClassicToRhsmWithNonDefaultProductCertDirData",	// dataProvider="RhnMigrateClassicToRhsmData",  IS TOO TIME CONSUMING
 			enabled=true)
 	@ImplementsNitrateTest(caseId=130765)
 	public void RhnMigrateClassicToRhsmWithNonDefaultProductCertDir_Test(Object bugzilla, String rhnUsername, String rhnPassword, String rhnServer, List<String> rhnChannelsToAdd, String options, String regUsername, String regPassword, String regOrg, Integer serviceLevelIndex, String serviceLevelExpected) {
 		// NOTE: The configNonDefaultRhsmProductCertDir will handle the configuration setting
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm", "productCertDir"), nonDefaultProductCertDir,"A non-default rhsm.productCertDir has been configured.");
 		RhnMigrateClassicToRhsm_Test(bugzilla,rhnUsername,rhnPassword,rhnServer,rhnChannelsToAdd,options,regUsername,regPassword,regOrg,serviceLevelIndex,serviceLevelExpected);
 	}
 	
 	
 	@Test(	description="migrating a RHEL5 Client - Desktop versus Workstation",
 			groups={"blockedByBug-786257","blockedByBug-853233","RhnMigrateClassicToRhsm_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	public void RhnMigrateClassicToRhsm_Rhel5ClientDesktopVersusWorkstation_Test() {
 		if (sm_rhnHostname.equals("")) throw new SkipException("This test requires access to RHN Classic.");
 
 		log.info("Red Hat Enterprise Linux Desktop (productId=68) corresponds to the base RHN Channel (rhel-ARCH-client-5) for a 5Client system where ARCH=i386,x86_64.");
 		log.info("Red Hat Enterprise Linux Workstation (productId=71) corresponds to child RHN Channel (rhel-ARCH-client-workstation-5) for a 5Client system where ARCH=i386,x86_64.");	
 		log.info("After migrating from RHN Classic to RHSM, these two product certs should not be installed at the same time.");
 
 		// when we are migrating away from RHN Classic to a non-hosted candlepin server, choose the credentials that will be used to register
 		String regUsername=null, regPassword=null, regOrg=null;
 		if (!isCurrentlyConfiguredServerTypeHosted()) {	// or this may work too: if (!sm_serverType.equals(CandlepinType.hosted)) {
 			regUsername = sm_clientUsername;
 			regPassword = sm_clientPassword;
 			regOrg = sm_clientOrg;
 		}
 		
 		//	2273         "Name": "Red Hat Enterprise Linux Desktop", 
 		//	2274         "Product ID": "68", 
 		//	2275         "RHN Channels": [
 		//	2276             "rhel-i386-client-5", 
 		//	2277             "rhel-i386-client-5-beta", 
 		//	2278             "rhel-i386-client-5-beta-debuginfo", 
 		//	2279             "rhel-i386-client-5-debuginfo", 
 		//	2280             "rhel-i386-client-6", 
 		//	2281             "rhel-i386-client-6-beta", 
 		//	2282             "rhel-i386-client-6-beta-debuginfo", 
 		//	2283             "rhel-i386-client-6-debuginfo", 
 		//	2284             "rhel-i386-client-optional-6", 
 		//	2285             "rhel-i386-client-optional-6-beta", 
 		//	2286             "rhel-i386-client-optional-6-beta-debuginfo", 
 		//	2287             "rhel-i386-client-optional-6-debuginfo", 
 		//	2288             "rhel-i386-client-supplementary-5", 
 		//	2289             "rhel-i386-client-supplementary-5-beta", 
 		//	2290             "rhel-i386-client-supplementary-5-beta-debuginfo", 
 		//	2291             "rhel-i386-client-supplementary-5-debuginfo", 
 		//	2292             "rhel-i386-client-supplementary-6", 
 		//	2293             "rhel-i386-client-supplementary-6-beta", 
 		//	2294             "rhel-i386-client-supplementary-6-beta-debuginfo", 
 		//	2295             "rhel-i386-client-supplementary-6-debuginfo", 
 		//	2296             "rhel-i386-rhev-agent-5-client", 
 		//	2297             "rhel-i386-rhev-agent-5-client-beta", 
 		//	2298             "rhel-i386-rhev-agent-6-client", 
 		//	2299             "rhel-i386-rhev-agent-6-client-beta", 
 		//	2300             "rhel-i386-rhev-agent-6-client-beta-debuginfo", 
 		//	2301             "rhel-i386-rhev-agent-6-client-debuginfo", 
 		//	2302             "rhel-x86_64-client-5", 
 		//	2303             "rhel-x86_64-client-5-beta", 
 		//	2304             "rhel-x86_64-client-5-beta-debuginfo", 
 		//	2305             "rhel-x86_64-client-5-debuginfo", 
 		//	2306             "rhel-x86_64-client-6", 
 		//	2307             "rhel-x86_64-client-6-beta", 
 		//	2308             "rhel-x86_64-client-6-beta-debuginfo", 
 		//	2309             "rhel-x86_64-client-6-debuginfo", 
 		//	2310             "rhel-x86_64-client-optional-6", 
 		//	2311             "rhel-x86_64-client-optional-6-beta", 
 		//	2312             "rhel-x86_64-client-optional-6-beta-debuginfo", 
 		//	2313             "rhel-x86_64-client-optional-6-debuginfo", 
 		//	2314             "rhel-x86_64-client-supplementary-5", 
 		//	2315             "rhel-x86_64-client-supplementary-5-beta", 
 		//	2316             "rhel-x86_64-client-supplementary-5-beta-debuginfo", 
 		//	2317             "rhel-x86_64-client-supplementary-5-debuginfo", 
 		//	2318             "rhel-x86_64-client-supplementary-6", 
 		//	2319             "rhel-x86_64-client-supplementary-6-beta", 
 		//	2320             "rhel-x86_64-client-supplementary-6-beta-debuginfo", 
 		//	2321             "rhel-x86_64-client-supplementary-6-debuginfo", 
 		//	2322             "rhel-x86_64-rhev-agent-5-client", 
 		//	2323             "rhel-x86_64-rhev-agent-5-client-beta", 
 		//	2324             "rhel-x86_64-rhev-agent-6-client", 
 		//	2325             "rhel-x86_64-rhev-agent-6-client-beta", 
 		//	2326             "rhel-x86_64-rhev-agent-6-client-beta-debuginfo", 
 		//	2327             "rhel-x86_64-rhev-agent-6-client-debuginfo"
 		//	2328         ]
 		
 		//	10289         "Name": "Red Hat Enterprise Linux Workstation", 
 		//	10290         "Product ID": "71", 
 		//	10291         "RHN Channels": [
 		//	10292             "rhel-i386-client-5", 
 		//	10293             "rhel-i386-client-5-beta", 
 		//	10294             "rhel-i386-client-5-beta-debuginfo", 
 		//	10295             "rhel-i386-client-5-debuginfo", 
 		//	10296             "rhel-i386-client-supplementary-5", 
 		//	10297             "rhel-i386-client-supplementary-5-beta", 
 		//	10298             "rhel-i386-client-supplementary-5-beta-debuginfo", 
 		//	10299             "rhel-i386-client-supplementary-5-debuginfo", 
 		//	10300             "rhel-i386-client-vt-5", 
 		//	10301             "rhel-i386-client-vt-5-beta", 
 		//	10302             "rhel-i386-client-vt-5-beta-debuginfo", 
 		//	10303             "rhel-i386-client-vt-5-debuginfo", 
 		//	10304             "rhel-i386-client-workstation-5", 
 		//	10305             "rhel-i386-client-workstation-5-beta", 
 		//	10306             "rhel-i386-client-workstation-5-beta-debuginfo", 
 		//	10307             "rhel-i386-client-workstation-5-debuginfo", 
 		//	10308             "rhel-i386-rhev-agent-6-workstation", 
 		//	10309             "rhel-i386-rhev-agent-6-workstation-beta", 
 		//	10310             "rhel-i386-rhev-agent-6-workstation-beta-debuginfo", 
 		//	10311             "rhel-i386-rhev-agent-6-workstation-debuginfo", 
 		//	10312             "rhel-i386-workstation-6", 
 		//	10313             "rhel-i386-workstation-6-beta", 
 		//	10314             "rhel-i386-workstation-6-beta-debuginfo", 
 		//	10315             "rhel-i386-workstation-6-debuginfo", 
 		//	10316             "rhel-i386-workstation-optional-6", 
 		//	10317             "rhel-i386-workstation-optional-6-beta", 
 		//	10318             "rhel-i386-workstation-optional-6-beta-debuginfo", 
 		//	10319             "rhel-i386-workstation-optional-6-debuginfo", 
 		//	10320             "rhel-i386-workstation-supplementary-6", 
 		//	10321             "rhel-i386-workstation-supplementary-6-beta", 
 		//	10322             "rhel-i386-workstation-supplementary-6-beta-debuginfo", 
 		//	10323             "rhel-i386-workstation-supplementary-6-debuginfo", 
 		//	10324             "rhel-x86_64-client-5", 
 		//	10325             "rhel-x86_64-client-5-beta", 
 		//	10326             "rhel-x86_64-client-5-beta-debuginfo", 
 		//	10327             "rhel-x86_64-client-5-debuginfo", 
 		//	10328             "rhel-x86_64-client-supplementary-5", 
 		//	10329             "rhel-x86_64-client-supplementary-5-beta", 
 		//	10330             "rhel-x86_64-client-supplementary-5-beta-debuginfo", 
 		//	10331             "rhel-x86_64-client-supplementary-5-debuginfo", 
 		//	10332             "rhel-x86_64-client-vt-5", 
 		//	10333             "rhel-x86_64-client-vt-5-beta", 
 		//	10334             "rhel-x86_64-client-vt-5-beta-debuginfo", 
 		//	10335             "rhel-x86_64-client-vt-5-debuginfo", 
 		//	10336             "rhel-x86_64-client-workstation-5", 
 		//	10337             "rhel-x86_64-client-workstation-5-beta", 
 		//	10338             "rhel-x86_64-client-workstation-5-beta-debuginfo", 
 		//	10339             "rhel-x86_64-client-workstation-5-debuginfo", 
 		//	10340             "rhel-x86_64-rhev-agent-6-workstation", 
 		//	10341             "rhel-x86_64-rhev-agent-6-workstation-beta", 
 		//	10342             "rhel-x86_64-rhev-agent-6-workstation-beta-debuginfo", 
 		//	10343             "rhel-x86_64-rhev-agent-6-workstation-debuginfo", 
 		//	10344             "rhel-x86_64-workstation-6", 
 		//	10345             "rhel-x86_64-workstation-6-beta", 
 		//	10346             "rhel-x86_64-workstation-6-beta-debuginfo", 
 		//	10347             "rhel-x86_64-workstation-6-debuginfo", 
 		//	10348             "rhel-x86_64-workstation-optional-6", 
 		//	10349             "rhel-x86_64-workstation-optional-6-beta", 
 		//	10350             "rhel-x86_64-workstation-optional-6-beta-debuginfo", 
 		//	10351             "rhel-x86_64-workstation-optional-6-debuginfo", 
 		//	10352             "rhel-x86_64-workstation-supplementary-6", 
 		//	10353             "rhel-x86_64-workstation-supplementary-6-beta", 
 		//	10354             "rhel-x86_64-workstation-supplementary-6-beta-debuginfo", 
 		//	10355             "rhel-x86_64-workstation-supplementary-6-debuginfo"
 		//	10356         ]
 		
 		// this test is only applicable on a RHEL 5Client
 		final String applicableReleasever = "5Client";
 		if (!clienttasks.releasever.equals(applicableReleasever)) throw new SkipException("This test is only executable when the redhat-release is '"+applicableReleasever+"'.");
 		
 		// decide what product arch applies to our system
 		String arch = clienttasks.arch;	// default
 		//if (clienttasks.redhatReleaseX.equals("5") && clienttasks.arch.equals("ppc64")) arch = "ppc";	// RHEL5 only supports ppc packages, but can be run on ppc64 hardware
 		if (Arrays.asList("i386","i486","i586","i686").contains(clienttasks.arch)) arch = "i386";		// RHEL supports i386 packages, but can be run on all 32-bit arch hardware
 		if (!Arrays.asList("i386","x86_64").contains(arch)) Assert.fail("RHEL "+applicableReleasever+" should only be available on i386 and x86_64 arches (not: "+arch+").") ;
 		
 		
 		// Case 1: add RHN Channels for Desktop only; migration should only install Desktop product 68
 		List<String> rhnChannelsToAddForDesktop = new ArrayList<String>();
 		//rhnChannelsToAdd.add(String.format("rhel-%s-client-5",arch));	// this is the base channel and will already be consumed by rhnreg_ks
 		rhnChannelsToAddForDesktop.add(String.format("rhel-%s-client-5-beta",arch));
 		rhnChannelsToAddForDesktop.add(String.format("rhel-%s-client-5-beta-debuginfo",arch));
 		rhnChannelsToAddForDesktop.add(String.format("rhel-%s-client-5-debuginfo",arch));
 		rhnChannelsToAddForDesktop.add(String.format("rhel-%s-client-supplementary-5",arch));
 		rhnChannelsToAddForDesktop.add(String.format("rhel-%s-client-supplementary-5-beta",arch));
 		rhnChannelsToAddForDesktop.add(String.format("rhel-%s-client-supplementary-5-beta-debuginfo",arch));
 		rhnChannelsToAddForDesktop.add(String.format("rhel-%s-client-supplementary-5-debuginfo",arch));
 		RhnMigrateClassicToRhsm_Test(null,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnChannelsToAddForDesktop, "--no-auto", regUsername,regPassword,regOrg,null, null);		
 		List<ProductCert> productCertsMigrated = clienttasks.getCurrentProductCerts();
 		String productIdForDesktop = "68";
 		for (ProductCert productCert : productCertsMigrated) {
 			Assert.assertEquals(productCert.productId, productIdForDesktop, "Migration tool "+rhnMigrateTool+" should only install product certificate id '"+productIdForDesktop+"' when consuming RHN Child Channels "+rhnChannelsToAddForDesktop);
 		}
 		
 		// Case 2: add RHN Channels for Workstation only; migration should only install Workstation product 71
 		List<String> rhnChannelsToAddForWorkstation = new ArrayList<String>();
 		//rhnChannelsToAdd.add(String.format("rhel-%s-client-5",arch));	// this is the base channel and will already be consumed by rhnreg_ks
 		/*
 		rhnChannelsToAddForWorkstation.add(String.format("rhel-%s-client-vt-5",arch));
 		rhnChannelsToAddForWorkstation.add(String.format("rhel-%s-client-vt-5-beta",arch));
 		rhnChannelsToAddForWorkstation.add(String.format("rhel-%s-client-vt-5-beta-debuginfo",arch));
 		rhnChannelsToAddForWorkstation.add(String.format("rhel-%s-client-vt-5-debuginfo",arch));
 		*/
 		rhnChannelsToAddForWorkstation.add(String.format("rhel-%s-client-workstation-5",arch));
 		rhnChannelsToAddForWorkstation.add(String.format("rhel-%s-client-workstation-5-beta",arch));
 		rhnChannelsToAddForWorkstation.add(String.format("rhel-%s-client-workstation-5-beta-debuginfo",arch));
 		rhnChannelsToAddForWorkstation.add(String.format("rhel-%s-client-workstation-5-debuginfo",arch));
 		RhnMigrateClassicToRhsm_Test(null,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnChannelsToAddForWorkstation, "--no-auto", regUsername,regPassword,regOrg,null, null);		
 		productCertsMigrated = clienttasks.getCurrentProductCerts();
 		String productIdForWorkstation = "71";
 		for (ProductCert productCert : productCertsMigrated) {
 			Assert.assertEquals(productCert.productId, productIdForWorkstation, "Migration tool "+rhnMigrateTool+" should only install product certificate id '"+productIdForWorkstation+"' when consuming RHN Child Channels "+rhnChannelsToAddForWorkstation);
 		}
 		
 		// Case 3: add RHN Channels for Virtualization only; migration should only install Workstation product 71
 		// Bug 853233 - rhn-migrate-classic-to-rhsm is installing both Desktop(68) and Workstation(71) when rhel-ARCH-client-vt-5 channel is consumed 
 		List<String> rhnChannelsToAddForVirtualization = new ArrayList<String>();
 		//rhnChannelsToAdd.add(String.format("rhel-%s-client-5",arch));	// this is the base channel and will already be consumed by rhnreg_ks
 		rhnChannelsToAddForVirtualization.add(String.format("rhel-%s-client-vt-5",arch));
 		rhnChannelsToAddForVirtualization.add(String.format("rhel-%s-client-vt-5-beta",arch));
 		rhnChannelsToAddForVirtualization.add(String.format("rhel-%s-client-vt-5-beta-debuginfo",arch));
 		rhnChannelsToAddForVirtualization.add(String.format("rhel-%s-client-vt-5-debuginfo",arch));
 		RhnMigrateClassicToRhsm_Test(null,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnChannelsToAddForVirtualization, "--no-auto", regUsername,regPassword,regOrg,null, null);		
 		productCertsMigrated = clienttasks.getCurrentProductCerts();
 		/*String*/ productIdForWorkstation = "71";
 		for (ProductCert productCert : productCertsMigrated) {
 			Assert.assertEquals(productCert.productId, productIdForWorkstation, "Migration tool "+rhnMigrateTool+" should only install product certificate id '"+productIdForWorkstation+"' when consuming RHN Child Channels "+rhnChannelsToAddForVirtualization);
 		}
 		
 		// Case 4: add RHN Channels for both Desktop and Workstation; migration should only install Workstation product 71
 		List<String> rhnChannelsToAddForBoth = new ArrayList<String>();
 		rhnChannelsToAddForBoth.addAll(rhnChannelsToAddForDesktop);
 		rhnChannelsToAddForBoth.addAll(rhnChannelsToAddForWorkstation);
 		rhnChannelsToAddForBoth.addAll(rhnChannelsToAddForVirtualization);
 		RhnMigrateClassicToRhsm_Test(null,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnChannelsToAddForBoth, "--no-auto", regUsername,regPassword,regOrg,null, null);		
 		productCertsMigrated = clienttasks.getCurrentProductCerts();
 		for (ProductCert productCert : productCertsMigrated) {
 			Assert.assertEquals(productCert.productId, productIdForWorkstation, "Migration tool "+rhnMigrateTool+" should only install product certificate id '"+productIdForWorkstation+"' when consuming RHN Child Channels "+rhnChannelsToAddForBoth);
 		}
 	}
 	
 	
 	@Test(	description="when more than one JBoss Application Enterprise Platform (JBEAP) RHN Channel is currently being consumed classically, rhn-migrate-to-rhsm should abort",
 			groups={"blockedByBug-852894","RhnMigrateClassicToRhsm_Test"},
 			dependsOnMethods={"VerifyChannelCertMapping_Test"},
 			enabled=true)
 	public void RhnMigrateClassicToRhsm_MultipleVersionsOfJBEAP_Test() {
 		if (sm_rhnHostname.equals("")) throw new SkipException("This test requires access to RHN Classic.");
 
 		log.info("JBoss Enterprise Application Platform (productId=183) is currently provided in 3 versions: 4.3.0, 5.0, 6.0");
 		log.info("If RHN Channels providing more than one of these versions is currently being consumed, rhn-migrate-to-rhsm should abort.");
 
 		// when we are migrating away from RHN Classic to a non-hosted candlepin server, choose the credentials that will be used to register
 		String regUsername=null, regPassword=null, regOrg=null;
 		if (!isCurrentlyConfiguredServerTypeHosted()) {	// or this may work too: if (!sm_serverType.equals(CandlepinType.hosted)) {
 			regUsername = sm_clientUsername;
 			regPassword = sm_clientPassword;
 			regOrg = sm_clientOrg;
 		}
 		
 		//	32298         "Name": "JBoss Enterprise Application Platform", 
 		//	32299         "Product ID": "183", 
 		//	32300         "RHN Channels": [
 		//	32301             "jbappplatform-4.3.0-i386-server-5-rpm", 
 		//	32302             "jbappplatform-4.3.0-x86_64-server-5-rpm", 
 		//	32303             "jbappplatform-5-i386-server-5-rpm", 
 		//	32304             "jbappplatform-5-i386-server-6-rpm", 
 		//	32305             "jbappplatform-5-x86_64-server-5-rpm", 
 		//	32306             "jbappplatform-5-x86_64-server-6-rpm", 
 		//	32307             "jbappplatform-6-i386-server-6-rpm", 
 		//	32308             "jbappplatform-6-x86_64-server-6-rpm"
 		//	32309         ]
 		
 		// this test is only applicable on a RHEL 5Server,6Server and arches i386,x86_64
 		List<String> applicableReleasevers = Arrays.asList(new String[]{"5Server","6Server"});
 		List<String> applicableArchs = Arrays.asList(new String[]{"i386","x86_64"});
 		String arch = clienttasks.arch;	// default
 		//if (clienttasks.redhatReleaseX.equals("5") && clienttasks.arch.equals("ppc64")) arch = "ppc";	// RHEL5 only supports ppc packages, but can be run on ppc64 hardware
 		if (Arrays.asList("i386","i486","i586","i686").contains(clienttasks.arch)) arch = "i386";		// RHEL supports i386 packages, but can be run on all 32-bit arch hardware
 		if (!applicableReleasevers.contains(clienttasks.releasever)) throw new SkipException("This test is only executable on redhat-releases "+applicableReleasevers+" arches "+applicableArchs);
 		if (!applicableArchs.contains(arch)) throw new SkipException("This test is only executable on redhat-releases "+applicableReleasevers+" arches "+applicableArchs);
 		
 		List<String> rhnChannelsToAdd = new ArrayList<String>();
 		
 		// decide what jbappplatform channels to test
 		if (clienttasks.redhatReleaseX.equals("5")) {
 			rhnChannelsToAdd.add(String.format("jbappplatform-4.3.0-%s-server-5-rpm",arch));
 			rhnChannelsToAdd.add(String.format("jbappplatform-5-%s-server-5-rpm",arch));
 		} else if (clienttasks.redhatReleaseX.equals("6")) {
 			rhnChannelsToAdd.add(String.format("jbappplatform-5-%s-server-6-rpm",arch));
 			rhnChannelsToAdd.add(String.format("jbappplatform-6-%s-server-6-rpm",arch));
 		} else {
 			Assert.fail("This test needs additional RHN Channel information for jbappplatform product 183 on RHEL Release '"+clienttasks.redhatReleaseX+"'.");
 		}
 			
 		// make sure we are NOT registered to RHSM
 		clienttasks.unregister(null,null,null);
 		clienttasks.removeAllCerts(false, false, true);
 		clienttasks.removeAllFacts();
 		
 		// register to RHN Classic
 		String rhnSystemId = clienttasks.registerToRhnClassic(sm_rhnUsername, sm_rhnPassword, sm_rhnHostname);
 		Assert.assertTrue(clienttasks.isRhnSystemIdRegistered(sm_rhnUsername, sm_rhnPassword, sm_rhnHostname, rhnSystemId),"Confirmed that rhn systemId '"+rhnSystemId+"' is currently registered.");
 		
 		// subscribe to more RHN Classic channels
 		addRhnClassicChannels(sm_rhnUsername, sm_rhnPassword, rhnChannelsToAdd);
 		List<String> rhnChannelsConsumed = getCurrentRhnClassicChannels();
 		Assert.assertTrue(rhnChannelsConsumed.containsAll(rhnChannelsToAdd), "All of the RHN Classic channels added appear to be consumed.");
 
 		// execute rhn-migrate-classic-to-rhsm and assert the results
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsm(null,sm_rhnUsername, sm_rhnPassword,regUsername,regPassword,regOrg,null);
 		String expectedMsg = "You are subscribed to more than one jbappplatform channel.  This script does not support that configuration.  Exiting.";
 		Assert.assertTrue(sshCommandResult.getStdout().contains(expectedMsg), "Stdout from call to '"+rhnMigrateTool+" when consuming RHN Channels for multiple versions of JBEAP contains message: "+expectedMsg);	
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "ExitCode from call to '"+rhnMigrateTool+" when consuming RHN Channels for multiple versions of JBEAP "+rhnChannelsToAdd);
 		
 		// assert that no product certs have been copied yet
 		Assert.assertEquals(clienttasks.getCurrentlyInstalledProducts().size(), 0, "No productCerts have been migrated when "+rhnMigrateTool+" was aborted.");
 
 		// assert that we are not yet registered to RHSM
 		Assert.assertNull(clienttasks.getCurrentConsumerCert(),"We should NOT be registered to RHSM when "+rhnMigrateTool+" was aborted.");
 		
 		// assert that we are still registered to RHN
 		Assert.assertTrue(clienttasks.isRhnSystemIdRegistered(sm_rhnUsername, sm_rhnPassword, sm_rhnHostname, rhnSystemId),"Confirmed that rhn systemId '"+rhnSystemId+"' is still registered when '"+rhnMigrateTool+" was aborted.");
 		Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"The system id file '"+clienttasks.rhnSystemIdFile+"' exists.  This indicates this system is still registered using RHN Classic.");
 	}
 	
 	
 	@Test(	description="Execute migration tool rhn-migrate-classic-to-rhsm with invalid credentials",
 			groups={"blockedByBug-789008","blockedByBug-807477"},
 			dependsOnMethods={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=136404)
 	public void RhnMigrateClassicToRhsmWithInvalidCredentials_Test() {
 		clienttasks.unregister(null,null,null);
 		String regUsername=null, regPassword=null, regOrg=null;
 		if (!sm_serverType.equals(CandlepinType.hosted)) {regUsername="foo"; regPassword="bar";}
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsm(null,"foo","bar",regUsername,regPassword,regOrg,null);
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "The expected exit code from call to '"+rhnMigrateTool+"' with invalid credentials.");
 		//Assert.assertContainsMatch(sshCommandResult.getStdout(), "Unable to connect to certificate server.  See "+clienttasks.rhsmLogFile+" for more details.", "The expected stdout result from call to "+rhnMigrateTool+" with invalid credentials.");		// valid prior to bug fix 789008
 		String expectedStdout = "Unable to connect to certificate server: "+servertasks.invalidCredentialsMsg()+".  See "+clienttasks.rhsmLogFile+" for more details.";
 		Assert.assertTrue(sshCommandResult.getStdout().trim().endsWith(expectedStdout), "The expected stdout result from call to '"+rhnMigrateTool+"' with invalid credentials ended with: "+expectedStdout);
 	}
 	
 	
 	@Test(	description="Execute migration tool rhn-migrate-classic-to-rhsm with invalid credentials, but valid subscription-manager credentials",
 			groups={},
 			dependsOnMethods={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RhnMigrateClassicToRhsmWithInvalidRhnCredentials_Test() {
 		if (sm_serverType.equals(CandlepinType.hosted)) throw new SkipException("This test requires that your candlepin server NOT be a hosted RHN Classic system.");
 		clienttasks.unregister(null,null,null);
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsm(null,"foo","bar",sm_clientUsername,sm_clientPassword,sm_clientOrg,null);
 		String expectedStdout = "Unable to authenticate to RHN Classic.  See /var/log/rhsm/rhsm.log for more details.";
 		Assert.assertTrue(sshCommandResult.getStdout().trim().endsWith(expectedStdout), "The expected stdout result from call to '"+rhnMigrateTool+"' with invalid rhn credentials and valid subscription-manager credentials ended with: "+expectedStdout);
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "The expected exit code from call to '"+rhnMigrateTool+"' with invalid credentials.");
 	}
 	
 	
 	@Test(	description="Execute migration tool rhn-migrate-classic-to-rhsm without having registered to classic (no /etc/sysconfig/rhn/systemid)",
 			groups={"blockedByBug-807477","AcceptanceTests"},
 			dependsOnMethods={},
 			enabled=true)
 	public void RhnMigrateClassicToRhsmWithMissingSystemIdFile_Test() {
 	    removeProxyServerConfigurations();	// cleanup from prior tests
 	    clienttasks.unregister(null,null,null);
 	    clienttasks.removeRhnSystemIdFile();
 		Assert.assertTrue(!RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile),"This system is not registered using RHN Classic.");
 		
 		// when we are migrating away from RHN Classic to a non-hosted candlepin server, choose the credentials that will be used to register
 		String regUsername=null, regPassword=null, regOrg=null;
 		if (!isCurrentlyConfiguredServerTypeHosted()) {	// or this may work too: if (!sm_serverType.equals(CandlepinType.hosted)) {
 			regUsername = sm_clientUsername;
 			regPassword = sm_clientPassword;
 			regOrg = sm_clientOrg;
 		}
 		
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsm(null,sm_rhnUsername,sm_rhnPassword,regUsername,regPassword,regOrg,null);
 		String expectedStdout = "Unable to locate SystemId file. Is this system registered?";
 		Assert.assertTrue(sshCommandResult.getStdout().trim().endsWith(expectedStdout), "The expected stdout result from call to '"+rhnMigrateTool+"' without having registered to RHN Classic ended with: "+expectedStdout);
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "The expected exit code from call to '"+rhnMigrateTool+"' without having registered to RHN Classic.");
 	}
 	
 	
 	@Test(	description="Attempt to execute migration tool rhn-migrate-classic-to-rhsm with --no-auto and --service-level",
 			groups={"blockedByBug-850920"},
 			dependsOnMethods={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RhnMigrateClassicToRhsmWithNoAutoAndServiceLevel_Test() {
 		clienttasks.unregister(null,null,null);
 		
 		SSHCommandResult sshCommandResult = executeRhnMigrateClassicToRhsm("--no-auto --servicelevel=foo", sm_rhnUsername, sm_rhnPassword,null,null,null,null);
 		String expectedStdout = "The --servicelevel and --no-auto options cannot be used together.";
 		Assert.assertTrue(sshCommandResult.getStdout().trim().endsWith(expectedStdout), "Stdout from call to '"+rhnMigrateTool+"' specifying both --no-auto and --servicelevel ended with: "+expectedStdout);
 		Assert.assertEquals(sshCommandResult.getStderr().trim(), "", "Stderr from call to '"+rhnMigrateTool+"' specifying both --no-auto and --servicelevel.");
 //		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "Exit code from call to '"+rhnMigrateTool+"' specifying both --no-auto and --servicelevel.");
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(0), "Exit code from call to '"+rhnMigrateTool+"' specifying both --no-auto and --servicelevel.");
 	}
 	
 	
 	@Test(	description="Execute migration tool rhn-migrate-classic-to-rhsm while already registered to RHSM",
 			groups={"blockedByBug-807477"},
 			dependsOnMethods={},
 			enabled=true)
 	public void RhnMigrateClassicToRhsmWhileAlreadyRegisteredToRhsm_Test() {
 		clienttasks.removeRhnSystemIdFile();
 		String consumerid = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (List<String>)null, null, null, true, null, null, null, null));
 		SSHCommandResult sshCommandResult;
 		if (isCurrentlyConfiguredServerTypeHosted()) {
 			// note that the validity of the username and password really do not matter for this test
 			sshCommandResult = executeRhnMigrateClassicToRhsm(null,sm_clientUsername,sm_clientPassword,null,null,null,null);
 		} else {
 			sshCommandResult = executeRhnMigrateClassicToRhsm(null,sm_clientUsername,sm_clientPassword,sm_clientUsername,sm_clientPassword,sm_clientOrg,null);
 		}
 		String expectedStdout;
		expectedStdout = "This machine appears to be already registered to Certificate-based RHN.  Exiting.\n\nPlease visit https://access.redhat.com/management/consumers/"+consumerid+" to view the profile details.";	// changed by bug 847380
		expectedStdout = "This machine appears to be already registered to Red Hat Subscription Management.  Exiting.\n\nPlease visit https://access.redhat.com/management/consumers/"+consumerid+" to view the profile details.";
 		Assert.assertTrue(sshCommandResult.getStdout().trim().endsWith(expectedStdout), "The expected stdout result from call to '"+rhnMigrateTool+"' while already registered to RHSM ended with: "+expectedStdout);
 //		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(1), "The expected exit code from call to '"+rhnMigrateTool+"' while already registered to RHSM.");
 		Assert.assertEquals(sshCommandResult.getExitCode(), new Integer(0), "The expected exit code from call to '"+rhnMigrateTool+"' while already registered to RHSM.");
 	}
 	
 
 
 	
 
 	
 	// Candidates for an automated Test:
 	// TODO Bug 789007 - Migrate with normal user (non org admin) user .
 	// TODO https://tcms.engineering.redhat.com/case/130762/?from_plan=5223
 	// TODO Bug 816377 - rhn-migrate-classic-to-rhsm throws traceback when subscription-manager-migration-data is not installed
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=816364#c6
 	// TODO Bug 786450 - “Install-num-migrate-to-rhsm “ command not working as expected for ppc64 box (TODO FIGURE OUT IF EXISTING AUTOMATION ALREADY COVERS THIS ON PPC64)
 	// TODO Bug 863428 - Migration failed with message Organization A has more than one environment. 
 	// TODO Bug 866579 - rhn-migrate-classic-to-rhsm leaves system unregistered when a non-existant environment is specified/mistyped 
 
 	// Configuration methods ***********************************************************************
 	
 
 	@BeforeClass(groups="setup")
 	public void setupBeforeClass() {
 		if (clienttasks==null) return;
 		
 		// determine the full path to the channelCertMappingFile
 		baseProductsDir+="-"+clienttasks.redhatReleaseX;
 		channelCertMappingFilename = baseProductsDir+"/"+channelCertMappingFilename;
 		
 		// make sure needed rpms are installed
 		for (String pkg : new String[]{"subscription-manager-migration", "subscription-manager-migration-data", "expect"}) {
 			Assert.assertTrue(clienttasks.isPackageInstalled(pkg),"Required package '"+pkg+"' is installed for MigrationTests.");
 		}
 	}
 	
 	
 	@BeforeClass(groups="setup", dependsOnMethods={"setupBeforeClass"})
 	public void rememberOriginallyInstalledRedHatProductCertsBeforeClass() {
 		
 		// review the currently installed product certs and filter out the ones from test automation (indicated by suffix "_.pem")
 		for (File productCertFile : clienttasks.getCurrentProductCertFiles()) {
 			if (!productCertFile.getName().endsWith("_.pem")) {	// The product cert files ending in "_.pem" are not true RedHat products
 				originallyInstalledRedHatProductCerts.add(clienttasks.getProductCertFromProductCertFile(productCertFile));
 			}
 		}
 	}
 	
 	@BeforeClass(groups="setup", dependsOnMethods={"setupBeforeClass"})
 	public void rememberOriginallyConfiguredServerUrlBeforeClass() {
 		if (clienttasks==null) return;
 		
 		originalServerHostname 	= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "hostname");
 		originalServerPort		= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "port");
 		originalServerPrefix	= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "prefix");
 	}
 	
 	@BeforeClass(groups="setup", dependsOnMethods={"setupBeforeClass"})
 	public void backupProductCertsBeforeClass() {
 		
 		// determine the original productCertDir value
 		//productCertDirRestore = clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm", "productCertDir");
 		originalProductCertDir = clienttasks.productCertDir;
 		
 		log.info("Backing up all the currently installed product certs...");
 		client.runCommandAndWait("mkdir -p "+backupProductCertDir+"; rm -f "+backupProductCertDir+"/*.pem");
 		client.runCommandAndWait("cp "+originalProductCertDir+"/*.pem "+backupProductCertDir);
 	}
 	
 	@BeforeGroups(groups="setup",value={"InstallNumMigrateToRhsmWithInstNumber_Test","InstallNumMigrateToRhsm_Test","RhnMigrateClassicToRhsm_Test"})
 	public void configOriginalRhsmProductCertDir() {
 		if (clienttasks==null) return;
 		
 		//clienttasks.config(false, false, true, new String[]{"rhsm","productcertdir",productCertDirOriginal});
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", originalProductCertDir);
 	}
 	
 	@BeforeClass(groups="setup")
 	@AfterGroups(groups="setup",value={"RhnMigrateClassicToRhsmUsingProxyServer_Test"})
 	public void removeProxyServerConfigurations() {
 		if (clienttasks==null) return;
 		
 		// remove proxy settings from rhsm.conf
 		// these will actually remove the value from the config file; don't do this
 		//clienttasks.config(false, true, false, new String[]{"server","proxy_hostname"});
 		//clienttasks.config(false, true, false, new String[]{"server","proxy_user"});
 		//clienttasks.config(false, true, false, new String[]{"server","proxy_password"});
 		//clienttasks.config(false, true, false, new String[]{"server","proxy_port"});
 		clienttasks.config(false, false, true, new String[]{"server","proxy_hostname",""});
 		clienttasks.config(false, false, true, new String[]{"server","proxy_user",""});
 		clienttasks.config(false, false, true, new String[]{"server","proxy_password",""});
 		clienttasks.config(false, false, true, new String[]{"server","proxy_port",""});
 		
 		// remove proxy settings from up2date
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxy", "0");		// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "httpProxy", "");			// httpProxy[comment]=HTTP proxy in host:port format, e.g. squid.redhat.com:3128
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "enableProxyAuth", "0");	// enableProxyAuth[comment]=To use an authenticated proxy or not
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyUser", "");			// proxyUser[comment]=The username for an authenticated proxy
 		clienttasks.updateConfFileParameter(clienttasks.rhnUp2dateFile, "proxyPassword", "");		// proxyPassword[comment]=The password to use for an authenticated proxy
 		
 		iptablesAcceptPort(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "port"));
 	}
 	
 	@BeforeClass(groups="setup", dependsOnMethods={"setupBeforeClass"})
 	public void copyScriptsToClient() throws IOException {
 		// copy the rhn-channels.py script to the client
 		File rhnChannelsScriptFile = new File(System.getProperty("automation.dir", null)+"/scripts/rhn-channels.py");
 		if (!rhnChannelsScriptFile.exists()) Assert.fail("Failed to find expected script: "+rhnChannelsScriptFile);
 		RemoteFileTasks.putFile(client.getConnection(), rhnChannelsScriptFile.toString(), "/usr/local/bin/", "0755");
 		
 		// copy the rhn-is-registered.py script to the client
 		File rhnIsRegisteredScriptFile = new File(System.getProperty("automation.dir", null)+"/scripts/rhn-is-registered.py");
 		if (!rhnIsRegisteredScriptFile.exists()) Assert.fail("Failed to find expected script: "+rhnIsRegisteredScriptFile);
 		RemoteFileTasks.putFile(client.getConnection(), rhnIsRegisteredScriptFile.toString(), "/usr/local/bin/", "0755");
 
 		// copy the rhn-migrate-classic-to-rhsm.tcl script to the client
 		File expectScriptFile = new File(System.getProperty("automation.dir", null)+"/scripts/rhn-migrate-classic-to-rhsm.tcl");
 		if (!expectScriptFile.exists()) Assert.fail("Failed to find expected script: "+expectScriptFile);
 		RemoteFileTasks.putFile(client.getConnection(), expectScriptFile.toString(), "/usr/local/bin/", "0755");
 	}
 	
 	@BeforeClass(groups="setup", dependsOnMethods={"setupBeforeClass","copyScriptsToClient"})
 	public void determineRhnClassicBaseAndAvailableChildChannels() throws IOException {
 		if (sm_rhnUsername.equals("")) {log.warning("Skipping determination of the base and available RHN Classic channels"); return;}
 		if (sm_rhnPassword.equals("")) {log.warning("Skipping determination of the base and available RHN Classic channels"); return;}
 		if (sm_rhnHostname.equals("")) {log.warning("Skipping determination of the base and available RHN Classic channels"); return;}
 
 		// get the base channel
 		clienttasks.registerToRhnClassic(sm_rhnUsername, sm_rhnPassword, sm_rhnHostname);
 		List<String> rhnChannels = getCurrentRhnClassicChannels();
 		Assert.assertEquals(rhnChannels.size(), 1, "The number of base RHN Classic base channels this system is consuming.");
 		rhnBaseChannel = getCurrentRhnClassicChannels().get(0);
 
 		// get all of the available RHN Classic child channels available for consumption under this base channel
 		rhnAvailableChildChannels.clear();
 		String command = String.format("rhn-channels.py --username=%s --password=%s --server=%s --basechannel=%s --no-custom --available", sm_rhnUsername, sm_rhnPassword, sm_rhnHostname, rhnBaseChannel);
 		//debugTesting if (true) command = "echo rhel-x86_64-server-5 && echo rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5 && echo rhx-amanda-enterprise-backup-2.6-rhel-x86_64-server-5";
 //debugTesting if (true) return;
 		
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0));
 		rhnChannels = new ArrayList<String>();
 		if (!result.getStdout().trim().equals("")) {
 			rhnChannels	= Arrays.asList(result.getStdout().trim().split("\\n"));
 		}
 		for (String rhnChannel : rhnChannels) {
 			if (!rhnChannel.equals(rhnBaseChannel)) rhnAvailableChildChannels.add(rhnChannel.trim()); 
 		}
 		Assert.assertTrue(rhnAvailableChildChannels.size()>0,"A positive number of child channels under the RHN Classic base channel '"+rhnBaseChannel+"' are available for consumption.");
 
 	}
 	
 	@BeforeGroups(groups="setup",value={"InstallNumMigrateToRhsmWithNonDefaultProductCertDir_Test","RhnMigrateClassicToRhsmWithNonDefaultProductCertDir_Test"})
 	public void configNonDefaultRhsmProductCertDir() {
 		if (clienttasks==null) return;
 		
 		//clienttasks.config(false, false, true, new String[]{"rhsm","productcertdir",productCertDirNonDefault});
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", nonDefaultProductCertDir);
 	}
 
 	
 	public static SSHCommandRunner basicAuthProxyRunner = null;
 	public static SSHCommandRunner noAuthProxyRunner = null;
 	@BeforeClass(groups={"setup"})
 	public void setupProxyRunnersBeforeClass() throws IOException {
 		basicAuthProxyRunner = new SSHCommandRunner(sm_basicauthproxyHostname, sm_sshUser, sm_sshKeyPrivate, sm_sshkeyPassphrase, null);
 		noAuthProxyRunner = new SSHCommandRunner(sm_noauthproxyHostname, sm_sshUser, sm_sshKeyPrivate, sm_sshkeyPassphrase, null);
 	}
 	
 	@AfterClass(groups="setup")
 	public void restoreProductCertsAfterClass() {
 		if (clienttasks==null) return;
 		
 		log.info("Restoring the originally installed product certs...");
 		client.runCommandAndWait("rm -f "+originalProductCertDir+"/*.pem");
 		client.runCommandAndWait("cp "+backupProductCertDir+"/*.pem "+originalProductCertDir);
 		configOriginalRhsmProductCertDir();
 	}
 	
 	@AfterClass(groups="setup")
 	@AfterGroups(groups="setup",value={"RhnMigrateClassicToRhsm_Test"})
 	public void restoreOriginallyConfiguredServerUrl() {
 		if (clienttasks==null) return;
 		List<String[]> listOfSectionNameValues = new ArrayList<String[]>();
 		listOfSectionNameValues.add(new String[]{"server","hostname",originalServerHostname});
 		listOfSectionNameValues.add(new String[]{"server","port",originalServerPort});
 		listOfSectionNameValues.add(new String[]{"server","prefix",originalServerPrefix});
 		log.info("Restoring the originally configured server URL...");
 		clienttasks.config(null, null, true, listOfSectionNameValues);
 	}
 	
 	@BeforeClass(groups={"setup"})
 	public void determineCdnProductBaselineMapsBeforeClass() throws IOException, JSONException {
 
 		// Reference: https://engineering.redhat.com/trac/rcm/wiki/Projects/CDNBaseline
 
 		// THE JSON LOOKS LIKE THIS...
 		//	[
 		//		{
 		//			"Content Sets": [
 		//				{
 		//					"Label": "rhel-hpn-for-rhel-6-server-source-rpms", 
 		//					"Repos": [
 		//						{
 		//							"Relative URL": "/content/dist/rhel/server/6/6.1/i386/hpn/source/SRPMS"
 		//						}, 
 		//						{
 		//							"Relative URL": "/content/dist/rhel/server/6/6.2/x86_64/hpn/source/SRPMS"
 		//						}
 		//					]
 		//				}
 		//			], 
 		//			"Name": "Red Hat Enterprise Linux High Performance Networking (for RHEL Server)", 
 		//			"Product ID": "132", 
 		//			"RHN Channels": [
 		//				"rhel-x86_64-server-hpn-6", 
 		//				"rhel-x86_64-server-hpn-6-beta-debuginfo", 
 		//				"rhel-x86_64-server-hpn-6-beta", 
 		//				"rhel-x86_64-server-hpn-6-debuginfo"
 		//			]
 		//		}
 		//	]
 		client.runCommandAndWaitWithoutLogging("cat "+clienttasks.rhnDefinitionsDir+sm_rhnDefinitionsProductBaselineFile);
 		JSONArray jsonProducts = new JSONArray(client.getStdout());	
 		for (int p = 0; p < jsonProducts.length(); p++) {
 			JSONObject jsonProduct = (JSONObject) jsonProducts.get(p);
 			String productName = jsonProduct.getString("Name");
 			String productId = jsonProduct.getString("Product ID");
 			JSONArray jsonRhnChannels = jsonProduct.getJSONArray("RHN Channels");
 			
 			// process each of the RHN Channels
 			for (int r=0; r<jsonRhnChannels.length(); r++) {
 				String rhnChannel = jsonRhnChannels.getString(r);
 				
 				// store the rhnChannel in the cdnProductBaselineChannelMap
 				if (cdnProductBaselineChannelMap.containsKey(rhnChannel)) {
 					if (!cdnProductBaselineChannelMap.get(rhnChannel).contains(productId)) {
 						cdnProductBaselineChannelMap.get(rhnChannel).add(productId);
 					}
 				} else {
 					List<String> productIds = new ArrayList<String>(); productIds.add(productId);
 					cdnProductBaselineChannelMap.put(rhnChannel, productIds);
 				}
 				
 				// also store the inverse of this map into cdnProductBaselineProductIdMap
 				if (cdnProductBaselineProductIdMap.containsKey(productId)) {
 					if (!cdnProductBaselineProductIdMap.get(productId).contains(rhnChannel)) {
 						cdnProductBaselineProductIdMap.get(productId).add(rhnChannel);
 					}
 				} else {
 					List<String> rhnChannels = new ArrayList<String>(); rhnChannels.add(rhnChannel);
 					cdnProductBaselineProductIdMap.put(productId, rhnChannels);
 				}
 			}
 		}
 	}
 			
 	
 	// Protected methods ***********************************************************************
 	protected String baseProductsDir = "/usr/share/rhsm/product/RHEL";
 	protected String channelCertMappingFilename = "channel-cert-mapping.txt";
 	protected List<String> mappedProductCertFilenames = new ArrayList<String>();	// list of all the mapped product cert file names in the mapping file (e.g. Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem)
 	protected Map<String,String> channelsToProductCertFilenamesMap = new HashMap<String,String>();	// map of all the channels to product cert file names (e.g. key=rhn-tools-rhel-x86_64-server-5 value=Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem)
 	protected Map<String,List<String>> cdnProductBaselineChannelMap = new HashMap<String,List<String>>();	// map of all the channels to list of productIds (e.g. key=rhn-tools-rhel-x86_64-server-5 value=[69,169,269])
 	protected Map<String,List<String>> cdnProductBaselineProductIdMap = new HashMap<String,List<String>>();	// map of all the productIds to list of channels (e.g. key=69 value=[rhn-tools-rhel-x86_64-server-5, rhn-tools-rhel-x86_64-server-5-debug-info])	// inverse of cdnProductBaselineChannelMap
 	protected List<ProductCert> originallyInstalledRedHatProductCerts = new ArrayList<ProductCert>();
 	protected String migrationFromFact				= "migration.migrated_from";
 	protected String migrationSystemIdFact			= "migration.classic_system_id";
 	protected String migrationDateFact				= "migration.migration_date";
 	protected String originalProductCertDir			= null;
 	protected String backupProductCertDir			= "/tmp/backupOfProductCertDir";
 	protected String nonDefaultProductCertDir		= "/tmp/migratedProductCertDir";
 	protected String machineInstNumberFile			= "/etc/sysconfig/rhn/install-num";
 	protected String backupMachineInstNumberFile	= machineInstNumberFile+".bak";
 	protected String rhnBaseChannel = null;
 	protected List<String> rhnAvailableChildChannels = new ArrayList<String>();
 	static public String installNumTool = "install-num-migrate-to-rhsm";
 	static public String rhnMigrateTool = "rhn-migrate-classic-to-rhsm";
 	protected String originalServerHostname;
 	protected String originalServerPort;
 	protected String originalServerPrefix;
 	
 	protected boolean isCurrentlyConfiguredServerTypeHosted() {
 		return isHostnameHosted(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "hostname"));
 	}
 	protected boolean isHostnameHosted(String hostname) {
 		return hostname.matches("subscription\\.rhn\\.(.*\\.)*redhat\\.com");
 	}
 	
 	protected Set<String> getExpectedMappedProductCertFilenamesCorrespondingToChannels(List<String> channels) {
 		Set<String> mappedProductCertFilenamesCorrespondingToChannels = new HashSet<String>();
 		for (String channel : channels) {
 			String mappedProductCertFilename = channelsToProductCertFilenamesMap.get(channel);
 			if (mappedProductCertFilename==null) {
 				//log.warning("RHN Classic channel '"+channel+"' is NOT mapped in the file '"+channelCertMappingFilename+"'.");
 			} else {
 				log.info("The mapped product cert filename for RHN Classic channel '"+channel+"' is: "+mappedProductCertFilename);
 				if (!mappedProductCertFilename.equalsIgnoreCase("none")) {
 					mappedProductCertFilenamesCorrespondingToChannels.add(mappedProductCertFilename);
 				}
 			}
 		}
 		// SPECIAL CASE:  Red Hat Enterprise Workstation vs. Red Hat Enterprise Desktop
 		// See https://bugzilla.redhat.com/show_bug.cgi?id=786257#c1
 		
 		//	>	if customer subscribed to rhel-x86_64-client-supplementary-5:
 		//	>	   if customer subscribes to rhel-x86_64-client-workstation-5:
 		//	>	      install 71.pem
 		//	>	   else:
 		//	>	      install 68.pem
 		
 		// is product id 68 for "Red Hat Enterprise Linux Desktop" among the mappedProductCertFilenames
 		String mappedProductCertFilenameCorrespondingToBaseChannel = null;
 		String productIdForBase = "68";	// Red Hat Enterprise Desktop
 		for (String mappedProductCertFilename : mappedProductCertFilenamesCorrespondingToChannels) {
 			if (getProductIdFromProductCertFilename(mappedProductCertFilename).equals(productIdForBase)) {
 				mappedProductCertFilenameCorrespondingToBaseChannel = mappedProductCertFilename; break;
 			}
 		}
 		if (mappedProductCertFilenameCorrespondingToBaseChannel!=null) {
 			File mappedProductCertFileCorrespondingToBaseChannel = new File(baseProductsDir+"/"+mappedProductCertFilenameCorrespondingToBaseChannel);
 			for (String productId : Arrays.asList("71"/*Red Hat Enterprise Workstation*/)) {
 				for (String mappedProductCertFilename : new HashSet<String>(mappedProductCertFilenamesCorrespondingToChannels)) {
 					if (getProductIdFromProductCertFilename(mappedProductCertFilename).equals(productId)) {
 						File mappedProductCertFileCorrespondingToAddonChannel = new File(baseProductsDir+"/"+mappedProductCertFilename);
 						ProductCert productCertBase = clienttasks.getProductCertFromProductCertFile(mappedProductCertFileCorrespondingToBaseChannel);
 						ProductCert productCertAddon = clienttasks.getProductCertFromProductCertFile(mappedProductCertFileCorrespondingToAddonChannel);
 						log.warning("SPECIAL CASE ENCOUNTERED: "+rhnMigrateTool+" should NOT install product cert "+productIdForBase+" ["+productCertBase.productName+"] when product cert "+productId+" ["+productCertAddon.productName+"] is also installed.");
 						mappedProductCertFilenamesCorrespondingToChannels.remove(mappedProductCertFilenameCorrespondingToBaseChannel);
 					}
 				}
 			}
 		}
 		
 		// SPECIAL CASE:  Red Hat Beta vs. Red Hat Developer Toolset (for RHEL [Server|HPC Node|Client|Workstation])
 		// Check for special case!  email thread by dgregor entitled "Product certificates for a few channels"
 		// 180.pem is "special".  It's for the "Red Hat Beta" product, which is this generic placeholder
 		// that we created and it isn't tied to any specific Red Hat product release.
 						
 		//	> After the migration tool does it's normal migration logic, there is a hard-coded cleanup to...
 		//	>   if both 180.pem (rhel-ARCH-server-dts-5-beta) and 176.pem (rhel-ARCH-server-dts-5) were migrated
 		//	>       remove 180.pem from /etc/pki/product
 		//	>   if both 180.pem (rhel-ARCH-client-dts-5-beta) and 178.pem (rhel-ARCH-client-dts-5) were migrated
 		//	>       remove 180.pem from /etc/pki/product
 
 		// is product id 180 for "Red Hat Beta" among the mappedProductCertFilenames
 		mappedProductCertFilenameCorrespondingToBaseChannel = null;
 		productIdForBase = "180";	// Red Hat Beta
 		for (String mappedProductCertFilename : mappedProductCertFilenamesCorrespondingToChannels) {
 			if (getProductIdFromProductCertFilename(mappedProductCertFilename).equals(productIdForBase)) {
 				mappedProductCertFilenameCorrespondingToBaseChannel = mappedProductCertFilename; break;
 			}
 		}
 		if (mappedProductCertFilenameCorrespondingToBaseChannel!=null) {
 			File mappedProductCertFileCorrespondingToBaseChannel = new File(baseProductsDir+"/"+mappedProductCertFilenameCorrespondingToBaseChannel);
 			for (String productId : Arrays.asList("176"/*Red Hat Developer Toolset (for RHEL Server)*/, "177"/*Red Hat Developer Toolset (for RHEL HPC Node)*/, "178"/*Red Hat Developer Toolset (for RHEL Client)*/, "179"/*Red Hat Developer Toolset (for RHEL Workstation)*/)) {
 				for (String mappedProductCertFilename : new HashSet<String>(mappedProductCertFilenamesCorrespondingToChannels)) {
 					if (getProductIdFromProductCertFilename(mappedProductCertFilename).equals(productId)) {
 						File mappedProductCertFileCorrespondingToAddonChannel = new File(baseProductsDir+"/"+mappedProductCertFilename);
 						ProductCert productCertBase = clienttasks.getProductCertFromProductCertFile(mappedProductCertFileCorrespondingToBaseChannel);
 						ProductCert productCertAddon = clienttasks.getProductCertFromProductCertFile(mappedProductCertFileCorrespondingToAddonChannel);
 						log.warning("SPECIAL CASE ENCOUNTERED: "+rhnMigrateTool+" should NOT install product cert "+productIdForBase+" ["+productCertBase.productName+"] when product cert "+productId+" ["+productCertAddon.productName+"] is also installed.");
 						mappedProductCertFilenamesCorrespondingToChannels.remove(mappedProductCertFilenameCorrespondingToBaseChannel);
 					}
 				}
 			}
 		}
 		
 		return mappedProductCertFilenamesCorrespondingToChannels;
 	}
 	
 	protected boolean areAllChannelsMapped(List<String> channels) {
 		boolean allChannelsAreMapped = true;
 		for (String channel : channels) {
 			String mappedProductCertFilename = channelsToProductCertFilenamesMap.get(channel);
 			if (mappedProductCertFilename==null) {
 				allChannelsAreMapped = false;
 				log.warning("RHN Classic channel '"+channel+"' is NOT mapped in the file '"+channelCertMappingFilename+"'.");
 			}
 		}
 		return allChannelsAreMapped;
 	}
 	
 	/**
 	 * Use the python instnum.py program to determine what mapped product cert filenames from the channel-cert-mapping.txt correspond to this instnumber and should therefore be copied.
 	 * @param instnumber
 	 * @return
 	 * @throws JSONException
 	 */
 	protected List<String> getExpectedMappedProductCertFilenamesCorrespondingToInstnumberUsingInstnumTool(String instnumber) throws JSONException {
 		List<String> mappedProductCertFilenamesCorrespondingToInstnumber = new ArrayList<String>();
 
 		String command = "python /usr/lib/python2.4/site-packages/instnum.py "+instnumber;
 		//SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client,command,0);
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client,command+" | egrep \"^{.*}$\"", 0);
 		// [root@jsefler-onprem-5server ~]# python /usr/lib/python2.4/site-packages/instnum.py 0000000e0017fc01 | egrep "^{.*}$"
 		// {'Virt': 'VT', 'Workstation': 'Workstation', 'Base': 'Client'}
 		
 		// decide what product arch applies to our system
 		String arch = clienttasks.arch;	// default
 		if (clienttasks.redhatReleaseX.equals("5") && clienttasks.arch.equals("ppc64")) arch = "ppc";	// RHEL5 only supports ppc packages, but can be run on ppc64 hardware
 		if (Arrays.asList("i386","i486","i586","i686").contains(clienttasks.arch)) arch = "i386";		// RHEL supports i386 packages, but can be run on all 32-bit arch hardware
 		
 		// process result as a json object
 		JSONObject jsonResult = new JSONObject(result.getStdout());
 		String base = jsonResult.getString("Base");
 		
 		// Workstation (71.pem) is a special sub of the base Client (68.pem) - see bug 790217
 		// when the Workstation key is present on a base Client install, remove the base key - the effect will be to trump the base product cert with the Workstation product cert
 		if (jsonResult.has("Workstation")/* && base.equalsIgnoreCase("client") I DON'T THINK THIS IS NECESSARY*/) {
 			log.warning("This appears to be a Workstation install ("+instnumber+"). Therefore we will assume that the Workstation product cert trumps the Base.");
 			jsonResult.remove("Base");
 		}
 		
 		for (String mappedProductCertFilename : mappedProductCertFilenames) {
 			// example mappedProductCertFilenames:
 			// Server-Server-s390x-340665cdadee-72.pem  
 			// Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 			// base-sub-arch-hash-id.pem
 			Iterator<?> keys = jsonResult.keys();
 			while (keys.hasNext()) {
 				String key = (String)keys.next();
 				String sub = jsonResult.getString(key);
 				if (mappedProductCertFilename.startsWith(base+"-"+sub+"-"+arch+"-")) {
 					if (!mappedProductCertFilenamesCorrespondingToInstnumber.contains(mappedProductCertFilename)) {	// make sure the list contains unique filenames
 						mappedProductCertFilenamesCorrespondingToInstnumber.add(mappedProductCertFilename);
 					}
 				}
 			}
 		}
 		
 		return mappedProductCertFilenamesCorrespondingToInstnumber;
 	}
 	
 	
 	
 	/**
 	 * Extract the suffix pem filename from the long mapped filename.
 	 * @param productCertFilename example: Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 	 * @return example: 90.pem
 	 */
 	protected String getPemFileNameFromProductCertFilename(String productCertFilename) {
 		// Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 		return productCertFilename.split("-")[productCertFilename.split("-").length-1];
 	}
 	
 	/**
 	 * Extract the productId from the long mapped filename.
 	 * @param productCertFilename example: Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 	 * @return example: 90
 	 */
 	protected String getProductIdFromProductCertFilename(String productCertFilename) {
 		// Server-ClusterStorage-ppc-a3fea9e1dde3-90.pem
 		String pemFilename = getPemFileNameFromProductCertFilename(productCertFilename);
 		return pemFilename.replace(".pem", "");
 	}
 	
 	/**
 	 * Call rhn-migrate-classic-to-rhsm without asserting results.
 	 * @param options - command line options understood by rhn-migrate-classic-to-rhsm
 	 * @param rhnUsername - enter at the prompt for RHN Username
 	 * @param rhnPassword - enter at the prompt for RHN Password
 	 * @param regUsername - enter at the prompt for System Engine Username (will be used for subscription-manager register credentials)
 	 * @param regPassword - enter at the prompt for System Engine Password (will be used for subscription-manager register credentials)
 	 * @param regOrg - enter at the prompt for Org (will be used for subscription-manager register credentials)
 	 * @param serviceLevelIndex - index number to enter at the prompt for choosing servicelevel
 	 * @return
 	 */
 	protected SSHCommandResult executeRhnMigrateClassicToRhsm(String options, String rhnUsername, String rhnPassword, String regUsername, String regPassword, String regOrg, Integer serviceLevelIndex) {
 
 		// 8/4/2012 new behavior...
 		// the migration tool will always prompt rhn credentials to migrate the system "from"
 		// the migration tool will only prompt for destination credentials to migrate the system "to" when the configured hostname does not match subscription.rhn(.*).redhat.com
 		
 		// surround tcl args containing white space with ticks and call the TCL expect script for rhn-migrate-classic-to-rhsm
 		if (options!=null && options.contains(" "))			options		= String.format("'%s'", options);
 		if (options!=null && options.isEmpty())				options		= String.format("\"%s\"", options);
 		if (rhnUsername!=null && rhnUsername.contains(" "))	rhnUsername	= String.format("\"%s\"", rhnUsername);
 		if (rhnUsername!=null && rhnUsername.isEmpty())		rhnUsername	= String.format("\"%s\"", rhnUsername);
 		if (rhnPassword!=null && rhnPassword.contains(" "))	rhnPassword	= String.format("\"%s\"", rhnPassword);
 		if (rhnPassword!=null && rhnPassword.isEmpty())		rhnPassword	= String.format("\"%s\"", rhnPassword);
 		if (regUsername!=null && regUsername.contains(" "))	regUsername	= String.format("\"%s\"", regUsername);
 		if (regUsername!=null && regUsername.isEmpty())		regUsername	= String.format("\"%s\"", regUsername);
 		if (regPassword!=null && regPassword.contains(" "))	regPassword	= String.format("\"%s\"", regPassword);
 		if (regPassword!=null && regPassword.isEmpty())		regPassword	= String.format("\"%s\"", regPassword);
 		if (regOrg!=null && regOrg.contains(" ")) 			regOrg		= String.format("\"%s\"", regOrg);
 		if (regOrg!=null && regOrg.isEmpty())				regOrg		= String.format("\"%s\"", regOrg);
 		String command = String.format("rhn-migrate-classic-to-rhsm.tcl %s %s %s %s %s %s %s", options, rhnUsername, rhnPassword, regUsername, regPassword, regOrg, serviceLevelIndex);
 		return client.runCommandAndWait(command);
 	}
 
 	
 	/**
 	 * Call rhn-channel --list to get the currently consumed RHN channels.
 	 * @return
 	 */
 	protected List<String> getCurrentRhnClassicChannels() {
 		
 		// [root@jsefler-onprem-5server rhn]# rhn-channel --list
 		// rhel-x86_64-server-5
 		// rhel-x86_64-server-supplementary-5
 		// rhel-x86_64-server-supplementary-5-debuginfo
 		String command = String.format("rhn-channel --list");
 		SSHCommandResult result = client.runCommandAndWait(command);
 		
 		// assert result
 		Assert.assertEquals(result.getExitCode(), new Integer(0),"Exitcode from attempt to list currently consumed RHN Classic channels.");
 		Assert.assertEquals(result.getStderr(), "","Stderr from attempt to list currently consumed RHN Classic channels.");
 		
 		List<String> rhnChannels = new ArrayList<String>();
 		if (!result.getStdout().trim().equals("")) {
 			rhnChannels	= Arrays.asList(result.getStdout().trim().split("\\n"));
 		}
 		return rhnChannels;
 	}
 	
 	/**
 	 * Call rhn-channel --user=$rhnUsername --password=$rhnPassword --add --channel=$rhnChannel to consume RHN channels.
 	 * @param rhnUsername
 	 * @param rhnPassword
 	 * @param rhnChannels
 	 */
 	protected void addRhnClassicChannels(String rhnUsername, String rhnPassword, List<String> rhnChannels) {
 		if (false) {	// THIS APPROACH FAILS WHEN THERE IS AN OFFENDING CHANNEL OR THERE ARE TONS OF CHANNELS; I THINK THERE MIGHT BE AN INPUT LIMIT FOR PYTHON ARGS
 			// [root@jsefler-onprem-5server rhn]# rhn-channel --add --user=qa@redhat.com --password=CHANGE-ME --channel=rhel-x86_64-server-sap-5
 			String rhnChannelsAsOptions=""; for (String rhnChannel : rhnChannels) rhnChannelsAsOptions+=String.format("--channel=%s ",rhnChannel);
 			String command = String.format("rhn-channel --user=%s --password=%s --add %s",rhnUsername,rhnPassword,rhnChannelsAsOptions);
 			SSHCommandResult result = client.runCommandAndWait(command);
 			Assert.assertEquals(result.getExitCode(), new Integer(0),"Exitcode from attempt to add RHN Classic channel.");
 			Assert.assertEquals(result.getStderr(), "","Stderr from attempt to add RHN Classic channel.");
 		} else if (false) {	// THIS APPROACH DOES NOT HIDE AN OFFENDING CHANNEL, BUT IT IS SLOWER AND CONSUMES LOTS OF LOG SPACE
 			for (String rhnChannel : rhnChannels) {
 				String command = String.format("rhn-channel --user=%s --password=%s --add --channel=%s",rhnUsername,rhnPassword,rhnChannel);
 				SSHCommandResult result = client.runCommandAndWait(command);
 				Assert.assertEquals(result.getExitCode(), new Integer(0),"Exitcode from attempt to add RHN Classic channel.");
 				Assert.assertEquals(result.getStderr(), "","Stderr from attempt to add RHN Classic channel.");
 			}
 		} else {	// THIS APPROACH WORKS WELL, BUT IT HIDES OFFENDING CHANNELS
 			String command="";
 			for (String rhnChannel : rhnChannels) {
 				//command += String.format(" && rhn-channel --user=%s --password=%s --add --channel=%s",rhnUsername,rhnPassword,rhnChannel);
 				command += String.format(" && rhn-channel -u %s -p %s -a -c %s",rhnUsername,rhnPassword,rhnChannel);
 			}
 			command = command.replaceFirst("^ *&& *", "");
 			SSHCommandResult result = client.runCommandAndWait(command);
 			Assert.assertEquals(result.getExitCode(), new Integer(0),"Exitcode from attempt to add RHN Classic channels.");
 			Assert.assertEquals(result.getStderr(), "","Stderr from attempt to add RHN Classic channel.");
 		}
 	}
 
 	protected void iptablesRejectPort(String port) {
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	[root@jsefler-r63-server rhn]# iptables -A OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	REJECT     tcp  --  anywhere             anywhere            tcp dpt:https reject-with icmp-port-unreachable 
 		RemoteFileTasks.runCommandAndAssert(client, String.format("iptables -A OUTPUT -p tcp --dport %s -j REJECT",port), 0);
 	}
 	
 	protected void iptablesAcceptPort(String port) {
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	[root@jsefler-r63-server rhn]# iptables -A OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# iptables -A OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	REJECT     tcp  --  anywhere             anywhere            tcp dpt:https reject-with icmp-port-unreachable 
 		//	REJECT     tcp  --  anywhere             anywhere            tcp dpt:https reject-with icmp-port-unreachable 
 		//	[root@jsefler-r63-server rhn]# iptables -D OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# echo $?
 		//	0
 		//	[root@jsefler-r63-server rhn]# iptables -D OUTPUT -p tcp --dport 443 -j REJECT
 		//	[root@jsefler-r63-server rhn]# echo $?
 		//	0
 		//	[root@jsefler-r63-server rhn]# iptables -D OUTPUT -p tcp --dport 443 -j REJECT
 		//	iptables: No chain/target/match by that name.
 		//	[root@jsefler-r63-server rhn]# echo $?
 		//	1
 		//	[root@jsefler-r63-server rhn]# iptables -L OUTPUT
 		//	Chain OUTPUT (policy ACCEPT)
 		//	target     prot opt source               destination         
 		//	[root@jsefler-r63-server rhn]# 
 		do {
 			client.runCommandAndWait(String.format("iptables -D OUTPUT -p tcp --dport %s -j REJECT",port));
 		} while (client.getExitCode()==0);
 	}
 
 	// Data Providers ***********************************************************************
 
 	@DataProvider(name="InstallNumMigrateToRhsmData")
 	public Object[][] getInstallNumMigrateToRhsmDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInstallNumMigrateToRhsmDataAsListOfLists());
 	}
 	public List<List<Object>> getInstallNumMigrateToRhsmDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		// if (!clienttasks.redhatReleaseX.equals("5")) return ll;	// prone to improperly unexecuted tests
 		if (clienttasks.redhatReleaseX.equals("6")) return ll;
 		if (clienttasks.redhatReleaseX.equals("7")) return ll;
 		
 		// REFRENCE DATA FROM: http://linuxczar.net/articles/rhel-installation-numbers
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("790217"),	"0000000e0017fc01"}));	// Client
 		ll.add(Arrays.asList(new Object[]{null,							"000000990007fc02"}));	// Red Hat Global Desktop
 		ll.add(Arrays.asList(new Object[]{null,							"000000e90007fc00"}));	// Server
 		ll.add(Arrays.asList(new Object[]{null,							"00000065000bfc00"}));	// Server with Cluster
 		ll.add(Arrays.asList(new Object[]{null,							"000000ab000ffc00"}));	// Server with ClusterStorage
 		ll.add(Arrays.asList(new Object[]{null,							"000000e30013fc00"}));	// Server with HPC
 		ll.add(Arrays.asList(new Object[]{null,							"000000890017fc00"}));	// Server with Directory
 		ll.add(Arrays.asList(new Object[]{null,							"00000052001bfc00"}));	// Server with SMB
 
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("790217"),	"000000a4004ffc01"}));	// Product: RHEL Client   Options: Basic FullProd Workstation  {'Workstation': 'Workstation', 'Base': 'Client'}
 		ll.add(Arrays.asList(new Object[]{null,							"000000870003fc01"}));	// Product: RHEL Client   Options: NoSLA FullProd  {'Base': 'Client'}
 		return ll;
 	}
 	
 	
 	@DataProvider(name="InstallNumMigrateToRhsmWithInvalidInstNumberData")
 	public Object[][] getInstallNumMigrateToRhsmWithInvalidInstNumberDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInstallNumMigrateToRhsmWithInvalidInstNumberDataAsListOfLists());
 	}
 	protected List<List<Object>> getInstallNumMigrateToRhsmWithInvalidInstNumberDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		// if (!clienttasks.redhatReleaseX.equals("5")) return ll;	// prone to improperly unexecuted tests
 		if (clienttasks.redhatReleaseX.equals("6")) return ll;
 		if (clienttasks.redhatReleaseX.equals("7")) return ll;
 		
 		// due to design changes, this is a decent place to dump old commands that have been removed
 		
 		// String command, int expectedExitCode, String expectedStdout, String expectedStderr
 		ll.add(Arrays.asList(new Object[]{null, installNumTool+" -d -i 123456789012345",			1,	"Could not parse the installation number: Unsupported string length", ""}));
 		ll.add(Arrays.asList(new Object[]{null, installNumTool+" -d -i=12345678901234567",			1,	"Could not parse the installation number: Unsupported string length", ""}));
 		ll.add(Arrays.asList(new Object[]{null, installNumTool+"    --instnum 123456789X123456",	1,	"Could not parse the installation number: Not a valid hex string", ""}));
 		ll.add(Arrays.asList(new Object[]{null, installNumTool+"    --instnum=1234567890123456",	1,	"Could not parse the installation number: Checksum verification failed", ""}));
 		
 		return ll;
 	}
 	
 	
 	@DataProvider(name="RhnMigrateClassicToRhsmData")
 	public Object[][] getRhnMigrateClassicToRhsmDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnMigrateClassicToRhsmDataAsListOfLists());
 	}
 	public List<List<Object>> getRhnMigrateClassicToRhsmDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		int rhnChildChannelSubSize = 40;	// 50;	// used to break down rhnAvailableChildChannels into smaller sub-lists to avoid bug 818786 - 502 Proxy Error traceback during large rhn-migrate-classic-to-rhsm
 		
 		// when we are migrating away from RHN Classic to a non-hosted candlepin server, choose the credentials that will be used to register
 		String regUsername=null, regPassword=null, regOrg=null;
 		if (!isCurrentlyConfiguredServerTypeHosted()) {	// or this may work too: if (!sm_serverType.equals(CandlepinType.hosted)) {
 			regUsername = sm_clientUsername;
 			regPassword = sm_clientPassword;
 			regOrg = sm_clientOrg;
 		}
 		
 		// predict the valid service levels that will be available to the migrated consumer
 		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(regUsername==null?sm_rhnUsername:regUsername, regPassword==null?sm_rhnPassword:regPassword, regOrg, null, null, null, null, null, null, null, (String)null, null, null, true, null, null, null, null));
 		String orgKey = CandlepinTasks.getOwnerKeyOfConsumerId(regUsername==null?sm_rhnUsername:regUsername, regPassword==null?sm_rhnPassword:regPassword, sm_serverUrl, consumerId);
 		List<String> regServiceLevels = CandlepinTasks.getServiceLevelsForOrgKey(regUsername==null?sm_rhnUsername:regUsername, regPassword==null?sm_rhnPassword:regPassword, sm_serverUrl, orgKey);	
 		clienttasks.unregister(null, null, null);
 		
 		// predict the expected service level from the defaultServiceLevel on the Org
 		JSONObject jsonOrg = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(regUsername==null?sm_rhnUsername:regUsername, regPassword==null?sm_rhnPassword:regPassword, sm_serverUrl, "/owners/"+orgKey));
 		String defaultServiceLevel = (jsonOrg.get("defaultServiceLevel").equals(JSONObject.NULL))? "":jsonOrg.getString("defaultServiceLevel");
 		
 		// create some variations on a valid serverUrl to test the --serverurl option
 		List<String> regServerUrls = new ArrayList<String>();
 		if (isHostnameHosted(originalServerHostname)) {
 			regServerUrls.add(originalServerHostname);
 			regServerUrls.add("https://"+originalServerHostname);
 			regServerUrls.add("https://"+originalServerHostname+originalServerPrefix);
 			regServerUrls.add("https://"+originalServerHostname+":"+originalServerPort);
 			regServerUrls.add("https://"+originalServerHostname+":"+originalServerPort+originalServerPrefix);
 		} else {	// Note: only a fully qualified server url will work for a non-hosted hostname because otherwise the (missing port/prefix defaults to 443/subscription) results will end up with: Unable to connect to certificate server: (111, 'Connection refused'). See /var/log/rhsm/rhsm.log for more details.
 			regServerUrls.add(originalServerHostname+":"+originalServerPort+originalServerPrefix);
 			regServerUrls.add("https://"+originalServerHostname+":"+originalServerPort+originalServerPrefix);
 		}
 		
 		// Object bugzilla, String rhnUsername, String rhnPassword, String rhnServer, List<String> rhnChannelsToAdd, String options, String regUsername, String regPassword, String regOrg, Integer serviceLevelIndex, String serviceLevelExpected
 
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("849644"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"-n",		regUsername,	regPassword,	regOrg,	null,	defaultServiceLevel}));
 		//ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("849644"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,		"-n",		regUsername,	regPassword,	regOrg,	null,	defaultServiceLevel}));
 		for (int i=0; i<rhnAvailableChildChannels.size(); i+=rhnChildChannelSubSize) {	// split rhnAvailableChildChannels into sub-lists of 50 channels to avoid bug 818786 - 502 Proxy Error traceback during large rhn-migrate-classic-to-rhsm
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("849644"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels.subList(i,i+rhnChildChannelSubSize>rhnAvailableChildChannels.size()?rhnAvailableChildChannels.size():i+rhnChildChannelSubSize),	"-n -f",	regUsername,	regPassword,	regOrg,	null,	defaultServiceLevel}));		
 		}
 
 		ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"",			regUsername,	regPassword,	regOrg,	null,	defaultServiceLevel}));
 		//ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,		"",			regUsername,	regPassword,	regOrg,	/*areAllChannelsMapped(rhnAvailableChildChannels)?noServiceLevelIndex:*/null,	defaultServiceLevel}));
 		for (int i=0; i<rhnAvailableChildChannels.size(); i+=rhnChildChannelSubSize) {	// split rhnAvailableChildChannels into sub-lists of 50 channels to avoid bug 818786 - 502 Proxy Error traceback during large rhn-migrate-classic-to-rhsm
 			ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels.subList(i,i+rhnChildChannelSubSize>rhnAvailableChildChannels.size()?rhnAvailableChildChannels.size():i+rhnChildChannelSubSize),	"-f",	regUsername,	regPassword,	regOrg,	null,	defaultServiceLevel}));		
 		}
 		
 		// test variations of a valid serverUrl
 		for (String serverUrl : regServerUrls) {
 			List<String> availableChildChannelList = rhnAvailableChildChannels.isEmpty()? rhnAvailableChildChannels : Arrays.asList(rhnAvailableChildChannels.get(randomGenerator.nextInt(rhnAvailableChildChannels.size())));	// randomly choose an available child channel just to add a little fun
 			ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	availableChildChannelList,	"-f --serverurl="+serverUrl,		sm_clientUsername,	sm_clientPassword,	sm_clientOrg,	null,	defaultServiceLevel}));		
 		}
 
 		// test each servicelevel
 		for (String serviceLevel : regServiceLevels) {
 			String options;
 			options = String.format("--force --servicelevel=%s",serviceLevel); if (serviceLevel.contains(" ")) options = String.format("--force --servicelevel \"%s\"", serviceLevel);
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("840169"),							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	getRandomSubsetOfList(rhnAvailableChildChannels,rhnChildChannelSubSize),	options,	regUsername,	regPassword,	regOrg,	null,	serviceLevel}));	
 			options = String.format("-f -s %s",randomizeCaseOfCharactersInString(serviceLevel)); if (serviceLevel.contains(" ")) options = String.format("-f -s \"%s\"", randomizeCaseOfCharactersInString(serviceLevel));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"840169","841961"}),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	getRandomSubsetOfList(rhnAvailableChildChannels,rhnChildChannelSubSize),	options,	regUsername,	regPassword,	regOrg,	null,	serviceLevel}));
 		}
 		
 		// attempt an unavailable servicelevel, then choose an available one from the index table
 		if (!regServiceLevels.isEmpty()) {
 			int serviceLevelIndex = randomGenerator.nextInt(regServiceLevels.size());
 			String serviceLevel = regServiceLevels.get(serviceLevelIndex);
 			serviceLevelIndex++;	// since the interactive menu of available service-levels to choose from is indexed starting at 1.
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"840169"}),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),	"--force --servicelevel=UNAVAILABLE-SLA",				regUsername,	regPassword,	regOrg,	serviceLevelIndex,	serviceLevel}));	
 		}
 		
 		// attempt an unavailable servicelevel, then choose no service level
 		if (!regServiceLevels.isEmpty()) {
 			int noServiceLevelIndex = regServiceLevels.size()+1;	// since the last item in the interactive menu of available service-levels is "#. No service level preference"
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"840169"}),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),	"--force --servicelevel=UNAVAILABLE-SLA",				regUsername,	regPassword,	regOrg,	noServiceLevelIndex,	""}));	
 		}
 		
 		
 		// when regOrg is not null, add bug BlockedByBzBug 849483 to all rows
 		if (regOrg!=null) for (List<Object> l : ll) {
 			BlockedByBzBug blockedByBzBug = (BlockedByBzBug) l.get(0);	// get the existing BlockedByBzBug
 			List<String> bugIds = blockedByBzBug==null?new ArrayList<String>():new ArrayList<String>(Arrays.asList(blockedByBzBug.getBugIds()));
 			bugIds.add("849483");	// 849483 - rhn-migrate-classic-to-rhsm fails to prompt for needed System Engine org credentials 
 			blockedByBzBug = new BlockedByBzBug(bugIds.toArray(new String[]{}));
 			l.set(0, blockedByBzBug);
 		}
 		
 		return ll;
 	}
 	
 	
 	@DataProvider(name="RhnMigrateClassicToRhsmUsingProxyServerData")
 	public Object[][] getRhnMigrateClassicToRhsmUsingProxyServerDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnMigrateClassicToRhsmUsingProxyServerDataAsListOfLists());
 	}
 	protected List<List<Object>> getRhnMigrateClassicToRhsmUsingProxyServerDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		int rhnChildChannelSubSize = 40;	// 50;	// used to break down rhnAvailableChildChannels into smaller sub-lists to avoid bug 818786 - 502 Proxy Error traceback during large rhn-migrate-classic-to-rhsm
 
 		String basicauthproxyUrl = String.format("%s:%s", sm_basicauthproxyHostname,sm_basicauthproxyPort); basicauthproxyUrl = basicauthproxyUrl.replaceAll(":$", "");
 		String noauthproxyUrl = String.format("%s:%s", sm_noauthproxyHostname,sm_noauthproxyPort); noauthproxyUrl = noauthproxyUrl.replaceAll(":$", "");
 		
 		// when we are migrating away from RHN Classic to a non-hosted candlepin server, choose the credentials that will be used to register
 		String regUsername=null, regPassword=null, regOrg=null;
 		if (!isCurrentlyConfiguredServerTypeHosted()) {	// or this may work too: if (!sm_serverType.equals(CandlepinType.hosted)) {
 			regUsername = sm_clientUsername;
 			regPassword = sm_clientPassword;
 			regOrg = sm_clientOrg;
 		}
 		
 		// Object bugzilla, String rhnUsername, String rhnPassword, String rhnServer, List<String> rhnChannelsToAdd, String options, String regUsername, String regPassword, String regOrg, String proxy_hostnameConfig, String proxy_portConfig, String proxy_userConfig, String proxy_passwordConfig, Integer exitCode, String stdout, String stderr, SSHCommandRunner proxyRunner, String proxyLog, String proxyLogRegex
 
 		// basic auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"--no-auto",			regUsername,	regPassword,	regOrg,	sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicAuthProxyRunner,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("798015"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"--no-auto",			regUsername,	regPassword,	regOrg,	"http://"+sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicAuthProxyRunner,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		//ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("818786"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,		"--no-auto --force",	regUsername,	regPassword,	regOrg,	sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicAuthProxyRunner,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		for (int i=0; i<rhnAvailableChildChannels.size(); i+=rhnChildChannelSubSize) {	// split rhnAvailableChildChannels into sub-lists of 50 channels to avoid bug 818786 - 502 Proxy Error traceback during large rhn-migrate-classic-to-rhsm
 			ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	getRandomSubsetOfList(rhnAvailableChildChannels,rhnChildChannelSubSize),	"--no-auto --force",	regUsername,	regPassword,	regOrg,	sm_basicauthproxyHostname,	sm_basicauthproxyPort,		sm_basicauthproxyUsername,	sm_basicauthproxyPassword,	Integer.valueOf(0),		null,		null,		basicAuthProxyRunner,	sm_basicauthproxyLog,	"TCP_MISS"}));
 		}
 		
 		// no auth proxy test data...
 		ll.add(Arrays.asList(new Object[]{null,							sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"--no-auto",			regUsername,	regPassword,	regOrg,	sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noAuthProxyRunner,	sm_noauthproxyLog,		"Connect"}));
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("798015"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	new ArrayList<String>(),		"--no-auto",			regUsername,	regPassword,	regOrg,	"http://"+sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noAuthProxyRunner,	sm_noauthproxyLog,		"Connect"}));
 		//ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("818786"),	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	rhnAvailableChildChannels,		"--no-auto --force",	regUsername,	regPassword,	regOrg,	sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noAuthProxyRunner,	sm_noauthproxyLog,		"Connect"}));
 		for (int i=0; i<rhnAvailableChildChannels.size(); i+=rhnChildChannelSubSize) {	// split rhnAvailableChildChannels into sub-lists of 50 channels to avoid bug 818786 - 502 Proxy Error traceback during large rhn-migrate-classic-to-rhsm
 			ll.add(Arrays.asList(new Object[]{null /* AVOIDS BUG 818786 */,	sm_rhnUsername,	sm_rhnPassword,	sm_rhnHostname,	getRandomSubsetOfList(rhnAvailableChildChannels,rhnChildChannelSubSize),	"--no-auto --force",	regUsername,	regPassword,	regOrg,	sm_noauthproxyHostname,	sm_noauthproxyPort,		"",							"",						Integer.valueOf(0),		null,		null,		noAuthProxyRunner,	sm_noauthproxyLog,		"Connect"}));
 		}
 		
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="RhnMigrateClassicToRhsmWithNonDefaultProductCertDirData")
 	public Object[][] getRhnMigrateClassicToRhsmWithNonDefaultProductCertDirDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnMigrateClassicToRhsmWithNonDefaultProductCertDirDataAsListOfLists());
 	}
 	public List<List<Object>> getRhnMigrateClassicToRhsmWithNonDefaultProductCertDirDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = getRhnMigrateClassicToRhsmDataAsListOfLists();
 		
 		// simply return a few random rows from getRhnMigrateClassicToRhsmDataAsListOfLists
 		while (ll.size()>2) ll.remove(randomGenerator.nextInt(ll.size())); 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="RhnChannelFromProductBaselineData")
 	public Object[][] getRhnChannelFromProductBaselineDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnChannelFromProductBaselineDataAsListOfLists());
 	}
 	public List<List<Object>> getRhnChannelFromProductBaselineDataAsListOfLists() throws JSONException {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 				
 		for (String productId : cdnProductBaselineProductIdMap.keySet()) {
 			for (String rhnChannel : cdnProductBaselineProductIdMap.get(productId)) {
 
 				// filter out all RHN Channels not associated with this release  (e.g., assume that an rhn channel containing "-5-" or ends in "-5" is only applicable to rhel5 
 				if (!(rhnChannel.contains("-"+clienttasks.redhatReleaseX+"-") || rhnChannel.endsWith("-"+clienttasks.redhatReleaseX))) continue;
 				
 				// skip on these RHN Channels that slip through this ^ filter
 				// [root@jsefler-onprem-5server tmp]# grep jboss /tmp/product-baseline.json | grep -v Label
 	            // "rhel-x86_64-server-6-rhevm-3-jboss-5", 
 	            // "rhel-x86_64-server-6-rhevm-3-jboss-5-beta", 
 	            // "rhel-x86_64-server-6-rhevm-3-jboss-5-debuginfo", 
 	            // "rhel-x86_64-server-6-rhevm-3-jboss-5-beta-debuginfo"
 				List<String> rhnChannelExceptions = Arrays.asList("rhel-x86_64-server-6-rhevm-3-jboss-5","rhel-x86_64-server-6-rhevm-3-jboss-5-beta","rhel-x86_64-server-6-rhevm-3-jboss-5-debuginfo","rhel-x86_64-server-6-rhevm-3-jboss-5-beta-debuginfo");
 				if (rhnChannelExceptions.contains(rhnChannel) && !clienttasks.redhatReleaseX.equals(/*"5"*/"6")) continue;
 				
 				// bugzillas
 				Set<String> bugIds = new HashSet<String>();
 				if (rhnChannel.contains("-rhev-agent-") && clienttasks.redhatReleaseX.equals("5")/* && channelsToProductCertFilenamesMap.get(rhnChannel).equalsIgnoreCase("none")*/) { 
 					// Bug 786278 - RHN Channels for -rhev- and -vt- in the channel-cert-mapping.txt are not mapped to a productId
 					bugIds.add("786278");
 				}
 				if (rhnChannel.contains("-vt-")/* && channelsToProductCertFilenamesMap.get(rhnChannel).equalsIgnoreCase("none")*/) { 
 					// Bug 786278 - RHN Channels for -rhev- and -vt- in the channel-cert-mapping.txt are not mapped to a productId
 					bugIds.add("786278");
 				}
 				if (rhnChannel.startsWith("rhel-i386-rhev-agent-") /* && channelsToProductCertFilenamesMap.get(rhnChannel).equalsIgnoreCase("none")*/) { 
 					// Bug 816364 - channel-cert-mapping.txt is missing a mapping for product 150 "Red Hat Enterprise Virtualization" on i386
 					bugIds.add("816364");
 				}
 				if (rhnChannel.endsWith("-beta") && clienttasks.redhatReleaseX.equals("5")/* && channelsToProductCertFilenamesMap.get(rhnChannel).equalsIgnoreCase("none")*/) { 
 					// Bug 786203 - all RHN *beta Channels in channel-cert-mapping.txt are mapped to "none" instead of valid productId
 					bugIds.add("786203");
 				}			
 				if (rhnChannel.endsWith("-debuginfo") && clienttasks.redhatReleaseX.equals("5")) { 
 					// Bug 786140 - RHN Channels for "*debuginfo" are missing from the channel-cert-mapping.txt 
 					bugIds.add("786140");
 				}
 				if (rhnChannel.startsWith("rhel-x86_64-server-optional-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-sfs-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-ha-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-rs-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-rhevh") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-rhevm-3") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-rhevm-3-jboss-5") ||
 					rhnChannel.startsWith("rhel-x86_64-server-sjis-6") ||
 					rhnChannel.startsWith("rhel-x86_64-server-sap-6") ||
 					rhnChannel.startsWith("rhel-x86_64-server-lb-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-workstation-sfs-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-workstation-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-workstation-optional-6-htb") ||
 					rhnChannel.startsWith("rhel-x86_64-rhev-mgmt-agent-6") ||
 					rhnChannel.startsWith("rhel-i386-server-6-cf-tools-1") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-cf-tools-1") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-cf-ae-1") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-cf-ce-1") ||
 					rhnChannel.startsWith("rhel-x86_64-server-6-cf-se-1") ||
 					rhnChannel.startsWith("sam-rhel-x86_64-server-6-htb") || rhnChannel.startsWith("sam-rhel-x86_64-server-6-beta")) { 
 					// Bug 799152 - subscription-manager-migration-data is missing some product certs for RHN Channels in product-baseline.json
 					bugIds.add("799152");
 				}
 				if (rhnChannel.equals("rhel-s390x-server-6") ||
 					rhnChannel.equals("rhel-s390x-server-optional-6") ||
 					rhnChannel.equals("rhel-s390x-server-supplementary-6")) { 
 					// Bug 799103 - no mapping for s390x product cert included in the subscription-manager-migration-data
 					bugIds.add("799103");
 				}
 				if (rhnChannel.equals("sam-rhel-x86_64-server-6") ||
 					rhnChannel.equals("sam-rhel-x86_64-server-6-debuginfo")) { 
 					// Bug 815433 - sam-rhel-x86_64-server-6-beta channel mapping needs replacement in channel-cert-mapping.txt 
 					bugIds.add("815433");
 				}
 				if (productId.equals("167")) {
 					// Bug 811633 - channel-cert-mapping.txt is missing a mapping for product 167 "Red Hat CloudForms"
 					bugIds.add("811633");
 				}
 				if (productId.equals("183") || productId.equals("184") || productId.equals("185")) if (clienttasks.redhatReleaseX.equals("6")) {
 					// Bug 825603 - channel-cert-mapping.txt is missing a mapping for JBoss product ids 183,184,185
 					bugIds.add("825603");
 				}
 				if (rhnChannel.contains("-dts-")) if (clienttasks.redhatReleaseX.equals("6")) { 
 					// Bug 820749 - channel-cert-mapping.txt is missing a mapping for product "Red Hat Developer Toolset"
 					bugIds.add("820749");
 				}
 				if (rhnChannel.contains("-dts-")) if (clienttasks.redhatReleaseX.equals("5")) { 
 					// Bug 852551 - channel-cert-mapping.txt is missing a mapping for product "Red Hat Developer Toolset"
 					bugIds.add("852551");
 				}
 				if (productId.equals("195")) {
 					// Bug 869008 - mapping for productId 195 "Red Hat Developer Toolset (for RHEL for IBM POWER)" is missing
 					bugIds.add("869008");
 				}
 				if (productId.equals("181")) {
 					// Bug 840148 - missing product cert corresponding to "Red Hat EUCJP Support (for RHEL Server)"
 					bugIds.add("840148");
 					// Bug 847069 - Add certificates for rhel-x86_64-server-eucjp-5* channels.
 					bugIds.add("847069");
 				}
 				if (rhnChannel.startsWith("rhel-i386-rhev-agent-5-")) { 
 					// Bug 849305 - rhel-i386-rhev-agent-5-* maps in channel-cert-mapping.txt do not match CDN Product Baseline
 					bugIds.add("849305");
 				}
 				if (rhnChannel.startsWith("jbappplatform-4.2-els-")) { 
 					// Bug 861470 - JBoss Enterprise Application Platform - ELS (jbappplatform-4.2.0) 192.pem product certs are missing from subscription-manager-migration-data
 					bugIds.add("861470");
 				}
 				if (rhnChannel.startsWith("rhel-x86_64-rhev-mgmt-agent-5")) { 
 					// Bug 861420 - Red Hat Enterprise Virtualization (rhev-3.0) 150.pem product certs are missing from subscription-manager-migration-data
 					bugIds.add("861420");
 				}
 				if (rhnChannel.equals("rhel-x86_64-rhev-mgmt-agent-5-debuginfo") || rhnChannel.equals("rhel-x86_64-rhev-mgmt-agent-5-beta-debuginfo")) { 
 					// Bug 865566 - RHEL-5/channel-cert-mapping.txt is missing a mapping for two rhev debuginfo channels
 					bugIds.add("865566");
 				}
 				if (productId.equals("167") || productId.equals("155") || productId.equals("186") || productId.equals("191") || productId.equals("188") || productId.equals("172")) if (clienttasks.redhatReleaseX.equals("6")) {
 					// Bug 872959 - many product certs and their RHN Channel mappings are missing from the RHEL64 subscription-manager-migration-data
 					bugIds.add("872959");
 				}
 				
 				// Object bugzilla, String productBaselineRhnChannel, String productBaselineProductId
 				BlockedByBzBug blockedByBzBug = new BlockedByBzBug(bugIds.toArray(new String[]{}));
 				ll.add(Arrays.asList(new Object[]{blockedByBzBug,	rhnChannel,	productId}));
 			}
 		}
 		
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getRhnClassicBaseAndAvailableChildChannelsData")
 	public Object[][] getRhnClassicBaseAndAvailableChildChannelsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRhnClassicBaseAndAvailableChildChannelsDataAsListOfLists());
 	}
 	protected List<List<Object>> getRhnClassicBaseAndAvailableChildChannelsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (clienttasks==null) return ll;
 		
 		// add the base channel
 		if (rhnBaseChannel!=null) ll.add(Arrays.asList(new Object[]{null,	rhnBaseChannel}));
 		
 		// add the child channels
 		for (String rhnAvailableChildChannel : rhnAvailableChildChannels) {
 			
 			// bugzillas
 			Set<String> bugIds = new HashSet<String>();
 			if (rhnAvailableChildChannel.matches("sam-rhel-.+-server-6-beta.*")) {	// sam-rhel-x86_64-server-6-beta-debuginfo
 				// Bug 819092 - channels for sam-rhel-<ARCH>-server-6-beta-* are not yet mapped to product certs in rcm/rhn-definitions.git
 				bugIds.add("819092");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-rhui-2(-.*|$)")) {	// rhel-x86_64-server-6-rhui-2-debuginfo
 				// Bug 819089 - channels for rhel-<ARCH>-rhui-2-* are not yet mapped to product certs in rcm/rhn-definitions.git
 				bugIds.add("819089");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-6-mrg-.+")) {	// rhel-x86_64-server-6-mrg-grid-execute-2-debuginfo rhel-x86_64-server-6-mrg-messaging-2-debuginfo
 				// Bug 819088 - channels for rhel-<ARCH>-server-6-mrg-* are not yet mapped to product certs in rcm/rhn-definitions.git 
 				bugIds.add("819088");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-hpc-node-6-mrg-.*")) {	// rhel-x86_64-hpc-node-6-mrg-grid-execute-2  rhel-x86_64-hpc-node-6-mrg-grid-execute-2-debuginfo  rhel-x86_64-hpc-node-6-mrg-management-2  rhel-x86_64-hpc-node-6-mrg-management-2-debuginfo
 				// Bug 825608 - channels for rhel-<ARCH>-hpc-node-6-mrg-* are not yet mapped to product certs in rcm/rhn-definitions.git
 				bugIds.add("825608");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-v2vwin-6(-.*|$)")) {	// rhel-x86_64-server-v2vwin-6-beta-debuginfo
 				// Bug 817791 - v2vwin content does not exist in CDN
 				bugIds.add("817791");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-fastrack-6(-.*|$)")) {	// rhel-x86_64-server-ha-fastrack-6-debuginfo
 				// Bug 818202 - Using subscription-manager, some repositories like fastrack are not available as they are in rhn.
 				bugIds.add("818202");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-eucjp-6(-.+|$)")) {	// rhel-x86_64-server-eucjp-6 rhel-x86_64-server-eucjp-6-beta etc.
 				// Bug 840148 - missing product cert corresponding to "Red Hat EUCJP Support (for RHEL Server)"
 				bugIds.add("840148");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-fastrack-5(-.*|$)")) {	// rhel-x86_64-server-fastrack-5 rhel-x86_64-server-fastrack-5-debuginfo
 				// Bug 818202 - Using subscription-manager, some repositories like fastrack are not available as they are in rhn.
 				bugIds.add("818202");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-5-cf-tools-1(-beta)?-debuginfo")) {	// rhel-x86_64-server-5-cf-tools-1-beta-debuginfo, rhel-x86_64-server-5-cf-tools-1-debuginfo
 				// Bug 840099 - debug info channels for rhel-x86_64-server-5-cf-tools are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugIds.add("840099");	// CLOSED as a dup of bug 818202
 				bugIds.add("818202");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-5-mrg-.*")) {	// rhel-x86_64-server-5-mrg-grid-1 rhel-x86_64-server-5-mrg-grid-1-beta rhel-x86_64-server-5-mrg-grid-2 rhel-x86_64-server-5-mrg-grid-execute-1 rhel-x86_64-server-5-mrg-grid-execute-1-beta rhel-x86_64-server-5-mrg-grid-execute-2 etc.
 				// Bug 840102 - channels for rhel-<ARCH>-server-5-mrg-* are not yet mapped to product certs in rcm/rcm-metadata.git 
 				bugIds.add("840102");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-hpc-5(-.*|$)")) {	// rhel-x86_64-server-hpc-5-beta
 				// Bug 840103 - channel for rhel-x86_64-server-hpc-5-beta is not yet mapped to product cert in rcm/rcm-metadata.git
 				bugIds.add("840103");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-rhev-hdk-2-5(-.+|$)")) {	// rhel-x86_64-server-rhev-hdk-2-5 rhel-x86_64-server-rhev-hdk-2-5-beta
 				// Bug 840108 - channels for rhel-<ARCH>-rhev-hdk-2-5-* are not yet mapped to product certs in rcm/rhn-definitions.git
 				bugIds.add("840108");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-productivity-5-beta(-.+|$)")) {	// rhel-x86_64-server-productivity-5-beta rhel-x86_64-server-productivity-5-beta-debuginfo
 				// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugIds.add("840136");	// CLOSED in favor of bug 840099
 				bugIds.add("840099");	// CLOSED as a dup of bug 818202
 				bugIds.add("818202");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-rhsclient-5(-.+|$)")) {	// rhel-x86_64-server-rhsclient-5 rhel-x86_64-server-rhsclient-5-debuginfo
 				// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugIds.add("840136");	// CLOSED in favor of bug 840099
 				bugIds.add("840099");	// CLOSED as a dup of bug 818202
 				bugIds.add("818202");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-xfs-5(-.+|$)")) {	// rhel-x86_64-server-xfs-5 rhel-x86_64-server-xfs-5-beta
 				// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugIds.add("840136");	// CLOSED in favor of bug 840099
 				bugIds.add("840099");	// CLOSED as a dup of bug 818202
 				bugIds.add("818202");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-5-shadow(-.+|$)")) {	// rhel-x86_64-server-5-shadow-debuginfo
 				// Bug 840136 - various rhel channels are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugIds.add("840136");	// CLOSED in favor of bug 840099
 				bugIds.add("840099");	// CLOSED as a dup of bug 818202
 				bugIds.add("818202");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-eucjp-5(-.+|$)")) {	// rhel-x86_64-server-eucjp-5 rhel-x86_64-server-eucjp-5-beta etc.
 				// Bug 840148 - missing product cert corresponding to "Red Hat EUCJP Support (for RHEL Server)"
 				bugIds.add("840148");
 				// Bug 847069 - Add certificates for rhel-x86_64-server-eucjp-5* channels.
 				bugIds.add("847069");
 			}
 			if (rhnAvailableChildChannel.startsWith("rhx-")) {	// rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5 rhx-amanda-enterprise-backup-2.6-rhel-x86_64-server-5 etcetera
 				// Bug 840111 - various rhx channels are not yet mapped to product certs in rcm/rcm-metadata.git 
 				bugIds.add("840111");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-rhsclient-6(-.*|$)")) {	// rhel-x86_64-server-rhsclient-6 rhel-x86_64-server-rhsclient-6-debuginfo
 				// Bug 872980 - channels for rhel-<ARCH>-server-rhsclient-6* are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugIds.add("872980");
 			}
 			if (rhnAvailableChildChannel.matches("rhel-.+-server-6-ost-folsom(-.*|$)")) {	// rhel-x86_64-server-6-ost-folsom  rhel-x86_64-server-6-ost-folsom-debuginfo
 				// Bug 872983 - channels for rhel-<ARCH>-server-6-ost-folsom* are not yet mapped to product certs in rcm/rcm-metadata.git
 				bugIds.add("872983");
 			}
 			
 			BlockedByBzBug blockedByBzBug = new BlockedByBzBug(bugIds.toArray(new String[]{}));
 			ll.add(Arrays.asList(new Object[]{blockedByBzBug,	rhnAvailableChildChannel}));
 		}
 		
 		return ll;
 	}
 	
 	
 
 
 
 	
 	
 	
 }
 
 
 
 
 // Notes ***********************************************************************
 
 // EXAMPLE FOR install-num-migrate-to-rhsm TAKEN FROM THE DEPLOYMENT GUIDE http://documentation-stage.bne.redhat.com/docs/en-US/Red_Hat_Enterprise_Linux/5/html/Deployment_Guide/rhn-install-num.html
 //	[root@jsefler-onprem-5server ~]# python /usr/lib/python2.4/site-packages/instnum.py da3122afdb7edd23
 //	Product: RHEL Client
 //	Type: Installer Only
 //	Options: Eval FullProd Workstation
 //	Allowed CPU Sockets: Unlimited
 //	Allowed Virtual Instances: Unlimited
 //	Package Repositories: Client Workstation
 //
 //	key: 14299426 'da3122'
 //	checksum: 175 'af'
 //	options: 4416 'Eval FullProd Workstation'
 //	socklimit: -1 'Unlimited'
 //	virtlimit: -1 'Unlimited'
 //	type: 2 'Installer Only'
 //	product: 1 'client'
 //
 //	{'Workstation': 'Workstation', 'Base': 'Client'}
 //
 //	da31-22af-db7e-dd23
 //	[root@jsefler-onprem-5server ~]# 
 //
 //	[root@jsefler-onprem-5server ~]# install-num-migrate-to-rhsm -d -i da3122afdb7edd23
 //	Copying /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-efa6382a-44c4-408b-a142-37ad4be54aa6-71.pem to /etc/pki/product/71.pem
 //	Copying /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-efe91c1c-78d7-4d19-b2fb-3c88cfc2da35-68.pem to /etc/pki/product/68.pem
 //	[root@jsefler-onprem-5server ~]# 
 //	[root@jsefler-onprem-5server ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Client-Client-x86_64-efe91c1c-78d7-4d19-b2fb-3c88cfc2da35-68.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.68.1: 
 //	                . Red Hat Enterprise Linux Desktop
 //	            1.3.6.1.4.1.2312.9.1.68.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.68.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.68.4: 
 //	                ..rhel-5,rhel-5-client
 //	[root@jsefler-onprem-5server ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Client-Workstation-x86_64-efa6382a-44c4-408b-a142-37ad4be54aa6-71.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.71.1: 
 //	                .$Red Hat Enterprise Linux Workstation
 //	            1.3.6.1.4.1.2312.9.1.71.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.71.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.71.4: 
 //	                .,rhel-5-client-workstation,rhel-5-workstation
 //	[root@jsefler-onprem-5server ~]# 
 	
 	
 // EXAMPLE FOR install-num-migrate-to-rhsm
 //	[root@dell-pe1855-01 ~]# ls /etc/pki/product/
 //	69.pem
 //	[root@dell-pe1855-01 ~]# cat /etc/redhat-release 
 //	Red Hat Enterprise Linux Server release 5.8 Beta (Tikanga)
 //	[root@dell-pe1855-01 ~]# openssl x509 -text -in /etc/pki/product/69.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.69.1: 
 //	                ..Red Hat Enterprise Linux Server
 //	            1.3.6.1.4.1.2312.9.1.69.2: 
 //	                ..5.8 Beta
 //	            1.3.6.1.4.1.2312.9.1.69.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.69.4: 
 //	                ..rhel-5,rhel-5-server
 //	
 //	[root@dell-pe1855-01 ~]# cat /etc/sysconfig/rhn/install-num 
 //	49af89414d147589
 //	[root@dell-pe1855-01 ~]# install-num-migrate-to-rhsm -d -i 49af89414d147589
 //	Copying /usr/share/rhsm/product/RHEL-5/Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem to /etc/pki/product/69.pem
 //	Copying /usr/share/rhsm/product/RHEL-5/Server-ClusterStorage-x86_64-66e8d727-f5aa-4e37-a04b-787fbbc3430c-90.pem to /etc/pki/product/90.pem
 //	Copying /usr/share/rhsm/product/RHEL-5/Server-Cluster-x86_64-bebfe30e-22a5-4788-8611-744ea744bdc0-83.pem to /etc/pki/product/83.pem
 //	[root@dell-pe1855-01 ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Server-Server-x86_64-fbe6b460-a559-4b02-aa3a-3e580ea866b2-69.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.69.1: 
 //	                ..Red Hat Enterprise Linux Server
 //	            1.3.6.1.4.1.2312.9.1.69.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.69.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.69.4: 
 //	                ..rhel-5,rhel-5-server
 //	
 //	[root@dell-pe1855-01 ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Server-ClusterStorage-x86_64-66e8d727-f5aa-4e37-a04b-787fbbc3430c-90.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.90.1: 
 //	                .<Red Hat Enterprise Linux Resilient Storage (for RHEL Server)
 //	            1.3.6.1.4.1.2312.9.1.90.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.90.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.90.4: 
 //	                .2rhel-5-server-clusterstorage,rhel-5-clusterstorage
 //	                
 //	[root@dell-pe1855-01 ~]# openssl x509 -text -in /usr/share/rhsm/product/RHEL-5/Server-Cluster-x86_64-bebfe30e-22a5-4788-8611-744ea744bdc0-83.pem | grep -A1 1.3.6.1.4.1.2312.9.1
 //	            1.3.6.1.4.1.2312.9.1.83.1: 
 //	                .<Red Hat Enterprise Linux High Availability (for RHEL Server)
 //	            1.3.6.1.4.1.2312.9.1.83.2: 
 //	                ..5.7
 //	            1.3.6.1.4.1.2312.9.1.83.3: 
 //	                ..x86_64
 //	            1.3.6.1.4.1.2312.9.1.83.4: 
 //	                .$rhel-5-server-cluster,rhel-5-cluster
 
 	
 
 	
 // 	EXAMPLE FOR rhn-migrate-classic-to-rhsm
 //	[root@jsefler-onprem-5server ~]# rhnreg_ks -v --serverUrl=https://xmlrpc.rhn.code.stage.redhat.com/XMLRPC --username=qa@redhat.com --password=CHANGE-ME --force --norhnsd --nohardware --nopackages --novirtinfo 
 //		[root@jsefler-onprem-5server ~]# rhn-migrate-classic-to-rhsm -c
 //		RHN Username: qa@redhat.com
 //		Password: 
 //
 //		Retrieving existing RHN classic subscription information ...
 //		+----------------------------------+
 //		System is currently subscribed to:
 //		+----------------------------------+
 //		rhel-x86_64-server-5
 //
 //		List of channels for which certs are being copied
 //		rhel-x86_64-server-5
 //
 //		Product Certificates copied successfully to /etc/pki/product !!
 //
 //		Preparing to unregister system from RHN classic ...
 //		System successfully unregistered from RHN Classic.
 //
 //		Attempting to register system to Certificate-based RHN ...
 //		The system has been registered with id: 78cb5e26-3a5a-459d-848c-d5b3102a864d 
 //		System 'jsefler-onprem-5server.usersys.redhat.com' successfully registered to Certificate-based RHN.
 //
 //		Attempting to auto-subscribe to appropriate subscriptions ...
 //		Installed Product Current Status:         
 //
 //		ProductName:          	Red Hat Enterprise Linux Server
 //		Status:               	Subscribed             
 //
 //
 //		Please visit https://access.redhat.com/management/consumers/78cb5e26-3a5a-459d-848c-d5b3102a864d to view the details, and to make changes if necessary.
 //		[root@jsefler-onprem-5server ~]# subscription-manager unregister
 //		System has been un-registered.
 //		[root@jsefler-onprem-5server ~]# 
 
 	
 //	EXAMPLE FOR rhn-migrate-classic-to-rhsm
 //	[root@jsefler-onprem-5server rhn]# rhnreg_ks  --serverUrl=https://xmlrpc.rhn.code.stage.redhat.com/XMLRPC --username=qa@redhat.com --password=CHANGE-ME --force --norhnsd --nohardware --nopackages --novirtinfo
 //		ERROR: refreshing remote package list for System Profile
 //		[root@jsefler-onprem-5server rhn]# rhn-channel --list
 //		rhel-x86_64-server-5
 //		[root@jsefler-onprem-5server rhn]# rhn-channel --user=qa@redhat.com --password=CHANGE-ME --add -c  rhel-x86_64-server-5-debuginfo -c rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5
 //		[root@jsefler-onprem-5server rhn]# rhn-channel --list
 //		rhel-x86_64-server-5
 //		rhel-x86_64-server-5-debuginfo
 //		rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5
 //		[root@jsefler-onprem-5server rhn]# rhn-migrate-classic-to-rhsm --no-auto
 //		RHN Username: qa@redhat.com
 //		Password: 
 //
 //		Retrieving existing RHN classic subscription information ...
 //		+----------------------------------+
 //		System is currently subscribed to:
 //		+----------------------------------+
 //		rhel-x86_64-server-5
 //		rhel-x86_64-server-5-debuginfo
 //		rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5
 //
 //		+--------------------------------------------------+
 //		Below mentioned channels are NOT available on RHSM
 //		+--------------------------------------------------+
 //		rhx-alfresco-enterprise-2.0-rhel-x86_64-server-5
 // ^ THESE CHANNELS ARE IN THE MAP FILE MAPPED TO none
 //
 //		+---------------------------------------------------------------------------------------+ 
 //		Unrecognized channels. Channel to Product Certificate mapping missing for these channels.
 //		+---------------------------------------------------------------------------------------+
 //		rhel-x86_64-server-5-debuginfo
 // ^ THESE CHANNELS ARE NOT IN THE MAP FILE AT ALL
 //
 //		Use --force to ignore these channels and continue the migration.
 //
 //		[root@jsefler-onprem-5server rhn]# echo $?
 //		1
 //		[root@jsefler-onprem-5server rhn]# 
 
 
 //	RHEL59 EXAMPLE FOR rhn-migrate-classic-to-rhsm --servicelevel=INVALID_SLA
 //	[root@jsefler-rhel59 ~]# rhn-migrate-classic-to-rhsm --servicelevel=INVALID_SLA
 //	Red Hat account: qa@redhat.com
 //	Password: 
 //	
 //	Retrieving existing RHN Classic subscription information ...
 //	+----------------------------------+
 //	System is currently subscribed to:
 //	+----------------------------------+
 //	rhel-x86_64-server-5
 //	
 //	List of channels for which certs are being copied
 //	rhel-x86_64-server-5
 //	
 //	Product certificates copied successfully to /etc/pki/product
 //	
 //	Preparing to unregister system from RHN Classic ...
 //	System successfully unregistered from RHN Classic.
 //	
 //	Attempting to register system to Red Hat Subscription Management ...
 //	The system has been registered with id: 8fdf28e3-dc3a-44ae-910c-0f57c5187ba4 
 //	System 'jsefler-rhel59.usersys.redhat.com' successfully registered to Red Hat Subscription Management.
 //	
 //	
 //	Service level "INVALID_SLA" is not available.
 //	Please select a service level agreement for this system.
 //	1. SELF-SUPPORT
 //	2. PREMIUM
 //	3. STANDARD
 //	4. NONE
 //	5. No service level preference
 //	? 2
 //	Attempting to auto-subscribe to appropriate subscriptions ...
 //	Service level set to: PREMIUM
 //	Installed Product Current Status:
 //	Product Name:         	Red Hat Enterprise Linux Server
 //	Status:               	Not Subscribed
 //	
 //	
 //	Unable to auto-subscribe.  Do your existing subscriptions match the products installed on this system?
 //	
 //	Please visit https://access.redhat.com/management/consumers/8fdf28e3-dc3a-44ae-910c-0f57c5187ba4 to view the details, and to make changes if necessary.
 //	[root@jsefler-rhel59 ~]# 
 
