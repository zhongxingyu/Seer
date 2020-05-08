 /*
  * Created on 11. jan.. 2007
  *
  * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
  * 
  * Licensed under the GNU General Public License, v2
  */
 package org.spoofax.interpreter.library.ssl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.spoofax.interpreter.IConstruct;
 import org.spoofax.interpreter.IContext;
 import org.spoofax.interpreter.InterpreterException;
 import org.spoofax.interpreter.Tools;
 import org.spoofax.interpreter.library.AbstractPrimitive;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 
 public class SSL_fgetc extends AbstractPrimitive {
 
     SSL_fgetc() {
         super("SSL_fgetc", 0, 1);
     }
     
     @Override
     public boolean call(IContext env, List<IConstruct> svars, IStrategoTerm[] tvars)
             throws InterpreterException {
         if(!Tools.isTermInt(tvars[0]))
             return false;
         
         SSLLibrary or = (SSLLibrary) env.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
         InputStream is = or.getInputStream(Tools.asJavaInt(tvars[0]));

        int r = -1;
         
         try {
            r = is.read();
         } catch(IOException e) {
             throw new InterpreterException(e);
         }
         
        if(r == -1)
            return false;
        
        env.setCurrent(env.getFactory().makeInt(r));
         return true;
         
     }
 
 }
