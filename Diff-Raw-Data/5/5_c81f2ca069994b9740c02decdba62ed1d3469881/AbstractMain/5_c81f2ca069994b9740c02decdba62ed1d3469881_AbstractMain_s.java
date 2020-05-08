 package net.mirky.redis;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import net.mirky.redis.AbstractMain.CommandLineRules.Option;
 
 public abstract class AbstractMain {
     protected final CommandLineRules rules;
     public Method mode;
     public String[] arguments;
 
     public AbstractMain(String... rawArguments) throws CommandLineParseError {
         rules = new CommandLineRules(getClass());
         mode = rules.getDefaultMode();
         ArrayList<String> remainingArguments = new ArrayList<String>();
        boolean stillParsingOptions = true; // i. e., we haven't encountered
         try {
             rules.resetFlags(this);
         } catch (IllegalArgumentException e) {
             throw new RuntimeException("bug detected", e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException("bug detected", e);
         }
        // "--" yet
         try {
             OUTER_LOOP: for (int i = 0; i < rawArguments.length; i++) {
                 String arg = rawArguments[i];
                 if (stillParsingOptions && arg.length() >= 2 && arg.charAt(0) == '-') {
                     if (arg.charAt(1) == '-') {
                         if (arg.length() == 2) {
                             // "--"
                             stillParsingOptions = false;
                         } else {
                             // long option
                             String option = arg.substring(2);
                             int eq = option.indexOf('=');
                             if (eq == -1) {
                                 Option descriptor = rules.getOption(option);
                                 if (descriptor == null) {
                                     throw new CommandLineParseError("unknown option --" + option);
                                 } else if (descriptor instanceof Option.Mode) {
                                     mode = ((Option.Mode) descriptor).method;
                                     if (mode.isAnnotationPresent(SupremeMode.class)) {
                                         break;
                                     }
                                 } else if (descriptor instanceof Option.Flag) {
                                     ((Option.Flag) descriptor).field.setBoolean(this, true);
                                 } else if (descriptor instanceof Option.Valued) {
                                     i++;
                                     if (i >= rawArguments.length) {
                                         throw new CommandLineParseError("option --" + option + " requires an argument");
                                     }
                                     ((Option.Valued) descriptor).field.set(this, rawArguments[i]);
                                 } else {
                                     throw new RuntimeException("bug detected");
                                 }
                             } else {
                                 String name = option.substring(0, eq);
                                 String value = option.substring(eq + 1);
                                 Option descriptor = rules.getOption(name);
                                 if (descriptor == null) {
                                     throw new CommandLineParseError("unknown option --" + option);
                                 } else if (descriptor instanceof Option.Valued) {
                                     ((Option.Valued) descriptor).field.set(this, value);
                                 } else {
                                     throw new CommandLineParseError("option --" + name + " does not take a value");
                                 }
                             }
                         }
                     } else {
                         // short option
                         INNER_LOOP: for (int j = 1; j < arg.length(); j++) {
                             char c = arg.charAt(j);
                             Option descriptor = rules.getOption(c);
                             if (descriptor == null) {
                                 throw new CommandLineParseError("unknown option -" + c);
                             } else if (descriptor instanceof Option.Mode) {
                                 mode = ((Option.Mode) descriptor).method;
                                 if (mode.isAnnotationPresent(SupremeMode.class)) {
                                     break OUTER_LOOP;
                                 }
                             } else if (descriptor instanceof Option.Flag) {
                                 ((Option.Flag) descriptor).field.setBoolean(this, true);
                             } else if (descriptor instanceof Option.Valued) {
                                 if (j + 1 < arg.length()) {
                                     j++;
                                     // the value follows inside this raw argument
                                     if (arg.charAt(j) == '=') {
                                         j++;
                                     }
                                     ((Option.Valued) descriptor).field.set(this, arg.substring(j));
                                 } else {
                                     // the value is in the next raw argument
                                     if (i + 1 >= rawArguments.length) {
                                         throw new CommandLineParseError("option -" + c + " requires an argument");
                                     }
                                     ((Option.Valued) descriptor).field.set(this, rawArguments[i + 1]);
                                     i++;
                                 }
                                 break INNER_LOOP;
                             } else {
                                 throw new RuntimeException("bug detected");
                             }
                         }
                     }
                 } else {
                     remainingArguments.add(arg);
                 }
             }
         } catch (IllegalAccessException e) {
             throw new RuntimeException("bug detected", e);
         }
         this.arguments = remainingArguments.toArray(new String[0]);
         ArgCountLimits limits = this.mode.getAnnotation(ArgCountLimits.class);
         if (limits != null) {
             if (arguments.length < limits.min()) {
                 throw new CommandLineParseError("too few arguments");
             }
             if (arguments.length > limits.max()) {
                 throw new CommandLineParseError("too many arguments");
             }
         }
     }
 
     public final void run() throws Throwable {
         try {
             mode.invoke(this);
         } catch (IllegalArgumentException e) {
             throw new RuntimeException("bug detected", e);
         } catch (SecurityException e) {
             throw new RuntimeException("bug detected", e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException("bug detected", e);
         } catch (InvocationTargetException e) {
             throw e.getCause();
         }
     }
 
     /**
      * Used to annotate mode methods.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.METHOD)
     public static @interface Mode {
         String value();
     }
 
     /**
      * Used to annotate the default mode method. Must be used on exactly one
      * method in the main class, but this method does not necessarily have to be
      * annotated with {@link Mode}.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.METHOD)
     public static @interface DefaultMode {
         // marker annotation
     }
 
     /**
      * Marks a supreme mode method. If the supreme mode is specified in the
      * command line, the rest of the command line will be ignored, and this mode
      * will be considered the execution mode. This is primarily useful for
      * implementing GNUCS-compliant {@code --help} and {@code --version}.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.METHOD)
     public static @interface SupremeMode {
         // marker annotation
     }
 
     /**
      * Used to attach argument count limitation to a mode method.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.METHOD)
     public static @interface ArgCountLimits {
         int min() default 0;
         int max() default Integer.MAX_VALUE;
     }
 
     /**
      * Attaches a single-letter alias to an option.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target({ElementType.METHOD, ElementType.FIELD})
     public static @interface Letter {
         char value();
     }
 
     /**
      * Marks a field as a valued option.  The field must be of type {@link String} and non-final.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     public static @interface Valued {
         String value();
     }
     
     /**
      * Marks a field as a flag.  The field must be of type {@code boolean} and non-final.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.FIELD)
     public static @interface Flag {
         String value();
     }
 
     public static final class CommandLineRules {
         private final Map<String, Option> options;
         private final Map<Character, Option> shortOptions;
         private final List<Field> flagFields;
         private final Method defaultMode;
 
         public CommandLineRules(Class mainClass) {
             options = new TreeMap<String, Option>();
             shortOptions = new TreeMap<Character, Option>();
             flagFields = new ArrayList<Field>();
             Method theDefaultMode = null;
             for (Method method : mainClass.getDeclaredMethods()) {
                 Mode modeAnn = method.getAnnotation(Mode.class);
                 if (modeAnn != null) {
                     String name = modeAnn.value();
                     if (options.containsKey(name)) {
                         throw new RuntimeException("multiple contenders for --" + name);
                     }
                     Option.Mode option = new Option.Mode(name, method);
                     options.put(modeAnn.value(), option);
                     Letter letterAnn = method.getAnnotation(Letter.class);
                     if (letterAnn != null) {
                         Character letter = new Character(letterAnn.value());
                         if (shortOptions.containsKey(letter)) {
                             throw new RuntimeException("multiple contenders for -" + letter);
                         }
                         shortOptions.put(letter, option);
                     }
                 } else {
                     if (method.isAnnotationPresent(Letter.class)) {
                         throw new RuntimeException("method has @Letter but no @Mode: " + method);
                     }
                 }
                 // Note that it is permissible for the default mode to be
                 // reachable via a command line option.
                 if (method.isAnnotationPresent(DefaultMode.class)) {
                     if (theDefaultMode != null) {
                         throw new RuntimeException("duplicate @DefaultMode in " + mainClass);
                     }
                     theDefaultMode = method;
                 }
             }
             if (theDefaultMode == null) {
                 throw new RuntimeException("no @DefaultMode in " + mainClass);
             }
             defaultMode = theDefaultMode;
             for (Field field : mainClass.getDeclaredFields()) {
                 Valued valuedAnn = field.getAnnotation(Valued.class);
                 if (valuedAnn != null) {
                     String name = valuedAnn.value();
                     if (options.containsKey(name)) {
                         throw new RuntimeException("multiple contenders for --" + name);
                     }
                     if (field.getType() != String.class) {
                         throw new RuntimeException("non-string field has @Valued: " + field);
                     }
                     if (Modifier.isFinal(field.getModifiers())) {
                         throw new RuntimeException("final field has @Valued: " + field);
                     }
                     Option.Valued option = new Option.Valued(name, field);
                     options.put(name, option);
                     Letter letterAnn = field.getAnnotation(Letter.class);
                     if (letterAnn != null) {
                         Character letter = new Character(letterAnn.value());
                         if (shortOptions.containsKey(letter)) {
                             throw new RuntimeException("multiple contenders for -" + letter);
                         }
                         shortOptions.put(letter, option);
                     }
                 }
 
                 Flag flagAnn = field.getAnnotation(Flag.class);
                 if (flagAnn != null) {
                     String name = flagAnn.value();
                     if (options.containsKey(name)) {
                         throw new RuntimeException("multiple contenders for --" + name);
                     }
                     if (field.getType() != Boolean.TYPE) {
                         throw new RuntimeException("non-Boolean field has @Flag: " + field);
                     }
                     if (Modifier.isFinal(field.getModifiers())) {
                         throw new RuntimeException("final field has @Flag: " + field);
                     }
                     Option.Flag option = new Option.Flag(name, field);
                     options.put(name, option);
                     Letter letterAnn = field.getAnnotation(Letter.class);
                     if (letterAnn != null) {
                         Character letter = new Character(letterAnn.value());
                         if (shortOptions.containsKey(letter)) {
                             throw new RuntimeException("multiple contenders for -" + letter);
                         }
                         shortOptions.put(letter, option);
                     }
                     flagFields.add(field);
                 }
 
                 if (valuedAnn == null && flagAnn == null) {
                     if (field.isAnnotationPresent(Letter.class)) {
                         throw new RuntimeException("field has @Letter but no @Valued or @Flag: " + field);
                     }
                 }
             }
         }
 
         public final Option getOption(String name) {
             return options.get(name);
         }
 
         public final Option getOption(char c) {
             return shortOptions.get(new Character(c));
         }
 
         public final Method getDefaultMode() {
             return defaultMode;
         }
 
         public final void resetFlags(Object instance) throws IllegalArgumentException, IllegalAccessException {
             for (Field field : flagFields) {
                 field.setBoolean(instance, false);
             }
         }
 
         public static abstract class Option {
             /**
              * The long, canonical name of this option.
              */
             public final String name;
             
             public Option(String name) {
                 this.name = name;
             }
 
             public static final class Mode extends Option {
                 public final Method method;
 
                 public Mode(String name, Method method) {
                     super(name);
                     this.method = method;
                 }
             }
             
             public static final class Valued extends Option {
                 public final Field field;
                 
                 public Valued(String name, Field field) {
                     super(name);
                     this.field = field;
                 }
             }
             
             public static final class Flag extends Option {
                 public final Field field;
                 
                 public Flag(String name, Field field) {
                     super(name);
                     this.field = field;
                 }
             }
         }
     }
     
     static final class CommandLineParseError extends Exception {
         CommandLineParseError(String msg) {
             super(msg);
         }
     }
 }
