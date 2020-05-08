 package org.jfugue.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.PushbackReader;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jfugue.ChannelPressure;
 import org.jfugue.Controller;
 import org.jfugue.Instrument;
 import org.jfugue.JFugueDefinitions;
 import org.jfugue.KeySignature;
 import org.jfugue.Layer;
 import org.jfugue.Measure;
 import org.jfugue.Note;
 import org.jfugue.Note.NoteFactory;
 import org.jfugue.ParserListener;
 import org.jfugue.PitchBend;
 import org.jfugue.PolyphonicPressure;
 import org.jfugue.SystemExclusiveEvent;
 import org.jfugue.Tempo;
 import org.jfugue.Time;
 import org.jfugue.Voice;
 import org.jfugue.parsers.Environment;
 import org.jfugue.parsers.FireParserEventProxy;
 import org.jfugue.parsers.ParserContext;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
import org.junit.Ignore;
 
 public class NoteFactoryTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
    
	@Ignore(" NoteFactory::createElement is not yet implemented.") @Test
 	public void testParseElementPushbackReader() throws Exception {
 		Map<String,String> dict = new HashMap<String, String>(JFugueDefinitions.DICT_MAP);
 		Environment environment = new Environment(dict, new DummyProxy());
 		Note expected = Note.createNote("C");
 		PushbackReader reader = new PushbackReader(new StringReader(expected.getMusicString()));
 		ParserContext context = new ParserContext(reader, environment);
 		Note actual = NoteFactory.getInstance().createElement(context);
 		assertEquals("the expected note does not equal the actual", expected, actual);
 		
 	}
 
 	static private class DummyProxy implements FireParserEventProxy {
 
 		public void addParserListener(ParserListener listener) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void removeParserListener(ParserListener listener) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public ParserListener[] getParserListeners() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public void clearParserListeners() {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireVoiceEvent(Voice event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireTempoEvent(Tempo event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireInstrumentEvent(Instrument event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireLayerEvent(Layer event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireTimeEvent(Time event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireSystemExclusiveEvent(SystemExclusiveEvent event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireKeySignatureEvent(KeySignature event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireMeasureEvent(Measure event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireControllerEvent(Controller event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireChannelPressureEvent(ChannelPressure event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void firePolyphonicPressureEvent(PolyphonicPressure event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void firePitchBendEvent(PitchBend event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireNoteEvent(Note event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireSequentialNoteEvent(Note event) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void fireParallelNoteEvent(Note event) {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 }
