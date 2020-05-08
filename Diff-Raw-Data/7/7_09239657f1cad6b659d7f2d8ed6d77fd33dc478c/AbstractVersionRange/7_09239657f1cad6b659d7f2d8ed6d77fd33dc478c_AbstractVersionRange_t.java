 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
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
 package org.jboss.osgi.plugins.metadata;
 
 import java.io.ObjectStreamException;
 import java.io.Serializable;
 import java.util.StringTokenizer;
 
 import org.jboss.osgi.spi.metadata.VersionRange;
 import org.osgi.framework.Version;
 
 /**
  * Represents an OSGi version range:
  * version-range ::= interval | atleast
  * interval ::= ( '[' | '(' ) floor ',' ceiling ( ']' | ')' )
  * atleast ::= version
  * floor ::= version
  * ceiling ::= version
  *
  * @author Scott.Stark@jboss.org
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class AbstractVersionRange implements VersionRange, Serializable
 {
    private static final long serialVersionUID = 1l;
 
    /**
     * The lower bound of the range
     */
    private transient Version floor;
    /**
     * The upper bound of the range
     */
    private transient Version ceiling;
    /**
     * is the floor version a >(true) or >= constraint(false)
     */
    private transient boolean floorIsGreaterThan;
    /**
     * is the ceiling version a <(true) or <= constraint(false)
     */
    private transient boolean ceilingIsLessThan;
    /**
     * create object from this after serialization
     */
    private String rangeSpec;
 
    public static VersionRange parseRangeSpec(String rangeSpec)
    {
       Version floor = null;
       Version ceiling = null;
       StringTokenizer st = new StringTokenizer(rangeSpec, ",[]()", true);
       Boolean floorIsGreaterThan = null;
       Boolean ceilingIsLessThan = null;
       while (st.hasMoreTokens())
       {
          String token = st.nextToken();
          if (token.equals("["))
             floorIsGreaterThan = false;
          else if (token.equals("("))
             floorIsGreaterThan = true;
          else if (token.equals("]"))
             ceilingIsLessThan = false;
          else if (token.equals(")"))
             ceilingIsLessThan = true;
          else if (token.equals(",") == false)
          {
             // A version token
             if (floor == null)
                floor = new Version(token);
             else
                ceiling = new Version(token);
          }
 
       }
       // check for parenthesis
       if (floorIsGreaterThan == null || ceilingIsLessThan == null)
          throw new IllegalArgumentException("Missing parenthesis: " + rangeSpec);
 
       return new AbstractVersionRange(rangeSpec, floor, ceiling, floorIsGreaterThan, ceilingIsLessThan);
    }
 
    public AbstractVersionRange(String rangeSpec, Version floor, Version ceiling, boolean floorIsLessThan, boolean ceilingIsLessThan)
    {
       this.rangeSpec = rangeSpec;
       this.floor = floor;
       this.ceiling = ceiling;
       this.floorIsGreaterThan = floorIsLessThan;
       this.ceilingIsLessThan = ceilingIsLessThan;
    }
 
    public Version getFloor()
    {
       return floor;
    }
 
    public Version getCeiling()
    {
       return ceiling;
    }
 
    public boolean isInRange(Version v)
    {
       // Test a null floor version which implies 0.0.0 <= v <= inf
       boolean isInRange = floor == null;
       if (isInRange == false)
       {
          // Test the floor version
          int floorCompare = v.compareTo(floor);
          if ((floorIsGreaterThan && floorCompare > 0) || (floorIsGreaterThan == false && floorCompare >= 0))
          {
             isInRange = ceiling == null;
             if (isInRange == false)
             {
                // Test the ceiling version
                int ceilingCompare = v.compareTo(ceiling);
                if ((ceilingIsLessThan && ceilingCompare < 0) || (ceilingIsLessThan == false && ceilingCompare <= 0))
                {
                   isInRange = true;
                }
             }
          }
       }
       return isInRange;
    }
 
    public String toString()
    {
       StringBuilder tmp = new StringBuilder();
       if (floor == null)
          tmp.append("0.0.0");
       else
          tmp.append(floor.toString());
       tmp.append(" <");
       if (floorIsGreaterThan == false)
          tmp.append('=');
       tmp.append(" v ");
       // Ceiling
       tmp.append('<');
       if (ceilingIsLessThan == false)
          tmp.append('=');
       tmp.append(' ');
       if (ceiling == null)
          tmp.append("inf");
       else
          tmp.append(ceiling.toString());
       return tmp.toString();
    }
 
    protected Object readResolve() throws ObjectStreamException
    {
       return parseRangeSpec(rangeSpec);
    }
 
 }
