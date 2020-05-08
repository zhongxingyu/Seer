 package fit;
 
 import java.io.InputStream;
 import java.util.HashMap;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Unmarshaller;
 
 import com.seitenbau.testing.aliasconfigurationfixture.Alias;
 import com.seitenbau.testing.aliasconfigurationfixture.AliasConfiguration;
 import com.seitenbau.testing.aliasconfigurationfixture.IndexAlias;
 
 import net.sourceforge.xmlfit.fit.IReplace;
 import net.sourceforge.xmlfit.fit.ReplaceParser;
 import fit.ColumnFixture;
 import fit.Parse;
 import fit.TypeAdapter;
 
 public class ColumnSpecialValuesFixture extends ColumnFixture
 {
 
   public static final String SPECIAL_NULL = "#{NULL}";
 
   public static final String SPECIAL_EMPTY = "#{EMPTY}";
 
   private HashMap<String, String> fAliasMap = null;
 
   @Override
   public void doCells(Parse cells)
   {
     super.doCells(cells);
   }
 
   @Override
   public void check(Parse cells, TypeAdapter a)
   {
     // create wrapped Parse Object
     Parse cell = ReplaceParser.wrap(cells, new IReplace()
     {
       public String replaceAll(String source, int index)
       {
         return replace(source, index);
       }
     });
    String unmapped = cells.text();
    if (unmapped.equals(SPECIAL_EMPTY))
     {
       compareEmpty(cell, a, "");
     }
    else if (unmapped.equals(SPECIAL_NULL))
     {
       compareNull(cell, a);
     }
     else
     {
       super.check(cell, a);
     }
   }
 
   public void wrong(Parse cell, String actual)
   {
     String key = null;
     if(cell instanceof ReplaceParser) {
       key = ((ReplaceParser)cell).getUndecoratedParseObject().text();
     }
     if (key == null || !getMapping().containsKey(key))
     {
       super.wrong(cell, actual);
       return;
     }
     
     wrong(cell);
     cell.addToBody(label("expected") + "<hr>"
         + "key [" + key + "] resolved as [" + cell.text() + "] " 
         + "<hr>" + escape(actual) + label("actual"));
   }
 
   protected String replace(String value, int index)
   {
     String newValue = getMapping().get(value);
     if (newValue != null)
     {
       return newValue;
     }
     if (value.equals(SPECIAL_EMPTY))
     {
       return "";
     }
     if (value.equals(SPECIAL_NULL))
     {
       return null;
     }
     return value;
   }
 
   public void compareNull(Parse cell, TypeAdapter a)
   {
     try
     {
       Object result = a.get();
       if (result == null)
       {
         right(cell);
       }
       else
       {
         wrong(cell, a.toString(result));
       }
     }
     catch (Exception e)
     {
       exception(cell, e);
     }
   }
 
   public void compareEmpty(Parse cell, TypeAdapter a, String text)
   {
     try
     {
       Object result = a.get();
       if (a.equals(a.parse(text), result))
       {
         right(cell);
       }
       else
       {
         wrong(cell, a.toString(result));
       }
     }
     catch (Exception e)
     {
       exception(cell, e);
     }
   }
 
   public HashMap<String, String> getMapping()
   {
     if (fAliasMap == null)
     {
       loadMapping("/element-alias-mapping.xml");
     }
     return fAliasMap;
   }
 
   @SuppressWarnings("unchecked")
   public void loadMapping(String filename)
   {
     fAliasMap = new HashMap<String, String>();
     try
     {
       JAXBContext ctx = JAXBContext.newInstance(AliasConfiguration.class
           .getPackage().getName());
       Unmarshaller um = ctx.createUnmarshaller();
       InputStream stream = AliasConfiguration.class
           .getResourceAsStream(filename);
       JAXBElement<AliasConfiguration> obj = (JAXBElement<AliasConfiguration>) um
           .unmarshal(stream);
       AliasConfiguration cfg = obj.getValue();
       for (Alias alias : cfg.getAliasAndIndexAlias())
       {
         if (alias instanceof IndexAlias)
         {
         }
         else
         {
           String name = alias.getName();
           String value = alias.getValue();
           fAliasMap.put(name, value);
         }
       }
     }
     catch (Exception e)
     {
       e.printStackTrace();
       throw new RuntimeException(e);
     }
   }
 
 }
