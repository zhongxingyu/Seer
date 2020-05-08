 /* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package com.fluendo.plugin;
 
 import java.util.*;
 import com.jcraft.jogg.*;
 import com.fluendo.jst.*;
 import com.fluendo.utils.*;
 
 public class OggDemux extends Element
 {
   private SyncState oy;
   private OggChain chain;
   private Page og;
   private Packet op;
   private static final byte[] signature = { 0x4f, 0x67, 0x67, 0x53 };
   private static final byte[] fishead_signature = { 0x66, 0x69, 0x73, 0x68, 0x65, 0x61, 0x64};
   private static final byte[] cmml_signature = {0x43, 0x4d, 0x4d, 0x4c};
 
   private static final int TYPE_NEW = 0;
   private static final int TYPE_UNKNOWN = 1;
   private static final int TYPE_SKELETON = 2;
   private static final int TYPE_CMML = 3;
   private static final int TYPE_MEDIA = 4;
 
   private OggPayload payloads[] = {
     new TheoraDec(),
     new VorbisDec(),
     new KateDec()
   };
 
   class OggStream extends Pad {
     public int serialno;
     public StreamState os;
     private Vector headers;
     private long baseTs;
     public boolean haveHeaders;
     public Vector queue;
     public boolean started;
     public boolean complete;
     public boolean discont;
     public boolean active;
     public boolean haveKeyframe;
     public boolean sentHeaders;
     public int type;
     public int lastRet;
     
     private OggPayload payload;
 
     public OggStream (int serial) {
       super (Pad.SRC, "serial_"+serial);
 
       serialno = serial;
       os = new StreamState();
       os.init(serial);
       os.reset();
       queue = new Vector();
       headers = new Vector();
       haveHeaders = false;
       haveKeyframe = false;
       payload = null;
       discont = true;
       complete = false;
       started = false;
       baseTs = -1;
       lastRet = OK;
     }
 
     public void markDiscont () {
       discont = true;
       complete = false;
       haveKeyframe = false;
       started = false;
     }
     public void reset () {
       markDiscont();
       os.reset();
       lastRet = OK;
     }
 
     public boolean isComplete () {
       return complete;
     }
     public void activate() {
       if (active)
         return;
 
       sentHeaders = false;
       lastRet = OK;
       addPad(this);
       active = true;
     }
     public void deActivate() {
       if (!active)
         return;
       removePad(this);
       pushEvent (Event.newEOS());	
       active = false;
     }
     public void reStart(long firstTs) {
       com.fluendo.jst.Buffer buf;
       long time;
 
       if (!active)
         return;
 
       if (baseTs == -1)
 	baseTs = firstTs;
 
       time = firstTs - baseTs;
 
       Debug.log(Debug.DEBUG, this+" pushing segment start "+firstTs+", time "+time);
       pushEvent (Event.newNewsegment (false, Format.TIME, firstTs, -1, time));
 
       if (!sentHeaders) {
         for (int i=0; i<headers.size(); i++) {
           buf = (com.fluendo.jst.Buffer) headers.elementAt(i);
           buf.setFlag (com.fluendo.jst.Buffer.FLAG_DISCONT, discont);
 	  discont = false;
 	  push (buf);
         }
 	sentHeaders = true;
       }
       for (int i=0; i<queue.size(); i++) {
         buf = (com.fluendo.jst.Buffer) queue.elementAt(i);
 	if (i == 0)
           Debug.log(Debug.DEBUG, this+" first data buffer: "+buf.timestamp);
         buf.setFlag (com.fluendo.jst.Buffer.FLAG_DISCONT, discont);
 	discont = false;
 	push (buf);
       }
       queue.setSize(0);
       started = true;
     }
 
     public long getFirstTs () {
       return payload.getFirstTs (queue);
     }
 
     private com.fluendo.jst.Buffer bufferFromPacket (Packet op)
     {
       com.fluendo.jst.Buffer data = com.fluendo.jst.Buffer.create();
 
       data.copyData(op.packet_base, op.packet, op.bytes);
       data.time_offset = op.granulepos;
       if (payload != null)
         data.timestamp = payload.granuleToTime (op.granulepos);
       else
         data.timestamp = -1;
       data.setFlag (com.fluendo.jst.Buffer.FLAG_DISCONT, discont);
       data.setFlag (com.fluendo.jst.Buffer.FLAG_DELTA_UNIT, !payload.isKeyFrame(op));
 
       return data;
     }
 
 
     /* initialize a stream based on the first packet */
     private void initNewStream (Packet op) {
       int i;
 
       payload = null;
       /* find out if it is a media payload */
       for (i=0; i<payloads.length; i++) {
 	OggPayload pl = payloads[i];
 
 	if (pl.isType (op)) {
           try {
 	    payload = (OggPayload) pl.getClass().newInstance();
             /* we have a valid media type */
             type = TYPE_MEDIA;
             /* set mime type */
             String mime = payload.getMime();
             Debug.log(Debug.INFO, "new stream "+serialno+", mime "+mime);
             setCaps (new Caps (mime));
 	    return;
 	  }
 	  catch (Exception e) {}
 	}
       }
       /* no payload, check for skeleton */
       if (MemUtils.startsWith (op.packet_base, op.packet, op.bytes, fishead_signature)) {
         type = TYPE_SKELETON;
         Debug.log(Debug.INFO, "ignoring skeleton stream "+serialno);
         postMessage (Message.newWarning (this, "ignoring skeleton stream "+serialno));
 	return;
       }
       /* check for cmml */
       if (MemUtils.startsWith (op.packet_base, op.packet, op.bytes, cmml_signature)) {
         type = TYPE_CMML;
         Debug.log(Debug.INFO, "ignoring CMML stream "+serialno);
         postMessage (Message.newWarning (this, "ignoring CMML stream "+serialno));
 	return;
       }
       /* else we don't know what it is */
       type = TYPE_UNKNOWN;
       Debug.log(Debug.INFO, "ignoring unknown stream "+serialno);
       postMessage (Message.newWarning (this, "ignoring unknown stream "+serialno));
     }
 
     public int pushPacket (Packet op) {
       /* new stream, find out what it is  */
       if (type == TYPE_NEW) {
 	initNewStream (op);
       }
       /* drop everything that is not recognized as media from here on. */
       if (type != TYPE_MEDIA) {
 	complete = true;
 	return Pad.OK;
       }
 
       /* first read all the headers */
       if (!haveHeaders) {
 	if (payload.isHeader(op)) {
           int result = payload.takeHeader (op);
           if (result < 0) {
             postMessage (Message.newError (this, "cannot read header"));
 	    return Pad.ERROR;
 	  }
           com.fluendo.jst.Buffer data = bufferFromPacket (op);
           headers.addElement(data);
           if (result > 0) {
             haveHeaders = true;
           }
         }
         else {
           haveHeaders = true;
         }
       }
       /* if we have all the headers we can stream */
       if (haveHeaders) {
         /* discontinuous codecs do not need to wait for data to allow playback */
         if (!complete && payload.isDiscontinuous()) {
           complete = true;
         }
         if (complete && started) {
           int ret;
           com.fluendo.jst.Buffer data = bufferFromPacket (op);
 	  ret = push (data);
 	  return combineFlows (this, ret);
 	}
         if (haveKeyframe || payload.isKeyFrame(op)) {
           com.fluendo.jst.Buffer data = bufferFromPacket (op);
 	  queue.addElement (data);
 	  haveKeyframe = true;
 	  if (op.granulepos != -1) {
 	    complete = true;
 	  }
 	}
       }
       return Pad.OK;
     }
 
     public int pushPage (Page og) {
       int res;
       int flowRet = Pad.OK;
 
       res = os.pagein(og);
       if (res < 0) {
         // error; stream version mismatch perhaps
         System.err.println("Error reading first page of Ogg bitstream data.");
         postMessage (Message.newError (this, "Error reading first page of Ogg bitstream data."));
         return ERROR;
       }
       while (flowRet == OK) {
 	res = os.packetout(op);
         if(res == 0)
 	  break; // need more data
         if(res == -1) { 
 	  // missing or corrupt data at this page position
           // no reason to complain; already complained above
 	  Debug.log(Debug.WARNING, "ogg error: packetout gave "+res);
 	  discont = true;
         }
         else {
 	  flowRet = pushPacket(op);
         }
       }
       return flowRet;
     }
     protected boolean eventFunc (com.fluendo.jst.Event event) {
       return sinkPad.pushEvent (event);
     }
   }
 
   class OggChain {
     private Vector streams;
     private boolean active;
     private boolean synced;
     private long firstTs;
 
     public OggChain () {
       streams = new Vector();
       synced = false;
       active = false;
       firstTs = -1;
     }
 
     public boolean isActive() {
       return active;
     }
 
     public void activate() {
       if (active)
         return;
 
       Debug.log(Debug.DEBUG, "activating chain");
       for (int i=0; i<streams.size(); i++) {
         OggStream stream = (OggStream) streams.elementAt(i);
 	stream.activate();
       }
       active = true;
       noMorePads();
     }
     public void deActivate() {
       if (!active)
         return;
       Debug.log(Debug.DEBUG, "deActivating chain");
       for (int i=0; i<streams.size(); i++) {
         OggStream stream = (OggStream) streams.elementAt(i);
 	stream.deActivate();
       }
       active = false;
     }
     public void reStart() {
       if (!active)
         return;
 
       if (firstTs == -1) {
         long maxTs = 0;
         long minTs = Long.MAX_VALUE;
         /* collect first timestamp */
         for (int i=0; i<streams.size(); i++) {
           OggStream stream = (OggStream) streams.elementAt(i);
 
 	  /* skip all streams not recognized as media streams */
 	  if (stream.type != TYPE_MEDIA)
 	    continue;
 
 	  long ts = stream.getFirstTs();
 	  maxTs = Math.max (maxTs, ts);
 	  minTs = Math.min (minTs, ts);
         }
 	firstTs = maxTs;
       }
       for (int i=0; i<streams.size(); i++) {
         OggStream stream = (OggStream) streams.elementAt(i);
 	stream.reStart(firstTs);
       }
     }
 
 
     public void addStream (OggStream stream) {
       streams.addElement (stream);
     }
 
     public void markDiscont () {
       synced = false;
       firstTs = -1;
       for (int i=0; i<streams.size(); i++) {
 	OggStream stream = (OggStream) streams.elementAt(i);
 	stream.markDiscont();
       }
     }
 
     public OggStream findStream (int serial) {
       OggStream stream = null;
       for (int i=0; i<streams.size(); i++) {
         stream = (OggStream) streams.elementAt(i);
         if (stream.serialno == serial)
           break;
         stream = null;
       }
       return stream;
     }
     public void resetStreams ()
     {
       for (int i=0; i<streams.size(); i++) {
 	OggStream stream = (OggStream) streams.elementAt(i);
 	stream.reset();
       }
     }
     public boolean forwardEvent (com.fluendo.jst.Event event)
     {
       for (int i=0; i<streams.size(); i++) {
 	OggStream stream = (OggStream) streams.elementAt(i);
 	stream.pushEvent (event);
       }
       return true;
     }
 
     public int pushPage (Page og, OggStream stream) {
       int flowRet = Pad.OK;
 
 	flowRet = stream.pushPage (og);
 
       /* now check if all streams are Synced */
       if (!synced) {
         boolean check = true;
 	boolean hasMedia = false;
         for (int i=0; i<streams.size(); i++) {
 	  OggStream cstream = (OggStream) streams.elementAt(i);
 
	  if (cstream.type == TYPE_MEDIA) {
 	    hasMedia = true;
	    if (!(check = cstream.isComplete()))
	      break;
          }
         }
 	/* isComplete check do not work for annodex file right now, cause we don't parse
 	 * the annodex headers properly at this moment. So we shouldn't consider all
 	 * streams are synced unless we have at least one media stream which in turn
 	 * will ensure that all media streams are in sync. */
 	if (check && hasMedia) {
           Debug.log(Debug.DEBUG, "streams synced");
 	  activate();
 	  reStart();
 	  synced = true;
 	}
       }
       return flowRet;
     }
   }
 
   private Pad sinkPad = new Pad(Pad.SINK, "sink") {
     protected boolean eventFunc (com.fluendo.jst.Event event)
     {
       switch (event.getType()) {
         case Event.FLUSH_START:
           if (chain != null)
 	    chain.forwardEvent (event);
 	  synchronized (streamLock) {
             Debug.log(Debug.DEBUG, this+" synced");
 	  }
 	  break;
         case Event.FLUSH_STOP:
 	  oy.reset();
           if (chain != null) {
 	    chain.resetStreams();
 	    chain.forwardEvent (event);
 	  }
 	  break;
         case Event.NEWSEGMENT:
 	  break;
         case Event.EOS:
 	  Debug.log(Debug.INFO, "ogg: got EOS");
           if (chain != null)
 	    chain.forwardEvent (event);
 	  else
             postMessage (Message.newError (this, "unsupported media type"));
 	  break;
         default:
           if (chain != null)
 	    chain.forwardEvent (event);
 	  break;
       }
       return true;
     }
     protected int chainFunc (com.fluendo.jst.Buffer buf)
     {
       int res;
       int flowRet = OK;
 
       int index = oy.buffer(buf.length);
 
       if (buf.isFlagSet (com.fluendo.jst.Buffer.FLAG_DISCONT)) {
 	Debug.log(Debug.INFO, "ogg: got discont");
 	if (chain != null) {
 	  chain.markDiscont ();
 	}
       }
 
       System.arraycopy(buf.data, buf.offset, oy.data, index, buf.length);
       oy.wrote(buf.length);
   
       // Loop over all pages in the buffer
       while (flowRet == OK) {
         res = oy.pageout(og);
         if (res == 0)
 	  break; // need more data
         if(res == -1) { 
 	  // missing or corrupt data at this page position
           // no reason to complain; already complained above
 	  Debug.log(Debug.WARNING, "ogg: pageout gave "+res);
 	  if (chain != null) {
 	    chain.markDiscont ();
 	  }
         }
         else {
 	  // Find the stream associated with this page
 	  int serial = og.serialno();
 	  OggStream stream = null;
 	  if (chain != null) {
 	    stream = chain.findStream (serial);
 	  }
 	  if (stream == null) {
 	    // No stream for this serial
 	    // The Ogg spec says that the bos pages for all the streams in a chain
 	    // will come before the remaining stream data for any stream. The chain
 	    // is activated once the non-header pages start arriving. So if a new
 	    // serial comes in when the chain is active, that means it must be the 
 	    // start of a new chain.
 	    if (chain != null) {
 	      if (chain.isActive()) {
 	        chain.deActivate();
 	        chain = null;
 	      }
 	    }
             if (chain == null)
 	      chain = new OggChain();
 
 	    // Create the new stream
 	    stream = new OggStream(serial);
 	    chain.addStream (stream);
 	  }
 	  flowRet = chain.pushPage (og, stream);
         }
       }
       return flowRet;
     }
     protected boolean activateFunc (int mode) 
     {
       if (mode == MODE_NONE) {
 	synchronized (sinkPad) {
 	  oy.reset();
 	  if (chain != null) {
 	    chain.deActivate();
 	    chain = null;
 	  }
 	}
       }
       return true;
     }
   };
 
   private int combineFlows (OggStream stream, int ret) {
     /* store the value */
     stream.lastRet = ret;
 
     /* if it's success we can return the value right away */
     if (Pad.isFlowSuccess (ret))
       return ret;
 
     /* any other error that is not-linked can be returned right
      * away */
     if (ret != Pad.NOT_LINKED)
       return ret;
 
     /* only return NOT_LINKED if all other pads returned NOT_LINKED */
     if (chain != null) {
       for (int i=0; i<chain.streams.size(); i++) {
 	OggStream ostream = (OggStream) chain.streams.elementAt(i);
 
         ret = ostream.lastRet;
         /* some other return value (must be SUCCESS but we can return
          * other values as well) */
         if (ret != Pad.NOT_LINKED)
           return ret;
       }
       /* if we get here, all other pads were unlinked and we return
        * NOT_LINKED then */
     }
     return ret;
   }
 
 
   public String getFactoryName ()
   {
     return "oggdemux";
   }
   public String getMime ()
   {
     return "application/ogg";
   }
   public int typeFind (byte[] data, int offset, int length)
   {
     if (MemUtils.startsWith (data, offset, length, signature))
       return 10;
 
     return -1;
   }
 
   public OggDemux () {
     super ();
 
     oy = new SyncState();
     og = new Page();
     op = new Packet();
 
     chain = null;
 
     addPad (sinkPad);
   }
 }
