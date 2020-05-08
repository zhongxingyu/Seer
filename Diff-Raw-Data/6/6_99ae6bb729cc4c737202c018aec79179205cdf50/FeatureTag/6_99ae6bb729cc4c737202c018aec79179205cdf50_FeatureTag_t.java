 package br.ufal.cideei.soot.instrument;
 
 import java.util.AbstractCollection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import soot.tagkit.AttributeValueException;
 import soot.tagkit.Tag;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class FeatureTag is used to store feature-sensitive metadata. The
  * metadata is simply stored in a List collection.
  * 
  * @param <E>
  *            the element type
  */
 public class FeatureTag<E> extends AbstractCollection<E> implements Tag {
 
 	/** The Constant FEAT_TAG_NAME. */
 	private static final String FEAT_TAG_NAME = "FeatureTag";
 
 	/** The feature are kept in this list */
 	private List<E> features = new ArrayList<E>();
 
 	/**
 	 * Adds a feature to the list.
 	 * 
 	 * @param ft
 	 *            the feature to be added.
 	 */
 	public boolean add(E ft) {
 		return features.add(ft);
 	}
 
 	/**
 	 * Removes a given feature from the list.
 	 * 
 	 * @param ft
 	 *            the feature to be removed.
	 * @return 
 	 */
	public boolean remove(Object ft) {
		return features.remove(ft);
 	}
 
 	/**
 	 * Checks for feature.
 	 * 
 	 * @param ft
 	 *            the feature
 	 * @return true, if the feature is contained in this Tag.
 	 */
 	public boolean hasFeature(E ft) {
 		return features.contains(ft);
 	}
 
 	/**
 	 * Gets the features as an unmodifiable List.
 	 * 
 	 * @return the features
 	 */
 	public Collection<E> getFeatures() {
 		return Collections.unmodifiableList(this.features);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see soot.tagkit.Tag#getName()
 	 */
 	@Override
 	public String getName() {
 		return FeatureTag.FEAT_TAG_NAME;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see soot.tagkit.Tag#getValue()
 	 */
 	@Override
 	public byte[] getValue() throws AttributeValueException {
 		return null;
 	}
 	
 	@Override
 	public String toString() {
 		return features.toString();
 	}
 
 	@Override
 	public Iterator<E> iterator() {
 		return features.iterator();
 	}
 
 	@Override
 	public int size() {
 		return features.size();
 	}
 
 }
