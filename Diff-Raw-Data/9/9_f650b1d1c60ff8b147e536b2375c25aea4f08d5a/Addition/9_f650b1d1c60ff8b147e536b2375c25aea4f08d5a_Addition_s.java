 package com.ncgeek.manticore.character.stats;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import com.ncgeek.manticore.character.RuleEventArgs;
 import com.ncgeek.manticore.character.RuleEventType;
 import com.ncgeek.manticore.character.inventory.EquipmentManagerEventArgs;
 import com.ncgeek.manticore.character.inventory.EquipmentManagerEventType;
 import com.ncgeek.manticore.character.inventory.EquipmentSlot;
 import com.ncgeek.manticore.character.stats.Stat;
 import com.ncgeek.manticore.items.Armor;
 import com.ncgeek.manticore.items.ArmorTypes;
 import com.ncgeek.manticore.items.EquippableItem;
 import com.ncgeek.manticore.items.Weapon;
 import com.ncgeek.manticore.items.WeaponGroups;
 import com.ncgeek.manticore.rules.Rule;
 
 public class Addition implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	private int _value;
 	private Integer _level;
 	private String _statlink;
 	private Stat _stat;
 	
 	private boolean _abilityMod;
 	private String _type;
 	
 	private String _wearing;
 	private boolean _notWearing;
 	private boolean _foundItem;
 	
 	private String _requires;
 	private boolean _shouldApply;
 	private boolean _foundRule;
 	
 	private String _conditional;
 	
 	private Map<String, String> _additionalAttributes;
 	
 	public Addition() {
 		this(null);
 	}
 	
 	public Addition(Map<String,String> additionalAttributes) {
 		_additionalAttributes = additionalAttributes;
 		
 		_value = 0;
 		_level = null;
 		_statlink = null;
 		_stat = null;
 		
 		_abilityMod = false;
 		_type = _wearing = null;
 		_notWearing = false;
 		_foundItem = false;
 		
 		_requires = null;
 		_shouldApply = true;
 		
 		_conditional = null;
 	}
 	
 	public boolean shouldApply() {
 		return _shouldApply;
 	}
 	
 	public boolean ruleFound() {
 		return _foundRule;
 	}
 	
 	public boolean itemFound() {
 		return _foundItem;
 	}
 	
 	public int getValue() {
 		if(_stat == null && _statlink == null) {
 			if(_shouldApply)
 				return _value;
 			else
 				return 0;
 		}
 		
 		if(_stat == null && _statlink != null)
 			throw new UnlinkedStatException();
 		
 		if(_abilityMod)
 			return _stat.getModifier();
 		else
 			return _stat.getCalculatedValue();
 	}
 	public void setValue(int value) {
 		_value = value;
 	}
 	
 	public Integer getLevel() {
 		return _level;
 	}
 	
 	public void setLevel(Integer level) {
 		_level = level;
 	}
 	
 	public String getStatLink() {
 		return _statlink;
 	}
 	
 	public void setStatLink(String name) {
 		_statlink = name;
 	}
 	
 	public void setStatLink(Stat link) {
 		if(link == null)
 			throw new IllegalArgumentException("null link");
 		
 		if(_statlink == null)
 			throw new UnsupportedOperationException("statlink name is not set");
 		
 		if(!link.equals(_statlink))
 			return;
 		
 		_stat = link;
 		_statlink = _statlink + " [linked]";
 	}
 	
 	public boolean isAbilityModified() {
 		return _abilityMod;
 	}
 	
 	public void setAbilityModified(boolean abilitymod) {
 		_abilityMod = abilitymod;
 	}
 	
 	public String getType() {
 		return _type;
 	}
 	
 	public void setType(String type) {
 		_type = type;
 	}
 	
 	public String getConditional() { return _conditional; }
 	public void setConditional(String condition) {
 		_conditional = condition;
 		_shouldApply = false;
 	}
 	
 	public String getWearing() {
 		if(_notWearing)
 			return null;
 		else
 			return _wearing;
 	}
 	
 	public void setWearing(String wearing) {
 		if(wearing == null || wearing.trim().length() == 0)
 			return;
 		
 		_wearing = wearing;
 		_notWearing = false;
 		_shouldApply = false;
 		_foundItem = false;
 	}
 	
 	public boolean isNotWearing() {
 		return _notWearing;
 	}
 	
 	public void setNotWearing(String notWearing) {
 		if(notWearing == null || notWearing.trim().length() == 0)
 			return;
 		
 		_wearing = notWearing;
 		_notWearing = true;
 		_foundItem = false;
 		_shouldApply = true;
 	}
 	
 	public String getNotWearing() {
 		if(_notWearing)
 			return _wearing;
 		else
 			return null;
 	}
 	
 	public String getRequires() {
 		return _requires;
 	}
 	
 	public void setRequires(String requires) {
 		if(requires == null || requires.trim().length() == 0)
 			return;
 		
 		_requires = requires;
 		_shouldApply = requires.startsWith("!");
 		_foundRule = false;
 	}
 
 	public Set<String> getAdditionalAttributeNames() {
 		if(_additionalAttributes == null)
 			return null;
 		
 		return Collections.unmodifiableSet(_additionalAttributes.keySet());
 	}
 	
 	public String getAdditionalAttribute(String name) {
 		if(_additionalAttributes == null)
 			return null;
 		
 		if(!_additionalAttributes.containsKey(name))
 			return null;
 		
 		return _additionalAttributes.get(name);
 	}
 	
 	public void setAdditionalAttribute(String name, String value) {
 		if(_additionalAttributes == null)
 			_additionalAttributes = new HashMap<String,String>();
 		
 		_additionalAttributes.put(name, value);
 	}
 	
 	public Map<String,String> getAdditionalAttributes() {
 		if(_additionalAttributes == null)
 			return null;
 		
 		return Collections.unmodifiableMap(_additionalAttributes);
 	}
 	
 	public boolean process(EquipmentManagerEventArgs event) {
 		if(_wearing == null
 			|| event.getType() == EquipmentManagerEventType.ItemAdded
 			|| event.getType() == EquipmentManagerEventType.ItemRemoved)
 			return false;
 		
 		EquippableItem item = (EquippableItem)event.getItem();
 		
 		String[] parts = _wearing.trim().toLowerCase().split(":");
 		String category = parts[0];
 		String specific = null;
 		if(parts.length > 1 && parts[1].length() > 0)
 			specific = parts[1];
 		
 		boolean isRemove = event.getType() == EquipmentManagerEventType.ItemRemoved;
 		
 		if((isRemove && !_foundItem) 
 				|| (!isRemove && _foundItem))
 			return false;
 		
 		boolean oldShouldApply = _shouldApply;
 		
 		// what are we doing?
 		if(category.equals("armor")) {
 			equipArmor(item, specific, isRemove);
 		} else if(category.equals("slot")) {
 			equipSlot(item, specific, isRemove, event.getSlot());
 		} else if(category.equals("weapon")) {
 			equipWeapon(item, specific, isRemove);
 		} else if(category.equals("defensive")) {
 			equipDefensive(item, specific, isRemove);
 		} else if(category.equals("implement")) {
 			equipImplement(item, specific, isRemove);
 		} else {
 			throw new RuntimeException("Unknown category type: " + category);
 		}
 		
 		return _shouldApply != oldShouldApply;
 	}
 	
 	private void equipImplement(EquippableItem item, String specific, boolean isRemove) {
 		if(specific != null
 				&& !item.getName().toLowerCase().contains(specific))
 			return;
 		
 		if(!isRemove) {
 			_foundItem = true;
 			_shouldApply = !_notWearing;
 		} else {
 			_foundItem = false;
 			_shouldApply = _notWearing;
 		}
 	}
 
 	private void equipDefensive(EquippableItem item, String specific, boolean isRemove) {
 		// TODO Auto-generated method stub
 		//throw new UnsupportedOperationException("nyi item=" + item.getName() + " [" + item.getID() + "]");
 	}
 
 	private void equipWeapon(EquippableItem item, String specific, boolean isRemove) {
 		if(!(item instanceof Weapon))
 			return;
 		
 		if(specific != null) {
 			Weapon weapon = (Weapon)item;
 			WeaponGroups required = WeaponGroups.forName(specific);
 			
 			if(!weapon.getGroups().contains(required))
 				return;
 		}
 		
 		if(!isRemove) {
 			_foundItem = true;
 			_shouldApply = !_notWearing;
 		} else {
 			_foundItem = false;
 			_shouldApply = _notWearing;
 		}
 	}
 
 	private void equipSlot(EquippableItem item, String specific, boolean isRemove, Set<EquipmentSlot> equipSlot) {
 		
 		EquipmentSlot required = EquipmentSlot.forName(specific);
 		
 		if(!equipSlot.contains(required)) {
 			// ring2?
 			if(required != EquipmentSlot.Ring1 
 					|| !equipSlot.contains(EquipmentSlot.Ring2))
 				return;
 		}
 		
 		if(!isRemove) {
 			_foundItem = true;
 			_shouldApply = !_notWearing;
 		} else {
 			_foundItem = false;
 			_shouldApply = _notWearing;
 		}
 	}
 
 	private void equipArmor(EquippableItem item, String specific, boolean isRemove) {
 		if(!(item instanceof Armor))
 			return;
 		
 		if(specific != null) {
 			Armor armor = (Armor)item;
 			ArmorTypes required = ArmorTypes.forName(specific);
 			
 			if(armor.getArmorType() != required)
 				return;
 		}
 		
 		if(!isRemove) {
 			_foundItem = true;
 			_shouldApply = !_notWearing;
 		} else {
 			_foundItem = false;
 			_shouldApply = _notWearing;
 		}
 	}
 	
 	public boolean process(RuleEventArgs rule) {
 		if(_requires == null)
 			return false;
 		
 		boolean oldShouldApply = _shouldApply;
 		
 		if(rule.getType() == RuleEventType.RuleAdded)
 			addRule(rule.getRule());
 		else
 			removeRule(rule.getRule());
 		
 		return _shouldApply != oldShouldApply;
 	}
 	
 	private void addRule(Rule rule) {
 		if(_foundRule)
 			return;
 		
 		boolean inverse = _requires != null && _requires.startsWith("!");
 		String name = _requires;
 		
 		if(inverse)
 			name = name.substring(1);
 		
 		if(name != null && !name.equalsIgnoreCase(rule.getName()))
 			return;
 		
 		_foundRule = true;
 		_shouldApply = !inverse;
 	}
 	
 	private void removeRule(Rule rule) {
 		if(!_foundRule)
 			return;
 		
 		boolean inverse = _requires != null && _requires.startsWith("!");
 		String name = _requires;
 		if(inverse)
 			name = name.substring(1);
 		
 		if(name != null && !name.equalsIgnoreCase(rule.getName()))
 			return;
 		
 		_foundRule = false;
 		_shouldApply = inverse;
 	}
 	
 	@Override
 	public String toString() {
 		return String.format("Apply=%s Value=%d Level=%d Statlink=%s abilmod=%s type=%s notwearing=%s wearing=%s requires=%s",
 				_shouldApply, getValue(), getLevel(), _statlink, _abilityMod, _type, _notWearing, _wearing, _requires);		
 	}
 }
