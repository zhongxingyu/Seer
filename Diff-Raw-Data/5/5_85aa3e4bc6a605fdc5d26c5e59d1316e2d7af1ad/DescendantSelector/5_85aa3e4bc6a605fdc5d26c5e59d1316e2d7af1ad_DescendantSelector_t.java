 package html2windows.css.level1;
 
 import html2windows.dom.Element;
 import html2windows.dom.Node;
 
 class DescendantSelector extends SelectorAdapter{
 	public boolean match(Element element){
 		if (prev() == null)
 			return false;

 		Element ancestor = parentElement(element);
		for (; ancestor != null; ancestor = parentElement(ancestor)) {
 			if (prev().match(ancestor))
 				return true;
 		}
 		return false;
 	}
 	
 	private Element parentElement(Element element){
 		Node parent = element.parentNode();
 		if (parent instanceof Element){
 			return (Element)parent;
 		}
 		else {
 			return null;
 		}
 	}
 }
