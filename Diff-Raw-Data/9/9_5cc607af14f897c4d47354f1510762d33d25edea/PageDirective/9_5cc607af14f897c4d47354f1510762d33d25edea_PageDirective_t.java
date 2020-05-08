 package com.silentmatt.dss.directive;
 
 import com.silentmatt.dss.DeclarationList;
 import com.silentmatt.dss.EvaluationState;
 import com.silentmatt.dss.Immutable;
 import com.silentmatt.dss.Rule;
 import com.silentmatt.dss.SimpleSelector;
 import com.silentmatt.dss.css.CssCombinator;
 import com.silentmatt.dss.css.CssPageDirective;
 import com.silentmatt.dss.css.CssRule;
 import com.silentmatt.dss.css.CssSimpleSelector;
 import java.io.IOException;
 import java.util.List;
 
 /**
  *
  * @author Matthew Crumley
  */
 @Immutable
 public final class PageDirective extends DeclarationDirective {
     private final SimpleSelector selector;
 
     public PageDirective(SimpleSelector pseudo, DeclarationList declarations) {
         super(declarations);
         this.selector = pseudo;
     }
 
     @Override
     public String getName() {
         return "@page";
     }
 
     public SimpleSelector getSelector() {
         return selector;
     }
 
     @Override
     public String toString(int nesting) {
        String start = Rule.getIndent(nesting);

        StringBuilder txt = new StringBuilder(start).append("@page");
         if (selector != null) {
             txt.append(" ");
             txt.append(selector.toString());
         }
         txt.append(" ");
 
         txt.append(getDeclarationsString(nesting));
         return txt.toString();
     }
 
     @Override
     public CssRule evaluate(EvaluationState state, List<Rule> container) throws IOException {
         CssSimpleSelector pseudo = new CssSimpleSelector();
         pseudo.setCombinator(CssCombinator.None);
        if (selector != null) {
            pseudo.setPseudo(selector.toString().replaceFirst(":", ""));
        }
         return new CssPageDirective(pseudo, getDeclarationBlock().evaluateStyle(state, true).getCssDeclarations(state));
     }
 }
