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
 * InputTag class.
 * 
 * Class to specify the HTTP input tag.
 * 
 * @author cklab
 *
 */
public class InputTag {

	private String m_name, m_value;
	private String m_type;
	
	/**
	 * Construct an HTTP input tag with the give name, value, and type.
	 * @param name the name field of the tag
	 * @param value the value field of the tag
	 * @param type the type field of the tag
	 */
	public InputTag(String name, String value, String type)
	{
		m_name = name;
		m_value = value;
		m_type = type;
	}

	/**
	 * Get the name field of the tag.
	 * @return the name field of the tag.
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Get the value field of the tag.
	 * @return he value field of the tag.
	 */
	public String getValue() {
		return m_value;
	}

	/**
	 * Get the type field of the tag.
	 * @return the type field of the tag.
	 */
	public String getType() {
		return m_type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InputTag [m_name=").append(m_name).append(", m_value=")
				.append(m_value).append(", m_type=").append(m_type).append("]");
		return builder.toString();
	}
	
	
	
}
