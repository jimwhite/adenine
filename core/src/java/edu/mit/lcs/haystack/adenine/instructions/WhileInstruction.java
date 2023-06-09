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

package edu.mit.lcs.haystack.adenine.instructions;

import edu.mit.lcs.haystack.adenine.AdenineConstants;
import edu.mit.lcs.haystack.adenine.AdenineException;
import edu.mit.lcs.haystack.adenine.interpreter.ConstantTable;
import edu.mit.lcs.haystack.adenine.interpreter.DynamicEnvironment;
import edu.mit.lcs.haystack.adenine.interpreter.Environment;
import edu.mit.lcs.haystack.adenine.interpreter.IExpression;
import edu.mit.lcs.haystack.adenine.interpreter.IInstructionHandler;
import edu.mit.lcs.haystack.adenine.interpreter.Interpreter;
import edu.mit.lcs.haystack.adenine.interpreter.VariableFrame;
import edu.mit.lcs.haystack.rdf.IRDFContainer;
import edu.mit.lcs.haystack.rdf.Resource;
import edu.mit.lcs.haystack.rdf.Utilities;

/**
 * @version 	1.0
 * @author		Dennis Quan
 */
public class WhileInstruction implements IInstructionHandler {
	IRDFContainer m_source;
	Interpreter m_interpreter;

	/**
	 * @see IInstructionHandler#initialize(Interpreter)
	 */
	public void initialize(Interpreter interpreter) {
		m_interpreter = interpreter;
		m_source = interpreter.getRootRDFContainer();
	}

	/**
	 * @see IInstructionHandler#isConstantExpression()
	 */
	public boolean isConstantExpression() {
		return false;
	}

	/**
	 * @see IInstructionHandler#evaluate(Resource, Environment, DynamicEnvironment)
	 */
	public Object evaluate(Resource res, Environment env, DynamicEnvironment denv)
		throws AdenineException {
		Resource resCondition = Utilities.getResourceProperty(res, AdenineConstants.CONDITION, m_source);
		Resource resBody = Utilities.getResourceProperty(res, AdenineConstants.body, m_source);
		
		try {
			Object condition;
			while (((condition = m_interpreter.runInstruction(resCondition, env, denv)) != null) &&
				Interpreter.isTrue(condition)) {
				Environment env2 = (Environment)env.clone();
				try {
					m_interpreter.runInstruction(resBody, env2, denv);
				} catch (BreakException be) {
					return null;
				} catch (ContinueException ce) {
				}
			}
		} catch (AdenineException ae) {
			ae.addToStackTrace("while", Interpreter.getLineNumber(res, m_source));
			throw ae;
		} catch (NullPointerException npe) {
			throw new AdenineException("Null pointer exception", npe, Interpreter.getLineNumber(res, m_source));
		}
		
		return null;	
	}

	/**
	 * @see IInstructionHandler#generateExpression(Resource)
	 */
	public IExpression generateExpression(Resource res) throws AdenineException {
		return new IExpression() {
			IExpression m_body;
			IExpression m_condition;
			int m_line;
			
			IExpression init(Resource res2) throws AdenineException {
				Resource resCondition = Utilities.getResourceProperty(res2, AdenineConstants.CONDITION, m_source);
				Resource resBody = Utilities.getResourceProperty(res2, AdenineConstants.body, m_source);
		
				m_body = m_interpreter.compileInstruction(resBody);
				m_condition = m_interpreter.compileInstruction(resCondition);
				
				m_line = Interpreter.getLineNumber(res2, m_source);
				return this;
			}
			
			public void generateJava(String targetVar, StringBuffer buffer, VariableFrame frame, ConstantTable ct) throws AdenineException {
				buffer.append("while (true) {\n");
				m_condition.generateJava(targetVar, buffer, frame, ct);
				buffer.append("if (!Interpreter.isTrue(");
				buffer.append(targetVar);
				buffer.append(")) {\nbreak;\n}\n");
				Interpreter.generateJavaBlock(m_body, buffer, frame, ct);
				buffer.append("}\n");
				buffer.append(targetVar);
				buffer.append(" = null;\n");
			}
			
			public Object evaluate(Environment env, DynamicEnvironment denv) throws AdenineException {
				Object condition;
				try {
					while (((condition = m_condition.evaluate(env, denv)) != null) &&
						Interpreter.isTrue(condition)) {
						Environment env2 = (Environment)env.clone();
						try {
							m_body.evaluate(env2, denv);
						} catch (BreakException be) {
							return null;
						} catch (ContinueException ce) {
						}
					}
				} catch (AdenineException ae) {
					ae.addToStackTrace("while", m_line);
					throw ae;
				} catch (NullPointerException npe) {
					throw new AdenineException("Null pointer exception", npe, m_line);
				}
								
				return null;	
			}
		}.init(res);
	}

}
