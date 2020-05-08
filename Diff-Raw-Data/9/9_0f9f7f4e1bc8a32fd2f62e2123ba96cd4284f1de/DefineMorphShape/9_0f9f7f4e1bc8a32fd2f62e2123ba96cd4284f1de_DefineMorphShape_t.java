 /*
  * DefineMorphShape.java
  * Transform
  *
  * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of Flagstone Software Ltd. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.flagstone.transform.shape;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 
 import com.flagstone.transform.coder.CoderException;
 import com.flagstone.transform.coder.Context;
 import com.flagstone.transform.coder.DefineTag;
 import com.flagstone.transform.coder.Encoder;
 import com.flagstone.transform.coder.MovieTypes;
 import com.flagstone.transform.coder.SWFDecoder;
 import com.flagstone.transform.coder.SWFEncoder;
 import com.flagstone.transform.coder.SWFFactory;
 import com.flagstone.transform.datatype.Bounds;
 import com.flagstone.transform.exception.IllegalArgumentRangeException;
 import com.flagstone.transform.fillstyle.FillStyle;
 import com.flagstone.transform.linestyle.MorphLineStyle;
 
 /**
  * DefineMorphShape defines a shape that will morph from one form into another.
  *
  * <p>
  * Only the start and end shapes are defined the Flash Player will perform the
  * interpolation that transforms the shape at each staging in the morphing
  * process.
  * </p>
  *
  * <p>
  * Morphing can be applied to any shape, however there are a few restrictions:
  * </p>
  *
  * <ul>
  * <li>The start and end shapes must have the same number of edges (Line and
  * Curve objects).</li>
  * <li>The fill style (Solid, Bitmap or Gradient) must be the same in the start
  * and end shape.</li>
  * <li>If a bitmap fill style is used then the same image must be used in the
  * start and end shapes.</li>
  * <li>If a gradient fill style is used then the gradient must contain the same
  * number of points in the start and end shape.</li>
  * <li>The start and end shape must contain the same set of ShapeStyle objects.</li>
  * </ul>
  *
  * <p>
  * To perform the morphing of a shape the shape is placed in the display list
  * using a PlaceObject2 object. The ratio attribute in the PlaceObject2 object
  * defines the progress of the morphing process. The ratio ranges between 0 and
  * 65535 where 0 represents the start of the morphing process and 65535, the
  * end.
  * </p>
  *
  * <p>
  * The edges in the shapes may change their type when a shape is morphed.
  * Straight edges can become curves and vice versa.
  * </p>
  *
  */
 //TODO(class)
 public final class DefineMorphShape implements DefineTag {
     private static final String FORMAT = "DefineMorphShape: { identifier=%d;"
             + " startBounds=%s; endBounds=%s; fillStyles=%s; lineStyles=%s;"
             + " startShape=%s; endShape=%s }";
 
     private int identifier;
     private Bounds startBounds;
     private Bounds endBounds;
 
     private List<FillStyle> fillStyles;
     private List<MorphLineStyle> lineStyles;
 
     private Shape startShape;
     private Shape endShape;
 
     private transient int length;
     private transient int fillBits;
     private transient int lineBits;
 
     /**
      * Creates and initialises a DefineMorphShape object using values encoded
      * in the Flash binary format.
      *
      * @param coder
      *            an SWFDecoder object that contains the encoded Flash data.
      *
      * @param context
      *            a Context object used to manage the decoders for different
      *            type of object and to pass information on how objects are
      *            decoded.
      *
      * @throws CoderException
      *             if an error occurs while decoding the data.
      */
     // TODO(optimise)
     public DefineMorphShape(final SWFDecoder coder, final Context context)
             throws CoderException {
         final int start = coder.getPointer();
 
         length = coder.readWord(2, false) & 0x3F;
 
         if (length == 0x3F) {
             length = coder.readWord(4, false);
         }
         final int end = coder.getPointer() + (length << 3);
 
         final Map<Integer, Integer> vars = context.getVariables();
         vars.put(Context.TRANSPARENT, 1);
         vars.put(Context.ARRAY_EXTENDED, 1);
         vars.put(Context.TYPE, MovieTypes.DEFINE_MORPH_SHAPE);
 
         identifier = coder.readWord(2, false);
 
         startBounds = new Bounds(coder);
         endBounds = new Bounds(coder);
         fillStyles = new ArrayList<FillStyle>();
         lineStyles = new ArrayList<MorphLineStyle>();
 
         // offset to the start of the second shape
         coder.readWord(4, false);
         // final int first = coder.getPointer();
 
         int fillStyleCount = coder.readByte();
 
         if (vars.containsKey(Context.ARRAY_EXTENDED)
                 && (fillStyleCount == 0xFF)) {
             fillStyleCount = coder.readWord(2, false);
         }
 
         final SWFFactory<FillStyle> decoder = context.getRegistry()
                 .getMorphFillStyleDecoder();
 
         FillStyle fillStyle;
         int type;
 
         for (int i = 0; i < fillStyleCount; i++) {
             type = coder.scanByte();
             fillStyle = decoder.getObject(coder, context);
 
             if (fillStyle == null) {
                 throw new CoderException(String.valueOf(type), start >>> 3, 0,
                         0, "Unsupported FillStyle");
             }
 
             fillStyles.add(fillStyle);
         }
 
         int lineStyleCount = coder.readByte();
 
         if (vars.containsKey(Context.ARRAY_EXTENDED)
                 && (lineStyleCount == 0xFF)) {
             lineStyleCount = coder.readWord(2, false);
         }
 
         for (int i = 0; i < lineStyleCount; i++) {
             lineStyles.add(new MorphLineStyle(coder, context));
         }
 
         if (context.getRegistry().getShapeDecoder() == null) {
             startShape = new Shape();
             startShape.add(new ShapeData(new byte[length
                     - ((coder.getPointer() - start) >> 3)]));
 
             endShape = new Shape();
             endShape.add(new ShapeData(new byte[length
                     - ((coder.getPointer() - start) >> 3)]));
         } else {
             startShape = new Shape(coder, context);
             endShape = new Shape(coder, context);
         }
 
         vars.remove(Context.TRANSPARENT);
         vars.put(Context.ARRAY_EXTENDED, 1);
         vars.remove(Context.TYPE);
 
         if (coder.getPointer() != end) {
            final int delta = (coder.getPointer() - end) >> 3;
            if (delta == -33) {
                coder.setPointer(end);
            } else {
                throw new CoderException(getClass().getName(), start >> 3, length,
                        (coder.getPointer() - end) >> 3);
            }
         }
     }
 
     /**
      * Creates a DefineMorphShape object.
      *
      * @param uid
      *            an unique identifier for this object. Must be in the range
      *            1..65535.
      * @param startBounds
      *            the bounding rectangle enclosing the start shape. Must not be
      *            null.
      * @param endBounds
      *            the bounding rectangle enclosing the end shape. Must not be
      *            null.
      * @param fills
      *            an array of MorphSolidFill, MorphBitmapFill and
      *            MorphGradientFill objects. Must not be null.
      * @param lines
      *            an array of MorphLineStyle objects. Must not be null.
      * @param startShape
      *            the shape at the start of the morphing process. Must not be
      *            null.
      * @param endShape
      *            the shape at the end of the morphing process. Must not be
      *            null.
      */
     public DefineMorphShape(final int uid, final Bounds startBounds,
             final Bounds endBounds, final List<FillStyle> fills,
             final List<MorphLineStyle> lines, final Shape startShape,
             final Shape endShape) {
         setIdentifier(uid);
         setStartBounds(startBounds);
         setEndBounds(endBounds);
         setFillStyles(fills);
         setLineStyles(lines);
         setStartShape(startShape);
         setEndShape(endShape);
     }
 
     /**
      * Creates and initialises a DefineMorphShape object using the values copied
      * from another DefineMorphShape object.
      *
      * @param object
      *            a DefineMorphShape object from which the values will be
      *            copied.
      */
     public DefineMorphShape(final DefineMorphShape object) {
         identifier = object.identifier;
         startBounds = object.startBounds;
         endBounds = object.endBounds;
         fillStyles = new ArrayList<FillStyle>(object.fillStyles.size());
         for (final FillStyle style : object.fillStyles) {
             fillStyles.add(style.copy());
         }
         lineStyles = new ArrayList<MorphLineStyle>(object.lineStyles.size());
         for (final MorphLineStyle style : object.lineStyles) {
             lineStyles.add(style.copy());
         }
         startShape = object.startShape.copy();
         endShape = object.endShape.copy();
     }
 
     /** TODO(method). */
     public int getIdentifier() {
         return identifier;
     }
 
     /** TODO(method). */
     public void setIdentifier(final int uid) {
         if ((uid < 1) || (uid > 65535)) {
              throw new IllegalArgumentRangeException(1, 65536, uid);
         }
         identifier = uid;
     }
 
     /**
      * Returns the width of the shape at the start of the morphing process.
      */
     public int getWidth() {
         return startBounds.getWidth();
     }
 
     /**
      * Returns the height of the shape at the start of the morphing process.
      */
     public int getHeight() {
         return startBounds.getHeight();
     }
 
     /**
      * Add a LineStyle object to the array of line styles.
      *
      * @param aLineStyle
      *            and LineStyle object. Must not be null.
      */
     public DefineMorphShape add(final MorphLineStyle aLineStyle) {
         lineStyles.add(aLineStyle);
         return this;
     }
 
     /**
      * Add the fill style object to the array of fill styles.
      *
      * @param aFillStyle
      *            an FillStyle object. Must not be null.
      */
     public DefineMorphShape add(final FillStyle aFillStyle) {
         fillStyles.add(aFillStyle);
         return this;
     }
 
     /**
      * Returns the Bounds object that defines the bounding rectangle enclosing
      * the start shape.
      */
     public Bounds getStartBounds() {
         return startBounds;
     }
 
     /**
      * Returns the Bounds object that defines the bounding rectangle enclosing
      * the end shape.
      */
     public Bounds getEndBounds() {
         return endBounds;
     }
 
     /**
      * Returns the array of fill styles (MorphSolidFill, MorphBitmapFill and
      * MorphGradientFill objects) for the shapes.
      */
     public List<FillStyle> getFillStyles() {
         return fillStyles;
     }
 
     /**
      * Returns the array of line styles (MorphLineStyle objects) for the shapes.
      */
     public List<MorphLineStyle> getLineStyles() {
         return lineStyles;
     }
 
     /**
      * Returns the starting shape.
      */
     public Shape getStartShape() {
         return startShape;
     }
 
     /**
      * Returns the ending shape.
      */
     public Shape getEndShape() {
         return endShape;
     }
 
     /**
      * Sets the starting bounds of the shape.
      *
      * @param aBounds
      *            the bounding rectangle enclosing the start shape. Must not be
      *            null.
      */
     public void setStartBounds(final Bounds aBounds) {
         if (aBounds == null) {
             throw new NullPointerException();
         }
         startBounds = aBounds;
     }
 
     /**
      * Sets the ending bounds of the shape.
      *
      * @param aBounds
      *            the bounding rectangle enclosing the end shape. Must not be
      *            null.
      */
     public void setEndBounds(final Bounds aBounds) {
         if (aBounds == null) {
             throw new NullPointerException();
         }
         endBounds = aBounds;
     }
 
     /**
      * Sets the array of morph fill styles.
      *
      * @param anArray
      *            an array of MorphSolidFill, MorphBitmapFill and
      *            MorphGradientFill objects. Must not be null.
      */
     public void setFillStyles(final List<FillStyle> anArray) {
         if (anArray == null) {
             throw new NullPointerException();
         }
         fillStyles = anArray;
     }
 
     /**
      * Sets the array of morph line styles.
      *
      * @param anArray
      *            an array of MorphLineStyle objects. Must not be null.
      */
     public void setLineStyles(final List<MorphLineStyle> anArray) {
         if (anArray == null) {
             throw new NullPointerException();
         }
         lineStyles = anArray;
     }
 
     /**
      * Sets the shape that will be displayed at the start of the morphing
      * process.
      *
      * @param aShape
      *            the shape at the start of the morphing process. Must not be
      *            null.
      */
     public void setStartShape(final Shape aShape) {
         if (aShape == null) {
             throw new NullPointerException();
         }
         startShape = aShape;
     }
 
     /**
      * Sets the shape that will be displayed at the end of the morphing process.
      *
      * @param aShape
      *            the shape at the end of the morphing process. Must not be
      *            null.
      */
     public void setEndShape(final Shape aShape) {
         if (aShape == null) {
             throw new NullPointerException();
         }
         endShape = aShape;
     }
 
     /** TODO(method). */
     public DefineMorphShape copy() {
         return new DefineMorphShape(this);
     }
 
     @Override
     public String toString() {
         return String.format(FORMAT, identifier, startBounds, endBounds,
                 fillStyles, lineStyles, startShape, endShape);
     }
 
     // TODO(optimise)
     /** {@inheritDoc} */
     public int prepareToEncode(final SWFEncoder coder, final Context context) {
         fillBits = Encoder.unsignedSize(fillStyles.size());
         lineBits = Encoder.unsignedSize(lineStyles.size());
 
         final Map<Integer, Integer> vars = context.getVariables();
 
         if (vars.containsKey(Context.POSTSCRIPT)) {
             if (fillBits == 0) {
                 fillBits = 1;
             }
 
             if (lineBits == 0) {
                 lineBits = 1;
             }
         }
 
         vars.put(Context.TRANSPARENT, 1);
 
         length = 2 + startBounds.prepareToEncode(coder, context);
         length += endBounds.prepareToEncode(coder, context);
         length += 4;
 
         length += (fillStyles.size() >= 255) ? 3 : 1;
 
         for (final FillStyle style : fillStyles) {
             length += style.prepareToEncode(coder, context);
         }
 
         length += (lineStyles.size() >= 255) ? 3 : 1;
 
         for (final MorphLineStyle style : lineStyles) {
             length += style.prepareToEncode(coder, context);
         }
 
         vars.put(Context.ARRAY_EXTENDED, 1);
         vars.put(Context.FILL_SIZE, fillBits);
         vars.put(Context.LINE_SIZE, lineBits);
 
         length += startShape.prepareToEncode(coder, context);
 
         // Number of Fill and Line bits is zero for end shape.
         vars.put(Context.FILL_SIZE, 0);
         vars.put(Context.LINE_SIZE, 0);
 
         length += endShape.prepareToEncode(coder, context);
 
         vars.remove(Context.ARRAY_EXTENDED);
         vars.remove(Context.TRANSPARENT);
 
         return (length > 62 ? 6 : 2) + length;
     }
 
     // TODO(optimise)
     /** {@inheritDoc} */
     public void encode(final SWFEncoder coder, final Context context)
             throws CoderException {
         final int start = coder.getPointer();
 
         if (length >= 63) {
             coder.writeWord((MovieTypes.DEFINE_MORPH_SHAPE << 6) | 0x3F, 2);
             coder.writeWord(length, 4);
         } else {
             coder.writeWord((MovieTypes.DEFINE_MORPH_SHAPE << 6) | length, 2);
         }
         final int end = coder.getPointer() + (length << 3);
 
         coder.writeWord(identifier, 2);
         final Map<Integer, Integer> vars = context.getVariables();
         vars.put(Context.TRANSPARENT, 1);
 
         startBounds.encode(coder, context);
         endBounds.encode(coder, context);
 
         final int offsetStart = coder.getPointer();
         coder.writeWord(0, 4);
 
         if (fillStyles.size() >= 255) {
             coder.writeWord(0xFF, 1);
             coder.writeWord(fillStyles.size(), 2);
         } else {
             coder.writeWord(fillStyles.size(), 1);
         }
 
         for (final FillStyle style : fillStyles) {
             style.encode(coder, context);
         }
 
         if (lineStyles.size() >= 255) {
             coder.writeWord(0xFF, 1);
             coder.writeWord(lineStyles.size(), 2);
         } else {
             coder.writeWord(lineStyles.size(), 1);
         }
 
         for (final MorphLineStyle style : lineStyles) {
             style.encode(coder, context);
         }
 
         vars.put(Context.ARRAY_EXTENDED, 1);
         vars.put(Context.FILL_SIZE, fillBits);
         vars.put(Context.LINE_SIZE, lineBits);
 
         startShape.encode(coder, context);
 
         final int offsetEnd = (coder.getPointer() - offsetStart) >> 3;
         final int currentCursor = coder.getPointer();
 
         coder.setPointer(offsetStart);
         coder.writeWord(offsetEnd - 4, 4);
         coder.setPointer(currentCursor);
 
         // Number of Fill and Line bits is zero for end shape.
 
         vars.put(Context.FILL_SIZE, 0);
         vars.put(Context.LINE_SIZE, 0);
 
         endShape.encode(coder, context);
 
         vars.remove(Context.ARRAY_EXTENDED);
         vars.remove(Context.TRANSPARENT);
 
         if (coder.getPointer() != end) {
             throw new CoderException(getClass().getName(), start >> 3, length,
                     (coder.getPointer() - end) >> 3);
         }
     }
 }
