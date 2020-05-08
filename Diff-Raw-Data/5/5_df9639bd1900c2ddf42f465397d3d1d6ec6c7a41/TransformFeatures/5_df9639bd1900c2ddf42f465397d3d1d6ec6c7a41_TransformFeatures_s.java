 package net.derkholm.nmica.extra.app.seq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.program.gff.GFFDocumentHandler;
 import org.biojava.bio.program.gff.GFFParser;
 import org.biojava.bio.program.gff.GFFRecord;
 import org.biojava.bio.program.gff.GFFWriter;
 import org.biojava.bio.program.gff.SimpleGFFRecord;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview="Transform features in GFF files (move / scale)", generateStub=true)
 @NMExtraApp(launchName="nmtransformfeat", vm=VirtualMachine.SERVER)
 public class TransformFeatures {
 
 	private File featuresFile;
 	private int scaleTo;
 	private int moveBy;
 	private String outFile;
 	private GFFWriter gffw;
 	private int expandLeft;
 	private int expandRight;
 
 	@Option(help="Input features file (read from stdin if not specified)", optional=true)
 	public void setFeatures(File f) {
 		this.featuresFile = f;
 	}
 	
 	@Option(help="Scale regions to the specified number of nucleotides", optional=true)
 	public void setScaleTo(int i) {
 		this.scaleTo = i;
 	}
 	
 	@Option(help="Expand features to the left", optional=true)
	public void expandLeft(int i) {
 		this.expandLeft = i;
 	}
 	
 	@Option(help="Expand features to the right", optional=true)
 	public void setExpandRight(int i) {
 		this.expandRight = i;
 	}
 	
 	@Option(help="Move the regions by the  specified number of nucleotides", optional=true)
 	public void setMoveBy(int i) {
 		this.moveBy = i;
 	}
 	
 	@Option(help="Output features file (written to stdout if not specified)", optional=true)
 	public void setOut(String f) {
 		this.outFile = f;
 	}
 	
 	public void main(String[] args) throws SQLException, Exception {
 		
 		final OutputStream os;
 		if (this.outFile == null) {
 			os = System.out;
 		} else {
 			os = new FileOutputStream(this.outFile);
 		}
 
 		InputStream inputStream;
 		if (featuresFile == null) {
 			inputStream = System.in;
 		} else {
 			inputStream = new FileInputStream(this.featuresFile);
 		}
 		
 		gffw = new GFFWriter(new PrintWriter(new OutputStreamWriter(os)));
 		
 		GFFParser parser = new GFFParser();
 		parser.parse(
 				new BufferedReader(new InputStreamReader(inputStream)),
 				new GFFDocumentHandler() {
 
 					public void commentLine(String arg0) {
						
 					}
 
 					public void endDocument() {
 						gffw.endDocument();
 					}
 
 					public void recordLine(GFFRecord r) {
 						System.err.printf(".");
 
 						int start = r.getStart();
 						int end = r.getEnd();
 						int centrePoint = start + (end - start) / 2;
 						
 						int newStart, newEnd;
 						
 						if (scaleTo == 0) {
 							newStart = start;
 							newEnd = end;
 						} else {
 							newStart = Math.max(0,centrePoint - (scaleTo / 2));
 							newEnd = Math.max(0,centrePoint + (scaleTo / 2));
 						}
 						
 						if (expandLeft != 0) {
 							newStart = newStart - expandLeft;
 						}
 						
 						if (expandRight != 0) {
 							newEnd = newEnd + expandRight;
 						}
 						
 						if (moveBy != 0) {
 							newStart = newStart + moveBy;
 							newEnd = newStart + moveBy;
 						}
 						
 						GFFRecord rec = new SimpleGFFRecord(
 								r.getSeqName(),
 								r.getSource(),
 								r.getFeature(),
 								newStart,
 								newEnd,
 								r.getScore(),
 								r.getStrand(),
 								r.getFrame(),
 								r.getComment(),
 								r.getGroupAttributes());
 
 						gffw.recordLine(rec);
 						gffw.endDocument();
 						
 					}
 
 					public void startDocument(String arg0) {
 						// TODO Auto-generated method stub
 						
 					}
 				});
 		os.flush();
 	}	
 }
