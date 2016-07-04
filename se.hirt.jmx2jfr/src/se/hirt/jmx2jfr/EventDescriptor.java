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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.oracle.jrockit.jfr.DynamicEventToken;
import com.oracle.jrockit.jfr.DynamicValue;
import com.oracle.jrockit.jfr.InvalidEventDefinitionException;
import com.oracle.jrockit.jfr.InvalidValueException;
import com.oracle.jrockit.jfr.Producer;

import se.hirt.jmx2jfr.Registry.NameGenerationMode;
import se.hirt.jmx2jfr.util.TypeUtils;

/**
 * Contains the information required to emit events.
 * 
 * @author Marcus Hirt
 */
@SuppressWarnings("deprecation")
public class EventDescriptor {
	private final static String[] CLEAN_KEY_NAME_ORDER = { "name", "Name", "type", "Type" };
	private final static Producer PRODUCER;

	private final DynamicEventToken eventToken;
	private final ObjectName mbean;
	private final String[] attributeNames;

	static {
		PRODUCER = createProducer();
		PRODUCER.register();
	}

	public EventDescriptor(Registry.NameGenerationMode mode, ObjectName mbean,
			SortedMap<MBeanAttributeDescriptor, MBeanAttributeInfo> attributes)
			throws InvalidEventDefinitionException, InvalidValueException {
		this.mbean = mbean;
		this.eventToken = generateEventToken(mode, mbean, attributes);
		this.attributeNames = getNames(attributes);
	}

	public static EventDescriptor create(Registry.NameGenerationMode mode, MBeanServerConnection connection, ObjectName mbean,
			List<MBeanAttributeDescriptor> attributes) throws Exception {
		MBeanInfo mBeanInfo = connection.getMBeanInfo(mbean);
		MBeanAttributeInfo[] attributesInfos = mBeanInfo.getAttributes();
		SortedMap<MBeanAttributeDescriptor, MBeanAttributeInfo> mbeanAttributeInfos = new TreeMap<>();

		for (MBeanAttributeDescriptor attributeDescriptor : attributes) {
			MBeanAttributeInfo info = findInfo(attributeDescriptor, attributesInfos);
			if (info != null) {
				mbeanAttributeInfos.put(attributeDescriptor, info);
			}
		}
		return new EventDescriptor(mode, mbean, mbeanAttributeInfos);
	}

	public ObjectName getMBean() {
		return mbean;
	}

	public String[] getAttributeNames() {
		return attributeNames;
	}

	public DynamicEventToken getEventToken() {
		return eventToken;
	}

	private static String[] getNames(SortedMap<MBeanAttributeDescriptor, MBeanAttributeInfo> attributes) {
		Set<MBeanAttributeDescriptor> descriptors = attributes.keySet();
		String[] names = new String[descriptors.size()];
		int i = 0;
		for (MBeanAttributeDescriptor descriptor : descriptors) {
			names[i++] = descriptor.getAttributeName();
		}
		return names;
	}

	private static MBeanAttributeInfo findInfo(MBeanAttributeDescriptor attributeDescriptor, MBeanAttributeInfo[] attributesInfos) {
		// Linear search fine - always a relatively small amount of attributes
		for (MBeanAttributeInfo info : attributesInfos) {
			if (info.getName().equals(attributeDescriptor.getAttributeName())) {
				return info;
			}
		}
		return null;
	}

	private static DynamicEventToken generateEventToken(Registry.NameGenerationMode mode, ObjectName mbean,
			Map<MBeanAttributeDescriptor, MBeanAttributeInfo> attributes) throws InvalidEventDefinitionException, InvalidValueException {
		return PRODUCER.createDynamicInstantEvent(generateEventName(mode, mbean), generateDescription(mbean), generatePath(mode, mbean),
				false, false, generateValueDescriptions(attributes));
	}

	private static DynamicValue[] generateValueDescriptions(Map<MBeanAttributeDescriptor, MBeanAttributeInfo> attributes)
			throws InvalidValueException {
		List<DynamicValue> valueDescriptions = new ArrayList<>();
		for (Entry<MBeanAttributeDescriptor, MBeanAttributeInfo> entry : attributes.entrySet()) {
			valueDescriptions.add(createValueDescriptor(entry.getKey(), entry.getValue()));
		}
		return valueDescriptions.toArray(new DynamicValue[0]);
	}

	private static DynamicValue createValueDescriptor(MBeanAttributeDescriptor attribute, MBeanAttributeInfo info)
			throws InvalidValueException {
		return new DynamicValue(attribute.getAttributeName(), attribute.getAttributeName(), info.getDescription(),
				attribute.getContentType(), TypeUtils.resolve(info.getType()));
	}

	private static String generatePath(Registry.NameGenerationMode mode, ObjectName mbean) {
		return "MBeans" + "/" + mbean.getDomain().replace('.', '_') + "/" + generateEventName(mode, mbean);
	}

	private static String generateDescription(ObjectName mbean) {
		return "This event class represents the subscribed values from the " + mbean.getCanonicalName() + " mbean";
	}

	private static String generateEventName(Registry.NameGenerationMode mode, ObjectName mbean) {
		if (mode == NameGenerationMode.clean) {
			for (String key : CLEAN_KEY_NAME_ORDER) {
				String val = mbean.getKeyProperty(key);
				if (val != null) {
					return val;
				}
			}
		}
		String canonicalName = mbean.getKeyPropertyListString();
		return canonicalName.replace(':', '_').replace(',', '_');
	}

	@Override
	public int hashCode() {
		return mbean.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventDescriptor other = (EventDescriptor) obj;
		if (mbean == null) {
			if (other.mbean != null)
				return false;
		} else if (!mbean.equals(other.mbean))
			return false;
		return true;
	}

	private static Producer createProducer() {
		try {
			return new Producer("Dynamic MBean Attribute Producer", "This producer records MBean attribute values into the Flight Recorder",
					"http://www.hirt.se/jfr/jmx2jfr");
		} catch (URISyntaxException e) {
			// This cannot happen - the URI is fine...
			e.printStackTrace();
		}
		return null;
	}
}
