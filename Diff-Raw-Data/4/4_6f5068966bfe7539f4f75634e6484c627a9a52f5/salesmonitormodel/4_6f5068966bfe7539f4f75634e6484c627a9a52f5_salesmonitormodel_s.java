 package com.ai;
 
 import java.io.IOException;
 import java.util.Vector;
 import java.util.Hashtable;
 
 import org.ksoap2.SoapEnvelope;
 import org.ksoap2.serialization.SoapObject;
 import org.ksoap2.serialization.SoapSerializationEnvelope;
 import org.ksoap2.transport.HttpTransport;
 import org.xmlpull.v1.XmlPullParserException;
 
 public class salesmonitormodel 
 {
	//private String appserver="http://www.apweb.com.ve/webservice/server.php";
	private String appserver="http://www.librerialatina.com.ve/webservice/server.php";
 	public salesmonitormodel()
 	{
 		
 	}
 	public Vector adquireData(String webmethod,String soapaction, Hashtable parameters) throws Exception
 	{
         String serviceUrl =this.appserver;         
         //String serviceNamespace = "http://tempuri.org";
         //String soapAction = "http://tempuri.org/xml";
         
         SoapObject rpc = new SoapObject(serviceUrl, webmethod);
         for (int i=1; i<=parameters.size();i++)        
         {
         parameter param=(parameter) parameters.get(new Integer(i)); 
         rpc.addProperty(param.getName(), param.getValue());
         }
         //rpc.addProperty("id",new Integer(1));
         
         SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
 
         envelope.bodyOut = rpc;
         //envelope.dotNet = true;
         //envelope.encodingStyle = SoapSerializationEnvelope.XSD;
 
         HttpTransport ht = new HttpTransport(serviceUrl);
         ht.debug = true;
         ht.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 
         try
         {
 	        ht.call(soapaction, envelope);
 		    SoapObject resultSoap = (SoapObject)envelope.bodyIn;
 		    Vector resultSoap2 = (Vector)resultSoap.getProperty(0);
 		    return resultSoap2;
 		    
             // do something with this later.
         }
         catch (IOException e) {
     		e.printStackTrace();
 
     		throw new Exception(e.getMessage());
     	} catch (XmlPullParserException e) {
     		e.printStackTrace();
     		throw new Exception(ht.responseDump);
     	}
         
         catch(Exception ex)
         {
         	String msg = ex.toString();
         	throw new Exception(msg);
         	// do something with this later.
         }		
 		
 	}
 	
 }
