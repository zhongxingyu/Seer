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
     }
 
     public 
    VarSTO(String strName, Type typ)
     {
         super(strName, type);
         // You may want to change the isModifiable and isAddressable 
         // fields as necessary
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
