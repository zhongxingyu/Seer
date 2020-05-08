 package net.sf.anathema.charms.character.sheet.generic;
 
 import java.util.Collection;
 
 import net.sf.anathema.basics.eclipse.extension.UnconfiguredExecutableExtension;
 import net.sf.anathema.character.core.character.ICharacter;
 import net.sf.anathema.character.sheet.common.IDynamicPdfContentBoxEncoder;
 import net.sf.anathema.character.sheet.common.IEncodeContext;
 import net.sf.anathema.character.sheet.elements.Bounds;
 import net.sf.anathema.charms.character.model.GenericCharmCollector;
 
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.pdf.BaseFont;
 import com.lowagie.text.pdf.PdfContentByte;
 
 public class GenericCharmEncoder extends UnconfiguredExecutableExtension implements IDynamicPdfContentBoxEncoder {
   @Override
   public String getHeader(ICharacter character) {
     return Messages.GenericCharmEncoder_Header;
   }
 
   @Override
   public void encode(PdfContentByte directContent, IEncodeContext context, ICharacter character, Bounds bounds)
       throws DocumentException {
     BaseFont baseFont = context.getBaseFont();
     Collection<String> genericCharms = collect(character);
     new GenericCharmTableEncoder(baseFont, genericCharms).encode(directContent, character, bounds);
   }
 
   private Collection<String> collect(ICharacter character) {
    return new GenericCharmCollector(character).getUnvirtualGenericIdPatterns();
   }
 
   @Override
   public float getHeight(ICharacter character) {
     Collection<String> generics = collect(character);
     return 55 + generics.size() * 11;
   }
 }
