 package j3chess.systems;
 
 import j3chess.J3ChessApp;
 import j3chess.components.Paintable;
 import j3chess.components.Position;
 
 import java.awt.Image;
 
 import artemis.Aspect;
 import artemis.ComponentMapper;
 import artemis.Entity;
 import artemis.annotations.Mapper;
 import artemis.systems.EntityProcessingSystem;
 
 /**
  * entity processing system which draws every paintable entity at its
  * corresponding position.
  */
 public class PaintSystem extends EntityProcessingSystem {
 
     /** @brief fast component mapper to retrieve paintable component */
     @Mapper
     private ComponentMapper<Paintable> mPaintableMapper;
 
     /** @brief fast component mapper to retrieve position component */
     @Mapper
     private ComponentMapper<Position> mPositionMapper;
 
     /**
      * @brief the paint system draws every paintable component with a
      *        position.
      */
     public PaintSystem() {
         super(Aspect.getAspectForAll(Position.class));
     }
 
     /**
      * @brief draws a paintable component at its respective position.
      * @param entity the paintable entity to process
      * @see artemis.systems.EntityProcessingSystem#process(artemis.Entity)
      */
     @Override
     protected final void process(final Entity entity) {
         final Paintable paintable = mPaintableMapper.get(entity);
         final Position position = mPositionMapper.get(entity);
 
         final Image image = paintable.getImage();
 
 		// TODO sensible image drawing code
		if (img != null) {
			J3ChessApp.getInstance().getDrawGraphics().drawImage(img, 0, 0, null);
 		}
 	}
 
 }
