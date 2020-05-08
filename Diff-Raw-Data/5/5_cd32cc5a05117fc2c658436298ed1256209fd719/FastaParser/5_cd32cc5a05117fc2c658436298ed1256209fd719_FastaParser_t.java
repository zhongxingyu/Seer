 package enderdom.eddie.bio.lists;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.log4j.Logger;
 
 import enderdom.eddie.tools.Tools_String;
 import enderdom.eddie.tools.bio.Tools_Fasta;
 
 /**
  * 
  * @author dominic
  *
  * Whilst I'm sure biojava's FastaReader class
  * does a much better job, I like the handler style 
  * implementation which allows the Task calling the 
  * Parser to potentially decide what to do with all the
  * data (ie just pick out sequences wanted, drop quality,
  * etc...)
  */
 public class FastaParser{
 
 	FastaHandler handler;
 	boolean fastq;
 	private boolean shorttitles;
 
 	public FastaParser(){
 		shorttitles =false;
 	}
 	public FastaParser(FastaHandler handler){
 		shorttitles = false;
 		this.handler = handler;
 	}
 	
 	/**
 	 * 
 	 * @param fasta file containing fasta
 	 * @return
 	 * @throws IOException
 	 */
 	public int parseFastq(InputStream fis) throws IOException{
 		int count = 0;
 		int linecount = 0;
 		int multi =0;
 		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
 		BufferedReader reader = new BufferedReader(in);
 		
 		String line = null;
 		StringBuilder sequence = new StringBuilder();
 		StringBuilder quality = new StringBuilder();
 		String title = "";
 		boolean qual = false;
 		while ((line = reader.readLine()) != null){
 			if(line.startsWith("@") && !qual){
 				if(sequence.length() > 0){
 					count++;				
 					handler.addAll(title, sequence.toString(), quality.toString());
 					sequence = new StringBuilder();
 					quality = new StringBuilder();
 				}
				if(this.shorttitles && line.indexOf(" ") > 1){
 					title = line.substring(1, line.indexOf(" "));
 				}
 				else{
 					title= line.substring(1, line.length());
 				}
 			}
 			else if(line.startsWith("+") && !qual){
 				if(!this.fastq){
 					this.fastq=true;handler.setFastq(this.fastq);
 				}
 				qual = true;
 			}
 			else{
 				if(!qual){
 					sequence = sequence.append(line.trim());
 				}
 				else{
 					quality = quality.append(line);
 					if(quality.length() >= sequence.length()){
 						qual = false;
 						if(quality.length() != sequence.length()){
 							Logger.getRootLogger().debug("Name: " + title);
 							Logger.getRootLogger().trace("Sequence: " + Tools_String.splitintolines(Tools_String.fastadefaultlength, sequence.toString()));
 							Logger.getRootLogger().trace("Quality: " + Tools_String.splitintolines(Tools_String.fastadefaultlength, quality.toString()));
 							Logger.getRootLogger().warn("Quality out of sync by " + (quality.length()-sequence.length()));
 							throw new IOException("Quality too long!");
 						}
 					}
 				}
 			}
 			if(multi==5000){
 				multi=0;
 				System.out.print("\rParsing Line: "+linecount);
 			}
 			multi++;
 			linecount++;
 		}
 		System.out.println("\rParsing Line: "+linecount);
 		if(sequence.length() > 0){
 			count++;
 			handler.addAll(title, sequence.toString(), quality.toString());
 		}
 		return count;
 	}
 	
 	/**
 	 * 
 	 * @param fasta File containing fasta
 	 * @param qual boolean if the file is actually a quality file, 
 	 * this will then parse it as fasta but convert to Fastq style 
 	 * string and add to the handler through the FastaHandler.addQuality()
 	 * method
 	 * @return Number of individual sequences in the fasta
 	 * @throws IOException
 	 */
 	public int parseFasta(InputStream fis, boolean qual)  throws IOException{
 		int count = 0;
 		int linecount=0;
 		int multi =0;
 
 		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
 		BufferedReader reader = new BufferedReader(in);
 		
 		String line = null;
 		StringBuilder sequence = new StringBuilder();
 		String title = "";
 		while ((line = reader.readLine()) != null){
 			if(line.startsWith(">")){
 				if(sequence.length() > 0){
 					if(!qual)handler.addSequence(title, sequence.toString());
 					else handler.addQuality(title, Tools_Fasta.Qual2Fastq(sequence.toString()));
 					sequence = new StringBuilder();
 					count++;
 				}
				if(this.shorttitles && line.indexOf(" ") > 1){
 					title = line.substring(1, line.indexOf(" "));
 				}
 				else{
 					title= line.substring(1);
 				}
 			}
 			else{
 				if(!qual)sequence = sequence.append(line.trim());
 				else sequence = sequence.append(line + " "); /*Space is important as 
 				the quality string is broken into an int array based on spaces.
 				Without adding the space the last and first numbers on each line are
 				parsed as a single number.
 				*/
 			}
 			if(multi==5000){
 				multi=0;
 				System.out.print("\rParsing Line: "+linecount);
 			}
 			multi++;
 			linecount++;
 		}
 		System.out.println("\rParsing Line: "+linecount);
 		if(sequence.length() > 0){
 			if(!qual)handler.addSequence(title, sequence.toString());
 			else handler.addQuality(title, Tools_Fasta.Qual2Fastq(sequence.toString()));
 			count++;
 		}
 		return count;
 	}
 
 	public int parseFastq(File fasta) throws FileNotFoundException, IOException{
 		return parseFastq(new FileInputStream(fasta));
 	}
 	
 	public int parseFasta(File fasta, boolean qual) throws FileNotFoundException, IOException{
 		return parseFasta(new FileInputStream(fasta), qual);
 	}
 	
 	public int parseFasta(File fasta)  throws IOException{
 		return parseFasta(fasta, false);
 	}
 	
 	public int parseQual(InputStream fasta)  throws IOException{
 		if(handler.isFastq())handler.setFastq(false);
 		return parseFasta(fasta, true);
 	}
 	
 	public int parseQual(File fasta)  throws IOException{
 		if(handler.isFastq())handler.setFastq(false);
 		return parseFasta(fasta, true);
 	}
 	
 	public boolean isShorttitles() {
 		return shorttitles;
 	}
 	public void setShorttitles(boolean shorttitles) {
 		this.shorttitles = shorttitles;
 	}
 	
 	
 }
