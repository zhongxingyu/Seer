 package ontology;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import analyse.MyDocument;
 
 public class HTMLTagger {
 
 	public static void tagHTML(HashMap<MyDocument, LinkedDocument> docs) {		
 		for (LinkedDocument doc : docs.values()){
 			String line;
 			ArrayList<String> lignes = new ArrayList<String>();
 			InputStream file = null;
 			
 			String repoSaved="docTxt/docSaved/";
 			String repoTagged="docTxt/docTagged/";
 			
 			try {
 				file = new FileInputStream(repoSaved+doc.getHTMLfile());
 			} catch (FileNotFoundException e) {}
 			
 			BufferedReader br = new BufferedReader(new InputStreamReader(file));
 			try {
 				while((line = br.readLine()) !=null){
 					lignes.add(line);	
 				}
 				br.close();
 				file.close();
 			} catch (IOException e) {e.printStackTrace();}
 		
 			for(int n =0;n<lignes.size();n++){
 				ArrayList<String> newline = new ArrayList<String>();
 				String s = lignes.get(n);
 				int i=0,j=0;
 				while(i<s.length()){
 					char c = s.charAt(i);
 					if (c=='<' || c=='>' || c==' ' || c=='\''||c==8217 || c==';' || c=='.' || c==',' || c=='(' || c==')'){
 					//	System.out.println("caractère stop : "+c+" i="+i+" j="+j);
 					//	System.out.println((int)s.charAt(j+1));
 						newline.add(testMot(s.substring(j,i),doc));
 						j=i+1;
 						newline.add(s.substring(i,j));
 					}
					i++;
 				}
 				newline.add(s.substring(j,s.length()));		
 				lignes.set(n, append(newline));
 			}
 		
 			try {
 				FileWriter fw = new FileWriter(repoTagged+doc.HTMLfile);
 				BufferedWriter bw = new BufferedWriter(fw);
 				
 				for(String s: lignes){
 				//	System.out.println(s);
 					bw.write(s+"\n");
 				}
 				bw.close();
 				fw.close();
 			} catch (IOException e) {e.printStackTrace();}
 			lignes.clear();
 		}
 			
 	}
 
 	private static String append(ArrayList<String> newline) {
 		String result = "";
 		for (String s : newline)
 			result = result+s;
 		return result;
 	}
 
 	private static String testMot(String mot, LinkedDocument doc) {
 		if (doc.getWordsToEnhance().contains(mot.toLowerCase()))
 			//return ("<span class=\"verbeOnto\">"+mot+"</span>");
 			return ("<span style=\"color:red;\">"+mot+"</span>");
 		else if (doc.getWordsToSuggest().contains(mot.toLowerCase()))
 			//return ("<span class=\"verbePasOnto\">"+mot+"</span>");
 			return ("<span style=\"color:blue;\">"+mot+"</span>");
 		else if (doc.getMotsPropres().contains(mot.toLowerCase()))
 			//return ("<span class=\"nomsPropres;\">"+mot+"</span>");
 			return ("<span style=\"color:green;\">"+mot+"</span>");
 		else if (doc.getHighTfidfWords().containsKey(mot.toLowerCase())){
 			float tfidf = doc.getHighTfidfWords().get(mot.toLowerCase());
 			//return ("<span class=\"highTfidf;\">"+mot+"</span>");
 			return ("<span style=\"color:#2c3e50;\" tfidf=\""+tfidf+
 						"\" title=\"tfidf: "+tfidf+"\" >"+mot+"</span>");
 		}
 			
 			
 		else return mot;
 	}
 }
