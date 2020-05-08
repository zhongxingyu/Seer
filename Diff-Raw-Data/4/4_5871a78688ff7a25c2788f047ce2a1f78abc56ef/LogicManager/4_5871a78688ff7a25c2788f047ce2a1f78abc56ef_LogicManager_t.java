 package logic;
 
 import music.*;
 import instructions.*;
 
 import java.util.*;
 import java.awt.print.*;
 import java.awt.Graphics;
 import java.awt.Image;
 import javax.print.attribute.*;
 
 /*
  * LogicManager is the highest level class in the logic package, which is instantiated
  * the arranger package.  Primarily, it receives Instructions from the arranger and gui
  * packages, interprets those Instructions, and delegates the execution of those
  * Instructions to the classes contained within the package.
  */
 
 public class LogicManager implements Printable {
 	Piece _piece;
 	Editor _editor;
 	ArrangerXMLParser _arrangerXMLParser;
 	ArrangerXMLWriter _arrangerXMLWriter;
 
 	MidiAPI _api;
 
 	Image _toPrint;
 
 	public LogicManager(Piece piece) {
 		_piece = piece;
 		_editor = makeEditor();
 		_arrangerXMLParser = makeArrangerXMLParser();
 		_arrangerXMLWriter = makeArrangerXMLWriter();
 
 		_api = new MidiAPI();
 	}
 
 	public Editor getEditor() {
 		return _editor;
 	}
 
 	protected Editor makeEditor() {
 		Editor editor = new Editor();
 		editor.setPiece(_piece);
 		return editor;
 	}
 
 	protected ArrangerXMLParser makeArrangerXMLParser() {
 		return new ArrangerXMLParser(_editor);
 	}
 	protected ArrangerXMLWriter makeArrangerXMLWriter() {
 		return new ArrangerXMLWriter();
 	}
 
 	public void interpretInstrBlock(InstructionBlock instrBlock) {
 		List<Instruction> allInstructions = instrBlock.getInstructions();
 		for (Instruction instr : allInstructions) {
 			interpretInstr(instr);
 		}
 	}
 
 	/*
 	 * Finds the Instruction's class type and calls the cooresponding method to interpret
 	 * the Instruction, after casting it appropriately.
 	 */
 	private void interpretInstr(Instruction instr) {
 		System.out.println(instr);
 
 		if (instr instanceof FileInstruction){
 			FileInstruction fileInstr = (FileInstruction) instr;
 			interpretFileInstr(fileInstr);
 		}
 		else if (instr instanceof EditInstruction) {
 			EditInstruction editInstr = (EditInstruction) instr;
 			interpretEditInstr(editInstr);
 		}
 		else if (instr instanceof PlaybackInstruction) {
 			PlaybackInstruction playbackInstr = (PlaybackInstruction) instr;
 			interpretPlaybackInstr(playbackInstr);
 		}
 		else if (instr instanceof GenerateInstruction) {
 			GenerateInstruction genInstr = (GenerateInstruction) instr;
 			interpretGenerateInstr(genInstr);
 		}
 		else {
 			throw new RuntimeException("Instruction of unrecognized InstructionType");
 		}
 	}
 
 	private void interpretFileInstr(FileInstruction fileInstr) {
 		if (fileInstr instanceof FileInstructionIO) {
 			FileInstructionIO fileInstrIO = (FileInstructionIO) fileInstr;
 			interpretFileInstrIO(fileInstrIO);
 		}
 		else if (fileInstr instanceof FileInstructionNew) {
 			FileInstructionNew fileInstrNew = (FileInstructionNew) fileInstr;
 			interpretFileInstrNew(fileInstrNew);
 		}
 		else if (fileInstr instanceof FileInstructionPrint) {
 			FileInstructionPrint fileInstrPrint = (FileInstructionPrint) fileInstr;
 			interpretFileInstrPrint(fileInstrPrint);
 		}
 
 		// Since file instructions instantiate a new piece, rather than mutating the old one, the
 		// the new piece must be updated in Logic and passed back.
 		Piece piece = _editor.getPiece();
 		this._piece = piece;
 		// TODO: SEND NEW PIECE BACK TO MAIN, THEN TO GUI
 	}
 
 	public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
 		//FileInstructionPrint
 		g.drawImage(_toPrint, 0, 0, null);
 
 		return PAGE_EXISTS;
 	}
 	private void interpretFileInstrPrint(FileInstructionPrint fileInstrPrint) {
 		//fileInstrPrint
 		_toPrint = fileInstrPrint.getImage();
 		PrinterJob job = PrinterJob.getPrinterJob();
 		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
 		PageFormat pf = job.pageDialog(aset);
 		job.setPrintable(this, pf);
 		boolean ok = job.printDialog(aset);
 		if (ok) {
 			try {
 				job.print(aset);
 			}
 			catch (PrinterException ex) {
 				System.out.println("Print failed");
 			}
 		}
 
 	}
 	private void interpretFileInstrNew(FileInstructionNew fileInstrNew) {
 		System.out.println("start new file");
 
 		List<Clef> clefs = fileInstrNew.getClefs();
 		int numStaffs = clefs.size();
 		int numMeasures = fileInstrNew.getNumMeasures();
 		int timeSigNumer = fileInstrNew.getTimeSigNumer();
 		int timeSigDenom = fileInstrNew.getTimeSigDenom();
 		int accidentals = fileInstrNew.getAccidentals();
 		boolean isMajor = fileInstrNew.getIsMajor();
 
 		_editor.clearScore();
 
 		// loop through staffs
 		for (int stfnm = 0; stfnm < numStaffs; stfnm++) {
 			Staff staff = new Staff();
 			_editor.insertStaff(staff);
 
 			// loop through measures
 			for (int msrnm = 0; msrnm < numMeasures; msrnm++) {
 				Measure measure = new Measure();
 				_editor.insertMeasure(measure);
 
 				// Instantiate key signature, time signature
 				Rational duration = new Rational(timeSigNumer, timeSigDenom);
 				TimeSignature timeSig = new TimeSignature(duration, timeSigNumer, timeSigDenom);
 				KeySignature keySig = new KeySignature(duration, accidentals, isMajor);
 				_editor.insertKeySig(keySig);
 				_editor.insertTimeSig(timeSig);
 
 				// Instantiate clef
 				ClefName clefName = clefs.get(stfnm).getClefName();
 				int centerLine = clefs.get(stfnm).getCenterLine();
 				Clef clef = new Clef(duration, clefName, centerLine);
 				_editor.insertClef(clef);
 
 				// Instantiate single voice with a rest;
 				Voice voice = new Voice();
 				MultiNote rest = new MultiNote(duration);
 				_editor.insertVoice(voice);
 				_editor.insertMultiNote(rest);
 
 				// Add blank placeholder chordsymbols
 				ChordSymbol chordSymbol = new ChordSymbol(duration, new ScaleDegree(0, Accidental.NATURAL), ChordType.BLANK);
 				_editor.insertChordSymbol(chordSymbol);
 			}
 		}
 
 		System.out.println("created new piece");
 	}
 	private void interpretFileInstrIO(FileInstructionIO fileInstrIO) {
 		FileInstructionType fileInstrType = fileInstrIO.getType();
 		String fileName = fileInstrIO.getFileName();
 
 		switch (fileInstrType) {
 		case OPEN:
 			try {
 				_arrangerXMLParser.parse(fileName);
 			} catch (Exception e) {
 				System.out.println("Load failed");
 			}
 			break;
 		case SAVE:
 			try {
 				_arrangerXMLWriter.write(_piece, fileName);
 			} catch (Exception e) {
 				System.out.println("Save failed");
 			}
 			break;
 		}
 	}
 
 	private void interpretGenerateInstr(GenerateInstruction genInstr) {
 		GenerateInstructionType genType = genInstr.getType();
 
 		switch (genType) {
 		case CHORDS:
 
 			break;
 		case VOICES:
 
 			break;
 		}
 	}
 
 	private void interpretEditInstr(EditInstruction editInstr) {
 		EditType editType = editInstr.getElemType();
 
 		switch (editType) {
 		case STAFF:
 			editStaff(editInstr);
 			break;
 		case MEASURE:
 			editMeasure(editInstr);
 			break;
 		case CHORD_SYMBOL:
 			editChordSymbol(editInstr);
 			break;
 		case KEY_SIGNATURE:
 			editKeySignature(editInstr);
 			break;
 		case TIME_SIGNATURE:
 			editTimeSignature(editInstr);
 			break;
 		case CLEF:
 			editClef(editInstr);
 			break;
 		case VOICE:
 			editVoice(editInstr);
 			break;
 		case PITCH:
 			editPitch(editInstr);
 			break;
 		case MULTINOTE:
 			editMultiNote(editInstr);
 			break;
 		default:
 		}
 	}
 
 	private void editStaff(EditInstruction editInstr) {
 		InstructionIndex index = editInstr.getIndex();
 
 		int staffNumber = index.getStaffNumber();
 		ListIterator<Staff> iterator = _piece.getStaffs().listIterator(staffNumber);
 		_editor.setStaffIter(iterator);
 
 		EditInstructionType instrType = editInstr.getType();
 		switch (instrType) {
 		// TODO: THESE STAFF INSERTION/REMOVAL FUNCTIONS ARE DUMMIES, HAVE TO CHECK FOR STAFF LENGTH, MAKING KEY SIGNATURES/TIME SIGNATURES CONSISTENT, ETC.
 		// ALSO HAVE TO DECIDE WHAT KIND OF INFORMATION NEEDS TO BE PASSED WHEN CREATING A NEW STAFF.
 			case INSERT:
 				_editor.insertStaff(new Staff());
 				break;
 			case REMOVE:
 				_editor.removeStaff();
 				break;
 			case REPLACE:
 				_editor.replaceStaff(new Staff());
 				break;
 			default:
 				throw new RuntimeException("Instruction of unrecognized EditInstructionType");
 		}
 
 	}
 
 	private void editChordSymbol(EditInstruction editInstr) {
 		InstructionIndex index = editInstr.getIndex();
 
 		int staffNumber = index.getStaffNumber();
 		int measureNumber = index.getMeasureNumber();
 		Rational measureOffset = index.getMeasureOffset();
 		Measure measure = _piece.getStaffs().get(staffNumber).getMeasures().get(measureNumber);
 		List<ChordSymbol> chordSymbolList = measure.getChordSymbols();
 
 		// calculate iterator and offset
 		IteratorAndOffset iterAndOffset = calcIterAndOffset(chordSymbolList, measureOffset);
 		ListIterator<ChordSymbol> iter = (ListIterator<ChordSymbol>) iterAndOffset.getIter();
 		Rational offset = iterAndOffset.getOffset();
 
 		ChordSymbol chordSymbol;
 
 		// set the iterator in the editor
 		_editor.setChordSymbolIter(iter);
 
 		EditInstructionType instrType = editInstr.getType();
 		switch (instrType) {
 		// offset SHOULD be 0 for insertion and removal functions
 			case INSERT:
 				chordSymbol = (ChordSymbol) editInstr.getElement();
 				_editor.insertChordSymbol(chordSymbol);
 				break;
 			case REMOVE:
 				_editor.removeChordSymbol();
 				break;
 			case REPLACE:
 				// check if replacement note runs over end of measure
 				Rational measureLength = measure.getTimeSignatures().get(0).getMeasureDuration();
 				chordSymbol = (ChordSymbol) editInstr.getElement();
 				if (measureOffset.compareTo(measureLength) > 0) {
 					// Error, measureOffset is longer than the actual measure
 					return;
 				}
 				else {
 					_editor.replaceChordSymbol(chordSymbol);
 				}
 				break;
 			default:
 				throw new RuntimeException("Instruction of unrecognized EditInstructionType");
 		}
 	}
 
 	private void editMeasure(EditInstruction editInstr) {
 	}
 
 	private void editKeySignature(EditInstruction editInstr) {
 	}
 
 	private void editTimeSignature(EditInstruction editInstr) {
 	}
 
 	private void editClef(EditInstruction editInstr) {
 	}
 
 	private void editVoice(EditInstruction editInstr) {
 	}
 
 	private void editMultiNote(EditInstruction editInstr) {
 		InstructionIndex index = editInstr.getIndex();
 
 		int staffNumber = index.getStaffNumber();
 		int measureNumber = index.getMeasureNumber();
 		int voiceNumber = index.getVoiceNumber();
 		Rational measureOffset = index.getMeasureOffset();
 		Measure measure = _piece.getStaffs().get(staffNumber).getMeasures().get(measureNumber);
 		List<MultiNote> multiNoteList = measure.getVoices().get(voiceNumber).getMultiNotes();
 
 		// calculate iterator and offset
 		IteratorAndOffset iterAndOffset = calcIterAndOffset(multiNoteList, measureOffset);
 		ListIterator<MultiNote> iter = (ListIterator<MultiNote>) iterAndOffset.getIter();
 		Rational offset = iterAndOffset.getOffset();
 
 		MultiNote multiNote;
 
 		// set the iterator in the editor
 		_editor.setMultiNoteIter(iter);
 
 		EditInstructionType instrType = editInstr.getType();
 		switch (instrType) {
 		// offset SHOULD be 0 for insertion and removal functions
 			case INSERT:
 				multiNote = (MultiNote) editInstr.getElement();
 				_editor.insertMultiNote(multiNote);
 				break;
 			case REMOVE:
 				_editor.removeMultiNote();
 				break;
 			case REPLACE:
 				// check if replacement note runs over end of measure
 				Rational measureLength = measure.getTimeSignatures().get(0).getMeasureDuration();
 				Rational remainingSpace = measureLength.minus(measureOffset);
 				multiNote = (MultiNote) editInstr.getElement();
 				if (multiNote.getDuration().compareTo(remainingSpace) > 0) {
 					// if the replaced note is bigger than the remaining space in the measure
 					System.out.println("New note is too big");
 					return;
 				}
 				else {
 					_editor.replaceMultiNote(multiNote);
 				}
 				break;
 			default:
 				throw new RuntimeException("Instruction of unrecognized EditInstructionType");
 		}
 	}
 
 	private void editPitch(EditInstruction editInstr) {
 		InstructionIndex index = editInstr.getIndex();
 		
 		// get Measure
 		int staffNumber = index.getStaffNumber();
 		int measureNumber = index.getMeasureNumber();
 		int voiceNumber = index.getVoiceNumber();
 		int lineNumber = index.getLineNumber();
 		Rational measureOffset = index.getMeasureOffset();
 		Measure measure = _piece.getStaffs().get(staffNumber).getMeasures().get(measureNumber);
 		List<MultiNote> multiNoteList = measure.getVoices().get(voiceNumber).getMultiNotes();
 
 		// find clef and keysig
 		Clef clef = (Clef) getElementAt(measureOffset, measure.getClefs());
 		KeySignature keySig = (KeySignature) getElementAt(measureOffset, measure.getKeySignatures());
 		
 		// find MultiNote
 		MultiNote multiNote = (MultiNote) getElementAt(measureOffset, multiNoteList);
 		
 		Pitch pitch = calcPitch(lineNumber, clef, keySig);
 		// IF AN ACCIDENTAL IS SPECIFIED
 		if (index.getAccidental() != null) {
 			pitch = new Pitch(pitch.getNoteLetter(), pitch.getOctave(), index.getAccidental());
 		}
 		
 		EditInstructionType editInstrType = editInstr.getType();
 		ListIterator<Pitch> iter = multiNote.getPitches().listIterator();
 		
 		switch (editInstrType) {
 		case INSERT:
 			_editor.setPitchIter(iter);
 			_editor.insertPitch(pitch);
 			break;
 		case REMOVE:
 			// search pitches for a pitch that matches the pitch sent by the instruction
 			while (iter.hasNext()) {
 				if (iter.next().equals(pitch)) {
 					iter.previous();
 					break;
 				}
 			}
 			_editor.setPitchIter(iter);
 			_editor.removePitch();
 			break;
 		case REPLACE:
 			throw new RuntimeException("LogicManager does not support REPLACE for pitch edits.");
 		}
 		
 	}
 
 	private void interpretPlaybackInstr(PlaybackInstruction playbackInstr) {
 		switch (playbackInstr.getType()) {
 		case START:
 			_api.playPiece(_piece);
 			break;
 		case STOP:
 			_api.stopPlayback();
 			break;
 		}
 	}
 
 	// Used for finding the location of an edit, given a measure and an offset into that measure.
 	class IteratorAndOffset {
 		ListIterator<? extends Timestep> iter;
 		Rational offset;
 		public IteratorAndOffset(ListIterator<? extends Timestep> iter, Rational offset) {
 			this.iter = iter;
 			this.offset = offset;
 		}
 		public ListIterator<? extends Timestep> getIter() {
 			return this.iter;
 		}
 		public Rational getOffset() {
 			return this.offset;
 		}
 	}
 	private IteratorAndOffset calcIterAndOffset(List<? extends Timestep> list, Rational offset) {
 		ListIterator<? extends Timestep> iter;
 		Rational offsetFromIter;
 
 		iter = list.listIterator();
 		offsetFromIter = offset;
 
 		Rational rationalZero = new Rational(0,1);
 		while (iter.hasNext()) {
 			// subtract duration of next element from the current offset
 			Rational newOffset = offsetFromIter.minus(iter.next().getDuration());
 			if (newOffset.compareTo(rationalZero) < 0) {
 				// if the next element is longer than the current offset
 				iter.previous();
 				break;
 			}
 			else {
 				offsetFromIter = newOffset;
 			}
 		}
 		return new IteratorAndOffset(iter, offsetFromIter);
 	}
 	private Timestep getElementAt(Rational offset, List<? extends Timestep> list) {
 		IteratorAndOffset iterAndOff = calcIterAndOffset(list, offset);
 		return iterAndOff.getIter().next();
 	}
 	
 	// Calculates a pitch, given a line number, the clef, and the key signature
 	public Pitch calcPitch(int lineNum, Clef clef, KeySignature keySig) {
 		Pitch clefPitch = clef.getClefName().centerPitch();
 		Pitch keySigPitch = keySig.getKeySigPitch();
 		int clefLine = clef.getCenterLine();
 		boolean isMajor = keySig.getIsMajor();
 		int clefSclDgr = (clefPitch.getNoteLetter().intValue() - keySigPitch.getNoteLetter().intValue() + 1) % 7;
 		if (clefSclDgr < 0) clefSclDgr += 7;
		int lineNumSclDgr = (lineNum + (clefSclDgr - 1 - clefLine)) % 7;
 		if (lineNumSclDgr < 0) lineNumSclDgr += 7;
		lineNumSclDgr++;
 		ScaleDegree sclDgr = new ScaleDegree(lineNumSclDgr, Accidental.NATURAL);
 		Interval sclDgrInterval = sclDgr.getIntervalFromRoot(isMajor);
 		Pitch pitchLetter = keySigPitch.addInterval(sclDgrInterval);
 		int octave = (clef.getCenterValue() + lineNum - clefLine) / 7;
 		Pitch pitch = new Pitch(pitchLetter.getNoteLetter(), octave, pitchLetter.getAccidental());
 		return pitch;
 	}
 
 	public static void main(String[] args) {
 		// testing FileInstructionNew
 
 		LogicManager logicManager = new LogicManager(new Piece());
 		// TODO: THIS IS STILL ALL CONSTANTS
 		Clef trebleClef = new Clef(ClefName.GCLEF, -2);
 		Clef bassClef = new Clef(ClefName.FCLEF, 2);
 		List<Clef> clefList = new ArrayList<Clef>();
 		clefList.add(trebleClef);
 		clefList.add(bassClef);
 		FileInstruction testInstruction = new FileInstructionNew(clefList, 9, 3, 4, 0, true);
 		logicManager.interpretInstr(testInstruction);
 		try {
 			logicManager._arrangerXMLWriter.write(logicManager._piece, "tests/testNew.xml");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 
 		// testing EditMultiNote
 		/*
 		LogicManager logicManager = new LogicManager(new Piece());
 		FileInstruction testInstruction = new FileInstructionNew(logicManager, 2, 9, 3, 4, 0, true);
 		logicManager.interpretInstr(testInstruction);
 		List<InstructionIndex> indices1 = new ArrayList<InstructionIndex>();
 		List<InstructionIndex> indices2 = new ArrayList<InstructionIndex>();
 		indices1.add(new InstructionIndex(0, 0, 0, new Rational(0, 1)));
 		indices2.add(new InstructionIndex(0, 0, 0, new Rational(1, 8)));
 		EditInstruction editInstruction1 = new EditInstruction(logicManager,
 				indices1,
 				EditInstructionType.REPLACE,
 				EditType.MULTINOTE,
 				new MultiNote(new Rational(1, 8)));
 		EditInstruction editInstruction2 = new EditInstruction(logicManager,
 				indices2,
 				EditInstructionType.REPLACE,
 				EditType.MULTINOTE,
 				new MultiNote(new Rational(1, 4)));
 		EditInstruction editInstruction3 = new EditInstruction(logicManager,
 				indices2,
 				EditInstructionType.REPLACE,
 				EditType.MULTINOTE,
 				new MultiNote(new Rational(3, 4)));
 		logicManager.interpretInstr(editInstruction1);
 		logicManager.interpretInstr(editInstruction2);
 		logicManager.interpretInstr(editInstruction3);
 		try {
 			logicManager._arrangerXMLWriter.write(logicManager._piece, "tests/testNew.xml");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		*/
 
 		// testing FileInstructionIO
 		/*
 		LogicManager logicManager = new LogicManager(new Piece());
 		FileInstructionIO fileOpen = new FileInstructionIO(logicManager,
 				FileInstructionType.OPEN,
 				"tests/testLoad.xml");
 		FileInstructionIO fileSave = new FileInstructionIO(logicManager,
 				FileInstructionType.SAVE,
 				"tests/testSave.xml");
 		List<InstructionIndex> indices1 = new ArrayList<InstructionIndex>();
 		indices1.add(new InstructionIndex(0, 0, 0, new Rational(0, 1)));
 		EditInstruction editInstruction1 = new EditInstruction(logicManager,
 				indices1,
 				EditInstructionType.REPLACE,
 				EditType.MULTINOTE,
 				new MultiNote(new Rational(1, 8)));
 		logicManager.interpretInstr(fileOpen);
 		logicManager.interpretInstr(editInstruction1);
 		logicManager.interpretInstr(fileSave);
 		*/
 	}
 }
