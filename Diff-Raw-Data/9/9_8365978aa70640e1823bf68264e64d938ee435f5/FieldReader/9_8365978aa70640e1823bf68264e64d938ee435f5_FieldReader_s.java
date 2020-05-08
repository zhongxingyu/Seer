 /*
  * Copyright (C) 2009 - 2012 SMVP4G.COM
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package com.smvp4g.reflection.scan.reader;
 
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JField;
 import com.smvp4g.generator.scan.model.FieldScanModel;
 import com.smvp4g.generator.scan.reader.Reader;
 import com.smvp4g.generator.scan.utils.ScanUtils;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The Class FieldReader.
  *
  * @author Nguyen Duc Dung
  * @since 12/4/11, 10:32 AM
  */
 public class FieldReader implements Reader<FieldScanModel> {
 
     private List<FieldScanModel> models = new ArrayList<FieldScanModel>();
 
     @Override
     public void read(JClassType classType, GeneratorContext context) {
         for (JField field : classType.getFields()) {
            if (field.isPublic() && !field.isFinal()) {
                 FieldScanModel model = new FieldScanModel();
                 model.setName(field.getName());
                 if (field.getType().isPrimitive() != null) {
                     model.setTypeClassName(field.getType().isPrimitive()
                             .getQualifiedBoxedSourceName());
                 } else {
                     model.setTypeClassName(field.getType().getQualifiedSourceName());
                 }
                 model.setAnnotationScanModels(ScanUtils.getAnnotations(field));
                 models.add(model);
             }
         }
     }
 
     @Override
     public boolean isMath(Annotation checkAnnotation) {
         return true;
     }
 
     @Override
     public List<FieldScanModel> getData() {
         return models;
     }
 }
