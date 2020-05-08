 //---------------------------------------------------------------------
 // 
 //---------------------------------------------------------------------
public static final String INT_NAME = "int";
public static final int INT_SIZE    = 4;
 
 class IntType extends NumericType
 {
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public 
     IntType ()
     {
         IntType(INT_NAME, INT_SIZE);
     }
 
     public 
     IntType (String strName)
     {
         IntType(strName, INT_SIZE);
     }
 
     public
     IntType (String strName, int size)
     {
         super(strName, size);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public boolean isBasic()   { return true; }
     public boolean isNumeric() { return true; }
     public boolean isInt()     { return true; }
 
     //----------------------------------------------------------------
     //    
     //----------------------------------------------------------------
 }
