/*******************************************************************************
 * Copyright (C) 2011 CKLab
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.cklab.httpconn.util;
/**
 * FormData class.
 * 
 * Used primarily for specifying Post Data for a form in an HTTP Post request (the Post class).
 * 
 * @author cklab
 *
 */
public class FormData {

	private String name, value;
	
	/**
	 * Construct a FormData with the given name and value
	 * @param name the name
	 * @param value the value
	 */
	public FormData(String name, String value)
	{
		if (name == null) return;
		if (value == null) return;
		
		this.name = name.trim();
		this.value = value.trim();
	}
	
	/**
	 * Get the name.
	 * @return the name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Get the value.
	 * @return the value.
	 */
	public String getValue()
	{
		return value;
	}

	
	/**
	 * Determine whether or not this FormData is valid.
	 * @return true if either the name or value field is null; false otherwise.
	 */
	public boolean invalid()
	{
		if (name == null || value == null)
			return true;
		return false;
	}
	
	
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof FormData)) return false;
		FormData fd = (FormData)o;
		
		return fd.getValue().equals(getValue());
	}
	public String toString()
	{
		return getName()+": "+getValue();
	}
}
