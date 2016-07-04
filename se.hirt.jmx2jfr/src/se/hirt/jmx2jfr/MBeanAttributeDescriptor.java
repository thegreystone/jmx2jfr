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
package se.hirt.jmx2jfr;

import javax.management.ObjectName;

import com.oracle.jrockit.jfr.ContentType;
/**
 * Describes an MBean attribute to capture, and which JFR ContentType it corresponds to.
 * 
 * @author Marcus Hirt
 */
@SuppressWarnings("deprecation")
public class MBeanAttributeDescriptor implements Comparable<MBeanAttributeDescriptor>{
	
	private final ObjectName objectName;
	private final String attributeName;
	private final ContentType contentType;

	public MBeanAttributeDescriptor(ObjectName objectName, String attributeName, ContentType contentType) {
		this.objectName = objectName;
		this.attributeName = attributeName;
		this.contentType = contentType;		
	}

	public ObjectName getObjectName() {
		return objectName;
	}

	public String getAttributeName() {
		return attributeName;
	}
	
	public ContentType getContentType() {
		return contentType;
	}

	
	public String toString() {
		return getObjectName().toString() + "#" + attributeName.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result + ((objectName == null) ? 0 : objectName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MBeanAttributeDescriptor other = (MBeanAttributeDescriptor) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		if (objectName == null) {
			if (other.objectName != null)
				return false;
		} else if (!objectName.equals(other.objectName))
			return false;
		return true;
	}

	@Override
	public int compareTo(MBeanAttributeDescriptor o) {
		if (o.getObjectName().equals(getObjectName())) {
			return o.getAttributeName().compareTo(getAttributeName());
		}
		return o.getObjectName().compareTo(getObjectName());
	}
}
