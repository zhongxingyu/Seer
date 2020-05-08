 /**
  *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
  *  All rights reserved.
  *  Redistribution and use in source and binary forms, with or without
  *  modification, are permitted provided that the following conditions are met:
  *
  *      * Redistributions of source code must retain the above copyright
  *        notice, this list of conditions and the following disclaimer.
  *      * Redistributions in binary form must reproduce the above copyright
  *        notice, this list of conditions and the following disclaimer in the
  *        documentation and/or other materials provided with the distribution.
  *      * Neither the name of the Ecole des Mines de Nantes nor the
  *        names of its contributors may be used to endorse or promote products
  *        derived from this software without specific prior written permission.
  *
  *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
  *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
  *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package choco.annotations;
 
 import javax.annotation.processing.*;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.util.Elements;
 import javax.tools.StandardLocation;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Set;
 
 import static javax.tools.Diagnostic.Kind;
 
 /**
  * <br/>
  *
  * @author Charles Prud'homme
  * @since 07/11/11
  */
 @SupportedAnnotationTypes({"*"})
 public class PropagatorAnnotationProcessor extends AbstractProcessor {
 
     public boolean process(
             Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
 
 
         Messager messager = processingEnv.getMessager();
         Filer filer = processingEnv.getFiler();
         Elements eltUtils = processingEnv.getElementUtils();
         if (!roundEnvironment.processingOver()) {
             TypeElement elementTodo =
                     eltUtils.getTypeElement("choco.annotations.PropAnn");
             Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(elementTodo);
             messager.printMessage(Kind.NOTE, "<< start annotations processing >>");
 
 
             if (!elements.isEmpty()) {
                 StringBuilder st = new StringBuilder("\nPropagators:");
 
                 for (Element element : elements) {
                     PropAnn pa = element.getAnnotation(PropAnn.class);
                     st.append(String.format("\n> %s : %s", element.getSimpleName(), Arrays.toString(pa.tested())));
                 }
                 try {
                     PrintWriter pw = new PrintWriter(filer.createResource(
                             StandardLocation.SOURCE_OUTPUT,
                             "", "propagators.txt")
                             .openOutputStream());
 
                     messager.printMessage(Kind.NOTE, st.toString());
                     pw.println(st.toString());
 
 
                     pw.close();
                 } catch (IOException ioe) {
                     messager.printMessage(Kind.ERROR, ioe.getMessage());
                 }
             }
         } else {
             messager.printMessage(Kind.NOTE, "<< end annotations processing >>");
         }
 
         return true;
     }
 }
