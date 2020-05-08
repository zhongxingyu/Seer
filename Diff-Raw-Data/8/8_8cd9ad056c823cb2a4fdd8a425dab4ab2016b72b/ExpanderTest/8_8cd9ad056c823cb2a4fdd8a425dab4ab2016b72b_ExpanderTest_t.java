 package net.sf.laja.parser.cdd.statetemplate;
 
 import org.junit.Test;
 
 import java.util.*;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class ExpanderTest {
     Expander expander = new Expander();
 
     @Test
     public void calculateExpansionForHierarchy() {
         StateTemplate a = createStateTemplate("AState");
         addAttribute(a, "x", "int", false, " // (key) (id) (x)");
         addAttribute(a, "y", "int", false);
         expander.add(a);
         
         StateTemplate b = createStateTemplate("BState");
         addAttribute(b, "a", "AState", true);
         addAttribute(b, "z", "int", false);
         expander.add(b);
 
         StateTemplate c = createStateTemplate("CState");
         addAttribute(c, "b", "BState", true);
         expander.add(c);
 
         Map<String, Expander.ExpansionResult> result = expander.calculateExpansion();
 
         Map<String, EResult> expectedClassMap = new LinkedHashMap<String, EResult>();
         Imports imports = new Imports();
         List<Attr> attrs = new ArrayList<Attr>();
         attrs.add(new Attr("int", "x", " // (key) (id) (x)"));
         attrs.add(new Attr("int", "y"));
         expectedClassMap.put("AState", new EResult(imports, attrs));
 
         attrs = new ArrayList<Attr>();
         attrs.add(new Attr("int", "x", " // (x)"));
         attrs.add(new Attr("int", "y"));
         attrs.add(new Attr("int", "z"));
         expectedClassMap.put("BState", new EResult(imports, attrs));
 
         attrs = new ArrayList<Attr>();
         attrs.add(new Attr("int", "x", " // (x)"));
         attrs.add(new Attr("int", "y"));
         attrs.add(new Attr("int", "z"));
         expectedClassMap.put("CState", new EResult(imports, attrs));
 
         assertEquals(0, a.expandedTypes.size());
         assertEquals(1, b.expandedTypes.size());
         assertEquals("a", b.expandedTypes.iterator().next().variable);
         assertEquals(1, c.expandedTypes.size());
         assertEquals("b", c.expandedTypes.iterator().next().variable);
 
         assertEquals(0, a.allExpandedTypes.size());
         assertEquals(1, b.allExpandedTypes.size());
         assertEquals(2, c.allExpandedTypes.size());
 
         verifyResult(result, expectedClassMap);
     }
 
     @Test
     public void calculateExpansionWithExtraImportsNeeded() {
         StateTemplate a = createStateTemplate("AState");
         addImports(a, "net.XClass");
         addAttribute(a, "x", "XClass", false);
         expander.add(a);
 
         StateTemplate b = createStateTemplate("BState");
         addImports(b);
         addAttribute(b, "a", "AState", true);
         expander.add(b);
 
         Map<String, Expander.ExpansionResult> result = expander.calculateExpansion();
 
         Map<String, EResult> expectedClassMap = new LinkedHashMap<String, EResult>();
         List<Attr> attrs = new ArrayList<Attr>();
         attrs.add(new Attr("XClass", "x"));
         expectedClassMap.put("AState", new EResult(new Imports(), attrs));
 
         Imports imports = new Imports();
         imports.addImport("net.XClass");
         attrs = new ArrayList<Attr>();
         attrs.add(new Attr("XClass", "x"));
         expectedClassMap.put("BState", new EResult(imports, attrs));
 
         verifyResult(result, expectedClassMap);
     }
 
     @Test
     public void calculateCyclicExpansion() {
         StateTemplate a = createStateTemplate("AState");
         addAttribute(a, "x", "int", false);
         addAttribute(a, "c", "CState", true);
         expander.add(a);
 
         StateTemplate b = createStateTemplate("BState");
         addAttribute(b, "y", "int", false);
         addAttribute(b, "a", "AState", true);
         expander.add(b);
 
         StateTemplate c = createStateTemplate("CState");
         addAttribute(c, "z", "int", false);
         addAttribute(c, "b", "BState", true);
         expander.add(c);
 
         expander.calculateExpansion();
 
         assertTrue(expander.cyclic);
        assertEquals("Cyclic references: AState.c > CState.b > BState.a > AState", expander.cyclicMessage);
     }
 
     private void verifyResult(Map<String, Expander.ExpansionResult> result, Map<String, EResult> expectedClassMap) {
         for (String stateClass : result.keySet()) {
             Imports imports = result.get(stateClass).imports;
             Imports expectedImports = expectedClassMap.get(stateClass).imports;
             assertEquals(expectedImports, imports);
 
             List<Attribute> attributes = result.get(stateClass).attributes;
             List<Attr> expectedAttributes = expectedClassMap.get(stateClass).attributes;
             assertEquals(attributes.size(), expectedAttributes.size());
             for (Attribute attribute : attributes) {
                 assertTrue(expectedAttributes.contains(new Attr(attribute)));
             }
         }
     }
 
     private StateTemplate createStateTemplate(String stateClass) {
         final String state = "State";
         String templateClassName = stateClass.substring(0, stateClass.length() - state.length()) + "StateTemplate";
         StateTemplate stateTemplate = new StateTemplate(templateClassName);
         stateTemplate.setClassname(templateClassName);
         stateTemplate.stateClass = stateClass;
         stateTemplate.attributes = new ArrayList<Attribute>();
         stateTemplate.imports = new Imports();
         stateTemplate.allImports = new Imports();
         return stateTemplate;
     }
     
     private Attribute createAttribute(String variable, String type, boolean isExpand, String comment) {
         Attribute attribute = new Attribute();
         attribute.setVariable(variable);
         attribute.setType(type);
         attribute.isExpand = isExpand;
         attribute.setComment(comment);
         return  attribute;
     }
 
     private void addAttribute(StateTemplate stateTemplate, String variable, String type, boolean isMerge) {
         addAttribute(stateTemplate, variable, type, isMerge, "");
     }
 
     private void addAttribute(StateTemplate stateTemplate, String variable, String type, boolean isMerge, String comment) {
         Attribute attribute = createAttribute(variable, type, isMerge, comment);
         stateTemplate.addAttribute(attribute);
     }
 
     private void addImports(StateTemplate stateTemplate, String... importsToAdd) {
         Imports imports = new Imports();
         for (String i : importsToAdd) {
             imports.addImport(i);
         }
         stateTemplate.imports = imports;
     }
 
     public class EResult {
         Imports imports;
         List<Attr> attributes;
 
         public EResult(Imports imports, List<Attr> attributes) {
             this.imports = imports;
             this.attributes = attributes;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             EResult eResult = (EResult) o;
 
             if (attributes != null ? !attributes.equals(eResult.attributes) : eResult.attributes != null) return false;
             if (imports != null ? !imports.equals(eResult.imports) : eResult.imports != null) return false;
 
             return true;
         }
 
         @Override
         public String toString() {
             return "EResult{" +
                     "imports=" + imports +
                     ", attributes=" + attributes +
                     '}';
         }
     }
     
     public class Attr {
         String type;
         String variable;
         String comment;
 
         public Attr(String type, String variable) {
             this(type, variable, "");
         }
 
         public Attr(String type, String variable, String comment) {
             this.type = type;
             this.variable = variable;
             this.comment = comment;
         }
 
         public Attr(Attribute attribute) {
             type = attribute.type;
             variable = attribute.variable;
             comment = attribute.comment;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             Attr attr = (Attr) o;
 
             if (comment != null ? !comment.equals(attr.comment) : attr.comment != null) return false;
             if (type != null ? !type.equals(attr.type) : attr.type != null) return false;
             if (variable != null ? !variable.equals(attr.variable) : attr.variable != null) return false;
 
             return true;
         }
 
         @Override
         public String toString() {
             return "Attr{" +
                     "type='" + type + '\'' +
                     ", variable='" + variable + '\'' +
                     ", comment='" + comment + '\'' +
                     '}';
         }
     }
 }
