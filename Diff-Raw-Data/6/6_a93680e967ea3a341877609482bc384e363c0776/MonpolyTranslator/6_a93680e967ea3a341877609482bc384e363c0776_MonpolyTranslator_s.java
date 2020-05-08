 /******************************************************************************
   Event trace translator
   Copyright (C) 2012 Sylvain Halle
 
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  ******************************************************************************/
 package ca.uqac.info.trace.conversion;
 
 import java.util.*;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import ca.uqac.info.ltl.*;
 import ca.uqac.info.trace.*;
 import ca.uqac.info.util.Relation;
 
 /**
  * Translates an input trace and a formula into the MFOTL language
  * for the Monpoly tool.  Monpoly is a monitoring tool that checks
  * compliance of log files with respect to policies. Policies are
  * specified by formulas in metric first-order temporal logic. 
  * <p>
  * For more information about Monpoly, please refer to its
  * <a href="https://projects.developer.nokia.com/MonPoly">web site</a>
  * or the following paper:
  * <ul>
  * <li>Felix Klaedtke, David Basin, Matus Harvan, Eugen Zalinescu.
  * (2012). <cite>MONPOLY: Monitoring Usage-control Policies.</cite>
  * 2nd International Conference on Runtime Verification (RV 2011).
  * Springer: Lecture Notes in Computer Science 7186, 360-364.</li> 
  * </ul>
  * @author Aouatef Mrad
  *
  */
 public class MonpolyTranslator extends Translator
 {
   protected final int m_leftBound = 0;
   protected int m_rightBound = 1000;
 
   @Override
   public String translateTrace(EventTrace m_trace)
   {
     setTrace(m_trace);
     return translateTrace();
   }
 
   @Override
   public String translateTrace()
   {
     StringBuilder out = new StringBuilder();
     int trace_length = m_trace.size();
     for (int i = 0; i < trace_length; i++)
     {
       Event e = m_trace.elementAt(i);
       out.append("@").append(i);
       Node n = e.getDomNode();
       NodeList children = n.getChildNodes();
       Node child;
       String val = "";
       /* TODO: hard-coded nested loops is not a good way to do trace flattening! */
       if (children.getLength() > 1)
       {
         NodeList level1 = children.item(1).getChildNodes();
         // out.append(" ").append( children.item(1).getNodeName());
         if (level1.getLength() > 1)
         {
 
           NodeList level2 = level1.item(1).getChildNodes();
           if (level2.getLength() > 1)
           {
 
             for (int index = 1; index < level2.getLength(); index++)
             {
               child = level2.item(index);
 
               if (child.getChildNodes().getLength() == 1)
               {
                 val = child.getTextContent();
                 val = val.trim();
                 if (val.isEmpty())
                   continue;
                 out.append(toMonpoly(child, ""));
               } else if (child.getChildNodes().getLength() > 1)
               {
                 NodeList level3 = level2.item(index).getChildNodes();
 
                 if (level3.getLength() > 1)
                 {
                   for (int in = 1; in < level3.getLength(); in++)
                   {
                     child = level3.item(in);
 
                     if (child.getChildNodes().getLength() == 1)
                     {
                       val = child.getTextContent();
                       val = val.trim();
                       if (val.isEmpty())
                         continue;
                       out.append(toMonpoly(child, ""));
                     } else if (child.getChildNodes().getLength() > 1)
                     {
                       NodeList level4 = level3.item(in).getChildNodes();
 
                       if (level4.getLength() > 1)
                       {
                         for (int y = 1; y < level4.getLength(); y++)
                         {
                           child = level4.item(y);
                           if (child.getChildNodes().getLength() == 1)
                           {
                             val = child.getTextContent();
                             val = val.trim();
                             if (val.isEmpty())
                               continue;
                             out.append(toMonpoly(child, ""));
                           }
                         }
                       }
                     }
                   }
 
                 }
 
               }
             }
           } else
           {
             for (int j = 0; j < level1.getLength(); j++)
             {
               child = level1.item(j);
               val = child.getTextContent();
               val = val.trim();
               if (val.isEmpty())
                 continue;
               out.append(toMonpoly(child, ""));
             }
 
           }
         } else
         {
           for (int j = 0; j < children.getLength(); j++)
           {
             child = children.item(j);
             val = child.getTextContent();
             val = val.trim();
             if (val.isEmpty())
               continue;
             out.append(toMonpoly(child, ""));
           }
 
         }
       }
       out.append("\n");
     }
     return out.toString();
   }
 
   private StringBuilder toMonpoly(Node n, String indent)
   {
     StringBuilder out = new StringBuilder();
     NodeList children = n.getChildNodes();
     int num_children = children.getLength();
     if (num_children == 1 && children.item(0).getNodeType() == Node.TEXT_NODE)
     {
       Node child = children.item(0);
       String val = child.getNodeValue();
       val = val.trim();
       if (val.isEmpty())
       {
         out = new StringBuilder();
         return out;
       }
       out.append("\n\t").append(indent).append(n.getNodeName()).append(" ( ")
           .append(val).append(" ) ");
       return out;
     }
     for (int i = 0; i < num_children; i++)
     {
       Node child = children.item(i);
       if (child.getNodeType() == Node.TEXT_NODE)
       {
         String val = child.getNodeValue();
         val = val.trim();
         if (val.isEmpty()) // We ignore whitespace
           continue;
       }
       out.append(toMonpoly(child, ""));
       out.append("\n");
     }
     return out;
   }
 
   @Override
   /**
    * 
    */
   public String translateFormula()
   {
     // Since MFOTL operators are bounded, we set the upper bound of the
     // interval to the length of the trace
     m_rightBound = m_trace.size();
     StringBuilder out = new StringBuilder();
     MonpolyFormulaTranslator mft = new MonpolyFormulaTranslator();
     m_formula.accept(mft);
     out.append(mft.getFormula());
     return out.toString();
   }
 
   protected class MonpolyFormulaTranslator implements OperatorVisitor
   {
     Stack<StringBuilder> m_pieces;
 
     public MonpolyFormulaTranslator()
     {
       super();
       m_pieces = new Stack<StringBuilder>();
     }
 
     public String getFormula()
     {
       StringBuilder out = m_pieces.peek();
       return out.toString();
     }
 
     @Override
     public void visit(OperatorAnd o)
     {
       StringBuilder right = m_pieces.pop();
       StringBuilder left = m_pieces.pop();
       StringBuilder out = new StringBuilder("(").append(left).append(") AND (")
           .append(right).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorOr o)
     {
       StringBuilder right = m_pieces.pop();
       StringBuilder left = m_pieces.pop();
       StringBuilder out = new StringBuilder("(").append(left).append(") OR (")
           .append(right).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorImplies o)
     {
       StringBuilder right = m_pieces.pop();
       StringBuilder left = m_pieces.pop();
      StringBuilder op = m_pieces.pop();
 
      StringBuilder out = new StringBuilder("(").append(left).append(") IMPLIES")
          .append("(").append(right).append(op).append(")");
       m_pieces.push(out);
 
     }
 
     @Override
     public void visit(OperatorNot o)
     {
       StringBuilder op = m_pieces.pop();
       StringBuilder out = new StringBuilder("NOT (").append(op).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorF o)
     {
       StringBuilder op = m_pieces.pop();
       StringBuilder out = new StringBuilder("EVENTUALLY [")
           .append(m_leftBound).append(",").append(m_rightBound)
           .append("] ( ").append(op).append(" )");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorX o)
     {
       StringBuilder op = m_pieces.pop();
       StringBuilder out = new StringBuilder("NEXT [0,1] ( ")
         .append(op).append(" )");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorG o)
     {
       StringBuilder op = m_pieces.pop();
       StringBuilder out = new StringBuilder("ALWAYS [")
       .append(m_leftBound).append(",").append(m_rightBound)
       .append("] ( ").append(op).append(" )");
       m_pieces.push(out);
 
     }
     
     @Override
     public void visit(OperatorTrue o)
     {
       m_pieces.push(new StringBuilder("TRUE"));
       
     }
 
     @Override
     public void visit(OperatorFalse o)
     {
       m_pieces.push(new StringBuilder("FALSE"));
     }
 
     @Override
     public void visit(OperatorEquals o)
     {
       StringBuilder right = m_pieces.pop(); // Pop right-hand side
       StringBuilder left = m_pieces.pop(); // Pop left-hand side
       StringBuilder out = new StringBuilder();
       Operator o_left = o.getLeft();
       if (o_left instanceof XPathAtom)
       {
         // If we equal a path to a term, we must write it as a predicate
         out.append(left).append("(").append(right).append(")");
       }
       else
       {
         // Otherwise, we write the equality as an equality
         out.append("(").append(left).append(" = ").append(right).append(")");
       }
       m_pieces.push(out);
     }
 
     @Override
     public void visit(Atom o)
     {
       if (o instanceof Constant)
         m_pieces.push(new StringBuilder("\"").append(o.getSymbol()).append("\""));
       else
         m_pieces.push(new StringBuilder("?").append(o.getSymbol()));
     }
 
     @Override
     public void visit(OperatorEquiv o)
     {
       StringBuilder right = m_pieces.pop();
       StringBuilder left = m_pieces.pop();
       StringBuilder out = new StringBuilder("(").append(left).append(") <-> (")
           .append(right).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(OperatorU o)
     {
       StringBuilder right = m_pieces.pop();  // Pop right-hand side
       StringBuilder left = m_pieces.pop();  // Pop left-hand side
       StringBuilder out = new StringBuilder("(").append(left).append(" UNTIL [")
       .append(m_leftBound).append(",").append(m_rightBound)
       .append("] ( ").append(right).append(" )");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(Exists o)
     {
       StringBuilder operand = m_pieces.pop();
       StringBuilder variable = m_pieces.pop();
       StringBuilder var = m_pieces.peek();
       StringBuilder out = new StringBuilder();
       out.append("EXISTS ").append(var).append(". ").append("(")
           .append(variable).append("(").append(var).append(")")
           .append(" AND ").append(operand).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(ForAll o)
     {
       StringBuilder operand = m_pieces.pop();
       StringBuilder variable = m_pieces.pop();
       StringBuilder var = m_pieces.peek();
       StringBuilder out = new StringBuilder();
       out.append("FORALL ").append(var).append(". ").append("(")
           .append(variable).append("(").append(var).append(")")
           .append(" AND ").append(operand).append(")");
       m_pieces.push(out);
     }
 
     @Override
     public void visit(XPathAtom p)
     {
       // Not supposed to happen!
       // System.err.println("Error: XML Path found in MonPoly translator");
       // assert false;
       m_pieces.push(new StringBuilder(p.toString(false)));
     }
   }
 
   protected class MonpolyEqualityGetter implements OperatorVisitor
   {
 
     Set<OperatorEquals> m_equalities;
 
     public MonpolyEqualityGetter()
     {
       super();
       m_equalities = new HashSet<OperatorEquals>();
     }
 
     public Set<OperatorEquals> getEqualities()
     {
       return m_equalities;
     }
     
     @Override
     public void visit(OperatorTrue o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorFalse o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorEquals o)
     {
       m_equalities.add(o);
     }
 
     @Override
     public void visit(OperatorU o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorAnd o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorOr o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorNot o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorF o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorX o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorG o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorImplies o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(OperatorEquiv o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(Atom o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(Exists o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(ForAll o)
     {
       // Nothing to be done here
     }
 
     @Override
     public void visit(XPathAtom p)
     {
 
     }
 
   }
   
   @Override
   public String getSignature()
   {
     return getSignature(m_trace);
   }
 
 
   public String getSignature(EventTrace trace)
   {
     StringBuilder out = new StringBuilder();
     Relation<String, String> param_domains = trace.getParameterDomain();
     Set<String> params = param_domains.keySet();
     Vector<String> vectParams = new Vector<String>();
     vectParams.addAll(params);
 
     out = this.toMonpolySignature(vectParams);
     return out.toString();
   }
 
   /**
    * 
    * @param params
    * @return
    */
 
   private StringBuilder toMonpolySignature(Vector<String> params)
   {
     StringBuilder out = new StringBuilder();
     for (String p : params)
     {
       out.append(p).append(" (string)\n");
     }
     return out;
 
   }
 
   @Override
   public String translateFormula(Operator o)
   {
     setFormula(o);
     return translateFormula();
   }
 
   @Override
   public boolean requiresPropositional()
   {
     return false;
   }
   
   public static void main(String[] args)
   {
       // Definition of trace and property
       String filename = "traces/traces_0.txt";
       String formula = "F (∃i ∈ /p0 : (i=0))";
       
       // Read trace
       java.io.File fic = new java.io.File(filename);
       XmlTraceReader xtr = new XmlTraceReader();
       EventTrace t = null;
       try
       {
         t = xtr.parseEventTrace(new java.io.FileInputStream(fic));  
       }
       catch (java.io.FileNotFoundException ex)
       {
         ex.printStackTrace();
         System.exit(1);
       }
       assert t != null;
       
       // Parse property
       Operator o = null;
       try
       {
           o = Operator.parseFromString(formula);
       }
       catch (Operator.ParseException e)
       {
           System.out.println("Parse exception");
       }
       
       // Convert property and trace to SQL
       Translator bt = new MonpolyTranslator();
       bt.setFormula(o);
       bt.setTrace(t);
       System.out.println(bt.translateTrace());
       String f = bt.translateFormula();
       System.out.println(f);
       String sig = bt.getSignature();
       System.out.println(sig);
   }
   
   @Override
   public boolean requiresFlat()
   {
     return true;
   }
   
   @Override
   public boolean requiresAtomic()
   {
     return false;
   }
 }
