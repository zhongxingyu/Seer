 /* 
  * 
  * PROJECT
  *     Name
  *         APSGroups
  *     
  *     Code Version
  *         0.9.0
  *     
  *     Description
  *         Provides network groups where named groups can be joined as members and then send and
  *         receive data messages to the group. This is based on multicast and provides a verified
  *         multicast delivery with acknowledgements of receive to the sender and resends if needed.
  *         The sender will get an exception if not all members receive all data. Member actuality
  *         is handled by members announcing themselves relatively often and will be removed when
  *         an announcement does not come in expected time. So if a member dies unexpectedly
  *         (network goes down, etc) its membership will resolve rather quickly. Members also
  *         tries to inform the group when they are doing a controlled exit. Most network aspects
  *         are configurable. Please note that this does not support streaming! That would require
  *         a far more complex protocol. It waits in all packets of a message before delivering
  *         the message.
  *         
  *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
  *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
  *         This API has no external dependencies, only this jar is required for that use.
  *         
  *         When run with java -jar a for test command line shell will run where you can check
  *         members, send messages and files.
  *         
  * COPYRIGHTS
  *     Copyright (C) 2012 by Natusoft AB All rights reserved.
  *     
  * LICENSE
  *     Apache 2.0 (Open Source)
  *     
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *     
  *       http://www.apache.org/licenses/LICENSE-2.0
  *     
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *     
  * AUTHORS
  *     Tommy Svensson (tommy@natusoft.se)
  *         Changes:
  *         2012-12-28: Created!
  *         
  */
 package se.natusoft.apsgroups.internal.protocol.message;
 
 import se.natusoft.apsgroups.Debug;
 import se.natusoft.apsgroups.internal.net.Transport;
 import se.natusoft.apsgroups.internal.protocol.Group;
 import se.natusoft.apsgroups.internal.protocol.Groups;
 import se.natusoft.apsgroups.internal.protocol.Member;
 import se.natusoft.apsgroups.internal.protocol.message.exception.BadProtocolIDException;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.*;
 
 /**
  * The complete Message is made up of one or more message packets.
  */
 public class MessagePacket {
     //
     // Constants
     //
 
     /** Every packet is expected to start with this. */
     private static final int PROTOCOL_ID = 0xbadcafe;
 
     //
     // Private Members
     //
 
     /** The sequential number of the packet. */
     private int packetNumber = 0;
 
     /** The data content of the packet. */
     private byte[] data = new byte[0];
 
     /**
      * The acknowledgements from the different members of packet received.
      * <p/>
      * Please note that this is checked against the currently known and considered
      * valid members! If one member drops off during the complete message transfer,
      * the message is still considered transferred OK if all members available at
      * end of message transfer have acknowledged all packets.
      */
     private Map<UUID, Member> packetAcknowledgements = new HashMap<UUID, Member>();
 
     /**
      * Allows writing packet content using an OutputStream.
      * The written data is copied to the "data" member when the
      * stream is closed.
      */
     private OutputStream outStream = new ByteArrayOutputStream() {
         @Override
         public void close() throws IOException {
             try {
                 super.close();
             }
             finally {
                 MessagePacket.this.data = toByteArray();
             }
         }
     };
 
     /** The group this message packet belongs to. */
     private Group group = null;
 
     /** The member the packet belongs to. */
     private Member member = null;
 
     /** The message id of the Message this packet belongs to. */
     private UUID messageId = null;
 
     /** The type of the packet. */
     private PacketType type = null;
 
     /** The address we received the packet from or our own address for locally created MessagePacket. */
     private InetAddress address = null;
 
     //
     // Constructors
     //
 
     /**
      * Creates a new MessagePacket instance for creating a packet.
      *
      * @param group The group the packet belongs to.
      * @param member The member the packet belongs to.
      * @param messageId The id of the message the packet belongs to.
      * @param packetNumber The sequential number of this packet.
      * @param type The type of the packet.
      */
     public MessagePacket(Group group, Member member, UUID messageId, int packetNumber, PacketType type) {
         this.group = group;
         this.member = member;
         this.messageId = messageId;
         this.packetNumber = packetNumber;
         this.type = type;
         try {
             this.address = InetAddress.getLocalHost();
         }
         catch (UnknownHostException uhe) {
             // This should never happen unless your host is screwed up!
         }
     }
 
     /**
      * Creates a new MessagePacket instance for receiving a packet.
      *
      * @param packet The received Transport.Packet.
      *
      * @throws IOException on I/O failure.
      * @throws BadProtocolIDException when protocol id does not match.
      */
     public MessagePacket(Transport.Packet packet) throws  IOException, BadProtocolIDException {
         parsePacketBytes(packet.getBytes());
         this.address = packet.getAddress();
     }
 
     //
     // Methods
     //
 
     /**
      * Parses the specified packet bytes.
      *
      * @param packetBytes The packet bytes to parse.
      *
      * @throws IOException on I/O failure.
      * @throws BadProtocolIDException when protocol id does not match.
      */
     private void parsePacketBytes(byte[] packetBytes) throws IOException, BadProtocolIDException {
         try {
             ByteArrayInputStream byteStream = new ByteArrayInputStream(packetBytes);
             ObjectInputStream objStream = new ObjectInputStream(byteStream);
 
             int protocolId = objStream.readInt();
             if (protocolId != PROTOCOL_ID) {
                 throw new BadProtocolIDException(protocolId);
             }
 
             String groupName = objStream.readUTF();
             this.group = Groups.getGroup(groupName);
 
             UUID memberId = (UUID)objStream.readObject();
             this.member = this.group.getMemberById(memberId);
             if (this.member == null) {
                 // This means it is a new member not in the group yet!
                 this.member = new Member(memberId);
             }
 
             this.messageId = (UUID)objStream.readObject();
 
             this.packetNumber = objStream.readInt();
 
             this.type = (PacketType)objStream.readObject();
 
             int size = objStream.readInt();
             byte[] bytes = new byte[size];
             int read = objStream.read(bytes);
             if (read != size) {
                 throw new IOException("Expected " + size + " data bytes, but got " + read + "!");
             }
             this.data = bytes;
 
             objStream.close();
             byteStream.close();
 
         }
         catch (ClassNotFoundException cnfe) {
             throw new IOException(cnfe.getMessage(), cnfe);
         }
         catch (IOException ioe) {
             ioe.printStackTrace();
             throw ioe;
         }
     }
 
     /**
      * @return the complete packet bytes of this packet.
      *
      * @throws IOException on failure.
      */
     public byte[] getPacketBytes() throws IOException {
         ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
 
         objStream.writeInt(PROTOCOL_ID);
 
         objStream.writeUTF(this.group.getName());
 
         objStream.writeObject(this.member.getId());
 
         objStream.writeObject(this.messageId);
 
         objStream.writeInt(this.packetNumber);
 
         objStream.writeObject(this.type);
 
         objStream.writeInt(this.data.length);
         objStream.write(this.data);
 
         objStream.close();
         byteStream.close();
 
         return byteStream.toByteArray();
     }
 
     /**
      * @return the type of the packet.
      */
     public PacketType getType() {
         return this.type;
     }
 
     /**
      * Replaces the packet type.
      *
      * @param type The new type.
      */
     public void setType(PacketType type) {
         this.type = type;
     }
 
     /**
      * @return The sequential number of this packet.
      */
     public int getPacketNumber() {
         return this.packetNumber;
     }
 
     /**
      * @return The data content of this packet. This does not include the packet information! For the complete packet data
      * bytes use getPacketBytes().
      */
     public byte[] getData() {
         return data;
     }
 
     /**
      * @return The address the packet comes from.
      */
     public InetAddress getAddress() {
         return this.address;
     }
 
     /**
      * @return An OutputStream to provide data content with.
      */
     public OutputStream getOutputStream() {
         return this.outStream;
     }
 
     /**
      * @return An InputStream to read data content with.
      */
     public InputStream getInputStream() {
         return new ByteArrayInputStream(this.data);
     }
 
     /**
      * @return The byte length of this packet.
      */
     public int getPacketSize() {
         return this.data.length;
     }
 
     /**
      * @return The id of the message this packet belongs to.
      */
     public UUID getMessageId() {
         return this.messageId;
     }
 
     /**
      * @return The group this message is from and for.
      */
     public Group getGroup() {
         return this.group;
     }
 
     /**
      * @return The member this packet belongs to.
      */
     public Member getMember() {
         return this.member;
     }
 
     /**
      * @return true if all members have acknowledged that they have received this packet. Returns false otherwise.
      */
     public boolean hasAllAcknowledgements() {
         // We subtract one from all group members to exclude ourself!
         boolean hasAcks = this.packetAcknowledgements.size() == (this.group.getMembers().size() - 1);
         Debug.println("hasAllAcknowledgements(" + this.packetAcknowledgements.size() + "):" + hasAcks);
         return hasAcks;
     }
 
     /**
      * Adds an acknowledgement for a group member.
      *
      * @param member The group member to add acknowledgement from.
      */
     public void acknowledgeMember(Member member) {
        this.packetAcknowledgements.put(member.getId(), member);
     }
 
     /**
      * @return A List of members that has not acknowledged the packet.
      */
     public List<Member> getMembersWithMissingAcknowledgements() {
         List<Member> missingMembers = new LinkedList<>();
 
         for (Member member : this.getGroup().getListOfMembers()) {
             if (!this.packetAcknowledgements.containsKey(member.getId())) {
                 missingMembers.add(member);
             }
         }
 
         return missingMembers;
     }
 
     /**
      * Compares MessagePackets for equality.
      *
      * @param obj The object to compare to.
      */
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof MessagePacket)) return false;
 
         MessagePacket mp = (MessagePacket)obj;
         return this.messageId.equals(mp.getMessageId()) && this.member.getId().equals(mp.getMember().getId()) &&
             this.group.getName().equals(mp.getGroup().getName());
     }
 }
