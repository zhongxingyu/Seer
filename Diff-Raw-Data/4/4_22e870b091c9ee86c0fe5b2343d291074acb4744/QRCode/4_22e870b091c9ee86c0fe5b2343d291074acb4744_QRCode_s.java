 /*
  * Copyright 2013 John Ahlroos
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
 package fi.jasoft.qrcode;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 
 import com.vaadin.data.Property;
 import com.vaadin.server.StreamResource;
 import com.vaadin.ui.AbstractField;
 
 import fi.jasoft.qrcode.client.ui.QRCodeConnector;
 import fi.jasoft.qrcode.client.ui.SizeListener;
 import fi.jasoft.qrcode.zxing.ByteMatrix;
 import fi.jasoft.qrcode.zxing.Encoder;
 import fi.jasoft.qrcode.zxing.ErrorCorrectionLevel;
 import fi.jasoft.qrcode.zxing.WriterException;
 import fi.jasoft.qrcode.zxing.ZXingQRCode;
 
 /**
  * A component for encoding values into QR coded images and embedding them into
  * Vaadin applications.
  * 
  * @author John Ahlroos (www.jasoft.fi)
  */
 @SuppressWarnings("serial")
 public class QRCode extends AbstractField<String> implements SizeListener {
 
     private final ZXingQRCode qrcode = new ZXingQRCode();
 
     private static final Logger logger = Logger.getLogger(QRCode.class
             .getName());
 
     private int pixelWidth = -1;
     private int pixelHeight = -1;
     
     private Color fgColor = Color.BLACK;
     private Color bgColor = Color.WHITE;
 
     private ErrorCorrectionLevel ecl = ErrorCorrectionLevel.L;
 
     /**
      * Constructs an empty <code>QRCode</code> with no caption.
      */
     public QRCode() {
         setInternalValue("");
         registerRpc(this, SizeListener.class);
     }
 
     /**
      * Constructs an empty <code>QRCode</code> with given caption.
      * 
      * @param caption
      *            the caption <code>String</code> for the editor.
      */
     public QRCode(String caption) {
         this();
         setCaption(caption);
     }
 
     /**
      * Constructs a new <code>QRCode</code> that's bound to the specified
      * <code>Property</code> and has no caption.
      * 
      * @param dataSource
      *            the Property to be edited with this editor.
      */
     public QRCode(Property dataSource) {
         this();
         setPropertyDataSource(dataSource);
     }
 
     /**
      * Constructs a new <code>QRCode</code> that's bound to the specified
      * <code>Property</code> and has the given caption <code>String</code>.
      * 
      * @param caption
      *            the caption <code>String</code> for the editor.
      * @param dataSource
      *            the Property to be edited with this editor.
      */
     public QRCode(String caption, Property dataSource) {
         this(dataSource);
         setCaption(caption);
     }
 
     /**
      * Constructs a new <code>QRCode</code> with the given caption and initial
      * text contents. The editor constructed this way will not be bound to a
      * Property unless
      * {@link com.vaadin.data.Property.Viewer#setPropertyDataSource(Property)}
      * is called to bind it.
      * 
      * @param caption
      *            the caption <code>String</code> for the editor.
      * @param text
      *            the initial text content of the editor.
      */
     public QRCode(String caption, String value) {
        setInternalValue(value);
        setCaption(caption);
     }
     
     /*
      * (non-Javadoc)
      * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
      */
     @Override
     protected void setInternalValue(String newValue) {
     	super.setInternalValue(newValue);
     	generateQRCode();
     }
 
     /*
      * (non-Javadoc)
      * @see com.vaadin.ui.AbstractComponent#setWidth(float, com.vaadin.server.Sizeable.Unit)
      */
     @Override
     public void setWidth(float width, Unit unit) {
     	super.setWidth(width, unit);
     	generateQRCode();
     }
     
     /*
      * (non-Javadoc)
      * @see com.vaadin.ui.AbstractComponent#setHeight(float, com.vaadin.server.Sizeable.Unit)
      */
     @Override
     public void setHeight(float height, Unit unit) {
     	super.setHeight(height, unit);
     	generateQRCode();
     }
     
     private void generateQRCode() {
     	
     	if(pixelHeight < 0 || pixelWidth < 0){
     		return;
     	}
     	
     	String value = getValue();
     	if(value == null){
     		value = "";
     	}
     	
         // Try to encode
         try {
             Encoder.encode(value, ecl, qrcode);
         } catch (WriterException e1) {
             logger.log(Level.SEVERE, "Could not encode QR Code for '" + value
                     + "'", e1);
             return;
         }
 
         /*
          * Generate a unique filename for this qrcode relative to the value of
          * the qrcode and the width
          */
         String hash = value + pixelWidth + "x" + pixelHeight + fgColor.getRGB()
                 + bgColor.getRGB();
         String filename = "qrcode-"
                 + UUID.nameUUIDFromBytes(hash.getBytes()).toString() + ".png";
 
         // Create a image resource
         setResource(QRCodeConnector.RESOURCE_KEY, new StreamResource(
                 new StreamResource.StreamSource() {
                     public InputStream getStream() {
                         ByteMatrix matrix = renderResult(qrcode, pixelWidth,
                                 pixelHeight);
                         BufferedImage image = toBufferedImage(matrix,
                                 fgColor.getRGB(), bgColor.getRGB());
                         ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
 
                         try {
                             ImageIO.write(image, "png", imagebuffer);
                             return new ByteArrayInputStream(imagebuffer
                                     .toByteArray());
                         } catch (IOException e) {
                             logger.log(Level.SEVERE,
                                     "Could not create QRCode image file", e);
                         }
                         return null;
                     }
                 }, filename));
     }
 
     /**
      * Copy&Paste from com.google.zxing.qrcode.QRCodeWriter.java
      * 
      * http://zxing.googlecode.com/svn-history/r800/trunk/core/src/com/google/
      * zxing/qrcode/QRCodeWriter.java
      * 
      */
     private static final int QUIET_ZONE_SIZE = 4;
 
     private static ByteMatrix renderResult(ZXingQRCode code, int width,
             int height) {
         ByteMatrix input = code.getMatrix();
         int inputWidth = input.getWidth();
         int inputHeight = input.getHeight();
         int qrWidth = inputWidth + (QUIET_ZONE_SIZE << 1);
         int qrHeight = inputHeight + (QUIET_ZONE_SIZE << 1);
         int outputWidth = Math.max(width, qrWidth);
         int outputHeight = Math.max(height, qrHeight);
 
         int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
         int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
         int topPadding = (outputHeight - (inputHeight * multiple)) / 2;
 
         ByteMatrix output = new ByteMatrix(outputWidth, outputHeight);
         byte[][] outputArray = output.getArray();
         byte[] row = new byte[outputWidth];
 
         // 1. Write the white lines at the top
         for (int y = 0; y < topPadding; y++) {
             setRowColor(outputArray[y], (byte) 255);
         }
 
         // 2. Expand the QR image to the multiple
         byte[][] inputArray = input.getArray();
         for (int y = 0; y < inputHeight; y++) {
             // a. Write the white pixels at the left of each row
             for (int x = 0; x < leftPadding; x++) {
                 row[x] = (byte) 255;
             }
 
             // b. Write the contents of this row of the barcode
             int offset = leftPadding;
             for (int x = 0; x < inputWidth; x++) {
                 byte value = (inputArray[y][x] == 1) ? 0 : (byte) 255;
                 for (int z = 0; z < multiple; z++) {
                     row[offset + z] = value;
                 }
                 offset += multiple;
             }
 
             // c. Write the white pixels at the right of each row
             offset = leftPadding + (inputWidth * multiple);
             for (int x = offset; x < outputWidth; x++) {
                 row[x] = (byte) 255;
             }
 
             // d. Write the completed row multiple times
             offset = topPadding + (y * multiple);
             for (int z = 0; z < multiple; z++) {
                 System.arraycopy(row, 0, outputArray[offset + z], 0,
                         outputWidth);
             }
         }
 
         // 3. Write the white lines at the bottom
         int offset = topPadding + (inputHeight * multiple);
         for (int y = offset; y < outputHeight; y++) {
             setRowColor(outputArray[y], (byte) 255);
         }
 
         return output;
     }
 
     /**
      * Copy & Paste from com.google.zxing.qrcode.QRCodeWriter.java
      * 
      * http://zxing.googlecode.com/svn-history/r800/trunk/core/src/com/google/
      * zxing/qrcode/QRCodeWriter.java
      * 
      */
     private static void setRowColor(byte[] row, byte value) {
         for (int x = 0; x < row.length; x++) {
             row[x] = value;
         }
     }
 
     /**
      * Copy & Paste from com.google.zxing.client.j2se.MatrixToImageWriter.java
      * 
      * http://zxing.googlecode.com/svn-history/r1028/trunk/javase/src/com/google
      * /zxing/client/j2se/MatrixToImageWriter.java
      */
     protected static BufferedImage toBufferedImage(ByteMatrix matrix,
             int fgColor, int bgColor) {
         int width = matrix.getWidth();
         int height = matrix.getHeight();
         BufferedImage image = new BufferedImage(width, height,
                 BufferedImage.TYPE_INT_ARGB);
         for (int x = 0; x < width; x++) {
             for (int y = 0; y < height; y++) {
                 image.setRGB(x, y, matrix.get(x, y) == 0 ? fgColor : bgColor);
             }
         }
         return image;
     }
 
     /**
      * Set the color which will be the primary color of the QR Code. By default
      * this is black. This color should be darker than the secondary color.
      * 
      * @param color
      *            The color to use as the primary color
      */
     public void setPrimaryColor(Color color) {
         if (color == null) {
             fgColor = Color.BLACK;
         } else {
             fgColor = color;
         }
         generateQRCode();
     }
 
     /**
      * Get the color which will be the primary color of the QR Code. By default
      * this is black.
      */
     public Color getPrimaryColor() {
         return fgColor;
     }
 
     /**
      * Set the color which wll be the secondary, or background color, of the QR
      * Code. By default this is white. THis color should be lighter than the
      * primary color.
      * 
      * @param color
      *            The color to use as the secondary color
      */
     public void setSecondaryColor(Color color) {
         if (color == null) {
             bgColor = Color.WHITE;
         } else {
             bgColor = color;
         }
         generateQRCode();
     }
     
     /**
      * Returns the error correction level. Default is
      * {@link ErrorCorrectionLevel#L}
      * 
      * @return
      */
     protected ErrorCorrectionLevel getEcl() {
         return ecl;
     }
 
     /**
      * Set the error correction level. Default is {@link ErrorCorrectionLevel#L}
      * 
      * @param ecl
      *            The error correction level to use.
      */
     protected void setEcl(ErrorCorrectionLevel ecl) {
         this.ecl = ecl;
         generateQRCode();
     }
 
     /*
      * (non-Javadoc)
      * @see com.vaadin.ui.AbstractField#getType()
      */
 	@Override
 	public Class<? extends String> getType() {		
 		return String.class;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see fi.jasoft.qrcode.client.ui.SizeListener#sizeChanged(int, int)
 	 */
 	@Override
 	public void sizeChanged(int width, int height) {
 		pixelWidth = width;
 		pixelHeight = height;
 		generateQRCode();
 	}
 }
