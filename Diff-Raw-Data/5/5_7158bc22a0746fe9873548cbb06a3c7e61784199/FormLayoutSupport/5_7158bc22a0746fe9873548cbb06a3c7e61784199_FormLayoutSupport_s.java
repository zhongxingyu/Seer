 /*
  * FormLayoutSupport.java
  *
  * Created on January 3, 2005, 10:34 AM
  */
 
 package de.berlios.nblayoutpack.formlayout;
 
 import java.awt.*;
 import java.beans.*;
 import java.lang.reflect.*;
 import java.text.*;
 import java.util.*;
 
 import javax.swing.*;
 
 import org.openide.ErrorManager;
 import org.openide.nodes.*;
 import org.openide.util.Utilities;
 
 import org.netbeans.modules.form.*;
 import org.netbeans.modules.form.codestructure.*;
 import org.netbeans.modules.form.layoutsupport.*;
 
 import com.jgoodies.forms.debug.*;
 import com.jgoodies.forms.layout.*;
 
 import de.berlios.nblayoutpack.formlayout.converter.*;
 import de.berlios.nblayoutpack.formlayout.editors.*;
 
 /**
  *
  * @author  Illya Kysil
  */
 public class FormLayoutSupport extends AbstractLayoutSupport{
   
   /** The icon for FormLayout. */
   private static String iconURL =
       "de/berlios/nblayoutpack/formlayout/resources/formlayout16.png"; // NOI18N
   /** The icon for TableLayout. */
   private static String icon32URL =
       "de/berlios/nblayoutpack/formlayout/resources/formlayout32.png"; // NOI18N
 
   /** Creates a new instance of FormLayoutSupport */
   public FormLayoutSupport(){
   }
   
   public Class getSupportedClass(){
     return com.jgoodies.forms.layout.FormLayout.class;
   }
   
   /** Provides an icon to be used for the layout node in Component
    * Inspector. Only 16x16 color icon is required.
    * @param type is one of BeanInfo constants: ICON_COLOR_16x16,
    *        ICON_COLOR_32x32, ICON_MONO_16x16, ICON_MONO_32x32
    * @return icon to be displayed for node in Component Inspector
    */
   public Image getIcon(int type){
     switch (type) {
       case BeanInfo.ICON_COLOR_16x16:
       case BeanInfo.ICON_MONO_16x16:
         return Utilities.loadImage(iconURL);
       default:
         return Utilities.loadImage(icon32URL);
     }
   }
   
   public static ResourceBundle getBundle(){
     return org.openide.util.NbBundle.getBundle(FormLayoutSupport.class);
   }
 
   private String columns = "p";
   private String rows    = "p";
   private int[][] columnGroups = new int[0][0];
   private int[][] rowGroups    = new int[0][0];
   
   /**
    * Getter for property columns.
    * @return Value of property columns.
    */
   public String getColumns(){
     return columns;
   }
   
   /**
    * Setter for property columns.
    * @param columns New value of property columns.
    */
   public void setColumns(String columns){
     this.columns = columns;
   }
   
   /**
    * Getter for property rows.
    * @return Value of property rows.
    */
   public String getRows(){
     return rows;
   }
   
   /**
    * Setter for property rows.
    * @param rows New value of property rows.
    */
   public void setRows(String rows){
     this.rows = rows;
   }
   
   /**
    * Getter for property columnGroups.
    * @return Value of property columnGroups.
    */
   public int[][] getColumnGroups(){
     return this.columnGroups;
   }
   
   /**
    * Setter for property columnGroups.
    * @param columnGroups New value of property columnGroups.
    */
   public void setColumnGroups(int[][] columnGroups){
     this.columnGroups = columnGroups;
   }
   
   /**
    * Getter for property rowGroups.
    * @return Value of property rowGroups.
    */
   public int[][] getRowGroups(){
     return this.rowGroups;
   }
   
   /**
    * Setter for property rowGroups.
    * @param rowGroups New value of property rowGroups.
    */
   public void setRowGroups(int[][] rowGroups){
     this.rowGroups = rowGroups;
   }
   
   /** Sets up the layout (without adding components) on a real container,
    * according to the internal metadata representation. This method must
    * override AbstractLayoutSupport because FormLayout instance cannot
    * be used universally - new instance must be created for each container.
    * @param container instance of a real container to be set
    * @param containerDelegate effective container delegate of the container;
    *        for layout managers we always use container delegate instead of
    *        the container
    */
   public void setLayoutToContainer(Container container, Container containerDelegate){
     try{
       containerDelegate.setLayout(cloneLayoutInstance(container, containerDelegate));
     }
     catch(Exception e){
       ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
     }
   }
 
   protected LayoutManager cloneLayoutInstance(Container container, Container containerDelegate){
     FormLayout result = new FormLayout(columns, rows);
     result.setColumnGroups(columnGroups);
     result.setRowGroups(rowGroups);
     return result;
   }
   
   protected LayoutManager createDefaultLayoutInstance() throws Exception{
     return new FormLayout(columns, rows);
   }
   
   private static final String baseVarName = "_formLayoutInstance";
 
   /** Creates code structures for a new layout manager (opposite to
    * readInitLayoutCode). As the TableLayout is not a bean, this method must
    * override from AbstractLayoutSupport.
    * @param layoutCode CodeGroup to be filled with relevant
    *        initialization code;
    * @return new CodeExpression representing the TableLayout
    */
   protected CodeExpression createInitLayoutCode(CodeGroup layoutCode){
     CodeStructure codeStructure = getCodeStructure();
     FormProperty[] properties = getProperties();
     CodeExpression[] constrParams = new CodeExpression[2];
     constrParams[0] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[0]));
     constrParams[1] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[1]));
     CodeExpression varExpression = codeStructure.createExpression(FormLayoutIntrospector.getLayoutConstructor(), constrParams);
     String varName = baseVarName;
     CodeVariable var = codeStructure.getVariable(varName);
     int i = 1;
     while(var != null){
       varName = baseVarName + (i++);
       var = codeStructure.getVariable(varName);
     };
     var = codeStructure.createVariable(CodeVariable.LOCAL /*| CodeVariable.EXPLICIT_DECLARATION*/, FormLayout.class, varName);
     codeStructure.attachExpressionToVariable(varExpression, var);
     layoutCode.addStatement(0, var.getAssignment(varExpression));
     CodeExpression[] setColumnGroupsParams = new CodeExpression[1];
     setColumnGroupsParams[0] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[2]));
     layoutCode.addStatement(codeStructure.createStatement(varExpression, FormLayoutIntrospector.getSetColumnGroupsMethod(), setColumnGroupsParams));
     CodeExpression[] setRowGroupsParams = new CodeExpression[1];
     setRowGroupsParams[0] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[3]));
     layoutCode.addStatement(codeStructure.createStatement(varExpression, FormLayoutIntrospector.getSetRowGroupsMethod(), setRowGroupsParams));
     return varExpression;
   }
 
   protected void readInitLayoutCode(CodeExpression codeExpression, CodeGroup layoutCode){
     CodeVariable var = codeExpression.getVariable();
     layoutCode.addStatement(0, var.getAssignment(codeExpression));
     CodeExpression[] constructorParams = var.getAssignment(codeExpression).getStatementParameters();
     FormProperty[] properties = getProperties();
     FormCodeSupport.readPropertyExpression(constructorParams[0], properties[0], false);
     FormCodeSupport.readPropertyExpression(constructorParams[1], properties[1], true);
     Iterator it = CodeStructure.getDefinedStatementsIterator(codeExpression);
     while(it.hasNext()){
       CodeStatement statement = (CodeStatement)it.next();
       if(isMethod(statement, FormLayoutIntrospector.getSetColumnGroupsMethod())){
         FormCodeSupport.readPropertyStatement(statement, properties[2], true);
       }
       if(isMethod(statement, FormLayoutIntrospector.getSetRowGroupsMethod())){
         FormCodeSupport.readPropertyStatement(statement, properties[3], true);
       }
       layoutCode.addStatement(statement);
     }
     updateLayoutInstance();
   }
   
   public boolean isMethod(CodeStatement statement, Method method){
     Object obj = statement.getMetaObject();
     if (obj != null && obj instanceof Method) {
       Method other = (Method)obj;
       // Compare class names only since classes can be loaded by different ClassLoaders
       if((method.getDeclaringClass().getName().equals(other.getDeclaringClass().getName()))
          && (method.getName() == other.getName())) {
         if(!method.getReturnType().equals(other.getReturnType())){
           return false;
         }
         Class[] params1 = method.getParameterTypes();
         Class[] params2 = other.getParameterTypes();
         if (params1.length == params2.length) {
           for (int i = 0; i < params1.length; i++) {
             if (params1[i] != params2[i])
               return false;
           }
           return true;
         }
       }
     }
     return false;
   }
 
   /** Called from createComponentCode method, creates code for a component
    * layout constraints (opposite to readConstraintsCode).
    * @param constrCode CodeGroup to be filled with constraints code; not
    *        needed here because AbsoluteConstraints object is represented
    *        only by a single constructor code expression and no statements
    * @param constr layout constraints metaobject representing the constraints
    * @param compExp CodeExpression object representing the component; not
    *        needed here
    * @return created CodeExpression representing the layout constraints
    */
   protected CodeExpression createConstraintsCode(CodeGroup constrCode,
   LayoutConstraints constr, CodeExpression compExp, int index){
     if(!(constr instanceof FormLayoutSupportConstraints))
       return null;
     
     FormLayoutSupportConstraints flsConstr = (FormLayoutSupportConstraints)constr;
     // code expressions for constructor parameters are created in
     // FormLayoutSupportConstraints
     CodeExpression[] params = flsConstr.createPropertyExpressions(getCodeStructure());
     return getCodeStructure().createExpression(FormLayoutIntrospector.getConstraintsConstructor(), params);
   }
   
   protected LayoutConstraints readConstraintsCode(CodeExpression constrExp, CodeGroup constrCode, CodeExpression compExp){
     FormLayoutSupportConstraints constr = new FormLayoutSupportConstraints();
     // reading is done in FormLayoutSupportConstraints
     constr.readCodeExpression(constrExp, constrCode);
     return constr;
   }
   
   private FormProperty[] properties;
   
   protected FormProperty[] getProperties(){
     if(properties == null){
       properties = createProperties();
     }
     return properties;
   }
 
   protected FormProperty[] createProperties(){
     FormProperty[] properties = new FormProperty[]{
       new FormProperty("columns", // NOI18N
                        String.class,
                        getBundle().getString("PROP_columns"), // NOI18N
                        getBundle().getString("HINT_columns")){ // NOI18N
 
         public Object getTargetValue(){
           return columns;
         }
         public void setTargetValue(Object value){
           columns = (String)value;
         }
         public void setPropertyContext(FormPropertyContext ctx){
           // disabling this method due to limited persistence
           // capabilities (compatibility with previous versions)
         } 
         public PropertyEditor getExpliciteEditor(){
           return new ColumnEditor();
         }
         protected void propertyValueChanged(Object old, Object current){
           super.propertyValueChanged(old, current);
           updateLayoutInstance();
         }
       },
 
       new FormProperty("rows", // NOI18N
                        String.class,
                        getBundle().getString("PROP_rows"), // NOI18N
                        getBundle().getString("HINT_rows")){ // NOI18N
 
         public Object getTargetValue(){
           return rows;
         }
         public void setTargetValue(Object value){
           rows = (String)value;
         }
         public void setPropertyContext(FormPropertyContext ctx){
           // disabling this method due to limited persistence
           // capabilities (compatibility with previous versions)
         } 
         public PropertyEditor getExpliciteEditor(){
           return new RowEditor();
         }
         protected void propertyValueChanged(Object old, Object current){
           super.propertyValueChanged(old, current);
           updateLayoutInstance();
         }
       },
 
       new FormProperty("columnGroups", // NOI18N
                        int[][].class,
                        getBundle().getString("PROP_columnGroups"), // NOI18N
                        getBundle().getString("HINT_columnGroups")){ // NOI18N
 
         public Object getTargetValue(){
           return columnGroups;
         }
         public void setTargetValue(Object value){
           columnGroups = (int[][])value;
         }
         public void setPropertyContext(FormPropertyContext ctx){
           // disabling this method due to limited persistence
           // capabilities (compatibility with previous versions)
         }
         public PropertyEditor getExpliciteEditor(){
           return new ColumnGroupsEditor();
         }
       },
 
       new FormProperty("rowGroups", // NOI18N
                        int[][].class,
                        getBundle().getString("PROP_rowGroups"), // NOI18N
                        getBundle().getString("HINT_rowGroups")){ // NOI18N
 
         public Object getTargetValue(){
           return rowGroups;
         }
         public void setTargetValue(Object value){
           rowGroups = (int[][])value;
         }
         public void setPropertyContext(FormPropertyContext ctx){
           // disabling this method due to limited persistence
           // capabilities (compatibility with previous versions)
         }
         public PropertyEditor getExpliciteEditor(){
           return new RowGroupsEditor();
         }
       },
     };
     properties[2].setValue("canEditAsText", Boolean.TRUE); // NOI18N
     properties[3].setValue("canEditAsText", Boolean.TRUE); // NOI18N
     return properties;
   }
 
   public LayoutConstraints createDefaultConstraints(){
     return new FormLayoutSupportConstraints();
   }
   
   /** Provides resizing options for given component. It can combine the
    * bit-flag constants RESIZE_UP, RESIZE_DOWN, RESIZE_LEFT, RESIZE_RIGHT.
    * @param container instance of a real container in which the
    *        component is to be resized
    * @param containerDelegate effective container delegate of the container
    *        (e.g. like content pane of JFrame)
    * @param component real component to be resized
    * @param index position of the component in its container
    * @return resizing options for the component; 0 if no resizing is possible
    */
   public int getResizableDirections(Container container, Container containerDelegate,
   Component component, int index){
     FormLayout formLayout = (FormLayout)containerDelegate.getLayout();
     FormLayoutSupportConstraints flConstr = (FormLayoutSupportConstraints)getConstraints(index);
     
     int resizable = 0;
     
     if((flConstr.gridX > 1) || (flConstr.gridWidth > 0)){
       resizable |= RESIZE_LEFT;
     }
     
     if((flConstr.gridX + flConstr.gridWidth < formLayout.getColumnCount()) ||
        (flConstr.gridWidth > 0)){
       resizable |= RESIZE_RIGHT;
     }
     
     if((flConstr.gridY > 1) || (flConstr.gridHeight > 0)){
       resizable |= RESIZE_UP;
     }
     
     if((flConstr.gridY + flConstr.gridHeight < formLayout.getRowCount()) ||
        (flConstr.gridHeight > 0)){
       resizable |= RESIZE_DOWN;
     }
     
     return resizable;
   }
   
   private int[] getSizes(int[] offsets){
     int[] result = new int[offsets.length - 1];
     for(int i = 1; i < offsets.length; i++){
       result[i - 1] = offsets[i] - offsets[i - 1];
     }
     return result;
   }
   
   /** This method should paint a feedback for a component dragged over
    * a container (or just for mouse cursor being moved over container).
    * In principle, it should present given component layout constraints or
    * index graphically.
    * @param container instance of a real container over/in which the
    *        component is dragged
    * @param containerDelegate effective container delegate of the container
    *        (e.g. like content pane of JFrame) - here the feedback is painted
    * @param component the real component being dragged, can be null
    * @param newConstraints component layout constraints to be presented
    * @param newIndex component's index position to be presented
    *        (if newConstraints == null)
    * @param g Graphics object for painting (with color and line style set)
    * @return whether any feedback was painted (may return false if the
    *         constraints or index are invalid, or if the painting is not
    *         implemented)
    */
   public boolean paintDragFeedback(Container container, Container containerDelegate,
   Component component, LayoutConstraints newConstraints, int newIndex, Graphics g){
     FormLayout formLayout = (FormLayout)containerDelegate.getLayout();
     FormLayoutSupportConstraints flConstr = (FormLayoutSupportConstraints)newConstraints;
     FormLayout.LayoutInfo layoutInfo = FormDebugUtils.getLayoutInfo(containerDelegate);
     int[] columnSize = getSizes(layoutInfo.columnOrigins);
     int[] rowSize = getSizes(layoutInfo.rowOrigins);
 
     int counter;
 
     Color defColor = g.getColor();
     Rectangle sRect = new Rectangle(-1, -1, 0, 0);
     int y = 0;
     for(int row = 0; row < rowSize.length; row++){
       if((row + 1 >= flConstr.gridY) && (row + 1 <= flConstr.gridY + flConstr.gridHeight - 1)){
         if(sRect.y < 0){
           sRect.y = y;
         }
         sRect.height += rowSize[row];
       }
       g.drawLine(0, y, containerDelegate.getWidth(), y);
       y += rowSize[row];
     }
     g.drawLine(0, y, containerDelegate.getWidth(), y);
     int x = 0;
     for(int column = 0; column < columnSize.length; column++){
       if((column + 1 >= flConstr.gridX) && (column + 1 <= flConstr.gridX + flConstr.gridWidth - 1)){
         if(sRect.x < 0){
           sRect.x = x;
         }
         sRect.width += columnSize[column];
       }
       g.drawLine(x, 0, x, containerDelegate.getHeight());
       x += columnSize[column];
     }
     g.drawLine(x, 0, x, containerDelegate.getHeight());
     g.setColor(Color.red);
     g.drawRect(sRect.x, sRect.y, sRect.width, sRect.height);
     return true;
   }
   
     /** This method should calculate layout constraints for a component dragged
    * over a container (or just for mouse cursor being moved over container).
    * @param container instance of a real container over/in which the
    *        component is dragged
    * @param containerDelegate effective container delegate of the container
    *        (e.g. like content pane of JFrame)
    * @param component the real component being dragged, can be null
    * @param index position (index) of the component in its current container;
    *        -1 if there's no dragged component
    * @param posInCont position of mouse in the container delegate
    * @param posInComp position of mouse in the dragged component; null if
    *        there's no dragged component
    * @return new LayoutConstraints object corresponding to the position of
    *         the component in the container; may return null if the layout
    *         does not use component constraints, or if default constraints
    *         should be used
    */
   public LayoutConstraints getNewConstraints(Container container, Container containerDelegate,
   Component component, int index, Point posInCont, Point posInComp){
     FormLayout formLayout = (FormLayout)containerDelegate.getLayout();
     FormLayoutSupportConstraints flConstr = (FormLayoutSupportConstraints)getConstraints(index);
     FormLayout.LayoutInfo layoutInfo = FormDebugUtils.getLayoutInfo(containerDelegate);
     int[] columnSize = getSizes(layoutInfo.columnOrigins);
     int[] rowSize = getSizes(layoutInfo.rowOrigins);
     int[] columnOffset = layoutInfo.columnOrigins;
     int[] rowOffset = layoutInfo.rowOrigins;
     int x = posInCont.x;
     int y = posInCont.y;
     int column = 0;
     int row = 0;
     
     for(int i = 0; i < columnSize.length; i++){
       if(x >= columnOffset[i]){
         column = i;
       }
       if(x < columnOffset[i + 1]){
         break;
       }
     }
     
     for(int i = 0; i < rowSize.length; i++){
       if(y >= rowOffset[i]){
         row = i;
       }
       if(y < rowOffset[i + 1]){
         break;
       }
     }
     
     CellConstraints.Alignment hAlign = flConstr != null ? flConstr.hAlign : CellConstraints.DEFAULT;
     CellConstraints.Alignment vAlign = flConstr != null ? flConstr.vAlign : CellConstraints.DEFAULT;
     Insets insets = flConstr != null ? flConstr.insets : new Insets(0, 0, 0, 0);
     return new FormLayoutSupportConstraints(column + 1, row + 1, 1, 1, hAlign, vAlign, insets);
   }
   
 /** This method should calculate layout constraints for a component being
    * resized.
    * @param container instance of a real container in which the
    *        component is to be resized
    * @param containerDelegate effective container delegate of the container
    *        (e.g. like content pane of JFrame)
    * @param component real component to be resized
    * @param index position of the component in its container
    * @param sizeChanges Insets object with size differences
    * @param posInCont position of mouse in the container delegate
    * @return component layout constraints for resized component; null if
    *         resizing is not possible or not implemented
    */
   public LayoutConstraints getResizedConstraints(Container container, Container containerDelegate,
   Component component, int index, Insets sizeChanges, Point posInCont){
     FormLayout formLayout = (FormLayout)containerDelegate.getLayout();
     FormLayoutSupportConstraints flConstr = (FormLayoutSupportConstraints)getConstraints(index);
     FormLayout.LayoutInfo layoutInfo = FormDebugUtils.getLayoutInfo(containerDelegate);
     int[] columnSize = getSizes(layoutInfo.columnOrigins);
     int[] rowSize = getSizes(layoutInfo.rowOrigins);
     int[] columnOffset = layoutInfo.columnOrigins;
     int[] rowOffset = layoutInfo.rowOrigins;
     
     int fLeft = flConstr.gridX - 1;
     int fRight = flConstr.gridX + flConstr.gridWidth - 2;
     int fTop = flConstr.gridY - 1;
     int fBottom = flConstr.gridY + flConstr.gridHeight - 2;
     
     int left;
     if(sizeChanges.left != 0){
       left = 0;
       for(int i = 0; i < columnSize.length; i++){
         if(posInCont.x >= columnOffset[i]){
           left = i;
         }
         if(posInCont.x < columnOffset[i + 1]){
           break;
         }
       }
       if(left > fRight)
         left = fRight;
     }
     else{
       left = fLeft;
     }
     
     int top;
     if(sizeChanges.top != 0){
       top = 0;
       for(int i = 0; i < rowSize.length; i++){
         if(posInCont.y >= rowOffset[i]){
           top = i;
         }
         if(posInCont.y < rowOffset[i + 1]){
           break;
         }
       }
       if(top > fBottom){
         top = fBottom;
       }
     }
     else{
       top = fTop;
     }
     
     int right;
     if(sizeChanges.right != 0) {
       right = formLayout.getColumnCount() - 1;
       for(int i = columnSize.length; i > 0; i--){
         if(posInCont.x < columnOffset[i]){
           right = i - 1;
         }
         if(posInCont.x >= columnOffset[i - 1]){
           break;
         }
       }
       if(right < fLeft){
         right = fLeft;
       }
     }
     else{
       right = fRight;
     }
     
     int bottom;
     if(sizeChanges.bottom != 0){
       bottom = formLayout.getRowCount() - 1;
       for(int i = rowSize.length; i > 0; i--){
         if(posInCont.y < rowOffset[i]){
           bottom = i - 1;
         }
         if(posInCont.y >= rowOffset[i - 1]){
           break;
         }
       }
       if(bottom < fTop){
         bottom = fTop;
       }
     }
     else{
       bottom = fBottom;
     }
     return new FormLayoutSupportConstraints(left + 1, top + 1, right - left + 1, bottom - top + 1, flConstr.hAlign, flConstr.vAlign, flConstr.insets);
   }
 
   /** This method is called after a constraint property of some component
    * is changed by the user. Subclasses may check if the layout is valid
    * after the change and throw PropertyVetoException if the change should
    * be reverted. It's up to the delegate to display an error or warning
    * message, the exception is not reported outside. The default
    * implementation accepts any change.
    * @param index index of the component in the layout
    * @param ev PropertyChangeEvent object describing the change
    */
   public void acceptComponentLayoutChange(int index, PropertyChangeEvent ev) throws PropertyVetoException{
     FormLayout formLayout = (FormLayout)getLayoutContext().getPrimaryContainerDelegate().getLayout();
     FormLayoutSupportConstraints flConstr = new FormLayoutSupportConstraints((FormLayoutSupportConstraints)getConstraints(index));
     boolean isValid = true;
     if(ev.getPropertyName().equals("CellConstraints.gridX")){
       int gridX = ((Integer)ev.getNewValue()).intValue();
      isValid = (gridX > 1) && (gridX + flConstr.gridWidth - 1 <= formLayout.getColumnCount());
     }
     if(ev.getPropertyName().equals("CellConstraints.gridWidth")){
       int gridWidth = ((Integer)ev.getNewValue()).intValue();
       isValid = (gridWidth > 0) && (flConstr.gridX + gridWidth - 1 <= formLayout.getColumnCount());
     }
     if(ev.getPropertyName().equals("CellConstraints.gridY")){
       int gridY = ((Integer)ev.getNewValue()).intValue();
      isValid = (gridY > 1) && (gridY + flConstr.gridHeight - 1 <= formLayout.getRowCount());
     }
     if(ev.getPropertyName().equals("CellConstraints.gridHeight")){
       int gridHeight = ((Integer)ev.getNewValue()).intValue();
       isValid = (gridHeight > 0) && (flConstr.gridY + gridHeight - 1 <= formLayout.getRowCount());
     }
     if(!isValid){
       String messageFormat = getBundle().getString("EXC_InvalidComponentLayoutPropertyChange");
       String message = MessageFormat.format(messageFormat, new Object[]{ev.getPropertyName(), ev.getNewValue()});
       throw new PropertyVetoException(message, ev);
     }
   }
   
   /** This method is called after a property of the layout is changed by
    * the user. Subclasses may check whether the layout is valid after the
    * change and throw PropertyVetoException if the change should be reverted.
    * It's up to the delagate to display an error or warning message, the
    * exception is not reported outside. The default implementation accepts
    * any change.
    * @param ev PropertyChangeEvent object describing the change
    */
   public void acceptContainerLayoutChange(PropertyChangeEvent ev) throws PropertyVetoException{
     Container container = getLayoutContext().getPrimaryContainerDelegate();
     FormLayout formLayout = (FormLayout)container.getLayout();
     Component[] comps = container.getComponents();
     CellConstraints[] ccs = new CellConstraints[comps.length];
     for(int i = 0; i < comps.length; i++){
       ccs[i] = (CellConstraints)formLayout.getConstraints(comps[i]);
     }
     ColumnSpec[] colSpecs = new ColumnSpec[formLayout.getColumnCount()];
     for(int i = 0; i < colSpecs.length; i++){
       colSpecs[i] = formLayout.getColumnSpec(i + 1);
     }
     RowSpec[] rowSpecs = new RowSpec[formLayout.getRowCount()];
     for(int i = 0; i < rowSpecs.length; i++){
       rowSpecs[i] = formLayout.getRowSpec(i + 1);
     }
     int[][] colGroups = formLayout.getColumnGroups();
     int[][] rowGroups = formLayout.getRowGroups();
     if(ev.getPropertyName().equals("columns")){
       try{
         colSpecs = ColumnSpec.decodeSpecs((String)ev.getNewValue());
       }
       catch(Exception e){
         throw new PropertyVetoException("", ev);
       }
     }
     if(ev.getPropertyName().equals("rows")){
       try{
         rowSpecs = RowSpec.decodeSpecs((String)ev.getNewValue());
       }
       catch(Exception e){
         throw new PropertyVetoException("", ev);
       }
     }
     if(ev.getPropertyName().equals("columnGroups")){
       colGroups = (int[][])ev.getNewValue();
     }
     if(ev.getPropertyName().equals("rowGroups")){
       rowGroups = (int[][])ev.getNewValue();
     }
     for(int i = 0; i < ccs.length; i++){
       if(ccs[i].gridX + ccs[i].gridWidth - 2 >= colSpecs.length){
         throw new PropertyVetoException("", ev);
       }
       if(ccs[i].gridY + ccs[i].gridHeight - 2 >= rowSpecs.length){
         throw new PropertyVetoException("", ev);
       }
     }
     boolean[] colPresent = new boolean[formLayout.getColumnCount()];
     Arrays.fill(colPresent, false);
     for(int group = 0; group < colGroups.length; group++){
       for(int item = 0; item < colGroups[group].length; item++){
         if((colGroups[group][item] < 1) || (colGroups[group][item] > formLayout.getColumnCount()) || colPresent[colGroups[group][item] - 1]){
           throw new PropertyVetoException("", ev);
         }
         colPresent[colGroups[group][item] - 1] = true;
       }
     }
     boolean[] rowPresent = new boolean[formLayout.getRowCount()];
     Arrays.fill(rowPresent, false);
     for(int group = 0; group < rowGroups.length; group++){
       for(int item = 0; item < rowGroups[group].length; item++){
         if((rowGroups[group][item] < 1) || (rowGroups[group][item] > formLayout.getRowCount()) || rowPresent[rowGroups[group][item] - 1]){
           throw new PropertyVetoException("", ev);
         }
         rowPresent[rowGroups[group][item] - 1] = true;
       }
     }
     super.acceptContainerLayoutChange(ev);
   }
   
   /** This method is called when switching layout - giving an opportunity to
    * convert the previous constrainst of components to constraints of the new
    * layout (this layout). The default implementation does nothing.
    * @param previousConstraints [input] layout constraints of components in
    *                                    the previous layout
    * @param currentConstraints [output] array of converted constraints for
    *                                    the new layout - to be filled
    * @param components [input] real components in a real container having the
    *                           previous layout
    */
   public void convertConstraints(LayoutConstraints[] previousConstraints,
   LayoutConstraints[] currentConstraints, Component[] components){
     if((components == null) || (components.length == 0)){
       return;
     }
     ConstraintsConverter converter = new DefaultConstraintsConverter();
     converter.convertConstraints(getLayoutContext(), this, previousConstraints, currentConstraints, components);
   }
 
   public class FormLayoutSupportConstraints implements LayoutConstraints{
 
     private Node.Property[] properties;
 
     public int gridX;
     public int gridY;
     public int gridWidth;
     public int gridHeight;
     public CellConstraints.Alignment hAlign;
     public CellConstraints.Alignment vAlign;
     public Insets insets;
 
     /** Creates a new instance of FormLayoutSupportConstraints */
     public FormLayoutSupportConstraints(){
       CellConstraints cc = new CellConstraints();
       gridX = cc.gridX;
       gridY = cc.gridY;
       gridWidth = cc.gridWidth;
       gridHeight = cc.gridHeight;
       hAlign = cc.hAlign;
       vAlign = cc.vAlign;
       insets = cc.insets;
     }
 
     public FormLayoutSupportConstraints(FormLayoutSupportConstraints prototype){
       this.gridX = prototype.gridX;
       this.gridY = prototype.gridY;
       this.gridWidth = prototype.gridWidth;
       this.gridHeight = prototype.gridHeight;
       this.hAlign = prototype.hAlign;
       this.vAlign = prototype.vAlign;
       this.insets = prototype.insets;
     }
 
     public FormLayoutSupportConstraints(int gridX, int gridY, int gridWidth, int gridHeight,
     CellConstraints.Alignment hAlign, CellConstraints.Alignment vAlign, Insets insets){
       this.gridX = gridX;
       this.gridY = gridY;
       this.gridWidth = gridWidth;
       this.gridHeight = gridHeight;
       this.hAlign = hAlign;
       this.vAlign = vAlign;
       this.insets = insets;
     }
 
     public String toString(){
       StringBuffer sb = new StringBuffer(getClass().getName()).append("[");
       sb.append("gridX=").append(gridX).append(",");
       sb.append("gridY=").append(gridY).append(",");
       sb.append("gridWidth=").append(gridWidth).append(",");
       sb.append("gridHeight=").append(gridHeight).append(",");
       sb.append("hAlign=").append(hAlign).append(",");
       sb.append("vAlign=").append(vAlign).append(",");
       sb.append("insets=").append(insets).append("]");
       return sb.toString();
     }
     
     public Node.Property[] getProperties(){
       if(properties == null){
         properties = createProperties();
         reinstateProperties();
       }
       return properties;
     }
 
     public Object getConstraintsObject(){
       return new CellConstraints(gridX, gridY, gridWidth, gridHeight, hAlign, vAlign, insets);
     }
 
     public LayoutConstraints cloneConstraints(){
       return new FormLayoutSupportConstraints(this);
     }
 
     protected Node.Property[] createProperties(){
       return new Node.Property[]{
         new FormProperty("CellConstraints.gridX", // NOI18N
                          Integer.TYPE,
                          getBundle().getString("PROP_gridX"), // NOI18N
                          getBundle().getString("HINT_gridX")){ // NOI18N
 
           public Object getTargetValue(){
             return new Integer(gridX);
           }
           public void setTargetValue(Object value){
             gridX = ((Integer)value).intValue();
           }
           public void setPropertyContext(FormPropertyContext ctx){
             // disabling this method due to limited persistence
             // capabilities (compatibility with previous versions)
           } 
         },
 
         new FormProperty("CellConstraints.gridY", // NOI18N
                          Integer.TYPE,
                          getBundle().getString("PROP_gridY"), // NOI18N
                          getBundle().getString("HINT_gridY")){ // NOI18N
 
           public Object getTargetValue(){
             return new Integer(gridY);
           }
           public void setTargetValue(Object value){
             gridY = ((Integer)value).intValue();
           }
           public void setPropertyContext(FormPropertyContext ctx){
             // disabling this method due to limited persistence
             // capabilities (compatibility with previous versions)
           } 
         },
 
         new FormProperty("CellConstraints.gridWidth", // NOI18N
                          Integer.TYPE,
                          getBundle().getString("PROP_gridWidth"), // NOI18N
                          getBundle().getString("HINT_gridWidth")){ // NOI18N
 
           public Object getTargetValue(){
             return new Integer(gridWidth);
           }
           public void setTargetValue(Object value){
             gridWidth = ((Integer)value).intValue();
           }
           public void setPropertyContext(FormPropertyContext ctx){
             // disabling this method due to limited persistence
             // capabilities (compatibility with previous versions)
           }
         },
 
         new FormProperty("CellConstraints.gridHeight", // NOI18N
                          Integer.TYPE,
                          getBundle().getString("PROP_gridHeight"), // NOI18N
                          getBundle().getString("HINT_gridHeight")){ // NOI18N
 
           public Object getTargetValue(){
             return new Integer(gridHeight);
           }
           public void setTargetValue(Object value){
             gridHeight = ((Integer)value).intValue();
           }
           public void setPropertyContext(FormPropertyContext ctx){
             // disabling this method due to limited persistence
             // capabilities (compatibility with previous versions)
           } 
         },
 
         new FormProperty("CellConstraints.hAlign", // NOI18N
                          CellConstraints.Alignment.class,
                          getBundle().getString("PROP_hAlign"), // NOI18N
                          getBundle().getString("HINT_hAlign")){ // NOI18N
 
           public Object getTargetValue(){
             return hAlign;
           }
           public void setTargetValue(Object value){
             hAlign = (CellConstraints.Alignment)value;
           }
           public void setPropertyContext(FormPropertyContext ctx){
             // disabling this method due to limited persistence
             // capabilities (compatibility with previous versions)
           } 
           public PropertyEditor getExpliciteEditor(){
             return new HAlignEditor();
           }
         },
 
         new FormProperty("CellConstraints.vAlign", // NOI18N
                          CellConstraints.Alignment.class,
                          getBundle().getString("PROP_vAlign"), // NOI18N
                          getBundle().getString("HINT_vAlign")){ // NOI18N
 
           public Object getTargetValue(){
             return vAlign;
           }
           public void setTargetValue(Object value){
             vAlign = (CellConstraints.Alignment)value;
           }
           public void setPropertyContext(FormPropertyContext ctx){
             // disabling this method due to limited persistence
             // capabilities (compatibility with previous versions)
           } 
           public PropertyEditor getExpliciteEditor(){
             return new VAlignEditor();
           }
         },
 
         new FormProperty("CellConstraints.insets", // NOI18N
                          Insets.class,
                          getBundle().getString("PROP_insets"), // NOI18N
                          getBundle().getString("HINT_insets")){ // NOI18N
 
           public Object getTargetValue(){
             return insets;
           }
           public void setTargetValue(Object value){
             insets = (Insets)value;
           }
           public void setPropertyContext(FormPropertyContext ctx){
             // disabling this method due to limited persistence
             // capabilities (compatibility with previous versions)
           } 
         }
       };
     }
 
     private void reinstateProperties(){
       try{
         for(int i = 0; i < properties.length; i++){
           FormProperty prop = (FormProperty)properties[i];
           prop.reinstateProperty();
         }
       }
       catch(IllegalAccessException e1) {} // should not happen
       catch(java.lang.reflect.InvocationTargetException e2) {} // should not happen
     }
 
     /** This method creates CodeExpression objects for properties of
      * AbsoluteConstraints - this is used by the layout delegate's method
      * createConstraintsCode which uses the expressions as parameters
      * in AbsoluteConstraints constructor.
      * @param codeStructure main CodeStructure object in which the code
      *        expressions are created
      * @param shift this parameter is used only by subclasses of
      *        AbsoluteLayoutConstraints (which may insert another
      *        constructor parameters before x, y, w and h)
      * @return array of created code expressions
      */
     protected final CodeExpression[] createPropertyExpressions(CodeStructure codeStructure){
       // first make sure properties are created...
       getProperties();
       
       // ...then create code expressions based on the properties
       ArrayList params = new ArrayList();
       params.add(codeStructure.createExpression(FormCodeSupport.createOrigin(properties[0])));
       params.add(codeStructure.createExpression(FormCodeSupport.createOrigin(properties[1])));
       params.add(codeStructure.createExpression(FormCodeSupport.createOrigin(properties[2])));
       params.add(codeStructure.createExpression(FormCodeSupport.createOrigin(properties[3])));
       params.add(codeStructure.createExpression(FormCodeSupport.createOrigin(properties[4])));
       params.add(codeStructure.createExpression(FormCodeSupport.createOrigin(properties[5])));
       params.add(codeStructure.createExpression(FormCodeSupport.createOrigin(properties[6])));
       return (CodeExpression[])params.toArray(new CodeExpression[params.size()]);
     }
     
     protected final void readCodeExpression(CodeExpression constrExp, CodeGroup codeGroup){
       // first make sure properties are created...
       getProperties();
       CodeExpression[] params = constrExp.getOrigin().getCreationParameters();
       FormCodeSupport.readPropertyExpression(params[0], properties[0], false);
       FormCodeSupport.readPropertyExpression(params[1], properties[1], false);
       FormCodeSupport.readPropertyExpression(params[2], properties[2], false);
       FormCodeSupport.readPropertyExpression(params[3], properties[3], false);
       FormCodeSupport.readPropertyExpression(params[4], properties[4], false);
       FormCodeSupport.readPropertyExpression(params[5], properties[5], false);
       FormCodeSupport.readPropertyExpression(params[6], properties[6], false);
     }
 
   }
 
 }
