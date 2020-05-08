 package com.physion.ovation.gui.ebuilder.datamodel;
 
 import java.util.ArrayList;
 
 import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
 import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
 import com.physion.ovation.gui.ebuilder.datatypes.Type;
 import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;
 
 
 /**
  * 
  */
 public class RowData {
 
     /**
      * This is the "root" row for the whole tree.
      *
      * TODO: I'm not sure if I want to keep this value here.
      */
     private static RowData rootRow;
 
     /**
      * This is the "topmost", or "root" class that is the ancestor
      * of ALL other rows.
      *
      * Please note this is a static member data that applies to ALL
      * the RowData objects that exist.
      */
     private static ClassDescription classUnderQualification;
 
     /**
      * This is the class from whose attributes the user
      * will select to create a "path" to this row's childmost attribute.
      * You might also think of this as the "Class Under Qualification"
      * as far as this row is concerned.
      *
      * For example, this might be Epoch or User or Source.
      *
      * TODO: Perhaps come up with a better name?  cuq, entity?
      */
     //private ClassDescription parentClass;
 
     /**
      * This is the class from whose attributes the user
      * will select to create a "path" to this row's childmost attribute.
      * You might also think of this as the "Class Under Qualification"
      * as far as this row is concerned.
      *
      * For example, this might be Epoch or User or Source.
      *
      * If our parentRow member data is null, then this RowData instance
      * is the "root" row for the whole tree.  I.e. it is the
      * "Class Under Qualification".
      *
      * All rows except the root row have a non-null parentRow.
      * The class of the root row is stored in our static member
      * data classUnderQualification.
      */
     private RowData parentRow;
 
     /**
      * This is the path to the childmost attribute that
      * this row is specifying.
      *
      * For example, parentClass might be Epoch, and then
      * attributePath could be a list containing the Attributes:
      *
      *      epochGroup  (is of type EpochGroup)
      *      source      (is of type Source)
      *      label       (is of type string)
      *
      * So the above would be specifying the label of the source of
      * the epochGroup of the parentClass.
      *
      * TODO:  Do we want a list of Attribute objects or just Strings.
      */
     private ArrayList<Attribute> attributePath = new ArrayList<Attribute>();
 
     /**
      * The operator the user selected for this attribute.
      * For example, ==, !=, >=, <=, <, >.
      * Note that "is null" and "is not null" are also considered operators.
      *
      * Please note, this might be null if this row is a "compound" row
      * that ends with Any, All, or None.
      *
      * We could create enum types to hold these operators, but I think that
      * is a very heavy solution for not much gain.  The user won't be
      * able to enter the values because GUI widgets will be used to
      * select the values, so there isn't any user risk of invalid values
      * being used.  But, there is programmer risk of that.
      * We might change this.
      */
     private String attributeOperator;
 
     /**
      * If the attributeOperator is set to something AND it is not
      * set to "is null" or "is not null", then this value is the
      * value that the user entered as the desired value for the
      * attribute.
      *
      * For example, if the attributePath is:  epochGroup.source.label
      * then attributeValue might be something like "Test 27".
      */
     private Object attributeValue;
 
     /**
      * If this row is a "compound" row, this will be set to
      * Any, All, or None.
      *
      * If this is set to Count, then the
      * attributeOperator member data will be set to some sort of
      * operator such as ==, >, or "is null".
      *
      * If this is null, then this row is an "attribute" row as
      * opposed to a "compound" row.
      */
     private CollectionOperator collectionOperator;
 
     private ArrayList<RowData> childRows = new ArrayList<RowData>();
 
 
     /**
      * Get the number of descendents from this node.  I.e. this is returns
      * the count of our direct children, plus all their children, and all
      * our childrens' children, and so on.
      */
     public int getNumDescendents() {
 
         int count = childRows.size();
         for (RowData childRow : childRows) {
             count += childRow.getNumDescendents();
         }
         return(count);
     }
 
 
     /**
      * This returns the RowData object that is at the specified "index".
      * This method is intended to be used to get the RowData object at
      * the specified index as far as a List GUI widget is concerned.
      * This RowData object is at index 0.  Its first child is at index 1.
      * If the first child has a child, then that child is at index 2.
      * If the first child does not have a child, then the second child
      * is at index 2.  (I.e. the first child's sibling.)
      *
      * A simple "picture" of RowData objects and their "indexes" as
      * far as a List widget is concerned, makes this more obvious:
      *
      *      RowData 0
      *          RowData 1
      *          RowData 2
      *          RowData 3
      *               RowData 4
      *          RowData 5
      *               RowData 6
      *               RowData 7
      *               RowData 8
      *          RowData 9
      */
     //public RowData getDescendentAt(int index) {
     public RowData getChild(int index) {
 
         if (index == 0)
             return(this);
 
 
         for (RowData childRow : childRows) {
 
             index--;
             //RowData rd = childRow.getDescendentAt(index);
             RowData rd = childRow.getChild(index);
             if (rd != null)
                 return(rd);
 
             index -= childRow.getNumDescendents();
 
             if (index == 0)
                 return(childRow);
         }
 
         return(null);
     }
 
 
     /**
      * Remove the specified child RowData object from this RowData object's
      * list of direct children.
      */
     public void removeChild(RowData rowData) {
         childRows.remove(rowData);
     }
 
 
     /**
      * Remove this RowData object from its parent's list of direct children.
      */
     public void removeFromParent() {
 
         System.out.println("Removing rowData: "+this.getRowString());
         System.out.println("from parent: "+getParentRow().getRowString());
 
         getParentRow().getChildRows().remove(this);
     }
 
 
     /**
      * Create a child row for this row that is of type Compound Row.
      */
     public void createCompoundRow() {
 
         System.out.println("createCompoundRow rowData: "+this.getRowString());
 
         Attribute attribute = getChildmostAttribute();
         ArrayList<Attribute> attributePath = new ArrayList<Attribute>();
         attributePath.add(attribute);
         RowData compoundRow = new RowData();
         compoundRow.setAttributePath(attributePath);
         compoundRow.setCollectionOperator(CollectionOperator.ANY);
 
         addChildRow(compoundRow);
     }
 
 
     /**
      * Create a child row for this row that is of type Attribute Row.
      */
     public void createAttributeRow() {
 
         System.out.println("createAttributeRow rowData: "+this.getRowString());
 
         Attribute attribute = getChildmostAttribute();
         ClassDescription classDescription;
         if (this == getRootRow())
             classDescription = getClassUnderQualification();
         else
             classDescription = attribute.getClassDescription();
         if (classDescription == null) {
             System.out.println("ERROR: In createAttributeRow "+
                 "classDescription == null.  This should never happen.");
             return;
         }
 
         ArrayList<Attribute> attributes = classDescription.getAllAttributes();
         if (attributes.isEmpty()) {
             System.out.println("ERROR: In createAttributeRow "+
                 "attributes.isEmpty == true.  This should never happen.");
             return;
         }
 
         /**
          * For initial development, just get the first attribute.
          */
         attribute = attributes.get(0);
         //attribute = attributes.get(1);
         //attribute = attributes.get(2);
 
         ArrayList<Attribute> attributePath = new ArrayList<Attribute>();
         attributePath.add(attribute);
 
         RowData attributeRow = new RowData();
         attributeRow.setAttributePath(attributePath);
 
         addChildRow(attributeRow);
     }
 
 
     /**
      * Returns true if this row ends with the Any, All, or None
      * "collection" operator.
      */
     public boolean isCompoundRow() {
 
         /*
         if (childRows.isEmpty())
             System.err.println(
                 "WARNING: Compound row with no child rows defined.");
         */
 
         if (collectionOperator == null)
             return(false);
 
         return(collectionOperator.isCompoundOperator());
     }
 
 
     /**
      * TODO:  Decide whether I should make this a static method
      * that operates on the "rootRow" member data.
      */
     public /*static*/ void setClassUnderQualification(
         ClassDescription classUnderQualification) {
 
         RowData.classUnderQualification = classUnderQualification;
 
         if (parentRow != null) {
             System.out.println(
                 "WARNING:  parentRow != null.  Are you confused?");
             parentRow = null;
         }
 
         if (!childRows.isEmpty()) {
             System.out.println("INFO:  Clearing all childRows.");
             childRows.clear();
         }
     }
 
 
     public static ClassDescription getClassUnderQualification() {
         return(classUnderQualification);
     }
 
 
     public static RowData getRootRow() {
         return(rootRow);
     }
 
 
     public static void setRootRow(RowData rowData) {
         rootRow = rowData;
     }
 
 
     /*
     public void setParentClass(ClassDescription parentClass) {
         this.parentClass = parentClass;
     }
     */
     public void setParentRow(RowData parentRow) {
         this.parentRow = parentRow;
     }
 
     private RowData getParentRow() {
         return(parentRow);
     }
 
 
     private ArrayList<RowData> getChildRows() {
         return(childRows);
     }
 
     public void setCollectionOperator(CollectionOperator collectionOperator) {
         this.collectionOperator = collectionOperator;
     }
 
 
     public CollectionOperator getCollectionOperator() {
         return(collectionOperator);
     }
 
 
     public void setAttributeOperator(String attributeOperator) {
         this.attributeOperator = attributeOperator;
     }
 
 
     public void setAttributePath(ArrayList<Attribute> attributePath) {
         this.attributePath = attributePath;
     }
 
 
     public ArrayList<Attribute> getAttributePath() {
         return(attributePath);
     }
 
 
     public void setAttributeValue(Object attributeValue) {
         this.attributeValue = attributeValue;
     }
 
 
     /**
      * TODO: Do we want to set the parentRow of every child to
      * be this RowData instance?  If so, then we probably want
      * to have an addChildRow() method.
      */
     public void setChildRows(ArrayList<RowData> childRows) {
 
         this.childRows = childRows;
         for (RowData childRow : childRows)
             childRow.setParentRow(this);
     }
 
 
     public void addChildRow(RowData childRow) {
 
         childRow.setParentRow(this);
         childRows.add(childRow);
     }
 
 
     /**
      * Get the amount a RowString should be indented.
      * This method will probably be unused once I switch to using
      * widgets to render a cell.
      */
     public String getIndentString() {
 
         String indentString = "";
         for (RowData rowData = this.getParentRow(); rowData != null;
              rowData = rowData.getParentRow()) {
 
             indentString += "    ";
         }
         return(indentString);
     }
 
 
     public String toString() {
         return(toString(""));
     }
 
 
     public String getRowString() {
         return(getRowString(""));
     }
 
 
     /**
      * Get the String representation of just this row.  I.e. not this
      * row and its children.
      */
     public String getRowString(String indent) {
         
         //String string = indent+parentClass.getName()+" |";
 
         //ClassDescription parentClass = getParentClass();
         //String string = indent+getParentClass().getName()+" |";
         String string;
         if (getParentClass() != null) 
             string = indent+getParentClass().getName()+" |";
         else 
             string = indent+"ERROR: No Parent Class"+" |";
 
 
         /**
          * Do a quick sanity check.
          */
         if (collectionOperator != null &&
             collectionOperator.isCompoundOperator() &&
             attributeOperator != null) {
             string += "ERROR: RowData is in an inconsistent state.";
             string += "\ncollectionOperator = "+collectionOperator;
             string += "\nattributeOperator = "+attributeOperator;
             return(string);
         }
 
         //if (!attributePath.isEmpty()) {
         //}
         boolean first = true;
         //for (String attributeName : attributePath) {
         for (Attribute attribute : attributePath) {
 
             if (first)
                 string += " ";
             else
                 string += ".";
 
             //string += attributeName;
            string += attribute.getName();
 
             first = false;
         }
 
         if (collectionOperator != null)
             string += " "+collectionOperator;
 
         if (attributeOperator != null)
             string += " "+attributeOperator;
 
         /**
          * Another quick sanity check.
          * If the user specified an attributeOperator other than
          * the "is null" and "is not null" values, then s/he also
          * must have specified an attributeValue.
          */
         if ((attributeOperator != null) &&
             ((attributeOperator != "is null") &&
              (attributeOperator != "is not null")) &&
             (attributeValue == null)) {
             string += "ERROR: RowData is in an inconsistent state.";
             string += "\nattributeOperator = "+attributeOperator;
             string += "\nattributeValue = "+attributeValue;
             return(string);
         }
 
         if (attributeValue != null) {
             string += " \""+attributeValue+"\"";
         }
 
         return(string);
     }
 
 
     /**
      * Get the "childmost" or "leaf" Attribute that is specified
      * by this row.
      */
     public Attribute getChildmostAttribute() {
 
         if (getParentRow() == null) {
             /**
              * This is the root row, which does not have an attribute path.
              */
             //return(classUnderQualification);
             //System.out.println("getParentRow() == null");
             return(null);
         }
         else if (attributePath.isEmpty()) {
             System.out.println("attributePath.isEmpty()");
             return(null);
         }
         else
             return(attributePath.get(attributePath.size()-1));
     }
 
 
     public ClassDescription getParentClass() {
 
         if (parentRow == null) {
             //System.out.println("parentRow == null");
             return(classUnderQualification);
         }
         else {
             if (parentRow.getChildmostAttribute() == null) {
                 //System.out.println("parentRow.getChildmostAttribute == null");
                 return(classUnderQualification);
             }
             else {
                 //System.out.println("parentRow.getChildmostAttribute != null");
                 //System.out.println("parentRow.getChildmostAttribute().getClassDescription() = "+parentRow.getChildmostAttribute().getClassDescription());
                 //System.out.println("parentRow.getChildmostAttribute() = "+parentRow.getChildmostAttribute());
                 return(parentRow.getChildmostAttribute().getClassDescription());
             }
         }
     }
 
 
     public String toString(String indent) {
 
         String string = getRowString(indent);
 
         for (RowData childRow : childRows)
             string += "\n"+childRow.toString(indent+"  ");
 
         return(string);
     }
 
 
     /**
      * This method creates a RowData initialized with a few values
      * for testing purposes.
      */
     public static RowData createTestRowData() {
 
         /*
         ClassDescription entityBaseCD =
             new ClassDescription("EntityBase", null);
         ClassDescription taggableEntityBaseCD =
             new ClassDescription("TaggableEntityBase", entityBaseCD);
         ClassDescription userCD =
             new ClassDescription("User", taggableEntityBaseCD);
         ClassDescription keywordTagCD =
             new ClassDescription("KeywordTag", entityBaseCD);
         ClassDescription timelineElementCD =
             new ClassDescription("TimelineElement", taggableEntityBaseCD);
         ClassDescription epochCD =
             new ClassDescription("Epoch", timelineElementCD);
         ClassDescription epochGroupCD =
             new ClassDescription("EpochGroup", timelineElementCD);
         ClassDescription sourceCD =
             new ClassDescription("Source", taggableEntityBaseCD);
         */
         ClassDescription entityBaseCD =
             DataModel.getClassDescription("EntityBase");
         ClassDescription taggableEntityBaseCD =
             DataModel.getClassDescription("TaggableEntityBase");
         ClassDescription userCD =
             DataModel.getClassDescription("User");
         ClassDescription keywordTagCD =
             DataModel.getClassDescription("KeywordTag");
         ClassDescription timelineElementCD =
             DataModel.getClassDescription("TimelineElement");
         ClassDescription epochCD =
             DataModel.getClassDescription("Epoch");
         ClassDescription epochGroupCD =
             DataModel.getClassDescription("EpochGroup");
         ClassDescription sourceCD =
             DataModel.getClassDescription("Source");
 
         /**
          * Initialize values of the EntityBase class.
          */
 /*
         Attribute attribute = new Attribute("owner", Type.REFERENCE,
                                             userCD, Cardinality.TO_ONE);
         entityBaseCD.addAttribute(attribute);
 
         attribute = new Attribute("uuid", Type.UTF_8_STRING);
         entityBaseCD.addAttribute(attribute);
 
         attribute = new Attribute("incomplete", Type.BOOLEAN);
         entityBaseCD.addAttribute(attribute);
 */
         /**
          * Initialize values of the Epoch class.
          */
 /*
         attribute = new Attribute("protocolID", Type.UTF_8_STRING);
         epochCD.addAttribute(attribute);
 
         attribute = new Attribute("excludeFromAnalysis", Type.BOOLEAN);
         epochCD.addAttribute(attribute);
 
         attribute = new Attribute("nextEpoch", Type.REFERENCE,
                                   epochCD, Cardinality.TO_ONE);
         epochCD.addAttribute(attribute);
 */
         /**
          * Initialize values of the Source class.
          */
 /*
         attribute = new Attribute("label", Type.UTF_8_STRING);
         sourceCD.addAttribute(attribute);
 
         attribute = new Attribute("parent", Type.REFERENCE,
                                   sourceCD, Cardinality.TO_ONE);
         sourceCD.addAttribute(attribute);
 */
         /**
          * Initialize values of the EpochGroup class.
          */
 /*
         attribute = new Attribute("label", Type.UTF_8_STRING);
         epochGroupCD.addAttribute(attribute);
 
         attribute = new Attribute("source", Type.REFERENCE,
                                   sourceCD, Cardinality.TO_ONE);
         epochGroupCD.addAttribute(attribute);
 */
 
         /**
          * Now create some RowData values.
          */
         RowData rootRow = new RowData();
         RowData.setRootRow(rootRow);
         rootRow.setClassUnderQualification(epochCD);
 
         rootRow.setCollectionOperator(CollectionOperator.ANY);
 
         Attribute attribute;
         ArrayList<Attribute> attributePath;
 ///*
         RowData rowData = new RowData();
 
 
         attributePath = new ArrayList<Attribute>();
         attribute = new Attribute("epochGroup", Type.REFERENCE,
                                   epochGroupCD, Cardinality.TO_ONE);
         attributePath.add(attribute);
         attribute = new Attribute("source", Type.REFERENCE,
                                   sourceCD, Cardinality.TO_ONE);
         attributePath.add(attribute);
         attribute = new Attribute("label", Type.UTF_8_STRING);
         attributePath.add(attribute);
         rowData.setAttributePath(attributePath);
 
         rowData.setAttributeOperator("==");
         rowData.setAttributeValue("Test 27");
 
         ArrayList<RowData> childRows = new ArrayList<RowData>();
 
         childRows.add(rowData);
         rootRow.setChildRows(childRows);
 
         /**
          * Create another child row.
          */
 
 /*
         rowData = new RowData();
         //rowData.setParentClass(epochCD);
         rowData.setCollectionOperator(CollectionOperator.ALL);
 
         attributePath = new ArrayList<Attribute>();
         attribute = new Attribute("epochGroup", Type.REFERENCE,
                                   epochGroupCD, Cardinality.TO_ONE);
         attributePath.add(attribute);
         attribute = new Attribute("epochs", Type.REFERENCE,
                                   epochCD, Cardinality.TO_MANY);
         attributePath.add(attribute);
 
         rowData.setAttributePath(attributePath);
 
         childRows.add(rowData);
 
         RowData rowData2 = new RowData();
         //rowData2.setParentClass(epochCD);
         attributePath = new ArrayList<Attribute>();
         attribute = new Attribute("startTime", Type.DATE_TIME);
         attributePath.add(attribute);
 
         rowData2.setAttributeOperator(">=");
         rowData2.setAttributeValue("07/23/2011");
 
         rowData2.setAttributePath(attributePath);
         ArrayList<RowData> childRows2 = new ArrayList<RowData>();
         childRows2.add(rowData2);
         rowData.setChildRows(childRows2);
 
         /**
          * Create another child row.
          */
 /*
         attributePath = new ArrayList<Attribute>();
         attribute = new Attribute("epochGroup", Type.REFERENCE,
                                   epochGroupCD, Cardinality.TO_ONE);
         attributePath.add(attribute);
         attribute = new Attribute("source", Type.REFERENCE,
                                   sourceCD, Cardinality.TO_ONE);
         attributePath.add(attribute);
 
         RowData rowData3 = new RowData();
         rowData3.setAttributePath(attributePath);
         rowData3.setCollectionOperator(CollectionOperator.NONE);
 
         RowData rowData4 = new RowData();
         attribute = new Attribute("label", Type.UTF_8_STRING);
         attributePath = new ArrayList<Attribute>();
         attributePath.add(attribute);
         rowData4.setAttributePath(attributePath);
         rowData4.setAttributeOperator("==");
         rowData4.setAttributeValue("Test 50");
 
         ArrayList<RowData> childRows3 = new ArrayList<RowData>();
         childRows3.add(rowData4);
         rowData3.setChildRows(childRows3);
 
         childRows.add(rowData3);
 
         rootRow.setChildRows(childRows);
 */
         /**
          * The only reason we create an attributePath for the
          * root row is so the getChildmostAttribute() method
          * can be used to get the class of the root row.
          */
         /*
         attributePath = new ArrayList<Attribute>();
         attribute = new Attribute("epoch", Type.REFERENCE,
                                   epochCD, Cardinality.TO_ONE);
         attributePath.add(attribute);
         rootRow.setAttributePath(attributePath);
         */
 
         return(rootRow);
     }
 
     
     /**
      * This is a simple test program for this class.
      */
     public static void main(String[] args) {
 
         System.out.println("RowData test is starting...");
 
         RowData rootRow = createTestRowData();
         System.out.println(rootRow);
 
         System.out.println("RowData test is ending.");
     }
 }
