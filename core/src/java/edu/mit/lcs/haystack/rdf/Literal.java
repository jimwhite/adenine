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

package edu.mit.lcs.haystack.rdf;

/**
 * Represents an RDF literal.
 * @author Dennis Quan
 */
public class Literal extends RDFNode {
	/**
	 * Constructs a Literal object with the specified content.
	 */
	public Literal(String content) {
		super(content);
	}
	
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (o.hashCode() != hashCode()) {
			return false;
		}
		return (o != null) && (o.getClass() == getClass()) && 
			((Literal)o).getContent().equals(getContent());
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(100);
		sb.append('\"');
		sb.append(content);
		sb.append('\"');
		return sb.toString();
	}
}

