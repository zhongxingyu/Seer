 //Copyright 2012 Joshua Scoggins. All rights reserved.
 //
 //Redistribution and use in source and binary forms, with or without modification, are
 //permitted provided that the following conditions are met:
 //
 //   1. Redistributions of source code must retain the above copyright notice, this list of
 //      conditions and the following disclaimer.
 //
 //   2. Redistributions in binary form must reproduce the above copyright notice, this list
 //      of conditions and the following disclaimer in the documentation and/or other materials
 //      provided with the distribution.
 //
 //THIS SOFTWARE IS PROVIDED BY Joshua Scoggins ``AS IS'' AND ANY EXPRESS OR IMPLIED
 //WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 //FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Joshua Scoggins OR
 //CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 //CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 //SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 //ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 //NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 //ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 //The views and conclusions contained in the software and documentation are those of the
 //authors and should not be interpreted as representing official policies, either expressed
 //or implied, of Joshua Scoggins. 
 package functions;
 
public abstract class DynamicDelegateBase extends Hashtable<String, Object> implements DynamicDelegate {
 
 	public DynamicDelegateBase() {
 		initVariables();
 	}
 
	public DynamicDelegateBase(DynamicDelegate b) {
 		this();
 		Set<String> keys = b.names();
 		for (String str : keys) {
 			Object var = b.getDynamicVariable(str);
 			if (var == null) //null check...just in case {
 				put(str, null);
 			}
 			if (var instanceof NonLocalClosedVariable) {
 				NonLocalClosedVariable v = new NonLocalClosedVariable((NonLocalClosedVariable) var);
 				put(str, v);
 			} else if (var instanceof ClosedVariable) {
 				ClosedVariable v = new ClosedVariable((ClosedVariable) var);
 				put(str, v);
 			} else {
 				put(str, var);
 			}
 		}
 	}
 
 	protected abstract void initVariables();
 
 	public boolean registerDynamicVariable(String name, Object value, boolean readonly) {
 		if (dynamicVariableExists(name)) {
 			return false;
 		}
 		Object var = readonly ? new ClosedVariable(value) : new NonLocalClosedVariable(value);
 		put(name, var);
 		return true;
 	}
 
 	public boolean dynamicVariableExists(String name) {
 		return containsKey(name);
 	}
 
 	private boolean isReadonly(String name) {
 		try {
 			Object value = getDynamicVariable(name);
 			return !(value instanceof NonLocalClosedVariable);
 		} catch (NoSuchElementException n) {
 			return false;
 		}
 	}
 
 	public void setDynamicVariable(String name, Object value) throws DynamicVariableReadonlyException {
 		if (!isReadonly(name)) {
 			NonLocalClosedVariable oldValue = (NonLocalClosedVariable) getDynamicVariable(name);
 			oldValue.actualValue(value);
 		} else {
 			throw new DynamicVariableReadonlyException();
 		}
 	}
 
 	public Object getDynamicVariable(String name) {
 		if (!containsKey(name)) {
 			throw new NoSuchElementException("The provided key " + name + " does not exist.");
 		} else {
 			return get(name);
 		}
 	}
 
 	public <T> T getDynamicVariableAsType(String name) {
 		if (!containsKey(name)) {
 			throw new NoSuchElementException("The provided key " + name + " does not exist.");
 		} else {
 			return (T)as(get(name));
 		}
 	}
 
	public abstract DynamicDelegate copy();
 	public abstract Object clone();
 
 	public Set<String> getNames() {
 		return super.keySet();
 	}
 }
