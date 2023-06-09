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

import edu.mit.lcs.haystack.adenine.AdenineConstants;
import edu.mit.lcs.haystack.adenine.AdenineException;
import edu.mit.lcs.haystack.adenine.interpreter.CompiledMethod;
import edu.mit.lcs.haystack.adenine.interpreter.Message;
import edu.mit.lcs.haystack.rdf.IRDFContainer;
import edu.mit.lcs.haystack.rdf.ListUtilities;
import edu.mit.lcs.haystack.rdf.RDFException;
import edu.mit.lcs.haystack.rdf.Resource;
import edu.mit.lcs.haystack.rdf.Utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author David Huynh
 */
public class AnonymousCompiledMethod extends CompiledMethod {
	Resource	m_wrappedMethod;
	Message		m_message;
	
	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.interpreter.CompiledMethod#doInvoke()
	 */
	protected Message doInvoke() throws AdenineException, RDFException {
		return __interpreter__.callMethod(m_wrappedMethod, m_message, __dynamicenvironment__);
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.interpreter.CompiledMethod#initializeParameters(edu.mit.lcs.haystack.adenine.interpreter.Message)
	 */
	protected void initializeParameters(Message msg) {
		IRDFContainer source = __dynamicenvironment__.getSource();
		
		m_wrappedMethod = Utilities.getResourceProperty(__methodresource__, AdenineConstants.function, source);
		
		List		orderedParameters = new ArrayList();
		Resource 	backquotedValueList = Utilities.getResourceProperty(__methodresource__, AdenineConstants.BACKQUOTED_PARAMETERS, source);
		
		if (backquotedValueList != null) {
			Iterator i = ListUtilities.accessDAMLList(backquotedValueList, source);
			
			while (i.hasNext()) {
				orderedParameters.add(i.next());
			}
		}
		
		Object[] values = msg.getOrderedValues();
		for (int i = 0; i < values.length; i++) {
			orderedParameters.add(values[i]);
		}
		
		m_message = new Message(orderedParameters, msg.getNamedValues());
	}
}
