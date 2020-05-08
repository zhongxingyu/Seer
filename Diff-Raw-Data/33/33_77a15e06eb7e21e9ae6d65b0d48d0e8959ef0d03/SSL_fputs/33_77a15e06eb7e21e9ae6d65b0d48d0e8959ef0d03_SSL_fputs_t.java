 /*
  * Created on 08.aug.2005
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.library.ssl;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import org.spoofax.interpreter.IConstruct;
 import org.spoofax.interpreter.IContext;
 import org.spoofax.interpreter.InterpreterException;
 import org.spoofax.interpreter.Tools;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class SSL_fputs extends AbstractPrimitive {
 
     protected SSL_fputs() {
         super("SSL_fputs", 0, 2);
     }
     
     public boolean call(IContext env, List<IConstruct> sargs, IStrategoTerm[] targs) throws InterpreterException {
         
         if(!Tools.isTermString(targs[0]))
             return false;
         if(!(Tools.isTermInt(targs[1])))
             return false;
 
         SSLLibrary or = (SSLLibrary) env.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
         
         OutputStream ous = or.getOutputStream(Tools.asJavaInt(targs[1]));
         try {
             ous.write(Tools.javaString(targs[0]).getBytes());
         } catch(IOException e) {
             throw new InterpreterException(e);
         }
         
        env.setCurrent(targs[1]);
        
         return true;
     }
 }
