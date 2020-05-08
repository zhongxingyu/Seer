 package org.vinodkd.jnv;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.ArrayList;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Map;
 
 import org.stringtree.json.*;
 
 public class Notes implements Model{
 	private HashMap<String,Note> notes = new HashMap<String,Note>();
 
 	private final String ABOUTNOTE_lOC = "res/about.txt";
 
 	private String saveLoc;
 	private File saveFile;
 	private final String FILENAME = "notes.json";
 
 	public Notes(String saveLoc) {
 		this.saveLoc = saveLoc;
 		saveFile = new File(saveLoc + System.getProperty("file.separator") + FILENAME);
 		load();
 	}
 
 	public void add(Note n){ notes.put(n.getTitle(),n);	}
 	public void set(String title, String contents) {
 		Note n = new Note(title,contents);
 		notes.put(title,n);
 	}
 
 	public Note get(String title)	{ return notes.get(title);}
 
 	public void load(){
 		if(saveFile.exists()){
 			BufferedReader br;
 			StringBuffer notesAsStr = new StringBuffer("");
 			char[] buf = new char[4000];
 			int chars;
 
 			try{
 				br = new BufferedReader(new FileReader(saveFile));
 				while((chars = br.read(buf,0,4000))!= -1){
 					notesAsStr.append(buf,0,chars);
 				}
 				br.close();
 			}catch(Exception ioe){
 				//TODO: HANDLE IT IF REQD
 				// didnt feel like catching each exception separately: IOException, FileNotFoundException.
 			}
 
 			JSONReader notesReader = new JSONReader();
 
 			// System.out.println("read json:" + notesAsStr);
 
 			//notes = (HashMap<String,Note>)
 			Object allNotes = notesReader.read(notesAsStr.toString());
 			if(allNotes != null){
 				fromJson(allNotes);
 			}
 		}
 	}
 
 	private void fromJson(Object allNotes){
 		@SuppressWarnings("unchecked")
 		Map<Object,Object> map = (Map<Object,Object>) allNotes;
 
 		 Set<Object> keys = map.keySet();
 		 for(Object o: keys){
 		 	Note n = new Note("","");
 		 	n.fromJson(map.get(o));
 		 	JSONWriter jsw = new JSONWriter();
		 	//System.out.println("note:"+ n.getTitle() + ",date:" + n.getLastModified() + ",date from json:" + jsw.write(n.getLastModified()));
 		 	add(n);
 		 }
 	}
 
 	public void store(){
 		try{	
 			FileWriter fw = new FileWriter(saveFile);
 			String notesAsStr = new JSONWriter().write(notes);
 			// System.out.println("writing json:" + notesAsStr);
 			fw.write(notesAsStr);
 			fw.close();
 			System.out.println("stored in " + saveFile.toString());
 		}catch(Exception e){
 			System.out.println("Problems storing to " + saveFile.toString());
 		}
 	}
 
 	public Object getInitialValue(){
 		load();
 		if(!notes.containsKey("About")){
 			Note aboutNote = new Note("About", getAboutText());
 			add(aboutNote);
 		}
 		return notes;
 	}
 
 	private String getAboutText(){
 		InputStream is = getClass().getClassLoader().getResourceAsStream(ABOUTNOTE_lOC);
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
 		StringBuffer aboutText = new StringBuffer("");
 		String line;
 		try{
 			while((line=br.readLine())!=null){
 				aboutText.append(line);aboutText.append("\n");
 			}
 			br.close();
 		}catch(IOException ioe){
 			// TODO: PUT IN DEFAULT TEXT IF THIS FAILS
 		}
 		return aboutText.toString();
 
 	}
 
 	public Object getCurrentValue(){
 		return notes;
 	}
 
 	public List<String> search(String searchText){
 		ArrayList<String> searchResult = new ArrayList<String>();
 		Set<String> titles = notes.keySet();
 		for(String title: titles){
 			if(title.contains(searchText)){
 				searchResult.add(title);
 			}
 		}
 		return searchResult;
 	}
 }
