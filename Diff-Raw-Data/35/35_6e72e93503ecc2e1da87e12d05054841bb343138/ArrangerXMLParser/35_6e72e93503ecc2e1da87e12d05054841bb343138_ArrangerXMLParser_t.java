 package logic;
 
 import music.*;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import java.io.*;
 import java.net.URL;
 import org.dom4j.*;
 import org.dom4j.io.*;
 
 public class ArrangerXMLParser {
 	SAXReader _reader;
 	Editor _editor;
 
 	public ArrangerXMLParser(Editor editor) {
 		_editor = editor;
 		_reader = new SAXReader();
 	}
 
 	public void parse(String fileName) throws Exception {
 		Document doc = _reader.read(new FileReader(fileName));
 
 		Element root = doc.getRootElement();
 		_editor.clearScore();									// make new piece
 
 		Element elemStaffs = root.element("staffs");
 		parseStaffs(elemStaffs);
 	}
 
 	private void parseStaffs(Element elemStaffs) {
 		List<Element> elemStaffList = elemStaffs.elements("staff");
 		for (Element elemStaff : elemStaffList) {
 			parseStaff(elemStaff);								// parse staff data
 		}
 	}
 
 	private void parseStaff(Element elemStaff) {
 		Staff staff = new Staff();
 		_editor.insertStaff(staff);								// insert new staff
 
		Element elemMeasures = elemStaff.element("measures");	// parse measure list
		parseMeasures(elemMeasures);
 	}
 
 	private void parseMeasures(Element elemMeasures) {
 		List<Element> elemMeasureList = elemMeasures.elements("measure");
 		for (Element elemMeasure : elemMeasureList) {
 			parseMeasure(elemMeasure);							// parse measure data
 		}
 	}
 
 	private void parseMeasure(Element elemMeasure) {
 		Measure measure = new Measure();
	_editor.insertMeasure(measure);								// insert new measure
 
	Element elemKeySignatures = elemMeasure.element("keySignatures");
	Element elemTimeSignatures = elemMeasure.element("timeSignatures");
	Element elemClefs = elemMeasure.element("clefs");
	Element elemChordSymbols = elemMeasure.element("chordSymbols");
	Element elemVoices = elemMeasure.element("voices");

	parseKeySignatures(elemKeySignatures);						// parse keysig list
	parseTimeSignatures(elemTimeSignatures);					// parse timesig list
	parseClefs(elemClefs);										// parse clef list
	parseChordSymbols(elemChordSymbols);						// parse chordsymbol list
	parseVoices(elemVoices);									// parse voice list
 	}
 
 
 	private void parseKeySignatures(Element elemKeySigs) {
 		List<Element> elemKeySigList = elemKeySigs.elements("keySignature");
 		for (Element keySig : elemKeySigList) {
 			parseKeySignature(keySig);							// parse keysig data
 		}
 	}
 	private void parseKeySignature(Element elemKeySig) {
 		Rational dur = parseRationalAttribute(elemKeySig, "duration");
 		int accidentals = parseIntAttribute(elemKeySig, "accidentals");
 		boolean isMajor = parseBooleanAttribute(elemKeySig, "isMajor");
 		KeySignature keySig = new KeySignature(dur, accidentals, isMajor);
 		_editor.insertKeySig(keySig);							// insert keysig
 	}
 
 	private void parseTimeSignatures(Element elemTimeSigs) {
 		List<Element> elemTimeSigList = elemTimeSigs.elements("timeSignature");
 		for (Element timeSig : elemTimeSigList) {
 			parseTimeSignature(timeSig);						// parse timesig data
 		}
 	}
 
 	private void parseTimeSignature(Element elemTimeSig) {
 		Rational dur = parseRationalAttribute(elemTimeSig, "duration");
 		int numer = parseIntAttribute(elemTimeSig, "numerator");
 		int denom = parseIntAttribute(elemTimeSig, "denominator");
 		TimeSignature timeSig = new TimeSignature(dur, numer, denom);
 		_editor.insertTimeSig(timeSig);
 	}
 
 	private void parseChordSymbols(Element elemChordSymbols) {
 		List<Element> elemChordSymbolList = elemChordSymbols.elements("chordSymbol");
 		for (Element elemChordSymbol : elemChordSymbolList) {
 			parseChordSymbol(elemChordSymbol);					// parse chordsymbol data
 		}
 	}
 
 	private void parseChordSymbol(Element elemChordSymbol) {
 		Rational dur = parseRationalAttribute(elemChordSymbol, "duration");
 		int scaleDegree = parseIntAttribute(elemChordSymbol, "scaleDegree");
 		ChordType chordType = getChordType(parseStringAttribute(elemChordSymbol, "chordType"));
 		ChordSymbol chordSymbol = new ChordSymbol(dur, scaleDegree, chordType);
 		_editor.insertChordSymbol(chordSymbol);
 	}
 
 	private void parseClefs(Element e) {
 		List<Element> elemClefs = e.elements("clef");
 		for (Element clef : elemClefs) {
 			parseClef(clef);									// parse clef data
 		}
 	}
 
 	private void parseClef(Element elemClef) {
 		Rational dur = parseRationalAttribute(elemClef, "duration");
 		ClefName type = getClefName(parseStringAttribute(elemClef, "type"));
 		int centLine = parseIntAttribute(elemClef, "center_line");
 		Clef clef = new Clef(dur, type, centLine);
 		_editor.insertClef(clef);
 	}
 	
 	private void parseVoices(Element elemVoices) {
 		List<Element> elemVoiceList = elemVoices.elements("voice");
 		for (Element elemVoice : elemVoiceList) {
 			parseVoice(elemVoice);								// parse voice list
 		}
 	}
 
 	private void parseVoice(Element elemVoice) {
 		Voice voice = new Voice();
 		_editor.insertVoice(voice);								// insert voice
 
 		Element elemMNs = elemVoice.element("multinotes");
 		parseMultiNotes(elemMNs);								// parse multinote list
 	}
 
 	private void parseMultiNotes(Element elemMNs) {
 		List<Element> elemMNList = elemMNs.elements("multinote");
 		for (Element elemMN : elemMNList) {
 			parseMultiNote(elemMN);								// parse multinote data
 		}
 	}
 
 	private void parseMultiNote(Element elemMultiNote) {
 		Rational dur = parseRationalAttribute(elemMultiNote, "duration");
 		MultiNote multiNote = new MultiNote(dur);
 		_editor.insertMultiNote(multiNote);						// insert multinote
 
 		Element elemPitches = elemMultiNote.element("pitches");
 		parsePitches(elemPitches);								// parse pitch list
 	}
 
 	private void parsePitches(Element elemPitches) {
 		List<Element> elemPitchList = elemPitches.elements("pitch");
 		for (Element elemPitch : elemPitchList) {
 			parsePitch(elemPitch);								// parse pitch data
 		}
 	}
 
 	private void parsePitch(Element elemPitch) {
 		NoteLetter letter = getNoteLetter(parseStringAttribute(elemPitch, "key"));
 		int octave = parseIntAttribute(elemPitch, "octave");
 		Accidental accid = getAccidental(parseStringAttribute(elemPitch, "accidental"));
 		boolean isTied = false; 								// TODO: THIS NEEDS TO BE CHANGED
 
 		Pitch pitch = new Pitch(letter, octave, accid, isTied);
 		_editor.insertPitch(pitch);		// insert pitch
 	}
 
 	private NoteLetter getNoteLetter(String letter) {
 		return NoteLetter.valueOf(letter);
 	}
 
 	private Accidental getAccidental(String letter) {
 		return Accidental.valueOf(letter);
 	}
 
 	private ClefName getClefName(String type){
 		return ClefName.valueOf(type);
 	}
 
 	private ChordType getChordType(String type){
 		return ChordType.valueOf(type);
 	}
 	
 	private Rational parseRationalAttribute(Element e, String attr) {
		String[] strings = e.attribute(attr).getValue().split("/");
 		int numerator = Integer.parseInt(strings[0]);
 		int denominator = Integer.parseInt(strings[1]);
 		return new Rational(numerator, denominator);
 	}
 
 	private int parseIntAttribute(Element e, String attr) {
 		return Integer.parseInt(e.attribute(attr).getValue());
 	}
 
 	private boolean parseBooleanAttribute(Element e, String attr) {
 		return e.attribute(attr).getValue().equals("true");
 	}
 
 	private String parseStringAttribute(Element e, String attr) {
 		return e.attribute(attr).getValue();
 	}
 }
