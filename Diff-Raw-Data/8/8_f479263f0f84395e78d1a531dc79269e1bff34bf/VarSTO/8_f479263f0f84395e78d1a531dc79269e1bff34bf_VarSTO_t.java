 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class VarSTO extends STO
 {
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     VarSTO(String strName)
     {
         super(strName);
         // You may want to change the isModifiable and isAddressable 
         // fields as necessary
        setIsAddressable(true);
        setIsModifiable(true);
     }
 
     public 
    VarSTO(String strName, Type type)
     {
         super(strName, type);
         // You may want to change the isModifiable and isAddressable 
         // fields as necessary
        setIsAddressable(true);
        setIsModifiable(true);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean   
     isVar() 
     {
         return true;
     }
 }
