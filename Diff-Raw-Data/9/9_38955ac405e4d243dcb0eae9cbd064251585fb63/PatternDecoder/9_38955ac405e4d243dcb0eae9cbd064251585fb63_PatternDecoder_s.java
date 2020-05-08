 /*
  * Sonar Switch Off Violations Plugin
  * Copyright (C) 2011 SonarSource
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.switchoffviolations;
 
 import com.google.common.collect.Lists;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.sonar.api.utils.SonarException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 final class PatternDecoder {
 
   static final String LINE_RANGE_REGEXP = "\\[((\\d+|\\d+-\\d+),?)*\\]";
 
   Pattern decodeLine(String line) {
     if (isBlankOrComment(line)) {
       return null;
     }
 
     String[] fields = StringUtils.split(line, ';');
     if (fields.length != 3) {
       throw new SonarException("Unvalid format. The following line does not define 3 fields separated by comma: " + line);
     }
 
     if (!isResource(fields[0])) {
       throw new SonarException("Unvalid format. The first field does not define a resource pattern: " + line);
     }
     if (!isRule(fields[1])) {
       throw new SonarException("Unvalid format. The second field does not define a rule pattern: " + line);
     }
     if (!isLinesRange(fields[2])) {
       throw new SonarException("Unvalid format. The third field does not define a range of lines: " + line);
     }
 
     Pattern pattern = new Pattern(StringUtils.trim(fields[0]), StringUtils.trim(fields[1]));
     decodeRangeOfLines(pattern, fields[2]);
 
     return pattern;
   }
 
   void decodeRangeOfLines(Pattern pattern, String field) {
     if (StringUtils.equals(field, "*")) {
       pattern.setCheckLines(false);
     } else {
       pattern.setCheckLines(true);
       String s = StringUtils.substringBetween(StringUtils.trim(field), "[", "]");
       String[] parts = StringUtils.split(s, ',');
       for (String part : parts) {
         if (StringUtils.contains(part, '-')) {
           String[] range = StringUtils.split(part, '-');
           pattern.addLineRange(Integer.valueOf(range[0]), Integer.valueOf(range[1]));
         } else {
           pattern.addLine(Integer.valueOf(part));
         }
       }
     }
   }
 
   boolean isLinesRange(String field) {
     return StringUtils.equals(field, "*") || java.util.regex.Pattern.matches(LINE_RANGE_REGEXP, field);
   }
 
   boolean isBlankOrComment(String line) {
     return StringUtils.isBlank(line) || StringUtils.startsWith(line, "#");
   }
 
   boolean isResource(String field) {
     return StringUtils.isNotBlank(field);
   }
 
   boolean isRule(String field) {
     return StringUtils.isNotBlank(field);
   }
 
   List<Pattern> decodeFile(File file) {
     try {
       List<String> lines = FileUtils.readLines(file);
       List<Pattern> patterns = Lists.newLinkedList();
       for (String line : lines) {
         Pattern pattern = decodeLine(line);
         if (pattern != null) {
           patterns.add(pattern);
         }
       }
       return patterns;
 
     } catch (IOException e) {
      throw new SonarException("Fail to load the file: " + file.getAbsolutePath());
     }
   }
 }
