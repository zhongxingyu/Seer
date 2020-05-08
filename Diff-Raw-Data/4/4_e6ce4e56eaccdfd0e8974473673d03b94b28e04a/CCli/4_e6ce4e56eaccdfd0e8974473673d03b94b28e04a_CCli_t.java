 /*
  * Copyright (C) 2011 by Serge Arkhipov <serge@aerialsounds.org>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 
 
 package org.aerialsounds.ccli;
 
 
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 import org.aerialsounds.ccli.datacontainer.DataContainer;
 import org.aerialsounds.ccli.optionobservable.Observable;
 import org.aerialsounds.ccli.optionobservable.Observer;
 import org.aerialsounds.ccli.options.AbstractOption;
 import org.aerialsounds.ccli.options.ParseableOption;
 import org.aerialsounds.ccli.options.ShortOption;
 import org.aerialsounds.ccli.options.AbstractOption.DataIsNotValid;
 import org.aerialsounds.ccli.valueparsers.BooleanConverter;
 
 
 /**
  * <p>This class represents repository for all created {@linkplain Option options}
  * and should be used for parsing application commands. It manages options, handles option bindings
  * and stores data containers for each option (or for binded set).</p>
  *
  * <p>The workflow as is: user creates Options with given option type ({@linkplain OptionTypes#SHORT short}
  * with single hyphen, {@linkplain OptionTypes#LONG long} with double hyphen or {@linkplain OptionTypes#CUSTOM custom}
  * with user defined prefix). After creating, user may remove some of them from repository. User may bind
  * option to other created options but main idea is transparent binding. User may suppose binding as internal
  * option process but it is de-facto encapsulated in {@code CCli} object.</p>
  *
  * <p>After creating of all options, user invokes {@link CCli#parse parse()} method.
  * It can throw {@link CannotParse CannotParse} exception if something is wrong with command line.
  * If everything is OK, user can obtain parsed values with {@link Option#getValue getValue()}
  * method of each {@code Option}.<p>
  *
  * <p>After parsing, user also may obtain application arguments (e.g filenames) with
  * {@link CCli#getApplicationArguments getApplicationArguments()} method. Also, user may generate
  * help with {@link CCli#help help()} method.</p>
  *
  * @see Option
  * @see CannotParse
  * @see OptionTypes
  *
  * @since 1.0
  *
  * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
  *
  */
 public class CCli
     implements Observer {
 
 
 
 // ===============================================================================================================
 // E X C E P T I O N S
 // ===============================================================================================================
 
 
     /**
      * <p>This exception should be thrown when {@code CCli} repository is unable to create option
      * with given parameters. There are 2 reasons for this fault: full name of option (with prefixes)
      * is already presented in repository or it contains depreciated characters such as
      * {@code "="} or not correctly defined. You can obtain it as a cause of raised exception.</p>
      *
      * <p>Please not that validation of data consistency performed only during parsing
      * operation.</p>
      *
      * @see HaveSuchOption
      * @see org.aerialsounds.ccli.options.AbstractOption.DataIsNotValid DataIsNotValid
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public class CannotCreateOption
         extends RuntimeException {
         private static final long serialVersionUID = -5248825722401579070L;
     } /* class CannotCreateOption */
 
 
     /**
      * <p>This exception should be raised if user tries to create option with full name, already
      * registered in {@code CCli} repository. Note that full name is concatenation of option prefix
      * and own name.</p>
      *
      * <p>For example, {@linkplain OptionTypes#SHORT short} option with {@code "z"} name is equal to
      * {@linkplain OptionTypes#CUSTOM custom} option with {@code "-z"} name. You can obtain generated
      * full name with {@link Option#getFullName getFullName()} method of {@link Option Option}.</p>
      *
      * <p>This exception implicates {@link CannotCreateOption} exception and acts as its cause.</p>
      *
      * @see Option
      * @see OptionTypes
      * @see CannotCreateOption
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public class HaveSuchOption
         extends RuntimeException {
         private static final long serialVersionUID = 5086623510834120688L;
     } /* class HaveSuchOption */
 
 
     /**
      * <p>This exception should be raised if parsing is not succesfull and there were some errors during
      * this procedure. You can obtain the reason of fault as cause of such exception.</p>
      *
      * <p>Please not that parsed values of each created {@linkplain Option options} are flushed if such fault
      * appears.</p>
      *
      * <p>There are several errors that caused this fault. Please see references for detailed description.</p>
      *
      * @see NothingToParse
      * @see UnexpectedOption
      * @see UnexpectedEndOfArgumentList
      * @see IncorrectParsingOfOption
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public class CannotParse
         extends RuntimeException {
         private static final long serialVersionUID = 6228517677445066958L;
     } /* class CannotParse */
 
 
     /**
      * <p>This exception should be raised if application arguments is {@code null} for some reason. Of course
      * it is not real fault, but it can be coding fault. Application arguments should not be {@code null}
      * with normal work flow. It can be empty (with 0 elements in {@code args} array) of course, but not
      * {@code null}. If you ever get such exception, please check your code.</p>
      *
      * <p>This exception implicates {@link CannotParse} exception and acts as its cause.</p>
      *
      * @see CannotParse
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public class NothingToParse
         extends RuntimeException {
         private static final long serialVersionUID = -6990780110727348311L;
     } /* class NothingToParse */
 
 
     /**
      * <p>This exception should be raised if {@code CCli} meets unexpected option during parsing. For example,
      * it can be option after application arguments list. {@code CCli} allows application arguments to be placed
      * only at the end of list.</p>
      *
      * <p>Such list is fault:</p>
      *
      * <pre><code>
      *     java app.jar -z --quiet mighty_source.c --compile=true
      * </code></pre>
      *
      * <p>{@code mighty_source.c} should be placed at the end of list:</p>
      *
      * <pre><code>
      *     java app.jar -z --quiet --compile=true mighty_source.c
      * </code></pre>
      *
      * <p>This exception implicates {@link CannotParse} exception and acts as its cause. It contains errorous
      * option as message of this exception.</p>
      *
      * @see CannotParse
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public class UnexpectedOption
         extends RuntimeException {
 
         private static final long serialVersionUID = -7689966940656377554L;
 
         public
         UnexpectedOption (final String currentArgument) {
             super(currentArgument);
         } /* UnexpectedOption */
 
     } /* class UnexpectedOption */
 
 
     /**
      * <p>This exception should be raised if {@code CCli} meets unexpected end of argument list.</p>
      *
      * <p>Example of fault:</p>
      *
      * <pre><code>
      *     java app.jar -z --quiet -q
      * </code></pre>
      *
      * <p>Assume that {@code -q} option has {@link ValueTypes#INTEGER integer} value type. So what value
      * should it have? It has not inline value as {@code -q10}. It cannot have default value because user
      * tries to define it but made a mistake.</p>
      *
      * <p><strong>Please note that {@link ValueTypes#BOOLEAN boolean} and
      * {@link ValueTypes#ATOMIC_BOOLEAN atomic boolean} options always have {@code true} value as
      * inlined</strong>. Therefore if {@code -q} option is boolean, such fault never appears.</p>
      *
      * <p>This exception implicates {@link CannotParse} exception and acts as its cause.</p>
      *
      * @see CannotParse
      * @see ValueTypes
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public class UnexpectedEndOfArgumentList
         extends RuntimeException {
         private static final long serialVersionUID = -4104774535367707313L;
     } /* class UnexpectedEndOfArgumentList */
 
 
     /**
      * <p>This exception should be raised if parsed value has other type than expected.</p>
      *
      * <p>Example of fault:</p>
      *
      * <pre><code>
      *     java app.jar -z 10
      * </code></pre>
      *
      * <p>Assume that {@code -z} option has {@link ValueTypes#CHAR character} value type. 10 is numerical
      * value so it looks like user made a mistake with input of arguments. Yes, 10 can be casted to character,
      * but why do we need try to fix such mistake? If user wants 10 to be casted to character, he need to define
      * it as {@code '\0010'} or something else. It is better to raise fault in this case neither to interpert
      * value wrong.</p>
      *
      * <p>This exception implicates {@link CannotParse} exception and acts as its cause. It contains option with
      * incorrect value as a message of this exception.</p>
      *
      * @see CannotParse
      * @see ValueTypes
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public class IncorrectParsingOfOption
         extends RuntimeException {
 
         private static final long serialVersionUID = -7594488127011543528L;
 
         public
         IncorrectParsingOfOption (final String option) {
             super(option);
         } /* IncorrectParsingOfOption */
 
     } /* class IncorrectParsingOfOption */
 
 
 
 // ===============================================================================================================
 // S T A T I C   M E T H O D S
 // ===============================================================================================================
 
 
 
     /**
     * <p>This is simple wrapper of {@link Option#bind bind()} method of {@link Option}. You can use it
     * if you like, following lines of code are equal:</p>
      *
      * <pre><code>
      *     CCli.bind(one, another);
      *     one.bind(another);
      * </code></pre>
      *
      * @param one     - one {@code Option} to bind
      * @param another - another {@code Option} to bind
      *
      * @throws CannotBind if given options cannot bind one to another.
      *
      * @see Option
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     static public void
     bind (final Option one, final Option another) throws CannotBind {
         one.bind(another);
     } /* bind */
 
 
 
 // ===============================================================================================================
 // F I E L D S
 // ===============================================================================================================
 
 
 
     /**
      * Raw unparsed application arguments. As a rule, it is argument of {@code main} method.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     protected final String[] args;
 
     /**
      * Factory which is used to create {@link Option Options} and
      * {@link org.aerialsounds.ccli.datacontainer.DataContainer DataContainers}.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     protected final CliFactory factory;
 
     /**
      * Help generator.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     private final CliHelpGenerator helpGenerator;
 
     /**
      * <p>Contains {@link org.aerialsounds.ccli.datacontainer.DataContainer DataContainer}
      * and set of {@link org.aerialsounds.ccli.options.AbstractOption AbstractOptions} which contains
      * that container.</p>
      *
      * <p>In other words, it is {@code DataContainer} and set of binded {@code AbstractOptions}. Such
      * mapping is used for convenient binding of options. Each {@code AbstractOption} of a binded set
      * contains reference to the same {@code DataContainer}.</p>
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     protected final Map<DataContainer,Set<AbstractOption>> containers;
 
     /**
      * <p>A collection of {@link org.aerialsounds.ccli.options.ParseableOption ParseableOptions}.
      * It should be used for searching appropriate options with given full name and so on.</p>
      *
      * <p>Please do not use {@link CCli#containers containers} field for searching purposes. It also
      * contains all created options, but searching with {@code options} field is more faster.</p>
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     protected final Collection<ParseableOption> options;
 
     /**
      * <p>Stores application arguments. It is not {@link CCli#args args} collection, it is
      * parsed values.</p>
      *
      * <p>For example:</p>
      *
      * <pre><code>
      *     java app.jar -z --quiet --compile=true mighty_source.c
      * </code></pre>
      *
      * <p>{@code mighty_source.c} should be placed in this collection after parsing.</p>
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     protected final Collection<String> appArguments;
 
     /**
      * Determines if repository was parsed already.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     protected boolean parsed;
 
 
 
 // ===============================================================================================================
 // C O N S T R U C T O R S
 // ===============================================================================================================
 
 
 
     /**
      * <p>Constructor.</p>
      *
      * <p>Creates {@code CCli} repository with default {@link CliHelpGenerator}.</p>
      *
      * @param args - raw unparsed application arguments.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public
     CCli (final String[] args) {
         this(args, new CliHelpGenerator());
     } /* CCli */
 
 
     /**
      * <p>Constructor.</p>
      *
      * <p>Creates {@code CCli} repository with given {@link CliHelpGenerator} instance.</p>
      *
      * @param args          - raw unparsed application arguments.
      * @param helpGenerator - generator of help instance.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public
     CCli (final String[] args, final CliHelpGenerator helpGenerator) {
         this.args          = args;
         this.helpGenerator = helpGenerator;
 
         factory      = new CliFactory(this);
         containers   = new HashMap<DataContainer,Set<AbstractOption>>();
         options      = new LinkedList<ParseableOption>();
         appArguments = new LinkedList<String>();
     } /* CCli */
 
 
 
 // ===============================================================================================================
 // P U B L I C   M E T H O D S
 // ===============================================================================================================
 
 
 
     /**
      * <p>This method should be used for creating new options. It can raise {@link CannotCreateOption}
      * exception if repository cannot create such option. Please see references for detailed
      * description of possible fault reasons.</p>
      *
      * @param type         - the type of option to be create
      * @param name         - option name. Please note that you do not need to set explicit hyphen prefix
      *                       for {@linkplain OptionTypes#SHORT short} and {@linkplain OptionTypes#LONG long}
      *                       options.
      * @param defaultValue - default value of this option. It is used if option value was not redefined
      *                       during parsing.
      * @param valueType    - type of value should be parsed.
      * @param help         - description of option meaning.
      *
      * @return Created {@link Option} instance with given parameters.
      *
      * @throws CannotCreateOption if it is not possible to create option with given parameters.
      *
      * @see OptionTypes
      * @see ValueTypes
      * @see Option
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     final public Option
     createOption (
         final OptionTypes type,
         final String      name,
         final Object      defaultValue,
         final ValueTypes  valueType,
         final String      help
     ) throws CannotCreateOption {
         final DataContainer container = factory.createDataContainer(defaultValue, valueType, help);
         try {
             final ParseableOption opt = factory.createOption(type, name, container);
             register(opt);
             return opt;
         }
         catch ( DataIsNotValid e ) {
             interruptOptionCreating(e);
         }
         catch ( HaveSuchOption e ) {
             interruptOptionCreating(e);
         }
 
         return null;
     } /* createOption */
 
 
     /**
      * <p>This method should be used for removing {@link Option} instances from repository. After removing,
      * you cannot get any values from existing object, you cannot bind it to other or doing something else.
      * But you can create new option with such name if you want.</p>
      *
      * @param option - {@code Option} instance should be removed from repository.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public void
     remove (final Option option) {
         if ( option instanceof ParseableOption ) {
             final ParseableOption removed = (ParseableOption)option;
             if ( options.contains(removed) ) {
                 options.remove(removed);
                 removeContainer(removed);
                 removed.dispose();
             }
         }
     } /* remove */
 
 
     /**
      * <p>This method should be used for parsing command line. Command line should be set when
      * {@code CCli} repository was created. If parsing is OK, all created options obtained its values
      * which may obtain using {@link Option#getValue getValue()} method.</p>
      *
      * <p>Note that each option will contain proper value after parsing. If option is not defined
      * in command line, it will contain default value. Otherwise, it will contain redefined one.
      * Please note also that command line may contain multiple values of option. In that case,
      * last in line will be used, other will be dropped.</p>
      *
      * <p>Please note also if parsion was not ok, all obtained values will be lost and options will be
      * signed as not defined. You can get define state with {@link Option#isParsed isParsed()} method.</p>
      *
      * <p>If you add new option after invokation of this method, it will be not parsed, but other will store
      * their data. You need to re-invoke this method to refresh information. If you remove option after
      * parsing, information of existing ones will be stored also and there is no need for refresh.</p>
      *
      * @throws CannotParse if parsing command line is errorous.
      *
      * @see Option
      * @see CannotParse
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     public void
     parse ()
     throws CannotParse {
         if ( args == null )
             interruptParsing(new NothingToParse());
 
         if ( !parsed ) {
             clearParsedData();
 
             String          inlineValue = null;
             Object          parsedValue = null;
             String          current     = null;
             ParseableOption option      = null;
             for ( int i = 0; i < args.length; ++i ) {
                 current = args[i];
                 option  = findOption(current);
 
                 if ( option != null ) {
                     if ( !appArguments.isEmpty() )
                         interruptParsing(new UnexpectedOption(current));
 
                     inlineValue = option.getInlineValue(current);
                     parsedValue = null;
                     if ( inlineValue == null )
                         if ( i < args.length - 1 ) {
                             parsedValue = option.parse(args[i + 1]);
                             if ( parsedValue != null )
                                 ++i;
                         } else
                             interruptParsing(new UnexpectedEndOfArgumentList());
                     else // if ( inlineValue == null )
                         parsedValue = option.parse(inlineValue);
 
                     if ( option.getValueType().isBoolean() && parsedValue == null )
                         parsedValue = option.parse(BooleanConverter.TRUE);
 
                     if ( parsedValue == null )
                         interruptParsing(new IncorrectParsingOfOption(current));
                     else
                         option.setValue(parsedValue);
                 } else if ( !parseUnknownOption(current) ) // if ( option != null )
                     appArguments.add(current);
             } // for ( int i = 0; i < args.length; ++i )
 
             parsed = true;
         } // if ( !parsed )
     } /* parse */
 
 
     /**
      * <p>This method should be used to check that parsing is complete. It will be
      * {@code false} if there was no any parsing activity before or user added new option
      * after parsing.</p>
      *
      * @return {@code true} if repository is need to be parsed, {@code false} otherwise.
      *
      * @see CCli#parse parse()
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     final public boolean
     isParsed () {
         return parsed;
     } /* isParsed */
 
 
     /**
      * <p>This method should be used to obtain own application arguments such as file names or
      * something else. Suppose such arguments as own command values of application.</p>
      *
      * <p>For example:</p>
      *
      * <pre><code>
      *     java app.jar -z --quiet --compile=true mighty_source.c
      * </code></pre>
      *
      * <p>{@code mighty_source.c} is supposed to be application argument.</p>
      *
      * @return {@code Iterator<String>} to collection of such arguments.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     final public Iterator<String>
     getApplicationArguments () {
         return appArguments.iterator();
     } /* getApplicationArguments */
 
 
     /**
      * <p>This method should be used for generating help for created options.</p>
      *
      * @return {@code String} help representation.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     final public String
     help () {
         return helpGenerator.generate(containers.values());
     } /* help */
 
 
     /**
      * {@inheritDoc}
      *
      */
     @Override
     public void
     update (final Observable initiator, final Object initiated) {
         if ( initiator instanceof AbstractOption && initiated instanceof AbstractOption ) {
             final DataContainer              firstContainer  = ((AbstractOption)initiator).getContainer();
             final DataContainer              secondContainer = ((AbstractOption)initiated).getContainer();
             final Collection<AbstractOption> firstSet        = containers.get(firstContainer);
             final Collection<AbstractOption> secondSet       = containers.get(secondContainer);
 
             if ( firstSet != null && secondSet != null && firstSet != secondSet ) {
                 firstSet.addAll(secondSet);
                 for ( AbstractOption opt : secondSet )
                     opt.setContainer(firstContainer);
                 containers.remove(secondContainer);
             }
         }
     } /* update */
 
 
 
 // ===============================================================================================================
 // P R O T E C T E D   M E T H O D S
 // ===============================================================================================================
 
 
 
     /**
      * <p>This method should be used to get created
      * {@link org.aerialsounds.ccli.options.ParseableOption ParseableOption} instance from repository
      * with given full name</p>
      *
      * @param option - full name of {@code ParseableOption} to search.
      *
      * @return {@code ParseableOption} instance or {@code null} if option was not found.
      *
      * @see org.aerialsounds.ccli.options.ParseableOption ParseableOption
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     protected ParseableOption
     findOption (final String option) {
         final Iterator<ParseableOption> it = options.iterator();
         while ( it.hasNext() ) {
             final ParseableOption entry = it.next();
             if ( entry.appropriate(option) )
                 return entry;
         }
 
         return null;
     } /* findOption */
 
 
     /**
      * <p>This method trying to parse string with unknown option. If string is joined options string,
      * it will be evaluate in such way.</p>
      *
      * <p>Example of joined options:</p>
      *
      * <pre><code>
      *     java app.jar -zcv mighty_source.c
      * </code></pre>
      *
      * <p>Let's assume that {@code "-zcv"} option is not created, but {@linkplain OptionTypes#SHORT short}
      * {@linkplain ValueTypes#BOOLEAN boolean} (or {@linkplain ValueTypes#ATOMIC_BOOLEAN atomic boolean}) options
      * {@code "-z"}, {@code "-c"} and {@code "-v"} is created. These options are called <em>joined options</em>
      * and will be in the same way with following command line:</p>
      *
      * <pre><code>
      *     java app.jar -z -c -v mighty_source.c
      * </code></pre>
      *
      * <p>Please note that numerical options as {@code "-5"} cannot be joined because of possible
      * misunderstandings.</p>
      *
      * @param current - string with unknown options.
      *
      * @return {@code true} if string can be parsed, {@code false} otherwise.
      *
      * @see ValueTypes
      * @see OptionTypes
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     final protected boolean
     parseUnknownOption (final String current) {
         final Iterable<ParseableOption> joined = findJoinedOptions(current);
         if ( joined != null ) {
             for ( ParseableOption opt : joined )
                 opt.setValue(opt.parse(BooleanConverter.TRUE));
             return true;
         }
 
         return false;
     } /* parseUnknownOption */
 
 
     /**
      * <p>This method should be used to obtain set of joined options.</p>
      *
      * <p>Example of joined options:</p>
      *
      * <pre><code>
      *     java app.jar -zcv mighty_source.c
      * </code></pre>
      *
      * <p>Let's assume that {@code "-zcv"} option is not created, but {@linkplain OptionTypes#SHORT short}
      * {@linkplain ValueTypes#BOOLEAN boolean} (or {@linkplain ValueTypes#ATOMIC_BOOLEAN atomic boolean}) options
      * {@code "-z"}, {@code "-c"} and {@code "-v"} is created. These options are called <em>joined options</em>
      * and will be in the same way with following command line:</p>
      *
      * <pre><code>
      *     java app.jar -z -c -v mighty_source.c
      * </code></pre>
      *
      * <p>Please note that numerical options as {@code "-5"} cannot be joined because of possible
      * misunderstandings.</p>
      *
      * @param current - suspected string with joined options.
      *
      * @return {@code Iterable} container with {@link org.aerialsounds.ccli.options.ParseableOption ParseableOptions}
      *         or {@code null} if options was not found.
      *
      * @see org.aerialsounds.ccli.options.ParseableOption ParseableOption
      * @see ValueTypes
      * @see OptionTypes
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     protected Iterable<ParseableOption>
     findJoinedOptions (final String current) {
         final String prefix = OptionTypes.SHORT.getPrefix();
         Collection<ParseableOption> list = null;
 
         if (
                !current.startsWith(OptionTypes.LONG.getPrefix())
             && current.startsWith(prefix)
             && !ShortOption.haveNumbers(current)
         ) {
             final char[] currentLine = current.substring(prefix.length()).toCharArray();
             list = new LinkedList<ParseableOption>();
             ParseableOption opt;
             for ( char currentChar : currentLine ) {
                 opt = findOption(prefix + Character.toString(currentChar));
 
                 if ( opt != null && opt.getValueType().isBoolean() )
                     list.add(opt);
                 else
                     return null;
             }
         }
 
         return list;
     } /* findJoinedOptions */
 
 
     /**
      * <p>This method should be used for registering
      * {@link org.aerialsounds.ccli.options.ParseableOption ParseableOption} within repository.</p>
      *
      * @param opt - {@code ParseableOption} to be registered.
      *
      * @throws HaveSuchOption if repository already have registered option with such full name.
      *
      * @see org.aerialsounds.ccli.options.ParseableOption ParseableOption
      * @see HaveSuchOption
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     protected void
     register (final ParseableOption opt)
     throws HaveSuchOption {
         final String name = opt.getFullName();
         for ( Option o : options )
             if ( name.equals(o.getFullName()) )
                 throw new HaveSuchOption();
 
         final Set<AbstractOption> containerSet = new HashSet<AbstractOption>();
         containerSet.add(opt);
         containers.put(opt.getContainer(), containerSet);
 
         options.add(opt);
 
         parsed = false;
     } /* register */
 
 
     /**
      * <p>This method should be used for removing data container mapping with
      * {@link org.aerialsounds.ccli.options.AbstractOption AbstractOption}. If there is no any other
      * {@code AbstractOption} associated with container, it will remove from repository completely.</p>
      *
      * @param option - {@code AbstractOption} which mapping is need to be removed.
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     final protected void
     removeContainer (final AbstractOption option) {
         final DataContainer container = option.getContainer();
         final Collection<AbstractOption> containerSet = containers.get(container);
         if ( containerSet != null ) {
             containerSet.remove(option);
             if ( containerSet.isEmpty() )
                 containers.remove(container);
         }
     } /* removeContainer */
 
 
     /**
      * <p>This method should be used to clear and drop all parsed data in repository.</p>
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     final protected void
     clearParsedData () {
         for ( DataContainer container : containers.keySet() )
             container.dropDefined();
 
         appArguments.clear();
         parsed = false;
     } /* clearParsedData */
 
 
 
 // ===============================================================================================================
 // P R I V A T E   M E T H O D S
 // ===============================================================================================================
 
 
 
     /**
      * <p>This method should be used to interrupt parsing and initialize cause. It will clear
      * all parsed data as well.</p>
      *
      * @param cause - the cause of parsing interruption.
      *
      * @throws CannotParse such exception will be thrown during each invokation of this method.
      *
      * @see CCli#parse parse()
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     private void
     interruptParsing (final Throwable cause)
     throws CannotParse {
         clearParsedData();
         throw (CannotParse) new CannotParse().initCause(cause);
     } /* interruptParsing */
 
 
     /**
      * <p>This method should be used to interrupt {@link Option} creating.</p>
      *
      * @param cause - the cause of creating interruption.
      *
      * @throws CannotCreateOption such exception will be thrown during each invokation of this method.
      *
      * @see Option
      * @see CCli#createOption createOption()
      *
      * @since 1.0
      *
      * @author Serge Arkhipov &lt;<a href="mailto:serge@aerialsounds.org">serge@aerialsounds.org</a>&gt;
      *
      */
     private void
     interruptOptionCreating (final Throwable cause)
     throws CannotCreateOption {
         throw (CannotCreateOption) new CannotCreateOption().initCause(cause);
     } /* interruptOptionCreating */
 
 
 } /* class CCli */
 
