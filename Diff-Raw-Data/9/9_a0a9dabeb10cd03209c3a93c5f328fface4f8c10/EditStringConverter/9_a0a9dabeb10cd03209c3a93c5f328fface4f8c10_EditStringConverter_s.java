 /*
  * jPOS Presentation Manager [http://jpospm.blogspot.com]
  * Copyright (C) 2010 Jeronimo Paoletti [jeronimo.paoletti@gmail.com]
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.struts.converter;
 
 import org.jpos.ee.pm.converter.ConverterException;
 import org.jpos.ee.pm.core.EntityInstanceWrapper;
 import org.jpos.ee.pm.core.Field;
 import org.jpos.ee.pm.core.PMContext;
 
 public class EditStringConverter extends StrutsEditConverter {
 
     public Object build(PMContext ctx) throws ConverterException {
         Object value =  ctx.get(PM_FIELD_VALUE);
         return (value !=null)?value.toString():null;
     }
     
     public String visualize(PMContext ctx) throws ConverterException {
         EntityInstanceWrapper einstance = (EntityInstanceWrapper) ctx.get(PM_ENTITY_INSTANCE_WRAPPER);
         Field field = (Field) ctx.get(PM_FIELD);
         Object p = getNestedProperty (einstance.getInstance(), field.getId());
        return super.visualize("string_converter.jsp?value="+normalize((p==null)?"":p.toString()));
     }
 
     public String normalize (String s) {
         StringBuffer str = new StringBuffer();
 
         int len = (s != null) ? s.length() : 0;
         for (int i = 0; i < len; i++) {
             char ch = s.charAt(i);
             switch (ch) {
                 case '<': 
                     str.append("&lt;");
                     break;
                 case '>': 
                     str.append("&gt;");
                     break;
                 case '&': 
                     str.append("&amp;");
                     break;
 /*
                 case '"': 
                     str.append("&quot;");
                     break;
 */
                 case '\'': 
                     str.append("&apos;");
                     break;
                 case '\r':
                 case '\n': 
                     str.append("&#x");
                     str.append(Integer.toHexString((int) (ch & 0xFF)));
                     str.append(';');
                     break;
                 default: 
                     if (ch < 0x20) {
                         str.append("&#x");
                         str.append(Integer.toHexString((int) (ch & 0xFF)));
                         str.append(';');
                     }
                     else if (ch > 0xff00) {
                         str.append((char) (ch & 0xFF));
                     } else
                         str.append(ch);
             }
         }
         return (str.toString());
     }
 }
