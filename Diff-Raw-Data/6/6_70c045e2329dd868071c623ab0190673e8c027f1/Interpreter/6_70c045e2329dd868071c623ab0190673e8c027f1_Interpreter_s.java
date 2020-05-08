 package pl.edu.pjwstk.interpreter.envs;
 
 import edu.pjwstk.jps.ast.IExpression;
 import edu.pjwstk.jps.ast.auxname.IAsExpression;
 import edu.pjwstk.jps.ast.auxname.IGroupAsExpression;
 import edu.pjwstk.jps.ast.binary.*;
 import edu.pjwstk.jps.ast.terminal.*;
 import edu.pjwstk.jps.ast.unary.*;
 import edu.pjwstk.jps.datastore.*;
 import edu.pjwstk.jps.interpreter.envs.IInterpreter;
 import edu.pjwstk.jps.result.*;
 import pl.edu.pjwstk.interpreter.exception.WrongTypeException;
 import pl.edu.pjwstk.interpreter.qres.*;
 
 import java.util.*;
 
 public class Interpreter implements IInterpreter {
 
     private QresStack stack;
     private ISBAStore store;
     private ENVS envs;
 
     public Interpreter(QresStack stack, ISBAStore store, ENVS envs) {
         this.stack = stack;
         this.store = store;
         this.envs = envs;
         this.envs.init(store.getEntryOID(), store);
     }
 
     @Override
     public IAbstractQueryResult eval(IExpression queryTreeRoot) {
         return null;
     }
 
     @Override
     public void visitAsExpression(IAsExpression expr) {
         expr.getInnerExpression().accept(this);
         IAbstractQueryResult result = stack.pop();
 
         if (result instanceof ISingleResult) {
             stack.push(new BinderResult(expr.getAuxiliaryName(), result));
         }
 
         if (result instanceof IBagResult) {
             List<ISingleResult> source = new ArrayList<ISingleResult>(((IBagResult) result).getElements());
             List<ISingleResult> resultList = new ArrayList<ISingleResult>();
             for (ISingleResult singleResult : source) {
                 resultList.add(new BinderResult(expr.getAuxiliaryName(), singleResult));
             }
             stack.push(new BagResult(resultList));
         }
 
         if (result instanceof ISequenceResult) {
             List<ISingleResult> source = ((ISequenceResult) result).getElements();
             List<ISingleResult> resultList = new ArrayList<ISingleResult>();
             for (ISingleResult singleResult : source) {
                 resultList.add(new BinderResult(expr.getAuxiliaryName(), singleResult));
             }
             stack.push(new SequenceResult(resultList));
         }
     }
 
     @Override
     public void visitGroupAsExpression(IGroupAsExpression expr) {
         expr.getInnerExpression().accept(this);
         IAbstractQueryResult result = stack.pop();
         stack.push(new BinderResult(expr.getAuxiliaryName(), result));
     }
 
     @Override
     public void visitAllExpression(IForAllExpression expr) {
         expr.getLeftExpression().accept(this);
         IAbstractQueryResult left = stack.pop();
 
         for (ISingleResult result : getResultListResolveStruct(left)) {
             this.envs.push(envs.nested(result, this.store));
             expr.getRightExpression().accept(this);
             IAbstractQueryResult right = stack.pop();
             try {
                 right = getResult(right);
             } catch (RuntimeException e) {
                 throw new WrongTypeException("Unable to retrieve a single value");
             }
             right = doDereference(right);
 
             if (!(right instanceof IBooleanResult)) {
                 throw new WrongTypeException("Only Boolean is allowed");
             }
 
             if (!((IBooleanResult) right).getValue()) {
                 stack.push(new BooleanResult(false));
                 envs.pop();
                 return;
             }
         }
 
         stack.push(new BooleanResult(true));
         envs.pop();
     }
 
     @Override
     public void visitAndExpression(IAndExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         if (!(left instanceof IBooleanResult && right instanceof IBooleanResult)) {
             //throw new WrongTypeException("Only Boolean is allowed");
             stack.push(new BooleanResult(false));
         }
 
         stack.push(new BooleanResult(((IBooleanResult) left).getValue() && ((IBooleanResult) right).getValue()));
     }
 
     @Override
     public void visitAnyExpression(IForAnyExpression expr) {
         expr.getLeftExpression().accept(this);
         IAbstractQueryResult left = stack.pop();
 
         for (ISingleResult result : getResultListResolveStruct(left)) {
             this.envs.push(envs.nested(result, this.store));
             expr.getRightExpression().accept(this);
             IAbstractQueryResult right = stack.pop();
             try {
                 right = getResult(right);
             } catch (RuntimeException e) {
                 throw new WrongTypeException("Unable to retrieve a single value");
             }
             right = doDereference(right);
 
             if (!(right instanceof IBooleanResult)) {
                 throw new WrongTypeException("Only Boolean is allowed");
             }
 
             if (((IBooleanResult) right).getValue()) {
                 stack.push(new BooleanResult(true));
                 envs.pop();
                 return;
             }
         }
 
         stack.push(new BooleanResult(false));
         envs.pop();
     }
 
     @Override
     public void visitCloseByExpression(ICloseByExpression expr) {
         BagResult eres = new BagResult();
 
         expr.getLeftExpression().accept(this);
         IAbstractQueryResult left = stack.pop();
 
         for (ISingleResult el1 : getResultListResolveStruct(left)) {
             this.envs.push(envs.nested(el1, this.store));
             expr.getRightExpression().accept(this);
             IAbstractQueryResult right = stack.pop();
             for (ISingleResult el2 : getResultListResolveStruct(right)) {
                 StructResult struct = new StructResult();
                 struct.elements().add(el1);
                 if (el2 instanceof IStructResult) {
                     struct.elements().addAll(((IStructResult) el2).elements());
                 } else {
                     struct.elements().add(el2);
                 }
                 eres.getElements().add(struct);
             }
             this.envs.pop();
         }
 
         stack.push(eres);
     }
 
     @Override
     public void visitCommaExpression(ICommaExpression expr) {
         BagResult eres = new BagResult();
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult e2Res = stack.pop();
         IAbstractQueryResult e1Res = stack.pop();
 
         for (ISingleResult e1 : getResultListNotResolveStruct(e1Res)) {
             for (ISingleResult e2 : getResultListNotResolveStruct(e2Res)) {
                 List<ISingleResult> struct = new ArrayList<ISingleResult>();
                 if (e1 instanceof IStructResult) {
                     struct.addAll(((IStructResult) e1).elements());
                 } else {
                     struct.add(e1);
                 }
                 if (e2 instanceof IStructResult) {
                     struct.addAll(((IStructResult) e2).elements());
                 } else {
                     struct.add(e2);
                 }
                 eres.getElements().add(new StructResult(struct));
             }
         }
 
         stack.push(eres);
 
     }
 
     @Override
     public void visitDivideExpression(IDivideExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         boolean isDoubleInstance = false;
         Double result = 0.0;
         if (left instanceof IIntegerResult) {
             result += ((IIntegerResult) left).getValue();
         } else if (left instanceof IDoubleResult) {
             result += ((IDoubleResult) left).getValue();
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + left.getClass() + " was used");
         }
 
         if (right instanceof IIntegerResult) {
             int value = ((IIntegerResult) right).getValue();
             if (0 == value) {
                 throw new ArithmeticException("Division by zero");
             }
             result /= value;
         } else if (right instanceof IDoubleResult) {
             Double value = ((IDoubleResult) right).getValue();
             if (0 == value) {
                 throw new ArithmeticException("Division by zero");
             }
             result /= value;
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + right.getClass() + " was used");
         }
 
         if (isDoubleInstance) {
             stack.push(new DoubleResult(result));
         } else {
             stack.push(new IntegerResult(result.intValue()));
         }
     }
 
     @Override
     public void visitDotExpression(IDotExpression expr) {
         BagResult bag = new BagResult();
         expr.getLeftExpression().accept(this);
         IAbstractQueryResult leftResult = stack.pop();
 
         for (ISingleResult result : getResultListResolveStruct(leftResult)) {
             this.envs.push(envs.nested(result, this.store));
             expr.getRightExpression().accept(this);
             IAbstractQueryResult rightResult = stack.pop();
             bag.getElements().add(getResult(rightResult));
             envs.pop();
         }
 
         stack.push(bag);
     }
 
     @Override
     public void visitEqualsExpression(IEqualsExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         stack.push(new BooleanResult(left.equals(right)));
     }
 
     @Override
     public void visitGreaterThanExpression(IGreaterThanExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
    /*     if (right.getClass() != left.getClass()) {
             throw new RuntimeException("Cannot compare two different instances");
         }
 */
         if (left instanceof IStringResult && right instanceof IStringResult) {
             int result = ((IStringResult) left).getValue().compareTo(((IStringResult) right).getValue());
 
             if (result > 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
 
         if (left instanceof IIntegerResult && right instanceof IIntegerResult) {
             int result = ((IIntegerResult) left).getValue().compareTo(((IIntegerResult) right).getValue());
 
             if (result > 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
 
         if ((left instanceof IDoubleResult  || left instanceof IIntegerResult) && (right instanceof IIntegerResult  || right instanceof IDoubleResult)) {
             int result = new Double(((Number)((ISimpleResult) left).getValue()).doubleValue()).compareTo(new Double(((Number)((ISimpleResult) right).getValue()).doubleValue()));
             if (result > 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
         stack.push(new BooleanResult(false));
     }
 
     @Override
     public void visitGreaterOrEqualThanExpression(IGreaterOrEqualThanExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
        /* if (right.getClass() != left.getClass()) {
             throw new RuntimeException("Cannot compare two different instances");
         }*/
 
         if (left instanceof IStringResult && right instanceof IStringResult) {
             int result = ((IStringResult) left).getValue().compareTo(((IStringResult) right).getValue());
 
             if (result >= 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
 
         if (left instanceof IIntegerResult && right instanceof IIntegerResult) {
             int result = ((IIntegerResult) left).getValue().compareTo(((IIntegerResult) right).getValue());
 
             if (result >= 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
 
         if ((left instanceof IDoubleResult  || left instanceof IIntegerResult) && (right instanceof IIntegerResult  || right instanceof IDoubleResult)) {
             int result = new Double(((Number)((ISimpleResult) left).getValue()).doubleValue()).compareTo(new Double(((Number)((ISimpleResult) right).getValue()).doubleValue()));
 
             if (result >= 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
 
         stack.push(new BooleanResult(false));
     }
 
     @Override
     public void visitInExpression(IInExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
         List<ISingleResult> rightList = getResultListResolveStruct(right);
         List<ISingleResult> leftList = getResultListResolveStruct(left);
         if (rightList.containsAll(leftList)) {
             stack.push(new BooleanResult(true));
         } else {
             stack.push(new BooleanResult(false));
         }
     }
 
     @Override
     public void visitIntersectExpression(IIntersectExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
        right = doDereference(right);
        left = doDereference(left);
         List<ISingleResult> rightList = getResultListResolveStruct(right);
         List<ISingleResult> leftList = getResultListResolveStruct(left);
 
         leftList.retainAll(rightList);
 
         stack.push(new BagResult(leftList));
     }
 
     @Override
     public void visitJoinExpression(IJoinExpression expr) {
         BagResult eres = new BagResult();
 
         expr.getLeftExpression().accept(this);
         IAbstractQueryResult left = stack.pop();
 
         // dwa rozwiniecia
         for (ISingleResult el1 : getResultListResolveStruct(left)) {
             this.envs.push(envs.nested(el1, this.store));
             expr.getRightExpression().accept(this);
             IAbstractQueryResult right = stack.pop();
             for (ISingleResult el2 : getResultListResolveStruct(right)) {
                 StructResult struct = new StructResult();
                 struct.elements().add(el1);
                 if (el2 instanceof IStructResult) {
                     struct.elements().addAll(((IStructResult) el2).elements());
                 } else {
                     struct.elements().add(el2);
                 }
                 eres.getElements().add(struct);
             }
             this.envs.pop();
         }
 
         stack.push(eres);
     }
 
     @Override
     public void visitLessOrEqualThanExpression(ILessOrEqualThanExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
     /*    if (right.getClass() != left.getClass()) {
             //throw new RuntimeException("Cannot compare two different instances");
             stack.push(new BooleanResult(false));
             return;
         }*/
 
         if (left instanceof IStringResult && right instanceof IStringResult) {
             int result = ((IStringResult) left).getValue().compareTo(((IStringResult) right).getValue());
 
             if (result <= 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
 
         if (left instanceof IIntegerResult && right instanceof IIntegerResult) {
             int result = ((IIntegerResult) left).getValue().compareTo(((IIntegerResult) right).getValue());
 
             if (result <= 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
 
         if ((left instanceof IDoubleResult  || left instanceof IIntegerResult) && (right instanceof IIntegerResult  || right instanceof IDoubleResult)) {
             int result = new Double(((Number)((ISimpleResult) left).getValue()).doubleValue()).compareTo(new Double(((Number)((ISimpleResult) right).getValue()).doubleValue()));
 
             if (result <= 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
         stack.push(new BooleanResult(false));
     }
 
     @Override
     public void visitLessThanExpression(ILessThanExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 /*
         if (right.getClass() != left.getClass()) {
             //throw new RuntimeException("Cannot compare two different instances");
             stack.push(new BooleanResult(false));
             return;
         }*/
 
         if (left instanceof IStringResult && right instanceof IStringResult) {
             int result = ((IStringResult) left).getValue().compareTo(((IStringResult) right).getValue());
 
             if (result < 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
 
         if (left instanceof IIntegerResult && right instanceof IIntegerResult) {
             int result = ((IIntegerResult) left).getValue().compareTo(((IIntegerResult) right).getValue());
 
             if (result < 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
 
         if ((left instanceof IDoubleResult  || left instanceof IIntegerResult) && (right instanceof IIntegerResult  || right instanceof IDoubleResult)) {
             int result = new Double(((Number)((ISimpleResult) left).getValue()).doubleValue()).compareTo(new Double(((Number)((ISimpleResult) right).getValue()).doubleValue()));
 
             if (result < 0) {
                 stack.push(new BooleanResult(true));
             } else {
                 stack.push(new BooleanResult(false));
             }
             return;
         }
         stack.push(new BooleanResult(false));
     }
 
     @Override
     public void visitMinusExpression(IMinusExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         boolean isDoubleInstance = false;
         Double result = 0.0;
         if (left instanceof IIntegerResult) {
             result += ((IIntegerResult) left).getValue();
         } else if (left instanceof IDoubleResult) {
             result += ((IDoubleResult) left).getValue();
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + left.getClass() + " was used");
         }
 
         if (right instanceof IIntegerResult) {
             result -= ((IIntegerResult) right).getValue();
         } else if (right instanceof IDoubleResult) {
             result -= ((IDoubleResult) right).getValue();
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + right.getClass() + " was used");
         }
 
         if (isDoubleInstance) {
             stack.push(new DoubleResult(result));
         } else {
             stack.push(new IntegerResult(result.intValue()));
         }
     }
 
     @Override
     public void visitMinusSetExpression(IMinusSetExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
        right = doDereference(right);
        left = doDereference(left);
         List<ISingleResult> rightList = getResultListResolveStruct(right);
         List<ISingleResult> leftList = getResultListResolveStruct(left);
 
         leftList.removeAll(rightList);
 
         stack.push(new BagResult(leftList));
     }
 
     @Override
     public void visitModuloExpression(IModuloExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         boolean isDoubleInstance = false;
         Double result = 0.0;
         if (left instanceof IIntegerResult) {
             result += ((IIntegerResult) left).getValue();
         } else if (left instanceof IDoubleResult) {
             result += ((IDoubleResult) left).getValue();
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + left.getClass() + " was used");
         }
 
         if (right instanceof IIntegerResult) {
             result %= ((IIntegerResult) right).getValue();
         } else if (right instanceof IDoubleResult) {
             result %= ((IDoubleResult) right).getValue();
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + right.getClass() + " was used");
         }
 
         if (isDoubleInstance) {
             stack.push(new DoubleResult(result));
         } else {
             stack.push(new IntegerResult(result.intValue()));
         }
     }
 
     @Override
     public void visitMultiplyExpression(IMultiplyExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         boolean isDoubleInstance = false;
         Double result = 0.0;
         if (left instanceof IIntegerResult) {
             result += ((IIntegerResult) left).getValue();
         } else if (left instanceof IDoubleResult) {
             result += ((IDoubleResult) left).getValue();
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + left.getClass() + " was used");
         }
 
         if (right instanceof IIntegerResult) {
             result *= ((IIntegerResult) right).getValue();
         } else if (right instanceof IDoubleResult) {
             result *= ((IDoubleResult) right).getValue();
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + right.getClass() + " was used");
         }
 
         if (isDoubleInstance) {
             stack.push(new DoubleResult(result));
         } else {
             stack.push(new IntegerResult(result.intValue()));
         }
     }
 
     @Override
     public void visitNotEqualsExpression(INotEqualsExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         stack.push(new BooleanResult(!left.equals(right)));
     }
 
     @Override
     public void visitOrderByExpression(IOrderByExpression expr) {
         SequenceResult eres = new SequenceResult();
 
         expr.getLeftExpression().accept(this);
         IAbstractQueryResult left = stack.pop();
 
         for (ISingleResult el1 : getResultListResolveStruct(left)) {
             this.envs.push(envs.nested(el1, this.store));
             expr.getRightExpression().accept(this);
             IAbstractQueryResult right = stack.pop();
             for (ISingleResult el2 : getResultListResolveStruct(right)) {
                 StructResult struct = new StructResult();
                 struct.elements().add(el1);
                 if (el2 instanceof IStructResult) {
                     struct.elements().addAll(((IStructResult) el2).elements());
                 } else {
                     struct.elements().add(el2);
                 }
                 eres.getElements().add(struct);
             }
             this.envs.pop();
         }
 
         stack.push(eres);
 
     }
 
     @Override
     public void visitOrExpression(IOrExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         if (!(left instanceof IBooleanResult && right instanceof IBooleanResult)) {
             // throw new WrongTypeException("Only Boolean is allowed");
             stack.push(new BooleanResult(false));
             return;
         }
 
         stack.push(new BooleanResult(((IBooleanResult) left).getValue() || ((IBooleanResult) right).getValue()));
     }
 
     @Override
     public void visitPlusExpression(IPlusExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         if (left instanceof IStringResult || right instanceof IStringResult) {
             stack.push(new StringResult(getString((ISingleResult) left) + getString((ISingleResult) right)));
 
             return;
         }
 
         boolean isDoubleInstance = false;
         Double result = 0.0;
         if (left instanceof IIntegerResult) {
             result += ((IIntegerResult) left).getValue();
         } else if (left instanceof IDoubleResult) {
             result += ((IDoubleResult) left).getValue();
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + left.getClass() + " was used");
         }
 
         if (right instanceof IIntegerResult) {
             result += ((IIntegerResult) right).getValue();
         } else if (right instanceof IDoubleResult) {
             result += ((IDoubleResult) right).getValue();
             isDoubleInstance = true;
         } else {
             throw new WrongTypeException("Only Integer or Double are allowed. " + right.getClass() + " was used");
         }
 
         if (isDoubleInstance) {
             stack.push(new DoubleResult(result));
         } else {
             stack.push(new IntegerResult(result.intValue()));
         }
     }
 
     @Override
     public void visitUnionExpression(IUnionExpression expr) {
 
         BagResult eres = new BagResult();
 
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         eres.getElements().addAll(getResultListNotResolveStruct(left));
         eres.getElements().addAll(getResultListNotResolveStruct(right));
         stack.push(eres);
     }
 
     @Override
     public void visitWhereExpression(IWhereExpression expr) {
         BagResult eres = new BagResult();
         expr.getLeftExpression().accept(this);
         IAbstractQueryResult left = stack.pop();
 
         for (ISingleResult result : getResultListResolveStruct(left)) {
             this.envs.push(envs.nested(result, this.store));
             expr.getRightExpression().accept(this);
             IAbstractQueryResult right = stack.pop();
 
             try {
                 right = getResult(right);
             } catch (RuntimeException e) {
                 throw new WrongTypeException("Unable to retrieve a single value");
             }
 
             right = doDereference(right);
 
             if (!(right instanceof IBooleanResult)) {
                 throw new WrongTypeException("Only Boolean is allowed");
             }
 
             if (((IBooleanResult) right).getValue()) {
                 eres.getElements().add(result);
             }
 
             envs.pop();
         }
         stack.push(eres);
     }
 
     @Override
     public void visitXORExpression(IXORExpression expr) {
         expr.getLeftExpression().accept(this);
         expr.getRightExpression().accept(this);
 
         IAbstractQueryResult right = stack.pop();
         IAbstractQueryResult left = stack.pop();
 
         try {
             right = getResult(right);
             left = getResult(left);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         left = doDereference(left);
         right = doDereference(right);
 
         if (!(left instanceof IBooleanResult && right instanceof IBooleanResult)) {
             throw new WrongTypeException("Only Boolean is allowed");
         }
 
         if (((IBooleanResult) right).getValue() && !((IBooleanResult) left).getValue()) {
             stack.push(new BooleanResult(true));
             return;
         }
 
         if (!((IBooleanResult) right).getValue() && ((IBooleanResult) left).getValue()) {
             stack.push(new BooleanResult(true));
             return;
         }
 
         stack.push(new BooleanResult(false));
     }
 
     @Override
     public void visitBooleanTerminal(IBooleanTerminal expr) {
         stack.push(new BooleanResult(expr.getValue()));
     }
 
     @Override
     public void visitDoubleTerminal(IDoubleTerminal expr) {
         stack.push(new DoubleResult(expr.getValue()));
     }
 
     @Override
     public void visitIntegerTerminal(IIntegerTerminal expr) {
         stack.push(new IntegerResult(expr.getValue()));
     }
 
     @Override
     public void visitNameTerminal(INameTerminal expr) {
         //stack.push(new BinderResult(expr.getName(), this.envs.bind(expr.getName())));
         stack.push(this.envs.bind(expr.getName()));
     }
 
     @Override
     public void visitStringTerminal(IStringTerminal expr) {
         stack.push(new StringResult(expr.getValue()));
     }
 
     @Override
     public void visitBagExpression(IBagExpression expr) {
 
         BagResult eres = new BagResult();
         expr.getInnerExpression().accept(this);
 
         IAbstractQueryResult result = stack.pop();
 
         eres.getElements().addAll(getResultListResolveStruct(result));
 
         stack.push(eres);
     }
 
     @Override
     public void visitCountExpression(ICountExpression expr) {
         expr.getInnerExpression().accept(this);
 
         IAbstractQueryResult result = stack.pop();
         List<ISingleResult> eres = getResultListNotResolveStruct(result);
 
         stack.push(new IntegerResult(eres.size()));
     }
 
     @Override
     public void visitExistsExpression(IExistsExpression expr) {
         expr.getInnerExpression().accept(this);
 
         IAbstractQueryResult result = stack.pop();
 
         List<ISingleResult> list = getResultListNotResolveStruct(result);
         if (list.size() > 0) {
             stack.push(new BooleanResult(true));
         } else {
             stack.push(new BooleanResult(false));
         }
     }
 
     @Override
     public void visitMaxExpression(IMaxExpression expr) {
         expr.getInnerExpression().accept(this);
 
         IAbstractQueryResult result = stack.pop();
         List<ISingleResult> eres = getResultListResolveStruct(result);
 
         if (0 == eres.size()) {
             throw new RuntimeException("Empty collection!");
         }
 
         boolean isDoubleInstance = false;
         double max = Double.MIN_VALUE;
 
         for (ISingleResult element : eres) {
             element = (ISingleResult) doDereference(element);
 
             if (element instanceof IIntegerResult) {
                 int value = ((IIntegerResult) element).getValue();
                 if (max < value) {
                     max = value;
                     isDoubleInstance = false;
                 }
             } else if (element instanceof IDoubleResult) {
                 Double value = ((IDoubleResult) element).getValue();
                 if (max < value) {
                     max = value;
                     isDoubleInstance = true;
                 }
             } else {
                 throw new WrongTypeException("Only Integer or Double are allowed. " + element.getClass() + " was used");
             }
         }
 
         if (isDoubleInstance) {
             stack.push(new DoubleResult(max));
         } else {
             stack.push(new IntegerResult((int) max));
         }
     }
 
     @Override
     public void visitMinExpression(IMinExpression expr) {
         expr.getInnerExpression().accept(this);
 
         IAbstractQueryResult result = stack.pop();
         List<ISingleResult> eres = getResultListResolveStruct(result);
 
         if (0 == eres.size()) {
             throw new RuntimeException("Empty collection!");
         }
 
         boolean isDoubleInstance = false;
         double min = Double.MAX_VALUE;
 
         for (ISingleResult element : eres) {
             element = (ISingleResult) doDereference(element);
 
             if (element instanceof IIntegerResult) {
                 int value = ((IIntegerResult) element).getValue();
                 if (min > value) {
                     min = value;
                     isDoubleInstance = false;
                 }
             } else if (element instanceof IDoubleResult) {
                 Double value = ((IDoubleResult) element).getValue();
                 if (min > value) {
                     min = value;
                     isDoubleInstance = true;
                 }
             } else {
                 throw new WrongTypeException("Only Integer or Double are allowed. " + element.getClass() + " was used");
             }
         }
 
         if (isDoubleInstance) {
             stack.push(new DoubleResult(min));
         } else {
             stack.push(new IntegerResult((int) min));
         }
     }
 
     @Override
     public void visitNotExpression(INotExpression expr) {
         expr.getInnerExpression().accept(this);
 
         IAbstractQueryResult result = stack.pop();
 
         try {
             result = getResult(result);
         } catch (RuntimeException e) {
             throw new WrongTypeException("Unable to retrieve a single value");
         }
 
         result = doDereference(result);
 
         if (!(result instanceof IBooleanResult)) {
             throw new WrongTypeException("Illegal type for operation");
         }
 
         stack.push(new BooleanResult(!((IBooleanResult) result).getValue()));
     }
 
     @Override
     public void visitStructExpression(IStructExpression expr) {
         expr.getInnerExpression().accept(this);
         IAbstractQueryResult res = stack.pop();
         if (res instanceof IStructResult) {
             stack.push(res);
         } else {
             stack.push(new StructResult(getResultListResolveStruct(res)));
         }
     }
 
     @Override
     public void visitSumExpression(ISumExpression expr) {
         expr.getInnerExpression().accept(this);
 
         IAbstractQueryResult result = stack.pop();
         List<ISingleResult> eres = getResultListResolveStruct(result);
 
         boolean isDoubleInstance = false;
         Double sum = 0.0;
 
         for (ISingleResult element : eres) {
             element = (ISingleResult) doDereference(element);
 
             if (element instanceof IIntegerResult) {
                 sum += ((IIntegerResult) element).getValue();
             } else if (element instanceof IDoubleResult) {
                 sum += ((IDoubleResult) element).getValue();
                 isDoubleInstance = true;
             } else {
                 throw new WrongTypeException("Only Integer or Double are allowed. " + element.getClass() + " was used");
             }
         }
 
         if (isDoubleInstance) {
             stack.push(new DoubleResult(sum));
         } else {
             stack.push(new IntegerResult(sum.intValue()));
         }
     }
 
     @Override
     public void visitUniqueExpression(IUniqueExpression expr) {
         expr.getInnerExpression().accept(this);
 
         IAbstractQueryResult result = stack.pop();
 
         List<ISingleResult> uniqueList = new ArrayList<ISingleResult>(new LinkedHashSet<ISingleResult>(getResultListResolveStruct(result)));
 
         stack.push(new BagResult(uniqueList));
     }
 
     @Override
     public void visitAvgExpression(IAvgExpression expr) {
         expr.getInnerExpression().accept(this);
 
         IAbstractQueryResult result = stack.pop();
         List<ISingleResult> eres = getResultListResolveStruct(result);
 
         if (eres.size() == 0) {
             stack.push(new DoubleResult(0.0));
             return;
         }
 
         double avg = 0;
         for (ISingleResult singleResult : eres) {
             if (singleResult instanceof IIntegerResult) {
                 avg += ((IIntegerResult) singleResult).getValue();
             } else if (singleResult instanceof IDoubleResult) {
                 avg += ((IDoubleResult) singleResult).getValue();
             } else {
                 throw new WrongTypeException("Only Integer or Double are allowed. " + singleResult.getClass() + " was used");
             }
         }
         avg /= eres.size();
         stack.push(new DoubleResult(avg));
     }
 
     private ISingleResult getResult(IAbstractQueryResult result) {
         if (result instanceof ISingleResult) {
             if (result instanceof IStructResult) {
                 List<ISingleResult> list = ((IStructResult) result).elements();
                 if (list.size() == 1) {
                     ISingleResult singleResult = list.get(0);
 
                     if (singleResult instanceof IStructResult) {
                         return getResult(singleResult);
                     }
 
                     return singleResult;
                 } else {
                     throw new WrongTypeException();
                 }
             }
 
             return (ISingleResult) result;
         }
 
         if (result instanceof IBagResult) {
             Collection<ISingleResult> collection = ((IBagResult) result).getElements();
             Iterator<ISingleResult> iterator = collection.iterator();
             if (iterator.hasNext()) {
                 ISingleResult singleResult = iterator.next();
                 if (singleResult instanceof IStructResult) {
                     return getResult(singleResult);
                 }
                 return singleResult;
             } else {
                 throw new WrongTypeException();
             }
         }
 
         if (result instanceof ISequenceResult) {
             List<ISingleResult> list = ((ISequenceResult) result).getElements();
             if (list.size() == 1) {
                 ISingleResult singleResult = list.get(0);
 
                 if (singleResult instanceof IStructResult) {
                     return getResult(singleResult);
                 }
 
                 return singleResult;
             } else {
                 throw new WrongTypeException();
             }
         }
         throw new WrongTypeException();
     }
 
     private List<ISingleResult> getResultListResolveStruct(IAbstractQueryResult result) {
         List<ISingleResult> results = new ArrayList<ISingleResult>();
 
         if (result instanceof ISingleResult) {
             if (result instanceof IStructResult) {
                 results.addAll(((IStructResult) result).elements());
             } else {
                 results.add((SingleResult) result);
             }
         }
 
         if (result instanceof IBagResult) {
             for (ISingleResult singleResult : ((IBagResult) result).getElements()) {
                 if (singleResult instanceof IStructResult) {
                     results.addAll(((IStructResult) singleResult).elements());
                 } else {
                     results.add(singleResult);
                 }
             }
         }
 
         if (result instanceof ISequenceResult) {
             for (ISingleResult singleResult : ((ISequenceResult) result).getElements()) {
                 if (singleResult instanceof IStructResult) {
                     results.addAll(((IStructResult) singleResult).elements());
                 } else {
                     results.add(singleResult);
                 }
             }
         }
 
         return results;
     }
 
     private List<ISingleResult> getResultListNotResolveStruct(IAbstractQueryResult result) {
         List<ISingleResult> results = new ArrayList<ISingleResult>();
 
         if (result instanceof ISingleResult) {
             results.add((SingleResult) result);
         }
 
         if (result instanceof IBagResult) {
             for (ISingleResult singleResult : ((IBagResult) result).getElements()) {
                 results.add(singleResult);
             }
         }
 
         if (result instanceof ISequenceResult) {
             for (ISingleResult singleResult : ((ISequenceResult) result).getElements()) {
                 if (singleResult instanceof IStructResult) {
                     results.addAll(((IStructResult) singleResult).elements());
                 }
             }
         }
 
         return results;
     }
 
     private IAbstractQueryResult doDereference(IAbstractQueryResult result) {
 
         ISingleResult singleResult;
         try {
             singleResult = getResult(result);
         } catch (RuntimeException e) {
             return result;
         }
 
         if (singleResult instanceof IReferenceResult) {
             IOID oid = ((IReferenceResult) singleResult).getOIDValue();
             ISBAObject object = store.retrieve(oid);
 
             if (object instanceof IBooleanObject) {
                 return new BooleanResult(((IBooleanObject) object).getValue());
             }
 
             if (object instanceof IDoubleResult) {
                 return new DoubleResult(((IDoubleResult) object).getValue());
             }
 
             if (object instanceof IIntegerObject) {
                 return new IntegerResult(((IIntegerObject) object).getValue());
             }
 
             if (object instanceof IStringObject) {
                 return new StringResult(((IStringObject) object).getValue());
             }
 
             return new ReferenceResult(object.getOID());
         }
 
         return singleResult;
 
     }
 
     private String getString(final ISingleResult result) {
         if (result instanceof IStringResult) {
             return ((IStringResult) result).getValue();
         } else if (result instanceof ISimpleResult) {
             return ((ISimpleResult) result).getValue().toString();
         } else if (result instanceof IStructResult) {
             try {
                 return getString(getResult(result));
             } catch (WrongTypeException e) {
                 return "[WrongTypeException in getString]";
             }
         } else if (result instanceof IReferenceResult) {
             IOID oid = ((IReferenceResult) result).getOIDValue();
             return oid.toString();
         } else if (result instanceof IBinderResult) {
             IBinderResult binder = (IBinderResult) result;
             try {
                 return getString(getResult(binder.getValue())) + " as " + binder.getName();
             } catch (WrongTypeException e) {
                 return "[WrongTypeException in getString]";
             }
         }
 
         return "[I have no idea whats up]";
     }
 }
