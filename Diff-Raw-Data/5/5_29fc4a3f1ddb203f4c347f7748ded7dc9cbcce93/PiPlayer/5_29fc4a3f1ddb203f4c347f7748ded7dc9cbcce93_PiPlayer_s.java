 /* 
  * PiPlayer.java
  * Copyright 2012 Aditya Vaidya <kroq.gar78@gmail.com>
  * 
  * Plays the first 1 million digits of Pi based on a certain key (what 
  * that key is, is yet to be determined :P)
  * 
  * Don't worry, it can also play Tau, for those who believe Pi is wrong.
  * Digits of Tau obtained from http://tauday.com/tau-digits.
  * 
 * All note frequencies taken from http://en.wikipedia.org/wiki/Piano_key_frequencies
  * 
  * 
  * This file is part of PiPlayer.
  * 
  * PiPlayer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * PiPlayer is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 import java.math.BigInteger;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.LinkedList;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 
 public class PiPlayer
 {
     public static float SAMPLE_RATE = 8000f;
     public static int SAMPLE_LENGTH = 125; // length of tone in milliseconds
     public static int BUFFER_SIZE = 100; // how many tones can fit into one buffer
     
     public static double[] KEY = new double[10]; // C Major
     
     public static boolean EOF = false;
     
     protected static LinkedList<byte[]> buf2 = null;
     
     protected static AudioFormat AUDIO_FORMAT;
     protected static SourceDataLine SOURCE_DATA_LINE;
     
     private static class GeneratorThread implements Runnable
     {
         public GeneratorThread(BufferedReader pin)
         {
             this.pin = pin;
         }
         
         public void run()
         {
             int digitCount = 0;
             try
             {
                 int digitChar = 0;
                 while((digitChar = pin.read()) != -1)
                 {
                     while(buf2.size()>=BUFFER_SIZE*10)
                     {
                         Thread.sleep(500/SAMPLE_LENGTH*BUFFER_SIZE);
                     }
                     if( (char)digitChar == '.' )
                     {
                         System.out.print('.');
                         continue;
                     }
                     int digit = Character.getNumericValue((char)digitChar);
                     System.out.print(digit);
                     buf2.offer((digitCount&1)==0? getSquareWave( KEY[digit], SAMPLE_LENGTH, 0.2 ) : getSineWave( KEY[digit], SAMPLE_LENGTH, 0.2 ));
                     digitCount++;
                 }
                 PiPlayer.EOF = true;
             }
             catch( Exception e ) { e.printStackTrace(); }
         }
         
         // pin = pi in ;)
         private BufferedReader pin;
     }
     
     private static class PlayerThread implements Runnable
     {
         public PlayerThread()
         {
             digitCount = 0;
         }
         
         public void run()
         {
             try
             {
                 while(!PiPlayer.EOF || buf2.size()>1)
                 {
                     if(buf2.size()<1)
                     {
                         Thread.sleep(50);
                         System.out.println("Too fast!");
                         continue;
                     }
                     for( int i = 0; i < BUFFER_SIZE && buf2.size() > 1; i++ )
                     {
                         SOURCE_DATA_LINE.write(buf2.poll(),0,getToneArrayLength(SAMPLE_LENGTH));
                     }
                     SOURCE_DATA_LINE.drain();
                     Thread.sleep(80);
                     //digitCount++;
                 }
             }
             catch( Exception e ) { e.printStackTrace(); }
             SOURCE_DATA_LINE.close();
         }
         
         private int digitCount;
     }
     
     public static byte[] getSineWave( double hz, int millis, double vol )
     {
         if(hz<=0) throw new IllegalArgumentException("Frequency <= 0 Hz");
         if(millis<=0) throw new IllegalArgumentException("Duration <= 0 msecs");
         if(vol > 1.0 || vol < 0.0) throw new IllegalArgumentException("Volume not in range 0.0 - 1.0");
         
         byte[] buf = new byte[getToneArrayLength(millis)];
         
         for( int i = 0; i < buf.length; i++ )
         {
             double angle = i / (SAMPLE_RATE/hz) * 2.0 * Math.PI;
             buf[i] = (byte)(Math.sin(angle) * 127.0 * vol);
         }
         
         // shape front and back 10ms of the wave form
         for( int i = 0; i < SAMPLE_RATE/100.0 && i < buf.length / 2; i++ )
         {
             buf[i] = (byte)(buf[i] * i / (SAMPLE_RATE / 100.0));
             buf[buf.length-1-i] = (byte)(buf[buf.length-1-i] * i / (SAMPLE_RATE/100.0));
         }
         
         return buf;
     }
     
     public static byte[] getSawtoothWave( double hz, int millis, double vol )
     {
         if(hz<=0) throw new IllegalArgumentException("Frequency <= 0 Hz");
         if(millis<=0) throw new IllegalArgumentException("Duration <= 0 msecs");
         if(vol > 1.0 || vol < 0.0) throw new IllegalArgumentException("Volume not in range 0.0 - 1.0");
         
         byte[] buf = new byte[getToneArrayLength(millis)];
         
         for( int i = 0; i < buf.length; i++ )
         {
             //double angle = i / (SAMPLE_RATE/hz) * 2.0 * Math.PI;
             buf[i] = (byte)((i%(SAMPLE_RATE/hz)) * 127.0 * vol);
         }
         
         // shape front and back 10ms of the wave form
         /*for( int i = 0; i < SAMPLE_RATE/100.0 && i < buf.length / 2; i++ )
         {
             buf[i] = (byte)(buf[i] * i / (SAMPLE_RATE / 100.0));
             buf[buf.length-1-i] = (byte)(buf[buf.length-1-i] * i / (SAMPLE_RATE/100.0));
         }*/
         
         return buf;
     }
     
     public static byte[] getSquareWave( double hz, int millis, double vol )
     {
         if(hz<=0) throw new IllegalArgumentException("Frequency <= 0 Hz");
         if(millis<=0) throw new IllegalArgumentException("Duration <= 0 msecs");
         if(vol > 1.0 || vol < 0.0) throw new IllegalArgumentException("Volume not in range 0.0 - 1.0");
         
         byte[] buf = new byte[getToneArrayLength(millis)];
         
         for( int i = 0; i < buf.length; i++ )
         {
             double angle = i / (SAMPLE_RATE/hz) * 2.0 * Math.PI;
             buf[i] = (byte)(Math.signum(Math.sin(angle)) * 127.0 * vol);
         }
         
         // shape front and back 10ms of the wave form
         /*for( int i = 0; i < SAMPLE_RATE/100.0 && i < buf.length / 2; i++ )
         {
             buf[i] = (byte)(buf[i] * i / (SAMPLE_RATE / 100.0));
             buf[buf.length-1-i] = (byte)(buf[buf.length-1-i] * i / (SAMPLE_RATE/100.0));
         }*/
         
         return buf;
     }
     
     public static void playSound( byte[] buf ) throws LineUnavailableException
     {
         
         //System.out.println(buf.length);
         SOURCE_DATA_LINE.drain();
     }
     /*public static void playSound( double hz, int millis, double vol ) throws LineUnavailableException
     {
         playSound(getSound(hz,millis,vol));
     }*/
     
     public static int getToneArrayLength( int millis )
     {
         return (int)SAMPLE_RATE * millis / 1000;
     }
     
     public static double getFrequency(int halfStepsAboveConcertA)
     {
         return Math.pow(2,(double)(halfStepsAboveConcertA/12))*440;
     }
     
     public static void main( String[] args ) throws LineUnavailableException, InterruptedException
     {
         // TONS of pitch definitions; all is based on A = 440Hz
         double[] naturals = {440.000,493.883,523.251,587.330,659.255,698.456,783.991};
         double a = naturals[0];
         double b = naturals[1];
         double c = naturals[2];
         double d = naturals[3];
         double e = naturals[4];
         double f = naturals[5];
         double g = naturals[6];
         
         double[] sharps = {466.164,c,554.365,622.254,f,739.989,830.609};
         double aSharp = sharps[0];
         double bSharp = sharps[1];
         double cSharp = sharps[2];
         double dSharp = sharps[3];
         double eSharp = sharps[4];
         double fSharp = sharps[5];
         double gSharp = sharps[6];
         
         double[] flats = {415.305,aSharp,b,cSharp,dSharp,e,fSharp};
         double aFlat = flats[0];
         double bFlat = flats[1];
         double cFlat = flats[2];
         double dFlat = flats[3];
         double eFlat = flats[4];
         double fFlat = flats[5];
         double gFlat = flats[6];
         
         // digit = index
         //double[] key = {cSharp,a,b,cSharp,d,e,fSharp,gSharp,a*2,b*2}; // D Major
         //double[] key = {e,cSharp,d,e,fSharp,g,a,b,cSharp,d}; // Random Major
         //KEY = {c*2,a,b,c,d,e,f,g,a*2,b*2}; // C Major
         /*KEY[0] = c*2;
         KEY[1] = a;
         KEY[2] = b;
         KEY[3] = c;
         KEY[4] = d;
         KEY[5] = e;
         KEY[6] = f;
         KEY[7] = g;
         KEY[8] = a*2;
         KEY[9] = b*2;*/
         KEY[0] = e*2;
         KEY[1] = c;
         KEY[2] = d;
         KEY[3] = e;
         KEY[4] = f;
         KEY[5] = g;
         KEY[6] = a;
         KEY[7] = b;
         KEY[8] = c*2;
         KEY[9] = d*2;
         
         buf2 = new LinkedList<byte[]>();
         AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE,8,1,true,true);
         SOURCE_DATA_LINE = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
         SOURCE_DATA_LINE.open(AUDIO_FORMAT,getToneArrayLength(SAMPLE_LENGTH)*BUFFER_SIZE);
         SOURCE_DATA_LINE.start();
         
         try
         {
             // open pi_1mil.txt; pin = pi in ;)
             BufferedReader pin = new BufferedReader( new FileReader("pi_1mil.txt") );
             
             Thread generatorThread = new Thread(new GeneratorThread(pin));
             Thread playerThread = new Thread(new PlayerThread());
             
             generatorThread.start();
             Thread.sleep(250);
             playerThread.start();
             
             /*BigInteger pi = new BigInteger(pin.readLine().replace(".",""));
             String piBase12 = pi.toString(12);
             System.out.println(piBase12);*/
         }
         catch(Exception ex) { ex.printStackTrace(); System.exit(1); }
     }
 }
