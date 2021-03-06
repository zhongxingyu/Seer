 /*
  * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the Lesser GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Lesser GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.fge.uritemplate.parse;
 
 import com.github.fge.uritemplate.URITemplateParseException;
 import com.github.fge.uritemplate.expression.ExpressionType;
 import com.github.fge.uritemplate.expression.TemplateExpression;
 import com.github.fge.uritemplate.expression.URITemplateExpression;
 import com.github.fge.uritemplate.vars.VariableSpec;
 import com.google.common.base.CharMatcher;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 
 import java.nio.CharBuffer;
 import java.util.List;
 import java.util.Map;
 
 import static com.github.fge.uritemplate.ExceptionMessages.*;
 
 public final class ExpressionParser
     implements TemplateParser
 {
     private static final Map<Character, ExpressionType> EXPRESSION_TYPE_MAP;
     private static final CharMatcher COMMA = CharMatcher.is(',');
     private static final CharMatcher END_EXPRESSION = CharMatcher.is('}');
 
     static {
         final ImmutableMap.Builder<Character, ExpressionType> builder
             = ImmutableMap.builder();
 
         char c;
         ExpressionType type;
 
         c = '+';
         type = ExpressionType.RESERVED;
         builder.put(c, type);
 
         c = '#';
         type = ExpressionType.FRAGMENT;
         builder.put(c, type);
 
         c = '.';
         type = ExpressionType.NAME_LABELS;
         builder.put(c, type);
 
         c = '/';
         type = ExpressionType.PATH_SEGMENTS;
         builder.put(c, type);
 
         c = ';';
         type = ExpressionType.PATH_PARAMETERS;
         builder.put(c, type);
 
         c = '?';
         type = ExpressionType.QUERY_STRING;
         builder.put(c, type);
 
         c = '&';
         type = ExpressionType.QUERY_CONT;
         builder.put(c, type);
 
         EXPRESSION_TYPE_MAP = builder.build();
     }
 
     @Override
     public URITemplateExpression parse(final CharBuffer buffer)
         throws URITemplateParseException
     {
         // Swallow the '{'
         buffer.get();
 
         /*
          * Error if the buffer is empty after that
          */
         if (!buffer.hasRemaining())
            throw new URITemplateParseException(UNEXPECTED_EOF, buffer,
                true);
 
         /*
          * If the next char is a known expression type, swallow it; otherwise,
          * select SIMPLE.
          */
         ExpressionType type = ExpressionType.SIMPLE;
         char c = buffer.charAt(0);
         if (EXPRESSION_TYPE_MAP.containsKey(c))
             type = EXPRESSION_TYPE_MAP.get(buffer.get());
 
         /*
          * Now, swallow varspec by varspec.
          */
         final List<VariableSpec> varspecs = Lists.newArrayList();
 
         while (true) {
             /*
              * Swallow one varspec
              */
             varspecs.add(VariableSpecParser.parse(buffer));
             /*
              * Error if the buffer is empty after that
              */
             if (!buffer.hasRemaining())
                 throw new URITemplateParseException(UNEXPECTED_EOF, buffer,
                     true);
             /*
              * Grab next character
              */
             c = buffer.get();
             /*
              * If it is a comma, swallow next varspec
              */
             if (COMMA.matches(c))
                 continue;
             /*
              * If it is a closing bracket, we're done
              */
             if (END_EXPRESSION.matches(c))
                 break;
             /*
              * If we reach this point, this is an error
              */
            throw new URITemplateParseException(UNEXPECTED_TOKEN, buffer,
                true);
         }
 
         return new TemplateExpression(type, varspecs);
     }
 }
