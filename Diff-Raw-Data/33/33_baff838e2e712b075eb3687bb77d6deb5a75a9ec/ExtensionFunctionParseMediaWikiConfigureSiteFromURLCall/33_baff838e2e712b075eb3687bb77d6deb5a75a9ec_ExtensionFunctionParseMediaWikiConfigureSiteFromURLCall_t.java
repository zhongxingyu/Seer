 /**
  * Copyright 2012 Omar Siam
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.sweble.wikitext.saxon;
 
 import net.sf.saxon.expr.XPathContext;
 import net.sf.saxon.om.*;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.tree.iter.EmptyIterator;
 import net.sf.saxon.value.BooleanValue;
 import net.sf.saxon.value.StringValue;
 
 public class ExtensionFunctionParseMediaWikiConfigureSiteFromURLCall 
 		extends 
 			ExtensionFunctionParseMediaWikiConfigureSiteCall 
 {
 
 
 	private static final long serialVersionUID = -6835617630116534748L;
 
 
 	/**
 	 * see also http://old.nabble.com/problem-returning-a-document-fragment-from-saxon-9.3-integrated-extension-function-td32318492.html
 	 */
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public SequenceIterator<NodeInfo> call(
 			@SuppressWarnings("rawtypes") SequenceIterator<? extends Item>[] args, XPathContext ctx)
 					throws XPathException {
 		boolean reportProblems = false;
 		String arg, contentLang, iwPrefix, siteName, wikiUrl;
 
 		if (args.length < 2)
 		{
 			return EmptyIterator.getInstance();
 		}
 		
 		StringValue in = (StringValue) args[0].next();
 
 		if(null == in) {
 			return EmptyIterator.getInstance();
 		}
 
 		siteName = in.getStringValue();
 		
 		in = (StringValue) args[1].next();
 
 		if(null == in) {
 			return EmptyIterator.getInstance();
 		}
 		
 		arg = in.getStringValue();
 		wikiUrl =  arg.replaceAll("wiki/.+$", "wiki/\\$1");
 		contentLang = iwPrefix = arg.replaceAll("http://", "").replaceAll("\\..*$", ""); 
 		
 		boolean doWOMLikeProcessing = false;
 		
		if (args.length > 2) {
 			BooleanValue lastOption = (BooleanValue) args[2].next();
 			if(null != lastOption) {
 				doWOMLikeProcessing = lastOption.getBooleanValue();
 			}
 		}
 
 		if (args.length == 4) {
 			BooleanValue lastOption = (BooleanValue) args[3].next();
 			if(null != lastOption) {
 				reportProblems = lastOption.getBooleanValue();
 			}
 		}
 
 		return call(siteName, wikiUrl, contentLang, iwPrefix, doWOMLikeProcessing, reportProblems, ctx);
 	}
 
 }
