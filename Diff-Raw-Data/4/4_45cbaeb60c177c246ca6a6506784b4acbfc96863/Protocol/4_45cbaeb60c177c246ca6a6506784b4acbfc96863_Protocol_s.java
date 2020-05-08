 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brutes.net;
 
 /**
  *
  * @author Karl
  */
 public abstract class Protocol {
     //Network datas
     public static final int CONNECTION_PORT = 42666;
     public static final int SIZE_DISCRININANT = 1;
     public static final int SIZE_BOOL = 1;
     public static final int SIZE_CHAR = 1;
     public static final int SIZE_SHORTINT = 2;
     public static final int SIZE_LONGINT = 4;
     public static final int SIZE_FLOAT = 4;
     
     //Codes des types primitifs
     public static final byte TYPE_BOOL = (byte)0x01;
     public static final byte TYPE_CHAR = (byte)0x02;
     public static final byte TYPE_SHORT = (byte)0x03;
     public static final byte TYPE_LONG = (byte)0x04;
     public static final byte TYPE_FLOAT = (byte)0x05;
     public static final byte TYPE_STRING = (byte)0x06;
     public static final byte Type_IMG = (byte)0x07;
     
     //Discriminants clients > serveur
     public static final byte D_LOGIN = (byte)0x80;
     public static final byte D_LOGOUT = (byte)0x81;
     
     public static final byte D_CREATE_CHARACTER = (byte)0x90;
     public static final byte D_UPDATE_CHARACTER = (byte)0x91;
     public static final byte D_DELETE_CHARACTER = (byte)0x92;
     
    public static final byte D_GET_MY_CHARACTER = (byte)0xA0;
    public static final byte D_GET_CHALLENGER = (byte)0xA1;
     
     public static final byte D_CHEAT_FIGHT_WIN = (byte)0xB0;
     public static final byte D_CHEAT_FIGHT_LOOSE = (byte)0xB1;
     public static final byte D_CHEAT_FIGHT_RANDOM = (byte)0xB2;
     public static final byte D_DO_FIGHT = (byte)0xB3;
     
     public static final byte D_GET_CHARACTER = (byte)0xC0;
     public static final byte D_GET_IMAGE = (byte)0xC2;
     public static final byte D_GET_BONUS = (byte)0xC3;
     
     //Discriminants serveur > client
     public static final byte R_LOGIN_SUCCESS = (byte)0x01;
     public static final byte R_LOGOUT_SUCCESS = (byte)0x02;
     
     public static final byte R_ACTION_SUCCESS = (byte)0x03;
     
     public static final byte R_CHARACTER = (byte)0x10;
     public static final byte R_FIGHT_RESULT = (byte)0x11;
     
     public static final byte R_DATA_CHARACTER = (byte)0x20;
     public static final byte R_DATA_IMAGE = (byte)0x21;
     public static final byte R_DATA_BONUS = (byte)0x22;
     
     public static final byte ERROR = (byte)0x40;
     public static final byte ERROR_LOGIN_NOT_FOUND = (byte)0x41;
     public static final byte ERROR_WRONG_PASSWORD = (byte)0x42;
     public static final byte ERROR_CREATE_CHARACTER = (byte)0x43;
     public static final byte ERROR_UPDATE_CHARACTER = (byte)0x44;
     public static final byte ERROR_DELETE_CHARACTER = (byte)0x45;
     public static final byte ERROR_CHARACTER_NOT_FOUND = (byte)0x46;
     public static final byte ERROR_IMAGE_NOT_FOUND = (byte)0x47;
     public static final byte ERROR_BONUS_NOT_FOUND = (byte)0x48;
     public static final byte ERROR_FIGHT = (byte)0x49;
     public static final byte ERROR_TOKEN = (byte)0x4A;
     public static final byte ERROR_SRLY_WTF = (byte)0x4B;
 }
