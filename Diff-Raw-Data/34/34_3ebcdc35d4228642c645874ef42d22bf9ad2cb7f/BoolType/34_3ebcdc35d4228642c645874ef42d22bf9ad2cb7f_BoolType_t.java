 //---------------------------------------------------------------------
 // 
 //---------------------------------------------------------------------
 
 class BoolType extends BasicType
 {
    // constants
    public static final String BOOL_NAME = "bool";
    public static final int BOOL_SIZE    = 1;

     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public 
     BoolType ()
     {
         BoolType(BOOL_NAME, BOOL_SIZE);
     }
 
     public 
     BoolType (String strName)
     {
         BoolType(strName, BOOL_SIZE);
     }
 
     public 
     BoolType (String strName, int size)
     {
         super(strName, size);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public boolean isBasic() { return true; }
 
     //----------------------------------------------------------------
     //    
     //----------------------------------------------------------------
 }
