 package com.patrick_vane.unrealscript.editor.hyperlink_ctrl;
 
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import com.patrick_vane.unrealscript.editor.UnrealScriptEditor;
 import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptClass;
 import com.patrick_vane.unrealscript.editor.constants.WordConstant;
 import com.patrick_vane.unrealscript.editor.default_classes.MyAbstractHyperlink;
 import com.patrick_vane.unrealscript.editor.default_classes.MyAbstractHyperlinkDetector;
 import com.patrick_vane.unrealscript.editor.parser.CodeAttributeFunction;
 import com.patrick_vane.unrealscript.editor.parser.CodeAttributeVariable;
 import com.patrick_vane.unrealscript.editor.parser.CodeWord;
 import com.patrick_vane.unrealscript.editor.parser.UnrealScriptAttributes;
 
 
 public class HyperlinkDetector extends MyAbstractHyperlinkDetector
 {
 	private UnrealScriptAttributes lastAttributes;
 	private long lastTime;
 	
 	
 	@Override
 	protected IHyperlink getHyperlink( final CodeWord word, final String before, final String after ) throws Exception
 	{
 		if( ".".equals(before) )
 			return null;
 		
 		boolean startWithClass = true;
		if( (word != null) && (word.getWord() != null) )
 		{
			char firstChar = word.getWord().charAt( 0 );
 			if( firstChar == Character.toLowerCase(firstChar) ) // firstChar is lower case
 			{
 				 // before starts with lower case
 				startWithClass = false;
 			}
 		}
 		
 		IHyperlink hyperlink;
 		if( startWithClass )
 		{
 			hyperlink = getHyperlinkOfClass( word, before, after );
 			if( hyperlink != null )
 				return hyperlink;
 			
 			hyperlink = getHyperlinkOfAttribute( word, before, after );
 			if( hyperlink != null )
 				return hyperlink;
 		}
 		else
 		{
 			hyperlink = getHyperlinkOfAttribute( word, before, after );
 			if( hyperlink != null )
 				return hyperlink;
 			
 			hyperlink = getHyperlinkOfClass( word, before, after );
 			if( hyperlink != null )
 				return hyperlink;
 		}
 		
 		return null;
 	}
 	
 	
 	protected IHyperlink getHyperlinkOfClass( final CodeWord word, final String before, final String after ) throws Exception
 	{
 		final UnrealScriptClass unrealscriptClass = UnrealScriptEditor.getUnrealScriptClass( word.getWord() );
 		if( unrealscriptClass != null )
 		{
 			return new MyAbstractHyperlink( word )
 			{
 				@Override
 				public void open()
 				{
 					UnrealScriptEditor.openFile( unrealscriptClass.getFile() );
 				}
 			};
 		}
 		return null;
 	}
 	
 	protected IHyperlink getHyperlinkOfAttribute( final CodeWord word, final String before, final String after ) throws Exception
 	{
 		if( System.currentTimeMillis()-lastTime >= 3000 )
 		{
 			lastAttributes = UnrealScriptEditor.getActiveUnrealScriptAttributesWithParents();
 			lastTime = System.currentTimeMillis();
 		}
 		
 		if( "(".equals(after) )
 		{
 			final CodeAttributeFunction function = lastAttributes.getAttributeFunction( word.getWord(), (WordConstant.FUNCTION_KEYWORDS_HASHSET.contains(before) ? 1 : 0) );
 			
 			if( function != null )
 			{
 				final UnrealScriptClass unrealscriptClass = UnrealScriptEditor.getUnrealScriptClass( function.getClassName() );
 				if( unrealscriptClass != null )
 				{
 					return new MyAbstractHyperlink( word )
 					{
 						@Override
 						public void open()
 						{
 							UnrealScriptEditor.openFile( unrealscriptClass.getFile(), function.getFirstCharacterPosition(), function.getLastCharacterPosition() );
 						}
 					};
 				}
 			}
 		}
 		else
 		{
 			final CodeAttributeVariable variable = lastAttributes.getAttributeVariable( word.getWord() );
 			if( variable != null )
 			{
 				final UnrealScriptClass unrealscriptClass = UnrealScriptEditor.getUnrealScriptClass( variable.getClassName() );
 				if( unrealscriptClass != null )
 				{
 					return new MyAbstractHyperlink( word )
 					{
 						@Override
 						public void open()
 						{
 							UnrealScriptEditor.openFile( unrealscriptClass.getFile(), variable.getFirstCharacterPosition(), variable.getLastCharacterPosition() );
 						}
 					};
 				}
 			}
 		}
 		return null;
 	}
 }
