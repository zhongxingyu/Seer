 /*
  * Copylight (C) 2009, Shunichi Yamamoto, tkrworks.net
  *
  * This file is part of PICnomeSerial.
  *
  * PicnomeSerial is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option ) any later version.
  *
  * PicnomeSerial is distributed in the hope that it will be useful,
  * but WITHIOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.   See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with PicnomeSerial. if not, see <http:/www.gnu.org/licenses/>.
  *
  * PicnomeCommunication.java,v.1.3.10 2010/02/07
  */
 
 // RXTX
 import gnu.io.*;
 
 // JavaOSC
 import com.illposed.osc.*;
 import com.illposed.osc.utility.*;
 
 // JavaMIDI
 import javax.sound.midi.*;
 
 import javax.swing.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 public class PicnomeCommunication {
   Vector<String> device_list = new Vector<String>();
   Vector<String> midiinput_list = new Vector<String>();
   Vector<String> midioutput_list = new Vector<String>();
   ArrayList<MidiDevice.Info> midiinputdevices = new ArrayList<MidiDevice.Info>();
   ArrayList<MidiDevice.Info> midioutputdevices = new ArrayList<MidiDevice.Info>();
   JButton openclose_b, mididetail_b;
   JComboBox protocol_cb, device_cb, cable_cb, midiinput_cb, midioutput_cb, midiparameter_cb;
   JTextField hostaddress_tf, prefix_tf, hostport_tf, listenport_tf, hex_tf;
   JSpinner intensity_s, startcolumn_s, startrow_s;
   JCheckBox[] adc_ck = new JCheckBox[7];
   JButton led_clear_b,led_test_b, hex_b, update_b;
   JProgressBar update_pb;
 
 /* for DEBUG
   JTextField debug_tf;
   JTextField debug2_tf;
 */
 
   int max_picnome_num = 2;
   CommPortIdentifier[] portId = new CommPortIdentifier[2];
   SerialPort[] port = new SerialPort[2];
   InputStream[] in = new InputStream[2];
   InputStreamReader[] inr = new InputStreamReader[2];
   OutputStream[] out = new OutputStream[2];
 
   OSCPortIn oscpin;
   OSCPortOut oscpout;
 
   //for Mac OS X
   MidiDevice midiin, midiout;
   Receiver midi_r;
   Transmitter midi_t;
   int midi_in_port, midi_out_port, midi_pgm_number, prev_index;
   boolean para_change_flag;
 
   String[] device = new String[2];
   String[] device2 = new String[2];
   String[] host_address = new String[2];
   int[] host_port = new int[2];
   int[] listen_port = new int[2];
   String[] connect_state = new String[2];
   String[] cable_orientation = new String[2];
   String[] address_pattern_prefix = new String[2];
   int[] intensity = new int[2];
   int[] starting_column = new int[2];
   int[] starting_row = new int[2];
   int[] co_max_num = new int[2];
   boolean[][] adc_enable = new boolean[2][7];
   int[][][] midi_parameter = new int[16][8][5];;
 
   PicnomeCommunication() {
     this.in[0] = null;
     this.out[0] = null;
     this.in[1] = null;
     this.out[1] = null;
     this.initDeviceList();
 
     this.host_address[0] = "127.0.0.1";
     this.host_address[1] = "127.0.0.1";
     this.host_port[0] = 8000;
     this.host_port[1] = 8000;
     this.listen_port[0] = 8080;
     this.listen_port[1] = 8080;
     this.connect_state[0] = "Open";
     this.connect_state[1] = "Open";
     this.cable_orientation[0] = "left";
     this.cable_orientation[1] = "left";
     this.address_pattern_prefix[0] = "/test";
     this.address_pattern_prefix[1] = "/test";
     this.intensity[0] = 15;
     this.intensity[1] = 15;
     this.starting_column[0] = 0;
     this.starting_column[1] = 0;
     this.starting_row[0] = 0;
     this.starting_row[1] = 0;
     this.co_max_num[0] = 7;
     this.co_max_num[1] = 7;
     for(int i = 0; i < 7; i++) {
       this.adc_enable[0][i] = false;
       this.adc_enable[1][i] = false;
     }
     this.initMIDIPort();
   }
 
   ArrayList<String> getUsbInfo(String name) {
     String id = "none";
     String iousbdevices = new String();
 
     try {
       ProcessBuilder pb = new ProcessBuilder("ioreg", "-w", "0", "-S", "-p", "IOUSB", "-n", name, "-r");
       Process p = pb.start();
       InputStream is = p.getInputStream();
 
       int c;
       while((c = is.read()) != -1)
         iousbdevices += (new Character((char)c)).toString();
       is.close();
     }catch(IOException e) {}
 
     ArrayList<String> sfx = new ArrayList<String>();
     ArrayList<String> vid = new ArrayList<String>();
     ArrayList<String> pid = new ArrayList<String>();
     while(iousbdevices.indexOf(name) != -1) {
       int pos_start = iousbdevices.indexOf(name);
       int pos_end = iousbdevices.indexOf(" }");
       String iousbdevice = iousbdevices.substring(pos_start, pos_end);
 
       if(iousbdevice.indexOf(name + "@") != -1) {
         pos_start = iousbdevice.indexOf(name + "@") + name.length() + 1;
         pos_end = iousbdevice.indexOf("00");
         if(pos_start != -1)
           id = iousbdevice.substring(pos_start, pos_end);
         sfx.add(id);
       }
 
       if((pos_start = iousbdevice.indexOf("idVendor")) != -1) {
         pos_end = iousbdevice.length();
       
         iousbdevice = iousbdevice.substring(pos_start, pos_end);
         pos_end = iousbdevice.indexOf("\n");
         id = iousbdevice.substring(iousbdevice.indexOf(" = ") + 3, pos_end);
         vid.add(id);
       }
 
       iousbdevice = iousbdevices.substring(iousbdevices.indexOf(name), iousbdevices.indexOf(" }"));
       if((pos_start = iousbdevice.indexOf("idProduct")) != -1) {
         pos_end = iousbdevice.length();
       
         iousbdevice = iousbdevice.substring(pos_start, pos_end);
         pos_end = iousbdevice.indexOf("\n");
         id = iousbdevice.substring(iousbdevice.indexOf(" = ") + 3, pos_end);
         pid.add(id);
       }
       iousbdevices = iousbdevices.substring(iousbdevices.indexOf(" }") + 2, iousbdevices.length());
 
       if((vid.get(vid.size() - 1).equals("1240") && (pid.get(pid.size() - 1).equals("65477") || pid.get(pid.size() - 1).equals("64768"))) != true) {
         sfx.remove(sfx.size() - 1);
         vid.remove(vid.size() - 1);
         pid.remove(pid.size() - 1);
       }
     }
     return sfx;
   }
 
   void initDeviceList() {
     int dev_num = 0;
     String device_name;
     ArrayList<String> suffix0 = this.getUsbInfo("IOUSBDevice");
     ArrayList<String> suffix1 = this.getUsbInfo("PICnome");
     ArrayList<String> suffix2 = this.getUsbInfo("PICnome128");
     Enumeration e = CommPortIdentifier.getPortIdentifiers();
     while(e.hasMoreElements()) {
       device_name = ((CommPortIdentifier)e.nextElement()).getName();
 
       if(device_name.indexOf("/dev/cu.usbmodem") != -1) {
         for(int i = 0; i < suffix0.size(); i++) {
           if(device_name.indexOf(suffix0.get(i)) != -1) {
             this.device[dev_num] = "tkrworks-PICnome-" + suffix0.get(i);
             this.device2[dev_num] = device_name;
             dev_num++;
             this.device_list.add("tkrworks-PICnome-" + suffix0.get(i));
           }
         }
         for(int i = 0; i < suffix2.size(); i++) {//for one twenty eight
           if(device_name.indexOf(suffix2.get(i)) != -1) {
             this.device[dev_num] = "tkrworks-PICnome128-" + suffix2.get(i);
             this.device2[dev_num] = device_name;
             dev_num++;
             this.device_list.add("tkrworks-PICnome128-" + suffix2.get(i));
           }
         }
         for(int i = 0; i < suffix1.size(); i++) {//for sixty four
           if(device_name.indexOf(suffix1.get(i)) != -1) {
             this.device[dev_num] = "tkrworks-PICnome-" + suffix1.get(i);
             this.device2[dev_num] = device_name;
             dev_num++;
             this.device_list.add("tkrworks-PICnome-" + suffix1.get(i));
           }
         }
       }
     }
   }
 
   void changeDeviceSettings(int index) {
     this.connect_state[1 - index] = this.openclose_b.getText();
 
     this.hostaddress_tf.setText(this.host_address[index]);
     this.hostport_tf.setText(Integer.toString(this.host_port[index]));
     this.listenport_tf.setText(Integer.toString(this.listen_port[index]));
     this.openclose_b.setText(this.connect_state[index]);
     this.cable_cb.setSelectedItem(this.cable_orientation[index]);
     this.prefix_tf.setText(this.address_pattern_prefix[index]);
     this.intensity_s.setValue(this.intensity[index]);
     this.startcolumn_s.setValue(this.starting_column[index]);
     this.startrow_s.setValue(this.starting_row[index]);
 
     for(int i = 0; i < 7; i++)
       this.adc_ck[i].setSelected(this.adc_enable[index][i]);
   }
 
   boolean openSerialPort(int index) {
     try {
       if(this.device[index].indexOf("PICnome128") != -1)
         this.co_max_num[index] = 15;
       else
         this.co_max_num[index] = 7;
       this.portId[index] = CommPortIdentifier.getPortIdentifier(this.device2[index]);
       this.port[index] = (SerialPort)portId[index].open("PICnomeSerial", 2000);
     }
     catch (NoSuchPortException e) {
       e.printStackTrace();
       return false;
     }
     catch (PortInUseException e) {
       e.printStackTrace();
       return false;
     }
 
     try {
       this.in[index] = this.port[index].getInputStream();
       this.inr[index] = new InputStreamReader(this.in[index]);
       this.out[index] = this.port[index].getOutputStream();
       this.initSerialListener(index);
 
 
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control")) {
         this.initOSCPort();
         this.initOSCListener("all");
       }
       else//for MIDI
         this.openMIDIPort();
 
       //sy this.initOSCPort();
       //sy this.initOSCListener("all");
       //sy this.openMIDIPort();
 
     }
     catch(IOException e) {}
     return true;
   }
 
   boolean setSerialPort(int index) {
     try {
       this.port[index].setSerialPortParams(230400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
       this.port[index].setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
     }
     catch (UnsupportedCommOperationException e) {
       e.printStackTrace();
       return false;
     }
     this.port[index].setDTR(true);
     this.port[index].setRTS(false);
     return true;
   }
 
   boolean closeSerialPort(int index) {
     try {
       if(((String)this.protocol_cb.getSelectedItem()).equals("MIDI")) {
         //sy this.midiio.closeInput(this.midi_in_port);
       }
 
       this.inr[index].close();
       this.in[index].close();
       this.out[index].flush();
       this.out[index].close();
       this.port[index].close();
 
     }
     catch(Exception e) {
       e.printStackTrace();
       return false;
     }
     return true;
   }
 
   void initOSCPort() {
     byte[] hostaddress = new byte[4];
     String ha_str = this.hostaddress_tf.getText();
 
     int idx = 0, idx2;
     for(int i = 0; i < 3; i++) {
       idx2 = ha_str.indexOf(".", idx);
       hostaddress[i] = Byte.parseByte(ha_str.substring(idx, idx2));
       idx = idx2 + 1;
     }
     hostaddress[3] = Byte.parseByte(ha_str.substring(idx, ha_str.length()));
 
     try {
       this.oscpin = new OSCPortIn(Integer.parseInt(this.listenport_tf.getText()));
       this.oscpout = new OSCPortOut(InetAddress.getByAddress(hostaddress), Integer.parseInt(this.hostport_tf.getText()));
     }
     catch(UnknownHostException e){}
     catch(SocketException e){}
   }
 
   //sy MIDI Setup
   public void initMIDIPort() {
     MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
     MidiDevice device;
     for(MidiDevice.Info info : infos) {
       try {
         device = MidiSystem.getMidiDevice(info);
       }
       catch(MidiUnavailableException e) {
         e.printStackTrace();
         continue;
       }
       if(device.getMaxTransmitters() == 0)
         continue;
       this.midiinput_list.add(info.getName());
       this.midiinputdevices.add(info);
     }
 
     for(MidiDevice.Info info : infos) {
       try {
         device = MidiSystem.getMidiDevice(info);
       }
       catch(MidiUnavailableException e) {
         e.printStackTrace();
         continue;
       }
       if(device.getMaxReceivers() == 0)
         continue;
       this.midioutput_list.add(info.getName());
       this.midioutputdevices.add(info);
     }
   }
 /*
   public void initMIDIPort()
   {
     this.midiio = MidiIO.getInstance();
     this.midiio.printDevices();
     int in_num = midiio.numberOfInputDevices();
     for(int i = 0; i < in_num; i++)
       this.midiinput_list.add(midiio.getInputDeviceName(i));
     int out_num = midiio.numberOfOutputDevices();
     for(int i = 0; i < out_num; i++)
       this.midioutput_list.add(midiio.getOutputDeviceName(i));
   }
 */
 
   public void openMIDIPort() {
     try {
       this.midiin = MidiSystem.getMidiDevice(this.midiinputdevices.get(this.midiinput_cb.getSelectedIndex()));
       this.midiin.open();
       this.midi_t = this.midiin.getTransmitter();
       this.enableMidiLed();
 
       this.midiout = MidiSystem.getMidiDevice(this.midioutputdevices.get(this.midioutput_cb.getSelectedIndex()));
       this.midiout.open();
       this.midi_r = this.midiout.getReceiver();
     }
     catch(MidiUnavailableException mue){}
   }
 /*
   public void openMIDIPort()
   {
     this.midi_in_port = this.midiinput_cb.getSelectedIndex();
     this.midiin.plug(this, "enableMIDILed", this.midi_in_port, 0);
 
     this.midi_out_port = this.midioutput_cb.getSelectedIndex();
     for(int i = 0; i < 128; i++)
       this.midiout[i] = this.midiio.getMidiOut(0, this.midi_out_port);
   }
 */
 
   boolean checkAddressPatternPrefix(OSCMessage message, int index) {
     boolean b;
     String address = message.getAddress();
     int location = address.lastIndexOf("/");
     String prefix = address.substring(0, location);
 
     if(this.address_pattern_prefix[index] != null && prefix.equals(this.address_pattern_prefix[index]))
       b = true;
     else
       b = false;
 
     return b;
   }
 
   void sendOSCMessageFromHw(int index, String str) {
     StringTokenizer st = new StringTokenizer(str);
     Object[] args;
     OSCMessage msg;
     String token = st.nextToken();
 
     if(token.equals("press")) {
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control")) {
         args = new Object[3];
 
         int sc = this.starting_column[index];
         int sr = this.starting_row[index];
 
         if(this.cable_orientation[index].equals("left")) {
           args[0] = Integer.valueOf(st.nextToken()) + sc; // X
           args[1] = Integer.valueOf(st.nextToken()) + sr; // Y
         }
         else if(this.cable_orientation[index].equals("right")) {
           args[0] = this.co_max_num[index] - Integer.valueOf(st.nextToken()) + sc; // X
           args[1] = 7 - Integer.valueOf(st.nextToken()) + sr; // Y
         }
         else if(this.cable_orientation[index].equals("up")) {
           args[1] = this.co_max_num[index] - Integer.valueOf(st.nextToken()) + sr; // Y
           args[0] = Integer.valueOf(st.nextToken()) + sc;     // X
         }
         else if(this.cable_orientation[index].equals("down")) {
           args[1] = Integer.valueOf(st.nextToken()) + sr;     // Y
           args[0] = 7 - Integer.valueOf(st.nextToken()) + sc; // X
         }
         args[2] = Integer.valueOf(st.nextToken()); // State
 
         //debug this.debug_tf.setText(args[0] + " " + args[1] + " " + args[2]);
         msg = new OSCMessage(this.address_pattern_prefix[index] + "/press", args);
         try {
           this.oscpout.send(msg);
         }
         catch(IOException e){}
       }
       else {// for MIDI
         int notex = Integer.valueOf(st.nextToken());
         int notey = Integer.valueOf(st.nextToken());
         int state = Integer.valueOf(st.nextToken());
         int note_number = notex + (notey * (this.co_max_num[index] + 1));
         //sy this.debug_tf.setText(Integer.toString(note_number));
 
         try {
           ShortMessage sm = new ShortMessage();
           if(state == 1)
             sm.setMessage(ShortMessage.NOTE_ON, (byte)note_number, 127);
           else
             sm.setMessage(ShortMessage.NOTE_OFF, (byte)note_number, 0);
           this.midi_r.send(sm, 1);
         }
         catch(InvalidMidiDataException imde) {}
 /*
         Note note;
         if(state == 1)
         {
           note = new Note(note_number, this.midi_parameter[notex][notey][1], this.midi_parameter[notex][notey][3]);
         }
         else
         {
           note = new Note(note_number, this.midi_parameter[notex][notey][2], this.midi_parameter[notex][notey][4]);
         }
         this.midiout[note_number].sendNote(note);
 */
       }
     }
     else if(token.equals("input")) {
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control")) {
         args = new Object[2];
 
         args[0] = Integer.valueOf(st.nextToken()); // Pin
         args[1] = Integer.valueOf(st.nextToken()); // State
 
         msg = new OSCMessage(this.address_pattern_prefix[index] + "/input", args);
 
         try {
           this.oscpout.send(msg);
         }
         catch(IOException e) {}
       }
 /*
       else//for MIDI
       {
         int pin = Integer.valueOf(st.nextToken()); // Pin
         int state = Integer.valueOf(st.nextToken()); // State
 
         if(pin == 0 && state == 1)
           this.midi_pgm_number++;
         else if(pin == 1 && state == 1)
           this.midi_pgm_number--;
 
         if(this.midi_pgm_number > 127)
           this.midi_pgm_number = 127;
         else if(this.midi_pgm_number < 0)
           this.midi_pgm_number = 0;
 
         //sy this.midiout.sendProgramChange(new ProgramChange(this.midi_pgm_number));
         this.midiout[0].sendProgramChange(new ProgramChange(this.midi_pgm_number));
       }
 */
     }
     else if(token.equals("adc")) {
       if(((String)this.protocol_cb.getSelectedItem()).equals("Open Sound Control")) {
         args = new Object[2];
 
         args[0] = Integer.valueOf(st.nextToken()); // Pin
         args[1] = Float.valueOf(st.nextToken());   // Value
 
         msg = new OSCMessage(this.address_pattern_prefix[index] + "/adc", args);
 
         try {
           this.oscpout.send(msg);
         }
         catch(IOException e){}
       }
 /*
       else//for MIDI
       {
         int ctrl_number = Integer.valueOf(st.nextToken()); // Pin
         int ctrl_value = (int)(127.0 * Float.valueOf(st.nextToken()));   // Value
         Controller cc = new Controller(ctrl_number, ctrl_value);
 
         //sy this.midiout.sendController(new Controller(ctrl_number, ctrl_value));
         this.midiout[0].sendController(new Controller(ctrl_number, ctrl_value));
       }
 */
     }
     else if(token.equals("report")) {
       int v1,v2;
 
       v1 = Integer.valueOf(st.nextToken());
       v2 = Integer.valueOf(st.nextToken());
 
       if(v2 == 1) {
         this.hex_tf.setEnabled(false);
         this.hex_b.setEnabled(false);
         this.update_b.setEnabled(false);
       }
     }
   }
 
   public void enableMsgLed() {
     OSCListener listener = new OSCListener() {
         public synchronized void acceptMessage(java.util.Date time, OSCMessage message) {
           String address = message.getAddress();
           Object[] args = message.getArguments();
 
           int args0 = (int)Float.parseFloat(args[0].toString());
           int args1 = (int)Float.parseFloat(args[1].toString());
           int args2 = (int)Float.parseFloat(args[2].toString());
 
           int[] sc = new int[2];
           int[] sr = new int[2];
 
           for(int i = 0; i < 2; i++) {
             if(!checkAddressPatternPrefix(message, i))
               continue;
 
             sc[i] = starting_column[i];
             sr[i] = starting_row[i];
 
             if(cable_orientation[i].equals("left")) {
               //sy sc[i] = (Integer)args[0] - sc[i];
               //sy sr[i] = (Integer)args[1] - sr[i];
               sc[i] = args0 - sc[i];
               sr[i] = args1 - sr[i];
             }
             else if(cable_orientation[i].equals("right")) {
               //sy sc[i] = co_max_num[i] - (Integer)args[0] + sc[i];
               //sy sr[i] = 7 - (Integer)args[1] + sr[i];
               sc[i] = co_max_num[i] - args0 + sc[i];
               sr[i] = 7 - args1 + sr[i];
             }
             else if(cable_orientation[i].equals("up")) {
               //sy int sc1 = co_max_num[i] - (Integer)args[1] + sr[i];
               //sy int sr1 = (Integer)args[0] - sc[i];
               int sc1 = co_max_num[i] - args1 + sr[i];
               int sr1 = args0 - sc[i];
               sc[i] = sc1;
               sr[i] = sr1;
             }
             else if(cable_orientation[i].equals("down")) {
               //sy int sc1 = (Integer)args[1] - sr[i];
               //sy int sr1 = 7 - (Integer)args[0] + sc[i];
               int sc1 = args1 - sr[i];
               int sr1 = 7 - args0 + sc[i];
               sc[i] = sc1;
               sr[i] = sr1;
             }
             
             if(sc[i] < 0 || sr[i] < 0) continue ;
             
             try {
               //sy String str =new String("led " + sc[i] + " " + sr[i] + " " + (Integer)args[2] + (char)0x0D);
               String str =new String("led " + sc[i] + " " + sr[i] + " " + args2 + (char)0x0D);
               //debug debug_tf.setText(str);
               if(portId[i] != null && portId[i].isCurrentlyOwned()) {
                 out[i].write(str.getBytes());
                wait(0, 20);
               }
             }
             catch(IOException e) {}
             catch(InterruptedException e) {}
           }//end for
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/led", listener);
   }
 
   public void enableMidiLed() {
     Receiver rcv = new Receiver() {
         public void close() {}
  
         public void send(MidiMessage message, long timeStamp) {
           byte[] data = message.getMessage();
  
           if((256 + data[0]) == 144 || (256 + data[0]) == 128) {// NOTE_ON -> 144, NOTE_OFF -> 128
             int sc = (Integer)startcolumn_s.getValue();
             int sr = (Integer)startrow_s.getValue();
             sc = (data[1] % 8) - sc;
             sr = (data[1] / 8) - sr;
             if(sc < 0) sc = 0;
             if(sr < 0) sr = 0;
  
             try {
               String str;
               if(data[2] == 0)
                 str =new String("led " + sc + " " + sr + " " + 0 + (char)0x0D);
               else
                 str =new String("led " + sc + " " + sr + " " + 1 + (char)0x0D);
 
               //debug debug_tf.setText(str);
               out[0].write(str.getBytes());
             }
             catch(IOException e) {}
           }
         }
       };
     this.midi_t.setReceiver(rcv);
   }
 /*
   //sy void enableMIDILed(Note note)
   public void noteOnReceived(Note note)
   {
     this.debug_tf.setText("test");
     if(((String)this.protocol_cb.getSelectedItem()).equals("MIDI"))
     {
       int pitch = note.getPitch();
       int velocity = note.getVelocity();
 
       int[] sc = new int[2];
       int[] sr = new int[2];
 
       for(int i = 0; i < 2; i++)
       {
         if(this.co_max_num[i] == 7)
         {
           if(pitch > 63) return;
 
           sc[i] = (pitch % 8);
           sr[i] = (pitch / 8);
         }
         else if(this.co_max_num[i] == 15)
         {
           if(pitch > 127) return;
 
           sc[i] = (pitch % 16);
           sr[i] = (pitch / 16);
         }
 
         try
         {
           String str;
           if(velocity == 0)
             str =new String("led " + sc[i] + " " + sr[i] + " " + 0 + (char)0x0D);
           else
             str =new String("led " + sc[i] + " " + sr[i] + " " + 1 + (char)0x0D);
           this.out[i].write(str.getBytes());
         }
         catch(IOException e){}
       }
     }
   }
 */
 
   public void enableMsgLedCol() {
     OSCListener listener = new OSCListener() {
        public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
 
           int args0 = (int)Float.parseFloat(args[0].toString());
           int args1 = (int)Float.parseFloat(args[1].toString());
 
           int[] sc = new int[2];
           int[] sr = new int[2];
 
           for(int j = 0; j < 2; j++) {
             if(!checkAddressPatternPrefix(message, j))
               continue;
 
             if(cable_orientation[j].equals("left"))
               //sy sc[j] = (Integer)args[0] - starting_column[j];
               sc[j] = args0 - starting_column[j];
             else if(cable_orientation[j].equals("right"))
               //sy sc[j] = co_max_num[j] - (Integer)args[0] + starting_column[j];
               sc[j] = co_max_num[j] - args0 + starting_column[j];
             else if(cable_orientation[j].equals("up"))
               //sy sc[j] = (Integer)args[0] - starting_column[j];
               sc[j] = args0 - starting_column[j];
             else if(cable_orientation[j].equals("down"))
               //sy sc [j]= 7 - (Integer)args[0] + starting_column[j];
               sc [j]= 7 - args0 + starting_column[j];
 
             if(sc[j] < 0) continue ;
 
             int shift = starting_row[j] % (co_max_num[j] + 1);
 
             if(cable_orientation[j].equals("left"))
               //sy sr[j] = (short)(((Integer)args[1]).shortValue() >> shift);
               sr[j] = (short)(args1 >> shift);
             else if(cable_orientation[j].equals("right")) {
               //sy short sr0 = ((Integer)args[1]).shortValue();
               short sr0 = (short)args1;
               short sr1 = 0;
               for(int i = 0; i < 8; i++)
                 if((sr0 & (0x01 << i)) == (0x01 << i))
                   sr1 |= (0x01 << (7 - i));
               sr[j] = (short)(sr1 << shift);
             }
             else if(cable_orientation[j].equals("up")) {
               //sy short sr0 = ((Integer)args[1]).shortValue();
               short sr0 = (short)args1;
               short sr1 = 0;
               for(int i = 0; i < co_max_num[j] + 1; i++)
                 if((sr0 & (0x01 << i)) == (0x01 << i))
                   sr1 |= (0x01 << (co_max_num[j] - i));
               sr[j] = (short)(sr1 << shift);
             }
             else if(cable_orientation[j].equals("down"))
               //sy sr[j] = (short)(((Integer)args[1]).shortValue() >> shift);
               sr[j] = (short)(args1 >> shift);
 
             try {
               String str;
               if(cable_orientation[j].equals("left") || cable_orientation[j].equals("right"))
                 str =new String("led_col " + sc[j] + " " + sr[j] + (char)0x0D);
               else
                 str =new String("led_row " + sc[j] + " " + sr[j] + (char)0x0D);
               //debug debug_tf.setText(str);
              if(portId[j] != null && portId[j].isCurrentlyOwned())
                 out[j].write(str.getBytes());
             }
             catch(IOException e){}
           }//end for
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/led_col", listener);
   }
 
   public void enableMsgLedRow() {
     OSCListener listener = new OSCListener() {
        public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
 
           int args0 = (int)Float.parseFloat(args[0].toString());
           int args1 = (int)Float.parseFloat(args[1].toString());
 
           int[] sc = new int[2];
           int[] sr = new int[2];
 
           for(int j = 0; j < 2; j++) {
             if(!checkAddressPatternPrefix(message, j))
               continue;
 
             if(cable_orientation[j].equals("left"))
               //sy sr[j] = (Integer)args[0] - starting_row[j];
               sr[j] = args0 - starting_row[j];
             else if(cable_orientation[j].equals("right"))
               //sy sr[j] = 7 - (Integer)args[0] + starting_row[j];
               sr[j] = 7 - args0 + starting_row[j];
             else if(cable_orientation[j].equals("up"))
               //sy sr[j] = co_max_num[j] - (Integer)args[0] + starting_row[j];
               sr[j] = co_max_num[j] - args0 + starting_row[j];
             else if(cable_orientation[j].equals("down"))
               //sy sr[j] = (Integer)args[0] - starting_row[j];
               sr[j] = args0 - starting_row[j];
             
             if(sr[j] < 0) continue;
             
             int shift = starting_column[j] % (co_max_num[j] + 1);
             
             if(cable_orientation[j].equals("left"))
               //sy sc[j] = (short)(((Integer)args[1]).shortValue() >> shift);
               sc[j] = (short)(args1 >> shift);
             else if(cable_orientation[j].equals("right")) {
               //sy short sc0 = ((Integer)args[1]).shortValue();
               short sc0 = (short)args1;
               short sc1 = 0;
               for(int i = 0; i < co_max_num[j] + 1; i++)
                 if((sc0 & (0x01 << i)) == (0x01 << i))
                   sc1 |= (0x01 << (co_max_num[j] - i));
               sc[j] = (short)(sc1 << shift);
             }
             else if(cable_orientation[j].equals("up"))
               //sy sc[j] = (short)(((Integer)args[1]).shortValue() >> shift);
               sc[j] = (short)(args1 >> shift);
             else if(cable_orientation[j].equals("down")) {
               //sy short sc0 = ((Integer)args[1]).shortValue();
               short sc0 = (short)args1;
               short sc1 = 0;
               for(int i = 0; i < 8; i++)
                 if((sc0 & (0x01 << i)) == (0x01 << i))
                   sc1 |= (0x01 << (7 - i));
               sc[j] = (short)(sc1 << shift);
             }
 
             try {
               String str;
               if(cable_orientation[j].equals("left") || cable_orientation[j].equals("right"))
                 str =new String("led_row " + sr[j] + " " + sc[j] + (char)0x0D);
               else
                 str =new String("led_col " + sr[j] + " " + sc[j] + (char)0x0D);
 
               //debug debug_tf.setText(str);
              if(portId[j] != null && portId[j].isCurrentlyOwned())
                 out[j].write(str.getBytes());
             }
             catch(IOException e){}
           }//end for
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/led_row", listener);
   }
 
   public void enableMsgLedFrame() {
     OSCListener listener = new OSCListener() {
        public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args0 = message.getArguments();
           int[] sc = new int[2];
           int[] sr = new int[2];
 
           int[] args = new int[16];
           for(int i = 0; i < args0.length; i++)
             args[i] = (int)Float.parseFloat(args0[i].toString());
 
           for(int k = 0; k < 2; k++) {
             if(!checkAddressPatternPrefix(message, k))
               continue;
 
             int shift = starting_column[k] % (co_max_num[k] + 1);
 
             for(int i = 0; i < 16; i++) {
               if(cable_orientation[k].equals("left"))
                 sr[k] = i - starting_row[k];
               else if(cable_orientation[k].equals("right"))
                 sr[k] = 7 - i + starting_row[k];
               else if(cable_orientation[k].equals("up"))
                 sr[k] = co_max_num[k] - i + starting_row[k];
               else if(cable_orientation[k].equals("down"))
                 sr[k] = i - starting_row[k];
 
               if(i < starting_row[k] || (i - starting_row[k]) > 7) continue;
 
               if(cable_orientation[k].equals("left"))
                 //sy sc[k] = (short)(((Integer)args[i]).shortValue() >> shift);
                 sc[k] = (short)(args[i] >> shift);
               else if(cable_orientation[k].equals("right")) {
                 //sy short sc0 = ((Integer)args[i]).shortValue();
                 short sc0 = (short)args[i];
                 short sc1 = 0;
                 for(int j = 0; j < co_max_num[k] + 1; j++)
                   if((sc0 & (0x01 << j)) == (0x01 << j))
                     sc1 |= (0x01 << (co_max_num[k] - j));
                 sc[k] = (short)(sc1 << shift);
               }
               else if(cable_orientation[k].equals("up"))
                 //sy sc[k] = (short)(((Integer)args[i]).shortValue() >> shift);
                 sc[k] = (short)(args[i] >> shift);
               else if(cable_orientation[k].equals("down")) {
                 //sy short sc0 = ((Integer)args[i]).shortValue();
                 short sc0 = (short)args[i];
                 short sc1 = 0;
                 for(int j = 0; j < 8; j++)
                   if((sc0 & (0x01 << j)) == (0x01 << j))
                     sc1 |= (0x01 << (7 - j));
                 sc[k] = (short)(sc1 << shift);
               }
               
               try {
                 String str;
                 if(cable_orientation[k].equals("left") || cable_orientation[k].equals("right"))
                   str =new String("led_row " + sr[k] + " " + sc[k] + (char)0x0D);
                 else
                   str =new String("led_col " + sr[k] + " " + sc[k] + (char)0x0D);
                 
                 //debug debug_tf.setText(str);
                if(portId[k] != null && portId[k].isCurrentlyOwned())
                   out[k].write(str.getBytes());
               }
               catch(IOException e) {}
             }//end for i
           }//end for j
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/frame", listener);
   }
 
   public void enableMsgClear() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
 
           int args0 = 0;
           if(args.length == 1)
             args0 = (int)Float.parseFloat(args[0].toString());
 
           for(int j = 0; j < 2; j++) {
             if(!checkAddressPatternPrefix(message, j))
               continue;
 
             for(int i = 0; i < 8; i++) {
               short state;
               if(co_max_num[j] == 7) {//sixty four
                 //sy if(args.length == 0 || ((Integer)args[0]).intValue() == 0)
                 if(args.length == 0 || args0 == 0)
                   state = (short)0x00;
                 else
                   state = (short)0xFF;
               }
               else {//one twenty eight
                 //sy if(args.length == 0 || ((Integer)args[0]).intValue() == 0)
                 if(args.length == 0 || args0 == 0)
                   state = (short)0x0000;
                 else
                   state = (short)0xFFFF;
               }
 
               try {
                 String str =new String("led_row " + i + " " + state + (char)0x0D);
                 //debug debug_tf.setText(str);
                 if(portId[j] != null && portId[j].isCurrentlyOwned())
                   out[j].write(str.getBytes());
               }
               catch(IOException e){}
             }
           }
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/clear", listener);
   }
 
   public void enableMsgAdcEnable() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args0 = message.getArguments();
 
           int[] args = new int[args0.length];
           for(int i = 0; i < args.length; i++)
             args[i] = (int)Float.parseFloat(args0[i].toString());
 
           try {
             //sy String str =new String("adc_enable " + (Integer)args[0] + " " + (Integer)args[1] + (char)0x0D);
             String str =new String("adc_enable " + args[0] + " " + args[1] + (char)0x0D);
             //debug debug_tf.setText(str);
             if(portId[0] != null && portId[0].isCurrentlyOwned())
               out[0].write(str.getBytes());
             if(portId[1] != null && portId[1].isCurrentlyOwned())
               out[1].write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/adc_enable", listener);
   }
 
   public void enableMsgPwm() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args0 = message.getArguments();
 
           int[] args = new int[2];
           for(int i = 0; i < 2; i++)
             args[i] = (int)Float.parseFloat(args0[i].toString());
           float args_f = Float.parseFloat(args0[2].toString());
 
           try {
             //sy String str =new String("pwm " + (Integer)args[0] + " " + (Integer)args[1] + " " + (Float)args[2] + (char)0x0D);
             String str =new String("pwm " + args[0] + " " + args[1] + " " + args_f + (char)0x0D);
             //debug debug_tf.setText(str);
             if(portId[0] != null && portId[0].isCurrentlyOwned())
               out[0].write(str.getBytes());
             if(portId[1] != null && portId[1].isCurrentlyOwned())
               out[1].write(str.getBytes());
           }
           catch(IOException e) {}
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/pwm", listener);
   }
 
 /*sy
   public void enableMsgOutput()
   {
     OSCListener listener = new OSCListener()
       {
         public void acceptMessage(java.util.Date time, OSCMessage message)
         {
           Object[] args = message.getArguments();
 
           try
           {
             String str =new String("output " + (Integer)args[0] + " " + (Integer)args[1] + (char)0x0D);
             //debug debug_tf.setText(str);
             out.write(str.getBytes());
           }
           catch(IOException e){}
         }
       };
     this.oscpin.addListener(this.prefix_tf.getText() + "/output", listener);
   }
 */
 
   public void enableMsgDevice() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
           int device_no = ((Integer)args[0]).intValue();
           if(device_no == 0 || device_no == 1) {
             PicnomeCommunication.this.device_cb.setSelectedIndex(device_no);
             PicnomeCommunication.this.changeDeviceSettings(device_no);
           }
         }
       };
     this.oscpin.addListener("/sys/device", listener);
   }
 
   public void enableMsgOscconfig() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
           PicnomeCommunication.this.address_pattern_prefix[(Integer)args[2]] = (String)args[0];
           PicnomeCommunication.this.host_port[(Integer)args[2]] = (Integer)args[1];
           if(PicnomeCommunication.this.device_cb.getSelectedIndex() == (Integer)args[2]) {
             PicnomeCommunication.this.prefix_tf.setText((String)args[0]);
             PicnomeCommunication.this.hostport_tf.setText(((Integer)args[1]).toString());
           }
 
           if(args.length == 4) {
             PicnomeCommunication.this.host_address[(Integer)args[2]] = Integer.toString((Integer)args[3]);
             if(PicnomeCommunication.this.device_cb.getSelectedIndex() == (Integer)args[2])
               PicnomeCommunication.this.hostaddress_tf.setText(Integer.toString((Integer)args[3]));
           }
           PicnomeCommunication.this.initOSCPort();
           PicnomeCommunication.this.initOSCListener("all");
         }
       };
     this.oscpin.addListener("/sys/oscconfig", listener);
   }
 
   public void enableMsgPrefix() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
           if(args.length == 0) {
             for(int i = 0; i < device_list.size(); i++) {
               OSCMessage msg;
               Object[] args0 = new Object[2];
               args0[0] = String.valueOf(i);
               args0[1] = address_pattern_prefix[i];
               msg = new OSCMessage("/sys/prefix", args0);
               try {
                 oscpout.send(msg);
               }
               catch(IOException e) {}
             }
           }
           else if(args.length == 2) {
             PicnomeCommunication.this.address_pattern_prefix[(Integer)args[0]] = (String)args[1];
             if(PicnomeCommunication.this.device_cb.getSelectedIndex() == (Integer)args[0])
               prefix_tf.setText((String)args[1]);
             PicnomeCommunication.this.initOSCListener("prefix");
           }
           else {
             prefix_tf.setText((String)args[0]);
             PicnomeCommunication.this.initOSCListener("prefix");
           }
         }
       };
     this.oscpin.addListener("/sys/prefix", listener);
   }
 
   public void enableMsgIntensity() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
 
           for(int i = 0; i < 2; i++) {
             try {
               if(args.length == 1) {
                 String str =new String("intensity " + (Integer)args[0] + (char)0x0D);
                 //debug debug_tf.setText(str);
                 if(portId[i] != null && portId[i].isCurrentlyOwned())
                   out[i].write(str.getBytes());
               }
               else if(args.length == 2 && (Integer)args[0] == i) {
                 String str =new String("intensity " + (Integer)args[1] + (char)0x0D);
                 //debug debug_tf.setText(str);
                 if(portId[i] != null && portId[i].isCurrentlyOwned())
                   out[i].write(str.getBytes());
               }
             }
             catch(IOException e) {}
           }
         }
       };
     this.oscpin.addListener("/sys/intensity", listener);
   }
 
   public void enableMsgTest() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
 
           for(int i = 0; i < 2; i++) {
             try {
               if(args.length == 1) {
                 String str =new String("test " + (Integer)args[0] + (char)0x0D);
                 if(portId[i] != null && portId[i].isCurrentlyOwned())
                   out[i].write(str.getBytes());
               }
               else if(args.length == 2 && (Integer)args[0] == i) {
                 String str =new String("test " + (Integer)args[1] + (char)0x0D);
                 if(portId[i] != null && portId[i].isCurrentlyOwned())
                   out[i].write(str.getBytes());
               }
             }
             catch(IOException e) {}
           }
         }
       };
     this.oscpin.addListener("/sys/test", listener);
   }
 
   public void enableMsgShutdown() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
 
           for(int i = 0; i < 2; i++) {
             try {
               String str =new String("shutdown " + (Integer)args[0] + (char)0x0D);
               //debug debug_tf.setText(str);
               if(portId[i] != null && portId[i].isCurrentlyOwned())
                 out[i].write(str.getBytes());
             }
             catch(IOException e) {}
           }
         }
       };
     this.oscpin.addListener("/sys/shutdown", listener);
   }
 
   public void enableMsgReport() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
           Object[] args0;
           String str;
           OSCMessage msg;
 
           //debug debug_tf.setText(String.valueOf(args.length));
 
           try {
             if(args.length == 0) {
               args0 = new Object[1];
               args0[0] = String.valueOf(device_list.size());
               msg = new OSCMessage("/sys/devices", args0);
               oscpout.send(msg);
 
               for(int i = 0; i < device_list.size(); i++) {
                 args0 = new Object[2];
                 args0[0] = String.valueOf(i);
                 if(device[i].indexOf("128") != -1)
                   args0[1] = "128";
                 else
                   args0[1] = "64";
                 msg = new OSCMessage("/sys/type", args0);
                 oscpout.send(msg);
               }
 
               for(int i = 0; i < device_list.size(); i++) {
                 args0 = new Object[2];
                 args0[0] = String.valueOf(i);
                 args0[1] = address_pattern_prefix[i];
                 msg = new OSCMessage("/sys/prefix", args0);
                 oscpout.send(msg);
               }
 
               for(int i = 0; i < device_list.size(); i++) {
                 args0 = new Object[2];
                 args0[0] = String.valueOf(i);
                 args0[1] = cable_orientation[i];
                 msg = new OSCMessage("/sys/cable", args0);
                 oscpout.send(msg);
               }
 
               for(int i = 0; i < device_list.size(); i++) {
                 args0 = new Object[3];
                 args0[0] = String.valueOf(i);
                 args0[1] = starting_column[i];
                 args0[2] = starting_row[i];
                 msg = new OSCMessage("/sys/offset", args0);
                 oscpout.send(msg);
               }
             }
             else {
               str = new String("report " + (Integer)args[0] + (char)0x0D);
               //debug debug_tf.setText(str);
               if(portId[0] != null && portId[0].isCurrentlyOwned())
                 out[0].write(str.getBytes());
               if(portId[1] != null && portId[1].isCurrentlyOwned())
                 out[1].write(str.getBytes());
             }
           }
           catch(IOException e) {}
         }
       };
     this.oscpin.addListener("/sys/report", listener);
   }
 
   public void enableMsgType() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
           Object[] args0;
           String str;
           OSCMessage msg;
 
           try {
             if(args.length == 0) {
               for(int i = 0; i < device_list.size(); i++) {
                 args0 = new Object[2];
                 args0[0] = String.valueOf(i);
                 if(device[i].indexOf("128") != -1)
                   args0[1] = "128";
                 else
                   args0[1] = "64";
                 msg = new OSCMessage("/sys/type", args0);
                 oscpout.send(msg);
               }
             }
           }
           catch(IOException e) {}
         }
       };
     this.oscpin.addListener("/sys/type", listener);
   }
 
   public void enableMsgOffset() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
           if(args.length == 2) {
             PicnomeCommunication.this.startcolumn_s.setValue((Integer)args[0]);
             PicnomeCommunication.this.startrow_s.setValue((Integer)args[1]);
           }
           else if(args.length == 3) {
             PicnomeCommunication.this.starting_column[(Integer)args[0]] = (Integer)args[1];
             PicnomeCommunication.this.starting_row[(Integer)args[0]] = (Integer)args[2];
             if(PicnomeCommunication.this.device_cb.getSelectedIndex() == (Integer)args[0]) {
               PicnomeCommunication.this.startcolumn_s.setValue((Integer)args[1]);
               PicnomeCommunication.this.startrow_s.setValue((Integer)args[2]);
             }
           }
         }
       };
     this.oscpin.addListener("/sys/offset", listener);
   }
 
   public void enableMsgCable() {
     OSCListener listener = new OSCListener() {
         public void acceptMessage(java.util.Date time, OSCMessage message) {
           Object[] args = message.getArguments();
           if(args.length == 0) {
             for(int i = 0; i < device_list.size(); i++) {
               OSCMessage msg;
               Object[] args0 = new Object[2];
               args0[0] = String.valueOf(i);
               args0[1] = cable_orientation[i];
               msg = new OSCMessage("/sys/cable", args0);
               try {
                 oscpout.send(msg);
               }
               catch(IOException e){}
             }
           }
           else if(args.length == 2) {
             String costr = "";
             switch((Integer)args[1]) {
             case 0:
               costr = "left";
               break;
             case 1:
               costr = "up";
               break;
             case 2:
               costr = "right";
               break;
             case 3:
               costr = "down";
               break;
             }
             PicnomeCommunication.this.cable_orientation[(Integer)args[0]] = costr;
             if(PicnomeCommunication.this.device_cb.getSelectedIndex() == (Integer)args[0])
               PicnomeCommunication.this.cable_cb.setSelectedItem(costr);
             PicnomeCommunication.this.initOSCListener("prefix");
           }
           else
             cable_cb.setSelectedItem(((String)args[0]));
         }
       };
     this.oscpin.addListener("/sys/cable", listener);
   }
 
   public void initOSCListener(String str) {
     if(((String)this.device_cb.getSelectedItem()).equals(this.device[0]))
       this.address_pattern_prefix[0] = this.prefix_tf.getText();
     else if(((String)this.device_cb.getSelectedItem()).equals(this.device[1]))
       this.address_pattern_prefix[1] = this.prefix_tf.getText();
 
     if(str.equals("all") || str.equals("prefix")) {
       this.enableMsgLed();
       this.enableMsgLedCol();
       this.enableMsgLedRow();
       this.enableMsgLedFrame();
       this.enableMsgClear();
       this.enableMsgAdcEnable();
       this.enableMsgPwm();
       //sy this.enableMsgOutput();
     }
     if(str.equals("all")) {
       this.enableMsgDevice();
       this.enableMsgOscconfig();
       this.enableMsgPrefix();
       this.enableMsgIntensity();
       this.enableMsgTest();
       this.enableMsgShutdown();
       this.enableMsgReport();
       this.enableMsgType();
       this.enableMsgOffset();
       this.enableMsgCable();
 
       this.oscpin.startListening();
     }
   }
 
   class SerialPortListener implements SerialPortEventListener {
     private int index;
     private InputStreamReader inr;
 
     SerialPortListener(int index, InputStreamReader inr) {
       this.index = index;
       this.inr = inr;
     }
 
     public void serialEvent(SerialPortEvent event) {
       if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
         try {
           int buffer = 0;
           StringBuffer sb = new StringBuffer();
           while((buffer = this.inr.read()) != -1) {
             if(buffer != 0x0A || buffer != 0x0D)
               sb.append((char)buffer);
             if(buffer == 0x0A || buffer == 0x0D)
               break;
           }
           String str = sb.toString();
           //debug debug_tf.setText(this.index + " / " + str);
           if(sb.length() > 0)
             sendOSCMessageFromHw(this.index, str);
         }
         catch(IOException e) {}
       }
     }
   }
 
   void initSerialListener(int index) {
     try {
       this.port[index].addEventListener(new SerialPortListener(index, this.inr[index]));
       this.port[index].notifyOnDataAvailable(true);
       this.port[index].enableReceiveTimeout(1000);
     }
     catch (TooManyListenersException e) {}
     catch (UnsupportedCommOperationException e) {}
   }
 }
