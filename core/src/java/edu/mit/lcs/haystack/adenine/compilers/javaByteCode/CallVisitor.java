/* 
 * Copyright (c) 1998-2003 Massachusetts Institute of Technology. 
 * This code was developed as part of the Haystack research project 
 * (http://haystack.lcs.mit.edu/). Permission is hereby granted, 
 * free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in 
 * the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, subject to 
 * the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS IN THE SOFTWARE. 
 */

package edu.mit.lcs.haystack.adenine.compilers.javaByteCode;

import edu.mit.lcs.haystack.adenine.AdenineException;
import edu.mit.lcs.haystack.adenine.constructs.ICallVisitor;
import edu.mit.lcs.haystack.adenine.instructions.FunctionCallInstruction;
import edu.mit.lcs.haystack.adenine.interpreter.DynamicEnvironment;
import edu.mit.lcs.haystack.adenine.interpreter.Interpreter;
import edu.mit.lcs.haystack.adenine.interpreter.Message;
import edu.mit.lcs.haystack.adenine.parser2.IExpressionVisitor;
import edu.mit.lcs.haystack.adenine.tokenizer.GenericToken;
import edu.mit.lcs.haystack.adenine.tokenizer.Location;
import edu.mit.lcs.haystack.adenine.tokenizer.ResourceToken;
import edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken;
import edu.mit.lcs.haystack.rdf.Resource;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

import java.util.List;
import java.util.Map;

/**
 * @author David Huynh
 */
