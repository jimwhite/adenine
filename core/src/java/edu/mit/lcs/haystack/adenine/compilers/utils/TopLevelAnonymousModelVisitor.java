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

package edu.mit.lcs.haystack.adenine.compilers.utils;

import edu.mit.lcs.haystack.adenine.parser2.IAnonymousModelVisitor;
import edu.mit.lcs.haystack.adenine.parser2.IAttributeVisitor;
import edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken;
import edu.mit.lcs.haystack.rdf.Resource;

/**
 * @author David Huynh
 */
public class TopLevelAnonymousModelVisitor extends ParserVisitorBase implements IAnonymousModelVisitor {
	TopLevelVisitorBase m_topLevelVisitor;
	Resource			m_anonymousResource;

	public TopLevelAnonymousModelVisitor(TopLevelVisitorBase topLevelVisitor) {
		super(topLevelVisitor.getChainedVisitor());
		m_topLevelVisitor = topLevelVisitor;
		m_anonymousResource = m_topLevelVisitor.getURIGenerator().generateAnonymousResource();
	}
	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.parser2.IAnonymousModelVisitor#onAttribute(edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken)
	 */
	public IAttributeVisitor onAttribute(SymbolToken semicolonT) {
		return new TopLevelAttributeVisitor(m_topLevelVisitor, m_anonymousResource);
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.parser2.IAnonymousModelVisitor#onModelEnd(edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken)
	 */
	public void onModelEnd(SymbolToken rightBraceT) {
	}

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.parser2.IAnonymousModelVisitor#onModelStart(edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken, edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken)
	 */
	public void onModelStart(SymbolToken dollarSignT, SymbolToken leftBraceT) {
	}

	public Resource getAnonymousResource() {
		return m_anonymousResource;
	}
}
