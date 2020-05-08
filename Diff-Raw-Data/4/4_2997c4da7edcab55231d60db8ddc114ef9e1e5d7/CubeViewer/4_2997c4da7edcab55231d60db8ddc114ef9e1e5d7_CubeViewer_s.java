 /**
  * 
  */
 package de.xwic.cube.webui.viewer;
 
 import java.io.PrintWriter;
 import java.util.StringTokenizer;
 
 import de.jwic.base.Control;
 import de.jwic.base.IControlContainer;
 import de.jwic.base.ImageRef;
 import de.jwic.base.RenderContext;
 import de.jwic.base.UserAgentInfo;
 import de.jwic.renderer.self.ISelfRenderingControl;
 import de.jwic.renderer.self.SelfRenderer;
 import de.xwic.cube.IDimensionElement;
 import de.xwic.cube.webui.util.TableCell;
 import de.xwic.cube.webui.util.TableRenderer;
 
 /**
  * @author Florian Lippisch
  */
 public class CubeViewer extends Control implements ISelfRenderingControl {
 
 	private final static ImageRef IMG_SPACER = new ImageRef("/jwic/gfx/clear.gif", 14, 8);
 	
 	public enum Align { BEGIN, END };
 	
 	private static final long serialVersionUID = 2L;
 	private CubeViewerModel model;
 	
 	private Align columnTotalAlign = Align.BEGIN;
 	private Align rowTotalAlign = Align.END;
 	private int leftNavMinWidth = 250;
 	private int columnWidth = 0; // default
 	private boolean emptyCellsClickable = false;
 	
 	/**
 	 * @param container
 	 * @param name
 	 */
 	public CubeViewer(IControlContainer container, String name) {
 		super(container, name);
 
 		setRendererId(SelfRenderer.RENDERER_ID);
 		
 		model = new CubeViewerModel(getSessionContext().getLocale());
 		model.addCubeViewerModelListener(new CubeViewerModelAdapter() {
 			public void filterUpdated(CubeViewerModelEvent event) {
 				onFilterUpdated(event);				
 			}
 			public void cubeUpdated(CubeViewerModelEvent event) {
 				onCubeUpdated(event);
 			}
 		});
 	}
 
 	/**
 	 * @param event
 	 */
 	protected void onCubeUpdated(CubeViewerModelEvent event) {
 		requireRedraw();
 	}
 	
 	/**
 	 * @param event
 	 */
 	protected void onFilterUpdated(CubeViewerModelEvent event) {
 		requireRedraw();
 	}
 
 	/**
 	 * @return the model
 	 */
 	public CubeViewerModel getModel() {
 		return model;
 	}
 
 	/**
 	 * Handle click action.
 	 * @param parameter
 	 */
 	public void actionClick(String parameter) {
 		
 		StringTokenizer stk = new StringTokenizer(parameter, ";");
 		int len = stk.countTokens();
 		String dimKey;
 		if (len > 0) {
 			dimKey = stk.nextToken();
 			String[] args = new String[len - 1];
 			int i = 0;
 			while (stk.hasMoreTokens()) {
 				args[i++] = stk.nextToken();
 			}
 			model.notifyCellSelection(dimKey, args);
 		} else {
 			log.warn("Invalid parameter for cell selection - parameter must not be empty.");
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.renderer.self.ISelfRenderingControl#render(de.jwic.base.RenderContext)
 	 */
 	public void render(RenderContext renderContext) {
 		
 		if (model.getCube() == null) {
 			// render empty block
 			renderWarning(renderContext, "No Cube Defined");
 			return;
 		}
 		if (model.getMeasure() == null) {
 			// render empty block
 			renderWarning(renderContext, "No Measure Defined");
 			return;
 		}
 
 		PrintWriter out = renderContext.getWriter();
 
 		TableRenderer tbl = renderTable();
 		tbl.render(out);
 		
 		//out.println("<span class=\"x-cube-info\">cube: " + model.getCube().getKey() + ", measure: " + model.getMeasure().getKey() + "</span>");
 		
 		
 	}
 
 	/**
 	 * @return
 	 */
 	public TableRenderer renderTable() {
 		TableRenderer tbl = new TableRenderer(this);
 		tbl.setCssClass("xcube-tbl");
 		
 		UserAgentInfo userAgent = getSessionContext().getUserAgent();
 		if (userAgent.isIE() && userAgent.getVersion().equals("6.0")) {
 			tbl.setEnableJSHoverMode(true);
 		}
 
 		// render header...
 		
 		if (model.getColumnProvider().size() == 0 && model.getRowProvider().size() == 0) {
 			tbl.initSize(2, 1);
 			tbl.getCell(0, 0).setContent("Total");
 			tbl.getCell(1, 0).setContent(formatValue(model.getCube().getCellValue(model.getTotalKey(), model.getMeasure())));
 			// early EXIT
 			return tbl;
 		} 
 
 		// evaluate horizontal header size (columns)
 		int colHeight = 0;
 		int colCount = 0;
 		for (INavigationProvider navProvider : model.getColumnProvider()) {
 			NavigationSize size = navProvider.getNavigationSize();
 			int depth = size.depth + navProvider.getIndention();
 			if (depth > colHeight) {
 				colHeight = depth;
 			}
 			colCount += size.cells;
 		}
 		
 		// evaluate vertical header size (rows)
 		int rowDepth = 0;
 		int rowCount = 0;
 		for (INavigationProvider navProvider : model.getRowProvider()) {
 			NavigationSize size = navProvider.getNavigationSize();
 			int depth = size.depth + navProvider.getIndention();
 			if (depth > rowDepth) {
 				rowDepth = depth;
 			}
 			rowCount += size.cells;
 		}
 		
 		tbl.initSize(colHeight + rowCount, rowDepth + colCount);
 		
 		if (columnWidth > 0) {
 			for (int col = rowDepth; col < (rowDepth + colCount); col ++) {
 				tbl.setColumnWidth(col, columnWidth);
 			}
 		}
 		
 		// render Header
 		int startCol = rowDepth;
 		for (INavigationProvider navProvider : model.getColumnProvider()) {
 			NavigationSize size = navProvider.getNavigationSize();
 			size.cells += startCol;
 			renderNavigation(tbl, navProvider.getIndention(), startCol, navProvider, size, true);
 			startCol += (size.cells - startCol);
 		}		
 		
 		for (int r = 0; r < colHeight; r++) {
 			tbl.getCell(r, 0)
 				.setColSpan(rowDepth)
 				.setContent(IMG_SPACER.toImgTag(leftNavMinWidth, 2));
 		}
 		
 		// render rows
 		int startRow = colHeight;
 		for (INavigationProvider navProvider : model.getRowProvider()) {
 			NavigationSize size = navProvider.getNavigationSize();
 			size.depth = rowDepth;
 			if (size.cells > 0) {
 				renderNavigation(tbl, startRow, navProvider.getIndention(), navProvider, size, false);
 			}
 			startRow += size.cells;
 		}		
 		
 		// render content
 		for (int row = 0; row < rowCount; row++) {
 			for (int col = 0; col < colCount; col++) {
 				int rowIdx = row + colHeight;
 				int colIdx = col + rowDepth;
 				ContentInfo ciRow = (ContentInfo) tbl.getRowData(rowIdx);
 				ContentInfo ciCol = (ContentInfo) tbl.getColumnData(colIdx);
 				
 				TableCell cell = tbl.getCell(rowIdx, colIdx);
 				boolean empty = true;
 				if (ciCol != null && ciRow != null) {
 					ICubeDataProvider dataProvider = ciRow.getCubeDataProvider().getPriority() > ciCol.getCubeDataProvider().getPriority() ?
 							ciRow.getCubeDataProvider() :
 							ciCol.getCubeDataProvider();
 					
 					String content = dataProvider.getCellData(model, ciRow, ciCol);
 					empty = content == null || content.length() == 0;
 					cell.setContent(content);
 					cell.setCssClass("xcube-data xcube-data-vlvl-" + ciRow.getLevel() + " xcube-data-hlvl-" + ciCol.getLevel());
 				} else {
 					cell.setContent("NoCI");
 				}
 				if (ciRow.isClickable() && ciCol.isClickable() && (!empty || emptyCellsClickable)) {
 					cell.setAction("click");
 					cell.setActionParam(buildActionParameter(ciRow, ciCol));
 				}
 				
 			}
 		}
 		return tbl;
 	}
 
 	/**
 	 * @param ciRow
 	 * @param ciCol
 	 * @return
 	 */
 	private String buildActionParameter(ContentInfo ciRow, ContentInfo ciCol) {
 
 		// build dimension data
 		StringBuilder sb = new StringBuilder();
 		for (IDimensionElement de : ciRow.getElements()) {
 			sb.append(de.getID());
 		}
 		for (IDimensionElement de : ciCol.getElements()) {
 			sb.append(de.getID());
 		}
 
 		if (ciRow.getExtraClickInfo() != null) {
 			sb.append(";")
 			  .append(ciRow.getExtraClickInfo());
 		}
 		if (ciCol.getExtraClickInfo() != null) {
 			sb.append(";")
 			  .append(ciCol.getExtraClickInfo());
 		}
 		
 		return sb.toString();
 	}
 
 	/**
 	 * @param tbl 
 	 * @param i
 	 * @param startCol
 	 * @param parentElement
 	 * @param size
 	 * @param b
 	 */
 	private NavigationSize renderNavigation(TableRenderer tbl, int startRow, int startCol, INavigationElementProvider parentElement, NavigationSize totalSize, boolean horizontal) {
 		
 		NavigationSize size = new NavigationSize();
 		int row = startRow;
 		int col = startCol;
 		
 		int totalItems = 0;
 		int level = horizontal ? startRow : startCol;
 		
 		for (INavigationElement elm : parentElement.getNavigationElements()) {
 			boolean expanded = model.isExpanded(elm.getElementId());
 
 			int titleRow = row;
 			int titleCol = col;
 			int items = 1;
 			ContentInfo contentInfo = elm.getContentInfo();
 			contentInfo.setLevel(level);
 			if (expanded) {
 				Align align = horizontal ? columnTotalAlign : rowTotalAlign;
 				int startIndent = !elm.hideTotal() && (align == Align.BEGIN) ? 1 : 0;
 				NavigationSize subSize;
 				if (horizontal) {
 					subSize = renderNavigation(tbl, row + 1, col + startIndent, elm, totalSize, horizontal);
 				} else {
 					subSize = renderNavigation(tbl, row + startIndent, col + 1, elm, totalSize, horizontal);
 				}
 				if (size.depth < subSize.depth) {
 					size.depth = subSize.depth;
 				}
 				items = subSize.cells;
 				if (!elm.hideTotal()) {
 					items++;
 					// render "sub-total" text
 					int stRow = row + (horizontal ? subSize.depth : (align == Align.BEGIN ? 0 : subSize.cells));
 					int stCol = col + (horizontal ? (align == Align.BEGIN ? 0 : subSize.cells) : subSize.depth);
 					if (horizontal) {
 						tbl.setColumnData(stCol, contentInfo);
 						for (int r = row + 1; r <= stRow; r++) {
 							tbl.getCell(r, stCol).setCssClass("xcube-hl-" + level + " xcube-hor-end");
 							
 						}
 						titleCol = stCol;
 					} else {
 						tbl.setRowData(stRow, contentInfo);
 						for (int c = col + 1; c <= stCol; c++) {
 							tbl.getCell(stRow, c).setCssClass("xcube-hl-" + level);
 						}
 						titleRow = stRow;
 						//tbl.getCell(stRow, stCol).setContent(elm.getTitle() + " Total");
 					}
 				}
 			} else {
 				if (horizontal) {
 					tbl.setColumnData(col, contentInfo);
 					for (int r = row + 1; r <= totalSize.depth; r++) {
 						tbl.getCell(r, col).setCssClass("xcube-hl-" + level + " xcube-hor-end");
 						
 					}
 				} else {
 					tbl.setRowData(row, contentInfo);
 				}
 			}
 			
 			// render navigation element cell
 			totalItems += items;
 			if (horizontal) {
 				for (int c = 0; c < items; c++) {
 					tbl.getCell(row, col + c).setCssClass("xcube-hl-" + level + " xcube-hor-span");
 				}
 				col += items;
 			} else {
 				for (int r = 0; r < items; r++) {
 					tbl.getCell(row + r, col).setCssClass("xcube-hl-" + level)
 					.setContent(IMG_SPACER.toImgTag());
 
 				}
 				row += items;
 			}
 
 			TableCell cell = tbl.getCell(titleRow, titleCol);
 			StringBuilder sb = new StringBuilder();
 			if (elm.isExpandable()) {
 				String action = expanded ? "collapse" : "expand";
 				sb.append("<a class=\"xcube-action-" + action + "\"");
 				sb.append(" href=\"" + createActionURL(action, elm.getElementId()) + "\">");
 				sb.append(elm.getTitle());
 				sb.append("</A>");
 			} else {
 				sb.append("<span class=\"xcube-title\">").append(elm.getTitle()).append("</span>");
 			}
 			cell.setContent(sb.toString());
 			cell.setCssClass("xcube-hl-" + level + " xcube-" +
 					(horizontal ? "hor" : (rowTotalAlign == Align.BEGIN ? "ver" : "verBt")) + 
 					"-end");
 			
 			if (horizontal) {
 				if (columnTotalAlign == Align.BEGIN) {
 					cell.setColSpan(items);
 				}
 			} else {
 				cell.setColSpan(totalSize.depth - titleCol);
 			}
 
 		}
 		size.cells = totalItems;
 		size.depth++; // add "myself";
 		return size;
 	}
 
 	/**
 	 * Handles expand action.
 	 * @param elementId
 	 */
 	public void actionExpand(String elementId) {
 		model.expand(elementId);
 		requireRedraw(); // TODO to be replaced by listeners mechanism...
 	}
 	
 	/**
 	 * Handles Collapse action.
 	 * @param elementId
 	 */
 	public void actionCollapse(String elementId) {
 		model.collapse(elementId);
 		requireRedraw(); // TODO to be replaced by listener mechanism.
 	}
 	
 	/**
 	 * @param value
 	 * @return
 	 */
 	private String formatValue(Double value) {
 		if (value != null) {
 			return model.getValueFormat().format(value);
 		}
 		return "<i>na</i>";
 	}
 
 	/**
 	 * @param renderContext
 	 * @param string
 	 */
 	private void renderWarning(RenderContext renderContext, String msg) {
 		renderContext.getWriter().println(msg);
 	}
 
 	/**
 	 * @return the columnTotalAlign
 	 */
 	public Align getColumnTotalAlign() {
 		return columnTotalAlign;
 	}
 
 	/**
 	 * @param columnTotalAlign the columnTotalAlign to set
 	 */
 	public void setColumnTotalAlign(Align columnTotalAlign) {
 		this.columnTotalAlign = columnTotalAlign;
 	}
 
 	/**
 	 * @return the rowTotalAlign
 	 */
 	public Align getRowTotalAlign() {
 		return rowTotalAlign;
 	}
 
 	/**
 	 * @param rowTotalAlign the rowTotalAlign to set
 	 */
 	public void setRowTotalAlign(Align rowTotalAlign) {
 		this.rowTotalAlign = rowTotalAlign;
 	}
 
 	/**
 	 * @return the leftNavMinWidth
 	 */
 	public int getLeftNavMinWidth() {
 		return leftNavMinWidth;
 	}
 
 	/**
 	 * @param leftNavMinWidth the leftNavMinWidth to set
 	 */
 	public void setLeftNavMinWidth(int leftNavMinWidth) {
 		this.leftNavMinWidth = leftNavMinWidth;
 	}
 
 	/**
 	 * @return the columnWidth
 	 */
 	public int getColumnWidth() {
 		return columnWidth;
 	}
 
 	/**
 	 * @param columnWidth the columnWidth to set
 	 */
 	public void setColumnWidth(int columnWidth) {
 		this.columnWidth = columnWidth;
 	}
 
 	/**
 	 * @return the emptyCellsClickable
 	 */
 	public boolean isEmptyCellsClickable() {
 		return emptyCellsClickable;
 	}
 
 	/**
 	 * @param emptyCellsClickable the emptyCellsClickable to set
 	 */
 	public void setEmptyCellsClickable(boolean emptyCellsClickable) {
 		this.emptyCellsClickable = emptyCellsClickable;
 	}
 
 }
