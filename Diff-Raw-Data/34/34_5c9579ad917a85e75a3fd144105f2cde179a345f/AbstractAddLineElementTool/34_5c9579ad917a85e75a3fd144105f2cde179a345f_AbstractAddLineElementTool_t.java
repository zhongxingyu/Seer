 package controller.game.edition.tools;
 
 import static controller.game.edition.ConnectionRules.canConnectLineElements;
 import static controller.game.edition.tools.Colors.*;
 import static controller.game.edition.tools.LineElementRecognizer.recognizeLineElement;
 import static model.production.elements.ProductionLineElement.connectLineElements;
 import static model.utils.StringUtils.join;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.game.Player;
 import model.production.Direction;
 import model.production.elements.ProductionLineElement;
 import model.warehouse.Position;
 import model.warehouse.TileElement;
 import controller.game.GamePanelController;
 import controller.game.edition.ConnectionRules;
 import controller.game.edition.EditionTool;
 
 public abstract class AbstractAddLineElementTool extends EditionTool {
 
 	private ProductionLineElement lineElement;
 
 	public AbstractAddLineElementTool(GamePanelController gamePanelController,
 			Player game) {
 		this(gamePanelController, game, null);
 	}

 	public AbstractAddLineElementTool(GamePanelController gamePanelController,
 			Player game, ProductionLineElement startElement) {
 		super(gamePanelController, game);
 		this.lineElement = startElement;
 	}
 
 	@Override
 	public void reset() {
 		this.lineElement = createLineElement();
 	}
 
 	@Override
 	public void paint(Graphics2D graphics) {
 		Position mousePosition = this.getGroundPanel()
 				.getCurrentMousePosition();
 		if (mousePosition != null) {
 
 			List<String> warnings = new ArrayList<String>();
 			boolean canPutElement = canPutElementAt(mousePosition, warnings);
 			boolean enoughMoney = haveEnoughMoney();
 			Color color = canPutElement && enoughMoney ? OK_COLOR : BAD_COLOR;
 
 			if (!enoughMoney)
 				warnings.add("Not enough money!");
 
 			drawWarnings(graphics, warnings);
 			drawElementRectagle(graphics, mousePosition, color);
 			drawInputArrow(graphics, mousePosition, color);
 			drawOutputArrow(graphics, mousePosition, color);
 		}
 	}
 
 	@Override
 	public void mouseClicked(Position position) {
 		if (canPutElementAt(position) && haveEnoughMoney()) {
 			putLineElementAt(position, this.lineElement);
 			tryConnectInput(position);
 			tryConnectOutput(position);
 
 			this.lineElement = createLineElement();
 		}
 	}
 
	protected void putLineElementAt(Position position,
			ProductionLineElement element) {
 		getPlayer().buyAndAddProductionLineElement(element, position);
 	}
 
 	protected abstract ProductionLineElement createLineElement();
 
 	private void drawWarnings(Graphics2D graphics, List<String> warnings) {
 		if (!warnings.isEmpty())
 			getGroundPanel().drawNotificationBesideMouse(join(warnings, " - "),
 					graphics);
 	}
 
 	private void drawElementRectagle(Graphics2D graphics,
 			Position mousePosition, Color color) {
 		int width = this.lineElement.getWidth();
 		int height = this.lineElement.getHeight();
 		getGroundPanel().drawRectangle(graphics, mousePosition, width, height,
 				color);
 	}
 
 	private void drawInputArrow(Graphics2D graphics, Position mousePosition,
 			Color color) {
 		if (this.lineElement.canHavePreviousLineElement()) {
 			Position inPos = getInputConnectionPosition(mousePosition);
 			Direction inDir = this.lineElement.getInputConnectionDirection();
 			getGroundPanel().drawInputArrow(inPos, inDir, color, graphics);
 		}
 	}
 
 	private void drawOutputArrow(Graphics2D graphics, Position mousePosition,
 			Color color) {
 		if (this.lineElement.canHaveNextLineElement()) {
 			Position outPos = getOutputConnectionPosition(mousePosition);
 			Direction outDir = this.lineElement.getOutputConnectionDirection();
 			getGroundPanel().drawOutputArrow(outPos, outDir, color, graphics);
 		}
 	}
 
 	private void tryConnectInput(Position position) {
 		if (this.lineElement.canHavePreviousLineElement()) {
 			ProductionLineElement inputLineElement = searchForInputElement(position);
 			if (inputLineElement != null)
 				connectLineElements(inputLineElement, this.lineElement,
 						ConnectionRules.getInstance());
 		}
 	}
 
 	private void tryConnectOutput(Position position) {
 		if (this.lineElement.canHaveNextLineElement()) {
 			ProductionLineElement outputLineElement = searchForOutputElement(position);
 			if (outputLineElement != null)
 				connectLineElements(this.lineElement, outputLineElement,
 						ConnectionRules.getInstance());
 		}
 	}
 
 	private boolean canPutElementAt(Position position) {
 		return canPutElementAt(position, null);
 	}
 
 	private boolean canPutElementAt(Position position, List<String> whyNot) {
 		int width = this.lineElement.getWidth();
 		int height = this.lineElement.getHeight();
 
 		boolean insideBounds = getGround().isAreaInsideBounds(width, height,
 				position);
 		if (!insideBounds && whyNot != null)
 			whyNot.add("Out of bounds");
 
 		boolean canPutElement = getGround().canAddTileElementByDimension(width,
 				height, position);
 		if (!canPutElement && insideBounds && whyNot != null)
 			whyNot.add("Area not empty");
 
 		return canPutElement && canPutInput(position, whyNot)
 				&& canPutOutput(position, whyNot);
 	}
 
 	private boolean canPutInput(Position position, List<String> whyNot) {
 		if (!this.lineElement.canHavePreviousLineElement())
 			return true;
 		Position inputPos = getInputConnectionPosition(position);
 		boolean emptyArea = getGround().canAddTileElementByDimension(1, 1,
 				inputPos);
 		boolean canConnect = canConnectInput(position);
 
 		boolean canPutInput = emptyArea || canConnect;
 		if (!canPutInput && whyNot != null)
 			whyNot.add("Cannot connect input");
 
 		return canPutInput;
 	}
 
 	private boolean canPutOutput(Position position, List<String> whyNot) {
 		if (!this.lineElement.canHaveNextLineElement())
 			return true;
 		Position outputPos = getOutputConnectionPosition(position);
 		boolean emptyArea = getGround().canAddTileElementByDimension(1, 1,
 				outputPos);
 		boolean canConnect = canConnectOutput(position);
 
 		boolean canPutOutput = emptyArea || canConnect;
 		if (!canPutOutput && whyNot != null)
 			whyNot.add("Cannot connect output");
 
 		return canPutOutput;
 	}
 
 	private Position getInputConnectionPosition(Position position) {
 		return position.add(this.lineElement
 				.getInputConnectionRelativePosition());
 	}
 
 	private Position getOutputConnectionPosition(Position position) {
 		return position.add(this.lineElement
 				.getOutputConnectionRelativePosition());
 	}
 
 	private boolean canConnectInput(Position position) {
 		return searchForInputElement(position) != null;
 	}
 
 	private boolean canConnectOutput(Position position) {
 		return searchForOutputElement(position) != null;
 	}
 
 	private ProductionLineElement searchForInputElement(Position position) {
 		Position inputPos = getInputConnectionPosition(position);
 		Direction inputDir = this.lineElement.getInputConnectionDirection();
 		TileElement tileElement = getGround().getTileElementAt(inputPos);
 
 		ProductionLineElement inputElement = recognizeLineElement(tileElement);
 		boolean canConnectByRule = canConnectLineElements(inputElement,
 				this.lineElement);
		if (inputElement == null || !inputElement.canHaveNextLineElement()
				|| inputElement.hasNextLineElement() || !canConnectByRule) {
 			inputElement = null;
 		} else {
 			Position expectedOutputPos = inputPos.subtract(inputDir
 					.getAssociatedPosition());
 			boolean canConnect = inputElement.getOutputConnectionPosition()
 					.equals(expectedOutputPos);
 			if (!canConnect)
 				inputElement = null;
 		}
 		return inputElement;
 	}
 
 	private ProductionLineElement searchForOutputElement(Position position) {
 		Position outputPos = getOutputConnectionPosition(position);
 		Direction outputDir = this.lineElement.getOutputConnectionDirection();
 		TileElement tileElement = getGround().getTileElementAt(outputPos);
 
 		ProductionLineElement outputElement = recognizeLineElement(tileElement);
 		boolean canConnectByRule = canConnectLineElements(this.lineElement,
 				outputElement);
		if (outputElement == null
				|| !outputElement.canHavePreviousLineElement()
				|| outputElement.hasPreviousLineElement() || !canConnectByRule) {
 			outputElement = null;
 		} else {
 			Position expectedInputPos = outputPos.subtract(outputDir
 					.getAssociatedPosition());
 			boolean canConnect = outputElement.getInputConnectionPosition()
 					.equals(expectedInputPos);
 			if (!canConnect)
 				outputElement = null;
 		}
 		return outputElement;
 	}
 
 	protected boolean haveEnoughMoney() {
 		return this.getPlayer().canAfford(this.lineElement.getPurchasePrice());
 	}
 
 }
