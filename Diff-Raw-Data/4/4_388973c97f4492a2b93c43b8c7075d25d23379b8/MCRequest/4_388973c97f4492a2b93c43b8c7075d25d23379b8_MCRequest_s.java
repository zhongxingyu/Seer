 package com.anusiewicz.MCForAndroid.model;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Szymon Anusiewicz
  * Date: 29.06.13
  * Time: 22:14
  */
 public class MCRequest {
 
     private MCCommand command;
     private MCDeviceCode deviceType;
     private Integer deviceNumber;
     private Integer wordValue;
     private Boolean bitValue;
     //private  int numberOfDevicePoints;
 
     public MCRequest(MCCommand command) {
         this(command, null, null);
     }
 
     public MCRequest(MCCommand command, MCDeviceCode deviceType, Integer deviceNumber) throws IndexOutOfBoundsException {
         this(command, deviceType, deviceNumber,null,null);
     }
 
     public MCRequest(MCCommand command, MCDeviceCode deviceType, Integer deviceNumber, Integer word, Boolean bit) throws IndexOutOfBoundsException {
         this.command = command;
 
         if (command.equals(MCCommand.PLC_STOP) || command.equals(MCCommand.PLC_RUN)) {
             this.deviceType = null;
             this.deviceNumber = null;
             this.wordValue = null;
             this.bitValue = null;
 
         }  else {
 
             this.deviceType = deviceType;
 
             if (deviceNumber == null) {
                 this.deviceNumber = null;
             } else
             if (deviceNumber >=0 && deviceNumber <=deviceType.getDeviceRange()) {
                 this.deviceNumber = deviceNumber;
             }    else {
                 throw new IndexOutOfBoundsException("Choose " + deviceType + " devices from 0 to " + deviceType.getDeviceRange());
             }
 
             if (command.equals(MCCommand.READ_BIT) || command.equals(MCCommand.READ_WORD)) {
                 this.wordValue = null;
                 this.bitValue = null;
 
             }   else if (command.equals(MCCommand.WRITE_WORD)) {
                 this.wordValue = word;
                 this.bitValue = null;
             }   else if (command.equals(MCCommand.WRITE_BIT)) {
                 this.wordValue = null;
                 this.bitValue = bit;
             }
         }
     }
 
     public static String generateStringFromRequest(MCRequest request) {
 
         StringBuilder builder = new StringBuilder(30);
         builder.append(request.getCommand().getCommandCode())
                 .append("FF0000");
         if (request.getDeviceType() != null && request.getDeviceNumber() !=null) {
                 builder.append(request.getDeviceType().getDeviceCode());
 
                 String devNum = Integer.toHexString(request.getDeviceNumber());
 
                 for ( int i = 1; i<= 8-devNum.length(); i++ ) {
                     builder.append("0");
                 }
                 builder.append(devNum)
                         .append("0100");
         }
 
         if (request.getWordValue() != null) {
 
             String word = Integer.toHexString(request.getWordValue());
 
             for ( int i = 1; i<= 4-word.length(); i++ ) {
                 builder.append("0");
             }
             builder.append(word);
         }
 
         if (request.getBitValue() != null) {
            builder.append("0");
             if (request.getBitValue().equals(Boolean.TRUE)) {
                builder.append("01");
             }   else {
                 builder.append("00");
             }
         }
 
 
          return builder.toString();
     }
 
     public MCCommand getCommand() {
         return command;
     }
 
     public MCDeviceCode getDeviceType() {
         return deviceType;
     }
 
     public Integer getDeviceNumber() {
         return deviceNumber;
     }
     public Integer getWordValue() {
         return wordValue;
     }
 
     public Boolean getBitValue() {
         return bitValue;
     }
 
     public enum MCCommand {
         READ_BIT("00"),
         READ_WORD("01"),
         WRITE_BIT("02"),
         WRITE_WORD("03"),
         PLC_RUN("13"),
         PLC_STOP("14");
         private String commandCode;
         private MCCommand(String code) {
             commandCode = code;
         }
 
         public String getCommandCode() {
             return commandCode;
         }
     }
 
     public enum MCDeviceCode {
         D("4420", 7999),
         R("5220", 32767),
         TN("544E", 511),
         TS("5453", 511),
         CN("434E", 199),
         CS("4353", 199),
         X("5820", 377),
         Y("5920", 377),
         M("4D20", 7679),
         S("5320", 4095);
 
         private String deviceCode;
 
         private int deviceRange;
         private MCDeviceCode(String code, int range) {
             deviceCode = code;
             deviceRange = range;
         }
 
         public String getDeviceCode() {
             return deviceCode;
         }
 
         private int getDeviceRange() {
             return deviceRange;
         }
     }
 
 
 
 }
