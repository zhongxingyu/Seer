 package net.derkholm.nmica.extra.app;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.motif.Motif;
 import net.derkholm.nmica.motif.MotifIOTools;
 
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview="A tool for splitting motifs in an XMS motif set", 
 		generateStub=true)
 @NMExtraApp(launchName="nmsplit", vm=VirtualMachine.SERVER)
 public class MotifSetSplitter {
 	private String prefix;
 	
 	@Option(help="Output filename prefix",optional=true)
 	public void setPrefix(String str) {
 		this.prefix = str;
 	}
 		
 	public void main(String[] args) throws Exception {
 		for (String filen : args) {
 			try {
 				FileReader fileReader = new FileReader(new File(filen));
 				Motif[] motifs = MotifIOTools.loadMotifSetXML(fileReader);
 				if (fileReader != null) fileReader.close();
 				
 				for (Motif m : motifs) {
					
 					MotifIOTools.writeMotifSetXML(
						new FileOutputStream(prefix + m.getName() + ".xms"), new Motif[] {m});
 				}
 			} catch (Exception e) {
 				System.err.println(
 					"ERROR! Could not parse motifs from " + filen);
 			}
 		}
 	}
 }
