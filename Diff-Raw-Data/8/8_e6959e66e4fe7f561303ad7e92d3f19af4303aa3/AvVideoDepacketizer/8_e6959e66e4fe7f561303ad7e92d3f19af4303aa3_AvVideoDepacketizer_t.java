 package com.limelight.nvstream.av.video;
 
 import java.util.LinkedList;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.limelight.nvstream.av.AvByteBufferDescriptor;
 import com.limelight.nvstream.av.AvByteBufferPool;
 import com.limelight.nvstream.av.AvDecodeUnit;
 import com.limelight.nvstream.av.AvRtpPacket;
 
 import android.media.MediaCodec;
 
 public class AvVideoDepacketizer {
 	
 	// Current NAL state
 	private LinkedList<AvByteBufferDescriptor> avcNalDataChain = new LinkedList<AvByteBufferDescriptor>();
 	private int avcNalDataLength = 0;
 	private int currentlyDecoding;
 	
 	// Sequencing state
 	private short lastSequenceNumber;
 	
 	private LinkedBlockingQueue<AvDecodeUnit> decodedUnits = new LinkedBlockingQueue<AvDecodeUnit>();
 	
 	private AvByteBufferPool bbPool = new AvByteBufferPool(1500);
 	
 	public byte[] allocatePacketBuffer()
 	{
 		return bbPool.allocate();
 	}
 	
 	public void trim()
 	{
 		bbPool.purge();
 	}
 	
 	private void clearAvcNalState()
 	{
 		for (AvByteBufferDescriptor avbb : avcNalDataChain)
 		{
 			AvVideoPacket packet = (AvVideoPacket) avbb.context;
 			
 			if (packet.release() == 0) {
 				bbPool.free(avbb.data);
 				packet.free();
 			}
 			
 			avbb.free();
 		}
 		
 		avcNalDataChain.clear();
 		avcNalDataLength = 0;
 	}
 	
 	public void releaseDecodeUnit(AvDecodeUnit decodeUnit)
 	{
 		// Remove the reference from each AvVideoPacket (freeing if okay)
 		for (AvByteBufferDescriptor buff : decodeUnit.getBufferList())
 		{
 			AvVideoPacket packet = (AvVideoPacket) buff.context;
 			
 			if (packet.release() == 0) {
 				bbPool.free(buff.data);
 				packet.free();
 			}
 			
 			buff.free();
 		}
 		
 		decodeUnit.free();
 	}
 	
 	private void reassembleAvcNal()
 	{
 		// This is the start of a new NAL
 		if (!avcNalDataChain.isEmpty() && avcNalDataLength != 0)
 		{
 			int flags = 0;
 			
 			// Check if this is a special NAL unit
 			AvByteBufferDescriptor header = avcNalDataChain.getFirst();
 			AvByteBufferDescriptor specialSeq = NAL.getSpecialSequenceDescriptor(header);
 			
 			if (specialSeq != null)
 			{
 				// The next byte after the special sequence is the NAL header
 				byte nalHeader = specialSeq.data[specialSeq.offset+specialSeq.length];
 				
 				switch (nalHeader)
 				{
 				// SPS and PPS
 				case 0x67:
 				case 0x68:
 					System.out.println("Codec config");
 					flags |= MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
 					break;
 					
 				// IDR
 				case 0x65:
 					System.out.println("Reference frame");
 					flags |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
 					break;
 					
 				// non-IDR frame
 				case 0x61:
 					break;
 					
 				// Unknown type
 				default:
 					System.out.printf("Unknown NAL header: %02x %02x %02x %02x %02x\n",
 						header.data[header.offset], header.data[header.offset+1],
 						header.data[header.offset+2], header.data[header.offset+3],
 						header.data[header.offset+4]);
 					break;
 				}
 				
 				specialSeq.free();
 			}
 			else
 			{
 				System.out.printf("Invalid NAL: %02x %02x %02x %02x %02x\n",
 						header.data[header.offset], header.data[header.offset+1],
 						header.data[header.offset+2], header.data[header.offset+3],
 						header.data[header.offset+4]);
 			}
 
 			// Construct the H264 decode unit
 			AvDecodeUnit du = AvDecodeUnit.newDecodeUnit(AvDecodeUnit.TYPE_H264, new LinkedList<AvByteBufferDescriptor>(avcNalDataChain), avcNalDataLength, flags);
 			if (!decodedUnits.offer(du))
 			{
 				releaseDecodeUnit(du);
 			}
 			
 			// Clear old state
 			avcNalDataChain.clear();
 			avcNalDataLength = 0;
 		}
 	}
 	
 	public void addInputData(AvVideoPacket packet)
 	{
 		AvByteBufferDescriptor location = packet.getNewPayloadDescriptor();
 		
 		// Add an initial reference
 		packet.addRef();
 		
 		while (location.length != 0)
 		{
 			// Remember the start of the NAL data in this packet
 			int start = location.offset;
 			
 			// Check for a special sequence
 			AvByteBufferDescriptor specialSeq = NAL.getSpecialSequenceDescriptor(location);
 			if (specialSeq != null)
 			{
 				if (NAL.isAvcStartSequence(specialSeq))
 				{
 					// We're decoding H264 now
 					currentlyDecoding = AvDecodeUnit.TYPE_H264;
 					
 					// Check if it's the end of the last frame
 					if (NAL.isAvcFrameStart(specialSeq))
 					{
 						// Reassemble any pending AVC NAL
 						reassembleAvcNal();
 						
 						// Setup state for the new NAL
 						avcNalDataChain.clear();
 						avcNalDataLength = 0;
 					}
 					
 					// Skip the start sequence
 					location.length -= specialSeq.length;
 					location.offset += specialSeq.length;
 				}
 				else
 				{
 					// Check if this is padding after a full AVC frame
 					if (currentlyDecoding == AvDecodeUnit.TYPE_H264 &&
 						NAL.isPadding(specialSeq)) {
 						// The decode unit is complete
 						reassembleAvcNal();
 					}
 
 					// Not decoding AVC
 					currentlyDecoding = AvDecodeUnit.TYPE_UNKNOWN;
 
 					// Just skip this byte
 					location.length--;
 					location.offset++;
 				}
				
				specialSeq.free();
 			}
 			
 			// Move to the next special sequence
 			while (location.length != 0)
 			{
 				// Catch the easy case first where byte 0 != 0x00
 				if (location.data[location.offset] == 0x00)
 				{
 					specialSeq = NAL.getSpecialSequenceDescriptor(location);
 					
 					// Check if this should end the current NAL
 					if (specialSeq != null)
 					{
 						// Only stop if we're decoding something or this
 						// isn't padding
 						if (currentlyDecoding != AvDecodeUnit.TYPE_UNKNOWN ||
 							!NAL.isPadding(specialSeq))
 						{
							specialSeq.free();
 							break;
 						}
						else {
							specialSeq.free();
						}
 					}
 				}
 
 				// This byte is part of the NAL data
 				location.offset++;
 				location.length--;
 			}
 			
 			if (currentlyDecoding == AvDecodeUnit.TYPE_H264)
 			{
 				// This is release if the NAL is cleared or decoded
 				AvByteBufferDescriptor data = AvByteBufferDescriptor.newDescriptor(location.data, start, location.offset-start);
 				
 				// Attach the current packet as the buffer context and increment the refcount
 				data.context = packet;
 				packet.addRef();
 				
 				// Add a buffer descriptor describing the NAL data in this packet
 				avcNalDataChain.add(data);
 				avcNalDataLength += location.offset-start;
 			}
 		}
 		
 		// If nothing useful came out of this, release the packet now
 		if (packet.release() == 0) {
 			bbPool.free(location.data);
 			packet.free();
 		}
 		
 		// Done with the buffer descriptor
 		location.free();
 	}
 	
 	public void addInputData(AvRtpPacket packet)
 	{
 		short seq = packet.getSequenceNumber();
 		
 		// Toss out the current NAL if we receive a packet that is
 		// out of sequence
 		if (lastSequenceNumber != 0 &&
 			(short)(lastSequenceNumber + 1) != seq)
 		{
 			System.out.println("Received OOS video data (expected "+(lastSequenceNumber + 1)+", got "+seq+")");
 			
 			// Reset the depacketizer state
 			currentlyDecoding = AvDecodeUnit.TYPE_UNKNOWN;
 			clearAvcNalState();
 		}
 		
 		lastSequenceNumber = seq;
 		
 		// Pass the payload to the non-sequencing parser. It now owns that descriptor.
 		AvByteBufferDescriptor rtpPayload = packet.getNewPayloadDescriptor();
 		addInputData(AvVideoPacket.createNoCopy(rtpPayload));
 	}
 	
 	public AvDecodeUnit getNextDecodeUnit() throws InterruptedException
 	{
 		return decodedUnits.take();
 	}
 }
 
 class NAL {
 	
 	// This assumes that the buffer passed in is already a special sequence
 	public static boolean isAvcStartSequence(AvByteBufferDescriptor specialSeq)
 	{
 		// The start sequence is 00 00 01 or 00 00 00 01
 		return (specialSeq.data[specialSeq.offset+specialSeq.length-1] == 0x01);
 	}
 	
 	// This assumes that the buffer passed in is already a special sequence
 	public static boolean isPadding(AvByteBufferDescriptor specialSeq)
 	{
 		// The padding sequence is 00 00 00
 		return (specialSeq.data[specialSeq.offset+specialSeq.length-1] == 0x00);
 	}
 	
 	// This assumes that the buffer passed in is already a special sequence
 	public static boolean isAvcFrameStart(AvByteBufferDescriptor specialSeq)
 	{
 		if (specialSeq.length != 4)
 			return false;
 		
 		// The frame start sequence is 00 00 00 01
 		return (specialSeq.data[specialSeq.offset+specialSeq.length-1] == 0x01);
 	}
 	
 	// Returns a buffer descriptor describing the start sequence
 	public static AvByteBufferDescriptor getSpecialSequenceDescriptor(AvByteBufferDescriptor buffer)
 	{
 		// NAL start sequence is 00 00 00 01 or 00 00 01
 		if (buffer.length < 3)
 			return null;
 		
 		// 00 00 is magic
 		if (buffer.data[buffer.offset] == 0x00 &&
 			buffer.data[buffer.offset+1] == 0x00)
 		{
 			// Another 00 could be the end of the special sequence
 			// 00 00 00 or the middle of 00 00 00 01
 			if (buffer.data[buffer.offset+2] == 0x00)
 			{
 				if (buffer.length >= 4 &&
 					buffer.data[buffer.offset+3] == 0x01)
 				{
 					// It's the AVC start sequence 00 00 00 01
 					return AvByteBufferDescriptor.newDescriptor(buffer.data, buffer.offset, 4);
 				}
 				else
 				{
 					// It's 00 00 00
 					return AvByteBufferDescriptor.newDescriptor(buffer.data, buffer.offset, 3);
 				}
 			}
 			else if (buffer.data[buffer.offset+2] == 0x01 ||
 					 buffer.data[buffer.offset+2] == 0x02)
 			{
 				// These are easy: 00 00 01 or 00 00 02
 				return AvByteBufferDescriptor.newDescriptor(buffer.data, buffer.offset, 3);
 			}
 			else if (buffer.data[buffer.offset+2] == 0x03)
 			{
 				// 00 00 03 is special because it's a subsequence of the
 				// NAL wrapping substitute for 00 00 00, 00 00 01, 00 00 02,
 				// or 00 00 03 in the RBSP sequence. We need to check the next
 				// byte to see whether it's 00, 01, 02, or 03 (a valid RBSP substitution)
 				// or whether it's something else
 				
 				if (buffer.length < 4)
 					return null;
 				
 				if (buffer.data[buffer.offset+3] >= 0x00 &&
 					buffer.data[buffer.offset+3] <= 0x03)
 				{
 					// It's not really a special sequence after all
 					return null;
 				}
 				else
 				{
 					// It's not a standard replacement so it's a special sequence
 					return AvByteBufferDescriptor.newDescriptor(buffer.data, buffer.offset, 3);
 				}
 			}
 		}
 		
 		return null;
 	}
 }
