 package com.github.pepe79.jats.demo;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import com.github.pepe79.jats.demo.model.Component;
 import com.github.pepe79.jats.demo.model.Modifier;
 import com.github.pepe79.jats.demo.model.ModifierEntry;
 import com.github.pepe79.jats.demo.model.Product;
 import com.github.pepe79.jats.repository.Repository;
 
 public class DemoProductRepository implements Repository<Product>
 {
 
 	public Product createComplexProduct()
 	{
 		IdGenerator idGen = new IdGenerator();
 
 		Product p = new Product();
 		p.setId(idGen.nextId());
 		p.setLabel("Demo product");
 
 		Component variant = new Component();
 		p.setVariants(new ArrayList<Component>());
 		p.getVariants().add(variant);
 
 		variant.setId(idGen.nextId());
 		variant.setLabel("Product variant 1");
 
 		List<Modifier> modifiers = new ArrayList<Modifier>();
 		modifiers.add(createModifier(3, "Yellow modifier", 2, 2, idGen));
 		modifiers.add(createModifier(5, "Blue modifier", 0, 0, idGen));
 		variant.setModifiers(modifiers);
 
 		return p;
 	}
 
 	private Modifier createModifier(int num, String baseLabel, int min, int max, IdGenerator idGen)
 	{
 		Modifier m = new Modifier();
 		m.setId(idGen.nextId());
 		m.setMin(min);
 		m.setMax(max);
 
 		List<ModifierEntry> modifierEntries = new ArrayList<ModifierEntry>();
 		for (int i = 0; i < num; i++)
 		{
 			ModifierEntry e = new ModifierEntry();
 			e.setId(idGen.nextId());
 			e.setComponent(createSimpleComponent(baseLabel + " " + (i + 1), idGen));
 			modifierEntries.add(e);
 		}
 		m.setModifierEntries(modifierEntries);
 
 		return m;
 	}
 
 	private Component createSimpleComponent(String label, IdGenerator idGen)
 	{
 		Component component = new Component();
 		component.setId(idGen.nextId());
 		component.setLabel(label);
 		return component;
 	}
 
 	@Override
 	public Collection<Product> find(String type, String id)
 	{
 		List<Product> products = new ArrayList<Product>();
 		products.add(createComplexProduct());
 
 		return products;
 	}
 
 }
