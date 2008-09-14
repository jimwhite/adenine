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

import java.io.PrintWriter;
import java.util.Iterator;

import edu.mit.lcs.haystack.adenine.AdenineException;
import edu.mit.lcs.haystack.adenine.interpreter.*;
import edu.mit.lcs.haystack.rdf.Resource;
import edu.mit.lcs.haystack.rdf.ListUtilities;

/**
 * @version 	1.0
 * @author		Dennis Quan
 */
public class PrintListFunction implements ICallable {
	private PrintWriter _pw;
	/**
	 * sets the print writer for this print function 
	 * @param pw
	 */
	public PrintListFunction(PrintWriter pw) {
		_pw = pw;
	}
	/**
	 * default constructor, output determined by __output__ in
	 * environment
	 */
	public PrintListFunction() {
		_pw = null;
	}
	
	/**
	 * @see ICallable#invoke(Message, DynamicEnvironment)
	 */
	public Message invoke(Message message, DynamicEnvironment denv) throws AdenineException {
		if (message.m_values.length != 1) {
			throw new AdenineException("printlist expects one parameter");
		}
		Iterator i = ListUtilities.accessDAMLList((Resource)message.m_values[0], denv.getSource());
		StringBuffer sb = new StringBuffer();
		while (i.hasNext()) {
			Object o = i.next();
			if (o == null) {
				sb.append("null");
			} else {
				sb.append(o.toString());
			}
			sb.append("\n");
		}
		if (_pw == null) {
			_pw = denv.getOutput();
		}
		
		_pw.print(sb.toString());
		return new Message();
	}
}
