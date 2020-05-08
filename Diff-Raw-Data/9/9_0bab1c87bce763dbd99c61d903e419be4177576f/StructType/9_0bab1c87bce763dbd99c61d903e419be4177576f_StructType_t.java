 import java.util.Vector;
 
 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class StructType extends CompositeType
 {
     private Vector<STO> m_fieldList;
     private String baseName; // For strict name equivalence
     
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public StructType(String strName, int size, Vector<STO> fieldList)
     {
         super(strName, size);
         setFields(fieldList);
         setBaseName(strName);
     }
 
     public StructType(String strName)
     {
         super(strName, 4);
         setBaseName(strName);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public boolean isStruct()
     {
         return true;
     }
 
     Vector<STO> getFields()
     {
         return m_fieldList;
     }
 
     public void setFields(Vector<STO> fields)
     {
         m_fieldList = fields;
     }
     
     private void setBaseName(String name)
     {
     	baseName = name;
     }
     
     private String getBaseName()
     {
     	return baseName;
     }
     
     public boolean isEquivalent(Type type)
     {
     	if(type.isStruct()) {
    		return (this.getBaseName() == ((StructType)type).getBaseName());
     	}
     	else {
    		return false;
     	}
     }
 }
