 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package de.weltraumschaf.shackhack;
 
 import com.google.common.collect.Lists;
 import de.weltraumschaf.shackhack.ShackHackException.Code;
 import de.weltraumschaf.shackhack.SymbolTable.Entry;
 import de.weltraumschaf.shackhack.antlr.ShackHackBaseVisitor;
 import de.weltraumschaf.shackhack.antlr.ShackHackParser;
 import de.weltraumschaf.shackhack.antlr.ShackHackParser.AddSubExpressionContext;
 import de.weltraumschaf.shackhack.antlr.ShackHackParser.AssignStatementContext;
 import de.weltraumschaf.shackhack.antlr.ShackHackParser.ExpressionStatementContext;
 import de.weltraumschaf.shackhack.antlr.ShackHackParser.IdentiferExpressionContext;
 import de.weltraumschaf.shackhack.antlr.ShackHackParser.MulDivExpressionContext;
 import de.weltraumschaf.shackhack.antlr.ShackHackParser.ParenExpressionContext;
 import de.weltraumschaf.shackhack.antlr.ShackHackParser.ProgramContext;
 import de.weltraumschaf.shackhack.antlr.ShackHackParser.ValueExpressionContext;
 import java.util.List;
 import org.antlr.v4.runtime.tree.ParseTree;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 final class ByteCodeVisitor extends ShackHackBaseVisitor<List<Instruction>> {
 
     private final SymbolTable table = new SymbolTable();
 
     @Override
     protected List<Instruction> defaultResult() {
         return Lists.newArrayList();
     }
 
     @Override
     public List<Instruction> visitProgram(final ProgramContext ctx) {
         final ParseTree program = ctx.getChild(0);
 
         if (null == program) {
             return defaultResult();
         }
 
         return visit(program);
     }
 
     @Override
     public List<Instruction> visitExpressionStatement(final ExpressionStatementContext ctx) {
         return visit(ctx.getChild(0));
     }
 
     @Override
     public List<Instruction> visitAssignStatement(final AssignStatementContext ctx) {
         final List<Instruction> instructions = Lists.newArrayList();
         instructions.addAll(visit(ctx.val));
         final String identifier = ctx.id.getText();
         Entry symbol = table.lookup(identifier);
 
         if (null == symbol) {
             symbol = table.enter(identifier);
         }
         final Instruction instruction = Instruction.newInstance(ByteCode.ISTORE, symbol.getId());
         instruction.setComment("store variable " + symbol.getName());
         instructions.add(instruction);
         return instructions;
     }
 
     @Override
     public List<Instruction> visitMulDivExpression(final MulDivExpressionContext ctx) {
         final List<Instruction> instructions = Lists.newArrayList();
         instructions.addAll(visit(ctx.left));
         instructions.addAll(visit(ctx.right));
 
         if (ctx.operator.getType() == ShackHackParser.OP_STAR) {
             instructions.add(Instruction.newInstance(ByteCode.IMUL));
         } else if (ctx.operator.getType() == ShackHackParser.OP_SLASH) {
             instructions.add(Instruction.newInstance(ByteCode.IDIV));
         } else {
             throw new ShackHackException("Unexpected operator: " + ctx.operator, Code.SYNTAX_ERROR);
         }
 
         return instructions;
     }
 
     @Override
     public List<Instruction> visitAddSubExpression(final AddSubExpressionContext ctx) {
         final List<Instruction> instructions = Lists.newArrayList();
         instructions.addAll(visit(ctx.left));
         instructions.addAll(visit(ctx.right));
 
         if (ctx.operator.getType() == ShackHackParser.OP_PLUS) {
             instructions.add(Instruction.newInstance(ByteCode.IADD));
         } else if (ctx.operator.getType() == ShackHackParser.OP_MINUS) {
             instructions.add(Instruction.newInstance(ByteCode.ISUB));
         } else {
             throw new ShackHackException("Unexpected operator: " + ctx.operator, Code.SYNTAX_ERROR);
         }
 
         return instructions;
     }
 
     @Override
     public List<Instruction> visitValueExpression(final ValueExpressionContext ctx) {
         return Lists.newArrayList(Instruction.newInstance(ByteCode.LDC, ctx.getText()));
     }
 
 
     @Override
     public List<Instruction> visitIdentiferExpression(final IdentiferExpressionContext ctx) {
         final String identifier = ctx.id.getText();
         final Entry symbol = table.lookup(identifier);
 
         if (null == symbol) {
             throw ShackHackException.syntaxException(String.format("Undefined variable identifier '%s'!", identifier));
         }
 
         final Instruction instruction = Instruction.newInstance(ByteCode.ILOAD, symbol.getId());
         instruction.setComment("load variable " + symbol.getName());
         return Lists.newArrayList(instruction);
     }
 
     @Override
     public List<Instruction> visitParenExpression(final ParenExpressionContext ctx) {
         return visit(ctx.inParens);
     }
 
     @Override
     public List<Instruction> visitValue(ShackHackParser.ValueContext ctx) {
         return visit(ctx.getChild(0));
     }
 
 }
