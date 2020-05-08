 /*
  * Copyright (c) 2003-2005 JGGApi Development Team. All Rights Reserved.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package pl.mn.communicator.packet.in;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import pl.mn.communicator.IUser;
 import pl.mn.communicator.RemoteStatus;
 import pl.mn.communicator.User;
 import pl.mn.communicator.packet.GGConversion;
 import pl.mn.communicator.packet.GGUtils;
 
 /**
  * 
  * @author <a href="mailto:mnaglik@gazeta.pl">Marcin Naglik</a>
  * @author <a href="mailto:mati@sz.home.pl">Mateusz Szczap</a>
 * @version $Id: GGNotifyReply60.java,v 1.2 2007-05-12 13:54:48 winnetou25 Exp $
  */
 public class GGNotifyReply60 implements GGIncomingPackage {
 
 	public static final int GG_NOTIFY_REPLY60 = 0x0011;
 
     private static final Log LOGGER = LogFactory.getLog(GGNotifyReply60.class);
 
     private Map m_statuses = new HashMap();
 
 //  struct gg_notify_reply60 {
 //	int uin;		/* numerek plus flagi w najstarszym bajcie */  - 4
 //	char status;		/* status danej osoby */ - 1
 //	int remote_ip;		/* adres IP bezpośrednich połączeń */ - 4
 //	short remote_port;	/* port bezpośrednich połączeń */ - 2
 //	char version;		/* wersja klienta */ - 1
 //	char image_size;	/* maksymalny rozmiar obrazków w KB */ -1
 //	char unknown1;		/* 0x00 */ -1
 //	char description_size;	/* rozmiar opisu i czasu, nie musi wystąpić */ -1
 //	char description[];	/* opis, nie musi wystąpić */ 
 //	int time;		/* czas, nie musi wystąpić */ 4
 //};
     
     public GGNotifyReply60(byte[] data) {
     	handlePacket(data);
     }
 
     public int getPacketType() {
     	return GG_NOTIFY_REPLY60;
     }
     
     public Map getUsersStatus() {
         return m_statuses;
     }
     
     private void handlePacket(byte[] data) {
         int offset = 0;
         while (offset < data.length) {
         	int flag = data[offset+3];
         	data[offset+3] = GGUtils.intToByte(0)[0];
 
         	int uin = GGUtils.byteToInt(data, offset);
         	int status = GGUtils.unsignedByteToInt(data[offset+4]);
         	User.UserMode userMode = GGConversion.getUserMode(status);
 
         	int remoteIP = GGUtils.byteToInt(data, offset+5);
             byte[] remoteIPArray = GGUtils.convertIntToByteArray(remoteIP);
         	int remotePort = GGUtils.byteToShort(data, offset+9);
         	int version = GGUtils.unsignedByteToInt(data[offset+11]);
         	int imageSize = GGUtils.unsignedByteToInt(data[offset+12]);
         	String description = null;
         	int descriptionSize = -1;
             long timeInMillis = -1;
             if ((status == GGStatus.GG_STATUS_AVAIL_DESCR)
             	|| (status == GGStatus.GG_STATUS_BUSY_DESCR)
 				|| (status == GGStatus.GG_STATUS_INVISIBLE_DESCR)
 				|| (status == GGStatus.GG_STATUS_NOT_AVAIL_DESCR)) {
                 descriptionSize = GGUtils.unsignedByteToInt(data[offset+14]);
 
                 byte[] descBytes = new byte[descriptionSize];
                 System.arraycopy(data, offset+15, descBytes, 0, descriptionSize);
                description = GGUtils.byteToString(descBytes, 0);
                 
                 boolean isTimeSet = data[(offset+15+descriptionSize)-5]==0;
 
                 if (isTimeSet) {
                 	int timeInSeconds = GGUtils.byteToInt(data, (offset+15+descriptionSize)-4);
                     timeInMillis = GGUtils.secondsToMillis(timeInSeconds);
                     descriptionSize -= 5;
                 }
 
                 offset+=(15+descriptionSize);
 
                 if (isTimeSet) {
                     offset += 5;
                 }
             } else {
             	 offset += 14; // packet without description is only 14 bytes long            	
             }
         	IUser user = new User(uin, userMode);
             RemoteStatus status60 = GGConversion.getClientRemoteStatus(status, description, timeInMillis);
 
             if (remotePort == 0) {
             	status60.setSupportsDirectCommunication(false);
             } else if (remotePort == 1) {
             	status60.setUserBehindFirewall(true);
             } else  if (remotePort == 2) {
             	status60.setAreWeInRemoteUserBuddyList(false);
             } else {
             	status60.setRemotePort(remotePort);
             }
 
             status60.setRemoteIP(remoteIPArray);
             status60.setImageSize(imageSize);
             status60.setGGVersion(version);
             
             if (descriptionSize > 0) {
             	status60.setDescriptionSize(descriptionSize);
             }
             
         	if (flag == 0x40) {
         		status60.setSupportsVoiceCommunication(true);
         	}
         	
         	m_statuses.put(user, status60);
         }
     }
 
 }
