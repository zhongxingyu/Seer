 // Copyright 2002 Finn Bock
 
 package org.python.core;
 
 public class PyGenerator extends PyIterator {
     public PyFrame gi_frame;
     PyObject closure;
     public boolean gi_running;
     private PyException generatorExit;
 
     public PyGenerator(PyFrame frame, PyObject closure) {
         this.gi_frame = frame;
         this.closure = closure;
         this.gi_running = false;
         // Create an exception instance while we have a frame to create it from.
         // When the GC runs it doesn't have any associated thread state.
         // this is necessary for finalize calling close on the generator
         this.generatorExit = Py.makeException(Py.GeneratorExit);
     }
 
     private static final String[] __members__ = {
         "close", "gi_frame", "gi_running", "next", "send", "throw"
     };
 
     public PyObject __dir__() {
         PyString members[] = new PyString[__members__.length];
         for (int i = 0; i < __members__.length; i++)
             members[i] = new PyString(__members__[i]);
         PyList ret = new PyList(members);
         PyDictionary accum = new PyDictionary();
         addKeys(accum, "__dict__");
         ret.extend(accum.keys());
         ret.sort();
         return ret;
     }
 
     public PyObject send(PyObject value) {
         if (gi_frame == null) {
             throw Py.StopIteration("");
         }
         if (gi_frame.f_lasti == 0 && value != Py.None) {
             throw Py.TypeError("can't send non-None value to a just-started generator");
         }
         gi_frame.setGeneratorInput(value);
         return next();
     }
 
     private PyObject raiseException(PyException ex) {
        if (gi_frame == null || gi_frame.f_lasti == 0) {
            throw ex;
        }
         gi_frame.setGeneratorInput(ex);
         return next();
     }
 
     public PyObject throw$(PyObject type) {
         return raiseException(Py.makeException(type));
     }
 
     public PyObject throw$(PyObject type, PyObject value) {
         return raiseException(Py.makeException(type, value));
     }
 
     public PyObject throw$(PyObject type, PyObject value, PyTraceback tb) {
         return raiseException(Py.makeException(type, value, tb));
     }
 
     public PyObject close() {
         try {
             raiseException(generatorExit);
             throw Py.RuntimeError("generator ignored GeneratorExit");
         } catch (PyException e) {
             if (!(e.type == Py.StopIteration || e.type == Py.GeneratorExit)) {
                 throw e;
             }
         }
         return Py.None;
     }
 
     protected void finalize() throws Throwable {
         if (gi_frame == null || gi_frame.f_lasti == -1) 
             return;
         try {
             close();
         } catch (Throwable e) {
         } finally {
             super.finalize();
         }
     }
 
     public PyObject __iternext__() {
         if (gi_running)
             throw Py.ValueError("generator already executing");
         if (gi_frame == null) {
             return null;
         }
             
         if (gi_frame.f_lasti == -1) {
             gi_frame = null;
             return null;
         }
         gi_running = true;
         PyObject result = null;
         try {
             result = gi_frame.f_code.call(gi_frame, closure);
         } catch(PyException e) {
             if (!(e.type == Py.StopIteration || e.type == Py.GeneratorExit)) {
                 gi_frame = null;
                 throw e;
             } else {
                 stopException = e;
                 gi_frame = null;
                 return null;
             }
         } finally {
             gi_running = false;
         }
         if (result == Py.None && gi_frame.f_lasti == -1) {
             return null;
         }
         return result;
     }
 }
