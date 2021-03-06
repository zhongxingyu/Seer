 /*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Bastian
 Website : http://www.gephi.org
 
 This file is part of Gephi.
 
 Gephi is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 Gephi is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.gephi.preview;
 
 import com.itextpdf.text.FontFactory;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.BaseFont;
 import com.itextpdf.text.pdf.PdfContentByte;
 
 import java.awt.geom.AffineTransform;
 import org.gephi.preview.api.PDFTarget;
 import org.gephi.preview.api.PreviewModel;
 import org.gephi.preview.api.PreviewProperties;
 import org.gephi.preview.api.RenderTarget;
 import org.gephi.preview.spi.RenderTargetBuilder;
 import org.gephi.utils.progress.Progress;
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 import org.openide.util.lookup.ServiceProvider;
 
 /**
  * Default implementation to PDFRenderTargetBuilder.
  * 
  * @author Mathieu Bastian
  */
 @ServiceProvider(service = RenderTargetBuilder.class)
 public class PDFRenderTargetBuilder implements RenderTargetBuilder {
 
     @Override
     public String getName() {
         return RenderTarget.PDF_TARGET;
     }
 
     @Override
     public RenderTarget buildRenderTarget(PreviewModel previewModel) {
         double width = previewModel.getDimensions().getWidth();
         double height = previewModel.getDimensions().getHeight();
         width = Math.max(1, width);
         height = Math.max(1, height);
         int topLeftX = previewModel.getTopLeftPosition().x;
         int topLeftY = previewModel.getTopLeftPosition().y;
         PreviewProperties properties = previewModel.getProperties();
         float marginBottom = properties.getFloatValue(PDFTarget.MARGIN_BOTTOM);
         float marginLeft = properties.getFloatValue(PDFTarget.MARGIN_LEFT);
         float marginRight = properties.getFloatValue(PDFTarget.MARGIN_RIGHT);
         float marginTop = properties.getFloatValue(PDFTarget.MARGIN_TOP);
         Rectangle pageSize = properties.getValue(PDFTarget.PAGESIZE);
         boolean landscape = properties.getBooleanValue(PDFTarget.LANDSCAPE);
         PdfContentByte cb = properties.getValue(PDFTarget.PDF_CONTENT_BYTE);
         PDFRenderTargetImpl renderTarget = new PDFRenderTargetImpl(cb, width, height, topLeftX, topLeftY,
                 pageSize, marginLeft, marginRight, marginTop, marginBottom, landscape);
         return renderTarget;
     }
 
     public static class PDFRenderTargetImpl extends AbstractRenderTarget implements PDFTarget {
 
         private final PdfContentByte cb;
         private static boolean fontRegistered = false;
         //Parameters
         private final float marginTop;
         private final float marginBottom;
         private final float marginLeft;
         private final float marginRight;
         private final boolean landscape;
         private final Rectangle pageSize;
 
         public PDFRenderTargetImpl(PdfContentByte cb, double width, double height, double topLeftX, double topLeftY,
                 Rectangle size, float marginLeft, float marginRight, float marginTop, float marginBottom, boolean landscape) {
             this.cb = cb;
             this.marginTop = marginTop;
             this.marginLeft = marginLeft;
             this.marginBottom = marginBottom;
             this.marginRight = marginRight;
             this.pageSize = size;
             this.landscape = landscape;
 
             double centerX = topLeftX + width / 2;
             double centerY = topLeftY + height / 2;
 
             //Transform
             double pageWidth = size.getWidth() - marginLeft - marginRight;
             double pageHeight = size.getHeight() - marginTop - marginBottom;
             double ratioWidth = pageWidth / width;
             double ratioHeight = pageHeight / height;
             double scale = (float) (ratioWidth < ratioHeight ? ratioWidth : ratioHeight);
             double translateX = (marginLeft + pageWidth / 2.) / scale;
             double translateY = (marginBottom + pageHeight / 2.) / scale;
            cb.transform(AffineTransform.getTranslateInstance(-centerX * scale, -centerY * scale));
             cb.transform(AffineTransform.getScaleInstance(scale, scale));
             cb.transform(AffineTransform.getTranslateInstance(translateX, translateY));
 
             FontFactory.register("/org/gephi/preview/fonts/LiberationSans.ttf", "ArialMT");
         }
 
         @Override
         public PdfContentByte getContentByte() {
             return this.cb;
         }
 
         @Override
         public BaseFont getBaseFont(java.awt.Font font) {
             try {
                 if (font != null) {
                     BaseFont baseFont = null;
                     if (!font.getFontName().equals(FontFactory.COURIER)
                             && !font.getFontName().equals(FontFactory.COURIER_BOLD)
                             && !font.getFontName().equals(FontFactory.COURIER_OBLIQUE)
                             && !font.getFontName().equals(FontFactory.COURIER_BOLDOBLIQUE)
                             && !font.getFontName().equals(FontFactory.HELVETICA)
                             && !font.getFontName().equals(FontFactory.HELVETICA_BOLD)
                             && !font.getFontName().equals(FontFactory.HELVETICA_BOLDOBLIQUE)
                             && !font.getFontName().equals(FontFactory.HELVETICA_OBLIQUE)
                             && !font.getFontName().equals(FontFactory.SYMBOL)
                             && !font.getFontName().equals(FontFactory.TIMES_ROMAN)
                             && !font.getFontName().equals(FontFactory.TIMES_BOLD)
                             && !font.getFontName().equals(FontFactory.TIMES_ITALIC)
                             && !font.getFontName().equals(FontFactory.TIMES_BOLDITALIC)
                             && !font.getFontName().equals(FontFactory.ZAPFDINGBATS)
                             && !font.getFontName().equals(FontFactory.COURIER_BOLD)
                             && !font.getFontName().equals(FontFactory.COURIER_BOLD)
                             && !font.getFontName().equals(FontFactory.COURIER_BOLD)) {
 
                         com.itextpdf.text.Font itextFont = FontFactory.getFont(font.getFontName(), BaseFont.IDENTITY_H, font.getSize(), font.getStyle());
                         baseFont = itextFont.getBaseFont();
                         if (baseFont == null && !PDFRenderTargetImpl.fontRegistered) {
 
                             if (progressTicket != null) {
                                 String displayName = progressTicket.getDisplayName();
                                 Progress.setDisplayName(progressTicket, NbBundle.getMessage(PDFRenderTargetImpl.class, "PDFRenderTargetImpl.font.registration"));
                                 FontFactory.registerDirectories();
                                 Progress.setDisplayName(progressTicket, displayName);
                             }
 
                             itextFont = FontFactory.getFont(font.getFontName(), BaseFont.IDENTITY_H, font.getSize(), font.getStyle());
                             baseFont = itextFont.getBaseFont();
 
                             PDFRenderTargetImpl.fontRegistered = true;
                         }
                     } else {
                         com.itextpdf.text.Font itextFont = FontFactory.getFont(font.getFontName(), BaseFont.IDENTITY_H, font.getSize(), font.getStyle());
                         baseFont = itextFont.getBaseFont();
                     }
 
                     if (baseFont != null) {
                         return baseFont;
                     }
                     return BaseFont.createFont();
                 }
                 return BaseFont.createFont();
             } catch (Exception e) {
                 Exceptions.printStackTrace(e);
             }
             return null;
         }
 
         @Override
         public float getMarginBottom() {
             return marginBottom;
         }
 
         @Override
         public float getMarginLeft() {
             return marginLeft;
         }
 
         @Override
         public float getMarginRight() {
             return marginRight;
         }
 
         @Override
         public float getMarginTop() {
             return marginTop;
         }
 
         @Override
         public boolean isLandscape() {
             return landscape;
         }
 
         @Override
         public Rectangle getPageSize() {
             return pageSize;
         }
     }
 }
