 /**
  * Copyright (c) 2011 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.common.manifest.osgi.parser;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.eclipse.emf.common.util.EList;
 import org.sourcepit.common.manifest.Header;
 import org.sourcepit.common.manifest.Parseable;
 import org.sourcepit.common.manifest.osgi.BundleActivationPolicy;
 import org.sourcepit.common.manifest.osgi.BundleHeaderName;
 import org.sourcepit.common.manifest.osgi.BundleRequirement;
 import org.sourcepit.common.manifest.osgi.BundleSymbolicName;
 import org.sourcepit.common.manifest.osgi.ClassPathEntry;
 import org.sourcepit.common.manifest.osgi.FragmentHost;
 import org.sourcepit.common.manifest.osgi.PackageExport;
 import org.sourcepit.common.manifest.osgi.PackageImport;
 import org.sourcepit.common.manifest.osgi.Parameter;
 import org.sourcepit.common.manifest.osgi.Parameterized;
 import org.sourcepit.common.manifest.osgi.Version;
 import org.sourcepit.common.manifest.osgi.VersionRange;
 
 public class BundleHeaderParserImpl implements BundleHeaderParser
 {
    public Object parse(Parseable parseable)
    {
       if (parseable instanceof Header)
       {
          return parse((Header) parseable);
       }
       if (parseable instanceof Parameter)
       {
          return parse((Parameter) parseable);
       }
       return null;
    }
 
    protected Object parse(Header header)
    {
       final String name = header.getName();
       final String value = header.getValue();

      final BundleHeaderName headerName = BundleHeaderName.get(name);
      if (headerName == null)
      {
         return null;
      }

      switch (headerName)
       {
          case BUNDLE_ACTIVATIONPOLICY :
             return parseBundleActivationPolicy(value);
          case BUNDLE_VERSION :
             return Version.parse(value);
          case BUNDLE_SYMBOLICNAME :
             return parseBundleSymbolicName(value);
          case EXPORT_PACKAGE :
             return parseExportPackage(value);
          case IMPORT_PACKAGE :
             return parseImportPackage(value);
          case DYNAMICIMPORT_PACKAGE :
             return parseDynamicImportPackage(value);
          case REQUIRE_BUNDLE :
             return parseRequireBundle(value);
          case BUNDLE_CLASSPATH :
             return parseBundleClassPath(value);
          case FRAGMENT_HOST :
             return parseFragmentHost(value);
          default :
             return null;
       }
    }
 
    protected Object parse(Parameter parameter)
    {
       final String name = parameter.getName();
       if ("version".equals(name) || "specification-version".equals(name))
       {
          final Parameterized parameterized = parameter.getParameterized();
          if (parameterized instanceof PackageImport)
          {
             return VersionRange.parse(parameter.getValue());
          }
          if (parameterized instanceof PackageExport)
          {
             return Version.parse(parameter.getValue());
          }
       }
       else if ("singleton".equals(name))
       {
          final Parameterized parameterized = parameter.getParameterized();
          if (parameterized instanceof BundleSymbolicName)
          {
             return Boolean.valueOf(parameter.getValue());
          }
       }
       else if ("bundle-version".equals(name))
       {
          final Parameterized parameterized = parameter.getParameterized();
          if (parameterized instanceof FragmentHost || parameterized instanceof PackageImport
             || parameterized instanceof BundleRequirement)
          {
             return VersionRange.parse(parameter.getValue());
          }
       }
       return null;
    }
 
    public BundleActivationPolicy parseBundleActivationPolicy(String value)
    {
       return new ParseOperation<BundleActivationPolicy>(value)
       {
          @Override
          protected BundleActivationPolicy doParse() throws RecognitionException
          {
             return newParser().activationPolicy();
          }
       }.parse();
    }
 
    public BundleSymbolicName parseBundleSymbolicName(String value)
    {
       return new ParseOperation<BundleSymbolicName>(value)
       {
          @Override
          protected BundleSymbolicName doParse() throws RecognitionException
          {
             return newParser().symbolicName();
          }
       }.parse();
    }
 
    public FragmentHost parseFragmentHost(String value)
    {
       return new ParseOperation<FragmentHost>(value)
       {
          @Override
          protected FragmentHost doParse() throws RecognitionException
          {
             return newParser().fragmentHost();
          }
       }.parse();
    }
 
    public EList<PackageExport> parseExportPackage(String value)
    {
       return new ParseOperation<EList<PackageExport>>(value)
       {
          @Override
          protected EList<PackageExport> doParse() throws RecognitionException
          {
             return newParser().exportPackage();
          }
       }.parse();
    }
 
    public EList<PackageImport> parseImportPackage(String value)
    {
       return new ParseOperation<EList<PackageImport>>(value)
       {
          @Override
          protected EList<PackageImport> doParse() throws RecognitionException
          {
             return newParser().importPackage();
          }
       }.parse();
    }
 
    public EList<PackageImport> parseDynamicImportPackage(String value)
    {
       return parseImportPackage(value);
    }
 
    public EList<BundleRequirement> parseRequireBundle(String value)
    {
       return new ParseOperation<EList<BundleRequirement>>(value)
       {
          @Override
          protected EList<BundleRequirement> doParse() throws RecognitionException
          {
             return newParser().requireBundle();
          }
       }.parse();
    }
 
    public EList<ClassPathEntry> parseBundleClassPath(String value)
    {
       return new ParseOperation<EList<ClassPathEntry>>(value)
       {
          @Override
          protected EList<ClassPathEntry> doParse() throws RecognitionException
          {
             return newParser().classPath();
          }
       }.parse();
    }
 
    public String toString(Parseable parseable)
    {
       if (parseable instanceof Header)
       {
          return toString((Header) parseable);
       }
       if (parseable instanceof Parameter)
       {
          return toString((Parameter) parseable);
       }
       return null;
    }
 
    protected String toString(Header header)
    {
       final StringBuilder sb = new StringBuilder();
       sb.append(header.getName());
       sb.append(':');
       sb.append(' ');
       sb.append(header.getValue());
       return sb.toString();
    }
 
    protected String toString(Parameter parameter)
    {
       final StringBuilder sb = new StringBuilder();
       sb.append(parameter.getName());
       switch (parameter.getType())
       {
          case DIRECTIVE :
             sb.append(':');
          case ATTRIBUTE :
             sb.append('=');
             break;
          default :
             throw new IllegalStateException();
       }
       if (parameter.isQuoted())
       {
          sb.append('"');
          sb.append(StringEscapeUtils.escapeJava(parameter.getValue()));
          sb.append('"');
       }
       else
       {
          sb.append(parameter.getValue());
       }
       return sb.toString();
    }
 
    private static abstract class ParseOperation<R>
    {
       private final String value;
 
       private BundleManifestParser parser;
 
       public ParseOperation(String value)
       {
          this.value = value;
       }
 
       public final R parse()
       {
          try
          {
             return doParse();
          }
          catch (RecognitionException e)
          {
             final String hdr = parser.getErrorHeader(e);
             final String msg = parser.getErrorMessage(e, parser.getTokenNames());
             throw new IllegalArgumentException(hdr + " " + msg, e);
          }
       }
 
       protected abstract R doParse() throws RecognitionException;
 
       protected BundleManifestParser newParser()
       {
          return parser = new BundleManifestParser(new CommonTokenStream(new BundleManifestLexer(new ANTLRStringStream(
             value))));
       }
    }
 }
