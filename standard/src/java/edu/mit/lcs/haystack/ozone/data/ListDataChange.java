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

package edu.mit.lcs.haystack.ozone.data;

import java.util.List;

/**
 * @author David Huynh
 * 
 * Encapsulates some change to the data inside a list data provider.
 */
public class ListDataChange {
	public int	m_index;
	public int	m_count;
	public List	m_elements;
	public List 	m_indices;
	
	public ListDataChange(int index, int count, List elements) {
		m_index = index;
		m_count = count;
		m_elements = elements;
	}

	public ListDataChange(List indices) {
		m_indices = indices;
	}
	
	public ListDataChange(List indices, List elements) {
		m_indices = indices;
		m_elements = elements;
	}
	
	public String toString() {
		return m_elements + " at index " + m_index + " with count " + m_count;
	}
}