 /**
  * Copyright (C) 2013 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcgen4j.core.velocity;
 
 import java.io.File;
 import java.util.Map;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.fuin.srcgen4j.commons.AbstractElement;
 import org.fuin.srcgen4j.commons.GeneratorConfig;
 import org.fuin.srcgen4j.commons.InitializableElement;
 
 /**
 * Configuration for a {@link VelocityProducerParser}.
  */
 @XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "velocity-producer-parser")
 public class VelocityProducerParserConfig extends AbstractElement implements
         InitializableElement<VelocityProducerParserConfig, GeneratorConfig> {
 
     @XmlAttribute(name = "modelPath")
     private String modelPath;
 
     /**
      * Default constructor.
      */
     public VelocityProducerParserConfig() {
         super();
     }
 
     /**
      * Constructor with model path.
      * 
      * @param modelPath
      *            Model path.
      */
     public VelocityProducerParserConfig(final String modelPath) {
         super();
         this.modelPath = modelPath;
     }
 
     /**
      * Returns the model path.
      * 
      * @return Model path.
      */
     public final String getModelPath() {
         return modelPath;
     }
 
     /**
      * Returns the model directory.
      * 
      * @return Model directory or NULL.
      */
     public final File getModelDir() {
         if (modelPath == null) {
             return null;
         }
         return new File(modelPath);
     }
 
     /**
      * Sets the model path to a new value.
      * 
      * @param modelPath
      *            Model path to set.
      */
     public final void setModelPath(final String modelPath) {
         this.modelPath = modelPath;
     }
 
     @Override
     public final VelocityProducerParserConfig init(final GeneratorConfig parent,
             final Map<String, String> vars) {
         setModelPath(replaceVars(getModelPath(), vars));
         return this;
     }
 
 }
