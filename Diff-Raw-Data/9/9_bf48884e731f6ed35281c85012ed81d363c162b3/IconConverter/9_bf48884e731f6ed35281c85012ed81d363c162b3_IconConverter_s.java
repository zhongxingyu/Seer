 /*
  * Created on 2004/11/23
  * Author aki@akjava.com
  * License Apache2.0 or Common Public License
  */
 package com.akjava.wiki.client.htmlconverter;
 
 
 import com.akjava.wiki.client.core.Node;
 import com.akjava.wiki.client.modules.SimpleCommand;
 
 /**
  * 
  *
  */
 public class IconConverter extends AbstractConverter{
 
 	private  String iconHost=null;
 	private  String iconDir="img";
 	private String iconExtension="png";
 	
 public boolean canConvert(Node node){
     return (node instanceof SimpleCommand) && ((SimpleCommand)node).getName().equals("icon");
 	}
 public String toHeader(Node node){
     String result="";
     if(iconHost==null){
    result+="<p><img src='/"+iconDir+"'/";
     }else{
     	 result+="<p><img src='"+iconHost+"/"+iconDir+"/";
     }
     String attributes[]=((SimpleCommand)node).getAttributes();
     if(attributes.length>0){
        result+=attributes[0];
     }else{
         throw new RuntimeException("icon command need attribute[0]:"+node);
     }
     result+="."+iconExtension+"'>";
     return result;
 	}
 public String toFooter(Node node){
     return "</p>";
 }
 public String getIconHost() {
 	return iconHost;
 }
 public void setIconHost(String iconHost) {
 	this.iconHost = iconHost;
 }
 public String getIconDir() {
 	return iconDir;
 }
 public void setIconDir(String iconDir) {
 	this.iconDir = iconDir;
 }
 public String getIconExtension() {
 	return iconExtension;
 }
 public void setIconExtension(String iconExtension) {
 	this.iconExtension = iconExtension;
 }
 }
