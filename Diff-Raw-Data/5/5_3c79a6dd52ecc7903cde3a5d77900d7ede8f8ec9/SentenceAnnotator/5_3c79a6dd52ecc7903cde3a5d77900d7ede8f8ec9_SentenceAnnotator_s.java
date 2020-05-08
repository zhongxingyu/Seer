 import org.apache.uima.analysis_engine.ResultSpecification;
 import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
 import org.apache.uima.analysis_engine.annotator.JTextAnnotator_ImplBase;
 import org.apache.uima.jcas.JCas;
 import com.aliasi.chunk.Chunker;
 import com.aliasi.chunk.Chunking;
 import com.aliasi.util.AbstractExternalizable;
 import java.io.File;
 import java.io.IOException;
 
 /*Sentence gene annotator
  * Extracts sentence
  * Finds the gene name, location and adds it to the typesystem annotation*/
 @SuppressWarnings("deprecation")
 public class SentenceAnnotator extends JTextAnnotator_ImplBase {
 	
 	public void process(JCas arg0, ResultSpecification arg1) throws AnnotatorProcessException	
 	{
 		System.out.println("Analysis engine 1");
 		String docText = arg0.getDocumentText();
 		String[] lines=docText.split("\n");
 		
 		//Add LingPipe annotation
		File modelFile = new File("src/main/resources/ne-en-bio-genetag.HmmChunker");
 		System.out.println("Reading chunker from file=" + modelFile);
 	    Chunker chunker = null;
 		try {
 			chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		for(int i=0;i<lines.length;i++)
 		{
 			//Create a sentence annotator type
 			//curLine[0] contains sentence Id, curLine[1] contains the sentence without the ID
 			String [] curLine=lines[i].split(" ",2);
 			String sentenceId=curLine[0];
 			int lenSentenceId=sentenceId.length();
 			Chunking chunking = chunker.chunk(curLine[1]);
 			
 			String chunkedText = chunking.toString();
 			String[] tagsArr=chunkedText.split(": \\[");
 			//Find the tags
 			String tags=tagsArr[1];
 			tags=tags.replaceAll("[\\[\\],]","");
 			if(tags.length()>0)
 			{
 				String []locsTags=tags.split(" ");
 				for(int j=0;j<locsTags.length;j++)
 				{
 					String thisLoc=locsTags[j].split(":")[0];
 					if(thisLoc.length()>1)
 					{
 					Sentence annotation=new Sentence(arg0);
 					//Find the gene name under consideration
 					String []locs=thisLoc.split("-");
 					int startLoc=Integer.parseInt(locs[0].trim()) + lenSentenceId;
 					int endLoc=Integer.parseInt(locs[1]) + lenSentenceId + 1;
 					annotation.setGeneName(lines[i].substring(startLoc,endLoc).trim());
 					annotation.setSentenceID(sentenceId);
 					annotation.setSentenceString(lines[i]);
 					//Part where we find the number of spaces between start of string and locs[0]
 					int begin=Integer.parseInt(locs[0].trim());
 					String lineUnderCons=curLine[1];
 					int cnt=0;
 					for(int cc=0;cc<begin;cc++)
 					{
 						if(lineUnderCons.charAt(cc)==' ')
 							cnt=cnt+1;
 					}
 					int curLoc=Integer.parseInt(locs[0].trim())-cnt;
 					annotation.setGeneLoc(Integer.toString(curLoc));
 					annotation.addToIndexes();
 					}
 				}
 			}
 		}
 		
 	}
 
 	
 }
