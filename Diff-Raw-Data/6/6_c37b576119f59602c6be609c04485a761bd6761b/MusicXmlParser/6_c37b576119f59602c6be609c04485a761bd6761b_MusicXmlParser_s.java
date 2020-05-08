 package crescendo.base.parsing;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import crescendo.base.song.Creator;
 import crescendo.base.song.Note;
 import crescendo.base.song.SongModel;
 import crescendo.base.song.TimeSignature;
 import crescendo.base.song.Track;
 
 public class MusicXmlParser implements SongFileParser{
 	private String movementTitle;
 	private double movementNumber;
 	private String workTitle;
 	private int workNumber;
 	private List<Creator> creators;
 	private String copyright;
 	private Map<String,Track> tracks;
 	private int bpm;
 	private TimeSignature timeSignature;
 	
 	private int currentMeasure = -1;	//Counts the current measure, useful for determining if directives are global or not
 	private int currentDynamic = 90;	//The midi dynamic for the notes currently being parsed. MusicXML default is 90 (about forte)
 	private int currentDivision = -1;	//The current count of number of divisions per quarter note
 	private Track currentTrack;
 	
 	
 	//All of the possible named notes in midi
 	private static String[] midiNoteArray = new String[]{"c","db","d","eb","f","gb","g","ab","a","bb","b"};
 	
 	/**
 	 * Constructs a MusicXmlParser
 	 * initializes global variables.
 	 */
 	public MusicXmlParser(){
 		currentTrack = null;
 		tracks = new HashMap<String,Track>();
 		creators = new ArrayList<Creator>();
 		bpm = -1;
 		timeSignature = null;
 	}
 	
 	/**
 	 * Basic function to parse a file into a song function.
 	 * @param file - MusicXML file to parse
 	 * @return SongModel - model which contains the data contained in the MusicXML file
 	 */
 	@Override
 	public SongModel parse(File file) throws IOException {
 		boolean validating = false; //Semaphore for the DocumentBuilderFactory
 		DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
 		factory.setValidating(validating);
 		DocumentBuilder builder;
 		Document document = null;
 		try {
 			builder = factory.newDocumentBuilder();
 			document = builder.parse(file);
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		}
 		for(int i=0;i<document.getChildNodes().getLength();i++)
 		{
 			parseNode(document.getChildNodes().item(i));
 		}		
		return new SongModel(new ArrayList<Track>(tracks.values()), workTitle, creators, "", "", copyright, bpm, timeSignature);
 	}
 	
 	/**
 	 * Catch-all helper function for recursively parsing nodes in the MusicXML DOM.
 	 * Currently parses:
 	 * 	* 	movement number
 	 * 	*	creators
 	 *  *	copyright information
 	 *  *	time signature
 	 *  *	tempo
 	 *  *	dynamic
 	 * @param node - XML DOM node to parse
 	 */
 	private void parseNode(Node node){
 		String nodeName = node.getNodeName();
 		boolean parseChildren=true;	//determines if the children of the current node should be recursed upon
 
 		if(nodeName.equals("score-part")){
 			tracks.put(node.getAttributes().getNamedItem("id").getNodeValue(), parseTrackMeta(node));
 			parseChildren = false;
 		}else if(nodeName.equals("work")){
 			parseWork(node);
 			parseChildren = false;
 		}else if(nodeName.equals("movement-number")){
 			movementNumber = Integer.parseInt(node.getTextContent());
 		}else if(nodeName.equals("movement-title")){
 			movementTitle = node.getTextContent();
 		}else if(nodeName.equals("creator")){
 			creators.add(new Creator(node.getTextContent(), node.getAttributes().getNamedItem("type").getNodeValue()));
 		}else if(nodeName.equals("rights")){
 			copyright = node.getTextContent();
 		}else if(nodeName.equals("part")){
 			currentTrack = tracks.get(node.getAttributes().getNamedItem("id").getNodeValue());
 		}else if(nodeName.equals("measure")){
 			currentMeasure = Integer.parseInt(node.getAttributes().getNamedItem("number").getNodeValue());
 		}else if(nodeName.equals("divisions")){
 			currentDivision = Integer.parseInt(node.getTextContent());
 		}else if(nodeName.equals("key")){
 			//TODO handle key signature
 			if(currentMeasure==1){
 				//Song-wide key signature
 			}else{
 				//non-playable note with a modulation modifier in currentTrack
 			}
 		}else if(nodeName.equals("time")){
 			int beatsPerMeasure = -1;
 			int beatNote = -1;
 			for(int i=0;i<node.getChildNodes().getLength();i++){
 				if(node.getChildNodes().item(i).getNodeName().equals("beats")){
 					beatsPerMeasure = Integer.parseInt(node.getChildNodes().item(i).getTextContent());
 				}else if(node.getChildNodes().item(i).getNodeName().equals("beat-type")){
 					beatNote = Integer.parseInt(node.getChildNodes().item(i).getTextContent());
 				}
 			}
 			TimeSignature ts = new TimeSignature(beatsPerMeasure,beatNote);
 			if(currentMeasure==1){
 				timeSignature = ts;
 			}else{
 				//TODO non-playable note with a time signature modifier in the currentTrack
 			}
 			parseChildren = false;
 		}else if(nodeName.equals("sound")){
 			if(node.getAttributes().getNamedItem("tempo")!=null){
 				if(currentMeasure==1){
 					bpm = Integer.parseInt(node.getAttributes().getNamedItem("tempo").getNodeValue());
 				}else{
 					//TODO non-playable note with a tempo change modifier in the current track
 				}
 			}
 			if(node.getAttributes().getNamedItem("dynamics")!=null){
 				currentDynamic = Integer.parseInt(node.getAttributes().getNamedItem("dynamics").getNodeValue());
 			}
 		}else if(nodeName.equals("note")){
 			currentTrack.addNote(parseNote(node,false,0,0));
 		}
 		
 		
 		if(node.hasChildNodes()&&parseChildren){
 			for(int i=0;i<node.getChildNodes().getLength();i++)
 			{
 				parseNode(node.getChildNodes().item(i));
 			}
 		}
 	}
 	
 	/**
 	 * Helper function for parsing out track information
 	 * Currently only supports track name and midi instrument. It currently uses the last midi instrument defined for a given track.
 	 * @param node - score-part node
 	 * @return - a fully constructed Track object
 	 */
 	private Track parseTrackMeta(Node node){
 		String trackName = "";
 		int instrument = 1;
 		if(!node.getNodeName().equals("score-part")){
 			throw new IllegalArgumentException("Node must be of type score-part");
 		}
 		if(node.hasChildNodes()){
 			for(int i=0;i<node.getChildNodes().getLength();i++)
 			{
 				Node cNode = node.getChildNodes().item(i);
 				if(cNode.getNodeName().equals("midi-instrument")){
 					for(int j=0;j<cNode.getChildNodes().getLength();j++){
 						if(cNode.getChildNodes().item(j).getNodeName().equals("midi-channel")){
 							instrument = Integer.parseInt(cNode.getChildNodes().item(j).getTextContent());
 						}
 					}
 				}
 				else if(cNode.getNodeName().equals("part-name")){
 					trackName = cNode.getTextContent();
 				}
 			}
 		}
 		return new Track(trackName,instrument);
 	}
 
 	private Note parseNote(Node node, boolean isRest, int duration, int pitch){
 		Note retValue = null;
 		if(node.getNodeName().equals("pitch")){
 			char note = ' ';
 			int alter = 0;
 			int octave = 0;
 			for(int i=0;i<node.getChildNodes().getLength();i++)
 			{
 				if(node.getChildNodes().item(i).getNodeName().equals("step")){
 					note = node.getChildNodes().item(i).getTextContent().toLowerCase().charAt(0);
 				}else if(node.getChildNodes().item(i).getNodeName().equals("alter")){
 					alter = Integer.parseInt(node.getChildNodes().item(i).getTextContent());
 				}else if(node.getChildNodes().item(i).getNodeName().equals("octave")){
 					octave = Integer.parseInt(node.getChildNodes().item(i).getTextContent());
 				}
 			}
 			pitch = noteToMidi(note,alter,octave);
 		}else if(node.getNodeName().equals("rest")){
 			isRest = true;
 		}else if(node.getNodeName().equals("duration")){
 			duration =  Integer.parseInt(node.getTextContent());
 		}else if(node.getNodeName().equals("voice")){
 			if(Integer.parseInt(node.getTextContent())!=currentTrack.getVoice()){
 				//TODO non-playable note with a voice change modifier in the current track
 			}
 		}
 		
 		
 		
 		if(!node.hasChildNodes() && node.getNextSibling()==null){
 			retValue = new Note((isRest)?1:pitch , (double)duration/(double)currentDivision , (isRest)?0:currentDynamic , currentTrack);
 		}else if(node.getNodeName().equals("note")){
 			retValue = parseNote(node.getFirstChild(),isRest,duration,pitch);
 		}else{			
 			retValue = parseNote(node.getNextSibling(),isRest,duration,pitch);
 		}
 		return retValue;
 	}
 	
 	private void parseWork(Node node){
 		if(node.hasChildNodes()){
 			for(int i=0;i<node.getChildNodes().getLength();i++)
 			{
 				Node cNode = node.getChildNodes().item(i);
 				if(cNode.getNodeName().equals("work-title")){
 					workTitle = cNode.getTextContent();
 				}else if(cNode.getNodeName().equals("work-number")){
 					workNumber = Integer.parseInt(cNode.getTextContent());
 				}
 			}
 		}
 	}
 
 	private static int noteToMidi(char note, int alter, int octave){
 		String noteValue = String.valueOf(note);
 		int location = -1;
 		for(int i=0;i<midiNoteArray.length;i++){
 			if(noteValue.equals(midiNoteArray[i]))
 				location = i;
 		}
 		location+=alter;
 		if(location<0)
 			location+=12;
 		location+=(12)*(octave+1);
 		return location;
 	}
 }
