 // Please note much of the code in this program was taken from the DSD software
 // and converted into Java. The author of this software is unknown but has the
 // GPG Key ID below
 
 // Copyright (C) 2010 DSD Author
 // GPG Key ID: 0x3F1D7FD0 (74EF 430D F7F2 0A48 FCE6  F630 FAA2 635D 3F1D 7FD0)
 // 
 // Permission to use, copy, modify, and/or distribute this software for any
 // purpose with or without fee is hereby granted, provided that the above
 // copyright notice and this permission notice appear in all copies.
 //
 // THE SOFTWARE IS PROVIDED "AS IS" AND ISC DISCLAIMS ALL WARRANTIES WITH
 // REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
 // AND FITNESS.  IN NO EVENT SHALL ISC BE LIABLE FOR ANY SPECIAL, DIRECT,
 // INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 // LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE
 // OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 // PERFORMANCE OF THIS SOFTWARE.
 //
 
 package com.dmr;
 
 import java.awt.*;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowAdapter;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import javax.swing.*;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.Element;
 import javax.swing.text.html.HTML;
 import javax.swing.text.StyleConstants;
 import javax.swing.JEditorPane;
 import java.text.DateFormat;
 import java.util.Date;
 
 public class DMRDecode {
 	private DisplayModel display_model;
 	private DisplayView display_view;
 	private static DMRDecode theApp;
 	static DisplayFrame window;
 	public String program_version="DMR Decoder V0.00 Build 9";
 	public int vertical_scrollbar_value=0;
 	public int horizontal_scrollbar_value=0;
 	private static boolean RUNNING=true;
 	private static final int SAMPLESPERSYMBOL=10;
 	private int jitter=-1;
 	private static final int SYMBOLCENTRE=4;
 	private static final int MAXSTARTVALUE=15000;
 	private static final int MINSTARTVALUE=-15000;
 	private int max=MAXSTARTVALUE;
 	private int min=MINSTARTVALUE;
 	private int centre=0;
 	private int lastsample=0;
 	private int maxref=12000;
 	private int minref=-12000;
 	private int lastsynctype=-1;
 	private int symbolcnt=0;
 	private static final int DMR_DATA_SYNC[]={3,1,3,3,3,3,1,1,1,3,3,1,1,3,1,1,3,1,3,3,1,1,3,1};
 	private static final int DMR_VOICE_SYNC[]={1,3,1,1,1,1,3,3,3,1,1,3,3,1,3,3,1,3,1,1,3,3,1,3};
 	private boolean carrier=false;
 	public boolean inverted=true;
 	private boolean firstframe=false;
 	public JEditorPane editorPane;
 	public HTMLDocument doc;
 	public Element el;
 	private int lmid=0;
 	private int umid=0;
 	private int synctype;
 	private BufferedReader br;
 	private int dibit_buf[]=new int[144];
 	private boolean frameSync=false;
 	public boolean saveToFile=false;
 	public FileWriter file;
 	public boolean logging=false;
 	public boolean pReady=false;
 	private boolean audioSuck=false;
 	private int symbolBuffer[]=new int[24];
 	public AudioInThread lineInThread=new AudioInThread(this);
 	private boolean debug=false;
 	private boolean viewVoiceFrames=true;
 	private boolean viewDataFrames=true;
 	private boolean viewEmbeddedFrames=true;
 	public int frameCount=0;
 	public int badFrameCount=0;
 	public ShortLC short_lc=new ShortLC();
 	public int embeddedFrameCount=0;
 
 	public static void main(String[] args) {
 		theApp=new DMRDecode();
 		SwingUtilities.invokeLater(new Runnable(){public void run(){theApp.createGUI();}});
 		// If sucking in test data then open the file
 		if (theApp.audioSuck==true) theApp.prepareAudioSuck("aor3000_audiodump.csv");
 		 else theApp.lineInThread.startAudio();
 		// The main routine
 		while (RUNNING)	{
 			if ((theApp.lineInThread.getAudioReady()==true)&&(theApp.pReady==true)) theApp.decode();
 		}
 
 		}
 	
 	// Setup the window //
 	public void createGUI() {
 		window=new DisplayFrame(program_version,this);	
 		Toolkit theKit=window.getToolkit();
 		Dimension wndsize=theKit.getScreenSize();
 		window.setBounds(wndsize.width/6,wndsize.height/6,2*wndsize.width/3,2*wndsize.height/3);
 		window.addWindowListener(new WindowHandler());
 		display_model=new DisplayModel();
 		editorPane=new JEditorPane();
 		editorPane.setContentType("text/html");
 		editorPane.setEditable(false);
 		editorPane.setText("<html><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"></table></html>");
 	    doc=(HTMLDocument)editorPane.getDocument();
 		el=doc.getElement(doc.getDefaultRootElement(),StyleConstants.NameAttribute,HTML.Tag.TABLE);
 		JScrollPane scrollPane=new JScrollPane(editorPane);
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		window.getContentPane().add(scrollPane,BorderLayout.CENTER);
 		window.setVisible(true);
 		// Make certain the program knows the GUI is ready
 		pReady=true;
 		}
 
 	class WindowHandler extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {	
 			}
 		}
 
 	public DisplayFrame getWindow()	{
 		return window;	
 		}
 
 	public DisplayModel getModel() {
 		return display_model;
 		}
 
 	public DisplayView getView() {
 		return display_view;	
 		}
 	
   
 	// The main routine for decoding DMR data
 	public void decode()	{
 		  noCarrier();
 		  synctype=getFrameSync();
 		  calcMids();
 	      while (synctype!=-1)	{
 	          processFrame();
 	          synctype=getFrameSync(); 
 	        }  
 	  }
 	
 	// Calculate the waveform centre and mid points
 	public void calcMids()	{
 			centre=(max+min)/2;
 			umid=((max-centre)*5/8)+centre;
 		    lmid=((min-centre)*5/8)+centre;		
 	}
 	
 	// A function containing the calculations required when a frame is detected
 	private void frameCalcs (int lmin,int lmax)	{
 		// The code required below appears to depend on the soundcard
 		// Viglen PC code
 		//max=(lmax+max)/2;
 		//min=(lmin+min)/2;	
 		// Acer PC Code 
 		max=lmax;
 		min=lmin;
 		///////////////////
 		maxref=max;
 		minref=min;
 	}
 	
 	
 	// This code lifted straight from the DSD source code converted to Java and tidied up removing non DMR code
 	public int getSymbol(boolean have_sync)	{
 		  int sample,i,sum=0,symbol,count=0;
 		  for (i=0;i<SAMPLESPERSYMBOL;i++)	{
 		      if ((i==0)&&(have_sync==false))	{
 		        if ((jitter>0)&&(jitter<=SYMBOLCENTRE)) i--;          
 		         else if ((jitter>SYMBOLCENTRE)&&(jitter<SAMPLESPERSYMBOL)) i++;          
 		        jitter=-1;
 		       }
 			  if (audioSuck==false)	{ 
 				  // Loop until a sample is ready
 				  while (lineInThread.sampleReady()==false)	{}
 				  // Get the sample from the sound card via the sound thread
 				  sample=lineInThread.returnSample();
 			  }
 			  else	{
 				  // Get the data from the suck file
 				  int fsample=getSuckData();
 				  // Push this through a root raised filter
 				  sample=lineInThread.rootRaisedFilter(fsample);
 			  }
 			  if ((sample>max)&&(have_sync==true)) sample=max;  
 			    else if ((sample<min)&&(have_sync==true)) sample=min;
 		      if (sample>centre)	{
 		    	  if ((jitter<0)&&(lastsample<centre)&&(sample<(maxref*1.25))) jitter=i;   
 		        }
 		      else if ((sample>(minref*1.25))&&(jitter<0)&&(lastsample>centre)) jitter=i;
       
 		      if ((i>=SYMBOLCENTRE-1)&&(i<=SYMBOLCENTRE+2)) {
 		    	  sum=sum+sample;
 		          count++;
 		          }
 		      lastsample=sample;
 		    }
 		  symbol=(sum/count);
 		  symbolcnt++;		  
 		  return symbol;
 	  }
 	  
 
 	// Grab either 24 or 144 dibits depending on if you have sync
 	// Check if they have a sync pattern and if they do then process them accordingly
 	public int getFrameSync ()	{
 		int i,t=0,dibit,symbol,synctest_pos=0;
 		int lmin=0,lmax=0;
 		int lbufCount;
 		boolean dataSync=false,voiceSync=false;
 		Quicksort qsort=new Quicksort();
 		// Clear the symbol counter
 		symbolcnt=0;
 		// Buffer size
 		if (frameSync==true) lbufCount=144;
 		 else lbufCount=23;
 		
 		while (true) {
 			t++;
 			// Get a symbol from the soundcard
 			symbol=getSymbol(frameSync);
 			// Store this in the rotating symbol buffer
 			// Only needed if we don't have frame sync
 			if (frameSync==false) addToSymbolBuffer(symbol);
 			// Set the dibit state
 			dibit=symboltoDibit(symbol);
 			// Add the dibit to the rotating dibit buffer
 			addToDitbitBuf(dibit,frameSync);
 		    // If we have received either 24 or 144 dibits (depending if we have sync)
 			// then check for a valid sync sequence
 			if (t>=lbufCount) {
 				// If we don't have frame sync then rotate the symbol buffer
 				// and also find the new minimum and maximum
 				if (frameSync==false)	{
 					int lbuf2[]=new int[24];
 					for (i=0;i<24;i++) {
 						lbuf2[i]=symbolBuffer[i];
 					}
 					qsort.sort(lbuf2);
 					lmin=(lbuf2[2]+lbuf2[3]+lbuf2[4])/3;
 					lmax=(lbuf2[21]+lbuf2[20]+lbuf2[19])/3;
 					maxref=max;
 					minref=min;
 				}
 				
 				// Check if a frame has a voice or data sync
 				// If no frame sync do this at any time but if we do have
 				// frame sync then only do this every 144 bits
 				if ((frameSync==false)||((frameSync==true)&&(symbolcnt%144==0)))	{
 					// Look for a voice frame
 					voiceSync=syncCompare(DMR_VOICE_SYNC,frameSync);
 					// Look for a data frame
 					if (voiceSync==false) dataSync=syncCompare(DMR_DATA_SYNC,frameSync);
 					else dataSync=false;
 					// Embedded signalling frame
 					if ((frameSync==true)&&(voiceSync==false)&&(dataSync==false)&&(firstframe==false)&&(embeddedFrameCount<7))	{
 						// Increment the embedded frame counter
 						embeddedFrameCount++;
 						lastsynctype=13;
 						return (13);
 					}					
 					// Data frame
 					if (dataSync==true) {
 						// Clear the embedded frame counter
 						embeddedFrameCount=0;
 						carrier=true;
 						if (frameSync==false)	{
 							frameCalcs(lmin,lmax);
 							frameSync=true;
 						}
 						if (lastsynctype==-1) firstframe=true;
 						else firstframe=false;
 						lastsynctype=10;
 						return (10);
 					}
 					// Voice frame
 					if (voiceSync==true) {
 						// Clear the embedded frame counter
 						embeddedFrameCount=0;
 						carrier=true;
 						if (frameSync==false)	{
 							frameCalcs(lmin,lmax);
 							frameSync=true;
 						}
 						if (lastsynctype==-1) firstframe=true;
 						else firstframe=false;
 						lastsynctype=12;
 						return (12);
 					}
 				
 				}
 		}					
 		// We had a signal but appear to have lost it
 		if (carrier==true) {
 			// If we have missed 12 frames then something is wrong
 			if (synctest_pos>=1728) {
 				// If in debug mode show that sync has been lost
 				if (debug==true)	{
 					String l=getTimeStamp()+" Sync Lost";
 					l=l+" : centre="+Integer.toString(centre)+" jitter="+Integer.toString(jitter);
 					l=l+" max="+Integer.toString(max)+" min="+Integer.toString(min)+" umid="+Integer.toString(umid)+" lmid="+Integer.toString(lmid);
 					addLine(l);
 					fileWrite(l);
 				}
 				frameSync=false;
 				noCarrier();
 				return (-1);
 				}
 			}
 		// If the hunt has gone on for a while then reset everything
 		if (t>32000) {
 			t=0;
 			synctest_pos=0;
 			}
 		else synctest_pos++;
 		}
 	  }
 	  
 	// Add a dibit to the dibit buffer
 	void addToDitbitBuf (int dibit,boolean sync)	{
 		int a,max;
 		if (sync==false) max=23;
 		 else max=143;
 		// Rotate the dibit buffer to the left
 		for (a=0;a<max;a++)	{
 			dibit_buf[a]=dibit_buf[a+1];
 		}
 		dibit_buf[max]=dibit;
 	}
 	
 	// Add a symbol to the symbol buffer
 	void addToSymbolBuffer (int symbol)	{
 		int a;
 		for (a=0;a<23;a++)	{
 			symbolBuffer[a]=symbolBuffer[a+1];
 		}
 		symbolBuffer[23]=symbol;
 	}
 	
 	// No carrier or carrier lost so clear the variables
 	public void noCarrier ()	{
 		jitter=-1;
 		lastsynctype=-1;
 		carrier=false;
 		max=MAXSTARTVALUE;
 		min=MINSTARTVALUE;
 		centre=0;
 		firstframe=false;
 	  	}
 	
 	// Given a symbol return a dibit
 	int symboltoDibit (int symbol)	{
 		// With Sync
 		if (frameSync==true)	{
 			if (inverted==false)	{
 				// Normal
 				if (symbol>centre) {
 					if (symbol>umid) return 1;
 					else return 0;
 				}
 				else {
 					if (symbol<lmid) return 3;
 					else return 2;
 				}
 			} else	{	
 				// Inverted
 				if (symbol>centre) {
 					if (symbol>umid) return 3;
 					else return 2;
 				}
 				else {
 					if (symbol<lmid) return 1;
 					else return 0;
 				}
 			}
 		} else	{
 				// No Sync
 				// Normal
 				if (inverted==false)	{
 					if (symbol>0) return 1;
 					else return 3;
 				}
 				// Inverted
 				else	{
 					if (symbol>0) return 3;
 					else return 1;
 				}
 			}
 	}
 	  
 	// Compare the sync sequence held in an array with the contents of the dibit_buf passed
 	public boolean syncCompare(int c[],boolean sync)	{
		int i,offset,same=0,diff;
 		if (sync==true) offset=66;
 		 else offset=0;
 		for (i=0;i<24;i++)	{
 			if (dibit_buf[i+offset]==c[i]) same++;
 		}
		// Allow 1 dibit to be incorrect when syncronised 
		if (sync==true) diff=1;
		else diff=0;
		if ((c.length-same)>diff) return false;
 		else return true;
 	}
 	  
 	// Adds a line to the display
 	public void addLine(String line) {
 		  try {
 			  doc.insertAfterStart(el,"<tr>"+line +"</tr>");
 		  }
 		  catch (Exception e) {
 			  System.out.println("Exception:" + e.getMessage());
 		  }		
 	}
 
 	// Return a time stamp
 	public String getTimeStamp() {
 		Date now=new Date();
 		DateFormat df=DateFormat.getTimeInstance();
 		return df.format(now);
 	}	
 	
 	// Handle an incoming DMR Frame
 	void processFrame ()	{
 		String l;
 	    maxref=max;
 	    minref=min;
 	    if (firstframe==true)	{	
 	    	// As we now have sync then skip the next 54 dibits as we can't do anything with them
 			skipDibit(54);			
 			//audioDump();
 			if (debug==true)	{
 				if (synctype==12) l=getTimeStamp()+" DMR Voice Sync Acquired";
 				else l=getTimeStamp()+" DMR Data Sync Acquired";
 				l=l+" : centre="+Integer.toString(centre)+" jitter="+Integer.toString(jitter);
 				l=l+" max="+Integer.toString(max)+" min="+Integer.toString(min)+" umid="+Integer.toString(umid)+" lmid="+Integer.toString(lmid);
 				addLine(l);
 				fileWrite(l);
 			}
 			return;
 	    }
 	    if ((synctype==12)&&(viewVoiceFrames==true)) processDMRvoice ();
 	    else if ((synctype==10)&&(viewDataFrames==true)) processDMRdata ();
 	    else if ((synctype==13)&&(viewEmbeddedFrames==true)) processEmbedded ();
 	}
 
 	// Handle a DMR Voice Frame
 	void processDMRvoice ()	{	
 		DMRVoice DMRvoice=new DMRVoice();
 		String line[]=new String[10];
 		line=DMRvoice.decode(theApp,dibit_buf);
 		line[0]=line[0]+dispSymbolsSinceLastFrame();
 		if (debug==true)	{
 			line[8]=returnDibitBufferPercentages();
 			line[9]=displayDibitBuffer();
 		}
 		frameCount++;
 		if (DMRvoice.isError()==false)	{
 			badFrameCount++;
 			line[0]=getTimeStamp()+" DMR Voice Frame - Error ! ";
 			line[0]=line[0]+dispSymbolsSinceLastFrame();	
 		}
 		displayLines(line);
 	}
 	
 	// Handle a DMR Data Frame
 	void processDMRdata ()	{
 		DMRDataDecode DMRdata=new DMRDataDecode();
 		String line[]=new String[10];
 		line=DMRdata.decode(theApp,dibit_buf);
 		line[0]=line[0]+dispSymbolsSinceLastFrame();
 		if (debug==true)	{
 			line[8]=returnDibitBufferPercentages();
 			line[9]=displayDibitBuffer();
 		}
 		frameCount++;
 		if (DMRdata.isError()==false)	{
 			badFrameCount++;
 			line[0]=getTimeStamp()+" DMR Data Frame - Error ! ";
 			line[1]=null;
 			line[2]=null;
 			line[0]=line[0]+dispSymbolsSinceLastFrame();	
 			int gval=DMRdata.getGolayValue();
 			if (gval!=-1) line[0]=line[0]+" ("+Integer.toString(gval)+")";
 		}
 		displayLines(line);
 	}
 	
 	// Handle an embedded frame
 	void processEmbedded ()	{
 		DMREmbedded DMRembedded=new DMREmbedded();
 		String line[]=new String[10];
 		line=DMRembedded.decode(theApp,dibit_buf);
 		line[0]=line[0]+dispSymbolsSinceLastFrame();
 		if (debug==true)	{
 			line[8]=returnDibitBufferPercentages();
 			line[9]=displayDibitBuffer();
 		}
 		frameCount++;
 		if (DMRembedded.isError()==false)	{
 			badFrameCount++;
 			line[0]=getTimeStamp()+" DMR Embedded Frame - Error ! ";
 			line[0]=line[0]+dispSymbolsSinceLastFrame();	
 		}
 		else	{
 			// Set last sync type to 14 to show this was a good embedded frame
 			lastsynctype=14;
 		}
 		displayLines(line);
 	}
 
 	// Display a group of lines
 	void displayLines (String line[])	{
 		int a;
 		int len=line.length;
 		for (a=(len-1);a>=0;a--)	{
 			if (line[a]!=null) addLine(line[a]);
 		}
 		// Log to disk if needed
 		if (logging==true)	{
 			for (a=0;a<len;a++)	{
 				if (line[a]!=null) fileWrite(line[a]);
 			}
 		}
 	}
 	
 	// Write to a string to the logging file
 	public boolean fileWrite(String fline) {
 		// Add a CR to the end of each line
 		fline=fline+"\r\n";
 		// If we aren't logging don't try to do anything
 		if (logging==false)
 			return false;
 		try {
 			file.write(fline);
 			file.flush();
 		} catch (Exception e) {
 			// Stop logging as we have a problem
 			logging=false;
 			System.out.println("\nError writing to the logging file");
 			return false;
 		}
 		return true;
 	}
 	
 	// Display the number of symbols since the last frame with a valid sync
 	public String dispSymbolsSinceLastFrame ()	{
 		String l=" (Symbols="+Integer.toString(symbolcnt)+")";
 		return l;
 	}
 	
 	// Grab 5 seconds worth of audio and write to the file "audiodump_out.csv"
 	public void audioDump ()	{
 		long a;
 		final long sample_max=48000*5;
 		int samples[]=new int[48000*5];
 		for (a=0;a<sample_max;a++)	{
 			samples[(int)a]=lineInThread.returnSample();
 		}	
 	    try	{
 	    	FileWriter dfile=new FileWriter("audiodump_out.csv");
 			for (a=0;a<sample_max;a++)	{
 				dfile.write(Integer.toString(samples[(int)a]));
 				dfile.write("\r\n");
 			}
 	    	dfile.flush();  
 	    	dfile.close();
 	    	}catch (Exception e)	{
 	    		System.err.println("Error: " + e.getMessage());
 	    		}
 	    // Saved everything so shut down the program
 	    System.exit(0);
 		}
 	
 	// Write a line to the debug file
 	public void debugDump (String line)	{
 	    try	{
 	    	FileWriter dfile=new FileWriter("debug.csv",true);
 	    	dfile.write(line);
 	    	dfile.write("\r\n");
 	    	dfile.flush();  
 	    	dfile.close();
 	    	}catch (Exception e)	{
 	    		System.err.println("Error: " + e.getMessage());
 	    		}
 		}
 		
 	// Display the dibit buffer as a string
 	public String displayDibitBuffer ()	{
 		String lb="";
 		int a;
 		for (a=0;a<144;a++)	{
 			lb=lb+Integer.toString(dibit_buf[a]);
 		}
 		return lb;
 	}
 	
 	// Grab a certain number of symbols but ignore their content
 	public void skipDibit (int count)
 	{
 	  int i,r;
 	  for (i=0;i<count;i++)
 	    {
 		r=getSymbol(true);
 		addToDitbitBuf(r,true);
 	    }
 	}
 	
 	// Return a string showing the percentages of each dibit in the dibit buffer
 	public String returnDibitBufferPercentages ()	{
 		String dline;
 		int a,c0=0,c1=0,c2=0,c3=0;
 		for (a=0;a<144;a++)	{
 			// Exclude the sync burst from the percentages 
 			if ((a<66)||(a>89))	{
 			if (dibit_buf[a]==0) c0++;
 			if (dibit_buf[a]==1) c1++;
 			if (dibit_buf[a]==2) c2++;
 			if (dibit_buf[a]==3) c3++;
 			}
 		}
 		c0=(int)(((float)c0/(float)120.0)*(float)100);
 		c1=(int)(((float)c1/(float)120.0)*(float)100);
 		c2=(int)(((float)c2/(float)120.0)*(float)100);
 		c3=(int)(((float)c3/(float)120.0)*(float)100);
 		// Write this to a line
 		dline="Dibit 0="+Integer.toString(c0)+"% ";	
 		dline=dline+"Dibit 1="+Integer.toString(c1)+"% ";	
 		dline=dline+"Dibit 2="+Integer.toString(c2)+"% ";	
 		dline=dline+"Dibit 3="+Integer.toString(c3)+"% ";	
 		return dline;
 	}
 	
 	// Open a file which contains data that can be sucked in
 	public void prepareAudioSuck (String fn)	{
 		try	{
 			br=new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
 		} catch (Exception e)	{
 			e.printStackTrace();
 			audioSuck=false;
 		}
 		audioSuck=true;
 	}
 
 	// Read in a line from the suck file and return the int it contains
 	private int getSuckData ()	{
 		int data=0;
 		String line;
 		try	{
 			line=br.readLine();
 			data=Integer.parseInt(line);
 		} catch (Exception e)	{
 			// We have a problem so stop sucking
 			audioSuck=false;
 		}
 		return data;
 	}
 
 	public void setViewVoiceFrames(boolean viewVoiceFrames) {
 		this.viewVoiceFrames=viewVoiceFrames;
 	}
 
 	public boolean isViewVoiceFrames() {
 		return viewVoiceFrames;
 	}
 		
 	public boolean isDebug() {
 		return debug;
 	}
 
 	public void setDebug(boolean debug) {
 		this.debug=debug;
 	}
 
 	public void setViewDataFrames(boolean viewDataFrames) {
 		this.viewDataFrames = viewDataFrames;
 	}
 
 	public boolean isViewDataFrames() {
 		return viewDataFrames;
 	}
 
 	public void setViewEmbeddedFrames(boolean viewEmbeddedFrames) {
 		this.viewEmbeddedFrames = viewEmbeddedFrames;
 	}
 
 	public boolean isViewEmbeddedFrames() {
 		return viewEmbeddedFrames;
 	}
 	
 
 
 
 	
 }
