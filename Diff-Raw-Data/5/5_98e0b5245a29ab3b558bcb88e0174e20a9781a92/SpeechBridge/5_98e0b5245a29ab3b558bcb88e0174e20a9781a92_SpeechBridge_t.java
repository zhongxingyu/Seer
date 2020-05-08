 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.sodbeans.phonemic.tts.impl;
 
 import java.util.Iterator;
 import java.util.concurrent.PriorityBlockingQueue;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import org.sodbeans.phonemic.RequestType;
 import org.sodbeans.phonemic.SpeechRequest;
 import org.sodbeans.phonemic.SpeechPriority;
 import org.sodbeans.phonemic.SpeechVoice;
 import org.sodbeans.phonemic.SpeechProcessor;
 import org.sodbeans.phonemic.tts.TextToSpeech;
 import org.sodbeans.phonemic.tts.TextToSpeechEngine;
 
 /**
  *
  * @author Andreas Stefik and Jeff Wilson
  */
 public class SpeechBridge implements TextToSpeech{
     /**
      * Takes TTS requests from the given PriorityBlockingQueue q and passes
      * them off to the TTS engine in the order denoted by their priority.
      *
      * Can be stopped using the stop() method. Implementation for stop()
      * method copied from:
      * http://download.oracle.com/javase/1.4.2/docs/guide/misc/threadPrimitiveDeprecation.html
      */
     private class ConsumerThread implements Runnable {
         private PriorityBlockingQueue<SpeechRequest> queue;
         private volatile Thread blinker;
         private boolean block = false;
         private SpeechRequest lastRequest =
                 new SpeechRequest();
 
         public ConsumerThread(PriorityBlockingQueue q) {
             this.queue = q;
         }
 
         public void setLastRequest(SpeechRequest r) {
             lastRequest = r;
         }
         
         public SpeechRequest getLastRequest() {
             return lastRequest;
         }
         
         public void start() {
             blinker = new Thread(this);
             blinker.start();
         }
 
         /**
          * Set whether or not the TTS engine should be blocking.
          *
          * If true, all speech requests are dropped until setBlocking
          * is called again with false.
          * @param blocking
          */
         public void setBlocking(boolean blocking) {
             this.block = blocking;
         }
 
         @Override
         public void run() {
             try {
                 Thread thisThread = Thread.currentThread();
                 while (thisThread == blinker) {
                     SpeechRequest request = queue.take();
 
                     // If we aren't blocking and we aren't speaking or this is
                     // of greater priority, send off to the TTS engine.
                     // Otherwise, we ignore te request.
                     //System.out.println(request.getText() + " " + request.getPriority());
 
                     if (!block && (!speech.isSpeaking() ||
                             request.getPriority().compareTo(lastRequest.getPriority()) <= 0)) {
                         SpeechProcessor proc = request.getProcessor();
                         String textToSpeak = request.getText();
 
                         if (proc != null)
                             textToSpeak = proc.process();
                        
                        if (textToSpeak != null)
                            speech.speak(textToSpeak, request.getPriority(), request.getType());
 
                         // We don't want to log CHARACTER requests.
                         if (request.getType() != RequestType.CHARACTER)
                             lastRequest = request;
                     }
                 }
 
                 blinker = null;
             }
             catch (InterruptedException e) { }
         }
 
         public void stop() {
             blinker = null;
         }
     }
 
     private PriorityBlockingQueue<SpeechRequest> speechQueue =
             new PriorityBlockingQueue<SpeechRequest>(1000);
     private TextToSpeech speech = null;
     private ConsumerThread consumer = new ConsumerThread(speechQueue);
 
     @Override
     public boolean canBlock() {
         return speech.canBlock();
     }
 
     @Override
     public boolean canPause() {
         return speech.canPause();
     }
 
     @Override
     public boolean canResume() {
         return speech.canResume();
     }
 
     @Override
     public boolean canStop() {
         return speech.canStop();
     }
 
     @Override
     public boolean canSetVoice() {
         return speech.canSetVoice();
     }
 
     @Override
     public boolean canSetVolume() {
         return speech.canSetVolume();
     }
 
     @Override
     public boolean canSetSpeed() {
         return speech.canSetSpeed();
     }
 
     @Override
     public boolean canSetPitch() {
         return speech.canSetPitch();
     }
 
     @Override
     public Iterator<SpeechVoice> getAvailableVoices() {
         return speech.getAvailableVoices();
     }
 
     @Override
     public SpeechVoice getCurrentVoice() {
         return speech.getCurrentVoice();
     }
 
     @Override
     public double getSpeed() {
         return speech.getSpeed();
     }
 
     @Override
     public TextToSpeechEngine getTextToSpeechEngine() {
         return speech.getTextToSpeechEngine();
     }
 
     @Override
     public double getVolume() {
         return speech.getVolume();
     }
 
     @Override
     public double getPitch() {
         return speech.getPitch();
     }
 
     @Override
     public boolean isSpeaking() {
         return speech.isSpeaking();
     }
 
     @Override
     public boolean pause() {
         return speech.pause();
     }
 
     @Override
     public void reinitialize() {
         speech.reinitialize();
     }
 
     @Override
     public boolean respeak() {
         SpeechRequest last = consumer.getLastRequest();
 
         speechQueue.offer(last);
         return true;
     }
 
     @Override
     public boolean copyToClipboard() {
         try {
             String previousSpeech = consumer.getLastRequest().getText();
             StringSelection selection = new StringSelection(previousSpeech.trim());
             Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
             return true;
         } catch (Exception exception) {
             return false;
         }
         
     }
     
     @Override
     public boolean resume() {
         return speech.resume();
     }
 
     @Override
     public boolean setVolume(double vol) {
         return speech.setVolume(vol);
     }
 
     @Override
     public boolean setSpeed(double speed) {
         return speech.setSpeed(speed);
     }
 
     @Override
     public boolean setPitch(double pitch) {
         return speech.setPitch(pitch);
     }
 
     @Override
     public boolean setVoice(SpeechVoice voice) {
         return speech.setVoice(voice);
     }
 
     @Override
     public boolean speak(char c) {
         return speak(c, SpeechPriority.MEDIUM);
     }
 
     @Override
     public boolean speak(char c, final SpeechPriority priority) {
         // If it's a blocking call, handle that special case.
         if (priority == SpeechPriority.BLOCKING) {
             return speakBlocking(c);
         }
 
         String text = Character.toString(c);
         SpeechRequest request = new SpeechRequest();
         request.setText(text);
         request.setPriority(priority);
         request.setType(RequestType.CHARACTER);
 
         speechQueue.offer(request);
         return true;
     }
 
     @Override
     public boolean speak(final String text) {
         return speak(text, SpeechPriority.MEDIUM, RequestType.TEXT);
     }
 
     @Override
     public boolean speak(final String text, final SpeechPriority priority) {
         return speak(text, priority, RequestType.TEXT);
     }
 
     @Override
     public boolean speak(final String text, final SpeechPriority priority, final RequestType type) {
          if (text == null) {
             return false;
         }
 
         // Don't pass blank strings...
         if (text.trim().length() == 0)
         {
             return false;
         }
 
         // If it's a blocking call, handle that special case.
         if (priority == SpeechPriority.BLOCKING) {
             return speakBlocking(text, SpeechPriority.MEDIUM);
         }
 
         SpeechRequest request = new SpeechRequest();
         request.setText(text);
         request.setPriority(priority);
         speechQueue.offer(request);
         return true;
     }
 
     @Override
     public boolean speak(SpeechProcessor proc) {
         if (proc == null) {
             return false;
         }
         else if (proc.getText().trim().length() == 0 && proc.getRequestType() != RequestType.CHARACTER
                 && proc.process().trim().length() == 0 ) {
             // We don't want to pass blank text.
             return false;
         }
         
         if (proc.getPriority() == SpeechPriority.BLOCKING) {
             speakBlocking(proc.process(), proc.getPriority(), proc.getRequestType());
         }
         else {
             SpeechRequest req = new SpeechRequest();
             req.setText(proc.getText());
             req.setProcessor(proc);
             req.setPriority(proc.getPriority());
             req.setType(proc.getRequestType());
             speechQueue.offer(req);
 
         }
 
         return true;
     }
     
     @Override
     public synchronized boolean speakBlocking(String text) {
         return speakBlocking(text, SpeechPriority.MEDIUM);
     }
 
     @Override
     public synchronized boolean speakBlocking(String text, SpeechPriority priority) {
         return speakBlocking(text, priority, RequestType.TEXT);
     }
 
     @Override
     public synchronized boolean speakBlocking(String text, SpeechPriority priority, RequestType type) {
         if (text == null) {
             return false;
         }
 
         // Don't pass blank strings...
         if (text.trim().length() == 0)
         {
             return false;
         }
 
         if (speech.canBlock()) {
             consumer.setBlocking(true);
             boolean result = speech.speakBlocking(text, priority, type);
             consumer.setBlocking(false);
             
             // Set this as the last request in the consumer thread.
             SpeechRequest request = new SpeechRequest();
             request.setText(text);
             request.setPriority(priority);
             request.setType(type);
             consumer.setLastRequest(request);
             return result;
         }
         else
         {
             // Treat it like a normal call.
             return speak(text);
         }
     }
     
     @Override
     public synchronized boolean speakBlocking(char c) {
         return speakBlocking(c, SpeechPriority.MEDIUM);
     }
 
     @Override
     public synchronized boolean speakBlocking(char c, SpeechPriority priority) {
         if (speech.canBlock()) {
             consumer.setBlocking(true);
             boolean result = speech.speakBlocking(c);
             consumer.setBlocking(false);
             return result;
         }
         else
         {
             // Treat it like a normal call.
             return speak(c);
         }
     }
 
     @Override
     public boolean stop() {
         boolean result = speech.stop();
         
         // Ramp down to the lowest priority so that future calls aren't blocked.
         SpeechRequest dummy = new SpeechRequest();
         dummy.setPriority(SpeechPriority.LOWEST);
         consumer.setLastRequest(dummy);
         return result;
     }
 
     /**
      * @return the speech
      */
     public TextToSpeech getSpeech() {
         return speech;
     }
 
     /**
      * @param speech the speech to set
      */
     public void setSpeech(TextToSpeech speech) {
         consumer.stop(); // safe to call even if not running
         this.speech = speech;
 
         // Set some good defaults.
         this.speech.setVolume(0.5);
         this.speech.setSpeed(0.5);
         this.speech.setPitch(0.5);
         
         consumer.start();
     }
 }
