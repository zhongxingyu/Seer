 package logic;
 
 import music.*;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import java.io.*;
 import java.net.URL;
 import org.dom4j.*;
 import org.dom4j.io.*;
 
 public class ArrangerXMLWriter {
 	public ArrangerXMLWriter() {}
 	
 	public void write(Piece piece, String fileName) throws Exception {
 		Document document = DocumentHelper.createDocument();
 		
 		Element root = DocumentHelper.createElement("piece");
 		document.setRootElement(root);
 		
 		List<Staff> staffs = piece.getStaffs();
 		writeStaffs(staffs, root);
 		
 		//write to file
 		XMLWriter writer = new XMLWriter(new FileWriter(fileName), OutputFormat.createPrettyPrint());
 		writer.write(document);
 		writer.close();
 	}
 	
 	private void writeStaffs(List<Staff> staffs, Element root) {
 		Element elemStaffs = DocumentHelper.createElement("staffs");
 		root.add(elemStaffs);
 		
 		for (Staff staff : staffs) {
 			Element elemStaff = DocumentHelper.createElement("staff");
 			elemStaffs.add(elemStaff);
 			
 			List<Measure> measures = staff.getMeasures();
 			writeMeasures(measures, elemStaff);
 		}
 	}
 	
 	private void writeMeasures(List<Measure> measures, Element elemStaff) {
 		Element elemMeasures = DocumentHelper.createElement("measures");
 		elemStaff.add(elemMeasures);
 		
 		for (Measure measure : measures) {
 			Element elemMeasure = DocumentHelper.createElement("measure");
 			elemMeasures.add(elemMeasure);
 
 			List<TimeSignature> timeSigs = measure.getTimeSignatures();
 			List<KeySignature> keySigs = measure.getKeySignatures();
 			List<Clef> clefs = measure.getClefs();
 			List<ChordSymbol> chordSymbols = measure.getChordSymbols();
 			List<Voice> voices = measure.getVoices();
 			
 			writeTimeSigs(timeSigs, elemMeasure);
 			writeKeySigs(keySigs, elemMeasure);
 			writeClefs(clefs, elemMeasure);
 			writeChordSymbols(chordSymbols, elemMeasure);
 			writeVoices(voices, elemMeasure);
 		}
 	}
 	
 	private void writeTimeSigs(List<TimeSignature> timeSigs, Element elemMeasure) {
 		Element elemTimeSigs = DocumentHelper.createElement("timeSignatures");
 		elemMeasure.add(elemTimeSigs);
 
 		for (TimeSignature timeSig : timeSigs) {
 			Element elemTimeSig = DocumentHelper.createElement("timeSignature");
 			elemTimeSigs.add(elemTimeSig);
 
 			Rational duration = timeSig.getDuration();
 			elemTimeSig.addAttribute("duration", duration.toString());
 			elemTimeSig.addAttribute("numerator", "" + timeSig.getNumerator());
 			elemTimeSig.addAttribute("denominator", "" + timeSig.getDenominator());
 		}
 	}
 	private void writeKeySigs(List<KeySignature> keySigs, Element elemMeasure) {
 		Element elemKeySigs = DocumentHelper.createElement("keySignatures");
 		elemMeasure.add(elemKeySigs);
 		
 		for (KeySignature keySig : keySigs) {
 			Element elemKeySig = DocumentHelper.createElement("keySignature");
 			elemKeySigs.add(elemKeySig);
 
 			Rational duration = keySig.getDuration();
 			elemKeySig.addAttribute("duration", duration.toString());
 			elemKeySig.addAttribute("accidentals", "" + keySig.getAccidentalNumber());
 			elemKeySig.addAttribute("isMajor", "" + keySig.getIsMajor());
 		}
 	}
 	
 	private void writeChordSymbols(List<ChordSymbol> chordSymbols, Element elemMeasure) {
 		Element elemChordSymbols = DocumentHelper.createElement("chordSymbols");
 		elemMeasure.add(elemChordSymbols);
 		
 		for (ChordSymbol chordSymbol : chordSymbols) {
 			Element elemChordSymbol = DocumentHelper.createElement("chordSymbol");
 			elemChordSymbols.add(elemChordSymbol);
 
 			Rational duration = chordSymbol.getDuration();
 			elemChordSymbol.addAttribute("duration", duration.toString());
 			elemChordSymbol.addAttribute("scaleDegree", "" + chordSymbol.getScaleDegree());
 			elemChordSymbol.addAttribute("chordType", "" + chordSymbol.getChordType());
 		}
 	}
 	
 	private void writeClefs(List<Clef> clefs, Element elemMeasure) {
 		Element elemClefs = DocumentHelper.createElement("clefs");
 		elemMeasure.add(elemClefs);
 		
 		for (Clef clef : clefs) {
 			Element elemClef = DocumentHelper.createElement("clef");
 			elemClefs.add(elemClef);
 
 			Rational duration = clef.getDuration();
 			elemClef.addAttribute("duration", duration.toString());
 			elemClef.addAttribute("type", "" + clef.getClefName());
 			elemClef.addAttribute("center_line", "" + clef.getCenterLine());
 		}
 	}
 	
 	private void writeVoices(List<Voice> voices, Element elemMeasure) {
 		Element elemVoices = DocumentHelper.createElement("voices");
 		elemMeasure.add(elemVoices);
 		
 		for (Voice voice : voices) {
 			Element elemVoice = DocumentHelper.createElement("voice");
 			elemVoices.add(elemVoice);
 			
 			List<MultiNote> multinotes = voice.getMultiNotes();
 			writeMultiNotes(multinotes, elemVoice);
 		}
 	}
 	
 	private void writeMultiNotes(List<MultiNote> multinotes, Element elemVoice) {
 		Element elemMultinotes = DocumentHelper.createElement("multinotes");
 		elemVoice.add(elemMultinotes);
 		
 		for (MultiNote multinote : multinotes) {
 			Element elemMultinote = DocumentHelper.createElement("multinote");
 			elemMultinotes.add(elemMultinote);
 
 			Rational duration = multinote.getDuration();
 			elemMultinote.addAttribute("duration", duration.toString());
 			List<Pitch> pitches = multinote.getPitches();
 			writePitches(pitches, elemMultinote);
 		}
 	}
 	
 	private void writePitches(List<Pitch> pitches, Element elemMultinote) {
 		Element elemPitches = DocumentHelper.createElement("pitches");
 		elemMultinote.add(elemPitches);
 		
 		for (Pitch pitch : pitches) {
 			Element elemPitch = DocumentHelper.createElement("pitch");
 			elemPitches.add(elemPitch);
 			
 			elemPitch.addAttribute("octave", "" + pitch.getOctave());
 			elemPitch.addAttribute("key", "" + pitch.getNoteLetter());
 			elemPitch.addAttribute("accidental", "" + pitch.getAccidental());
 		}
 	}
 	
 	public static void main(String[] args) {
 		ArrangerXMLWriter writer = new ArrangerXMLWriter();
 		Editor editor = new Editor();
 		ArrangerXMLParser parser = new ArrangerXMLParser(editor);
		Piece p = new Piece();
 		editor.setPiece(p);
 		
 		try {
			parser.parse("test/testWrite2.xml");
			writer.write(editor.getPiece(), "test/testWrite.xml");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
