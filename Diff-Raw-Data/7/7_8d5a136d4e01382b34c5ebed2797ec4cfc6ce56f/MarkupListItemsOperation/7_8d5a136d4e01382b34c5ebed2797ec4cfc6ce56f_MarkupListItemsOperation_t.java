 package nota.oxygen.common.list;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import nota.oxygen.common.BaseAuthorOperation;
 
 import org.w3c.dom.Element;
import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import ro.sync.ecss.extensions.api.ArgumentDescriptor;
 import ro.sync.ecss.extensions.api.ArgumentsMap;
 import ro.sync.ecss.extensions.api.AuthorConstants;
 import ro.sync.ecss.extensions.api.AuthorDocumentController;
 import ro.sync.ecss.extensions.api.AuthorOperationException;
 import ro.sync.ecss.extensions.api.node.AuthorElement;
 import ro.sync.ecss.extensions.api.node.AuthorNode;
 
 
 /**
  * Marks-up the selected items as list items
  * @author Ole Holst Andersen (oha@nota.nu)
  */
 public class MarkupListItemsOperation extends BaseAuthorOperation {
 
 	@Override
 	protected void doOperation() throws AuthorOperationException {
 		try {
 			AuthorDocumentController docCtrl = getAuthorAccess().getDocumentController();
 			int startSel = getSelectionStart();
 			int endSel = getSelectionEnd();
 			AuthorElement firstAthElem = (AuthorElement)docCtrl.getNodeAtOffset(startSel+1);
 			AuthorElement lastAthElem = (AuthorElement)docCtrl.getNodeAtOffset(endSel-1);
 			if (firstAthElem.getParent()!=lastAthElem.getParent()) {
 				String msg = "Selected nodes are not siblings:\n";
 				msg += "first element: "+firstAthElem.getLocalName()+"[@id='"+firstAthElem.getAttribute("id")+"']\n";
 				msg += "last element: "+lastAthElem.getLocalName()+"[@id='"+lastAthElem.getAttribute("id")+"']\n";
 				showMessage(msg);
 				return;
 			}
 			startSel = firstAthElem.getStartOffset();
 			endSel = lastAthElem.getEndOffset();
 			docCtrl.surroundInFragment(listFragment, startSel, endSel);
 			startSel = firstAthElem.getStartOffset();
 			endSel = lastAthElem.getEndOffset();
 			AuthorElement list = (AuthorElement)docCtrl.getNodeAtOffset(startSel);
 			int childIndex = 0;
 			while (childIndex<list.getContentNodes().size()) {
 				AuthorNode nod = list.getContentNodes().get(childIndex);
 				startSel = list.getStartOffset();
 				endSel = list.getEndOffset();
 				if (!(nod instanceof AuthorElement)) {
 					throw new AuthorOperationException("Cannot apply markup to mixed content");
 				}
 				AuthorElement elem = (AuthorElement)nod;
 				if (!elementsToSubstitute.contains(elem.getLocalName())) {
 					String msg =							
 						"One of the elements in the selection is not applicable for markup: "
 						+elem.getLocalName()+"[@id='"+elem.getAttribute("id").getValue()+"']\n"
 						+"Applicable elements are:";
 					for (String e : elementsToSubstitute) msg += "\n"+e;
 					throw new AuthorOperationException(msg);
 				}
 				docCtrl.surroundInFragment(itemFragment, elem.getStartOffset(), elem.getEndOffset());
 				if (!keepSubstitutes) {
 					//remove the original element that was surrounded with the item element,
 					//keeping its children as children of the item element
 					AuthorElement item = (AuthorElement)elem.getParent();
 					Element itemElem = deserializeElement(serialize(item));
 					if (itemElem.getFirstChild() instanceof Element) {
 						Element e = (Element)itemElem.getFirstChild();
						while (e.hasChildNodes()) {
							itemElem.insertBefore(e.removeChild(e.getFirstChild()), e);
 						}
 						itemElem.removeChild(e);
 					}
 					docCtrl.insertXMLFragment(serialize(itemElem), item, AuthorConstants.POSITION_BEFORE);
 					docCtrl.deleteNode(item);
 				}
 				childIndex++;
 			}
 		}
 		catch (AuthorOperationException e) {
 			throw e;
 		}
 		catch (Exception e) {
 			throw new AuthorOperationException(
 					"Unexpected "+e.getClass().getName()+"occured: "+e.getMessage(),
 					e);
 		}
 	}
 
 	@Override
 	protected void parseArguments(ArgumentsMap args)
 			throws IllegalArgumentException {
 		listFragment = (String)args.getArgumentValue(ARG_LIST_FRAGMENT);
 		itemFragment = (String)args.getArgumentValue(ARG_ITEM_FRAGMENT);
 		String elems = (String)args.getArgumentValue(ARG_ELEMENTS_TO_SUBSTITUTE);
 		elementsToSubstitute = new ArrayList<String>();
 		for (String e : elems.split(" ")) elementsToSubstitute.add(e);
 		keepSubstitutes = YES_NO[0].equals(args.getArgumentValue(ARG_KEEP_SUBSTITUTES));
 	}
 	
 	private static String ARG_LIST_FRAGMENT = "list fragment";
 	private static String ARG_ITEM_FRAGMENT = "item fragment";
 	private static String ARG_ELEMENTS_TO_SUBSTITUTE = "elements to substitute";
 	private static String ARG_KEEP_SUBSTITUTES = "keep substitutes";
 	private static String[] YES_NO = new String[] {"yes", "no"};
 
 	private String listFragment;
 	private String itemFragment;
 	private List<String> elementsToSubstitute;
 	private boolean keepSubstitutes;
 	@Override
 	public ArgumentDescriptor[] getArguments() {
 		return new ArgumentDescriptor[] {
 				new ArgumentDescriptor(ARG_LIST_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "list xml fragment"),
 				new ArgumentDescriptor(ARG_ITEM_FRAGMENT, ArgumentDescriptor.TYPE_FRAGMENT, "item xml fragment"),
 				new ArgumentDescriptor(ARG_ELEMENTS_TO_SUBSTITUTE, ArgumentDescriptor.TYPE_STRING, "local names of elements to substitute with item - space separated"),
 				new ArgumentDescriptor(ARG_KEEP_SUBSTITUTES, ArgumentDescriptor.TYPE_CONSTANT_LIST, "local names of elements to substitute with item - space separated", YES_NO, YES_NO[1])
 		};
 	}
 
 	@Override
 	public String getDescription() {
 		return "Markup list items";
 	}
 
 }
