 /*
  * Copyright 2012 jccastrejon
  *  
  * This file is part of ExSchema.
  *
  * ExSchema is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * ExSchema is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with ExSchema. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.imag.exschema;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
 
 import fr.imag.exschema.model.Set;
 
 /**
  * To be implemented by classes that can discover schemas from the source code
  * of existing applications.
  * 
  * @author jccastrejon
  * 
  */
 public interface SchemaFinder {
     /**
      * Discover schemas in the specified Java project.
      * 
      * @param project
      * @return
     * @throws JavaModelException
      */
     public List<Set> discoverSchemas(final IJavaProject project) throws CoreException;
 }
