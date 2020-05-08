 package rest;
 
 import java.util.ArrayList;
 
 public class NetworkStatus {
     ArrayList <String> mMessageNames;
 	ArrayList <String> mMessageValues;
     
     public NetworkStatus()
     {
         mMessageNames = new ArrayList<String>();
         mMessageValues = new ArrayList<String>();
     }  // NetworkStatus
     
     public void setMessage(String pMessageName, String pMessageValue)
     {
     	mMessageNames.add(pMessageName);
    	mMessageValues.add(pMessageValue);
     }  // public void setMessage
     
     @Override
     public String toString()
     {
     	int lMessages = mMessageNames.size();
     	StringBuilder lString = new StringBuilder();
     	
     	lString.append("[");
     	
     	for(int lIndex = 0; lIndex < lMessages; lIndex++)
     	{
     		lString.append("{");
     		
     		lString.append("\"name\":\"" + mMessageNames.get(lIndex) + "\",");
     		lString.append("\"value\":\"" + mMessageValues.get(lIndex) + "\"");
     		
     		lString.append("}");
     	
     		if(lIndex + 1 < lMessages)
     		{
     			lString.append(",");
     		}  // if
     	}  // for
     	
     	lString.append("]");
         return lString.toString(); 
     }  // String toString
 }
