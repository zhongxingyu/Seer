 package com.lolcode.runtime;
 
 
 public class LolObject {
     public LolType type;
     protected int intVal;
     protected double doubleVal;
     protected String strVal;
 
     public LolObject() {
         type = LolType.UNDEFINED;
     }
 
     public LolObject(LolType type) {
         this.type = type;
     }
 
     protected boolean toBool() {
         return intVal != 0;
     }
 
     public int toInt() {
         return this.toLolBool().toInt();
     }
 
     public LolObject get(LolObject key) {
         throw new LolRuntimeException("get() called on non-array type: " + type);
     }
 
     public LolObject put(LolObject key) {
         throw new LolRuntimeException("get() called on non-array type: " + type);
     }
 
     public LolObject compare(LolObject rhs) {
         if (rhs.type != this.type)
             throw new LolRtBadCompareException("cannot compare " + this.type + " with " + rhs.type);
         switch (type) {
             case INT:
                 return new LolInt(Integer.compare(this.intVal, rhs.intVal));
             case STRING:
                 return new LolInt(this.strVal.compareTo(rhs.strVal));
             case DOUBLE:
                 return new LolInt(Double.compare(this.doubleVal, rhs.doubleVal));
             case BOOL:
                 return new LolInt(Boolean.compare(this.intVal != 0, rhs.intVal != 0));
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("compare()", type);
         }
     }
 
     public LolObject toLolBool() {
         switch (type) {
             case INT:
                 return new LolBool(toBool());
             case STRING:
                 return new LolBool(!strVal.isEmpty());
             case DOUBLE:
                 return new LolBool(doubleVal > 0);
             case BOOL:
                 return new LolBool(toBool());
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("toLolBool()", type);
         }
     }
 
     public LolObject toLolInt() {
         switch (type) {
             case INT:
                 return new LolInt(intVal);
             case STRING:
                 return new LolInt(Integer.parseInt(strVal));
             case DOUBLE:
                 return new LolInt((int) doubleVal);
             case BOOL:
                 return new LolInt(intVal);
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("toLolInt()", type);
         }
     }
 
     public LolObject toLolDouble() {
         switch (type) {
             case INT:
                 return new LolDouble(intVal);
             case STRING:
                 return new LolDouble(Double.parseDouble(strVal));
             case DOUBLE:
                 return new LolDouble(doubleVal);
             case BOOL:
                 return new LolDouble(intVal);
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("toLolDouble()", type);
         }
     }
 
     public LolObject toLolString() {
         switch (type) {
             case INT:
                 return new LolString(Integer.toString(intVal));
             case STRING:
                 return new LolString(strVal);
             case DOUBLE:
                 return new LolString(Double.toString(doubleVal));
             case BOOL:
                 return new LolString(Boolean.toString(toBool()));
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("toLolString()", type);
         }
     }
 
     public LolObject add(LolObject rhs) {
 //        if (rhs.type != this.type)
 //            throw new LolRtBinOpWrongTypeException("add", this.type, rhs.type);
         switch (type) {
             case INT:
                 if (rhs.type == LolType.INT)
                     return new LolInt(this.intVal + rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.intVal + rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("add", this.type, rhs.type);
             case STRING:
                 if (rhs.type != LolType.STRING)
                     throw new LolRtBinOpWrongTypeException("add", this.type, rhs.type);
                 return new LolString(this.strVal + rhs.strVal);
             case DOUBLE:
                 if (rhs.type == LolType.INT)
                     return new LolDouble(this.doubleVal + rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.doubleVal + rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("add", this.type, rhs.type);
             case BOOL:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("add", type);
         }
     }
 
     public LolObject sub(LolObject rhs) {
 //        if (rhs.type != this.type)
 //            throw new LolRtBinOpWrongTypeException("sub", this.type, rhs.type);
         switch (type) {
             case INT:
                 if (rhs.type == LolType.INT)
                     return new LolInt(this.intVal - rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.intVal - rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("sub", this.type, rhs.type);
             case DOUBLE:
                 if (rhs.type == LolType.INT)
                     return new LolDouble(this.doubleVal - rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.doubleVal - rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("sub", this.type, rhs.type);
             case BOOL:
             case STRING:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("sub", type);
         }
     }
 
     public LolObject mul(LolObject rhs) {
 //        if (rhs.type != this.type)
 //            throw new LolRtBinOpWrongTypeException("sub", this.type, rhs.type);
         switch (type) {
             case INT:
                 if (rhs.type == LolType.INT)
                     return new LolInt(this.intVal * rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.intVal * rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("mul", this.type, rhs.type);
             case DOUBLE:
                 if (rhs.type == LolType.INT)
                     return new LolDouble(this.doubleVal * rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.doubleVal * rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("mul", this.type, rhs.type);
             case BOOL:
             case STRING:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("mul", type);
         }
     }
 
     public LolObject div(LolObject rhs) {
 //        if (rhs.type != this.type)
 //            throw new LolRtBinOpWrongTypeException("div", this.type, rhs.type);
         switch (type) {
             case INT:
                 if (rhs.type == LolType.INT)
                     return new LolInt(this.intVal / rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.intVal / rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("div", this.type, rhs.type);
             case DOUBLE:
                 if (rhs.type == LolType.INT)
                     return new LolDouble(this.doubleVal / rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.doubleVal / rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("div", this.type, rhs.type);
             case BOOL:
             case STRING:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("div", type);
         }
     }
 
     public LolObject mod(LolObject rhs) {
 //        if (rhs.type != this.type)
 //            throw new LolRtBinOpWrongTypeException("mod", this.type, rhs.type);
         switch (type) {
             case INT:
                 if (rhs.type == LolType.INT)
                     return new LolInt(this.intVal % rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.intVal % rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("mod", this.type, rhs.type);
             case DOUBLE:
                 if (rhs.type == LolType.INT)
                     return new LolDouble(this.doubleVal % rhs.intVal);
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(this.doubleVal % rhs.doubleVal);
                 else
                     throw new LolRtBinOpWrongTypeException("mod", this.type, rhs.type);
             case BOOL:
             case STRING:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("mod", type);
         }
     }
 
     public LolObject and(LolObject rhs) {
         if (rhs.type != this.type)
             throw new LolRtBinOpWrongTypeException("and", this.type, rhs.type);
         switch (type) {
             case BOOL:
                 return new LolBool(this.toBool() & rhs.toBool());
             case INT:
             case DOUBLE:
             case STRING:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("and", type);
         }
     }
 
     public LolObject or(LolObject rhs) {
         if (rhs.type != this.type)
             throw new LolRtBinOpWrongTypeException("or", this.type, rhs.type);
         switch (type) {
             case BOOL:
                 return new LolBool(this.toBool() | rhs.toBool());
             case INT:
             case DOUBLE:
             case STRING:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("or", type);
         }
     }
 
     public LolObject not() {
         switch (type) {
             case BOOL:
                 return new LolBool(!this.toBool());
             case INT:
             case DOUBLE:
             case STRING:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("not", type);
         }
     }
 
     public LolObject eq(LolObject rhs) {
         return (this.compare(rhs).intVal == 0) ? LolBool.TRUE : LolBool.FALSE;
     }
 
     public LolObject neq(LolObject rhs) {
         return this.eq(rhs).not();
     }
 
     public LolObject max(LolObject rhs) {
 //        if (rhs.type != this.type)
 //            throw new LolRtBinOpWrongTypeException("add", this.type, rhs.type);
         switch (type) {
             case INT:
                 if (rhs.type == LolType.INT)
                     return new LolInt(Math.max(this.intVal,rhs.intVal));
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(Math.max(this.intVal,rhs.doubleVal));
                 else
                     throw new LolRtBinOpWrongTypeException("max", this.type, rhs.type);
             case STRING:
                 throw new LolRtBinOpWrongTypeException("max", this.type, rhs.type);
             case DOUBLE:
                 if (rhs.type == LolType.INT)
                     return new LolDouble(Math.max(this.doubleVal,rhs.intVal));
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(Math.max(this.doubleVal,rhs.doubleVal));
                 else
                     throw new LolRtBinOpWrongTypeException("max", this.type, rhs.type);
             case BOOL:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("max", type);
         }
     }
 
     public LolObject min(LolObject rhs) {
 //        if (rhs.type != this.type)
 //            throw new LolRtBinOpWrongTypeException("add", this.type, rhs.type);
         switch (type) {
             case INT:
                 if (rhs.type == LolType.INT)
                     return new LolInt(Math.min(this.intVal,rhs.intVal));
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(Math.min(this.intVal,rhs.doubleVal));
                 else
                     throw new LolRtBinOpWrongTypeException("min", this.type, rhs.type);
             case STRING:
                 throw new LolRtBinOpWrongTypeException("min", this.type, rhs.type);
             case DOUBLE:
                 if (rhs.type == LolType.INT)
                     return new LolDouble(Math.min(this.doubleVal,rhs.intVal));
                 else if (rhs.type == LolType.DOUBLE)
                     return new LolDouble(Math.min(this.doubleVal,rhs.doubleVal));
                 else
                     throw new LolRtBinOpWrongTypeException("min", this.type, rhs.type);
             case BOOL:
             case UNDEFINED:
             default:
                 throw new LolRtUnsupportedOpException("min", type);
         }
     }
 }
