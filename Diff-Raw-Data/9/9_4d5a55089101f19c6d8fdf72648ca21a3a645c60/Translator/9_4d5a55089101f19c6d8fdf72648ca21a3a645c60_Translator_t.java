 package com.hifiremote.jp1;
 
 public class Translator
   extends Translate
 {
   public Translator( String[] textParms )
   {
     super( textParms );
     int parmIndex = 0;
     for ( int i = 0; i < textParms.length; i ++ )
     {
       String text = textParms[ i ];
       if ( text.equalsIgnoreCase( "lsb" ))
         lsb = true;
       else if ( text.equalsIgnoreCase( "comp" ))
         comp = true;
       else
       {
         int val = Integer.parseInt( text );
         switch ( parmIndex )
         {
           case indexIndex:
             index = val;
             break;
           case bitsIndex:
             bits = val;
             break;
           case bitOffsetIndex:
             bitOffset = val;
             break;
           case lsbOffsetIndex:
             lsbOffset = val;
             break;
           case adjustOffset:
           {
             adjust = val;
             break;
           }
           default:
             break;
         }
         parmIndex++;
       }
     }
   }
 
   public Translator( boolean lsb, boolean comp, int index, int bits, int bitOffset )
   {
     super( null );
     this.lsb = lsb;
     this.comp = comp;
     this.index = index;
     this.bits = bits;
     this.bitOffset = bitOffset;
   }
 
   public void in( Value[] parms, Hex hexData, DeviceParameter[] devParms, int onlyIndex )
   {
     int[] hex = hexData.getData();
     if (onlyIndex >= 0 && onlyIndex != index)
       return;
     if ( index >= parms.length )
     {
       System.err.println("Translator.in() index="+ index +" exceeds "+ parms.length +" item buffer");
       return;
     }
     int w=0;
     Value v = parms[index];
     if ( v == null )
     {
       System.err.println("Translator.in() index="+ index +" missing parameter");
     }
     else
     {
       Integer i = ( Integer )v.getValue();
       if ( i == null )
         System.err.println("Translator.in() index="+ index +" missing parameter value");
       else
        w = ( i.intValue() + adjust ) >> lsbOffset;
     }
 
     if ( comp )
     {
       w = 0xFFFFFFFF - w;
     }
     if ( lsb )
     {
       w = reverse(w, bits );
     }
 
     insert( hexData, bitOffset, bits, w );
   }
 
   public void out( Hex hexData, Value[] parms, DeviceParameter[] devParms )
   {
     int[] hex = hexData.getData();
     if ( index >= parms.length )
     {
       System.err.println("Translator.out() index="+ index +" exceeds "+ parms.length +" item buffer");
       return;
     }
     int w = extract( hexData, bitOffset, bits);
     if ( comp )
     {
       w = (2<<(bits-1)) - 1 - w;
     }
     if ( lsb )
     {
       w = reverse(w, bits );
     }
     parms[ index ] = insert( parms[ index ], lsbOffset, bits, w - adjust );
   }
 
   public String toString()
   {
     StringBuffer buff = new StringBuffer();
     buff.append( "Translator(" );
     if ( lsb )
       buff.append( "lsb," );
     if ( comp )
       buff.append( "comp," );
     buff.append( index );
     if ( bits != 8 )
     {
       buff.append( ',' );
       buff.append( bits );
     }
     if ( bitOffset != 0 )
     {
       buff.append( ',' );
       buff.append( bitOffset );
     }
     if ( lsbOffset != 0 )
     {
       buff.append( ',' );
       buff.append( lsbOffset );
     }
     buff.append( ')' );
     return buff.toString();
   }
 
   public boolean getLSB(){ return lsb; }
   public boolean getComp(){ return comp; }
   public int getIndex(){ return index; }
   public int getBits(){ return bits; }
   public int getBitOffset(){ return bitOffset; }
   protected boolean lsb = false;
   protected boolean comp = false;
   protected int index = 0;
   protected int bits = 8;
   protected int bitOffset = 0;
   protected int lsbOffset = 0;
   protected int adjust = 0;
 
   private final static int indexIndex = 0;
   private final static int bitsIndex = 1;
   private final static int bitOffsetIndex = 2;
   private final static int lsbOffsetIndex = 3;
   private final static int adjustOffset = 4;
 }
 
 
