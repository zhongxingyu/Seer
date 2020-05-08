 package org.webmacro.directive;
 
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.engine.*;
 
 public class ParseDirective extends Directive {
 
   private static final int PARSE_TEMPLATE = 1;
 
   private Macro template;
 
   private static final ArgDescriptor[] 
     myArgs = new ArgDescriptor[] {
       new QuotedStringArg(PARSE_TEMPLATE), 
     };
 
   private static final DirectiveDescriptor 
     myDescr = new DirectiveDescriptor("parse", ParseDirective.class, 
                                       myArgs, null);
 
   public static DirectiveDescriptor getDescriptor() {
     return myDescr;
   }
 
   public Object build(DirectiveBuilder builder, 
                       BuildContext bc) 
   throws BuildException {
     Object o = builder.getArg(PARSE_TEMPLATE, bc);
     if (o instanceof Macro) {
       template = (Macro) o;
       return this;
     } 
     else 
       try {
         return bc.getBroker().get("template", o.toString());
       } catch (NotFoundException ne) {
         throw new BuildException("Template " + o + " not found: ", ne); 
       }
   }
 
   public void write(FastWriter out, Context context) 
     throws ContextException, IOException {
 
     String fname = template.evaluate(context).toString();
     try {
       Template tmpl = (Template) context.getBroker().get("template", fname);
       tmpl.write(out,context);
     } catch (IOException e) {
      String warning = "Error reading template: " + fname;
       context.getLog("engine").warning(warning, e);
      writeWarning(warning, out);
     } catch (Exception e) {
       String warning = "Template not found: " + fname;
       context.getLog("engine").warning(warning,e);
      writeWarning(warning, out);
     }
   }
   
   public void accept(TemplateVisitor v) {
     v.beginDirective(myDescr.name);
     v.visitDirectiveArg("ParseTemplate", template);
     v.endDirective();
   }
 
 }
