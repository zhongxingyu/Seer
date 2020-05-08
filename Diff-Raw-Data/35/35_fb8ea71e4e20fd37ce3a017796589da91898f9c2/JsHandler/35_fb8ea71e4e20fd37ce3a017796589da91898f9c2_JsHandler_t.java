 package com.theminequest.common.quest.js;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Level;
 
 import org.mozilla.universalchardet.UniversalDetector;
 
 import com.theminequest.api.Managers;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.quest.handler.QuestHandler;
 import com.theminequest.common.impl.v1parser.AcceptTextHandler;
 import com.theminequest.common.impl.v1parser.CancelTextHandler;
 import com.theminequest.common.impl.v1parser.CompleteTextHandler;
 import com.theminequest.common.impl.v1parser.DescriptionHandler;
 import com.theminequest.common.impl.v1parser.EditMessageHandler;
 import com.theminequest.common.impl.v1parser.FailedTextHandler;
 import com.theminequest.common.impl.v1parser.GetRequirementHandler;
 import com.theminequest.common.impl.v1parser.NameHandler;
 import com.theminequest.common.impl.v1parser.RepeatableHandler;
 import com.theminequest.common.impl.v1parser.RequirementHandler;
 import com.theminequest.common.impl.v1parser.ResetHandler;
 import com.theminequest.common.impl.v1parser.SpawnHandler;
 import com.theminequest.common.impl.v1parser.StartRequirementHandler;
 import com.theminequest.common.quest.v1.V1Parser;
 
 public class JsHandler implements QuestHandler<V1Parser> {
 	
 	private HashMap<String, V1Parser> parsers;
 	private UniversalDetector encodingDetector;
 	
 	public JsHandler() {
 		parsers = new HashMap<String, V1Parser>();
 		encodingDetector = new UniversalDetector(null);
 		
 		setupDefaults();
 	}
 	
 	private void setupDefaults() {
 		addParser("accepttext", new AcceptTextHandler());
 		addParser("canceltext", new CancelTextHandler());
 		addParser("description", new DescriptionHandler());
 		addParser("editmessage", new EditMessageHandler());
 		addParser("failedtext", new FailedTextHandler());
 		addParser("finishtext", new CompleteTextHandler());
 		addParser("getreq", new GetRequirementHandler());
 		addParser("name", new NameHandler());
 		addParser("repeatable", new RepeatableHandler());
 		addParser("requirement", new RequirementHandler());
 		addParser("reset", new ResetHandler());
 		addParser("spawn", new SpawnHandler());
 		addParser("startreq", new StartRequirementHandler());
 	}
 	
 	@Override
 	public QuestDetails parseQuest(File questFile) throws IOException {
 		
 		// questFile is the jsq file (js quest file)
 		
		JsQuestDetails details = new JsQuestDetails(questFile);
 		
 		// detect encoding
 		FileInputStream encodingStreamDetect = null;
 		String encoding = null;
 		try {
 			encodingStreamDetect = new FileInputStream(questFile);
 			encodingDetector.reset();
 			byte[] detectorBuffer = new byte[4096];
 			int nread;
 			while ((nread = encodingStreamDetect.read(detectorBuffer)) > 0 && !encodingDetector.isDone()) {
 				encodingDetector.handleData(detectorBuffer, 0, nread);
 			}
 			encodingDetector.dataEnd();
 			encoding = encodingDetector.getDetectedCharset();
 		} finally {
 			if (encodingStreamDetect != null)
 				encodingStreamDetect.close();
 		}
 		
 		Scanner fileReader = null;
 		try {
 			if (encoding == null)
 				fileReader = new Scanner(questFile);
 			else
 				fileReader = new Scanner(questFile, encoding);
 			
 			int line = 0;
 			
 			while (fileReader.hasNextLine()) {
 				line++;
 				String nextline = new String(fileReader.nextLine().getBytes("UTF-8"), "UTF-8").trim();
 				if (nextline.startsWith("#") || nextline.isEmpty()) // ignore
 																	// and
 																	// continue
 					continue;
 				else if (nextline.startsWith(">>>"))
 					// we'll use >>> to denote the actual breaking of the
 					// properties at the top and the js itself
 					break;
 				
 				List<String> ar = new ArrayList<String>();
 				for (String s : nextline.split(":"))
 					ar.add(s);
 				String type = ar.remove(0).trim();
 				
 				if (!hasParser(type)) {
 					Managers.log(Level.WARNING, "[Common|V1Handler] Unable to handle " + type + " at " + questFile.getName() + ":" + line + ".");
 					continue;
 				}
 				
 				V1Parser handler = getParser(type);
 				handler.handle(details, ar);
 			}
 			
 			line++;
 			details.setProperty(JsQuestDetails.JS_LINESTART, line);
 			
 			StringBuilder jsScript = new StringBuilder();
 			while (fileReader.hasNextLine())
 				jsScript.append(fileReader.nextLine()).append('\n');
 			details.setProperty(JsQuestDetails.JS_SOURCE, jsScript.toString());
 			
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (fileReader != null)
 				fileReader.close();
 		}
 		
 		return details;
 	}
 	
 	@Override
 	public void addParser(String name, V1Parser parser) {
 		parsers.put(name.toLowerCase(), parser);
 	}
 	
 	@Override
 	public boolean hasParser(String name) {
 		return parsers.containsKey(name.toLowerCase());
 	}
 	
 	@Override
 	public V1Parser getParser(String name) {
 		return parsers.get(name.toLowerCase());
 	}
 	
 	@Override
 	public V1Parser removeParser(String name) {
 		return parsers.remove(name.toLowerCase());
 	}
 	
 }
