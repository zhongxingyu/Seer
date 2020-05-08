 package com.github.wolfie.meteorcursor;
 
 import com.github.wolfie.meteorcursor.client.ui.VMeteorCursor;
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.AbstractComponent;
 
 @SuppressWarnings("serial")
 @com.vaadin.ui.ClientWidget(com.github.wolfie.meteorcursor.client.ui.VMeteorCursor.class)
 public class MeteorCursor extends AbstractComponent {
   
   private int gravity = 75;
   private int threshold = 10;
   private int particleLifetime = 1000;
   private double distanceMultiplier = 2.0d;
   private ThemeResource particleImage;
   
   @Override
   public void paintContent(final PaintTarget target) throws PaintException {
     target.addAttribute(VMeteorCursor.ATTRIBUTE_GRAVITY_INT, gravity);
     target.addAttribute(VMeteorCursor.ATTRIBUTE_THRESHOLD_INT, threshold);
     target.addAttribute(VMeteorCursor.ATTRIBUTE_PART_LIFETIME_INT,
         particleLifetime);
     target.addAttribute(VMeteorCursor.ATTRIBUTE_DISTANCE_DBL,
         distanceMultiplier);
     target.addAttribute(VMeteorCursor.ATTRIBUTE_IMAGE_RSRC, particleImage);
     
     // workaround to Vaadin not always telling whether it's enabled or not
     // TODO: Need to take a closer look at why.
     target.addAttribute(VMeteorCursor.ATTRIBUTE_VAADIN_DISABLED, !isEnabled());
   }
   
   /**
    * Enable or disable the rendering.
    * 
    * @param enabled
    *          <tt>true</tt> if you want have the {@link MeteorCursor} to render,
    *          <tt>false</tt> if you want to disable it.
    */
   @Override
   public void setEnabled(final boolean enabled) {
     // Overriding only for JavaDoc
     super.setEnabled(enabled);
   }
   
   public int getGravity() {
     return gravity;
   }
   
   /**
    * Sets the gravity for the particles.
    * 
    * @param gravity
    *          The gravity, measured by pixels downwards, during the whole
    *          lifetime of a particle. Positive gravity is downwards, negative
    *          gravity is upwards. Zero is no gravity.
    */
   public void setGravity(final int gravity) {
     this.gravity = gravity;
     requestRepaint();
   }
   
   /**
    * Sets the threshold to start rendering particles.
    * 
    * @param threshold
    *          The threshold in pixels. If the cursor is moved further than
    *          <tt>threshold</tt> pixels during one frame, a particle is drawn.
    *          For each double exceeding of <tt>treshold</tt>, additional
    *          particles are rendered.
    *          <p/>
    *          <em>Example:</em> threshold is set to 10, and the cursor is moved
    *          55px during one frame. This will render three particles: one at
    *          10px, the second at 30px and the third at 50px.
    */
   public void setThreshold(final int threshold) {
     this.threshold = threshold;
     requestRepaint();
   }
   
   public int getThreshold() {
     return threshold;
   }
   
   public int getParticleLifetime() {
     return particleLifetime;
   }
   
   /**
    * Sets the lifetime of a particle.
    * 
    * @param milliseconds
    *          How many milliseconds it takes for a particle to complete its
    *          animation cycle.
    */
   public void setParticleLifetime(final int milliseconds) {
     particleLifetime = milliseconds;
     requestRepaint();
   }
   
   /**
    * Sets the particle distance multiplier, compared to the moved cursor
    * distance.
    * 
    * @param distanceMultiplier
    *          How many times further the particles will fly compared to the
    *          length of the cursor movement.
    */
   public void setDistanceMultiplier(final double distanceMultiplier) {
     this.distanceMultiplier = distanceMultiplier;
     requestRepaint();
   }
   
   public double getDistanceMultiplier() {
     return distanceMultiplier;
   }
   
   /**
    * Set the image to be shown as the particle.
    * 
    * @param particleImage
    *          An image {@link ThemeResource}.
    * @return <tt>true</tt> if <tt>particleImage</tt>is an accepted type.
    *         <tt>false</tt> otherwise.
    */
   public boolean setParticleImage(final ThemeResource particleImage) {
    if (particleImage == null) {
      return false;
    }
    
     final String mt = particleImage.getMIMEType();
     
     // borrowed from Embedded
     if (mt.substring(0, mt.indexOf("/")).equalsIgnoreCase("image")) {
       this.particleImage = particleImage;
       requestRepaint();
       return true;
     } else {
       return false;
     }
   }
   
   public ThemeResource getParticleImage() {
     return particleImage;
   }
 }
