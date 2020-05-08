 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class ArrayType extends CompositeType
 {
     Type elementType;
     Integer dimensionSize;
     ArrEleSTO elementList;
 
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public ArrayType(Type elementType, Integer dimenionSize)
     {
        super(elementType.getName()+"["+dimenionSize+"]", dimenionSize);
         setElementType(elementType);
         setDimensionSize(dimenionSize);
 
     }
 
     public ArrayType(Type elementType, Integer dimenionSize, ArrEleSTO eleList)
     {
        super(elementType.getName()+"["+dimenionSize+"]", dimenionSize);
         setElementType(elementType);
         setDimensionSize(dimenionSize);
         setElementList(eleList);
 
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isArray()
     {
         return true;
     }
 
     public Type getElementType()
     {
         return elementType;
     }
 
     public void setElementType(Type type)
     {
         elementType = type;
     }
 
     public Integer getDimensionSize()
     {
         return dimensionSize;
     }
 
     public void setDimensionSize(Integer dimenionSize)
     {
         dimensionSize = dimenionSize;
     }
 
     public ArrEleSTO getElementList()
     {
         return elementList;
     }
 
     public void setElementList(ArrEleSTO eleList)
     {
         elementList = eleList;
     }
 
     // meaning: param = this
     public boolean isAssignable(Type type)
     {
         // If trying to assign to pointer, check if pointer type is this array's element type
         if(type.isPtrGrp()) {
             if(elementType.isAssignable(((PtrGrpType) type).getPointsToType()))
                 return true;
             else
                 return false;
         }
 
         if(isEquivalent(type)) {
             return true;
         }
 
         return false;
     }
 
 }
