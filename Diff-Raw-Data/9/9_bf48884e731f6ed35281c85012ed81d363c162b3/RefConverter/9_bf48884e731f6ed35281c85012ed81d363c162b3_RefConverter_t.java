 /*
  * Created on 2004/11/23
  * Author aki@akjava.com
  * License Apache2.0 or Common Public License
  */
 package com.akjava.wiki.client.htmlconverter;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.akjava.wiki.client.core.Node;
 import com.akjava.wiki.client.modules.SimpleCommand;
 
 /**
  * 
  *
  */
 public class RefConverter extends AbstractConverter{
 
 
 
 private List<BaseHost> baseHosts=new ArrayList<BaseHost>();
 public static class BaseHost{
 	private String baseHost;
 	private String startName;
 	public BaseHost(String startImage, String imageHost) {
 		this.startName=startImage;
 		this.baseHost=imageHost;
 	}
 	public String getBaseHost() {
 		return baseHost;
 	}
 	public void setBaseHost(String baseHost) {
 		this.baseHost = baseHost;
 	}
 	public String getStartName() {
 		return startName;
 	}
 	public void setStartName(String startName) {
 		this.startName = startName;
 	}
 }
 public void addBaseHost(BaseHost baseHost){
 	baseHosts.add(baseHost);
 }
 public boolean canConvert(Node node){
     return (node instanceof SimpleCommand) && ((SimpleCommand)node).getName().equals("ref");
 	}
 public String toHeader(Node node){
     String result="";
     result+="<p>";
     String attributes[]=((SimpleCommand)node).getAttributes();
     if(attributes.length>1){
        result+="<a href='"+attributes[1]+"'>";
     }
     
     if(attributes.length>0){
     	for(int i=0;i<baseHosts.size();i++){
     		if(attributes[0].startsWith(baseHosts.get(i).getStartName())){
    			attributes[0]=baseHosts.get(i).getBaseHost()+attributes[0];
     			break;
     		}
     	}
      
       
        result+="<img src='"+attributes[0]+"'>";
     }
     
     return result;
 	}
 public String toFooter(Node node){
     String attributes[]=((SimpleCommand)node).getAttributes();
     if(attributes.length>1){
         return "</a></p>";
     }
     return "</p>";
 }
 }
