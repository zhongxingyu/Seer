 package com.orange.groupbuy.addressparser;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.htmlparser.Node;
 import org.htmlparser.Parser;
 import org.htmlparser.Tag;
 import org.htmlparser.tags.ScriptTag;
 import org.htmlparser.util.NodeIterator;
 import org.htmlparser.util.NodeList;
 
 import com.sun.xml.internal.bind.v2.runtime.property.StructureLoaderBuilder;
 
 public class GenericAddressParser extends CommonAddressParser {
 	private List<String> addList = new LinkedList<String>();
 	@Override
 	public List<String> doParseAddress(String url) {
 		try {
 			addList.clear();
 			HttpURLConnection connection = (HttpURLConnection)(new URL(url)).openConnection();
 			if (connection != null){
 				long fetchTime = System.currentTimeMillis();
 				Parser parser = new Parser(connection);
 				long parseStartTime = System.currentTimeMillis();
 				find_common_add(parser, url);
 				long parseEndTime = System.currentTimeMillis();
 				connection.disconnect();
 				
 				System.out.println("<debug> parsing address, network "+(parseStartTime - fetchTime)+
 						" millseconds, parse "+(parseEndTime-parseStartTime)+" millseconds");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} 
 		return addList;
 	}
 	/**
 	 * find the address
 	 * @param str
 	 */
 	private int searchAdd(String str){
 		int cnt = 0;
 		int index = str.indexOf("ַ");
 //		System.out.println("enter searchAdd");
 		if(index != -1){
 			String s1 = str.substring(index+3);
 			s1 = s1.trim();
 			String s2 = s1.substring(0, 50);
 			//index of the blank
 			int index2 = s2.indexOf("\\s");
 			if(index2 != -1){
 				String s3 = s2.substring(0, index2);
 				if(isLegal(s3)){
 					boolean result = addtoList(s3);
 					if (result)
 						cnt++;
 				}
 				int len = s3.length();
 				int index3 = str.indexOf(s3);
 				searchAdd(str.substring(index3 + len));
 			} else {
 				addtoList(s2);
 			}
 		}
 		return cnt;
 	}
 	/**
 	 * find the address from ,·֣,...
 	 * 
 	 */
 	private void searchRoad(String str){
 		String[] ss = null;
 //		System.out.println("enter searchRoad");
 		//split the blank
 		ss = str.split("\\s");
 		if (ss == null)
 			return;
 		
 		int len = ss.length;
 		if(len > 10){
 			int[] scores = new int[len];
 			for(int i=0; i<len; i++)
 				scores[i] = 0;
 			for(int i=0; i<len; i++){
 				if(ss[i].length() > 8 && ss[i].length() < 80){
 					if(ss[i].indexOf("") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("·") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("¥") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("") != -1)
 						scores[i]++;
 					if(ss[i].indexOf("㳡") != -1)
 						scores[i]++;
 				}
 			}
 			// bubble sort
 			int temp;
 			String stemp;
 			for(int i=0; i<scores.length; i++){
 				for(int j=0; j<scores.length-i-1; j++){
 					if(scores[j] < scores[j+1]){
 						temp = scores[j];
 						scores[j] = scores[j+1];
 						scores[j+1] = temp;
 						//swap the string at the same time
 						stemp = ss[j];
 						ss[j] = ss[j+1];
 						ss[j+1] = stemp;
 					}
 				}
 			}
 			int i=0;
 			//consider the score
 			while(scores[i] >= 3) {
 				addtoList(ss[i]);
 				i++;
 			}		
 		}
 	}
 	/**
 	 * 
 	 */	
 	
 	private boolean isLegal(String str){
 		int score = 0;
 		if(str.contains(""))
 			score ++;		
 		if(str.contains(""))
 			score ++;		
 		if (str.contains("·"))
 			score ++;
 		if (str.contains(""))
 			score ++;
 		if (str.contains(""))
 			score ++;
 		if (str.contains(""))
 			score ++;
 		if (str.contains(""))
 			score ++;
 		if (str.contains(""))
 			score ++;
 		if (str.contains(""))
 			score ++;
 		if (str.contains("¥"))
 			score ++;
 		if (str.contains("㳡"))
 			score ++;
 		
 		return (score >= 2);
 	}
 	/**
 	 * ݹɾscriptڵ
 	 * @param list
 	 */
 	private void deletJSnode(NodeList list) {
 		for (int i = 0; i < list.size(); i++) {
 			Node node = list.elementAt(i);
 			if (node instanceof Tag) {
 				if (node instanceof ScriptTag) {
 					list.remove(i);
 					continue;
 				}
 			}
 			NodeList children = node.getChildren();
 			if (children != null && children.size() > 0)
 				deletJSnode(children);
 		}
 	}
 	/**
 	 * 
 	 */
 	private boolean addtoList(String str){	// return true if a valid address is found
 		str = str.trim();
 		int len = str.length();
 		if (len <= 0)
 			return false;
 		
 		int index_add = str.indexOf("ַ");
 		int index_yuyue = str.indexOf("ԤԼ");
 		int index_phone = str.indexOf("绰");
 		
 		int start = 0;
 		int end = str.length();
 		if(index_add != -1){
 			start = index_add + 3;
 			if(start > str.length()){
 				start = str.length();
 			}
 		}
 
 		if(index_yuyue != -1){
 			end = index_yuyue;
 		} else if(index_phone != -1){
 			end = index_phone;
 		}
 		
 		if (start >= len){
 			start = len - 1;
 		}
 		if (end <= 0){
 			end = start;
 		}
 		
 		if (start > end){
 			end = len - 1;
 		}
 
 		// TODO remove
 		System.out.println("<debug> parse str="+str+", start="+start+",end="+end);
 		
 		String address = str.substring(start, end);
 		String[] finalList = address.split("\\s");
 		if (finalList != null && finalList.length > 0)
 			address = finalList[0];
 		
 		// TODO remove
 		System.out.println("<debug> parse str result="+address);
 
 		if (!isLegal(address)){
 			// TODO remove
 			System.out.println("<debug> parse str, it's not legal address, skip");
 			return false;
 		}
		
 		if(address.length() > 5 && address.length() < 50){
 			if(addList.indexOf(address) == -1){
 				addList.add(address);
 			}
 			
 			return true;
 		}
 		else{
			System.out.println("<debug> parse str, address length "+address.length()+" too short or too long, skip");
 			return false;			
		} 
 	}
 	/**
 	 * 
 	 */
 	private void find_common_add(Parser parser, String url){
 		try {
 			parser.setEncoding("UTF-8");
 			NodeList nodes = parser.parse(null);
 			deletJSnode(nodes);
 //			System.out.println("nodes.size() = " + nodes.size());
 			for (NodeIterator i = nodes.elements(); i.hasMoreNodes(); ) {
 				Node node = i.nextNode();
 				String text = node.toPlainTextString();
 				int cnt = searchAdd(text);
 				if(cnt == 0){
 					searchRoad(text);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 	
 }
