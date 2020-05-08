 package com.zenika.dorm.core.graph.visitor;
 
 import com.zenika.dorm.core.graph.DependencyNode;
 import com.zenika.dorm.core.graph.visitor.impl.DependencyVisitorCheckException;
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 @Aspect
 public class DependencyVisitorCheckAspect {
 
     /**
      * pointcut on the method visitEnter used to enter to an composite node
      *
      * @param node    the composite node
      * @param visitor the current visitor
      */
     @Pointcut("execution(Boolean com.zenika.dorm.core.graph.visitor.DependencyVisitor.visitEnter" +
            "(com.zenika.dorm.core.graph.visitor.DependencyVisitor)) && args(node) && target(visitor)")
    public void visit(DependencyNode node, DependencyVisitor visitor) {
     }
 
    @Around("visit(node, visitor)")
     public Object performChecksOnComposite(ProceedingJoinPoint joinPoint, DependencyNode node,
                                            DependencyVisitor visitor) throws Throwable {
 
         try {
             visitor.performChecks(node);
         } catch (DependencyVisitorCheckException e) {
             return false;
         }
 
         return joinPoint.proceed();
     }
 }
