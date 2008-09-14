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

package edu.mit.lcs.haystack.adenine.functions;

import edu.mit.lcs.haystack.adenine.*;
import edu.mit.lcs.haystack.adenine.interpreter.*;

/**
 * @version 	1.0
 * @author		Dennis Quan
 */
public class MinusFunction implements ICallable {

	/**
	 * @see ICallable#invoke(Message, DynamicEnvironment)
	 */
	public Message invoke(Message message, DynamicEnvironment denv) throws AdenineException {
		Object[] parameters = message.m_values;
		Object[] numbers = PlusFunction.upgradeNumberList(parameters);
		if ((numbers.length > 0) && (numbers[0] instanceof Double)) {
			double n = 0;
			for (int i = 0; i < numbers.length; i++) {
				if ((i > 0) || (numbers.length == 1)) {
					n -= ((Double)numbers[i]).doubleValue();
				} else {
					n = ((Double)numbers[i]).doubleValue();
				}
			}
			return new Message(new Double(n));
		} else if ((numbers.length > 0) && (numbers[0] instanceof Long)) {
			long n = 0;
			for (int i = 0; i < numbers.length; i++) {
				if ((i > 0) || (numbers.length == 1)) {
					n -= ((Long)numbers[i]).longValue();
				} else {
					n = ((Long)numbers[i]).longValue();
				}
			}
			return new Message(new Long(n));
		} else {
			int n = 0;
			for (int i = 0; i < numbers.length; i++) {
				if ((i > 0) || (numbers.length == 1)) {
					n -= ((Integer)numbers[i]).intValue();
				} else {
					n = ((Integer)numbers[i]).intValue();
				}
			}
			return new Message(new Integer(n));
		}
	}

}
