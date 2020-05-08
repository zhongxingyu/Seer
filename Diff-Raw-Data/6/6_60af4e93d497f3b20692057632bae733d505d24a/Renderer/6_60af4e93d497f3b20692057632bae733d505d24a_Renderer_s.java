 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.rendering;
 
 import com.se.simplicity.model.Model;
 
 /**
  * <p>
  * Renders {@link com.se.simplicity.model.Model Model}s.
  * </p>
  * 
  * <p>
  * Any changes to settings made during the {@link com.se.simplicity.rendering.Renderer#init() init()} or
  * {@link com.se.simplicity.rendering.Renderer#renderModel(Model, DrawingMode) renderModel(Model, DrawingMode)} methods should be reverted during the
  * {@link com.se.simplicity.rendering.Renderer#dispose() dispose()} method. It is the responsibility of the <code>Renderer</code> to leave the
  * rendering environment as it was found (except for contents of buffers) so that multiple <code>Renderer</code>s may be used together without
  * effecting each other.
  * </p>
  * 
  * <p>
  * When used within a {@link com.se.simplicity.rendering.engine.RenderingEngine RenderingEngine}, the <code>Renderer</code> acts as a rendering pass.
  * Adding multiple <code>Renderer</code>s to a <code>RenderingEngine</code> effectively creates a multi pass rendering environment. Since lighting
  * settings may be changed by the <code>RenderingEngine</code>, they should be reverted to the state they were found in, not just the default state.
  * </p>
  * 
  * @author Gary Buyn
  */
 public interface Renderer
 {
     /**
      * <p>
      * Reverts the rendering environment.
      * </p>
      */
     void dispose();
 
     /**
      * <p>
      * Receives the {@link com.se.simplicity.rendering.DrawingMode DrawingMode} used to render the {@link com.se.simplicity.model.Model Model}s.
      * </p>
      * 
      * @return The <code>DrawingMode</code> used to render the <code>VertexGroup</code>s.
      */
     DrawingMode getDrawingMode();
 
     /**
      * <p>
      * Initialises the rendering environment.
      * </p>
      */
     void init();
 
     /**
      * <p>
     * Renders the given {@link com.se.simplicity.rendering.Model Model} with the given {@link com.se.simplicity.rendering.DrawingMode
     * DrawingMode}.
      * </p>
      * 
     * @param model The {@link com.se.simplicity.rendering.Model Model} to render.
      */
     void renderModel(Model model);
 
     /**
      * <p>
      * Sets the {@link com.se.simplicity.rendering.DrawingMode DrawingMode} used to render the {@link com.se.simplicity.model.Model Model} s.
      * </p>
      * 
      * @param mode The <code>DrawingMode</code> used to render the <code>VertexGroup</code>s.
      */
     void setDrawingMode(DrawingMode mode);
 }
