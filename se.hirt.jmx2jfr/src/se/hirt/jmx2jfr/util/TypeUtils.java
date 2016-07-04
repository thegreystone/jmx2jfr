/*
 *  Copyright (C) 2016 Marcus Hirt
 *                     www.hirt.se
 *
 * This software is free:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESSED OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (C) Marcus Hirt, 2016
 */
package se.hirt.jmx2jfr.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.oracle.jrockit.jfr.ContentType;

import se.hirt.jmx2jfr.MBeanAttributeDescriptor;

/**
 * Some utilities.
 * 
 * @author Marcus Hirt
 */
@SuppressWarnings("deprecation")
public final class TypeUtils {
	private TypeUtils() {
		throw new UnsupportedOperationException("Toolkit!");
	}

	public static ObjectName toObjectName(String objectName) {
		try {
			return new ObjectName(objectName);
		} catch (MalformedObjectNameException e) {
			Logger.getLogger(MBeanAttributeDescriptor.class.getName()).log(Level.SEVERE, "Problem parsing object name!", e);
			return null;
		}
	}

	public static ContentType toContentType(String contentType) {
		return ContentType.valueOf(contentType);
	}

	// Resolves an MBean type to the type we want to use in the event
	public static Class<?> resolve(String type) {
		switch (type) {
		case "int":
		case "java.lang.Integer":
			return Integer.TYPE;

		case "long":
		case "java.lang.Long":
			return Long.TYPE;

		case "double":
		case "java.lang.Double":
			return Double.TYPE;

		case "float":
		case "java.lang.Float":
			return Float.TYPE;
		}
		return String.class;
	}
}