public class CallVisitor
	extends ConstructVisitorBase
	implements ICallVisitor {
		
	protected int		m_argumentCount = 0;
	protected int		m_namedArgumentCount = 0;
	protected int		m_resultCount = 0;

	protected boolean	m_generatedCall = false;
	
	public CallVisitor(TopLevelVisitor visitor, CodeFrame codeFrame) {
		super(visitor, codeFrame);
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.constructs.ICallVisitor#onCall(edu.mit.lcs.haystack.adenine.tokenizer.GenericToken)
	 */
	public void onCall(GenericToken callKeyword) {
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.constructs.ICallVisitor#onCallable(Location)
	 */
	public IExpressionVisitor onCallable(Location location) {
		return new ExpressionVisitor(m_topLevelVisitor, m_codeFrame);
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.constructs.ICallVisitor#onComma(edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken)
	 */
	public void onComma(SymbolToken commaT) {
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.constructs.ICallVisitor#onArgument(Location)
	 */
	public IExpressionVisitor onArgument(Location location) {
		InstructionList iList = m_codeFrame.getInstructionList();

		if (m_argumentCount == 0) {
			pushArgumentList(location);
		}
		m_argumentCount++;
	
		m_codeFrame.getMethodGen().addLineNumber(
			iList.append(InstructionConstants.DUP), // dup the array list
			location.getTrueLine());
	
		/*
		 * 	Stack should look like:
		 * 
		 * 		argument list	<-- top
		 * 		argument list
		 * 		function
		 * 		...
		 */
	
		return new ExpressionVisitor(m_topLevelVisitor, m_codeFrame) {
			public void end(Location endLocation) {
				onDoneArgument(endLocation);
			}
		};
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.constructs.ICallVisitor#onNamedArgument(edu.mit.lcs.haystack.adenine.tokenizer.ResourceToken, edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken)
	 */
	public IExpressionVisitor onNamedArgument(
		ResourceToken name,
		SymbolToken equalT) {

		InstructionList iList = m_codeFrame.getInstructionList();
		MethodGen		mg = m_codeFrame.getMethodGen();
		Location		location = name.getSpan().getStart();
		int			line = location.getTrueLine();

		if (m_namedArgumentCount == 0) {
			if (m_argumentCount == 0) {
				pushArgumentList(location);
			}
			pushNamedArgumentMap(location);
		}
		m_namedArgumentCount++;

		mg.addLineNumber(
			iList.append(InstructionConstants.DUP), // dup the map
			line);
	
		/*
		 * 	Push the argument URI
		 */
	
		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createNew(JavaByteCodeUtilities.s_typeResource)),
			line);
		mg.addLineNumber(
			iList.append(InstructionConstants.DUP),
			line);
		mg.addLineNumber(
			iList.append(new PUSH(m_codeFrame.getConstantPoolGen(), m_topLevelVisitor.resolveURI(name).getURI())),
			line);
		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createInvoke(
				JavaByteCodeUtilities.s_typeResource.getClassName(),
				"<init>",
				Type.VOID,
				new Type[] { Type.STRING },
				org.apache.bcel.Constants.INVOKESPECIAL
			)),
			line);

		/*
		 * 	Stack should look like:
		 * 
		 * 		argument URI
		 * 		map
		 * 		map
		 * 		argument list
		 * 		function
		 * 		...
		 */

		return new ExpressionVisitor(m_topLevelVisitor, m_codeFrame) {
			public void end(Location endLocation) {
				onDoneNamedArgument(endLocation);
			}
		};
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.constructs.ICallVisitor#onResult(edu.mit.lcs.haystack.adenine.tokenizer.GenericToken)
	 */
	public void onResult(GenericToken variable) {
		if (!m_generatedCall) {
			generateCall(variable.getSpan().getStart());
		}

		InstructionList iList = m_codeFrame.getInstructionList();
		MethodGen		mg = m_codeFrame.getMethodGen();
		int			line = variable.getSpan().getStart().getTrueLine();
	
		mg.addLineNumber(
			iList.append(InstructionConstants.DUP), // duplicate result message
			line);
		mg.addLineNumber(
			iList.append(new PUSH(m_codeFrame.getConstantPoolGen(), variable.getContent())),
			line);
	
		/*
		 * 	Push __dynamicenvironment__
		 */
	 
		mg.addLineNumber(
			iList.append(InstructionConstants.ALOAD_0),		// load this
			line);
		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createGetField(
				m_codeFrame.getClassGen().getClassName(), 
				"__dynamicenvironment__", 
				JavaByteCodeUtilities.s_typeDynamicEnvironment)),		// get __dynamicenvironment__
			line);

		/*
		 * 	Push __interpreter__
		 */
		mg.addLineNumber(
			iList.append(InstructionConstants.ALOAD_0),		// load this
			line);
		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createGetField(
				m_codeFrame.getClassGen().getClassName(), 
				"__interpreter__", 
				JavaByteCodeUtilities.s_typeInterpreter)),				// get __interpreter__
			line);
		
		/*
		 * 	Push index of result
		 */
	 
		mg.addLineNumber(
			iList.append(new PUSH(m_codeFrame.getConstantPoolGen(), m_resultCount++)),
			line);
		
		/*
		 * 	Stack should look like this:
		 * 
		 * 		index of result
		 * 		interpreter
		 * 		dynamic environment
		 * 		variable name
		 * 		result message
		 * 		result message
		 * 		...
		 */

		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createInvoke(
				this.getClass().getName(),
				"setVariable",
				Type.VOID,
				new Type[] {
					JavaByteCodeUtilities.s_typeMessage, 
					Type.STRING,
					JavaByteCodeUtilities.s_typeDynamicEnvironment,
					JavaByteCodeUtilities.s_typeInterpreter,
					Type.INT
				},
				org.apache.bcel.Constants.INVOKESTATIC
			)),
			line);
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.constructs.ICallVisitor#onNamedResult(edu.mit.lcs.haystack.adenine.tokenizer.ResourceToken, edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken, edu.mit.lcs.haystack.adenine.tokenizer.GenericToken)
	 */
	public void onNamedResult(
		ResourceToken name,
		SymbolToken equalT,
		GenericToken variable) {

		if (!m_generatedCall) {
			generateCall(name.getSpan().getStart());
		}

		InstructionList iList = m_codeFrame.getInstructionList();
		MethodGen		mg = m_codeFrame.getMethodGen();
		int			line = variable.getSpan().getStart().getTrueLine();

		mg.addLineNumber(	
			iList.append(InstructionConstants.DUP), // duplicate result message
			line);
		mg.addLineNumber(	
			iList.append(new PUSH(m_codeFrame.getConstantPoolGen(), variable.getContent())),
			line);
	
		/*
		 * 	Push __dynamicenvironment__
		 */
	 
		mg.addLineNumber(	
			iList.append(InstructionConstants.ALOAD_0),		// load this
			line);
		mg.addLineNumber(	
			iList.append(m_codeFrame.getInstructionFactory().createGetField(
				m_codeFrame.getClassGen().getClassName(), 
				"__dynamicenvironment__", 
				JavaByteCodeUtilities.s_typeDynamicEnvironment)),		// get __dynamicenvironment__
			line);

		/*
		 * 	Push __interpreter__
		 */
		mg.addLineNumber(	
			iList.append(InstructionConstants.ALOAD_0),		// load this
			line);
		mg.addLineNumber(	
			iList.append(m_codeFrame.getInstructionFactory().createGetField(
				m_codeFrame.getClassGen().getClassName(), 
				"__interpreter__", 
				JavaByteCodeUtilities.s_typeInterpreter)),				// get __interpreter__
			line);
		
		/*
		 * 	Push index of result
		 */
	 
		mg.addLineNumber(	
			iList.append(new PUSH(m_codeFrame.getConstantPoolGen(), m_topLevelVisitor.resolveURI(name).getURI())),
			line);
		
		/*
		 * 	Stack should look like this:
		 * 
		 * 		uri of result
		 * 		interpreter
		 * 		dynamic environment
		 * 		variable name
		 * 		result message
		 * 		result message
		 * 		...
		 */

		mg.addLineNumber(	
			iList.append(m_codeFrame.getInstructionFactory().createInvoke(
				this.getClass().getName(),
				"setVariableByURI",
				Type.VOID,
				new Type[] {
					JavaByteCodeUtilities.s_typeMessage, 
					Type.STRING,
					JavaByteCodeUtilities.s_typeDynamicEnvironment,
					JavaByteCodeUtilities.s_typeInterpreter,
					Type.STRING
				},
				org.apache.bcel.Constants.INVOKESTATIC
			)),
			line);
	}

	public void end(Location endLocation) {
		if (!m_generatedCall) {
			generateCall(endLocation);
		}
	
		m_codeFrame.getMethodGen().addLineNumber(
			m_codeFrame.getInstructionList().append(InstructionConstants.POP),
			endLocation.getTrueLine()); // pop result message
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.parser2.IApplyVisitor#onLeftParenthesis(edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken)
	 */
	public void onLeftParenthesis(SymbolToken leftParenthesisT) {
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.parser2.IApplyVisitor#onRightParenthesis(edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken)
	 */
	public void onRightParenthesis(SymbolToken rightParenthesisT) {
	}

	protected void onDoneArgument(Location location) {
		InstructionList iList = m_codeFrame.getInstructionList();
		MethodGen		mg = m_codeFrame.getMethodGen();
		int			line = location.getTrueLine();

		/*
		 * 	Stack should look like:
		 *
		 * 		argument		<-- top 
		 * 		argument list
		 * 		argument list
		 * 		function
		 * 		...
		 */

		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createInvoke(
				JavaByteCodeUtilities.s_typeList.getClassName(),
				"add",
				Type.BOOLEAN,
				new Type[] { Type.OBJECT },
				org.apache.bcel.Constants.INVOKEINTERFACE
			)),
			line);
		mg.addLineNumber(
			iList.append(InstructionConstants.POP),
			line);

		/*
		 * 	Stack should look like:
		 *
		 * 		argument list	<-- top
		 * 		function
		 * 		...
		 */
	}

	protected void onDoneNamedArgument(Location location) {
		InstructionList iList = m_codeFrame.getInstructionList();
		MethodGen		mg = m_codeFrame.getMethodGen();
		int			line = location.getTrueLine();

		/*
		 * 	Stack should look like:
		 *
		 * 		argument		<-- top
		 * 		argument URI 
		 * 		map
		 * 		map
		 * 		argument list
		 * 		function
		 * 		...
		 */
	 
	 	mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createInvoke(
				JavaByteCodeUtilities.s_typeMap.getClassName(),
				"put",
				Type.OBJECT,
				new Type[] { Type.OBJECT, Type.OBJECT },
				org.apache.bcel.Constants.INVOKEINTERFACE
			)),
			line);
		mg.addLineNumber(
			iList.append(InstructionConstants.POP),
			line);

		/*
		 * 	Stack should look like:
		 *
		 *		map				<-- top
		 * 		argument list
		 * 		function
		 * 		...
		 */
	}

	protected void pushArgumentList(Location location) {
		InstructionList iList = m_codeFrame.getInstructionList();
		MethodGen		mg = m_codeFrame.getMethodGen();
		int			line = location.getTrueLine();

		/*
		 * 	Construct an ArrayList and push it on the stack
		 */
	
		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createNew(JavaByteCodeUtilities.s_typeArrayList)),
			line);
		mg.addLineNumber(
			iList.append(InstructionConstants.DUP),
			line);

		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createInvoke(
				JavaByteCodeUtilities.s_typeArrayList.getClassName(),
				"<init>",
				Type.VOID,
				Type.NO_ARGS,
				org.apache.bcel.Constants.INVOKESPECIAL
			)),
			line);
	}

	protected void pushNamedArgumentMap(Location location) {
		InstructionList iList = m_codeFrame.getInstructionList();
		MethodGen		mg = m_codeFrame.getMethodGen();
		int			line = location.getTrueLine();

		/*
		 * 	Construct a HashMap and push it on the stack
		 */
	
		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createNew(JavaByteCodeUtilities.s_typeHashMap)),
			line);
		mg.addLineNumber(
			iList.append(InstructionConstants.DUP),
			line);

		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createInvoke(
				JavaByteCodeUtilities.s_typeHashMap.getClassName(),
				"<init>",
				Type.VOID,
				Type.NO_ARGS,
				org.apache.bcel.Constants.INVOKESPECIAL
			)),
			line);
	}

	protected void generateCall(Location location) {
		InstructionList iList = m_codeFrame.getInstructionList();
		MethodGen		mg = m_codeFrame.getMethodGen();
		int			line = location.getTrueLine();

		if (m_namedArgumentCount == 0) {
			if (m_argumentCount == 0) {
				pushArgumentList(location);
			}
			pushNamedArgumentMap(location);
		}
	
		/*
		 * 	Push __dynamicenvironment__
		 */
		mg.addLineNumber(
			iList.append(InstructionConstants.THIS),		// load this
			line);
		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createGetField(
				m_codeFrame.getClassGen().getClassName(), 
				"__dynamicenvironment__", 
				JavaByteCodeUtilities.s_typeDynamicEnvironment)),		// get __dynamicenvironment__
			line);

		/*
		 * 	Push __interpreter__
		 */
		mg.addLineNumber(
			iList.append(InstructionConstants.THIS),		// load this
			line);
		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createGetField(
				m_codeFrame.getClassGen().getClassName(), 
				"__interpreter__", 
				JavaByteCodeUtilities.s_typeInterpreter)),				// get __interpreter__
			line);

		/*
		 * 	Push line
		 */
		mg.addLineNumber(
			iList.append(new PUSH(m_codeFrame.getConstantPoolGen(), m_startLocation.getTrueLine())),
			line);
	 
		mg.addLineNumber(
			iList.append(m_codeFrame.getInstructionFactory().createInvoke(
				this.getClass().getName(),
				"doFunctionCall",
				JavaByteCodeUtilities.s_typeMessage,
				new Type[] { 
					Type.OBJECT, 
					JavaByteCodeUtilities.s_typeList, 
					JavaByteCodeUtilities.s_typeMap,
					JavaByteCodeUtilities.s_typeDynamicEnvironment,
					JavaByteCodeUtilities.s_typeInterpreter,
					Type.INT
				},
				org.apache.bcel.Constants.INVOKESTATIC
			)),
			line);
	
		/*
		 * 	Stack should look like
		 * 
		 * 		result message
		 * 		...
		 */
	
		m_generatedCall = true;
	}

	static public Message doFunctionCall(
		Object 				callable, 
		List 				arguments, 
		Map 				namedArguments,
		DynamicEnvironment	dynamicEnvironment,
		Interpreter			interpreter,
		int				line
	) throws AdenineException {
		return FunctionCallInstruction.doFunctionCall(
			callable, 
			arguments.toArray(), 
			namedArguments, 
			dynamicEnvironment, 
			interpreter,
			line
		);
	}

	static public void setVariable(
		Message				message,
		String				name,
		DynamicEnvironment	dynamicEnvironment,
		Interpreter			interpreter,
		int				index
	) throws AdenineException {
		if (index < message.m_values.length) {
			dynamicEnvironment.setValue(name, message.m_values[index]);
		} else {
			dynamicEnvironment.setValue(name, null);
		}
	}

	static public void setVariableByURI(
		Message				message,
		String				name,
		DynamicEnvironment	dynamicEnvironment,
		Interpreter			interpreter,
		String				uri
	) throws AdenineException {
		Resource res = new Resource(uri);
	
		if (message.m_namedValues != null && res != null && message.m_namedValues.containsKey(res)) {
			dynamicEnvironment.setValue(name, message.m_namedValues.get(res));
		} else {
			dynamicEnvironment.setValue(name, null);
		}
	}
}
