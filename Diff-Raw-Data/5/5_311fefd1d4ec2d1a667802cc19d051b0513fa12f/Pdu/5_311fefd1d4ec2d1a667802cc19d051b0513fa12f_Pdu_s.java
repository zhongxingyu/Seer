 package edu.nps.moves.dis;
 
 import java.util.*;
 import java.io.*;
 import javax.xml.bind.annotation.*;
 
 /**
  * The superclass for all PDUs. This incorporates the PduHeader record, section 5.2.29.
  *
  * Copyright (c) 2008, MOVES Institute, Naval Postgraduate School. All rights reserved.
  * This work is licensed under the BSD open source license, available at https://www.movesinstitute.org/licenses/bsd.html
  *
  * @author DMcG
  */
 public class Pdu extends Object implements Serializable
 {
    /** The version of the protocol. 5=DIS-1995, 6=DIS-1998. */
    protected short  protocolVersion = 6;
 
    /** Exercise ID */
    protected short  exerciseID = 0;
 
    /** Type of pdu, unique for each PDU class */
    protected short  pduType;
 
    /** value that refers to the protocol family, eg SimulationManagement, et */
    protected short  protocolFamily;
 
    /** Timestamp value */
    protected long  timestamp;
 
    /** Length, in bytes, of the PDU */
    protected int  length;
 
    /** zero-filled array of padding */
    protected short  padding = 0;
 
 
 /** Constructor */
  public Pdu()
  {
  }
 
 public int getMarshalledSize()
 {
    int marshalSize = 0; 
 
    marshalSize = marshalSize + 1;  // protocolVersion
    marshalSize = marshalSize + 1;  // exerciseID
    marshalSize = marshalSize + 1;  // pduType
    marshalSize = marshalSize + 1;  // protocolFamily
    marshalSize = marshalSize + 4;  // timestamp
    marshalSize = marshalSize + 2;  // length
    marshalSize = marshalSize + 2;  // padding
 
    return marshalSize;
 }
 
 
 public void setProtocolVersion(short pProtocolVersion)
 { protocolVersion = pProtocolVersion;
 }
 
 @XmlAttribute
 public short getProtocolVersion()
 { return protocolVersion; 
 }
 
 public void setExerciseID(short pExerciseID)
 { exerciseID = pExerciseID;
 }
 
 @XmlAttribute
 public short getExerciseID()
 { return exerciseID; 
 }
 
 public void setPduType(short pPduType)
 { pduType = pPduType;
 }
 
 @XmlAttribute
 public short getPduType()
 { return pduType; 
 }
 
 public void setProtocolFamily(short pProtocolFamily)
 { protocolFamily = pProtocolFamily;
 }
 
 @XmlAttribute
 public short getProtocolFamily()
 { return protocolFamily; 
 }
 
 public void setTimestamp(long pTimestamp)
 { timestamp = pTimestamp;
 }
 
 @XmlAttribute
 public long getTimestamp()
 { return timestamp; 
 }
 
 public void setLength(int pLength)
 { length = pLength;
 }
 
 @XmlAttribute
 public int getLength()
{ return length; 
 }
 
 public void setPadding(short pPadding)
 { padding = pPadding;
 }
 
 @XmlAttribute
 public short getPadding()
 { return padding; 
 }
 
 
 public void marshal(DataOutputStream dos)
 {
     try 
     {
        dos.writeByte( (byte)protocolVersion);
        dos.writeByte( (byte)exerciseID);
        dos.writeByte( (byte)pduType);
        dos.writeByte( (byte)protocolFamily);
        dos.writeInt( (int)timestamp);
       dos.writeShort( (short)length);
        dos.writeShort( (short)padding);
     } // end try 
     catch(Exception e)
     { 
       System.out.println(e);}
     } // end of marshal method
 
 public void unmarshal(DataInputStream dis)
 {
     try 
     {
        protocolVersion = (short)dis.readUnsignedByte();
        exerciseID = (short)dis.readUnsignedByte();
        pduType = (short)dis.readUnsignedByte();
        protocolFamily = (short)dis.readUnsignedByte();
        timestamp = dis.readInt();
        length = (int)dis.readUnsignedShort();
        padding = dis.readShort();
     } // end try 
    catch(Exception e)
     { 
       System.out.println(e); 
     }
  } // end of unmarshal method 
 
 
  /**
   * The equals method doesn't always work--mostly on on classes that consist only of primitives. Be careful.
   */
  public boolean equals(Pdu rhs)
  {
      boolean ivarsEqual = true;
 
     if(rhs.getClass() != this.getClass())
         return false;
 
      if( ! (protocolVersion == rhs.protocolVersion)) ivarsEqual = false;
      if( ! (exerciseID == rhs.exerciseID)) ivarsEqual = false;
      if( ! (pduType == rhs.pduType)) ivarsEqual = false;
      if( ! (protocolFamily == rhs.protocolFamily)) ivarsEqual = false;
      if( ! (timestamp == rhs.timestamp)) ivarsEqual = false;
      if( ! (length == rhs.length)) ivarsEqual = false;
      if( ! (padding == rhs.padding)) ivarsEqual = false;
 
     return ivarsEqual;
  }
 } // end of class
