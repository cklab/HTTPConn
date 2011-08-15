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
