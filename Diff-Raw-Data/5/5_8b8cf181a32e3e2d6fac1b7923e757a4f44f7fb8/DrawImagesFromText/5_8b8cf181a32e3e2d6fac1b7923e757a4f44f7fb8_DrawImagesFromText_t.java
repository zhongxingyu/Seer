 package org.bitstrings.maven.plugins.splasher;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.bitstrings.maven.plugins.splasher.FlowLayout.Alignment;
 
 public class DrawImagesFromText
     extends Drawable
 {
     // - parameters --[
 
     private String text;
 
     private String imageNamePrefix;
 
     private int padding;
 
     private Alignment alignment = Alignment.HORIZONTAL;
 
     // ]--
 
     protected FlowLayout drawImagesFromText;
 
     public String getText()
     {
         return text;
     }
 
     public String getImageNamePrefix()
     {
         return imageNamePrefix;
     }
 
     public int getPadding()
     {
         return padding;
     }
 
     public Alignment getAlignment()
     {
         return alignment;
     }
 
     @Override
     public void init( Graphics2D g )
         throws MojoExecutionException
     {
         drawImagesFromText = new FlowLayout();
 
         drawImagesFromText.setDrawingContext( dwContext );
         drawImagesFromText.setPosition( getPosition() );
         drawImagesFromText.setAlignment( alignment );
         drawImagesFromText.setPadding( padding );
 
         final List<Drawable> drawables = new ArrayList<Drawable>();
 
         for ( int i = 0; i < text.length(); i++ )
         {
             DrawImage drawImage = new DrawImage();
 
             drawImage.setImageName( imageNamePrefix + text.charAt( i ) );
 
             drawables.add( drawImage );
         }
 
         drawImagesFromText.setDraw( drawables );
 
         Graphics2D sg = (Graphics2D) g.create();
 
         try
         {
             drawImagesFromText.init( sg );
         }
         finally
         {
             sg.dispose();
         }

        dwBounds.setBounds( drawImagesFromText.getBounds() );
     }
 
     @Override
     public void render( Graphics2D g )
     {
         Graphics2D sg = (Graphics2D) g.create();
 
         try
         {
             drawImagesFromText.draw( sg );
         }
         finally
         {
             sg.dispose();
         }
     }
 }
