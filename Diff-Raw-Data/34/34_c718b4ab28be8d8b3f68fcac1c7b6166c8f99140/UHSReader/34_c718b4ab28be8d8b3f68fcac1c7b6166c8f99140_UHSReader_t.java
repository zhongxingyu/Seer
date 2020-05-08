 package no.truben.uhs;
 
 import java.io.File;
 import java.io.IOException;
 
 import no.truben.uhs.format.Hunk;
 import no.truben.uhs.format.Parser;
 import no.truben.uhs.format.parserImpl.EasyParser;
 import no.truben.uhs.view.HintView;
 import no.truben.uhs.view.impl.HierarchicalText;
 
 
 public class UHSReader {
 	public static void main(String args[]) {
 		try {
 			Parser parser = new EasyParser();
			//Hunk h = parser.parse(new File("tunguska.uhs"));
			Hunk h = parser.parse(new File("brokensword-dc.uhs"));
			
 			HintView hv = new HierarchicalText();
 			hv.DisplayHints(h);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
