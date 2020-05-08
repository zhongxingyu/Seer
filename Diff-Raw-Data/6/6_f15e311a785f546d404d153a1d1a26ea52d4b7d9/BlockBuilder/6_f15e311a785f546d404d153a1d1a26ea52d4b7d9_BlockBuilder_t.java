 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)
  *
  * This software is provided "as is", with NO WARRANTY, not even the
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.
  */
 
 
 package org.webmacro.engine;
 
 import java.util.*;
 
 import org.webmacro.Macro;
 import org.webmacro.Context;
 
 /**
  * A block represents the text between two {}'s in a template, or else
  * the text that begins at the start of the template and runs until its
  * end ({}'s around the whole document are not required). It contains
  * all of the other directives, strings, etc. that can be in a template.
  */
 public class BlockBuilder implements Builder {
 
    private static final int INITIAL_SIZE = 64;
 
    private static Macro[] mArray = new Macro[0];
    private static String[] sArray = new String[0];
 
    private ArrayList elements = new ArrayList();
    private int[] lineNos = new int[INITIAL_SIZE];
    private int[] colNos = new int[INITIAL_SIZE];
 
    private String name = "unknown";
 
    public BlockBuilder() {
    }
 
    public BlockBuilder(String name) {
       this.name = name;
    }
 
    public interface BlockIterator extends Iterator {
 
       public int getLineNo();
 
       public int getColNo();
    }
 
    public class BBIterator implements BlockIterator {
 
       private int size, i;
 
       public BBIterator() {
          size = elements.size();
          i = 0;
       }
 
       public boolean hasNext() {
          return (i < size);
       }
 
       public Object next() {
          return elements.get(i++);
       }
 
       public int getLineNo() {
          return lineNos[i - 1];
       }
 
       public int getColNo() {
          return colNos[i - 1];
       }
 
       public void remove() {
          throw new UnsupportedOperationException();
       }
    }
 
    final public Object build(BuildContext bc) throws BuildException {
       ArrayList strings = new ArrayList((elements.size()));
       ArrayList macros = new ArrayList((elements.size()));
       int[] ln = new int[elements.size()];
       int[] cn = new int[elements.size()];
      int pos=0;
       Stack iterStack = new Stack();
       StringBuffer s = new StringBuffer();
       Context.TemplateEvaluationContext tec = bc.getTemplateEvaluationContext();
 
       // flatten everything and view the content as being:
       //        string (macro string)* string
       // store that as an array of strings and an array of
       // Macro objects and create a block.
 
       BlockIterator iter = new BBIterator();
       tec._templateName = name;
       while (iter.hasNext()) {
          Object o = iter.next();
 
          // track line/column numbers in the build context
          // so that bc.getCurrentLocation() stays current
         tec._lineNo = ln[pos];
         tec._columnNo = cn[pos++];
 
          if (o instanceof Builder)
             o = ((Builder) o).build(bc);
 
          if (o instanceof Block) {
             iterStack.push(iter);
             iter = ((Block) o).getBlockIterator();
          }
          else {
             if (o instanceof Macro) {
                strings.add(s.toString());
                s = new StringBuffer();
                // do not reuse StringBuffer,
                // otherwise all strings will contain char[] of max length!!
                macros.add(o);
 
                // Now deal with the line numbers
                int size = macros.size();
                if (ln.length < size) {
                   ln = resizeIntArray(ln, ln.length * 2);
                   cn = resizeIntArray(cn, cn.length * 2);
                }
                ln[size - 1] = iter.getLineNo();
                cn[size - 1] = iter.getColNo();
             }
             else if (o != null) {
                s.append(o.toString());
             }
          }
          while (!iter.hasNext() && !iterStack.empty())
             iter = (BlockIterator) iterStack.pop();
       }
       strings.add(s.toString());
 
       Macro finalMacros[] = (Macro[]) macros.toArray(mArray);
       String finalStrings[] = (String[]) strings.toArray(sArray);
       int finalLines[] = resizeIntArray(ln, macros.size());
       int finalCols[] = resizeIntArray(cn, macros.size());
       return new Block(name, finalStrings, finalMacros, finalLines, finalCols);
    }
 
    private static int[] resizeIntArray(int[] ia, int size) {
       int[] temp = new int[size];
       System.arraycopy(ia, 0, temp, 0, Math.min(ia.length, size));
       return temp;
    }
 
    // Methods that look like Vector methods
 
    public void addElement(Object o) {
       elements.add(o);
    }
 
    public void addElement(Object o, int lineNo, int colNo) {
       elements.add(o);
 
       int size = elements.size();
       if (lineNos.length < size) {
          lineNos = resizeIntArray(lineNos, Math.max(lineNos.length * 2,
                                                     size + INITIAL_SIZE));
          colNos = resizeIntArray(colNos, Math.max(colNos.length * 2,
                                                   size + INITIAL_SIZE));
       }
       lineNos[size - 1] = lineNo;
       colNos[size - 1] = colNo;
    }
 
    public int size() {
       return elements.size();
    }
 
    public void remove(int i) {
       elements.remove(i);
    }
 
    public Object elementAt(int i) {
       return elements.get(i);
    }
 
    public Object setElementAt(Object o, int i) {
       return elements.set(i, o);
    }
 
 }
 
