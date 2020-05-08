 package ru.socionicasys.analyst;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.socionicasys.analyst.model.AData;
 import ru.socionicasys.analyst.predicates.*;
 import ru.socionicasys.analyst.types.Aspect;
 import ru.socionicasys.analyst.types.Sign;
 
 import java.awt.Dimension;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.tree.*;
 
 /**
  * Дерево навигации по выделениям в документе, сгруппированным по функциям/размерностям/т.п.
  *
  * @author Виктор
  */
 public class ATree extends JTree {
 	private static final int MAX_PRESENTATION_CHARS = 100;
 
 	private static final String FUNCTIONS_LABEL = "Функции";
 	private static final String SIGNS_LABEL = "Знаки";
 	private static final String DIMENSIONS_LABEL = "Размерности";
 	private static final String MV_LABEL = "Ментал/Витал";
 	private static final String LOW_DIMENSION_LABEL = "Маломерность";
 	private static final String HIGH_DIMENSION_LABEL = "Многомерность";
 	private static final String DIMENSION1_LABEL = "Одномерность";
 	private static final String INDIVIDUALITY_LABEL = "Индивидуальность";
 	private static final String BLOCKS_LABEL = "Блоки";
 	private static final String DOUBT_LABEL = "Непонятные места";
 	private static final String JUMPS_LABEL = "Переводы";
 
 	private static final String HTML_CELL_OPEN = "<td align=\"center\">";
 	private static final String HTML_CELL_OPEN_STRONG = "<td style=\"font-weight:bold\">";
 	private static final String HTML_CELL_CLOSE = "</td>\n";
 	private static final String HTML_ROW_OPEN = "<tr>\n";
 	private static final String HTML_ROW_CLOSE = "</tr>\n";
 	private static final String HTML_TABLE_CLOSE = "</table>";
 
 	private static final Aspect[] ASPECTS = Aspect.values();
 
 	private static final Logger logger = LoggerFactory.getLogger(ATree.class);
 
 	private final DocumentHolder documentHolder;
 	private final DefaultMutableTreeNode rootNode;
 	private final DefaultTreeModel treeModel;
 	private final JumpCounter jumpCounter;
 	private TreePath path;
 
 	private final Map<DefaultMutableTreeNode, Predicate> nodePredicateMap;
 	private final Map<Predicate, Integer> predicateCounts;
 
 	private final DefaultMutableTreeNode doubtNode = new EndTreeNode(DOUBT_LABEL);
 	private final DefaultMutableTreeNode jumpNode = new EndTreeNode(JUMPS_LABEL);
 
 	public ATree(DocumentHolder documentHolder) {
 		this.documentHolder = documentHolder;
 		rootNode = new DefaultMutableTreeNode(documentHolder.getModel().getProperty(Document.TitleProperty));
 		treeModel = new DefaultTreeModel(rootNode);
 		setModel(treeModel);
 		documentHolder.addADocumentChangeListener(new ADocumentChangeListener() {
 			@Override
 			public void aDocumentChanged(ADocument document) {
 				updateTree(document);
 			}
 		});
 
 		jumpCounter = new JumpCounter();
 		predicateCounts = new HashMap<Predicate, Integer>();
 		nodePredicateMap = new HashMap<DefaultMutableTreeNode, Predicate>();
 
 		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		setEditable(false);
 		toggleClickCount = 1;
 		makeTreeStructure();
 		updateTree(documentHolder.getModel());
 		clearPredicateCounts();
 	}
 
 	private void clearPredicateCounts() {
 		for (Aspect aspect : ASPECTS) {
 			for (Sign sign : Sign.values()) {
 				predicateCounts.put(new SignPredicate(aspect, sign), 0);
 			}
 			for (int dimension = 1; dimension <= 4; dimension++) {
 				predicateCounts.put(new DimensionPredicate(aspect, dimension), 0);
 			}
 			predicateCounts.put(new Dimension1Predicate(aspect), 0);
 			predicateCounts.put(new LowDimensionPredicate(aspect), 0);
 			predicateCounts.put(new HighDimensionPredicate(aspect), 0);
 			predicateCounts.put(new IndividualityPredicate(aspect), 0);
 			predicateCounts.put(new SuperegoPredicate(aspect), 0);
 			predicateCounts.put(new SuperidPredicate(aspect), 0);
 			predicateCounts.put(new VitalPredicate(aspect), 0);
 			predicateCounts.put(new MentalPredicate(aspect), 0);
 			for (Aspect secondAspect : ASPECTS) {
 				predicateCounts.put(new BlockPredicate(aspect, secondAspect), 0);
 			}
 		}
 	}
 
 	private void updateTree(ADocument document) {
 		if (document == null) {
 			return;
 		}
 
 		rootNode.setUserObject(document.getProperty(Document.TitleProperty));
 		TreePath newPath = getSelectionPath();
 		if (newPath != null) {
 			path = newPath;
 		}
 
 		//Analyze document structure and update tree nodes
 		try {
 			removeAllChildren();
 			clearPredicateCounts();
 			for (Entry<DocumentSection, AData> entry : document.getADataMap().entrySet()) {
 				int sectionOffset = entry.getKey().getStartOffset();
 				int sectionLength = Math.abs(entry.getKey().getEndOffset() - sectionOffset);
 				int quoteLength = Math.min(sectionLength, MAX_PRESENTATION_CHARS);
 
 				AData data = entry.getValue();
 
 				String aspect = data.getAspect();
 				String secondAspect = data.getSecondAspect();
 				String quote = document.getText(sectionOffset, quoteLength);
 				EndNodeObject endNodeObject = new EndNodeObject(sectionOffset, String.format("...%s...", quote));
 
 				if (AData.DOUBT.equals(aspect)) {
 					doubtNode.add(new DefaultMutableTreeNode(endNodeObject, false));
 					continue;
 				}
 
 				if (AData.JUMP.equals(data.getModifier())) {
 					jumpNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset,
 							String.format(" Перевод %s -> %s", aspect, secondAspect)), false));
 					jumpCounter.addJump(secondAspect, aspect);
 				}
 
 				Collection<Predicate> predicates = SocionicsType.createPredicates(data);
 
 				for (Predicate predicate : predicates) {
 					predicateCounts.put(predicate, predicateCounts.get(predicate) + 1);
 				}
 
 				for (Entry<DefaultMutableTreeNode, Predicate> nodePredicateEntry : nodePredicateMap.entrySet()) {
 					Predicate predicate = nodePredicateEntry.getValue();
 					if (predicates.contains(predicate)) {
 						DefaultMutableTreeNode node = nodePredicateEntry.getKey();
 						node.add(new DefaultMutableTreeNode(endNodeObject, false));
 					}
 				}
 			}
 		} catch (BadLocationException e) {
 			logger.error("Illegal document location in updateTree()", e);
 		}
 		treeModel.reload();
 		if (path != null) {
 			if (path.getLastPathComponent() instanceof DefaultMutableTreeNode
 					&& ((DefaultMutableTreeNode) path.getLastPathComponent()).isLeaf()) {
 				expandPath(path.getParentPath());
 			}
 			else {
 				expandPath(path);
 			}
 		}
 	}
 
 	private void removeAllChildren() {
 		for (DefaultMutableTreeNode node : nodePredicateMap.keySet()) {
 			node.removeAllChildren();
 		}
 
 		doubtNode.removeAllChildren();
 		jumpNode.removeAllChildren();
 
 		jumpCounter.clear();
 	}
 
 	public JScrollPane getContainer() {
 		JScrollPane sp = new JScrollPane(this);
 		sp.setPreferredSize(new Dimension(200, 500));
 		return sp;
 	}
 
 	/**
 	 * Создает вершину, прикрепляет ее к родительской, и связывает с заданным предикатом.
 	 * Метка вершины соответствует ее предикату.
 	 * 
 	 * @param parent родительская вершина
 	 * @param predicate предикат, с которым будет связана новая вершина
 	 */
 	private void appendEndTreeNode(DefaultMutableTreeNode parent, Predicate predicate) {
 		appendEndTreeNode(parent, predicate.toString(), predicate);
 	}
 
 	/**
 	 * Создает вершину с заданной меткой, прикрепляет ее к родительской, и связывает с заданным предикатом.
 	 *
 	 * @param parent родительская вершина
 	 * @param label метка вершины
 	 * @param predicate предикат, с которым будет связана новая вершина
 	 */
 	private void appendEndTreeNode(DefaultMutableTreeNode parent, String label, Predicate predicate) {
 		EndTreeNode node = new EndTreeNode(label);
 		parent.add(node);
 		nodePredicateMap.put(node, predicate);
 	}
 
 	/**
 	 * Инициализирует структуру вершин дерева и соответствующих им предикатов.
 	 */
 	private void makeTreeStructure() {
 		DefaultMutableTreeNode aspectsNode = new DefaultMutableTreeNode(FUNCTIONS_LABEL);
 		rootNode.add(aspectsNode);
 
 		for (Aspect aspect : ASPECTS) {
 			DefaultMutableTreeNode aspectNode = new DefaultMutableTreeNode(aspect.getAbbreviation());
 			aspectsNode.add(aspectNode);
 
 			DefaultMutableTreeNode aspectSignsNode = new DefaultMutableTreeNode(SIGNS_LABEL);
 			aspectNode.add(aspectSignsNode);
 
 			for (Sign sign : Sign.values()) {
 				appendEndTreeNode(aspectSignsNode, new SignPredicate(aspect, sign));
 			}
 
 			DefaultMutableTreeNode aspectDimensionsNode = new DefaultMutableTreeNode(DIMENSIONS_LABEL);
 			aspectNode.add(aspectDimensionsNode);
 
 			for (int dimension = 1; dimension <= 4; dimension++) {
 				appendEndTreeNode(aspectDimensionsNode, new DimensionPredicate(aspect, dimension));
 			}
 
 			appendEndTreeNode(aspectDimensionsNode, new LowDimensionPredicate(aspect));
 			appendEndTreeNode(aspectDimensionsNode, new HighDimensionPredicate(aspect));
 			appendEndTreeNode(aspectDimensionsNode, new Dimension1Predicate(aspect));
 			appendEndTreeNode(aspectDimensionsNode, new IndividualityPredicate(aspect));
 
 			DefaultMutableTreeNode aspectMVsNode = new DefaultMutableTreeNode(MV_LABEL);
 			aspectNode.add(aspectMVsNode);
 
 			appendEndTreeNode(aspectMVsNode, new MentalPredicate(aspect));
 			appendEndTreeNode(aspectMVsNode, new VitalPredicate(aspect));
 			appendEndTreeNode(aspectMVsNode, new SuperegoPredicate(aspect));
 			appendEndTreeNode(aspectMVsNode, new SuperidPredicate(aspect));
 		}
 
 
 		DefaultMutableTreeNode dimensionsNode = new DefaultMutableTreeNode(DIMENSIONS_LABEL);
 		rootNode.add(dimensionsNode);
 
 		for (int dimension = 1; dimension <= 4; dimension++) {
 			DefaultMutableTreeNode dimensionNode =
 					new DefaultMutableTreeNode(DimensionPredicate.getDimensionName(dimension));
 			dimensionsNode.add(dimensionNode);
 			for (Aspect aspect : ASPECTS) {
 				appendEndTreeNode(dimensionNode, aspect.getAbbreviation(), new DimensionPredicate(aspect, dimension));
 			}
 		}
 
 		DefaultMutableTreeNode dimensionMaloNode = new DefaultMutableTreeNode(LOW_DIMENSION_LABEL);
  		dimensionsNode.add(dimensionMaloNode);
 		for (Aspect aspect : ASPECTS) {
 			appendEndTreeNode(dimensionMaloNode, aspect.getAbbreviation(), new LowDimensionPredicate(aspect));
 		}
 
 		DefaultMutableTreeNode dimensionMnogoNode = new DefaultMutableTreeNode(HIGH_DIMENSION_LABEL);
  		dimensionsNode.add(dimensionMnogoNode);
 		for (Aspect aspect : ASPECTS) {
			appendEndTreeNode(dimensionMnogoNode, aspect.getAbbreviation(), new HighDimensionPredicate(aspect));
 		}
 
 		DefaultMutableTreeNode dimensionOdnoNode = new DefaultMutableTreeNode(DIMENSION1_LABEL);
 		dimensionsNode.add(dimensionOdnoNode);
 		for (Aspect aspect : ASPECTS) {
			appendEndTreeNode(dimensionOdnoNode, aspect.getAbbreviation(), new Dimension1Predicate(aspect));
 		}
 
 		DefaultMutableTreeNode dimensionIndiNode = new DefaultMutableTreeNode(INDIVIDUALITY_LABEL);
 		dimensionsNode.add(dimensionIndiNode);
 		for (Aspect aspect : ASPECTS) {
			appendEndTreeNode(dimensionIndiNode, aspect.getAbbreviation(), new IndividualityPredicate(aspect));
 		}
 
 
 		DefaultMutableTreeNode blockNode = new DefaultMutableTreeNode(BLOCKS_LABEL);
 		rootNode.add(blockNode);
 
 		for (Aspect aspect1 : ASPECTS) {
 			for (Aspect aspect2 : ASPECTS) {
 				if (aspect1.isBlockWith(aspect2)) {
 					appendEndTreeNode(blockNode, new BlockPredicate(aspect1, aspect2));
 				}
 			}
 		}
 
 		DefaultMutableTreeNode signsNode = new DefaultMutableTreeNode(SIGNS_LABEL);
 		rootNode.add(signsNode);
 		for (Sign sign : Sign.values()) {
 			DefaultMutableTreeNode signNode = new DefaultMutableTreeNode(String.format(" %s ", sign));
 			signsNode.add(signNode);
 			for (Aspect aspect : ASPECTS) {
 				appendEndTreeNode(signNode, aspect.getAbbreviation(), new SignPredicate(aspect, sign));
 			}
 		}
 
 
 		rootNode.add(doubtNode);
 		rootNode.add(jumpNode);
 	}
 
 	public String getReport() {
 		if (documentHolder.getModel().getADataMap().isEmpty()) {
 			return "<br/><h2> В документе отсутствует анализ </h2><br/>";
 		}
 
 		StringBuilder reportBuilder = new StringBuilder("<br/>" +
 				"<h2> Выявленные параметры функций ИМ: </h2>" +
 				"Каждый из отмеченных экспертом фрагментов текста представляет собой анализ аспектного содержания фрагмента и <br/>" +
 				"параметров обработки этой информации. <br/>" +
 				"Приведенная ниже таблица иллюстрирует распределение ответов типируемого по параметрам модели А.<br/><br/>" +
 				"<table title=\"function analysis\" border=2 width=\"80%\">" +
 				HTML_ROW_OPEN +
 				"	<th width=\"20%\">  </th>\n");
 		for (Aspect aspect : ASPECTS) {
 			reportBuilder.append("	<th width=\"10%\"> ").append(aspect.getAbbreviation()).append(" </th>\n");
 		}
 
 		createReportRow(reportBuilder, "Ментал");
 		for (Aspect aspect : ASPECTS) {
 			reportBuilder.append(HTML_CELL_OPEN);
 			reportBuilder.append(predicateCounts.get(new MentalPredicate(aspect)));
 			reportBuilder.append(HTML_CELL_CLOSE);
 		}
 		reportBuilder.append(HTML_ROW_CLOSE);
 
 		createReportRow(reportBuilder, "Витал");
 		for (Aspect aspect : ASPECTS) {
 			reportBuilder.append(HTML_CELL_OPEN);
 			reportBuilder.append(predicateCounts.get(new VitalPredicate(aspect)));
 			reportBuilder.append(HTML_CELL_CLOSE);
 		}
 		reportBuilder.append(HTML_ROW_CLOSE);
 
 		for (Sign sign : Sign.values()) {
 			createReportRow(reportBuilder, String.format("Знак \"%s\"", sign.toString()));
 			for (Aspect aspect : ASPECTS) {
 				reportBuilder.append(HTML_CELL_OPEN);
 				reportBuilder.append(predicateCounts.get(new SignPredicate(aspect, sign)));
 				reportBuilder.append(HTML_CELL_CLOSE);
 			}
 			reportBuilder.append(HTML_ROW_CLOSE);
 		}
 
 		for (int dimension = 1; dimension <= 4; dimension++) {
 			createReportRow(reportBuilder, DimensionPredicate.getDimensionName(dimension));
 			for (Aspect aspect : ASPECTS) {
 				reportBuilder.append(HTML_CELL_OPEN);
 				reportBuilder.append(predicateCounts.get(new DimensionPredicate(aspect, dimension)));
 				reportBuilder.append(HTML_CELL_CLOSE);
 			}
 			reportBuilder.append(HTML_ROW_CLOSE);
 		}
 
 		createReportRow(reportBuilder, "Одномерность");
 		for (Aspect aspect : ASPECTS) {
 			reportBuilder.append(HTML_CELL_OPEN);
 			reportBuilder.append(predicateCounts.get(new Dimension1Predicate(aspect)));
 			reportBuilder.append(HTML_CELL_CLOSE);
 		}
 		reportBuilder.append(HTML_ROW_CLOSE);
 
 		createReportRow(reportBuilder, "Маломерность");
 		for (Aspect aspect : ASPECTS) {
 			reportBuilder.append(HTML_CELL_OPEN);
 			reportBuilder.append(predicateCounts.get(new LowDimensionPredicate(aspect)));
 			reportBuilder.append(HTML_CELL_CLOSE);
 		}
 		reportBuilder.append(HTML_ROW_CLOSE);
 
 		createReportRow(reportBuilder, "Многомерность");
 		for (Aspect aspect : ASPECTS) {
 			reportBuilder.append(HTML_CELL_OPEN);
 			reportBuilder.append(predicateCounts.get(new HighDimensionPredicate(aspect)));
 			reportBuilder.append(HTML_CELL_CLOSE);
 		}
 		reportBuilder.append(HTML_ROW_CLOSE);
 
 		createReportRow(reportBuilder, "Индивидуальность");
 		for (Aspect aspect : ASPECTS) {
 			reportBuilder.append(HTML_CELL_OPEN);
 			reportBuilder.append(predicateCounts.get(new IndividualityPredicate(aspect)));
 			reportBuilder.append(HTML_CELL_CLOSE);
 		}
 		reportBuilder.append(HTML_ROW_CLOSE);
 		reportBuilder.append(HTML_TABLE_CLOSE);
 
 		//Переводы
 		if (!jumpCounter.isEmpty()) {
 			reportBuilder.append("<br/>" +
 					"<h2> Переводы управления </h2>" +
 					"Это наблюдаемый перевод ответа из одного аспекта в другой. <br/>" +
 					"Перевод осуществляется: <br/>" +
 					"1) из  менее мерной функции в более мерную функцию (внутри блока или кольца); <br/>" +
 					"2) из витала в ментал.	<br/><br/>" +
 					"<table title=\"jumps\" border=2 width=\"80%\">" +
 					HTML_ROW_OPEN +
 					"	<th width=\"20%\">Переводы <br/>из функции \u25ba<br/>в функцию <br/> \u25bc </th>\n");
 			for (Aspect aspect : ASPECTS) {
 				reportBuilder.append(String.format("	<th width=\"10%%\"> %s </th>\n", aspect.getAbbreviation()));
 			}
 			reportBuilder.append(HTML_ROW_CLOSE);
 
 			for (Aspect firstAspect : ASPECTS) {
 				reportBuilder.append(String.format("<tr>\n	<td style=\"font-weight:bold\"> %s </td>\n",
 						firstAspect.getAbbreviation()));
 				for (Aspect secondAspect : ASPECTS) {
 					reportBuilder.append(HTML_CELL_OPEN);
 					if (firstAspect == secondAspect) {
 						reportBuilder.append('X');
 					} else {
 						reportBuilder.append(jumpCounter.getJumpCount(firstAspect, secondAspect));
 					}
 					reportBuilder.append(HTML_CELL_CLOSE);
 				}
 				reportBuilder.append(HTML_ROW_CLOSE);
 			}
 			reportBuilder.append(HTML_TABLE_CLOSE);
 		}
 		return reportBuilder.toString();
 	}
 	
 	private static void createReportRow(StringBuilder reportBuilder, String header) {
 		reportBuilder.append(HTML_ROW_OPEN);
 		reportBuilder.append(HTML_CELL_OPEN_STRONG);
 		reportBuilder.append(header);
 		reportBuilder.append(HTML_CELL_CLOSE);
 	}
 }
