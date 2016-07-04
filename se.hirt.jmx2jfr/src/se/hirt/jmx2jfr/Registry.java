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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import se.hirt.jmx2jfr.util.TypeUtils;

/**
 * The Registry is responsible for parsing the settings file.
 * 
 * @author Marcus Hirt
 */
public class Registry {
	private static final String XML_ELEMENT_ATTRIBUTE = "attribute";
	private static final long DEFAULT_PERIOD = 5000;
	private static final long DEFAULT_DELAY = 0;
	private static final long DEFAULT_RETRYTIME = 20000;
	
	private final Map<ObjectName, List<MBeanAttributeDescriptor>> attributes = new HashMap<>();
	private long delay = DEFAULT_DELAY;
	private long period = DEFAULT_PERIOD;
	private long retryTime = DEFAULT_RETRYTIME;
	private NameGenerationMode namingMode = NameGenerationMode.clean;
	
	public static enum NameGenerationMode {
		clean, canonical
	}
		
	public static Registry from(InputStream in) throws XMLStreamException  {
		Registry registry = new Registry();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
		
		Map<String, String> values = new HashMap<String, String>();
		while (streamReader.hasNext()) {
			if (streamReader.isStartElement()) {
				QName element = streamReader.getName();
				if (XML_ELEMENT_ATTRIBUTE.equals(element.getLocalPart())) {
					MBeanAttributeDescriptor mbad = parseAttribute(streamReader);
					registry.add(mbad);
					continue;
				}
				streamReader.next();
				if (streamReader.hasText()) {
					String value = streamReader.getText();
					values.put(element.getLocalPart(), value);
				}
			}
			streamReader.next();
		}
		registry.setFields(values);
		return registry;

	}

	private void setFields(Map<String, String> values) {
		delay = Long.parseLong(values.get("delay"));
		period = Long.parseLong(values.get("period"));
		retryTime = Long.parseLong(values.get("retrytime"));
		namingMode = NameGenerationMode.valueOf(values.get("namingmode"));
	}

	private void add(MBeanAttributeDescriptor mbad) {
		List<MBeanAttributeDescriptor> attributeList = attributes.get(mbad.getObjectName());
		if (attributeList == null) {
			attributeList = new ArrayList<>();
			attributes.put(mbad.getObjectName(), attributeList);
		}
		attributeList.add(mbad);
	}

	private static MBeanAttributeDescriptor parseAttribute(XMLStreamReader streamReader) throws XMLStreamException {		
		streamReader.next();
		String attributeName = null;
		String objectName = null;
		String contentType = null;
		while (streamReader.hasNext()) {
			if (streamReader.isStartElement()) {
				String key = streamReader.getName().getLocalPart();
				streamReader.next();
				if (streamReader.hasText()) {
					String value = streamReader.getText();
					if ("attributename".equals(key)) {
						attributeName = value;
					} else if ("objectname".equals(key)) {
						objectName = value;
					} else if ("contenttype".equals(key)) {
						contentType = value;
					}
				}
			} else if (streamReader.isEndElement()) {
				if (XML_ELEMENT_ATTRIBUTE.equals(streamReader.getName().getLocalPart())) {
					break;
				}
			}
			streamReader.next();
		}
		return new MBeanAttributeDescriptor(TypeUtils.toObjectName(objectName), attributeName, TypeUtils.toContentType(contentType));
	}
	
	public Map<ObjectName, List<MBeanAttributeDescriptor>> getAttributes() {
		return attributes;
	}

	public long getDelay() {
		return delay;
	}

	public long getPeriod() {
		return period;
	}
	
	public long getRetryTime() {
		return retryTime;
	}
	
	public NameGenerationMode getNamingMode() {
		return namingMode;
	}

}
