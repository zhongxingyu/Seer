 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class PointerType extends PtrGrpType
 {
     //---------------------------------------------------------------------
     //      Constants
     //---------------------------------------------------------------------
     private static final String POINTER_NAME = "pointer";
     private static final int POINTER_SIZE    = 4;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public PointerType()
     {
         super("null*", POINTER_SIZE, null);
     }
 
     public PointerType(Type pointsTo)
     {
         super(POINTER_NAME, POINTER_SIZE, pointsTo);
 
         String pointerName;
 
         if(pointsTo == null)
             pointerName = "null*";
         else
             pointerName = pointsTo.getName() + "*";
         setName(pointerName);
     }
 
     public PointerType(String name, int size, Type pointsTo)
     {
        super(POINTER_NAME, POINTER_SIZE, pointsTo);
 
         String pointerName;
 
         if(pointsTo == null)
             pointerName = "null*";
         else
             pointerName = pointsTo.getName() + "*";
         setName(pointerName);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isPointer()
     {
         return true;
     }
 
 }
