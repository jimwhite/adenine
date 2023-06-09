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

package edu.mit.lcs.haystack.adenine.parser2.constructs;

import edu.mit.lcs.haystack.adenine.constructs.IReturnVisitor;
import edu.mit.lcs.haystack.adenine.parser2.IAttributeVisitor;
import edu.mit.lcs.haystack.adenine.parser2.IConstructParser;
import edu.mit.lcs.haystack.adenine.parser2.IConstructVisitor;
import edu.mit.lcs.haystack.adenine.parser2.IExpressionVisitor;
import edu.mit.lcs.haystack.adenine.parser2.NullAttributeVisitor;
import edu.mit.lcs.haystack.adenine.parser2.NullExpressionVisitor;
import edu.mit.lcs.haystack.adenine.parser2.ParserUtilities;
import edu.mit.lcs.haystack.adenine.parser2.SyntaxException;
import edu.mit.lcs.haystack.adenine.tokenizer.CommentToken;
import edu.mit.lcs.haystack.adenine.tokenizer.GenericToken;
import edu.mit.lcs.haystack.adenine.tokenizer.ITokenIterator;
import edu.mit.lcs.haystack.adenine.tokenizer.IndentToken;
import edu.mit.lcs.haystack.adenine.tokenizer.Location;
import edu.mit.lcs.haystack.adenine.tokenizer.NewLineToken;
import edu.mit.lcs.haystack.adenine.tokenizer.ResourceToken;
import edu.mit.lcs.haystack.adenine.tokenizer.SymbolToken;
import edu.mit.lcs.haystack.adenine.tokenizer.Token;
import edu.mit.lcs.haystack.adenine.tokenizer.WhitespaceToken;

/**
 * @author David Huynh
 */
public class ReturnParser implements IConstructParser {

	/* (non-Javadoc)
	 * @see edu.mit.lcs.haystack.adenine.parser2.IConstructParser#parseConstruct(edu.mit.lcs.haystack.adenine.tokenizer.ITokenIterator, edu.mit.lcs.haystack.adenine.parser2.Parser, edu.mit.lcs.haystack.adenine.parser2.IConstructVisitor)
	 */
	public Location parseConstruct(
		ITokenIterator 		tIterator,
		IConstructVisitor 	constructVisitor, IndentToken indentToken
	) {
		IReturnVisitor visitor = 
			(constructVisitor instanceof IReturnVisitor) ? 
			(IReturnVisitor) constructVisitor :
			new IReturnVisitor() {
				public IAttributeVisitor onAttribute(SymbolToken semicolonT) {
					return new NullAttributeVisitor(m_constructVisitor);
				}

				public void onReturn(GenericToken returnKeyword) {
				}

				public IExpressionVisitor onResult(Location location) {
					return new NullExpressionVisitor(m_constructVisitor);
				}
				
				public IExpressionVisitor onNamedResult(ResourceToken name, SymbolToken equalT) {
					return new NullExpressionVisitor(m_constructVisitor);
				}

				public void start(Location startLocation) {
					m_constructVisitor.start(startLocation);
				}

				public void end(Location endLocation) {
					m_constructVisitor.end(endLocation);
				}

				public void onException(Exception exception) {
					m_constructVisitor.onException(exception);
				}
				
				public IReturnVisitor init(IConstructVisitor constructVisitor) {
					m_constructVisitor = constructVisitor;
					return this;
				}
				
				IConstructVisitor m_constructVisitor;
			}.init(constructVisitor);

		visitor.start(tIterator.getLocation());

		Token 		token = tIterator.getToken();
		Location	endLocation = null;
		
		if (token instanceof GenericToken && ((GenericToken) token).getContent().equals("return")) {
			visitor.onReturn((GenericToken) token);
			
			tIterator.swallow();
			while (true) {
				ParserUtilities.skipWhitespacesAndComments(tIterator);
				
				endLocation = tIterator.getLocation();
			
				token = tIterator.getToken();
				if (token == null || token instanceof NewLineToken) {
					break;
				}
				
				if (token instanceof ResourceToken) {
					int 		i = 1;
					boolean 	foundNamedParameters = false;
				
					while (true) {
						Token token2 = tIterator.getToken(i);
					
						if (token2 instanceof WhitespaceToken || 
							token2 instanceof CommentToken) {
							i++;
							continue;
						}
					
						if (token2 instanceof SymbolToken && ((SymbolToken) token2).getSymbol().equals("=")) {
							foundNamedParameters = true;
						}
					
						break;
					}
				
					if (foundNamedParameters) {
						break;
					}
				}
				 
				if (!ParserUtilities.parseExpression(tIterator, visitor.onResult(tIterator.getLocation()), false)) {
					break;
				}
			}
			
			while (true) {
				ParserUtilities.skipWhitespacesAndComments(tIterator);
				
				endLocation = tIterator.getLocation();
		
				token = tIterator.getToken();
				if (token instanceof ResourceToken) {
					tIterator.swallow();
					ParserUtilities.skipWhitespacesAndComments(tIterator);
				
					Token token2 = tIterator.getToken();
					if (token2 instanceof SymbolToken) {
						SymbolToken symbolToken = (SymbolToken) token2;
						String		s = symbolToken.getSymbol();
					
						if (s.equals("=")) {
							tIterator.swallow();
							ParserUtilities.skipWhitespacesAndComments(tIterator);
						
							if (!ParserUtilities.parseExpression(tIterator, visitor.onNamedResult((ResourceToken) token, symbolToken), false)) {
								break;
							}
						}
					}
				} else {
					break;
				}
			}
			
			token = ParserUtilities.skipToNextLine(tIterator);
			if (token != null) {
				visitor.onException(new SyntaxException("Expected new line", token.getSpan()));
			}
		}

		if (endLocation == null) {
			endLocation = tIterator.getLocation();
		}

		visitor.end(endLocation);
		
		return endLocation;
	}
}
