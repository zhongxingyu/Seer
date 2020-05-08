 /**
  * Copyright (c) 2007-2010, JAGaToo Project Group all rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the 'Xith3D Project Group' nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
  * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE
  */
 package org.jagatoo.commands;
 
import java.util.ArrayList;
 
 import org.jagatoo.util.strings.StringUtils;
 
 /**
  * This abstract base class for the Command interface correctly overrides
  * the {@link #hashCode()} and {@link #equals(Object)} methods.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public abstract class CommandBase implements Command
 {
     private final String key;
     private final String text;
     protected final Object[] paramsArray;
     
     /**
      * {@inheritDoc}
      */
     public final String getKey()
     {
         return ( key );
     }
     
     /**
      * {@inheritDoc}
      */
     public final String getText()
     {
         return ( text );
     }
     
     /**
      * {@inheritDoc}
      */
     public String getLocalizedText()
     {
         return ( getText() );
     }
     
     /**
      * {@inheritDoc}
      */
     public final int getNumParameters()
     {
         return ( paramsArray.length );
     }
     
    public Object[] createParametersArray( ArrayList< String > parameters )
     {
         if ( parameters == null )
             return ( null );
         
         if ( parameters.size() != getNumParameters() )
             throw new IllegalArgumentException( "number of parameters must be " + getNumParameters() + ". Got " + parameters.size() + "." );
         
         for ( int i = 0; i < getNumParameters(); i++ )
         {
             final String paramString = parameters.get( i );
             
             if ( paramString == null )
             {
                 paramsArray[ i ] = null;
             }
             else if ( StringUtils.isNumeric( paramString ) )
             {
                 if ( paramString.indexOf( '.' ) < 0 )
                     paramsArray[ i ] = Integer.parseInt( paramString );
                 else
                     paramsArray[ i ] = Float.parseFloat( paramString );
             }
             else if ( StringUtils.isBoolean( paramString ) )
             {
                 paramsArray[ i ] = Boolean.parseBoolean( paramString );
             }
             else
             {
                 paramsArray[ i ] = paramString;
             }
         }
         
         return ( paramsArray );
     }
     
     /**
      * {@inheritDoc}
      */
     public final String execute( Object[] parameters ) throws CommandException
     {
         return ( execute( null, parameters ) );
     }
     
     /**
      * {@inheritDoc}
      */
     public String execute( Boolean inputInfo, CommandLine commandLine ) throws CommandException
     {
         return ( execute( inputInfo, createParametersArray( commandLine.getParameters() ) ) );
     }
     
     /**
      * {@inheritDoc}
      */
     public String execute( CommandLine commandLine ) throws CommandException
     {
         return ( execute( createParametersArray( commandLine.getParameters() ) ) );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode()
     {
         return ( getKey().hashCode() );
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals( Object o )
     {
         if ( !( o instanceof Command ) )
             return ( false );
         
         return ( getKey().equals( ((Command)o).getKey() ) );
     }
     
     public CommandBase( final String key, final String text, final int numParams )
     {
         this.key = key;
         this.text = text;
         this.paramsArray = new Object[ numParams ];
     }
     
     public CommandBase( final String key, final int numParams )
     {
         this( key, null, numParams );
     }
 }
