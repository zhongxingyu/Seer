 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.adapters.api;
 
 import com.github.fhirschmann.clozegen.lib.generators.model.URLBasedModel;
 import com.google.common.io.Resources;
 import com.google.common.reflect.TypeToken;
 import java.io.IOException;
 import org.uimafit.descriptor.ConfigurationParameter;
 
 /**
  * Extending classes will have to declare a model which extends {@link URLBasedModel} of
  * which a new instance will be created.
  *
  * <p>
  * The {@link URLBasedModelAdapter#model} will by loaded by calling
  * {@link URLBasedModel#load(URL)}. This works by creating a new instance
  * of {@code <M>}.
  * </p>
  *
  * @param <M> a model which extends {@link URLBasedModel}
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class URLBasedModelAdapter<M extends URLBasedModel> extends AbstractResource {
     /**
      * The underlying model. This variable can be used in extending classes.
      */
     protected M model;
 
     /**
      * The path to the model. This parameter will be passed to
      * {@link URLBasedModel#load(URL)}.
      */
     public static final String PARAM_PATH = "Path";
     @ConfigurationParameter(name = PARAM_PATH, mandatory = true)
     private String path;
 
     @SuppressWarnings("unchecked")
     @Override
     public boolean initialize() {
         @SuppressWarnings("serial")
         TypeToken<M> type = new TypeToken<M>(getClass()) {};
         try {
             model = (M) type.getRawType().newInstance();
             model.load(Resources.getResource(path));
             return true;
         } catch (InstantiationException ex) {
             getLogger().error(ex);
         } catch (IllegalAccessException ex) {
             getLogger().error(ex);
         } catch (IOException ex) {
             getLogger().error(ex);
         }
         return false;
     }
 }
