 package com.telefonica.claudia.smi.provisioning;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.junit.Test;
 
 public class OneVmUtilitiesTest {
 
 
 	/*public void testOneVmUtilities() {
 		
 		OneOperations oneoperations = prepareOneOperations ();
 		OneVmUtilities vmutils = new OneVmUtilities (oneoperations, "3.0", "br0",  "/opt/claudia/repository", null, "18888",
 				"/boot/vmlinuz-2.6.26-2-xen-amd64", "/boot/initrd.img-2.6.26-2-xen-amd64", "s" , "", "10.65.44.44");
 		
 		
 		
 		String pathxml = "src/test/resources/ovf.xml";
 		String xml = null;
 		try {
 			xml = readFileAsString(pathxml);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println (xml);
 		try {
 			vmutils.TCloud2ONEVM(xml, "CESGA.customers.laura.services.servi3.vees.dd");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}*/
 
 	@Test
 	public void testOneVmUtilitiesVEE() {
 		
 		OneOperations oneoperations = prepareOneOperations ();
 		OneVmUtilities vmutils = new OneVmUtilities (oneoperations, "3.0", "br0",  "/opt/claudia/repository", null, "18888",
				"/boot/vmlinuz-2.6.26-2-xen-amd64", "/boot/initrd.img-2.6.26-2-xen-amd64", null , null, "10.65.44.44"); //"s"
 		
 		
 		
 		String pathxml = "src/test/resources/ovf.xml";
 		String xml = null;
 		try {
 			xml = readFileAsString(pathxml);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//System.out.println ("XML " +xml);
 		try {
 			System.out.println (vmutils.TCloud2ONEVM(xml, "henar.customers.cc1.services.ss1.vees.VEEMaster.replicas.1"));
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private OneOperations prepareOneOperations ()
 	{
 		String oneSession = "oneadmin:8944aada99d6d0121494dd0ac8129db653bc1f4a";
 		
 		// Testbed NUBA
 		/* oneUrl= http://84.21.173.28:2633/RPC2
 			oneUser=oneadmin
 			onePassword=8944aada99d6d0121494dd0ac8129db653bc1f4a
 			*/
 		
 		XmlRpcClient xmlRpcClient = new XmlRpcClient();
 		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 		try {
 			config.setServerURL(new URL("http://84.21.173.28:2633/RPC2"));
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		xmlRpcClient.setConfig(config);
 
 		
 		
 		OneOperations tester = new OneOperations(oneSession, xmlRpcClient);
 		return tester;
 	}
 	
 	private String readFileAsString(String filePath)
     throws java.io.IOException{
         StringBuffer fileData = new StringBuffer(1000);
         BufferedReader reader = new BufferedReader(
                 new FileReader(filePath));
         char[] buf = new char[1024];
         int numRead=0;
         while((numRead=reader.read(buf)) != -1){
             String readData = String.valueOf(buf, 0, numRead);
             fileData.append(readData);
             buf = new char[1024];
         }
         reader.close();
         return fileData.toString();
     }
 
 
 }
