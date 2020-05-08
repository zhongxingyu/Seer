 package ch.epfl.bbcf.parser;
 
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import ch.epfl.bbcf.exception.ParsingException;
 import ch.epfl.bbcf.feature.BEDFeature;
 import ch.epfl.bbcf.feature.Track;
 
 
 public class BEDParser extends Parser{
 
 	private Track cur_track;
 	/**
 	 * pattern matching the track's attributes
 	 */
 	private static final Pattern trackAttributesPattern = 
 		Pattern.compile("(\\w+=\\w+)|(\\w+=\".+\")");
 	/**
 	 * if the track parameters are finished to read
 	 */
 	private boolean trackParametersRead;
 
 	public BEDParser(Processing type) {
 		super(type);
 		trackParametersRead=false;
 	}
 	
 	@Override
 	protected void processLine(String line,Handler handler) throws ParsingException {
 		if(line.startsWith("track") || !trackParametersRead){
 			if(line.startsWith("track")){
 				cur_track = new Track();
 			}
 			trackParametersRead=false;
 			Matcher m = trackAttributesPattern.matcher(line);
 			if(m.find()){
 				String[]tab = m.group().split("=");
 				cur_track.addAttribute(tab[0],tab[1]);
 				while(m.find()){
 					tab = m.group().split("=");
 					cur_track.addAttribute(tab[0],tab[1]);
 				}
 			} else {
 				trackParametersRead = true;
 				newTrack(handler, cur_track);
 				processLine(line,handler);
 			}
 		} else {
 			Float score = null;
 			Integer strand = null;
 			String name = null;
 			String itemRgb=null;
 			String blockStarts=null;
 			String blockCount=null;
 			String blockSizes=null;
 			int start,end,thickStart = 0,thickEnd = 0;
 			String chromosome;
 			String[] chr_start_end_name_score_strand= line.split("\\s");
 			switch(chr_start_end_name_score_strand.length){
 			case 12:blockStarts = chr_start_end_name_score_strand[11];
 			case 11:blockSizes = chr_start_end_name_score_strand[10];
 			case 10:blockCount  = chr_start_end_name_score_strand[9];
 			case 9:itemRgb = chr_start_end_name_score_strand[8];
 			case 8:thickEnd = getInt(chr_start_end_name_score_strand[7]);
 			case 7:thickStart = getInt(chr_start_end_name_score_strand[6]);
 			case 6:strand = getStrand(chr_start_end_name_score_strand[5]);
 			case 5:score = getScore(chr_start_end_name_score_strand[4]);
 			case 4:name = chr_start_end_name_score_strand[3];
 			case 3:
 				chromosome = (chr_start_end_name_score_strand[0]);
 				start = getInt(chr_start_end_name_score_strand[1]);
 				end = getInt(chr_start_end_name_score_strand[2]);
 				break;
			default: throw new ParsingException("The entry doesn't have the required number of fields " +
					"(at least 3: chromosome, start, end separated by spaces or tabs): ", lineNb);
 			}
 			BEDFeature current = new BEDFeature(chromosome,start,end,name,strand,score,thickStart,thickEnd,itemRgb,blockCount,blockSizes,blockStarts);
 			newFeature(handler, cur_track, current);
 		}
 	}
 }
