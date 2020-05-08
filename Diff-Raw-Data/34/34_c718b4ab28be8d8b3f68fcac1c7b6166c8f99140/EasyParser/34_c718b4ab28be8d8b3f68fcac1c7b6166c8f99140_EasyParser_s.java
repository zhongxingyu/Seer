 package no.truben.uhs.format.parserImpl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import no.truben.uhs.format.Encryption;
 import no.truben.uhs.format.Hunk;
 import no.truben.uhs.format.HunkType;
 import no.truben.uhs.format.Parser;
 
 public class EasyParser implements Parser {
 	
 	private Map<Integer, Hunk> hunkLocations = new HashMap<Integer, Hunk>();
 	private List<Hunk> hunkLinks = new ArrayList<Hunk>();
 	
 	public Hunk parse(File file) throws IOException {
 		Scanner s = new Scanner(file);
 		
 		oldStuff(s);
 		
 		Hunk hunk = readHunk(s, null, 2);
 		fixLinks();
 		
 		return hunk;
 		
 	}
 	
 	private void fixLinks() {
 		for(Hunk h: hunkLinks) {
 			h.addHunk(hunkLocations.get(h.getLink()));
 		}
 	}
 	
 	private Hunk readHunk(Scanner s, int[] key, int currentLine) {
 		
 		Hunk thisHunk = new Hunk();
 		
 		String[] firstLine = s.nextLine().split(" ");
 		int lines = Integer.parseInt(firstLine[0]);
		HunkType type = HunkType.valueOf(firstLine[1].toUpperCase());
 		
 		thisHunk.setLines(lines);
 		thisHunk.setHunkType(type);
 		thisHunk.setMainLabel(s.nextLine());
 		thisHunk.setLineNumber(currentLine);
		
 		hunkLocations.put(currentLine, thisHunk);
 		
 		if(key == null)
 			key = Encryption.getKey(thisHunk.getMainLabel());
 		
 		String buffer = "";
 		
 		for(int line = 3; line <= lines; line++) {
 			
 			switch(thisHunk.getHunkType()) {
 			
 			case HINT:
 				String hintLine = s.nextLine();
 				if(hintLine.equals("-") || line == lines) {
 					thisHunk.addHint(Encryption.decryptOld(buffer));
 					buffer = "";
 				}
 				else
 					buffer += " " + hintLine;
 				
 				break;
 			case NESTHINT:
 				s.nextLine();
 				break;
			case TEXT:
				
				break;
 			case LINK:
 				thisHunk.setLink(Integer.parseInt(s.nextLine()));
 				hunkLinks.add(thisHunk);
 				break;
 			case SUBJECT:
 				Hunk newHunk = readHunk(s, key, line);
 				line += newHunk.getLines();
 				thisHunk.addHunk(newHunk);
 				break;
 			}
 		}
 		
 		return thisHunk;
 		
 	}
 
 	private void oldStuff(Scanner s) {
 		final String TERMINATE = "** END OF 88A FORMAT **";
 		while(s.hasNextLine()) {
 			if(s.nextLine().equals(TERMINATE))
 				break;
 		}
 	}
 	
 	
 
 }
