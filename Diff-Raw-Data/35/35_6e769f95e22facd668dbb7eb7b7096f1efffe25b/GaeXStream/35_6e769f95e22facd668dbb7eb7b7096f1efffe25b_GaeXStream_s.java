 package nl.gridshore.companyhr.axongeahacks;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.basic.*;
 import com.thoughtworks.xstream.converters.collections.*;
 import com.thoughtworks.xstream.converters.enums.EnumConverter;
 import com.thoughtworks.xstream.converters.extended.*;
 import com.thoughtworks.xstream.converters.reflection.*;
 import com.thoughtworks.xstream.core.util.ClassLoaderReference;
 import com.thoughtworks.xstream.io.xml.XppDriver;
 import com.thoughtworks.xstream.mapper.Mapper;
 
 /**
  * @author Jettro Coenradie
  */
 public class GaeXStream extends XStream {
    private ClassLoaderReference classLoaderReference;
 
     public GaeXStream(PureJavaReflectionProvider pureJavaReflectionProvider, XppDriver xppDriver,
                       ClassLoaderReference classLoaderReference) {
         super(pureJavaReflectionProvider, xppDriver, classLoaderReference);
        this.classLoaderReference = classLoaderReference;
     }
 
 
     @Override
     protected void setupConverters() {
         Mapper mapper = getMapper();
         ReflectionProvider reflectionProvider = getReflectionProvider();
 
         final ReflectionConverter reflectionConverter =
                 new ReflectionConverter(mapper, reflectionProvider);
         registerConverter(reflectionConverter, PRIORITY_VERY_LOW);
 
         registerConverter(new SerializableConverter(mapper, reflectionProvider), PRIORITY_LOW);
         registerConverter(new ExternalizableConverter(mapper), PRIORITY_LOW);
 
         registerConverter(new NullConverter(), PRIORITY_VERY_HIGH);
         registerConverter(new IntConverter(), PRIORITY_NORMAL);
         registerConverter(new FloatConverter(), PRIORITY_NORMAL);
         registerConverter(new DoubleConverter(), PRIORITY_NORMAL);
         registerConverter(new LongConverter(), PRIORITY_NORMAL);
         registerConverter(new ShortConverter(), PRIORITY_NORMAL);
         registerConverter((Converter) new CharConverter(), PRIORITY_NORMAL);
         registerConverter(new BooleanConverter(), PRIORITY_NORMAL);
         registerConverter(new ByteConverter(), PRIORITY_NORMAL);
 
         registerConverter(new StringConverter(), PRIORITY_NORMAL);
         registerConverter(new StringBufferConverter(), PRIORITY_NORMAL);
         registerConverter(new DateConverter(), PRIORITY_NORMAL);
         registerConverter(new BitSetConverter(), PRIORITY_NORMAL);
         registerConverter(new URLConverter(), PRIORITY_NORMAL);
         registerConverter(new BigIntegerConverter(), PRIORITY_NORMAL);
         registerConverter(new BigDecimalConverter(), PRIORITY_NORMAL);
 
         registerConverter(new ArrayConverter(mapper), PRIORITY_NORMAL);
         registerConverter(new CharArrayConverter(), PRIORITY_NORMAL);
         registerConverter(new CollectionConverter(mapper), PRIORITY_NORMAL);
         registerConverter(new MapConverter(mapper), PRIORITY_NORMAL);
         registerConverter(new TreeMapConverter(mapper), PRIORITY_NORMAL);
         registerConverter(new TreeSetConverter(mapper), PRIORITY_NORMAL);
         registerConverter(new EncodedByteArrayConverter(), PRIORITY_NORMAL);
 
         registerConverter(new FileConverter(), PRIORITY_NORMAL);
 
        registerConverter(new DynamicProxyConverter(mapper, classLoaderReference), PRIORITY_NORMAL);
        registerConverter(new JavaClassConverter(classLoaderReference), PRIORITY_NORMAL);
        registerConverter(new JavaMethodConverter(classLoaderReference), PRIORITY_NORMAL);
         registerConverter(new LocaleConverter(), PRIORITY_NORMAL);
         registerConverter(new GregorianCalendarConverter(), PRIORITY_NORMAL);
 
         registerConverter(new UUIDConverter(), PRIORITY_NORMAL);
 //        registerConverter(new StringBuilderConverter(), PRIORITY_NORMAL);
         registerConverter(new EnumConverter(), PRIORITY_NORMAL);
 //        registerConverter(new EnumSetConverter(getMapper()), PRIORITY_NORMAL);
 //        registerConverter(new EnumMapConverter(getMapper()), PRIORITY_NORMAL);
         registerConverter(new SelfStreamingInstanceChecker(reflectionConverter, this), PRIORITY_NORMAL);
 
     }
 
 }
