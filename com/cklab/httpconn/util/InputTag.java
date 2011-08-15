/*******************************************************************************
 * Copyright (c) 2011 CK Lab.
 * 
 * This file is part of HTTPConn.
 * 
 * HTTPConn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * HTTPConn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with HTTPConn. If not, see <http://www.gnu.org/licenses/>.
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
	
}
