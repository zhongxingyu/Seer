 package strategy.game.version.beta.configuration;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
import strategy.common.StrategyException;
 import strategy.game.common.Location;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 import strategy.game.version.beta.board.BetaStartingBoardRuleSet;
 import strategy.game.version.board.BoardRuleSet;
 import strategy.game.version.configuration.ConfigurationRuleSet;
 
 public class BetaStartingConfigurationRuleSet implements ConfigurationRuleSet {
 
 	private BoardRuleSet startingBoardRules = new BetaStartingBoardRuleSet();
 	final private Collection<PieceType> validPieceTypes;
 
 	public BetaStartingConfigurationRuleSet(){
 		validPieceTypes = new ArrayList<PieceType>();
 		validPieceTypes.add(PieceType.MARSHAL);
 		validPieceTypes.add(PieceType.COLONEL);
 		validPieceTypes.add(PieceType.CAPTAIN);
 		validPieceTypes.add(PieceType.LIEUTENANT);
 		validPieceTypes.add(PieceType.SERGEANT);
 		validPieceTypes.add(PieceType.FLAG);
 	}
 	
 	@Override
 	public boolean validateConfiguration(Collection<PieceLocationDescriptor> configuration){
 
 		if(configuration == null){
 			return false;
 		}
 		if(configuration.size() != 12){
 			return false;
 		}
 		
 		
 		//HashSet to keep track of duplicates
 		final Set<Location> map = new HashSet<Location>();
 
 		for(PieceLocationDescriptor i : configuration){
 
 			if(!validPieceTypes.contains(i.getPiece().getType())){
 				return false;
 			}
 
			startingBoardRules.validateLocation(i);
 
 			if (map.contains(i.getLocation())){
 				return false;
 			}else{ 
 				map.add(i.getLocation());
 			}
 		}
 		
 		return true;
 	}
 		
 //		
 //		/**
 //		 * Checks the following
 //		 * @param gameStarted True if the game has started, false otherwise
 //		 * @param redConfiguration One configuration to validate
 //		 * @param blueConfiguration Another configuration to validate
 //		 * @throws StrategyException if locations overlap
 //		 */
 //		@SafeVarargs
 //		static private void checkConfigurations(boolean gameStarted, Collection<PieceType> validPieceTypes, Collection<PieceLocationDescriptor>... configurations) throws StrategyException{
 //			//HashSet to keep track of duplicates
 //			final Set<Location> map = new HashSet<Location>();
 //
 //			for (Collection<PieceLocationDescriptor> configuration : configurations){
 //				for(PieceLocationDescriptor i : configuration){
 //					
 //					if(!validPieceTypes.contains(i.getPiece().getType())){
 //						throw new StrategyException("Piece is not valid for this Strategy game!");
 //					}
 //					
 //					checkLocationBoundaries(gameStarted, i.getPiece().getOwner(), i.getLocation());
 //					if (map.contains(i.getLocation())){
 //						throw new StrategyException("Two pieces are in the same location!");
 //					}else{ 
 //						map.add(i.getLocation());
 //					}
 //				}
 //			}
 //		}
 //	}
 }
