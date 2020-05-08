 //---------------------------------------------------------------------
 // 
 //---------------------------------------------------------------------
public static final String FLOAT_NAME = "float";
public static final int FLOAT_SIZE    = 4;

 
 class FloatType extends NumericType
 {
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public 
     FloatType ()
     {
         FloatType(FLOAT_NAME, FLOAT_SIZE);
     }
 
     public 
     FloatType (String strName)
     {
         FloatType(strName, FLOAT_SIZE);
     }
 
     public 
     FloatType (String strName, int size)
     {
         super(strName, size);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public boolean isBasic()   { return true; }
     public boolean isNumeric() { return true; }
     public boolean isFloat() { return true; }
 
     //----------------------------------------------------------------
     //    
     //----------------------------------------------------------------
 }
