 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 
 class PtrGrpType extends CompositeType
 {
     //---------------------------------------------------------------------
     //      Instance Variables
     //---------------------------------------------------------------------
     private Type m_pointsToType;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public PtrGrpType(String strName, int size, Type pointsTo)
     {
         super(strName, size);
         m_pointsToType = pointsTo; 
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
 
 
     public Type getPointsToType()
     {
         return m_pointsToType;
     }
     public void setPointsToType(Type pointsTo)
     {
         m_pointsToType = pointsTo;
     }
 
     public boolean isPtrGrp()
     {
         return true;
     }
 
     public Type getBottomPtrType()
     { 
     	if(m_pointsToType == null) 
             return null;
     	
         if(m_pointsToType.isPtrGrp())
             return ((PtrGrpType) m_pointsToType).getBottomPtrType();
         else
             return m_pointsToType;
     }
 
     public void setBottomPtrType(Type newType)
     { 
     	if(m_pointsToType == null)
             setPointsToType(newType);
     	
         if(m_pointsToType.isPtrGrp())
             ((PtrGrpType) m_pointsToType).setBottomPtrType(newType);
         else
             setPointsToType(newType);
     }
 
     public String setInitialName()
     {
         if(m_pointsToType == null)
             setName("nullptr");
         else if(m_pointsToType.isArray())
             setName(m_pointsToType.getName()); // TODO: TEMPORARY TO MAKE COMPILER
        else
             setName(((PtrGrpType) m_pointsToType).setInitialName() + "*"); 
 
         return getName();
 
     }
 }
