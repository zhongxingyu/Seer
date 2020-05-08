 package org.blockout.logic.validator;
 
 import javax.inject.Inject;
 
 import org.blockout.common.TileCoordinate;
 import org.blockout.world.IWorld;
 import org.blockout.world.event.CrateOpenedEvent;
 import org.blockout.world.event.IEvent;
 import org.blockout.world.state.IEventValidator;
 import org.blockout.world.state.ValidationResult;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CrateValidator implements IEventValidator {
 
 	private static final Logger	logger;
 	static {
 		logger = LoggerFactory.getLogger( CrateValidator.class );
 	}
 
 	protected IWorld			world;
 
 	@Inject
 	public CrateValidator(final IWorld world) {
 		this.world = world;
 	}
 
 	@Override
 	public ValidationResult validateEvent( final IEvent<?> event ) {
 		if ( !(event instanceof CrateOpenedEvent) ) {
 			return ValidationResult.Unknown;
 		}
 
 		CrateOpenedEvent coe = (CrateOpenedEvent) event;
 
 		TileCoordinate crateTile = world.findTile( coe.getCrate() );
 		TileCoordinate playerTile = world.findTile( coe.getPlayer() );
 
		if ( crateTile == null || playerTile == null ) {
			logger.info( "Crate has been removed." );
			return ValidationResult.Invalid;
		}

 		if ( crateTile.isNeighbour( playerTile ) ) {
 			return ValidationResult.Valid;
 		}
 
 		logger.info( "Openening crate denied. " + coe.getPlayer() + " is not next to " + coe.getCrate() );
 		return ValidationResult.Invalid;
 	}
 }
