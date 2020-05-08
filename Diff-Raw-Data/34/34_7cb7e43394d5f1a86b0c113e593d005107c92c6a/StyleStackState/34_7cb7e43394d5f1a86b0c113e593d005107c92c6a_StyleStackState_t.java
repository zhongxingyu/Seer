 package jbookreader.style.impl;
 
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
 
 import jbookreader.style.ERuleValueType;
 import jbookreader.style.IDimensionConvertor;
 import jbookreader.style.IStyleConfig;
 import jbookreader.style.IStyleRule;
 import jbookreader.style.StyleAttribute;
 
 class StyleStackState {
 	private static Map<StyleAttribute, IStyleValueComputer> specials;
 	private static Map<Class<?>, IStyleValueComputer> generics; 
 	static {
 		specials = new EnumMap<StyleAttribute, IStyleValueComputer>(StyleAttribute.class);
 		specials.put(StyleAttribute.FONT_WEIGHT, new FontWeightValueComputer());
 		specials.put(StyleAttribute.FONT_SIZE, new FontSizeValueComputer());
 		
 		generics = new HashMap<Class<?>, IStyleValueComputer>();
 		generics.put(Integer.class, new IntegerValueComputer());
 		generics.put(String[].class, new StringArrayValueComputer());
 		generics.put(Enum.class, new EnumValueComputer());
 	}
 	
 	private static final IStyleRule INHERIT_RULE = new IStyleRule() {
 
 		public StyleAttribute getAttribute() {
 			throw new InternalError("Attribute requested for special inheritance rule");
 		}
 
 		public <T> T getValue(Class<T> klass) throws ClassCastException {
 			return null;
 		}
 
 		public ERuleValueType getValueType() {
 			return ERuleValueType.INHERIT;
 		}
 
 		public long getWeight() {
 			return 0;
 		}
 		
 	};
 	
 	private final Map<StyleAttribute, Object> attributes;
 	private final IDimensionConvertor convertor = new DimensionConvertor();
 
 	StyleStackState(final IStyleConfig config) {
 		attributes = new EnumMap<StyleAttribute, Object>(StyleAttribute.class);
 		
 		for (StyleAttribute attribute : StyleAttribute.values()) {
 			IStyleValueComputer computer = findComputer(attribute);
 			attributes.put(attribute, computer.compute(attribute, attribute, null, config));
 		}
 	}
 	
 	StyleStackState(StyleStackState oldState, Map<StyleAttribute, IStyleRule> rules, IStyleConfig config) {
 		attributes = new EnumMap<StyleAttribute, Object>(StyleAttribute.class);
 		for (StyleAttribute attribute: StyleAttribute.values()) {
 			IStyleValueComputer computer = findComputer(attribute);
 
			IStyleRule rule;
 
			if (rules.containsKey(attribute)) {
				rule = rules.get(attribute);
			} else {
 				if (attribute.isInherit()) {
 					rule = INHERIT_RULE;
 				} else {
 					rule = attribute;
 				}
 			}
 
 			attributes.put(attribute, computer.compute(attribute, rule, oldState, config));
 		}
 	}
 
 	private IStyleValueComputer findComputer(StyleAttribute attrib) throws InternalError {
		IStyleValueComputer computer = null;
		if (specials.containsKey(attrib)) {
			computer = specials.get(attrib);
		} else {
 			for (Class<?> klass = attrib.getAttributeValueClass();
 				computer == null && klass != null;
 				klass = klass.getSuperclass()) {
 				
 				computer = generics.get(klass);
 			}
 		}
 		
 		if (computer == null) {
 			throw new InternalError("Can't get value computer for attribute " + attrib);
 		}
 		return computer;
 	}
 	
 	@SuppressWarnings("unchecked")
 	<T extends Object> T getAttributeValue(StyleAttribute attribute) {
 		return (T) attribute.getAttributeValueClass().cast(attributes.get(attribute));
 	}
 	
 	void setAttributeValue(StyleAttribute attribute, Object object) {
 		attributes.put(attribute, attribute.getAttributeValueClass().cast(object));
 	}
 
 	IDimensionConvertor getDimensionConvertor() {
 		return convertor;
 	}
 
 }
