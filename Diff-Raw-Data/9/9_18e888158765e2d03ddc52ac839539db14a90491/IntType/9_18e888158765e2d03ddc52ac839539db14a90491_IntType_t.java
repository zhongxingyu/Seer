 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class IntType extends NumericType
 {
     //---------------------------------------------------------------------
     //      Constants
     //---------------------------------------------------------------------
     private static final String INT_NAME = "int";
     private static final int INT_SIZE    = 4;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public IntType()
     {
         this(INT_NAME, INT_SIZE);
     }
 
     public IntType(String strName)
     {
         this(strName, INT_SIZE);
     }
 
     public IntType(String strName, int size)
     {
         super(strName, size);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isInt()
     {
         return true;
     }
 
     public boolean isAssignable(Type type)
     {
         if(isEquivalent(type)) {
             return true;
         }
/*
         if(type.getName().equals("float")) {
             return true;
         }
*/
        if(type.getClass() == FloatType.class) {
        	return true;
        }
        
         return false;
     }
 }
