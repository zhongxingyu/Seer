 package edu.uwlax.toze.editor;
 
 import edu.uwlax.toze.domain.*;
 import edu.uwlax.toze.objectz.TozeSpecificationChecker;
 import edu.uwlax.toze.objectz.TozeToken;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  *
  */
 public class SpecificationController extends Observable implements FocusListener, Observer
 {
     static private SpecObject cachedObject = null;
 
     private final SpecificationDocument specificationDocument;
     private final Specification specification;
 
     private SpecificationView specificationView;
     private final ControllerMouseAdapter mouseAdapter;
 //    private final ControllerKeyAdapter keyAdapter;
     private TozeTextArea currentTextArea = null;
     private SpecObject selectedObject;
     private ParagraphView selectedView;
 
     /**
      * Create a controller for the given specification model and view.
      *
      * @param specificationDocument The specification to display.
      *
      * @throws IllegalArgumentException specification and specificationView must
      *                                  not be null
      */
     public SpecificationController(SpecificationDocument specificationDocument)
             throws IllegalArgumentException
     {
         this.specificationDocument = specificationDocument;
         this.specification = specificationDocument.getSpecification();
         this.mouseAdapter = new ControllerMouseAdapter();
 //        this.keyAdapter = new ControllerKeyAdapter();
 
         // create a map of the UI views to the model objects
         // they represent
 //        viewToObjectMap = new HashMap<JComponent, SpecObjectPropertyPair>();
 
     }
 
     public SpecificationView getSpecificationView()
     {
         return specificationView;
     }
 
     public void setSpecificationView(SpecificationView specificationView)
     {
         this.specificationView = specificationView;
         initView();
     }
 
     public MouseAdapter getMouseAdapter()
     {
         return mouseAdapter;
     }
 
     /**
      * Create the UI views based on the initial specification. This build each
      * UI view based
      * on the specification document structure.
      */
     private void initView()
     {
         specificationView.addMouseListener(mouseAdapter);
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public SpecificationDocument getSpecificationDocument()
     {
         return specificationDocument;
     }
 
     /**
      * Get the specification (model) for this controller.
      *
      * @return A TOZE specification.
      */
     public Specification getSpecification()
     {
         return specification;
     }
 
     /**
      * Add a DeltaList to an Operation
      *
      * @param operation The Operation to add the DeltaList
      */
     public void addDeltaList(Operation operation)
     {
         checkIllegalArgument(operation, "operation");
 
         operation.setDeltaList("New Delta List");
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param operation
      */
     public void removeDeltaList(Operation operation)
     {
         checkIllegalArgument(operation, "operation");
 
         operation.setDeltaList(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Add a Declaration to an Operation
      *
      * @param operation The Operation to add the Declaration
      */
     public void addDeclaration(Operation operation)
     {
         checkIllegalArgument(operation, "operation");
 
         operation.setDeclaration("New Declaration");
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param operation
      */
     public void removeDeclaration(Operation operation)
     {
         checkIllegalArgument(operation, "operation");
 
         operation.setDeclaration(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Add an InitialState to a ClassDef
      *
      * @param classDef The ClassDef to add the InitialState
      */
     public void addInitialState(ClassDef classDef)
     {
         checkIllegalArgument(classDef, "classDef");
 
         InitialState initialState = new InitialState();
         initialState.setPredicate("New Initial State");
         classDef.setInitialState(initialState);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param classDef
      */
     public void removeInitialState(ClassDef classDef)
     {
         checkIllegalArgument(classDef, "classDef");
 
         classDef.setInitialState(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Add a State to a ClassDef
      *
      * @param classDef  The ClassDef to add the State
      * @param stateType Which state type is to be presented, All, No Predicate, No Declaration or Bracketed
      */
     public void addState(ClassDef classDef, StateType stateType)
     {
         checkIllegalArgument(classDef, "classDef");
 
         State state = new State();
 
         switch (stateType)
             {
             case All:
                 state.setDeclaration("New Declaration");
                 state.setPredicate("New Predicate");
                 break;
             case NoPredicate:
                 state.setDeclaration("New Declaration");
                 break;
             case NoDeclaration:
                 state.setPredicate("New Predicate");
                 break;
             case Bracket:
                 state.setName("New State");
             }
         classDef.setState(state);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Remove a State from a ClassDef
      */
     public void removeState(ClassDef classDef)
     {
         checkIllegalArgument(classDef, "classDef");
 
         classDef.setState(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Add an OperationExpression to an Operation
      *
      * @param operation The Operation to add the OperationExpression
      */
     public void addOperationExpression(Operation operation)
     {
         checkIllegalArgument(operation, "operation");
 
         operation.setOperationExpression("New Expression");
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Remove the expression from the given operation
      *
      * @param operation Operation from which to remove its expression
      */
     public void removeOperationExpression(Operation operation)
     {
         checkIllegalArgument(operation, "operation");
 
         operation.setOperationExpression(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param operation
      */
     public void addOperationPredicate(Operation operation)
     {
         checkIllegalArgument(operation, "operation");
 
         operation.setPredicate("New Predicate");
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Remove the predicate from the given operation
      *
      * @param operation Operation from which to remove its predicate
      */
     public void removeOperationPredicate(Operation operation)
     {
         checkIllegalArgument(operation, "operation");
 
         operation.setPredicate(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Add a ClassDef to the specification (TOZE)
      */
     public void addClass()
     {
         ClassDef classDef = new ClassDef();
         classDef.setName("New Class");
 
         specification.addClassDef(classDef);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void removeClass(ClassDef classDef)
     {
         specification.removeClassDef(classDef);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      */
     public void addAxiomaticType(SpecObject object, boolean hasPredicate)
     {
         checkIllegalArgument(object, "specObject");
 
         AxiomaticDef axiomaticDef = new AxiomaticDef();
         axiomaticDef.setDeclaration("New Axiomatic Type");
 
         if (hasPredicate)
             {
             axiomaticDef.setPredicate("New Predicate");
             }
 
         if (object == specification)
             {
             axiomaticDef.setSpecification(specification);
             specification.addAxiomaticDef(axiomaticDef);
             }
         else
             {
             ClassDef classDef = (ClassDef) object;
             axiomaticDef.setClassDef(classDef);
             classDef.addAxiomaticDef(axiomaticDef);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void removeAxiomaticType(AxiomaticDef axiomaticDef)
     {
         checkIllegalArgument(axiomaticDef, "axiomaticDef");
 
         if (axiomaticDef.getSpecification() != null)
             {
             specification.removeAxiomaticDef(axiomaticDef);
             }
         else if (axiomaticDef.getClassDef() != null)
             {
             ClassDef classDef = axiomaticDef.getClassDef();
             classDef.removeAxiomaticDef(axiomaticDef);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
 
     public void addAxiomaticPredicate(AxiomaticDef axiomaticDef)
     {
         checkIllegalArgument(axiomaticDef, "axiomaticDef");
 
         axiomaticDef.setPredicate("New Predicate");
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void removeAxiomaticPredicate(AxiomaticDef axiomaticDef)
     {
         checkIllegalArgument(axiomaticDef, "axiomaticDef");
 
         axiomaticDef.setPredicate(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      */
     public void addAbbreviation(SpecObject object)
     {
         checkIllegalArgument(object, "specObject");
 
         AbbreviationDef abbreviationDef = new AbbreviationDef();
         abbreviationDef.setName("New Abbreviation");
         abbreviationDef.setExpression("New Expression");
 
         if (object == specification)
             {
             abbreviationDef.setSpecification(specification);
             specification.addAbbreviationDef(abbreviationDef);
             }
         else
             {
             ClassDef classDef = (ClassDef) object;
             abbreviationDef.setClassDef(classDef);
             classDef.addAbbreviationDef(abbreviationDef);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param abbreviationDef
      */
     public void removeAbbreviation(AbbreviationDef abbreviationDef)
     {
         checkIllegalArgument(abbreviationDef, "abbreviationDef");
 
         if (abbreviationDef.getSpecification() != null)
             {
             specification.removeAbbreviationDef(abbreviationDef);
             }
         else
             {
             ClassDef classDef = abbreviationDef.getClassDef();
             classDef.removeAbbreviationDef(abbreviationDef);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param object
      */
     public void addBasicType(SpecObject object)
     {
         checkIllegalArgument(object, "specObject");
 
         BasicTypeDef basicTypeDef = new BasicTypeDef();
         basicTypeDef.setName("New Basic Type");
 
         if (object == specification)
             {
             basicTypeDef.setSpecification(specification);
             specification.addBasicTypeDef(basicTypeDef);
             }
         else
             {
             ClassDef classDef = (ClassDef) object;
             basicTypeDef.setClassDef(classDef);
             classDef.addBasicTypeDef(basicTypeDef);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void removeBasicType(BasicTypeDef basicTypeDef)
     {
         checkIllegalArgument(basicTypeDef, "basicTypeDef");
 
         if (basicTypeDef.getSpecification() != null)
             {
             specification.removeBasicTypeDef(basicTypeDef);
             }
         else if (basicTypeDef.getClassDef() != null)
             {
             ClassDef classDef = basicTypeDef.getClassDef();
             classDef.removeBasicTypeDef(basicTypeDef);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param object
      */
     public void addFreeType(SpecObject object)
     {
         checkIllegalArgument(object, "specObject");
 
         FreeTypeDef freeTypeDef = new FreeTypeDef();
         freeTypeDef.setDeclaration("New Free Type");
         freeTypeDef.setPredicate("New Predicate");
 
         if (object == specification)
             {
             freeTypeDef.setSpecification(specification);
             specification.addFreeTypeDef(freeTypeDef);
             }
         else
             {
             ClassDef classDef = (ClassDef) object;
             freeTypeDef.setClassDef(classDef);
             classDef.addFreeTypeDef(freeTypeDef);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void removeFreeType(FreeTypeDef freeTypeDef)
     {
         checkIllegalArgument(freeTypeDef, "freeTypeDef");
 
         if (freeTypeDef.getSpecification() != null)
             {
             specification.removeFreeTypeDef(freeTypeDef);
             }
         else if (freeTypeDef.getClassDef() != null)
             {
             ClassDef classDef = freeTypeDef.getClassDef();
             classDef.removeFreeTypeDef(freeTypeDef);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param genericDef
      * @param hasPredicate
      */
     public void addGenericType(GenericDef genericDef, boolean hasPredicate)
     {
         // TODO - add generic type support
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param genericDef
      */
     public void removeGenericType(GenericDef genericDef)
     {
         checkIllegalArgument(genericDef, "genericDef");
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      *
      */
     public void addSpecificationPredicate()
     {
         specification.setPredicate("New Predicate");
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      *
      */
     public void removeSpecificationPredicate()
     {
         specification.setPredicate(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Add a VisibiliityList to a ClassDef
      *
      * @param classDef The ClassDef to add the VisibilityList
      */
     public void addVisibilityList(ClassDef classDef)
     {
         checkIllegalArgument(classDef, "classDef");
 
         classDef.setVisibilityList("New Visibility List");
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void removeVisibilityList(ClassDef classDef)
     {
         checkIllegalArgument(classDef, "classDef");
 
         classDef.setVisibilityList(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * Add an InheritedClass to a ClassDef
      *
      * @param classDef The ClassDef to add the InheritedClass
      */
     public void addInheritedClass(ClassDef classDef)
     {
         checkIllegalArgument(classDef, "classDef");
 
         InheritedClass inheritedClass = new InheritedClass();
         inheritedClass.setName("New InheritedClass");
         classDef.setInheritedClass(inheritedClass);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param classDef
      */
     public void removeInheritedClass(ClassDef classDef)
     {
         checkIllegalArgument(classDef, "classDef");
 
         classDef.setInheritedClass(null);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * An Operation is added to the given ClassDef at the given index.  If the Operation is null a new Operation is created given
      * the OperationType.  If Operation is not null that Operation is added to the ClassDef, OperationType is ignored.
      *
      * @param classDef      A ClassDef to add the Operation to
      * @param operationType used when operation is null to determine the default type of operation to create, use null
      *                      when passing a non-null value for operation
      */
     public void addOperation(ClassDef classDef, OperationType operationType)
     {
         checkIllegalArgument(classDef, "classDef");
 
         Operation operation = new Operation();
         switch (operationType)
             {
             case All:
                 operation.setName("New Operation");
                 operation.setDeltaList("New Delta");
                 operation.setPredicate("New Predicate");
                 operation.setDeclaration("New Declaration");
                 operation.setName("New Operation");
                 break;
             case NoDeclaration:
                 operation.setName("New Operation");
                 operation.setPredicate("New Predicate");
                 break;
             case NoPredicate:
                 operation.setName("New Operation");
                 operation.setDeltaList("New Delta");
                 operation.setDeclaration("New Declaration");
                 break;
             case Expression:
                 operation.setName("New Operation");
                 operation.setOperationExpression("New Expression");
             }
 
         classDef.addOperation(operation);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     /**
      * @param operation
      */
     public void removeOperation(Operation operation)
     {
         checkIllegalArgument(operation, "operation");
 
         ClassDef classDef = operation.getClassDef();
         classDef.removeOperation(operation);
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     private void resetErrors()
     {
         specification.clearErrors();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void parseSpecification()
     {
         resetErrors();
 
         TozeSpecificationChecker parser = new TozeSpecificationChecker();
         parser.check(specification);
 
         List errors = specification.getErrors();
        errors.addAll(parser.getTypeErrors());
 
         setChanged();
         notifyObservers(errors);
 
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void insertSymbol(String symbol)
     {
         if (currentTextArea != null)
             {
             int p = currentTextArea.getCaretPosition();
             currentTextArea.insert(symbol, p);
             }
     }
 
     /**
      * Update the view that is selected (highlighted) in the specification view.
      * @param newSelectedView The view to be selected and whose specObject will also be selected.
      */
     private void updateSelectedView(ParagraphView newSelectedView)
     {
         if (selectedView != null)
             {
             selectedView.setSelected(false);
             }
 
         selectedView = null;
         selectedObject = null;
 
         if (newSelectedView != null)
             {
             selectedView = newSelectedView;
             selectedView.setSelected(true);
             selectedObject = selectedView.getSpecObject();
             }
 
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     @Override
     public void focusGained(FocusEvent e)
     {
         currentTextArea = (TozeTextArea) e.getSource();
         ParagraphView newSelectedView = (ParagraphView)currentTextArea.getParent();
         updateSelectedView(newSelectedView);
     }
 
     @Override
     public void focusLost(FocusEvent e)
     {
         currentTextArea = (TozeTextArea) e.getSource();
         ParagraphView newSelectedView = (ParagraphView)currentTextArea.getParent();
         updateSelectedView(newSelectedView);
     }
 
     public void moveUp(SpecObject object)
     {
         if (object instanceof AxiomaticDef)
             {
             AxiomaticDef axiomaticDef = (AxiomaticDef) object;
             List<AxiomaticDef> axiomaticDefList = null;
 
             if (axiomaticDef.getClassDef() != null)
                 {
                 axiomaticDefList = axiomaticDef.getClassDef().getAxiomaticDefList();
                 }
             else if (axiomaticDef.getSpecification() != null)
                 {
                 axiomaticDefList = specification.getAxiomaticDefList();
                 }
 
             int index = axiomaticDefList.indexOf(axiomaticDef);
 
             if (index > 0)
                 {
                 Utils.listMove(axiomaticDefList, axiomaticDef, index - 1);
                 }
 
             if (axiomaticDef.getClassDef() != null)
                 {
                 axiomaticDef.getClassDef().setAxiomaticDefList(axiomaticDefList);
                 }
             else if (axiomaticDef.getSpecification() != null)
                 {
                 specification.setAxiomaticDefList(axiomaticDefList);
                 }
             }
         else if (object instanceof AbbreviationDef)
             {
             AbbreviationDef abbreviationDef = (AbbreviationDef) object;
 
             List<AbbreviationDef> abbreviationDefList = null;
 
             if (abbreviationDef.getClassDef() != null)
                 {
                 abbreviationDefList = abbreviationDef.getClassDef().getAbbreviationDefList();
                 }
             else if (abbreviationDef.getSpecification() != null)
                 {
                 abbreviationDefList = specification.getAbbreviationDefList();
                 }
 
             int index = abbreviationDefList.indexOf(abbreviationDef);
 
             if (index > 0)
                 {
                 Utils.listMove(abbreviationDefList, abbreviationDef, index - 1);
                 }
 
             if (abbreviationDef.getClassDef() != null)
                 {
                 abbreviationDef.getClassDef().setAbbreviationDefList(abbreviationDefList);
                 }
             else if (abbreviationDef.getSpecification() != null)
                 {
                 specification.setAbbreviationDefList(abbreviationDefList);
                 }
             }
         else if (object instanceof BasicTypeDef)
             {
             BasicTypeDef basicTypeDef = (BasicTypeDef) object;
 
             List<BasicTypeDef> basicTypeDefList = null;
 
             if (basicTypeDef.getClassDef() != null)
                 {
                 basicTypeDefList = basicTypeDef.getClassDef().getBasicTypeDefList();
                 }
             else if (basicTypeDef.getSpecification() != null)
                 {
                 basicTypeDefList = specification.getBasicTypeDefList();
                 }
 
             int index = basicTypeDefList.indexOf(basicTypeDef);
 
             if (index > 0)
                 {
                 Utils.listMove(basicTypeDefList, basicTypeDef, index - 1);
                 }
 
             if (basicTypeDef.getClassDef() != null)
                 {
                 basicTypeDef.getClassDef().setBasicTypeDefList(basicTypeDefList);
                 }
             else if (basicTypeDef.getSpecification() != null)
                 {
                 specification.setBasicTypeDefList(basicTypeDefList);
                 }
             }
         else if (object instanceof FreeTypeDef)
             {
             FreeTypeDef freeTypeDef = (FreeTypeDef) object;
 
             List<FreeTypeDef> freeTypeDefList = null;
 
             if (freeTypeDef.getClassDef() != null)
                 {
                 freeTypeDefList = freeTypeDef.getClassDef().getFreeTypeDefList();
                 }
             else if (freeTypeDef.getSpecification() != null)
                 {
                 freeTypeDefList = specification.getFreeTypeDefList();
                 }
 
             int index = freeTypeDefList.indexOf(freeTypeDef);
 
             if (index > 0)
                 {
                 Utils.listMove(freeTypeDefList, freeTypeDef, index - 1);
                 }
 
             if (freeTypeDef.getClassDef() != null)
                 {
                 freeTypeDef.getClassDef().setFreeTypeDefList(freeTypeDefList);
                 }
             else if (freeTypeDef.getSpecification() != null)
                 {
                 specification.setFreeTypeDefList(freeTypeDefList);
                 }
             }
         else if (object instanceof ClassDef)
             {
             ClassDef classDef = (ClassDef) object;
 
             List<ClassDef> classDefList = specification.getClassDefList();
 
             int index = classDefList.indexOf(object);
 
             if (index > 0)
                 {
                 Utils.listMove(classDefList, classDef, index - 1);
                 }
 
             specification.setClassDefList(classDefList);
             }
         else if (object instanceof Operation)
             {
             Operation operation = (Operation) object;
 
             List<Operation> operationList = operation.getClassDef().getOperationList();
 
             int index = operationList.indexOf(operation);
 
             if (index > 0)
                 {
                 Utils.listMove(operationList, operation, index - 1);
                 }
 
             operation.getClassDef().setOperationList(operationList);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void moveDown(SpecObject object)
     {
         if (object instanceof AxiomaticDef)
             {
             AxiomaticDef axiomaticDef = (AxiomaticDef) object;
 
             List<AxiomaticDef> axiomaticDefList = null;
 
             if (axiomaticDef.getClassDef() != null)
                 {
                 axiomaticDefList = axiomaticDef.getClassDef().getAxiomaticDefList();
                 }
             else if (axiomaticDef.getSpecification() != null)
                 {
                 axiomaticDefList = specification.getAxiomaticDefList();
                 }
 
             int size = axiomaticDefList.size();
             int index = axiomaticDefList.indexOf(axiomaticDef);
             int last = size - 1;
 
             if (index < last)
                 {
                 Utils.listMove(axiomaticDefList, axiomaticDef, index + 1);
                 }
 
             if (axiomaticDef.getClassDef() != null)
                 {
                 axiomaticDef.getClassDef().setAxiomaticDefList(axiomaticDefList);
                 }
             else if (axiomaticDef.getSpecification() != null)
                 {
                 specification.setAxiomaticDefList(axiomaticDefList);
                 }
             }
         else if (object instanceof AbbreviationDef)
             {
             AbbreviationDef abbreviationDef = (AbbreviationDef) object;
 
             List<AbbreviationDef> abbreviationDefList = null;
 
             if (abbreviationDef.getClassDef() != null)
                 {
                 abbreviationDefList = abbreviationDef.getClassDef().getAbbreviationDefList();
                 }
             else if (abbreviationDef.getSpecification() != null)
                 {
                 abbreviationDefList = specification.getAbbreviationDefList();
                 }
 
             int size = abbreviationDefList.size();
             int index = abbreviationDefList.indexOf(abbreviationDef);
             int last = size - 1;
 
             if (index < last)
                 {
                 Utils.listMove(abbreviationDefList, abbreviationDef, index + 1);
                 }
 
             if (abbreviationDef.getClassDef() != null)
                 {
                 abbreviationDef.getClassDef().setAbbreviationDefList(abbreviationDefList);
                 }
             else if (abbreviationDef.getSpecification() != null)
                 {
                 specification.setAbbreviationDefList(abbreviationDefList);
                 }
             }
         else if (object instanceof BasicTypeDef)
             {
             BasicTypeDef basicTypeDef = (BasicTypeDef) object;
 
             List<BasicTypeDef> basicTypeDefList = null;
 
             if (basicTypeDef.getClassDef() != null)
                 {
                 basicTypeDefList = basicTypeDef.getClassDef().getBasicTypeDefList();
                 }
             else if (basicTypeDef.getSpecification() != null)
                 {
                 basicTypeDefList = specification.getBasicTypeDefList();
                 }
 
             int size = basicTypeDefList.size();
             int index = basicTypeDefList.indexOf(basicTypeDef);
             int last = size - 1;
 
             if (index < last)
                 {
                 Utils.listMove(basicTypeDefList, basicTypeDef, index + 1);
                 }
 
             if (basicTypeDef.getClassDef() != null)
                 {
                 basicTypeDef.getClassDef().setBasicTypeDefList(basicTypeDefList);
                 }
             else if (basicTypeDef.getSpecification() != null)
                 {
                 specification.setBasicTypeDefList(basicTypeDefList);
                 }
             }
         else if (object instanceof FreeTypeDef)
             {
             FreeTypeDef freeTypeDef = (FreeTypeDef) object;
 
             List<FreeTypeDef> freeTypeDefList = null;
 
             if (freeTypeDef.getClassDef() != null)
                 {
                 freeTypeDefList = freeTypeDef.getClassDef().getFreeTypeDefList();
                 }
             else if (freeTypeDef.getSpecification() != null)
                 {
                 freeTypeDefList = specification.getFreeTypeDefList();
                 }
 
             int size = freeTypeDefList.size();
             int index = freeTypeDefList.indexOf(freeTypeDef);
             int last = size - 1;
 
             if (index < last)
                 {
                 Utils.listMove(freeTypeDefList, freeTypeDef, index + 1);
                 }
 
             if (freeTypeDef.getClassDef() != null)
                 {
                 freeTypeDef.getClassDef().setFreeTypeDefList(freeTypeDefList);
                 }
             else if (freeTypeDef.getSpecification() != null)
                 {
                 specification.setFreeTypeDefList(freeTypeDefList);
                 }
             }
         else if (object instanceof ClassDef)
             {
             ClassDef classDef = (ClassDef) object;
 
             int size = specification.getClassDefList().size();
             int index = specification.getClassDefList().indexOf(object);
 
             List<ClassDef> classDefList = specification.getClassDefList();
 
             int last = size - 1;
 
             // only move down if not already at bottom
             if (index < last)
                 {
                 Utils.listMove(classDefList, classDef, index + 1);
                 }
 
             specification.setClassDefList(classDefList);
             }
         else if (object instanceof Operation)
             {
             Operation operation = (Operation) object;
             List<Operation> operationList = operation.getClassDef().getOperationList();
             int size = operationList.size();
             int index = operationList.indexOf(operation);
             int last = size - 1;
 
             // only move down if not already at bottom
             if (index < last)
                 {
                 Utils.listMove(operationList, operation, index + 1);
                 }
 
             operation.getClassDef().setOperationList(operationList);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void cut()
     {
         // put selected objects into the global cache
         // remove the selected object from the view
         cachedObject = selectedObject;
 
         if (selectedObject instanceof AbbreviationDef)
             {
             removeAbbreviation((AbbreviationDef) selectedObject);
             }
         else if (selectedObject instanceof AxiomaticDef)
             {
             removeAxiomaticType((AxiomaticDef) selectedObject);
             }
         else if (selectedObject instanceof BasicTypeDef)
             {
             removeBasicType((BasicTypeDef) selectedObject);
             }
         else if (selectedObject instanceof ClassDef)
             {
             ClassDef classDef = (ClassDef) selectedObject;
 
             if (selectedView instanceof ClassView)
                 {
                 removeClass(classDef);
                 }
             else if (selectedView instanceof VisibilityListView)
                 {
                 removeVisibilityList(classDef);
                 }
             }
         else if (selectedObject instanceof FreeTypeDef)
             {
             removeFreeType((FreeTypeDef) selectedObject);
             }
         else if (selectedObject instanceof GenericDef)
             {
             removeGenericType((GenericDef) selectedObject);
             }
         else if (selectedObject instanceof InheritedClass)
             {
             removeInheritedClass(((InheritedClass) selectedObject).getClassDef());
             }
         else if (selectedObject instanceof InitialState)
             {
             ClassDef classDef = ((InitialState) selectedObject).getClassDef();
             removeInitialState(classDef);
             }
         else if (selectedObject instanceof Operation)
             {
             Operation operation = (Operation) selectedObject;
 
             if (selectedView instanceof OperationView)
                 {
                 removeOperation(operation);
                 }
             else if (selectedView instanceof DeltaListView)
                 {
                 removeDeltaList(operation);
                 }
             }
         else if (selectedObject instanceof State)
             {
             ClassDef classDef = ((State) selectedObject).getClassDef();
             removeState(classDef);
             }
 
         selectedObject = null;
         selectedView = null;
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     public void copy()
     {
         // put selected objects into the global cache
         cachedObject = selectedObject;
 
         try
             {
             cachedObject = (SpecObject) selectedObject.clone();
             }
         catch (CloneNotSupportedException e)
             {
             }
     }
 
     public void paste()
     {
         // TODO: need to check where the paste is happening, fly a dialog
         // if additional user action is required (no parent object, etc.)
         SpecObject selectedObject = specification;
 
         if (selectedView != null)
             {
             selectedObject = selectedView.getSpecObject();
             }
 
         if (cachedObject instanceof AbbreviationDef)
             {
 
             AbbreviationDef abbreviationDef = (AbbreviationDef) cachedObject;
 
             if (selectedObject == specification)
                 {
                 abbreviationDef.setSpecification(specification);
                 specification.addAbbreviationDef((AbbreviationDef) cachedObject);
                 }
             else
                 {
                 ClassDef classDef = (ClassDef) selectedObject;
                 abbreviationDef.setClassDef(classDef);
                 classDef.addAbbreviationDef(abbreviationDef);
                 }
             }
         else if (cachedObject instanceof AxiomaticDef)
             {
             AxiomaticDef axiomaticDef = (AxiomaticDef) cachedObject;
 
             if (selectedObject == specification)
                 {
                 axiomaticDef.setSpecification(specification);
                 specification.addAxiomaticDef(axiomaticDef);
                 }
             else
                 {
                 ClassDef classDef = (ClassDef) selectedObject;
                 axiomaticDef.setClassDef(classDef);
                 classDef.addAxiomaticDef(axiomaticDef);
                 }
             }
         else if (cachedObject instanceof BasicTypeDef)
             {
             BasicTypeDef basicTypeDef = (BasicTypeDef) cachedObject;
 
             if (selectedObject == specification)
                 {
                 basicTypeDef.setSpecification(specification);
                 specification.addBasicTypeDef(basicTypeDef);
                 }
             else
                 {
                 ClassDef classDef = (ClassDef) selectedObject;
                 basicTypeDef.setClassDef(classDef);
                 classDef.addBasicTypeDef(basicTypeDef);
                 }
             }
         else if (cachedObject instanceof ClassDef)
             {
             specification.addClassDef((ClassDef) cachedObject);
             }
         else if (cachedObject instanceof FreeTypeDef)
             {
             FreeTypeDef freeTypeDef = (FreeTypeDef) cachedObject;
 
             if (selectedObject == specification)
                 {
                 freeTypeDef.setSpecification(specification);
                 specification.addFreeTypeDef(freeTypeDef);
                 }
             else
                 {
                 ClassDef classDef = (ClassDef) selectedObject;
                 freeTypeDef.setClassDef(classDef);
                 classDef.addFreeTypeDef(freeTypeDef);
                 }
             }
         else if (cachedObject instanceof GenericDef)
             {
             GenericDef genericDef = (GenericDef) cachedObject;
 
             if (selectedObject == specification)
                 {
                 genericDef.setSpecification(specification);
                 specification.addGenericDef(genericDef);
                 }
             }
         else if (cachedObject instanceof InheritedClass)
             {
             ((ClassDef) selectedObject).setInheritedClass((InheritedClass) cachedObject);
             }
         else if (cachedObject instanceof InitialState)
             {
             ((ClassDef) selectedObject).setInitialState((InitialState) cachedObject);
             }
         else if (cachedObject instanceof Operation)
             {
             ((ClassDef) selectedObject).addOperation((Operation) cachedObject);
             }
         else if (cachedObject instanceof State)
             {
             ((ClassDef) selectedObject).setState((State) cachedObject);
             }
 
         specificationView.requestRebuild();
         specificationView.revalidate();
         specificationView.repaint();
     }
 
     private void checkIllegalArgument(Object object, String description)
     {
         if (object == null)
             {
             throw new IllegalArgumentException(description + " is null");
             }
     }
 
     public void selectError(TozeToken errorToken)
     {
         SpecObject objectWithError = specification.findObjectWithError(errorToken);
 
         TozeTextArea textArea = null;
 
         if (objectWithError != null)
             {
             textArea = specificationView.findTextAreaForError(objectWithError, errorToken);
             }
 
         if (textArea != null)
             {
             // scroll the top of the selected text area
             textArea.scrollRectToVisible(new Rectangle(textArea.getSize()));
             ((JComponent)textArea.getParent()).scrollRectToVisible(textArea.getBounds());
             }
     }
 
     public void selectParagraph(SpecObject specObject)
     {
         ParagraphView viewToBeSelected = null;
 
         if (specObject != specification)
             {
             viewToBeSelected = specificationView.findParagraphViewForSpecObject(specObject);
             }
 
         updateSelectedView(viewToBeSelected);
 
         if (viewToBeSelected != null)
             {
             // scroll the top of the selected view
             viewToBeSelected.scrollRectToVisible(new Rectangle(viewToBeSelected.getSize()));
             ((JComponent)viewToBeSelected.getParent()).scrollRectToVisible(viewToBeSelected.getBounds());
             }
     }
 
     /**
      * Get notifications from typing in text fields, from the
      * SpecDocumentListener
      *
      * @param o
      * @param arg
      */
     @Override
     public void update(Observable o, Object arg)
     {
         try
             {
             parseSpecification();
             }
         catch (Throwable t)
             {
             System.out.println("Caught exception while parsing: " + t);
             t.printStackTrace();
             }
     }
 
 
     // Get Mouse events
     private class ControllerMouseAdapter extends MouseAdapter
     {
         @Override
         public void mouseClicked(MouseEvent e)
         {
             if (MouseEvent.BUTTON3 == e.getButton())
                 {
                 // right click
                 Component clickedComponent = e.getComponent();
 
                 if (clickedComponent instanceof ParagraphView)
                     {
                     SpecObject specObject = ((ParagraphView) clickedComponent).getSpecObject();
 
                     JPopupMenu popupMenu = PopUpMenuBuilder.buildPopup(specObject, SpecificationController.this);
                     popupMenu.show(clickedComponent, e.getX(), e.getY());
                     }
                 else if (clickedComponent instanceof SpecificationView)
                     {
                     JPopupMenu popupMenu = PopUpMenuBuilder.buildPopup(specification, SpecificationController.this);
                     popupMenu.show(clickedComponent, e.getX(), e.getY());
                     }
                 }
 
             else if (MouseEvent.BUTTON1 == e.getButton())
                 {
                 Component clickedComponent = e.getComponent();
 
                 if (clickedComponent instanceof ParagraphView)
                     {
                     ParagraphView newSelectedView = (ParagraphView) clickedComponent;
                     updateSelectedView(newSelectedView);
                     }
                 }
             super.mouseClicked(e);
 
             specificationView.revalidate();
             specificationView.repaint();
         }
     }
 }
