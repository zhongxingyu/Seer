 package com.hifiremote.jp1;
 
 import java.util.Properties;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class KeyMoveEFC.
  */
 public class KeyMoveEFC extends KeyMove
 {
 
   /**
    * Instantiates a new key move efc.
    * 
    * @param keyCode
    *          the key code
    * @param deviceButtonIndex
    *          the device button index
    * @param data
    *          the data
    * @param notes
    *          the notes
    */
   public KeyMoveEFC( int keyCode, int deviceButtonIndex, Hex data, String notes )
   {
     super( keyCode, deviceButtonIndex, data, notes );
   }
 
   /**
    * Instantiates a new key move efc.
    * 
    * @param keyCode
    *          the key code
    * @param deviceButtonIndex
    *          the device button index
    * @param deviceType
    *          the device type
    * @param setupCode
    *          the setup code
    * @param efc
    *          the efc
    * @param notes
    *          the notes
    */
   public KeyMoveEFC( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, int efc, String notes )
   {
     super( keyCode, deviceButtonIndex, deviceType, setupCode, new Hex( 2 ), notes );
     setEFC( new EFC( ( short )efc ) );
   }
 
   /**
    * Instantiates a new key move efc.
    * 
    * @param props
    *          the props
    */
   public KeyMoveEFC( Properties props )
   {
     super( props );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.KeyMove#clone()
    */
   public Object clone()
   {
     return new KeyMoveEFC( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), getEFC().getValue(),
         getNotes() );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.KeyMove#getEFC()
    */
   public EFC getEFC()
   {
//    return new EFC( ( short )data.get( 0 ) );
    return new EFC( ( short )data.get( CMD_INDEX ) );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.KeyMove#setEFC(com.hifiremote.jp1.EFC)
    */
   public void setEFC( EFC value )
   {
//    data.put( value.getValue(), 0 );
    data.put( value.getValue(), CMD_INDEX );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.KeyMove#getCmd()
    */
   public Hex getCmd()
   {
//    return EFC.toHex( data.get( 0 ) );
    return EFC.toHex( data.get( CMD_INDEX ) );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.KeyMove#setCmd(com.hifiremote.jp1.Hex)
    */
   public void setCmd( Hex hex )
   {
//    data.put( EFC.parseHex( hex ), 0 );
    data.put( EFC.parseHex( hex ), CMD_INDEX );
   }
 }
