 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package ui.hud.infobar;
 
 // TODO: A 1px high bar turns weird color-wise from time to time (probably because of aa)
 
 import game.components.ComponentMessage;
 import game.components.ComponentType;
 import game.components.interfaces.IRenderComponent;
 import game.entities.IEntity;
 
 import java.util.ArrayList;
 
 import math.time.GameTime;
 
 import org.newdawn.slick.Graphics;
 
 public class InfoBar implements IRenderComponent {
   private final float          barWidth, barHeight;
   private final int            offsetX, offsetY;
   private final ArrayList<Bar> bars;
 
   public InfoBar(final float width, final float height,
                  final int offsetX, final int offsetY) {
     barWidth = width;
     barHeight = height;
 
     this.offsetX = offsetX;
     this.offsetY = offsetY;
 
     bars = new ArrayList<Bar>();
   }
 
   @Override
  public void update(final GameTime time) {
    for (final Bar b : bars) {
      b.update();
    }
   }
 
   @Override
   public void render(final Graphics g) {
     int i = 0;
     for (final Bar b : bars) {
       b.render(g, offsetX, offsetY + (barHeight * i), barWidth, barHeight);
       ++i;
     }
   }
 
   public void add(final Bar bar) {
     bars.add(bar);
   }
 
   public float getWidth() {
     return barWidth;
   }
 
   @Override
   public void reciveMessage(final ComponentMessage message, final Object args) {
     // Do nothing
   }
 
   @Override
   public ComponentType getComponentType() {
     return ComponentType.INFO_BAR;
   }
 
   @Override
   public void setOwner(final IEntity owner) {
     // Do nothing
   }
 }
