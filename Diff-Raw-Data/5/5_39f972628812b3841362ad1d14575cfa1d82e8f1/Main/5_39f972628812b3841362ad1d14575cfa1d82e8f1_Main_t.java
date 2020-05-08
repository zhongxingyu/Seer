 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Main {
 
     // // -------------global variables
     // static long packetSumTCPport25 = 0L;
     // static long packetSumTCPport80 = 0L;
     // static long packetSumTCPport443 = 0L;
     // static long packetSumTCPOther = 0L;
     // static long packetSumUDPport53 = 0L;
     // static long packetSumUDPOther = 0L;
     // // static long packetSumTCPflagSYN = 0L;
     // // static long packetSumTCPflagACK = 0L;
     // static long packetSumIPv4 = 0L;
     // static long packetSumIPv6 = 0L;
     // // static long packetSumARP = 0L;
     // static long packetOther = 0L;
     // static long datagramTCP = 0L;
     // static long datagramUDP = 0L;
     // static long datagramOther = 0L;
     //
     // static long sizeOFallFramesWholePCAP = 0L;
     // static long numberOfFramesWholePCAP = 0L;
 
     // -------------------XYZ min. window
     static long timeWindowInMillis = 0L;
     static long lastEndOfWindow = 0L;
     static long lastFrameTime = 0L;
 
     static long window_sizeOfAll_frames_inbytes = 0L;
     static long window_numberOfAll_frames = 0L;
 
     static long window_datagramSumTCP_SYN_port22 = 0L;
     static long window_datagramSumTCP_SYN_port25 = 0L;
     static long window_datagramSumTCP_SYN_port80 = 0L;
     static long window_datagramSumTCP_SYN_port443 = 0L;
     static long window_datagramSumTCP_SYN_port587 = 0L;
     static long window_datagramSumTCP_allOther = 0L;
     static long window_datagramSumUDP_port53 = 0L;
     static long window_datagramSumUDP_allOther = 0L;
     static long window_packetSum_IPv4 = 0L;
     static long window_packetSum_IPv6 = 0L;
 
     static long timeToAdd = 0L;
 
     // static long window_datagram_TCP_general = 0L;
     // static long window_datagram_UDP_general = 0L;
     // static long window_datagram_allOther = 0L;
 
     static String rrdFileToUpdate = " ";
 
     public static void main(String[] args) {
 	try {
 	    if (args.length != 4) {
 		System.err
 			.println("Arguments have to be: \n\t-t X, where X is number of minutes of floating window \n\t-f file.rrd");
 		System.exit(0);
 	    }
 	    timeWindowInMillis = Long.parseLong(args[1]) * 30 * 1000;
	    //Current time is created only when the app. is launched
 	    timeToAdd = System.currentTimeMillis();
 	    
 	    rrdFileToUpdate = args[3];
 	    if (!new File(rrdFileToUpdate).exists()) {
 		System.out.println("xxCannot find: " + rrdFileToUpdate);
 		System.exit(0);
 	    }
 
 	    // new thread to avoid losing packet while parsing;
 	    read();
 	} catch (IOException e) {
 	    e.getMessage();
 	}
 
     }
 
     private static void read() throws IOException {
 
 	// tail -f livewlan.pcap -n 100000000 | tshark -r - -T fields -e
 	// frame.time -e frame.len -e eth.type -e ip.proto -e tcp.flags.syn -e
 	// tcp.dstport -e udp.dstport -e header=y -E separator=- -E occurrence=f
 
 	// frame.time =timestamp of sent frame
 	// frame.len = frame lenght in bytes
 	// eth.type:
 	// 0x0800 Internet Protocol version 4 (IPv4)
 	// 0x0806 Address Resolution Protocol (ARP)
 	// 0x86DD Internet Protocol Version 6 (IPv6)
 	// ip.proto:
 	// 1 ICMP Internet Control Message Protocol
 	// 6 TCP Transmission Control Protocol
 	// 17 UDP User Datagram Protocol
 	// tcp.flags.syn
 	// tcp.flags.ack
 	// tcp.srcport
 	// tcp.dstport
 	// udp.srcport
 	// udp.dstport
 
 	BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 	String frame[];
 	String line;
 
 	while ((line = input.readLine()) != null) {
 	    frame = line.split("-");
 	    // numberOfFramesWholePCAP++;
 	    window_numberOfAll_frames++;
 	    for (int i = 0; i < frame.length; i++) {
 		if (!frame[i].isEmpty()) {
 		    parseFrame(frame, i);
 		}
 
 	    }
 	    if (lastFrameTime > (lastEndOfWindow + timeWindowInMillis)) {
 		// System.out.println(lastFrameTime);
 		// System.out.println(lastEndOfWindow);
 		// System.out.println(timeWindowInMillis);
 
 		// printStatsWindow();
 
 		updateRRDfile(lastEndOfWindow);
 
 		// addWindowStatsToGlobal();
 		setWindowStatsToZero();
 		// printStatsGlobal();
 		lastEndOfWindow = lastFrameTime + timeWindowInMillis;
 	    }
 	}
 	// printEndStats();
 
     }
 
     private static void updateRRDfile(Long lastEndOfWindow) throws IOException {
 	StringBuilder s = new StringBuilder();
 	s.append("update ");
 	s.append(rrdFileToUpdate);
 	s.append(" ");
 	String x = String.valueOf(lastEndOfWindow);
 	s.append(x.substring(0, x.length() - 3));
 	s.append(":");
 	s.append(Long.valueOf(window_datagramSumUDP_port53));
 	s.append(":");
 	s.append(Long.valueOf(window_datagramSumTCP_SYN_port25+window_datagramSumTCP_SYN_port587));
 	s.append(":");
 	s.append(Long.valueOf(window_datagramSumTCP_SYN_port80));
 	s.append(":");
 	s.append(Long.valueOf(window_datagramSumTCP_SYN_port443));
 	s.append(":");
 	s.append(Long.valueOf(window_datagramSumTCP_SYN_port22));
 	s.append(":");
 	s.append(Long.valueOf(window_datagramSumTCP_allOther));
 	s.append(":");
 	s.append(Long.valueOf(window_datagramSumUDP_allOther));
 	s.append(":");
 	s.append(Long.valueOf(window_packetSum_IPv6));
 
 	System.out.println(s.toString());
 
 	// System.out.println("Amount of DNS conn - UDPport 53:\t" +
 	// window_datagramSumUDP_port53);
 	//
 	// System.out.println("Amount of SPAM conn - TCPport 25:\t" +
 	// window_datagramSumTCP_SYN_port25);
 	//
 	// System.out.println("Amount of WEB conn - TCPport 80:\t" +
 	// window_datagramSumTCP_SYN_port80);
 	//
 	//
 	// System.out.println("Amount of SSL conn - TCPport 443:\t" +
 	// window_datagramSumTCP_SYN_port443);
 	//
 	// System.out.println("Amount of SSH conn - TCPport 22:\t" +
 	// window_datagramSumTCP_SYN_port22);
 	//
 	// System.out.println("Amount of TCP+SYN all conn:\t" +
 	// window_datagramSumTCP_allOther);
 	//
 	// System.out.println("Amount of UDP all conn:\t" +
 	// window_datagramSumUDP_allOther);
 	//
 	// System.out.println("Amount of IPv6 packets:\t" +
 	// window_packetSum_IPv6);
 
     }
 
     private static void setWindowStatsToZero() {
 	window_sizeOfAll_frames_inbytes = 0L;
 	window_numberOfAll_frames = 0L;
 
 	window_datagramSumTCP_SYN_port22 = 0L;
 	window_datagramSumTCP_SYN_port25 = 0L;
 	window_datagramSumTCP_SYN_port80 = 0L;
 	window_datagramSumTCP_SYN_port443 = 0L;
 	window_datagramSumTCP_SYN_port587 = 0L;
 	window_datagramSumTCP_allOther = 0L;
 	window_datagramSumUDP_port53 = 0L;
 	window_datagramSumUDP_allOther = 0L;
 	window_packetSum_IPv4 = 0L;
 	window_packetSum_IPv6 = 0L;
 	// window_datagram_TCP_general = 0;
 	// window_datagram_UDP_general = 0;
 	// window_datagram_allOther = 0;
 
     }
 
     // private static void addWindowStatsToGlobal() {
     // packetSumTCPport25 = packetSumTCPport25 +
     // window_datagramSumTCP_SYN_port25;
     // packetSumTCPport80 = packetSumTCPport80 +
     // window_datagramSumTCP_SYN_port80;
     // packetSumTCPport443 = packetSumTCPport443 +
     // window_datagramSumTCP_SYN_port443;
     // packetSumTCPOther = packetSumTCPOther + window_datagramSumTCP_allOther;
     // packetSumUDPport53 = packetSumUDPport53 + window_datagramSumUDP_port53;
     // packetSumUDPOther = packetSumUDPOther + window_datagramSumUDP_allOther;
     // packetSumIPv4 = packetSumIPv4 + window_packetSum_IPv4;
     // packetSumIPv6 = packetSumIPv6 + window_packetSum_IPv6;
     // // datagramTCP = datagramTCP + window_datagram_TCP_general;
     // // datagramUDP = datagramUDP + window_datagram_UDP_general;
     // // datagramOther = datagramOther + window_datagram_allOther;
     // }
 
     // private static void printStatsGlobal() {
     // System.out.println("GLOBAL:" + new Date(lastEndOfWindow).toString() +
     // "Number of frames so far: "
     // + numberOfFramesWholePCAP + " Size of all frames so far: " +
     // sizeOFallFramesWholePCAP
     // + " packetSumIPv4: " + packetSumIPv4 + " packetSumIPv6: " + packetSumIPv6
     // + " datagramTCP: "
     // + datagramTCP + " packetSumTCP SYN+port80: " + packetSumTCPport80 +
     // " packetSumTCP all Other: "
     // + packetSumTCPOther + " packetSumTCP SYN+port25: " + packetSumTCPport25 +
     // " packetSumTCP SYN+port443: "
     // + packetSumTCPport443 + " datagramUDP: " + datagramUDP +
     // " packetSumUDPport53: " + packetSumUDPport53
     // + " packetSumUDPOther: " + packetSumUDPOther);
     // }
 
     private static void printEndStats() {
 	System.out.println("Window until:\t" + new Date(lastFrameTime).toString());
 	System.out.println("Number of frames in the window:\t" + window_numberOfAll_frames);
 	System.out.println("Size of all frames in the window in bytes:\t" + window_sizeOfAll_frames_inbytes);
 	System.out.println("Amount of SPAM conn - TCPport 25:\t" + window_datagramSumTCP_SYN_port25);
 	System.out.println("Amount of SPAM conn - TCPport 587:\t" + window_datagramSumTCP_SYN_port587);
 	System.out.println("Amount of WEB conn - TCPport 80:\t" + window_datagramSumTCP_SYN_port80);
 	System.out.println("Amount of SSL conn - TCPport 443:\t" + window_datagramSumTCP_SYN_port443);
 	System.out.println("Amount of SSH conn - TCPport 22:\t" + window_datagramSumTCP_SYN_port22);
 	System.out.println("Amount of TCP+SYN all conn:\t" + window_datagramSumTCP_allOther);
 	System.out.println("Amount of DNS conn - UDPport 53:\t" + window_datagramSumUDP_port53);
 	System.out.println("Amount of UDP all conn:\t" + window_datagramSumUDP_allOther);
 	System.out.println("Amount of IPv6 packets:\t" + window_packetSum_IPv6);
     }
 
     private static void printStatsWindow() {
 	System.out.println("Window until:\t" + new Date(lastEndOfWindow).toString());
 	System.out.println("Number of frames in the window:\t" + window_numberOfAll_frames);
 	System.out.println("Size of all frames in the window in bytes:\t" + window_sizeOfAll_frames_inbytes);
 	System.out.println("Amount of SPAM conn - TCPport 25:\t" + window_datagramSumTCP_SYN_port25);
 	System.out.println("Amount of SPAM conn - TCPport 587:\t" + window_datagramSumTCP_SYN_port587);
 	System.out.println("Amount of WEB conn - TCPport 80:\t" + window_datagramSumTCP_SYN_port80);
 	System.out.println("Amount of SSL conn - TCPport 443:\t" + window_datagramSumTCP_SYN_port443);
 	System.out.println("Amount of SSH conn - TCPport 22:\t" + window_datagramSumTCP_SYN_port22);
 	System.out.println("Amount of TCP+SYN all conn:\t" + window_datagramSumTCP_allOther);
 	System.out.println("Amount of DNS conn - UDPport 53:\t" + window_datagramSumUDP_port53);
 	System.out.println("Amount of UDP all conn:\t" + window_datagramSumUDP_allOther);
 	System.out.println("Amount of IPv6 packets:\t" + window_packetSum_IPv6);
 
 	// + " packetSumIPv4W: " + window_packetSum_IPv4 + " packetSumIPv6W: " +
 	// window_packetSum_IPv6
 	// + " packetSumTCP SYN+port80 W: " + window_datagramSumTCP_SYN_port80 +
 	// " packetSumTCP all OtherW: "
 	// + window_datagramSumTCP_allOther +
 	// + " packetSumTCP SYN+port443W: " + " packetSumUDPport53W: "
 	// + window_datagramSumUDP_port53 + " packetSumUDPOtherW: " +
 	// window_datagramSumUDP_allOther);
 
     }
 
     private static void parseFrame(String[] frame, int i) {
 	switch (i) {
 	case 0:
 	    parseTime(frame[0]);
 	    break;
 	case 1:
 	    parseSize(frame[1]);
 	    break;
 	case 2:
 	    parseNetworkType(frame[2]);
 	    break;
 	// break;
 	// case 2:
 	// parseDatagramType(frame[3]);
 	// break;
 	case 3:
 	    // [4] tcp flag syn [5] tcp dst port
 	    parseTCPportDst(frame[3], frame[4]);
 	    break;
 	case 5:
 	    parseUDPportDst(frame[5]);
 	    break;
 	default:
 	    break;
 	}
 	// tail -f livewlan.pcap -n 100000000 | tshark -r - -T fields -e
 	// frame.time -e frame.len -e eth.type -e ip.proto -e tcp.flags.syn -e
 	// tcp.dstport -e udp.dstport -e header=y -E separator=- -E occurrence=f
 
     }
 
     private static void parseTCPportDst(String tcpSyn, String tcpPort) {
 	if (tcpSyn.contentEquals("1")) {
 	    switch (tcpPort) {
 	    case "443":
 		window_datagramSumTCP_SYN_port443++;
 		break;
 	    case "80":
 		window_datagramSumTCP_SYN_port80++;
 		break;
 	    case "25":
 		window_datagramSumTCP_SYN_port25++;
 		break;
 	    case "22":
 		window_datagramSumTCP_SYN_port22++;
 		break;
 	    case "587":
 		window_datagramSumTCP_SYN_port587++;
 		break;
 	    default:
 		window_datagramSumTCP_allOther++;
 		break;
 	    }
 	}
 	// else {
 	// window_datagramSumTCP_allOther++;
 	// }
     }
 
     private static void parseUDPportDst(String udpPort) {
 	switch (udpPort) {
 	case "53":
 	    window_datagramSumUDP_port53++;
 	    break;
 	default:
 	    window_datagramSumUDP_allOther++;
 	    break;
 	}
 
     }
 
     // private static void parseDatagramType(String datagramType) {
     // switch (datagramType) {
     // case "1":
     // // // icmp
     // // datagramOtherW++;
     // // break;
     // // case "6":
     // // // tcp
     // // datagramTCPW++;
     // // break;
     // case "17":
     // // udp
     // window_datagram_UDP_general++;
     // break;
     //
     // default:
     // window_datagram_allOther++;
     // break;
     // }
     //
     // }
 
     // private static void parseTCPAck(String tcpAck) {
     // switch (tcpAck) {
     // case "1":
     // System.out.println("tcpAck!");
     // packetSumTCPflagSYN++;
     // break;
     // case "0":
     // break;
     // default:
     // System.out.println("TCP ACK what? " + tcpAck);
     // System.exit(-1);
     // break;
     // }
     //
     // }
 
     // private static void parseTCPSyn(String tcpSyn, String tcpPortDst) {
     //
     // switch (tcpSyn) {
     // case "1":
     // System.out.println("tcpSyn!");
     // packetSumTCPflagSYN++;
     // break;
     // case "0":
     // break;
     // default:
     // System.out.println("TCP SYN what? " + tcpSyn);
     // // System.exit(-1);
     // break;
     // }
     //
     // }
 
     private static void parseNetworkType(String ethType) {
 	switch (ethType) {
 	case "0x0800":
 	    window_packetSum_IPv4++;
 	    break;
 	case "0x08dd":
 	    window_packetSum_IPv6++;
 	    break;
 	// case "0x0806":
 	// packetSumARP++;
 	// break;
 	// default:
 	// window_packet_allOther++;
 	// break;
 	}
 
     }
 
     private static void parseSize(String bytesOfFrame) {
 	window_sizeOfAll_frames_inbytes = window_sizeOfAll_frames_inbytes + Long.parseLong((String) bytesOfFrame);
 	// sizeOFallFramesWholePCAP = sizeOFallFramesWholePCAP +
 	// Long.parseLong((String) bytesOfFrame);
     }
 
     private static void parseTime(String dateInString) {
 	dateInString = dateInString.substring(0, dateInString.length() - 6);
 	// Oct 4, 2013 13:57:41.242398000
 	// Sep 18, 2013 13:54:47.509564000
 	String pattern = "MMM dd, yyyy HH:mm:ss.SSS";
 	SimpleDateFormat formatter = new SimpleDateFormat(pattern);
 
 	Date date;
 	try {
 	    date = formatter.parse(dateInString);
 	    // System.out.println(date);
 	    // System.out.println(date.getTime());
 	    lastFrameTime = date.getTime();
 
 	    if (lastFrameTime < 15000000) {
 		// 15000000 = GMT: Tue, 23 Jun 1970 14:40:00 GMT
 		//timeToAdd = System.currentTimeMillis();
 		lastFrameTime = lastFrameTime + timeToAdd;
 	    } else {
 		//timeToAdd = 0;
 		// lastFrameTime = lastFrameTime + timeToAdd;
 		
		lastFrameTime = lastFrameTime +0;		
 	    }
 	    
 
 	    if (lastEndOfWindow == 0) {
 		lastEndOfWindow = lastFrameTime;
 	    }
 
 	    // date = formatter.parse(dateInString);
 	    // if (lastEndOfWindow == 0) {
 	    // lastEndOfWindow = date.getTime();
 	    // }
 	    // lastFrameTime = date.getTime();
 	    // if(lastFrameTime<1500000000){
 	    // //15000000 = GMT: Tue, 23 Jun 1970 14:40:00 GMT
 	    // timeToAdd=System.currentTimeMillis();
 	    // }else{
 	    // timeToAdd=0;
 	    // }
 	    // lastFrameTime=lastFrameTime+timeToAdd;
 
 	} catch (ParseException e) {
 	    e.printStackTrace();
 	}
 
     }
 
 }
