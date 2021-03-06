 /*
  * Copyright 1999-2002 Carnegie Mellon University.  
  * Portions Copyright 2002 Sun Microsystems, Inc.  
  * Portions Copyright 2002 Mitsubishi Electronic Research Laboratories.
  * All Rights Reserved.  Use is subject to license terms.
  * 
  * See the file "license.terms" for information on usage and
  * redistribution of this file, and for a DISCLAIMER OF ALL 
  * WARRANTIES.
  *
  */
 
 
 package edu.cmu.sphinx.frontend.endpoint;
 
 import edu.cmu.sphinx.frontend.Audio;
 import edu.cmu.sphinx.frontend.AudioSource;
 import edu.cmu.sphinx.frontend.DataProcessor;
 import edu.cmu.sphinx.frontend.FrontEnd;
 import edu.cmu.sphinx.frontend.Signal;
 
 import edu.cmu.sphinx.util.SphinxProperties;
 import edu.cmu.sphinx.util.LogMath;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 
 /**
  * Converts a stream of Audio objects, marked as speech and non-speech,
  * into utterances. This is done by inserting SPEECH_START and
  * SPEECH_END signals into the stream.
  *
  * <p>The algorithm for inserting the two signals is as follows.
  *
  * <p>The algorithm is always in one of two states: 'in-speech' and 
  * 'out-of-speech'. If 'out-of-speech', it will read in audio until 
  * we hit audio that is speech. If we have read more than 'startSpeech'
  * amount of <i>continuous</i> speech, we consider that speech has started, 
  * and insert a SPEECH_START at 'speechLeader' time before speech 
  * first started. The state of the algorithm changes to 'in-speech'.
  *
  * <p>Now consider the case when the algorithm is in 'in-speech' state.
  * If it read an audio that is speech, it is outputted. If the audio is
  * non-speech, we read ahead until we have 'endSilence' amount of 
  * <i>continuous</i> non-speech. At the point we consider that speech 
  * has ended. A SPEECH_END signal is inserted at 'speechTrailer' time 
  * after the first non-speech audio. The algorithm returns to 
  * 'ou-of-speech' state. If any speech audio is encountered in-between,
  * the accounting starts all over again.
  */
 public class SpeechMarker extends DataProcessor implements AudioSource {
 
     public static final String PROP_PREFIX = 
         "edu.cmu.sphinx.frontend.endpoint.SpeechMarker.";
 
     /**
      * The SphinxProperty for the minimum amount of time in speech
      * (in milliseconds) to be considered as utterance start.
      */
     public static final String PROP_START_SPEECH = 
         PROP_PREFIX + "startSpeech";
 
     /**
      * The default value of PROP_START_SPEECH.
      */
     public static final int PROP_START_SPEECH_DEFAULT = 200;
 
     /**
      * The SphinxProperty for the amount of time in silence
      * (in milliseconds) to be considered as utterance end.
      */
     public static final String PROP_END_SILENCE = PROP_PREFIX + "endSilence";
 
     /**
      * The default value of PROP_END_SILENCE.
      */
     public static final int PROP_END_SILENCE_DEFAULT = 500;
 
     /**
      * The SphinxProperty for the amount of time (in milliseconds)
      * before speech start to be included as speech data.
      */
     public static final String PROP_SPEECH_LEADER = 
         PROP_PREFIX + "speechLeader";
 
     /**
      * The default value of PROP_SPEECH_LEADER.
      */
     public static final int PROP_SPEECH_LEADER_DEFAULT = 100;
 
     /**
      * The SphinxProperty for the amount of time (in milliseconds)
      * after speech ends to be included as speech data.
      */
     public static final String PROP_SPEECH_TRAILER = 
         PROP_PREFIX + "speechTrailer";
 
     /**
      * The default value of PROP_SPEECH_TRAILER.
      */
     public static final int PROP_SPEECH_TRAILER_DEFAULT = 100;
 
 
     private AudioSource predecessor;
     private List outputQueue;  // Audio objects are added to the end
     private boolean inSpeech;
     private int startSpeechTime;
     private int endSilenceTime;
     private int speechLeader;
     private int speechTrailer;
     private int sampleRate;
 
 
     /**
      * Initializes this SpeechMarker with the given name, context,
      * and AudioSource predecessor.
      *
      * @param name the name of this SpeechMarker
      * @param context the context of the SphinxProperties this
      *    SpeechMarker uses
      * @param props the SphinxProperties to read properties from
      * @param predecessor the AudioSource where this SpeechMarker
      *    gets Cepstrum from
      *
      * @throws java.io.IOException
      */
     public void initialize(String name, String context, 
                            SphinxProperties props,
                            AudioSource predecessor) throws IOException {
         super.initialize(name, context, props);
         this.predecessor = predecessor;
         this.outputQueue = new ArrayList();
         setProperties();
         reset();
     }
 
     /**
      * Sets the properties for this SpeechMarker.
      */
     private void setProperties() {
         SphinxProperties props = getSphinxProperties();
         startSpeechTime = 
             props.getInt(PROP_START_SPEECH, PROP_START_SPEECH_DEFAULT);
         endSilenceTime = 
             props.getInt(PROP_END_SILENCE, PROP_END_SILENCE_DEFAULT);
         speechLeader =
             props.getInt(PROP_SPEECH_LEADER, PROP_SPEECH_LEADER_DEFAULT);
         speechTrailer =
             props.getInt(PROP_SPEECH_TRAILER, PROP_SPEECH_TRAILER_DEFAULT);
         sampleRate =
             props.getInt(FrontEnd.PROP_SAMPLE_RATE,
                          FrontEnd.PROP_SAMPLE_RATE_DEFAULT);
     }
 
     /**
      * Resets this SpeechMarker to a starting state.
      */
     private void reset() {
         inSpeech = false;
     }
 
     /**
      * Returns the next Audio object.
      *
      * @return the next Audio object, or null if none available
      *
      * @throws java.io.IOException if an error occurred
      *
      * @see Audio
      */
     public Audio getAudio() throws IOException {
         if (outputQueue.size() == 0) {
             if (!inSpeech) {
                 readInitialFrames();
             } else {
                 Audio audio = readAudio();
                 if (audio != null) {
                     if (audio.hasContent()) {
                         sendToQueue(audio);
                         if (!audio.isSpeech()) {
                             inSpeech = !(readEndFrames(audio));
                         }
                     } else if (audio.hasSignal(Signal.UTTERANCE_END)) {
                         sendToQueue(new Audio(Signal.SPEECH_END, 
                                               audio.getCollectTime(),
                                               audio.getFirstSampleNumber()));
                         sendToQueue(audio);
                         inSpeech = false;
                     } else if (audio.hasSignal(Signal.UTTERANCE_START)) {
                         throw new Error("Got UTTERANCE_START while in speech");
                     }
                 }
             }
         }
         if (outputQueue.size() > 0) {
             Audio audio = (Audio) outputQueue.remove(0);
             return audio;
         } else {
             return null;
         }
     }
 
     private Audio readAudio() throws IOException {
         Audio audio = predecessor.getAudio();
         /*
         if (audio != null) {
             String speech = "";
             if (audio.hasContent() && audio.isSpeech()) {
                 speech = " *";
             }
             System.out.println("SpeechMarker: incoming: " + 
                                audio.getSignal() + speech);
         }
         */
         return audio;
     }
 
     private int numUttStarts;
     private int numUttEnds;
 
     private void sendToQueue(Audio audio) {
         // now add the audio
         outputQueue.add(audio);
         if (audio.hasSignal(Signal.UTTERANCE_START)) {
             numUttEnds = 0;
             numUttStarts++;
             if (numUttStarts > 1) {
                throw new Error("Too many utterance starts");
             }
         } else if (audio.hasSignal(Signal.UTTERANCE_END)) {
             numUttStarts = 0;
             numUttEnds++;
             if (numUttEnds > 1) {
                throw new Error("Too many utterance ends");
             }
         }
     }
 
     /**
      * Returns the amount of audio data in milliseconds in the 
      * given Audio object.
      *
      * @param audio the Audio object
      *
      * @return the amount of audio data in milliseconds
      */
     public int getAudioTime(Audio audio) {
         return (int) (audio.getSamples().length * 1000.0f / sampleRate);
     }
         
     /**
      * Read the starting frames until the utterance has started.
      */
     private void readInitialFrames() throws IOException {
         int nonSpeechTime = 0;
         int minSpeechTime = (startSpeechTime > speechLeader) ?
             startSpeechTime : speechLeader;
 
         while (!inSpeech) {
             Audio audio = readAudio();
             if (audio == null) {
                 return;
             } else {
                 sendToQueue(audio);
                 if (audio.hasContent()) {
                     nonSpeechTime += getAudioTime(audio);
                     if (audio.isSpeech()) {
                         boolean speechStarted = handleFirstSpeech(audio);
                         if (speechStarted) {
                             // System.out.println("Speech started !!!");
                             addSpeechStart();
                             inSpeech = true;
                             break;
                         }
                     }
                 }
             }
             int i = 0;
             // prune any excessive non-speech
             while (nonSpeechTime > minSpeechTime) {
                 Audio next = (Audio) outputQueue.get(i);
                 if (next.hasContent()) {
                     int audioTime = getAudioTime(next);
                     if (nonSpeechTime - audioTime >= minSpeechTime) {
                         next = (Audio) outputQueue.remove(i);
                         nonSpeechTime -= audioTime;
                     }
                 } else {
                     /*
                     System.out.println
                         ("Not removed ("+i+"): "+next.getSignal());
                     */
                 }
                 i++;
             }
         }
     }
 
     /**
      * Handles an Audio object that can possibly be the first in
      * an utterance. 
      *
      * @param audio the Audio to handle
      *
      * @return true if utterance/speech has started for real, false otherwise
      */
     private boolean handleFirstSpeech(Audio audio) throws IOException {
         int speechTime = getAudioTime(audio);
         // System.out.println("Entering handleFirstSpeech()");
         
         // try to read more that 'startSpeechTime' amount of
         // audio that is labeled as speech (the condition for speech start)
         while (speechTime < startSpeechTime) {
             Audio next = readAudio();
             sendToQueue(next);
             if (!next.isSpeech()) {
                 return false;
             } else {
                 speechTime += getAudioTime(audio);
             }
         }
         return true;
     }
 
     /**
      * Backtrack from the current position to add a SPEECH_START Signal
      * to the outputQueue.
      */
     private void addSpeechStart() {
         long lastCollectTime = 0;
         long firstSampleNumber = 0;
         int silenceLength = 0;
         ListIterator i = outputQueue.listIterator(outputQueue.size()-1);
 
         // backtrack until we have 'speechLeader' amount of non-speech
         while (silenceLength < speechLeader && i.hasPrevious()) {
             Audio current = (Audio) i.previous();
             if (current.hasContent()) {
                 if (current.isSpeech()) {
                     silenceLength = 0;
                 } else {
                     silenceLength += getAudioTime(current);
                 }
                 lastCollectTime = current.getCollectTime();
                 firstSampleNumber = current.getFirstSampleNumber();
             } else if (current.hasSignal(Signal.UTTERANCE_START)) {
                 if (firstSampleNumber == 0) {
                     firstSampleNumber = current.getFirstSampleNumber();
                 }
                 i.next(); // put the SPEECH_START after the UTTERANCE_START
                 break;
             } else if (current.hasSignal(Signal.UTTERANCE_END)) {
                 throw new Error("No UTTERANCE_START after UTTERANCE_END");
             }
         }
 
         if (speechLeader > 0) {
             assert lastCollectTime != 0;
         }
         // add the SPEECH_START
         i.add(new Audio(Signal.SPEECH_START, lastCollectTime, 
                         firstSampleNumber));
     }
 
     /**
      * Given a non-speech frame, try to read more non-speech frames
      * until we think its the end of utterance.
      *
      * @param audio a non-speech frame
      *
      * @return true if speech has really ended, false if speech
      *    has not ended
      */
     private boolean readEndFrames(Audio audio) throws IOException {
 
         boolean speechEndAdded = false;
         boolean readTrailer = true;
         int originalLast = outputQueue.size() - 1;
         int silenceLength = getAudioTime(audio);
 
         // read ahead until we have 'endSilenceTime' amount of silence
         while (silenceLength < endSilenceTime) {
             Audio next = readAudio();
             if (next.hasContent()) {
                 sendToQueue(next);
                 if (next.isSpeech()) {
                     // if speech is detected again, we're still in
                     // an utterance
                     return false;
                 } else {
                     // it is non-speech
                     silenceLength += getAudioTime(next);
                 }
             } else if (next.hasSignal(Signal.UTTERANCE_END)) {
                 sendToQueue(next);
                 readTrailer = false;
                 break;
             } else {
                 throw new Error("Illegal signal: " + next.getSignal());
             }
         }
 
         if (readTrailer) {
             // read ahead until we have 'speechTrailer' amount of silence
             while (!speechEndAdded && silenceLength < speechTrailer) {
                 Audio next = readAudio();
                 if (next.hasContent()) {
                     if (next.isSpeech()) {
                         // if we have hit speech again, then the current
                         // speech should end
                         sendToQueue(new Audio(Signal.SPEECH_END,
                                               next.getCollectTime(),
                                               next.getFirstSampleNumber()-1));
                         sendToQueue(next);
                         speechEndAdded = true;
                         break;
                     } else {
                         silenceLength += getAudioTime(next);
                         sendToQueue(next);
                     }
                 } else if (next.hasSignal(Signal.UTTERANCE_END)) {
                     sendToQueue(new Audio(Signal.SPEECH_END,
                                           next.getCollectTime(),
                                           next.getFirstSampleNumber()));
                     sendToQueue(next);
                     speechEndAdded = true;
                 } else {
                     throw new Error("Illegal signal: " + next.getSignal());
                 }
             }
         }
 
         if (!speechEndAdded) {
             // iterate from the end of speech and read till we
             // have 'speechTrailer' amount of non-speech, and
             // then add an SPEECH_END
             ListIterator i = outputQueue.listIterator(originalLast);
             long nextCollectTime = 0;
 
             // the 'firstSampleNumber' of SPEECH_END actually contains
             // the last sample number of the segment
             long lastSampleNumber = 0;
             silenceLength = 0;
 
             while (silenceLength < speechTrailer && i.hasNext()) {
                 Audio next = (Audio) i.next();
                 nextCollectTime = next.getCollectTime();
                 if (next.hasSignal(Signal.UTTERANCE_END)) {
                     i.previous();
                     break;
                 } else {
                     assert !next.isSpeech();
                     silenceLength += getAudioTime(next);
                     if (next.hasContent()) {
                         lastSampleNumber = next.getFirstSampleNumber() +
                             next.getSamples().length - 1;
                     }
                 }
             }
             
             if (speechTrailer > 0) {
                 assert nextCollectTime != 0 && lastSampleNumber != 0;
             }
             i.add(new Audio(Signal.SPEECH_END, nextCollectTime, 
                             lastSampleNumber));
         }
 
         // System.out.println("Speech ended !!!");
         return true;
     }
 
 }
