 package com.redhat.contentspec;
 
 public class KeyValueNode<T> extends Node
 {
 	private final String key;
 	private T value = null;
 	private final char separator;
 	
 	public KeyValueNode(final String key, final T value, final char separator)
 	{
 		super(key + " " + separator + " " + value);
 		this.key = key;
 		this.setValue(value);
 		this.separator = separator;
 	}
 	
 	public KeyValueNode(final String key, final T value)
 	{
 		this(key, value, '=');
 	}
 	
 	@Override
 	public Integer getStep()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected void removeParent()
 	{
 		getParent().removeChild(this);
 	}
 
 	public String getKey()
 	{
 		return key;
 	}
 
 	public T getValue()
 	{
 		return value;
 	}
 
 	public void setValue(final T value)
 	{
 		this.value = value;
 	}
 
 	@Override
 	public ContentSpec getParent()
 	{
 		return (ContentSpec) parent;
 	}
 	
 	/**
 	 * Sets the Parent node for the Comment.
 	 * 
 	 * @param parent The parent node for the comment.
 	 */
 	protected void setParent(final ContentSpec parent)
 	{
 		super.setParent(parent);
 	}
 	
 	public String getText()
 	{
		return key + " " + separator + " " + value.toString();
 	}
 	
 	@Override
 	public String toString()
 	{
 		return getText() + "\n";
 	}
 }
