 package edu.nps.moves.dis;
 
 import java.util.*;
 import java.io.*;
 import javax.xml.bind.annotation.*;
 
 /**
  * Section 5.2.32. Variable Datum Record
  *
  * Copyright (c) 2008, MOVES Institute, Naval Postgraduate School. All rights reserved.
  * This work is licensed under the BSD open source license, available at https://www.movesinstitute.org/licenses/bsd.html
  *
  * @author DMcG
  */
 public class VariableDatum extends Object implements Serializable
 {
    /** ID of the variable datum */
    protected long  variableDatumID;
 
    /** length of the variable datums */
    protected long  variableDatumLength;
 
    /** variable length list of 64-bit datums */
    protected List variableDatums = new ArrayList(); 
 
 /** Constructor */
  public VariableDatum()
  {
  }
 
 public int getMarshalledSize()
 {
    int marshalSize = 0; 
 
    marshalSize = marshalSize + 4;  // variableDatumID
    marshalSize = marshalSize + 4;  // variableDatumLength
    for(int idx=0; idx < variableDatums.size(); idx++)
    {
         EightByteChunk listElement = (EightByteChunk)variableDatums.get(idx);
         marshalSize = marshalSize + listElement.getMarshalledSize();
    }
 
    return marshalSize;
 }
 
 
 public void setVariableDatumID(long pVariableDatumID)
 { variableDatumID = pVariableDatumID;
 }
 
 @XmlAttribute
 public long getVariableDatumID()
 { return variableDatumID; 
 }
 
 @XmlAttribute
 public long getVariableDatumLength()
 { return (long)variableDatums.size();
 }
 
 /** Note that setting this value will not change the marshalled value. The list whose length this describes is used for that purpose.
  * The getvariableDatumLength method will also be based on the actual list length rather than this value. 
  * The method is simply here for java bean completeness.
  */
 public void setVariableDatumLength(long pVariableDatumLength)
 { variableDatumLength = pVariableDatumLength;
 }
 
 public void setVariableDatums(List pVariableDatums)
 { variableDatums = pVariableDatums;
 }
 
 @XmlElementWrapper(name="variableDatumsList" )
 public List getVariableDatums()
 { return variableDatums; }
 
 
 public void marshal(DataOutputStream dos)
 {
     try 
     {
        dos.writeInt( (int)variableDatumID);
       dos.writeInt( (int)variableDatums.size() * 64 );
 
        for(int idx = 0; idx < variableDatums.size(); idx++)
        {
             EightByteChunk aEightByteChunk = (EightByteChunk)variableDatums.get(idx);
             aEightByteChunk.marshal(dos);
        } // end of list marshalling
 
     } // end try 
     catch(Exception e)
     { 
       System.out.println(e);}
     } // end of marshal method
 
 public void unmarshal(DataInputStream dis)
 {
     try 
     {
        variableDatumID = dis.readInt();
        variableDatumLength = dis.readInt();
       variableDatumLength = (variableDatumLength / 64) + ((variableDatumLength % 64) > 0);
         for(int idx = 0; idx < variableDatumLength; idx++)
         {
            EightByteChunk anX = new EightByteChunk();
             anX.unmarshal(dis);
             variableDatums.add(anX);
         };
 
     } // end try 
    catch(Exception e)
     { 
       System.out.println(e); 
     }
  } // end of unmarshal method 
 
 
  /**
   * The equals method doesn't always work--mostly on on classes that consist only of primitives. Be careful.
   */
  public boolean equals(VariableDatum rhs)
  {
      boolean ivarsEqual = true;
 
     if(rhs.getClass() != this.getClass())
         return false;
 
      if( ! (variableDatumID == rhs.variableDatumID)) ivarsEqual = false;
      if( ! (variableDatumLength == rhs.variableDatumLength)) ivarsEqual = false;
 
      for(int idx = 0; idx < variableDatums.size(); idx++)
      {
         EightByteChunk x = (EightByteChunk)variableDatums.get(idx);
         if( ! ( variableDatums.get(idx).equals(rhs.variableDatums.get(idx)))) ivarsEqual = false;
      }
 
 
     return ivarsEqual;
  }
 } // end of class
