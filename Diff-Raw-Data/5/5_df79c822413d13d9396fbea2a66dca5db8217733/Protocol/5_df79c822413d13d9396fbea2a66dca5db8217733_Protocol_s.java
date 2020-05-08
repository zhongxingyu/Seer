 package no.ntnu.osnap.com;
 
 import java.io.IOException;
 
 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 /**
  *
  * @author anders
  */
 public abstract class Protocol {

     public static final byte OPCODE_PING = 0;
     public static final byte OPCODE_TEXT = 1;
     public static final byte OPCODE_SENSOR = 2;
     public static final byte OPCODE_PIN_T = 3;
     public static final byte OPCODE_PIN_R = 4;
     public static final byte OPCODE_PIN_W = 5;
     public static final byte OPCODE_RESPONSE = (byte) 0xFE;
     public static final byte OPCODE_RESET = (byte) 0xFF;
     private Command currentCommand;
     private Byte waitingForAck;
     
     private static final byte[] ackProcessors = {
         OPCODE_SENSOR
     };
 
     public Protocol() {
         currentCommand = new Command();
         waitingForAck = null;
     }
     
     private final byte[] ack = {(byte)0xFF, (byte)0x04, (byte)0x00, (byte)0xFF, (byte)0x00};
     public final void ping(){
         lock();
         
         try {
             sendBytes(ack);
         } catch (IOException ex) {
             System.out.println("Derp send");
         }
         
         waitingForAck = OPCODE_PING;
         
         release();
     }
 
     public final void print(String text) {
         lock();
         int size = text.length() + 4;
 
         byte output[] = new byte[size];
 
         output[0] = (byte) 0xFF;
         output[1] = (byte) (size - 1);
         output[2] = OPCODE_TEXT;
         output[3] = 0; // Will eventually specify display
 
         for (int i = 4; i < size; ++i) {
             output[i] = text.getBytes()[i - 4];
         }
 
         waitingForAck = OPCODE_TEXT;
         try {
             sendBytes(output);
         } catch (IOException ex) {
             System.out.println("Send fail");
         }
         release();
     }
 
     public final int sensor(int sensor) {
         lock();
         int size = 5;
 
         byte output[] = new byte[size];
 
         output[0] = (byte) 0xFF;
         output[1] = (byte) (size - 1);
         output[2] = OPCODE_SENSOR;
         output[3] = (byte) sensor;
         output[4] = (byte) 0;
 
         waitingForAck = OPCODE_SENSOR;
 
         try {
             sendBytes(output);
         } catch (IOException ex) {
             System.out.println("Send fail");
         }
 
         release();
 
         while (waitingForAck != null) {
             try {
                 Thread.sleep(10);
             } catch (InterruptedException ex) {
             }
         }
 
         byte content[] = currentCommand.getContent();
 
         ackProcessingComplete();
 
         int sensorValue = (content[0] << 8) + toUnsigned(content[1]);
 
         return sensorValue;
     }
 
     public short toUnsigned(byte value) {
         if (value < 0) {
             return (short) ((short) value & (short) 0xFF);
         }
         return (short) value;
     }
 
     public final void toggle(int pin) {
         lock();
         int size = 5;
 
         byte output[] = new byte[size];
 
         output[0] = (byte) 0xFF;
         output[1] = (byte) (size - 1);
         output[2] = OPCODE_PIN_T;
         output[3] = (byte) pin;
         output[4] = (byte) 0;
 
         waitingForAck = OPCODE_PIN_T;
 
         try {
             sendBytes(output);
         } catch (IOException ex) {
             System.out.println("Send fail");
         }
         release();
     }
 
     public final void read(int pin) {
         lock();
         int size = 5;
 
         byte output[] = new byte[size];
 
         output[0] = (byte) 0xFF;
         output[1] = (byte) (size - 1);
         output[2] = OPCODE_PIN_R;
         output[3] = (byte) pin;
         output[4] = (byte) 0;
 
         waitingForAck = OPCODE_PIN_R;
 
         try {
             sendBytes(output);
         } catch (IOException ex) {
             System.out.println("Send fail");
         }
         release();
     }
 
     public final void write(int pin, boolean value) {
         lock();
         int size = 5;
 
         byte output[] = new byte[size];
 
         output[0] = (byte) 0xFF;
         output[1] = (byte) (size - 1);
         output[2] = OPCODE_PIN_W;
         output[3] = (byte) pin;
         output[4] = value ? (byte) 1 : (byte) 0;
 
         waitingForAck = OPCODE_PIN_W;
         try {
             sendBytes(output);
         } catch (IOException ex) {
             System.out.println("Send fail");
         }
         release();
     }
     private boolean locked = false;
 
     private void lock() {
         while (locked) {
             try {
                 Thread.sleep(10);
             } catch (InterruptedException ex) {
             }
         }
 
         locked = true;
 
         while (waitingForAck != null) {
             try {
                 Thread.sleep(10);
             } catch (InterruptedException ex) {
             }
         }
     }
 
     private void release() {
         if (!locked) {
             throw new IllegalStateException("Already released");
         }
         locked = false;
     }
     private boolean processingAck = false;
 
     private void ackProcessing() {
         processingAck = true;
 
         while (processingAck){
             try {
                 Thread.sleep(10);
             } catch (InterruptedException ex) {
                 
             }
         }
     }
 
     private void ackProcessingComplete() {
         processingAck = false;
     }
 
     public final synchronized void byteReceived(byte data) {
         if (currentCommand.byteReceived(data)) {
             // Process command
 
 
             if (currentCommand.isAckFor(waitingForAck)) {
                 byte tempAck = waitingForAck;
                 
                 waitingForAck = null;
                 
                 for (byte ack : ackProcessors){
                     if (tempAck == ack){
                         ackProcessing();
                         break;
                     }
                 }
                 currentCommand = new Command();
             } else {
                 throw new IllegalArgumentException("Received something unexpected");
             }
         }
     }
 
     public final synchronized void bytesReceived(byte[] data) {
         for (byte item : data) {
             byteReceived(item);
         }
     }
 
     public abstract void sendBytes(byte[] data) throws IOException;
 }
 
 class Command {
 
     private final byte START_BYTE = (byte) 0xFF;
 
     private enum State {
 
         STATE_START,
         STATE_SIZE,
         STATE_OPCODE,
         STATE_FLAG,
         STATE_CONTENT,
         STATE_DONE
     }
     private State state;
     private byte size;
     private byte opcode;
     private byte flag;
     private byte[] content;
     private int contentCounter;
 
     public Command() {
         state = State.STATE_START;
         contentCounter = 0;
     }
 
     public boolean byteReceived(byte data) {
         switch (state) {
             case STATE_START:
                 if (data == START_BYTE) {
                     state = State.STATE_SIZE;
                 }
                 break;
             case STATE_SIZE:
                 size = data;
                 content = new byte[size];
                 state = State.STATE_OPCODE;
                 break;
             case STATE_OPCODE:
                 opcode = data;
                 state = State.STATE_FLAG;
                 break;
             case STATE_FLAG:
                 flag = data;
                 state = State.STATE_CONTENT;
                 break;
             case STATE_CONTENT:
                 content[contentCounter++] = data;
                 if (contentCounter >= size - 3) {
                     state = State.STATE_DONE;
                     return true;
                 }
                 break;
             case STATE_DONE:
                 throw new IndexOutOfBoundsException("Command already finished");
             default:
                 break;
         }
 
         return false;
     }
 
     public byte getOpcode() {
         return opcode;
     }
 
     public byte[] getContent() {
         return content;
     }
 
     public boolean isAckFor(byte command) {
         return opcode == Protocol.OPCODE_RESPONSE
                 && flag == command;
     }
 }
