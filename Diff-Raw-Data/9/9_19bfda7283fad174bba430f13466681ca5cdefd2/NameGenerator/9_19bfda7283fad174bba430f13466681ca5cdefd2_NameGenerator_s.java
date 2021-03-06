 package org.wescheme.project;
 import javax.jdo.PersistenceManager;
 import javax.servlet.ServletContext;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 
 
 public class NameGenerator {
 	private List<String> names;
 	private Random gen;
 	
 	private static String DICTIONARY = "/WEB-INF/five-letter-nouns.txt";
 	
 	
 	private static NameGenerator _instance;
 	
 	public static NameGenerator getInstance(ServletContext ctx) throws IOException {
 		if (_instance == null) {
 			_instance = new NameGenerator(ctx);
 		}
 		return _instance;
 	}
 		
 	private NameGenerator(ServletContext ctx) throws IOException {
 		this.names = new ArrayList<String>();
 		this.gen = new Random();
 		BufferedReader r = new BufferedReader(new InputStreamReader(ctx.getResourceAsStream(DICTIONARY)));
 		String nextLine;
 		while ((nextLine = r.readLine()) != null) {
 				this.names.add(nextLine);
 		}
 	}
 	
 	// Generates a new id that's unique from any other program's public id.
 	@SuppressWarnings({ "unchecked" })
 	public String generateUniqueName(PersistenceManager pm) throws IOException {
 		javax.jdo.Query query = pm.newQuery(Program.class);
 		query.setFilter("publicId_ == param");
 		query.declareParameters("String param");
 		try {
 			while (true) {
 				String aName = generateName();
 				List<Program> list = (List<Program>) query.execute(aName);
 				if (list.size() == 0) {
 					return aName;
 				}
 			}
 		} finally {
 			query.closeAll();
 		}
 	}
 	
 	
 	// Generates a random name, but does not check for uniqueness.
 	public String generateName() {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(names.get(gen.nextInt(names.size())));
 		for(int i = 1; i < 5; i++) {
 			buffer.append("-");
 			buffer.append(names.get(gen.nextInt(names.size())));
 		}
		return buffer.toString();
 	}
 }
