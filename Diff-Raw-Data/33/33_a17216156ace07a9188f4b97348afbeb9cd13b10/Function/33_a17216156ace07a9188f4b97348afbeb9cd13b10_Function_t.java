 package net.sf.beezle.jasmin.scss.term;
 
import net.sf.beezle.jasmin.scss.Expr;

 public class Function extends BaseTerm {
    private final Expr expr;

    public Function(Expr expr) {
        this.expr = expr;
    }
 }
