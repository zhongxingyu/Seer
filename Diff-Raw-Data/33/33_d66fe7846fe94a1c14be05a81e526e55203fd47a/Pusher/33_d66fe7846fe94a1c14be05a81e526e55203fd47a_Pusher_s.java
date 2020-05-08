 package filter;
 
 import static filter.expression.BinaryExpression.Operator.AND;
 import static filter.expression.BinaryExpression.Operator.OR;
 import static filter.expression.Expression.F;
 import static filter.expression.Expression.T;
 import static filter.expression.Expression.toAnd;
 import static filter.expression.Expression.toOr;
 import static filter.expression.Expression.Status.Pushable;
 import static filter.expression.Expression.Status.Unpushable;
 import static java.util.Arrays.asList;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import filter.expression.BinaryExpression;
 import filter.expression.ConstantExpression;
 import filter.expression.Converter;
 import filter.expression.Expression;
 import filter.expression.NotExpression;
 import filter.expression.VariableExpression;
 
 public class Pusher {
 
   /**
    * @param e the original expression
    * @param known the know (aka fast fields)
    * @return a simplified AND or ORs
    */
   public Expression canonicalConjunction(Expression e) {
     Expression converted = e.convert(new PushDownConverter());
     return converted;
   }
 
   /**
    * factorizes the expression to reduce the number of operations required
    * @param e a canonical expression as returned by canonicalConjunction
    * @return the factorized expression
    */
   public Expression factorize(Expression e) {
     Expression converted = e.convert(new Converter<Expression>() {
       @Override
       public Expression convert(BinaryExpression root) {
         List<Expression> andList = root.toAndList();
         return factorize(andList);
       }
 
       private Expression factorize(List<Expression> andList) {
         if (andList.size() == 1) {
           return andList.get(0);
         }
         // (A OR B) AND (A OR C) = A OR (B AND C)
         int max = 0;
         Expression maxExpression = null;
         Map<Expression, Integer> count = new HashMap<>();
         for (Expression or : andList) {
           List<Expression> orList = or.toOrList();
           for (Expression e : orList) {
             int c = count.containsKey(e) ? count.get(e) + 1 : 1;
             count.put(e, c);
             if (c > max) {
               max = c;
               maxExpression = e;
             }
           }
         }
        List<Expression> newAndList = new ArrayList<>();
        for (Expression or : andList) {
          List<Expression> orList = or.toOrList();
          List<Expression> newOrList = new ArrayList<>();
          for (Expression e : orList) {
            if (!e.toString().equals(maxExpression.toString())) {
              newOrList.add(e);
             }
           }
          if (newOrList.size() > 0) {
            newAndList.add(toOr(newOrList));
           }
        }
        if (newAndList.size() < 2) {
          // there was nothing left to factorize
           return toAnd(andList);
         }
        return new BinaryExpression(OR, maxExpression, factorize(newAndList));
       }
 
       @Override
       public Expression convert(NotExpression e) {
         return e;
       }
 
       @Override
       public Expression convert(VariableExpression e) {
         return e;
       }
 
       @Override
       public Expression convert(ConstantExpression e) {
         return e;
       }
     });
     return converted;
   }
 
   public Expression pushDown(Expression e) {
     Expression converted = canonicalConjunction(e);
     return pushDownCanonical(converted);
   }
 
   Expression pushDownCanonical(Expression converted, String... known) {
     List<Expression> list = converted.toAndList();
     List<Expression> pushable = new ArrayList<>();
     for (Expression expression : list) {
       if (expression.isPushable()) {
         pushable.add(expression);
       }
     }
     if (pushable.size() > 0) {
       return factorize(toAnd(pushable));
     } else {
       return T;
     }
   }
 }
 final class PushDownConverter implements Converter<Expression> {
 
   @Override
   public Expression convert(BinaryExpression e) {
     switch (e.operator) {
       case OR:
         //(A | (B & C)) = (A | B) & (A | C)
         List<Expression> leftExpressions = e.left.convert(this).toAndList();
         List<Expression> rightExpressions = e.right.convert(this).toAndList();
         List<Expression> resultExpressions = new ArrayList<>();
         for (Expression l : leftExpressions) {
           for (Expression r : rightExpressions) {
             List<Expression> or = new ArrayList<>();
             or.addAll(l.toOrList());
             or.addAll(r.toOrList());
             resultExpressions.add(toOr(simplifyOr(or)));
           }
         }
         return toAnd(simplifyAnd(resultExpressions));
       case AND:
         List<Expression> and = e.toAndList();
         List<Expression> converted = new ArrayList<>(and.size());
         for (Expression expression : and) {
           converted.addAll(expression.convert(this).toAndList());
         }
         return toAnd(simplifyAnd(converted));
       default:
         throw new RuntimeException(String.valueOf(e.operator));
     }
   }
 
   private Collection<? extends Expression> simplifyOr(List<Expression> or) {
     final Map<Object, Expression> simplified = new HashMap<>();
     for (Expression e : or) {
       if (e.isConstant()) {
         if (e.evaluate(null)) {
           // A OR T = T
           return Arrays.asList(T);
         } else {
           // A OR F = A
           continue;
         }
       }
       Expression existing = simplified.get(e.getId());
       if (existing == null) {
         simplified.put(e.getId(), e);
       } else {
         if ((existing.isNegated() && !e.isNegated())
             || (!existing.isNegated() && e.isNegated())) {
           // (A OR (NOT A)) = T
           // T OR X = T
           return Arrays.asList(T);
         } // else do nothing A OR A = A
       }
     }
     return simplified.values();
   }
 
   private Collection<? extends Expression> simplifyAnd(List<Expression> and) {
     Map<Object, Expression> simplified = new HashMap<>();
     for (Expression e : and) {
       if (e.isConstant()) {
         if (e.evaluate(null)) {
           // A AND T = A
           continue;
         } else {
           // A AND F = F
           return asList(F);
         }
       }
       Expression existing = simplified.get(e.getId());
       if (existing == null) {
         simplified.put(e.getId(), e);
       } else {
         if ((existing.isNegated() && !e.isNegated())
             || (!existing.isNegated() && e.isNegated())) {
           // (A AND (NOT A)) = F
           // F AND X = F
           return asList(F);
         } // else do nothing A AND A = A
       }
     }
     List<Expression> simplifiedList = new ArrayList<>(simplified.values());
     simplifiedList = simplifyImplications(simplifiedList);
     return simplifiedList;
   }
 
   private List<Expression> simplifyImplications(
       List<Expression> simplifiedList) {
     if (simplifiedList.size() >= 2) {
       List<Set<Expression>> ors = new ArrayList<>();
       for (Expression expression : simplifiedList) {
         ors.add(new HashSet<>(expression.toOrList()));
       }
       boolean[] toRemove = new boolean[simplifiedList.size()];
       for (int i = 0; i < ors.size() - 1; i++) {
         Set<Expression> o1 = ors.get(i);
         for (int j = i + 1; j < simplifiedList.size(); j++) {
           if (toRemove[j]) {
             continue;
           }
           Set<Expression> o2 = ors.get(j);
           // (A AND (A OR B)) = A
           if (o2.size() > o1.size() && o2.containsAll(o1)) {
             toRemove[j] = true;
           } else if (o1.size() > o2.size() && o1.containsAll(o2)) {
             toRemove[i] = true;
             break;
           }
         }
       }
       List<Expression> newList = new ArrayList<>();
       for (int i = 0; i < simplifiedList.size(); i++) {
         if (!toRemove[i]) {
           newList.add(simplifiedList.get(i));
         }
       }
       simplifiedList = newList;
     }
     return simplifiedList;
   }
 
   @Override
   public Expression convert(NotExpression e) {
     return e.operand.convert(new Converter<Expression>() {
       @Override
       public Expression convert(VariableExpression e) {
         Expression c = e.convert(PushDownConverter.this);
         return new NotExpression(
             c.status,
             c);
       }
 
       @Override
       public Expression convert(NotExpression notExpression) {
         // not not
         return notExpression.operand.convert(PushDownConverter.this);
       }
 
       @Override
       public Expression convert(BinaryExpression e) {
         return new BinaryExpression(e.operator == OR ? AND : OR, e.left.convert(this), e.right.convert(this)).convert(PushDownConverter.this);
       }
 
       @Override
       public Expression convert(ConstantExpression e) {
         return e.negate();
       }
     });
 
   }
 
   @Override
   public Expression convert(VariableExpression e) {
     String name = e.getName();
     return new VariableExpression(
               name.startsWith("v") ?
               Pushable : Unpushable,
           name
         );
   }
 
   @Override
   public Expression convert(ConstantExpression e) {
     return e;
   }
 
 }
