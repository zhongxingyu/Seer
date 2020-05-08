 /*
 /*    Copyright 2012 Balajee.R.C
 /*    
 /*    This file is part of SpellSpider
 /*    
 /*    SpellSpider is free software: you can redistribute it and/or modify
 /*    it under the terms of the GNU General Public License as published by
 /*    the Free Software Foundation, either version 3 of the License, or
 /*    (at your option) any later version.
 /*    
 /*    SpellSpider is distributed in the hope that it will be useful,
 /*    but WITHOUT ANY WARRANTY; without even the implied warranty of
 /*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 /*    GNU General Public License for more details.
 /*    
 /*    You should have received a copy of the GNU General Public License
 /*    along with SpellSpider.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package info.balajeerc.spellspider;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.concurrent.locks.ReentrantLock;
 
import org.apache.commons.io.FilenameUtils;
 import org.languagetool.JLanguageTool;
 import org.languagetool.Language;
 import org.languagetool.rules.Rule;
 import org.languagetool.rules.RuleMatch;
 
 public class PageChecker {
 	private static PageChecker Instance;
 	private JLanguageTool languageTool;
 	private String outputFilePath;
 	private OutputQueue outputQueue;
 	private final ReentrantLock lock = new ReentrantLock();
 
 	private PageChecker(OutputQueue outputQueue){
         languageTool=null;
         this.outputQueue = outputQueue;
        outputFilePath = FilenameUtils.concat(outputQueue.getOutputDirectory(), "errors.csv");
         try {
         		languageTool = new JLanguageTool(Language.AMERICAN_ENGLISH);
         		languageTool.activateDefaultPatternRules();        		
         		//Write the header for the output file
         		FileWriter outFile = new FileWriter(outputFilePath);
             	outFile.write("Url, Description, In Text, Error, Category\n");
             	outFile.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 	
 	public static void CreateInstance(OutputQueue queue){
 		Instance = new PageChecker(queue);
 	}
 	
 	public static PageChecker GetInstance(){
 		return Instance;
 	}
 	
 	public void checkText(String textToCheck, String currentUrl){
 		lock.lock();
 		ArrayList<String> errorEntries = new ArrayList<String>();
 		try {
 	        List<RuleMatch> matches = languageTool.check(textToCheck);
 	        //Split the text into its constituent lines
 	        //String lines[] = textToCheck.split("\\r?\\n");	        
 	        for (RuleMatch match : matches) {
 	    		//Format: Url, Description, In Text, Error, Category
 	        	String errorEntry = "";
 	    		Rule correctionRule = match.getRule();
 	        	errorEntry += 	currentUrl+","+
 	        					match.getMessage()+","+
 	        					//textToCheck.substring(match.getColumn(), match.getEndColumn())+","+
 	        					textToCheck.substring(match.getFromPos(), match.getToPos())+","+
 	        					correctionRule.getCategory().toString()+"\n";
 	    		errorEntries.add(errorEntry);	    		
 	        }
 	        String outputString = "";
 	        int numEntries = errorEntries.size();
 	        if(numEntries>0){
 	        	for(int i=0; i<numEntries; i++){
 	        		outputString+=errorEntries.get(i);
 	        	}
 	        	outputQueue.addError(outputString);
 	        }
 		}catch(IOException e){
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		lock.unlock();
 	}	
 }
