 /**
  * Copyright (c) 2011 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.common.manifest.osgi;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.sourcepit.common.manifest.osgi.parser.BundleVersionLexer;
 import org.sourcepit.common.manifest.osgi.parser.BundleVersionParser;
 
 public class VersionRange
 {
    private final Version lowVersion;
    private final boolean lowInclusive;
    private final Version highVersion;
    private final boolean highInclusive;
 
    // If a version range is specified as a single version, it must be interpreted as the range [version,∞). The
    // default for a non-specified version range is 0, which maps to [0.0.0,∞).
    public static final VersionRange INFINITE_RANGE = new VersionRange(Version.EMPTY_VERSION, true, null, false);
 
    public static VersionRange parse(String range)
    {
       if (range == null || range.length() == 0)
       {
          return INFINITE_RANGE;
       }
 
       final BundleVersionParser parser = new BundleVersionParser(new CommonTokenStream(new BundleVersionLexer(
          new ANTLRStringStream(range))));
       try
       {
          return parser.versionRange();
       }
       catch (RecognitionException e)
       {
          String hdr = parser.getErrorHeader(e);
          String msg = parser.getErrorMessage(e, parser.getTokenNames());
          throw new IllegalArgumentException(hdr + " " + msg, e);
       }
    }
 
    /**
     * Returns a <code>VersionRange</code> that is the intersection of the two supplied <code>VersionRanges</code>.
     * 
     * @param rangeOne The first <code>VersionRange</code> for the intersection
     * @param rangeTwo The second <code>VersionRange</code> for the intersection
     * @return The intersection of the two <code>VersionRanges</code>
     */
    public static VersionRange intersect(VersionRange rangeOne, VersionRange rangeTwo)
    {
       Version lowVersion;
       boolean lowInclusive;
 
       Version highVersion;
       boolean highInclusive;
 
       int lowComparison = rangeOne.lowVersion.compareTo(rangeTwo.lowVersion);
       if (lowComparison < 0)
       {
          lowVersion = rangeTwo.lowVersion;
          lowInclusive = rangeTwo.lowInclusive;
       }
       else if (lowComparison > 0)
       {
          lowVersion = rangeOne.lowVersion;
          lowInclusive = rangeOne.lowInclusive;
       }
       else
       {
          lowVersion = rangeOne.lowVersion;
          lowInclusive = rangeOne.lowInclusive && rangeTwo.lowInclusive;
       }
 
       if (rangeOne.highVersion == null)
       {
          if (rangeTwo.highVersion == null)
          {
             highVersion = null;
             highInclusive = false;
          }
          else
          {
             highVersion = rangeTwo.highVersion;
             highInclusive = rangeTwo.highInclusive;
          }
       }
       else if (rangeTwo.highVersion == null)
       {
          highVersion = rangeOne.highVersion;
          highInclusive = rangeOne.highInclusive;
       }
       else
       {
          int highComparison = rangeOne.highVersion.compareTo(rangeTwo.highVersion);
          if (highComparison > 0)
          {
             highVersion = rangeTwo.highVersion;
             highInclusive = rangeTwo.highInclusive;
          }
          else if (highComparison < 0)
          {
             highVersion = rangeOne.highVersion;
             highInclusive = rangeOne.highInclusive;
          }
          else
          {
             highVersion = rangeOne.highVersion;
             highInclusive = rangeOne.highInclusive && rangeTwo.highInclusive;
          }
       }
 
       return new VersionRange(lowVersion, lowInclusive, highVersion, highInclusive);
    }
 
    public VersionRange(Version lowVersion, boolean lowInclusive, Version highVersion, boolean highInclusive)
    {
       this.lowVersion = lowVersion;
       this.lowInclusive = lowInclusive;
       this.highVersion = highVersion;
       this.highInclusive = highInclusive;
    }
 
    public Version getLowVersion()
    {
       return lowVersion;
    }
 
    public boolean isLowInclusive()
    {
       return lowInclusive;
    }
 
    public Version getHighVersion()
    {
       return highVersion;
    }
 
    public boolean isHighInclusive()
    {
       return highInclusive;
    }
 
    public boolean includes(Version version)
    {
       if (version == null)
       {
          version = Version.EMPTY_VERSION;
       }
       final int minCheck = lowInclusive ? 0 : 1;
       final int maxCheck = highInclusive ? 0 : -1;
       if (lowVersion == null)
       {
          throw new IllegalStateException("Low version may not be null.");
       }
       return version.compareTo(lowVersion) >= minCheck
          && (highVersion == null || version.compareTo(highVersion) <= maxCheck);
    }
 
    @Override
    public int hashCode()
    {
       final int prime = 31;
       int result = 1;
       result = prime * result + (highInclusive ? 1231 : 1237);
       result = prime * result + ((highVersion == null) ? 0 : highVersion.hashCode());
       result = prime * result + (lowInclusive ? 1231 : 1237);
       result = prime * result + ((lowVersion == null) ? 0 : lowVersion.hashCode());
       return result;
    }
 
    // CSOFF generated code
    @Override
    public boolean equals(Object obj)
    {
       if (this == obj)
          return true;
       if (obj == null)
          return false;
       if (getClass() != obj.getClass())
          return false;
       VersionRange other = (VersionRange) obj;
       if (highInclusive != other.highInclusive)
          return false;
       if (highVersion == null)
       {
          if (other.highVersion != null)
             return false;
       }
       else if (!highVersion.equals(other.highVersion))
          return false;
       if (lowInclusive != other.lowInclusive)
          return false;
       if (lowVersion == null)
       {
          if (other.lowVersion != null)
             return false;
       }
       else if (!lowVersion.equals(other.lowVersion))
          return false;
       return true;
    } // CSON
 
    public String toString()
    {
       if (highVersion != null)
       {
          StringBuffer sb = new StringBuffer();
          sb.append(lowInclusive ? '[' : '(');
         sb.append(lowVersion.toMinimalString());
          sb.append(',');
          sb.append(highVersion.toMinimalString());
          sb.append(highInclusive ? ']' : ')');
          return sb.toString();
       }
       else
       {
          return lowVersion.toMinimalString();
       }
    }
 }
