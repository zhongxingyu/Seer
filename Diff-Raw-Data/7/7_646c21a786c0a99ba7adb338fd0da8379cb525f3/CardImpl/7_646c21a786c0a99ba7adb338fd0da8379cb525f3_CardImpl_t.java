 package de.bjoernthalheim.asterixpuzzle.deck;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.bjoernthalheim.asterixpuzzle.solution.Orientation;
 
 /**
  * Implementation of a card.
  * 
  * @author bjoern
  */
 public class CardImpl implements Card {
 	
	private final Map<Orientation, FigureAndHalf> edges;
 
 	/**
 	 * Create a card accoringto the given spec.
 	 * 
 	 * @param spec A spec in the form (Figure/Orientation)^4, e.g. atOBMblT.
 	 */
 	public CardImpl(String spec) {
 		edges = new HashMap<Orientation, FigureAndHalf>();
 		String northSideSpec = spec.substring(0, 2);
 		String eastSideSpec = spec.substring(2, 4);
 		String southSideSpec = spec.substring(4, 6);
 		String westSideSpec = spec.substring(6, 8);
 		initEdge(northSideSpec, Orientation.NORTH);
 		initEdge(eastSideSpec, Orientation.EAST);
 		initEdge(southSideSpec, Orientation.SOUTH);
 		initEdge(westSideSpec, Orientation.WEST);
 	}
 
 	/**
 	 * Create a copy of the given card.
 	 * 
 	 * @param card The card which shall be cloned.
 	 */
 	public CardImpl(Card card) {
		edges = new HashMap<Orientation, FigureAndHalf>();
 		Orientation[] orientations = Orientation.values();
 		for (Orientation orientation : orientations) {
			edges.put(orientation, card.getEdge(orientation));
 		}
 	}
 
 	/**
 	 * Init the edge according to the given String.
 	 * 
 	 * @param spec The spec containing two chars for Figure/Half.
 	 * @param orientation The side of the card where the given figure and half shall be in.
 	 */
 	private void initEdge(String spec, Orientation orientation) {
 		Figure figure = Figure.fromChar(spec.charAt(0));
 		Half half = Half.fromChar(spec.charAt(1));
 		FigureAndHalf figureAndHalf = new FigureAndHalf(figure, half);
 		edges.put(orientation , figureAndHalf);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see de.bjoernthalheim.asterixpuzzle.deck.Card#getEdge(de.bjoernthalheim.asterixpuzzle.solution.Orientation)
 	 */
 	@Override
 	public FigureAndHalf getEdge(Orientation orientation) {
 		return this.edges.get(orientation);
 	}
 
 }
