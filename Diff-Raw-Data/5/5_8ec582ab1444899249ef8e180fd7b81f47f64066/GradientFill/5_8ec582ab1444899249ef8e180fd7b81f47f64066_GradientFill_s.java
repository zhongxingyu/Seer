 /*
  * GradientFill.java
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
 
 package com.flagstone.transform.fillstyle;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 
 import com.flagstone.transform.coder.CoderException;
 import com.flagstone.transform.coder.Context;
 import com.flagstone.transform.coder.SWFDecoder;
 import com.flagstone.transform.coder.SWFEncoder;
 import com.flagstone.transform.datatype.CoordTransform;
 
 /**
  * GradientFill defines how a colour changes across an area to be filled with
  * colour. Two types of gradient fill are supported:
  *
  * <ol>
  * <li>Linear - where the gradient changes in one direction across the area to
  * be filled.</li>
  *
  * <li>Radial - where the gradient changes radially from the centre of the area
  * to be filled.</li>
  * </ol>
  *
  * <p>
  * Gradients are defined in terms of a standard space called the gradient
  * square, centred at (0,0) and extending from (-16384, -16384) to (16384,
  * 16384).
  * </p>
  *
  * <img src="doc-files/gradientSquare.gif">
  *
  * <p>
  * A coordinate transform is required to map the gradient square to the
  * coordinates of the filled area. The transformation is applied in two steps.
  * First the gradient square is scaled so the colour covers the shape followed
  * by a translation to map the gradient square coordinates to the coordinate
  * range of the shape.
  * </p>
  *
  * <img src="gradientMapping.gif">
  *
  * <p>
  * A series of gradient points is used to control how the colour displayed
  * changes across the gradient. At least two points are required to define a
  * gradient - one for the starting colour and one for the final colour. When the
  * Flash Player displays the control points they are sorted by the ratio defined
  * in each Gradient object, with the smallest ratio value displayed first.
  * </p>
  *
  * @see Gradient
  */
 //TODO(class)
 public final class GradientFill implements FillStyle {
 
     private static final String FORMAT = "GradientFill: { transform=%s;"
             + " gradients=%s }";
 
     private transient int type;
     private int spread;
     private int interpolation;
     private CoordTransform transform;
     private List<Gradient> gradients;
 
     private transient int count;
 
     /**
      * Creates and initialises a GradientFill fill style using values encoded
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
     public GradientFill(final SWFDecoder coder, final Context context)
             throws CoderException {
         type = coder.readByte();
         transform = new CoordTransform(coder);
         count = coder.readByte();
         spread = count & 0x00C0;
         interpolation = count & 0x0030;
        count = count & 0x00FF;
         gradients = new ArrayList<Gradient>(count);
 
         for (int i = 0; i < count; i++) {
             gradients.add(new Gradient(coder, context));
         }
     }
 
     /**
      * Creates a GradientFill object specifying the type, coordinate transform
      * and array of gradient points.
      *
      * @param type
      *            identifies whether the gradient is rendered linearly or 
      *            radially.
      * @param aTransform
      *            the coordinate transform mapping the gradient square onto
      *            physical coordinates. Must not be null.
      * @param anArray
      *            an array of Gradient objects defining the control points for
      *            the gradient. For Flash 7 and earlier versions there can be up
      *            to 8 Gradients. For Flash 8 onwards this number was increased
      *            to 15. Must not be null.
      */
     public GradientFill(final GradientType type, final CoordTransform aTransform,
             final List<Gradient> anArray) {
         setType(type);
         setTransform(aTransform);
         setGradients(anArray);
     }
 
     /**
      * Creates and initialises a GradientFill fill style using the values copied
      * from another GradientFill object.
      *
      * @param object
      *            a  GradientFill fill style from which the values will be
      *            copied.
      */
     public GradientFill(final GradientFill object) {
         type = object.type;
         transform = object.transform;
         gradients = new ArrayList<Gradient>(object.gradients);
     }
 
     /** TODO(method). */
     public GradientType getType() {
         GradientType value;
         if (type == 0x10) {
             value = GradientType.LINEAR;
         } else {
             value = GradientType.RADIAL;
         }
         return value;
     }
 
     /** TODO(method). */
     public void setType(final GradientType type) {
         switch (type) {
         case LINEAR:
             this.type = 0x10;
             break;
         default:
             this.type = 0x12;
             break;
         }
      }
 
     /** TODO(method). */
     public Spread getSpread() {
         Spread value;
         switch (spread) {
         case 0:
             value = Spread.PAD;
             break;
         case 0x40:
             value = Spread.REFLECT;
             break;
         case 0xC0:
             value = Spread.REPEAT;
             break;
         default:
             throw new IllegalStateException();
         }
         return value;
     }
 
     /** TODO(method). */
     public void setSpread(final Spread type) {
         switch (type) {
         case PAD:
             spread = 0;
             break;
         case REFLECT:
             spread = 0x40;
             break;
         case REPEAT:
             spread = 0xC0;
             break;
         default:
             throw new IllegalArgumentException();
         }
     }
 
     /** TODO(method). */
     public Interpolation getInterpolation() {
         Interpolation value;
         switch (interpolation) {
         case 0:
             value = Interpolation.NORMAL;
             break;
         case 16:
             value = Interpolation.LINEAR;
             break;
         default:
             throw new IllegalStateException();
         }
         return value;
     }
 
     /** TODO(method). */
     public void setInterpolation(final Interpolation type) {
         switch (type) {
         case NORMAL:
             interpolation = 0;
             break;
         case LINEAR:
             interpolation = 16;
             break;
         default:
             throw new IllegalArgumentException();
         }
     }
 
     /**
      * Returns the coordinate transform mapping the gradient square onto
      * physical coordinates.
      */
     public CoordTransform getTransform() {
         return transform;
     }
 
     /**
      * Returns the array of Gradient objects defining the points for the
      * gradient fill.
      */
     public List<Gradient> getGradients() {
         return gradients;
     }
 
     /**
      * Sets the coordinate transform mapping the gradient square onto physical
      * coordinates.
      *
      * @param aTransform
      *            the coordinate transform. Must not be null.
      */
     public void setTransform(final CoordTransform aTransform) {
         if (aTransform == null) {
             throw new NullPointerException();
         }
         transform = aTransform;
     }
 
     /**
      * Sets the array of control points that define the gradient. For Flash 7
      * and earlier this array can contain up to 8 Gradient objects. For Flash 8
      * onwards this limit was increased to 15.
      *
      * @param anArray
      *            an array of Gradient objects. Must not be null.
      */
     public void setGradients(final List<Gradient> anArray) {
         if (anArray == null) {
             throw new NullPointerException();
         }
         if (anArray.size() > 15) {
             throw new IllegalStateException("Maximum number of gradients exceeded.");
         }
         gradients = anArray;
     }
 
     /**
      * Add a Gradient object to the array of gradient objects. For Flash 7 and
      * earlier versions there can be up to 8 Gradients. For Flash 8 onwards this
      * number was increased to 15.
      *
      * @param aGradient
      *            an Gradient object. Must not be null.
      */
     public GradientFill add(final Gradient aGradient) {
         if (aGradient == null) {
             throw new NullPointerException();
         }
         if (gradients.size() == 15) {
             throw new IllegalStateException("Maximum number of gradients exceeded.");
         }
         gradients.add(aGradient);
         return this;
     }
 
     /** {@inheritDoc} */
     public GradientFill copy() {
         return new GradientFill(this);
     }
 
     /** {@inheritDoc} */
     @Override
     public String toString() {
         return String.format(FORMAT, transform, gradients);
     }
 
     /** {@inheritDoc} */
     public int prepareToEncode(final SWFEncoder coder, final Context context) {
         count = gradients.size();
         return 2
                 + transform.prepareToEncode(coder, context)
                 + (count * (context.getVariables().containsKey(
                         Context.TRANSPARENT) ? 5 : 4));
     }
 
     /** {@inheritDoc} */
     public void encode(final SWFEncoder coder, final Context context)
             throws CoderException {
         coder.writeByte(type);
         transform.encode(coder, context);
        coder.writeWord(count | spread | interpolation, 1);
 
         for (final Gradient gradient : gradients) {
             gradient.encode(coder, context);
         }
     }
 }
