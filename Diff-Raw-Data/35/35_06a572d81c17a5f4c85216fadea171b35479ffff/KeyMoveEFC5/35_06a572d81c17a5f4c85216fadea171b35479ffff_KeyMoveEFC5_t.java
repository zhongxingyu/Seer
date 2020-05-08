 package com.hifiremote.jp1;
 
 import java.util.Properties;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class KeyMoveEFC5.
  */
 public class KeyMoveEFC5
   extends KeyMove
 {
   
   /**
    * Instantiates a new key move ef c5.
    * 
    * @param keyCode the key code
    * @param deviceButtonIndex the device button index
    * @param data the data
    * @param notes the notes
    */
   public KeyMoveEFC5( int keyCode, int deviceButtonIndex, Hex data, String notes )
   {
     super( keyCode, deviceButtonIndex, data, notes );
   }
 
   /**
    * Instantiates a new key move ef c5.
    * 
    * @param keyCode the key code
    * @param deviceButtonIndex the device button index
    * @param deviceType the device type
    * @param setupCode the setup code
    * @param efc the efc
    * @param notes the notes
    */
   public KeyMoveEFC5( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, int efc, String notes )
   {
     super( keyCode, deviceButtonIndex, deviceType, setupCode, new Hex( 2 ), notes );
     setEFC( efc );
   }
   
   /**
    * Instantiates a new key move ef c5.
    * 
    * @param props the props
    */
   public KeyMoveEFC5( Properties props )
   {
     super( props );
   }
 
   /* (non-Javadoc)
    * @see com.hifiremote.jp1.KeyMove#clone()
    */
   public Object clone()
   {
     return new KeyMoveEFC5( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), getEFC().getValue(), getNotes());
   }
   
   /* (non-Javadoc)
    * @see com.hifiremote.jp1.KeyMove#getEFC()
    */
   public EFC getEFC()
   {
//    return new EFC5( data.get( 0 ));
    return new EFC5( data.get( CMD_INDEX ));
   }
 
   /* (non-Javadoc)
    * @see com.hifiremote.jp1.KeyMove#setEFC(com.hifiremote.jp1.EFC)
    */
   public void setEFC( EFC efc )
   {
     setEFC((( EFC5 )efc ).getValue());
   }
   
   /**
    * Sets the eFC.
    * 
    * @param efc the new eFC
    */
   public void setEFC( int efc )
   {
//    data.put( efc, 0 );
    data.put( efc, CMD_INDEX );
   }
 
   /* (non-Javadoc)
    * @see com.hifiremote.jp1.KeyMove#getCmd()
    */
   public Hex getCmd()
   {
//    return EFC5.toHex( data.get( 0 ));
    return EFC5.toHex( data.get( CMD_INDEX ));
   }
   
   /* (non-Javadoc)
    * @see com.hifiremote.jp1.KeyMove#setCmd(com.hifiremote.jp1.Hex)
    */
   public void setCmd( Hex hex )
   {
//    data.put( EFC5.parseHex( hex ), 0 );
    data.put( EFC5.parseHex( hex ), CMD_INDEX );
   }
 }
