 package com.golddigger.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.golddigger.templates.GameTemplate;
 import com.golddigger.templates.generators.TemplateGenerator;
 
 public class FieldFileTemplateGenerator implements TemplateGenerator{
 
 	private File dir;
 
 	public void setDirectory(File directory){
 		this.dir = directory;
 	}
 
 	@Override
 	public GameTemplate[] generate() {
 		List<GameTemplate> templates = new ArrayList<GameTemplate>();
 		for (File file : dir.listFiles()){
 			try {
 				String text = load(file);
 				templates.add(LegacyTemplateParser.parse(text));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return templates.toArray(new GameTemplate[]{});
 	}
 	
	private String load(File file) throws IOException {
 		System.out.println("loading: "+file.getCanonicalPath());
 		String contents = null;
 		BufferedReader in =  new BufferedReader(new FileReader(file));
 		String line = null;
		while ((line = in.readLine()) != null) contents += line+"\n";
 		return contents;
 	}
 }
