 package org.drools.compiler;
 
 /*
  * Copyright 2005 JBoss Inc
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.jci.problems.CompilationProblem;
 import org.codehaus.jfdi.interpreter.ClassTypeResolver;
 import org.codehaus.jfdi.interpreter.TypeResolver;
 import org.drools.base.ClassFieldExtractorCache;
 import org.drools.facttemplates.FactTemplate;
 import org.drools.facttemplates.FactTemplateImpl;
 import org.drools.facttemplates.FieldTemplate;
 import org.drools.facttemplates.FieldTemplateImpl;
 import org.drools.lang.descr.BaseDescr;
 import org.drools.lang.descr.FactTemplateDescr;
 import org.drools.lang.descr.FieldTemplateDescr;
 import org.drools.lang.descr.FunctionDescr;
 import org.drools.lang.descr.FunctionImportDescr;
 import org.drools.lang.descr.GlobalDescr;
 import org.drools.lang.descr.ImportDescr;
 import org.drools.lang.descr.PackageDescr;
 import org.drools.lang.descr.RuleDescr;
 import org.drools.rule.Package;
 import org.drools.rule.Rule;
 import org.drools.rule.builder.Dialect;
 import org.drools.rule.builder.RuleBuilder;
 import org.drools.rule.builder.dialect.java.JavaDialect;
 import org.drools.xml.XmlPackageReader;
 import org.xml.sax.SAXException;
 
 /**
  * This is the main compiler class for parsing and compiling rules and assembling or merging them into a
  * binary Package instance.
  * This can be done by merging into existing binary packages, or totally from source.
  */
 public class PackageBuilder {
 
     private Package                     pkg;
 
     private List                        results;
 
     private PackageBuilderConfiguration configuration;
 
     private TypeResolver                typeResolver;
 
     private ClassFieldExtractorCache    classFieldExtractorCache;
 
     private RuleBuilder                 builder;
 
     private Dialect                     dialect;
 
     private DialectRegistry             dialects;
 
     /**
      * Use this when package is starting from scratch. 
      */
     public PackageBuilder() {
         this( null,
               null );
     }
 
     /**
      * This will allow you to merge rules into this pre existing package.
      */
     public PackageBuilder(final Package pkg) {
         this( pkg,
               null );
     }
 
     public PackageBuilder(final PackageBuilderConfiguration configuration) {
         this( null,
               configuration );
     }
 
     /**
      * This allows you to pass in a pre existing package, and a configuration (for instance to set the classloader).
      * @param pkg A pre existing package (can be null if none exists)
      * @param configuration Optional configuration for this builder.
      */
     public PackageBuilder(final Package pkg,
                           PackageBuilderConfiguration configuration) {
         if ( configuration == null ) {
             configuration = new PackageBuilderConfiguration();
         }
 
         this.configuration = configuration;
         this.results = new ArrayList();
         this.pkg = pkg;
         this.classFieldExtractorCache = new ClassFieldExtractorCache();
 
         this.dialects = new DialectRegistry();
         this.dialects.addDialect( "java",
                                   new JavaDialect( pkg,
                                                    configuration ) );
 
         this.dialect = this.dialects.getDialect( "java" );
     }
 
     /**
      * Load a rule package from DRL source.
      * @param reader
      * @throws DroolsParserException
      * @throws IOException
      */
     public void addPackageFromDrl(final Reader reader) throws DroolsParserException,
                                                       IOException {
         final DrlParser parser = new DrlParser();
         final PackageDescr pkg = parser.parse( reader );
         this.results.addAll( parser.getErrors() );
         addPackage( pkg );
     }
 
     /**
      * Load a rule package from XML source.
      * @param reader
      * @throws DroolsParserException
      * @throws IOException
      */
     public void addPackageFromXml(final Reader reader) throws DroolsParserException,
                                                       IOException {
         final XmlPackageReader xmlReader = new XmlPackageReader();
 
         try {
             xmlReader.read( reader );
         } catch ( final SAXException e ) {
             throw new DroolsParserException( e.toString(),
                                              e.getCause() );
         }
 
         addPackage( xmlReader.getPackageDescr() );
     }
 
     /**
      * Load a rule package from DRL source using the supplied DSL configuration.
      * @param source The source of the rules.
      * @param dsl The source of the domain specific language configuration.
      * @throws DroolsParserException
      * @throws IOException
      */
     public void addPackageFromDrl(final Reader source,
                                   final Reader dsl) throws DroolsParserException,
                                                    IOException {
         final DrlParser parser = new DrlParser();
         final PackageDescr pkg = parser.parse( source,
                                                dsl );
         this.results.addAll( parser.getErrors() );
         addPackage( pkg );
     }
 
     /** 
      * This adds a package from a Descr/AST 
      * This will also trigger a compile, if there are any generated classes to compile
      * of course.
      */
     public void addPackage(final PackageDescr packageDescr) {
 
         validatePackageName( packageDescr );
         validateUniqueRuleNames( packageDescr );
 
         if ( this.pkg != null ) {
             //mergePackage( packageDescr ) ;
             mergePackage( this.pkg,
                           packageDescr );
         } else {
             this.pkg = newPackage( packageDescr );
         }
 
         this.builder = new RuleBuilder( getTypeResolver(),
                                    this.classFieldExtractorCache,
                                    this.dialect );
 
         //only try to compile if there are no parse errors
         if ( !hasErrors() ) {
             for ( final Iterator it = packageDescr.getFactTemplates().iterator(); it.hasNext(); ) {
                 addFactTemplate( (FactTemplateDescr) it.next() );
             }
 
             //iterate and compile
             for ( final Iterator it = packageDescr.getFunctions().iterator(); it.hasNext(); ) {
                 addFunction( (FunctionDescr) it.next() );
             }
 
             //iterate and compile
             for ( final Iterator it = packageDescr.getRules().iterator(); it.hasNext(); ) {
                 addRule( (RuleDescr) it.next() );
             }
         }
 
         this.dialect.compileAll();
 
         this.results.addAll( this.dialect.getResults() );
     }
 
     private void validatePackageName(final PackageDescr packageDescr) {
         if (this.pkg != null) {
             return;
         }
         if ( packageDescr.getName() == null || "".equals( packageDescr.getName() ) ) {
 
             throw new MissingPackageNameException( "Missing package name for rule package." );
         }
     }
 
     private void validateUniqueRuleNames(final PackageDescr packageDescr) {
         final Set names = new HashSet();
         for ( final Iterator iter = packageDescr.getRules().iterator(); iter.hasNext(); ) {
             final RuleDescr rule = (RuleDescr) iter.next();
             final String name = rule.getName();
             if ( names.contains( name ) ) {
                 this.results.add( new ParserError( "Duplicate rule name: " + name,
                                                    rule.getLine(),
                                                    rule.getColumn() ) );
             }
             names.add( name );
         }
     }
 
     private Package newPackage(final PackageDescr packageDescr) {
         final Package pkg = new Package( packageDescr.getName(),
                                          this.configuration.getClassLoader() );
 
         this.dialect.init( pkg );
 
         mergePackage( pkg,
                       packageDescr );
 
         return pkg;
     }
 
     private void mergePackage(final Package pkg,
                               final PackageDescr packageDescr) {
         final List imports = packageDescr.getImports();
         for ( final Iterator it = imports.iterator(); it.hasNext(); ) {
             pkg.addImport( ((ImportDescr) it.next()).getTarget() );
         }
 
         for ( final Iterator it = packageDescr.getFunctionImports().iterator(); it.hasNext(); ) {
             pkg.addStaticImport( ((FunctionImportDescr) it.next()).getTarget() );
         }
 
         final TypeResolver typeResolver = new ClassTypeResolver( pkg.getImports(),
                                                                  pkg.getPackageCompilationData().getClassLoader() );
 
         final List globals = packageDescr.getGlobals();
         for ( final Iterator it = globals.iterator(); it.hasNext(); ) {
             final GlobalDescr global = (GlobalDescr) it.next();
             final String identifier = global.getIdentifier();
             final String className = global.getType();
 
             Class clazz;
             try {
                 clazz = typeResolver.resolveType( className );
                 pkg.addGlobal( identifier,
                                clazz );
             } catch ( final ClassNotFoundException e ) {
                 new GlobalError( identifier );
             }
         }
     }
 
     private void addFunction(final FunctionDescr functionDescr) {
         this.dialect.addFunction( functionDescr,
                                   getTypeResolver() );
     }
 
     private void addFactTemplate(final FactTemplateDescr factTemplateDescr) {
         final List fields = new ArrayList();
         int index = 0;
         for ( final Iterator it = factTemplateDescr.getFields().iterator(); it.hasNext(); ) {
             final FieldTemplateDescr fieldTemplateDescr = (FieldTemplateDescr) it.next();
             FieldTemplate fieldTemplate = null;
             try {
                 fieldTemplate = new FieldTemplateImpl( fieldTemplateDescr.getName(),
                                                        index++,
                                                        getTypeResolver().resolveType( fieldTemplateDescr.getClassType() ) );
             } catch ( final ClassNotFoundException e ) {
                 this.results.add( new FieldTemplateError( this.pkg,
                                                           fieldTemplateDescr,
                                                           null,
                                                           "Unable to resolve Class '" + fieldTemplateDescr.getClassType() + "'" ) );
             }
             fields.add( fieldTemplate );
         }
 
         final FactTemplate factTemplate = new FactTemplateImpl( this.pkg,
                                                                 factTemplateDescr.getName(),
                                                                 (FieldTemplate[]) fields.toArray( new FieldTemplate[fields.size()] ) );
     }
 
     private void addRule(final RuleDescr ruleDescr) {
         this.dialect.init( ruleDescr );
 
         this.builder.build( this.pkg,
                        ruleDescr );
 
         this.results.addAll( this.builder.getErrors() );
 
         final Rule rule = this.builder.getRule();
 
         this.dialect.addRuleSemantics( this.builder,
                                        rule,
                                        ruleDescr );
 
         this.pkg.addRule( rule );
     }
 
     /**
      * @return a Type resolver, lazily. 
      * If one does not exist yet, it will be initialised.
      */
     private TypeResolver getTypeResolver() {
         if ( this.typeResolver == null ) {
             this.typeResolver = new ClassTypeResolver( this.pkg.getImports(),
                                                        this.pkg.getPackageCompilationData().getClassLoader() );
             // make an automatic import for the current package
             this.typeResolver.addImport( this.pkg.getName() + ".*" );
         }
         return this.typeResolver;
     }
 
     /**
      * @return The compiled package. The package may contain errors, which you can report on
      * by calling getErrors or printErrors. If you try to add an invalid package (or rule)
      * to a RuleBase, you will get a runtime exception.
      * 
      * Compiled packages are serializable.
      */
     public Package getPackage() {
 
         if ( hasErrors() ) {
             this.pkg.setError( this.printErrors() );
         }
         return this.pkg;
     }
 
     /** This will return true if there were errors in the package building and compiling phase */
     public boolean hasErrors() {
         return this.results.size() > 0;
     }
 
     /**
      * @return A list of Error objects that resulted from building and compiling the package. 
      */
     public DroolsError[] getErrors() {
         return (DroolsError[]) this.results.toArray( new DroolsError[this.results.size()] );
     }
 
     /**
      * This will pretty print the errors (from getErrors())
      * into lines.
      */
     public String printErrors() {
         final StringBuffer buf = new StringBuffer();
         for ( final Iterator iter = this.results.iterator(); iter.hasNext(); ) {
             final DroolsError err = (DroolsError) iter.next();
             buf.append( err.getMessage() );
             buf.append( "\n" );
         }
         return buf.toString();
     }
    
    
    /**
     * Reset the error list.
     * This is useful when incrementally building packages.
     * Care should be used when building this, if you 
     * clear this when there were errors on items that a rule depends on
     * (eg functions), then you will get spurious errors which will not be that helpful.
     */
    void resetErrors() {
        this.results.clear();
    }
 
     public static class MissingPackageNameException extends IllegalArgumentException {
         private static final long serialVersionUID = 320L;
 
         public MissingPackageNameException(final String message) {
             super( message );
         }
 
     }
 
     /**
      * This is the super of the error handlers.
      * Each error handler knows how to report a compile error of its type, should it happen.
      * This is needed, as the compiling is done as one
      * hit at the end, and we need to be able to work out what rule/ast element
      * caused the error.
      * 
      * An error handler it created for each class task that is queued to be compiled.
      * This doesn't mean an error has occurred, it just means it *may* occur
      * in the future and we need to be able to map it back to the AST element
      * that originally spawned the code to be compiled.
      */
     public abstract static class ErrorHandler {
         private final List errors  = new ArrayList();
         protected String   message;
         private boolean    inError = false;
 
         /** This needes to be checked if there is infact an error */
         public boolean isInError() {
             return this.inError;
         }
 
         public void addError(final CompilationProblem err) {
             this.errors.add( err );
             this.inError = true;
         }
 
         /**
          * 
          * @return A DroolsError object populated as appropriate,
          * should the unthinkable happen and this need to be reported.
          */
         public abstract DroolsError getError();
 
         /**
          * We must use an error of JCI problem objects.
          * If there are no problems, null is returned.
          * These errors are placed in the DroolsError instances.
          * Its not 1 to 1 with reported errors.
          */
         protected CompilationProblem[] collectCompilerProblems() {
             if ( this.errors.size() == 0 ) {
                 return null;
             } else {
                 final CompilationProblem[] list = new CompilationProblem[this.errors.size()];
                 this.errors.toArray( list );
                 return list;
             }
         }
     }
 
     public static class RuleErrorHandler extends ErrorHandler {
 
         private BaseDescr descr;
         private Rule      rule;
 
         public RuleErrorHandler(final BaseDescr ruleDescr,
                                 final Rule rule,
                                 final String message) {
             this.descr = ruleDescr;
             this.rule = rule;
             this.message = message;
         }
 
         public DroolsError getError() {
             return new RuleError( this.rule,
                                   this.descr,
                                   collectCompilerProblems(),
                                   this.message );
         }
 
     }
 
     /**
      * There isn't much point in reporting invoker errors, as
      * they are no help. 
      */
     public static class RuleInvokerErrorHandler extends RuleErrorHandler {
 
         public RuleInvokerErrorHandler(final BaseDescr ruleDescr,
                                        final Rule rule,
                                        final String message) {
             super( ruleDescr,
                    rule,
                    message );
         }
     }
 
     public static class FunctionErrorHandler extends ErrorHandler {
 
         private FunctionDescr descr;
 
         public FunctionErrorHandler(final FunctionDescr functionDescr,
                                     final String message) {
             this.descr = functionDescr;
             this.message = message;
         }
 
         public DroolsError getError() {
             return new FunctionError( this.descr,
                                       collectCompilerProblems(),
                                       this.message );
         }
 
     }
 
 }
