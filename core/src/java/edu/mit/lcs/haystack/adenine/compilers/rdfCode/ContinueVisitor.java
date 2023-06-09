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

package edu.mit.lcs.haystack.adenine.compilers.rdfCode;

import edu.mit.lcs.haystack.adenine.AdenineConstants;
import edu.mit.lcs.haystack.adenine.constructs.IContinueVisitor;
import edu.mit.lcs.haystack.adenine.tokenizer.Token;

import java.util.List;

/**
 * @author David Huynh
 */
public class ContinueVisitor
	extends ConstructVisitorBase
	implements IContinueVisitor {

	public ContinueVisitor(TopLevelVisitor visitor, List instructionList) {
		super(visitor, instructionList);
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.constructs.IContinueVisitor#onContinue(edu.mit.lcs.haystack.adenine.tokenizer.Token)
	 */
	public void onContinue(Token continueKeyword) {
		m_topLevelVisitor.makeInstruction(m_instructionResource, AdenineConstants.Continue, continueKeyword.getSpan().getStart());
	}

}
