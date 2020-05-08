 package net.mirky.redis;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import net.mirky.redis.analysers.BlockedBinaryAnalyser;
 import net.mirky.redis.analysers.CelledFontAnalyser;
 
 public final class Format {
     private final String nameRoot;
     public final Analyser analyser;
     private final Option[] options;
 
     static abstract class OptionType<T> {
         abstract void stringify(T value, StringBuilder sb);
 
         abstract T parse(String spec) throws OptionError;
 
         abstract Option<T> parse(Option<T> option, String newValue) throws OptionError;
 
         static abstract class Simple<T> extends OptionType<T> {
             @Override
             final Option.SimpleOption<T> parse(Option<T> option, String newValue) throws OptionError {
                 return new Option.SimpleOption<T>(option.name, this, parse(newValue), true);
             }
         }
 
         private static abstract class SimpleIntegerBasedType extends Simple<Integer> {
             private final boolean outputHex;
 
             public SimpleIntegerBasedType(boolean outputHex) {
                 this.outputHex = outputHex;
             }
 
             @Override
             void stringify(Integer value, StringBuilder sb) {
                 if (outputHex) {
                     if (value.intValue() < 0) {
                         sb.append('-');
                        sb.append("0x");
                        sb.append(Long.toString(-(long) value.intValue(), 16).toUpperCase());
                    } else {
                        sb.append("0x");
                        sb.append(Integer.toString(value.intValue(), 16).toUpperCase());
                     }
                 } else {
                     sb.append(value.intValue());
                 }
             }
 
             @Override
             Integer parse(String spec) throws OptionError {
                 /*
                  * We'll use Integer.parseInt() eventually but it doesn't handle
                  * signs or radix prefixen, so some preparation is needed.
                  */
                 try {
                     boolean negate = false;
                     int i = 0;
                     if (i < spec.length()) {
                         switch (spec.charAt(i)) {
                             case '+':
                                 negate = false;
                                 i++;
                                 break;
                             case '-':
                                 negate = true;
                                 i++;
                                 break;
                         }
                     }
                     int radix;
                     if (i + 2 <= spec.length() && spec.charAt(i) == '0'
                             && Character.toLowerCase(spec.charAt(i + 1)) == 'x') {
                         i += 2;
                         radix = 16;
                     } else {
                         radix = 10;
                     }
                     // just try to catch awkwardness like double sign or "0x-1"
                     if (i >= spec.length() || spec.charAt(i) < '0') {
                         throw new NumberFormatException();
                     }
                     int value = Integer.parseInt((negate ? "-" : "") + spec.substring(i), radix);
                     if (suitable(value)) {
                         return new Integer(value);
                     } else {
                         throw new OptionError("unsuitable integer: " + spec);
                     }
                 } catch (NumberFormatException e) {
                     throw new OptionError("invalid integer: " + spec, e);
                 }
             }
 
             abstract boolean suitable(int value);
         }
 
         static final class RangedIntegerType extends SimpleIntegerBasedType {
             private final int floor;
             private final int ceiling;
 
             public RangedIntegerType(boolean outputHex, int floor, int ceiling) {
                 super(outputHex);
                 this.floor = floor;
                 this.ceiling = ceiling;
             }
 
             @Override
             final boolean suitable(int value) {
                 return value >= floor && value <= ceiling;
             }
         }
 
         @Retention(RetentionPolicy.RUNTIME)
         @Target(ElementType.FIELD)
         private static @interface SimpleTypeName {
             String value();
         }
 
         @SimpleTypeName("unsigned-decimal")
         static final Simple<Integer> UNSIGNED_DECIMAL_INTEGER = new RangedIntegerType(false, 0, Integer.MAX_VALUE);
 
         @SimpleTypeName("unsigned-hex")
         static final Simple<Integer> UNSIGNED_HEX_INTEGER = new RangedIntegerType(true, 0, Integer.MAX_VALUE);
 
         @SimpleTypeName("positive-decimal")
         static final Simple<Integer> POSITIVE_DECIMAL_INTEGER = new RangedIntegerType(false, 1, Integer.MAX_VALUE);
 
         @SimpleTypeName("positive-hex")
         static final Simple<Integer> POSITIVE_HEX_INTEGER = new RangedIntegerType(true, 1, Integer.MAX_VALUE);
 
         // Note that the {@code specialValue} may be negative.
         static class NullableUnsignedInteger extends SimpleIntegerBasedType {
             private final int specialValue;
 
             NullableUnsignedInteger(boolean outputHex, int specialValue) {
                 super(outputHex);
                 this.specialValue = specialValue;
             }
 
             @Override
             final void stringify(Integer value, StringBuilder sb) {
                 if (value.intValue() == specialValue) {
                     sb.append("nil");
                 } else {
                     super.stringify(value, sb);
                 }
             }
 
             @Override
             final Integer parse(String spec) throws OptionError {
                 String lowercaseSpec = spec.toLowerCase();
                 if (lowercaseSpec.equals("nil") || lowercaseSpec.equals("null")) {
                     return new Integer(specialValue);
                 } else {
                     return super.parse(spec); // will call suitable() for
                                               // postparse further validation
                 }
             }
 
             @Override
             boolean suitable(int value) {
                 return value >= 0 && value != specialValue;
             }
         }
 
         @SimpleTypeName("unsigned-hex/-1")
         static final NullableUnsignedInteger UNSIGNED_HEX_OR_MINUS_ONE = new NullableUnsignedInteger(true, -1);
 
         @SimpleTypeName("decoding")
         static final Simple<Decoding> DECODING = new Simple<Decoding>() {
             @Override
             final void stringify(Decoding value, StringBuilder sb) {
                 sb.append(value.name);
             }
 
             @Override
             final Decoding parse(String name) throws OptionError {
                 try {
                     return Decoding.MANAGER.get(name);
                 } catch (ResourceManager.ResolutionError e) {
                     throw new OptionError("unknown decoding " + name, e);
                 }
             }
         };
 
         @SimpleTypeName("lang")
         static final Simple<Disassembler.Lang> LANG = new Simple<Disassembler.Lang>() {
             @Override
             final void stringify(Disassembler.Lang value, StringBuilder sb) {
                 sb.append(value.name);
             }
 
             @Override
             final Disassembler.Lang parse(String name) throws OptionError {
                 try {
                     return Disassembler.Lang.MANAGER.get(name);
                 } catch (ResourceManager.ResolutionError e) {
                     throw new OptionError("unknown CPU: " + name, e);
                 }
             }
         };
 
         @SimpleTypeName("api")
         static final Simple<Disassembler.API> API = new Simple<Disassembler.API>() {
             @Override
             final void stringify(Disassembler.API value, StringBuilder sb) {
                 sb.append(value.name);
             }
 
             // Attempt to find the given API. Return it, or throw
             // SlashOptionError when it fails.
             // Note that the APIs are currently hardcoded.
             @Override
             final Disassembler.API parse(String name) throws OptionError {
                 try {
                     return Disassembler.API.MANAGER.get(name);
                 } catch (ResourceManager.ResolutionError e) {
                     throw new OptionError("API load failed: " + name, e);
                 }
             }
         };
 
         @SimpleTypeName("struct")
         static final Simple<Struct> STRUCT = new Simple<Struct>() {
             @Override
             final void stringify(Struct value, StringBuilder sb) {
                 sb.append(value.name);
             }
 
             @Override
             final Struct parse(String name) throws OptionError {
                 try {
                     return Struct.MANAGER.get(name);
                 } catch (ResourceManager.ResolutionError e) {
                     throw new OptionError("unknown structure: " + name, e);
                 }
             }
         };
 
         @SimpleTypeName("geometry")
         static final Simple<GeometryLevel[]> GEOMETRY = new Simple<GeometryLevel[]>() {
             @Override
             final void stringify(GeometryLevel[] value, StringBuilder sb) {
                 for (int i = 0; i < value.length; i++) {
                     if (i != 0) {
                         sb.append('/');
                     }
                     value[i].stringify(sb);
                 }
             }
 
             @Override
             final GeometryLevel[] parse(String geometrySpec) throws OptionError {
                 String[] specParts = geometrySpec.split(",");
                 if (specParts.length < 1) {
                     throw new OptionError("geometry parse error");
                 }
                 GeometryLevel[] parsedLevels = new GeometryLevel[specParts.length];
                 for (int i = 0; i < specParts.length; i++) {
                     parsedLevels[i] = GeometryLevel.parse(specParts[i]);
                 }
                 return parsedLevels;
             }
         };
 
         @SimpleTypeName("scanline-direction")
         static final Simple<CelledFontAnalyser.ScanlineDirection> SCANLINE_DIRECTION = new Simple<CelledFontAnalyser.ScanlineDirection>() {
             @Override
             final void stringify(CelledFontAnalyser.ScanlineDirection value, StringBuilder sb) {
                 switch (value) {
                     case MSB_LEFT:
                         sb.append("left");
                         break;
                     case MSB_RIGHT:
                         sb.append("right");
                         break;
                     default:
                         throw new RuntimeException("bug detected");
                 }
             }
 
             @Override
             final CelledFontAnalyser.ScanlineDirection parse(String directionName) throws OptionError {
                 if (directionName.toLowerCase().equals("left")) {
                     return CelledFontAnalyser.ScanlineDirection.MSB_LEFT;
                 } else if (directionName.toLowerCase().equals("right")) {
                     return CelledFontAnalyser.ScanlineDirection.MSB_RIGHT;
                 } else {
                     throw new OptionError("unknown direction: " + directionName);
                 }
             }
         };
 
         static final OptionType<Format.EntryPoint> ENTRY = new OptionType<Format.EntryPoint>() {
             @Override
             final void stringify(Format.EntryPoint value, StringBuilder sb) {
                 String name = "entry";
                 sb.append("/" + name + "=");
                 value.stringify(sb);
             }
 
             @Override
             final Format.EntryPoint parse(String spec) throws OptionError {
                 return Format.EntryPoint.parse(spec);
             }
 
             @Override
             Option<Format.EntryPoint> parse(Option<Format.EntryPoint> option, String newValue) throws OptionError {
                 return new Option.EntryPoints(option.name, ((Option.EntryPoints) option).addValue(parse(newValue)));
             }
         };
 
         private static final Map<String, Simple> simpleTypes = new TreeMap<String, Simple>();
         static {
             for (Field field : OptionType.class.getDeclaredFields()) {
                 SimpleTypeName ann = field.getAnnotation(SimpleTypeName.class);
                 if (ann != null) {
                     assert !simpleTypes.containsKey(ann.value());
                     try {
                         simpleTypes.put(ann.value(), (Simple) field.get(null));
                     } catch (IllegalArgumentException e) {
                         throw new RuntimeException("bug detected");
                     } catch (IllegalAccessException e) {
                         throw new RuntimeException("bug detected");
                     }
                 }
             }
         }
 
         /**
          * Resolves a simple (that is, non-multivalued) type by its name.
          * 
          * @param name
          *            name of the type, case sensitive
          * @return {@link Simple} instance representing the type
          * @throws OptionError
          *             if the given type name is unknown
          */
         public static final Simple getSimpleType(String name) throws OptionError {
             Simple type = simpleTypes.get(name);
             if (type == null) {
                 throw new OptionError("unknown format option type: " + name);
             }
             return type;
         }
 
         static final void listSimpleTypes() {
             for (Map.Entry entry : simpleTypes.entrySet()) {
                 System.out.println(entry.getKey());
             }
         }
     }
 
     public static abstract class Option<T> {
         final String name;
         final OptionType<T> type;
 
         Option(String name, OptionType<T> type) {
             this.name = name;
             this.type = type;
         }
 
         abstract void display(StringBuilder sb);
 
         final Option parse(String value) throws OptionError {
             return type.parse(this, value);
         }
 
         public static final class SimpleOption<T> extends Option<T> {
             public final T value;
             public final boolean explicit;
 
             SimpleOption(String name, OptionType.Simple<T> type, T value, boolean explicit) {
                 super(name, type);
                 this.value = value;
                 this.explicit = explicit;
             }
 
             @Override
             final void display(StringBuilder sb) {
                 if (explicit) {
                     sb.append("/" + name + "=");
                     stringifyValue(sb);
                 }
             }
 
             final void stringifyValue(StringBuilder sb) {
                 type.stringify(value, sb);
             }
 
             static final String hexAddress(int address) {
                 if (address >= 0 && address <= 0xFFFF) {
                     return Hex.w(address);
                 } else {
                     return Hex.t(address);
                 }
             }
         }
 
         public static final class EntryPoints extends Option<Format.EntryPoint> implements Iterable<Format.EntryPoint> {
             private final Format.EntryPoint[] values;
 
             EntryPoints(String name, Format.EntryPoint... values) {
                 super(name, OptionType.ENTRY);
                 this.values = values;
             }
 
             @Override
             final void display(StringBuilder sb) {
                 for (Format.EntryPoint entryPoint : values) {
                     OptionType.ENTRY.stringify(entryPoint, sb);
                 }
             }
 
             /**
              * Constructs an array of {@link EntryPoint}s in this option and the
              * given new entry point. Does not affect values in {@code this}.
              */
             final Format.EntryPoint[] addValue(Format.EntryPoint newEntryPoint) {
                 Format.EntryPoint[] newValues = new Format.EntryPoint[values.length + 1];
                 for (int i = 0; i < values.length; i++) {
                     newValues[i] = values[i];
                 }
                 newValues[values.length] = newEntryPoint;
                 return newValues;
             }
 
             public final Iterator<Format.EntryPoint> iterator() {
                 return new ArrayIterator<Format.EntryPoint>(values);
             }
         }
 
         static final class ArrayIterator<T> implements Iterator<T> {
             private final T[] values;
             private int currentIndex;
 
             ArrayIterator(T[] values) {
                 this.values = values;
                 currentIndex = 0;
             }
 
             public final boolean hasNext() {
                 return currentIndex < values.length;
             }
 
             public final T next() {
                 return values[currentIndex++];
             }
 
             public final void remove() {
                 throw new RuntimeException("bug detected");
             }
         }
     }
 
     /**
      * Construct a {@link Format} instance from previously parsed data.
      * 
      * Private constructor, only used via
      * {@link #parseFormatDeclaration(String, Class)}.
      */
     private Format(String nameRoot, Analyser analyser, Option... options) {
         this.nameRoot = nameRoot;
         this.analyser = analyser;
         this.options = options;
     }
 
     /**
      * Construct a {@link Format} instance by amending an existing
      * {@link Format}.
      * 
      * @param base
      *            the original {@link Format}
      * @param name
      *            name of the option to be replaced or, if multivalued, appended
      * @param value
      *            the new value as a string, will be parsed in accordance with
      *            the option's type
      * @throws OptionError
      *             if {@code base} does not have an option named {@code name} or
      *             if {@code value} can not be parsed
      */
     public Format(Format base, String name, String value) throws OptionError {
         this.nameRoot = base.nameRoot;
         this.analyser = base.analyser;
         this.options = new Option[base.options.length];
         boolean success = false;
         for (int i = 0; i < base.options.length; i++) {
             if (base.options[i].name.equals(name)) {
                 this.options[i] = base.options[i].parse(value);
                 success = true;
             } else {
                 this.options[i] = base.options[i];
             }
         }
         if (!success) {
             throw new OptionError("option /" + name + "=... is not applicable for this file format");
         }
     }
 
     /**
      * Construct a string representation of this format. Implicit options do not
      * appear in the result.
      * 
      * @return string representation of the format
      */
     @Override
     public final String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(nameRoot);
         for (Option option : options) {
             option.display(sb);
         }
         return sb.toString();
     }
 
     // Unfortunately, Java 1.5 does not provide ServiceLoader.
     // This here is a minimalist alternative.
     /**
      * Given a class, return a list of its declared providers. The mechanism is
      * inspired by Java 1.6's {@code ServiceLoader} and the declaration
      * interface should be compatible. The querying interface is deliberately
      * much simpler.
      * 
      * @param service
      *            ancestral class of interface declaring the service
      * @return {@link List} of providers of the given service, instantiated via
      *         the parameterless constructor of each service provider class
      */
     public static final <T> List<T> getProviders(Class<T> service) throws IOException, ClassNotFoundException {
         ClassLoader classLoader = Main.class.getClassLoader();
         /*
          * Supposedly, {@link Class#getClassLoader()} can sometimes return
          * {@code null} instead of an actual class loader, and whether it ever
          * does that is an "implementation detail". WTF? If Java can do things
          * like this, it's hypocritical to leave out an interface to ioctl:s
          * merely because some OSes don't have them.
          */
         if (classLoader == null) {
             classLoader = ClassLoader.getSystemClassLoader();
         }
         Enumeration<URL> resources = classLoader.getResources("META-INF/services/" + service.getName());
         List<T> providers = new ArrayList<T>();
         while (resources.hasMoreElements()) {
             BufferedReader reader = new BufferedReader(new InputStreamReader(resources.nextElement().openStream()));
             String line;
             while ((line = reader.readLine()) != null) {
                 int commentStart = line.indexOf('#');
                 if (commentStart != -1) {
                     line = line.substring(0, commentStart);
                 }
                 line = line.trim();
                 if (line.length() != 0) {
                     @SuppressWarnings("unchecked")
                     Class<T> c = (Class<T>) classLoader.loadClass(line);
                     try {
                         providers.add(c.newInstance());
                     } catch (InstantiationException e) {
                         throw new RuntimeException("unable to instantiate service " + service.getName() + " provider: "
                                 + c.getName(), e);
                     } catch (IllegalAccessException e) {
                         throw new RuntimeException("unable to instantiate service " + service.getName() + " provider: "
                                 + c.getName(), e);
                     }
                 }
             }
             reader.close();
         }
         return providers;
     }
 
     /**
      * This annotation is for use on analyser classes. It holds the format
      * declaration string (see {@link #parseFormatDeclaration(String, Class})
      * and possible aliases, if any. When a single analyser can deal with
      * multiple formats, use {@link Optionses} to bypass Java's one annotation
      * limit.
      * 
      * When the format declaration string's root or any aliases begins with a
      * period, this format will automatically be used on files having such a
      * filename suffix. This is not a feature of format strings but the guessing
      * mechanism which uses the filename suffix, folded to lowercase, as the
      * starting point.
      * 
      * Note that merely annotating an analyser class is not sufficient for it to
      * be visible, it also needs to be declared as a provider of the
      * {@link Analyser} service. See {@link #getProviders(Class)} for details of
      * our provider detection mechanism.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.TYPE)
     public static @interface Options {
         String value();
 
         String[] aliases() default {};
     }
 
     /**
      * An annotation to hold multiple {@link Options} instances for analyser
      * classes that can deal with multiple formats.
      */
     @Retention(RetentionPolicy.RUNTIME)
     @Target(ElementType.TYPE)
     public static @interface Optionses {
         Options[] value();
     }
 
     private static final Map<String, Format> formatsByName = new HashMap<String, Format>();
 
     static {
         try {
             for (Analyser provider : getProviders(Analyser.class)) {
                 Options options = provider.getClass().getAnnotation(Options.class);
                 Optionses optionses = provider.getClass().getAnnotation(Optionses.class);
                 if (options == null && optionses == null) {
                     throw new RuntimeException("declared analyser class lacks both @Options and @Optionses: "
                             + provider.getClass().getName());
                 }
                 if (options != null) {
                     processAnalyserOptionAnnotation(provider, options);
                 }
                 if (optionses != null) {
                     for (Options oneOptions : optionses.value()) {
                         processAnalyserOptionAnnotation(provider, oneOptions);
                     }
                 }
             }
         } catch (IOException e) {
             throw new RuntimeException("I/O error while trying to determine available analysers", e);
         } catch (ClassNotFoundException e) {
             throw new RuntimeException("declared analyser not found", e);
         } catch (OptionError e) {
             throw new RuntimeException("bug detected", e);
         }
     }
 
     private static final void processAnalyserOptionAnnotation(Analyser provider, Options annotation)
             throws RuntimeException, OptionError {
         Format format = parseFormatDeclaration(annotation.value(), provider);
         if (formatsByName.containsKey(format.nameRoot)) {
             throw new RuntimeException("duplicate contender for the base name " + format.nameRoot + ": "
                     + provider.getClass().getName());
         }
         formatsByName.put(format.nameRoot, format);
         for (String alias : annotation.aliases()) {
             if (formatsByName.containsKey(alias)) {
                 throw new RuntimeException("duplicate contender for the base name " + alias + ": "
                         + provider.getClass().getName());
             }
             formatsByName.put(alias, format);
         }
     }
 
     static final Format guess(String filename, @SuppressWarnings("unused") byte[] fileData) {
         int suffixStart = filename.lastIndexOf('.');
         if (suffixStart != -1) {
             String suffix = filename.substring(suffixStart).toLowerCase();
             Format format = formatsByName.get(suffix);
             if (format != null) {
                 Format newFormat = format.analyser.taste(format, filename);
                 assert newFormat.analyser.getClass() == format.analyser.getClass();
                 return newFormat;
             }
         }
         return getGuaranteedFormat("binary");
     }
 
     public final Format imposeDecodingIfExplicit(Format child) {
         Option.SimpleOption decodingOption;
         try {
             decodingOption = (Option.SimpleOption) getOption("decoding");
         } catch (UnknownOption e) {
             return child;
         }
         if (decodingOption.explicit) {
             try {
                 return new Format(child, "decoding", ((Decoding) decodingOption.value).name);
             } catch (OptionError e) {
                 // there's a format which does not support the /decoding option?
                 throw new RuntimeException("bug detected", e);
             }
         } else {
             return child;
         }
     }
 
     // hexdump asks for origin but not all formats have it. So, by default, we
     // will return zero.
     public final int getOrigin() {
         try {
             return getIntegerOption("origin");
         } catch (UnknownOption e) {
             return 0;
         }
     }
 
     public final Option getOption(String optionName) throws UnknownOption {
         for (Option option : options) {
             if (option.name.equals(optionName)) {
                 return option;
             }
         }
         throw new UnknownOption("unknown option /" + optionName);
     }
 
     public final int getIntegerOption(String optionName) throws UnknownOption {
         return ((Integer) ((Option.SimpleOption) getOption(optionName)).value).intValue();
     }
 
     public final Decoding getDecoding() {
         try {
             return (Decoding) ((Option.SimpleOption) getOption("decoding")).value;
         } catch (UnknownOption e) {
             try {
                 return Decoding.MANAGER.get("ascii");
             } catch (ResourceManager.ResolutionError e1) {
                 throw new RuntimeException("loading the ASCII decoding failed???", e1);
             }
         }
     }
 
     /**
      * Parse a format change request.
      * 
      * @param changeSpec
      *            specification of the changes to be performed (if starts with a
      *            slash) or the whole new format string (otherwise).
      *            {@code null} or an empty string indicates that no changes are
      *            to be performed.
      * @return new format
      * @throws OptionError
      */
     public final Format parseChange(String changeSpec) throws OptionError {
         if (changeSpec == null || changeSpec.length() == 0) {
             return this;
         }
         int slash = findSlash(changeSpec, 0);
         Format format;
         if (slash != 0) {
             String baseFormatName = changeSpec.substring(0, slash);
             format = Format.formatsByName.get(baseFormatName);
             if (format == null) {
                 throw new RuntimeException("unknown format specifier base: " + baseFormatName);
             }
         } else {
             format = this;
         }
         while (slash < changeSpec.length()) {
             int nextSlash = changeSpec.indexOf('/', slash + 1);
             if (nextSlash == -1) {
                 nextSlash = changeSpec.length();
             }
             String option = changeSpec.substring(slash + 1, nextSlash);
             int eq = option.indexOf('=');
             if (eq == -1) {
                 // We don't currently support *any* parameterless options.
                 throw new OptionError("option /" + option + " is not applicable for this file format");
             }
             format = new Format(format, option.substring(0, eq), option.substring(eq + 1));
             slash = nextSlash;
         }
         return format;
     }
 
     static final Format parseFormatDeclaration(String decl, Analyser analyser) throws OptionError {
         assert decl != null;
         if (decl.length() == 0) {
             throw new RuntimeException("empty format declaration string");
         }
         int slash = findSlash(decl, 0);
         if (slash == 0) {
             throw new RuntimeException("missing format base name: " + decl);
         }
         String formatNameRoot = decl.substring(0, slash);
         ArrayList<Option> options = new ArrayList<Option>();
         Map<String, Integer> seenOptions = new TreeMap<String, Integer>(); // name => index
         while (slash < decl.length()) {
             int nextSlash = findSlash(decl, slash + 1);
             if (nextSlash == -1) {
                 nextSlash = decl.length();
             }
             String optionDecl = decl.substring(slash + 1, nextSlash);
             // The general format of an option is {@code
             // name[!][:type][=value]}.
             int eq = optionDecl.indexOf('=');
             int colon = optionDecl.indexOf(':');
             if (eq != -1 && colon > eq) {
                 // the first colon appears after the first '='  it is not the
                 // type separator, it's a part of the value
                 colon = -1;
             }
             boolean explicit;
             int endOfName;
             if (colon > 0 && optionDecl.charAt(colon - 1) == '!') {
                 /*
                  * Exclamation point after name indicates that the parameter is
                  * to be considered explicit even though it appears in the
                  * implicit format string.
                  */
                 explicit = true;
                 endOfName = colon - 1;
             } else {
                 explicit = false;
                 endOfName = colon;
             }
             String name = optionDecl.substring(0, endOfName);
             String optionTypeName;
             if (colon >= 0) {
                 optionTypeName = eq != -1 ? optionDecl.substring(colon + 1, eq) : optionDecl.substring(colon + 1);
             } else {
                 optionTypeName = null;
             }
             if (optionTypeName == null) {
                 throw new OptionError("missing type: " + optionDecl);
             }
             String optionValue = eq != -1 ? optionDecl.substring(eq + 1) : null;
             if (seenOptions.containsKey(name)) {
                 throw new OptionError("duplicate format option declaration: " + optionDecl);
             }
             seenOptions.put(name, new Integer(options.size()));
             /*
              * Special case: the optionTypeName "entry" declares a multivalued
              * entry point option and does not take a value.
              * 
              * XXX: We don't currently support actual multiple values of
              * multivalued options in format declaration strings, only in format
              * change strings.
              */
             if (optionTypeName.equals("entry")) {
                 if (optionValue == null && explicit) {
                     throw new OptionError("explicit no-value in option declaration: " + optionDecl);
                 }
                 Option.EntryPoints option = new Option.EntryPoints(optionTypeName);
                 if (optionValue != null) {
                     EntryPoint value = (EntryPoint) ((OptionType) OptionType.ENTRY).parse(optionValue);
                     if (explicit) {
                         value = value.makeExplicit();
                     }
                     option = new Option.EntryPoints(optionTypeName, option.addValue(value));
                 }
                 options.add(option);
             } else {
                 if (optionValue == null) {
                     throw new OptionError("invalid format option declaration: " + optionDecl);
                 }
                 OptionType.Simple type = resolveType(optionTypeName);
                 @SuppressWarnings("unchecked")
                 Option.SimpleOption option = new Option.SimpleOption(name, type, type.parse(optionValue), explicit);
                 options.add(option);
             }
             slash = nextSlash;
         }
         return new Format(formatNameRoot, analyser, options.toArray(new Option[0]));
     }
 
     static final OptionType.Simple resolveType(String optionTypeName) throws OptionError {
         return OptionType.getSimpleType(deparen(optionTypeName));
     }
 
     private static final String deparen(String s) {
         if (isParenthesised(s)) {
             return s.substring(1, s.length() - 1);
         } else {
             return s;
         }
     }
 
     private static final boolean isParenthesised(String s) {
         if (s.length() >= 2 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
             int level = 0;
             for (int i = 0; i < s.length(); i++) {
                 char c = s.charAt(i);
                 if (c == '(') {
                     level++;
                 } else if (c == ')') {
                     level--;
                     if (level < 0) {
                         return false;
                     }
                     if (level == 0 && i != s.length() - 1) {
                         // the (foo)bar(baz) case
                         return false;
                     }
                 }
             }
             return level == 0;
         } else {
             return false;
         }
     }
 
     /**
      * Finds the first slash in {@code s} starting from {@code startPos},
      * skipping slashes in matching parentheses. Checks parenthesis balance.
      * 
      * @param s
      *            string to scan
      * @param startPos
      *            first position of {@code s} where a slash is to be considered
      * @return position of the found slash, or length of {@code s} if no further
      *         slashes were detected
      * @throws OptionError
      *             in case of parenthesis mismatch
      */
     static final int findSlash(String s, int startPos) throws OptionError {
         int parenBalance = 0;
         for (int i = startPos; i < s.length(); i++) {
             switch (s.charAt(i)) {
                 case '/':
                     if (parenBalance == 0) {
                         return i;
                     }
                     break;
                 case '(':
                     parenBalance++;
                     break;
                 case ')':
                     if (parenBalance == 0) {
                         throw new OptionError("parentheses out of balance");
                     }
                     parenBalance--;
                     break;
             }
         }
         if (parenBalance != 0) {
             throw new OptionError("parentheses out of balance");
         }
         return s.length();
     }
 
     /**
      * Represents an entry point into a memory image. In formats supporting
      * entry points, several are usually permitted.
      */
     public static final class EntryPoint {
         public final int address;
         public final Disassembler.Lang lang;
         final boolean explicit;
 
         EntryPoint(int address, Disassembler.Lang lang, boolean explicit) {
             this.address = address;
             this.lang = lang;
             this.explicit = explicit;
         }
 
         final EntryPoint makeExplicit() {
             return new EntryPoint(address, lang, true);
         }
 
         /**
          * Stringifies this entry point and appends it to the given
          * {@link StringBuilder}.
          */
         final void stringify(StringBuilder sb) {
             sb.append("0x");
             if (address >= 0 && address <= 0xFFFF) {
                 sb.append(Hex.w(address));
             } else {
                 sb.append(Hex.t(address));
             }
             if (lang != null) {
                 sb.append(':');
                 sb.append(lang.name);
             }
         }
 
         /**
          * Parses an entry point specification.
          */
         static final EntryPoint parse(String spec) throws OptionError {
             int colon = spec.indexOf(':');
             String addressPart;
             String langPart;
             if (colon != -1) {
                 addressPart = spec.substring(0, colon);
                 langPart = spec.substring(colon + 1);
             } else {
                 addressPart = spec;
                 langPart = null;
             }
             int address = Format.OptionType.UNSIGNED_HEX_INTEGER.parse(addressPart).intValue();
             Disassembler.Lang lang = langPart != null ? Format.OptionType.LANG.parse(langPart) : null;
             return new Format.EntryPoint(address, lang, true);
         }
     }
 
     /**
      * Represents a hierarchy level for a geometry. Currently only be used by
      * {@link BlockedBinaryAnalyser}.
      */
     public static final class GeometryLevel {
         public final String name;
         public final int size;
         public final int first;
 
         public GeometryLevel(String name, int size, int first) {
             this.name = name;
             this.size = size;
             this.first = first;
         }
 
         /**
          * Stringifies this geometry level and appends it to the given
          * {@link StringBuilder}.
          */
         final void stringify(StringBuilder sb) {
             sb.append(size);
             sb.append(',');
             sb.append(name);
             sb.append(',');
             sb.append(first);
         }
 
         /**
          * Parses a geometry level specification.
          */
         static final GeometryLevel parse(String spec) throws OptionError {
             int firstColon = spec.indexOf(':');
             // Note that if firstColon is -1, so will be secondColon.
             int secondColon = spec.indexOf(':', firstColon + 1);
             if (secondColon == -1) {
                 throw new OptionError("geometry level parse error");
             }
             String sizeSpec = spec.substring(0, firstColon);
             int size = Format.OptionType.UNSIGNED_DECIMAL_INTEGER.parse(sizeSpec).intValue();
             String name = spec.substring(firstColon + 1, secondColon);
             String firstSpec = spec.substring(secondColon + 1);
             int first = Format.OptionType.UNSIGNED_DECIMAL_INTEGER.parse(firstSpec).intValue();
             return new GeometryLevel(name, size, first);
         }
     }
 
     public static final class UnknownOption extends RuntimeException {
         UnknownOption(String message) {
             super(message);
         }
     }
 
     /**
      * Fetches a format by its short name and assumes it is known.
      * 
      * @param name
      *            plain, unoptioned name of the format
      * @return requested format
      * @throws RuntimeException
      *             if the assumption turns out to be incorrect
      */
     public static final Format getGuaranteedFormat(String name) throws RuntimeException {
         Format format = formatsByName.get(name);
         if (format == null) {
             throw new RuntimeException("bug detected, known format unknown: " + name);
         }
         return format;
     }
 
     public static final class OptionError extends Exception {
         OptionError(String msg) {
             super(msg);
         }
 
         OptionError(String msg, Exception cause) {
             super(msg, cause);
         }
     }
 }
