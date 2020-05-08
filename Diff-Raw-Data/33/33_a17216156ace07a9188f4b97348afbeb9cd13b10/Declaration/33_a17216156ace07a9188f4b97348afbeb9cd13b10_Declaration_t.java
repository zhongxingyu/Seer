 package net.sf.beezle.jasmin.scss;
 
 public class Declaration {
     private final String property;
    private final Expr expr;
 
    public Declaration(String property, Expr expr) {
         this.property = property;
        this.expr = expr;
     }
 }
