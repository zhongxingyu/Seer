 package logic;
 
 import music.*;
 import java.lang.Thread;
 import java.util.ListIterator;
 import java.util.TreeMap;
 import java.util.ArrayList;
 
 public class MidiPlayer extends Thread {
 
	TreeMap _starts;
	TreeMap _ends;
 	midiAPI _midi;
 	ArrayList<ListIterator<MultiNote>> _itrList;
 
 	public MidiPlayer(midiAPI midi, ArrayList<ListIterator<MultiNote>> iList) {
		_starts = new TreeMap();
		_ends = new TreeMap();
 		_midi = midi;
 		_itrList = iList;
 	}
 
 	public void start() {
 		// get start timestamp
 
 	}
 
 	public void addMultiNote(MultiNote mn) {
 
 	}
 
 	public void run() {
 
 		//set current time to 0 beats
 		Rational currentTime = new Rational().plus(new Rational().negate());
 		System.out.println("current time is " + currentTime.getNumerator() + "/" + currentTime.getDenominator());
 
 		//next time an action occurs
 		Rational nextTime;
 
 		//initiate the _starts treemap with the iterators to the lists of multinotes (that represent voices)
 		for(int i = 0; i < _itrList.size(); i++){
 			ListIterator<MultiNote> itr = _itrList.get(i);
 
 			Timestamp ts = new Timestamp();
 			ts.setDuration(currentTime);
 
 			_starts.put(ts, itr);
 		}
 
 		boolean _noteOnDone = false; //true when _starts is empty
 		boolean _noteOffDone = false; //true when _ends is empty
 
 		while (!_noteOnDone || !_noteOffDone) {
 
 			// turn on notes
 			if(_starts.isEmpty()){
 
 				_noteOnDone = true;
 			} else {
 
 				Timestamp firstKey = (Timestamp) _starts.firstKey();
 				ListIterator<MultiNote> itr = _starts.get(firstKey);
 				if(itr.hasNext()){

 					//if the timestamp of this multinote is greater than the current timestamp
 					if (firstKey.compareTo(currentTime) >= 0){
 
 						currentTime = firstKey.getDuration();
 
 						// play it
 
 						MultiNote mn = itr.next();
 						_midi.multiNoteOn(mn);
 
 						firstKey.addDuration(mn);
 
 						Timestamp ts2 = new Timestamp();
 						ts2.setDuration(currentTime.plus(mn.getDuration()));
 						_ends.put(ts2, mn);
 
 						nextTime = currentTime.plus(mn.getDuration());
 
 					} else {
 	//					nextTime = ts.getDuration();
 						System.out.println("nextTime is reset");
 
 					}
 
 				} else {
 
 					_starts.remove(firstKey);
 				}
 			}
 
 
 			if(_ends.isEmpty()){
 
 				_noteOffDone = true;
 			} else {
 
 				// turn off notes
 				Timestamp ts2 = (Timestamp) _ends.firstKey();
 
 				//if the timestamp of this multinote is greater than the current timestamp
 				if (ts2.compareTo(currentTime) >= 0){
 					// stop playing it
 
 					MultiNote mn = _ends.get(ts2);
 
 					_midi.multiNoteOff(mn);
 					nextTime = min(nextTime, currentTime.plus(mn.getDuration()));
 
 				}
 //				else{
 //					nextTime = Math.min(nextTime, ts.getDuration());
 //				}
 			}
 
 			//thread sleeps for the amount of time between the current time and the next note_on/note_off event
 			Rational sleepDuration = nextTime.plus(currentTime.negate());
			int sleepMilli = _midi.getWholeNoteDuration() * (double) sleepDuration.getNumerator()/ sleepDuration.getDenominator();
			Thread.sleep(sleepMilli);
 		}
 	}
 
 	//returns the minimum of two Rationals
 	public Rational min(Rational a, Rational b){
 
		if(a.compareTo(b) == 1){
 			return b;
 		} else {
 			return a;
 		}
 	}
 }
