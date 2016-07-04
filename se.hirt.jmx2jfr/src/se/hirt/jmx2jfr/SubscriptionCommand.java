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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.oracle.jrockit.jfr.InstantEvent;

/**
 * Implements the logic to retrieve the data from the mbeans and emit the
 * events. Used by a single threaded executor.
 * 
 * @author Marcus Hirt
 */
@SuppressWarnings("deprecation")
public class SubscriptionCommand implements Runnable {
	private final Registry registry;
	private final List<EventDescriptor> events = new ArrayList<EventDescriptor>();
	private final MBeanServerConnection connection;
	private final List<ObjectName> failList = new ArrayList<>();
	private long lastRetry = System.currentTimeMillis();
	private boolean isInitialized = false;

	public SubscriptionCommand(MBeanServerConnection connection, Registry registry) {
		this.connection = connection;
		this.registry = registry;
	}

	private void setUpEvents() {
		for (Entry<ObjectName, List<MBeanAttributeDescriptor>> entry : registry.getAttributes().entrySet()) {
			try {
				events.add(EventDescriptor.create(registry.getNamingMode(), connection, entry.getKey(), entry.getValue()));
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Added attribute to the fail list. Will retry later.", e);
				failList.add(entry.getKey());
			}
		}
	}

	@Override
	public void run() {
		if (!isInitialized) {
			// FIXME(marcus/24 mars 2016): Used to set this up on
			// SubscriptionCommand construction, but doing dynamic JFR events
			// in premain turned out to be a very bad idea. Now using lazy set up.
			setUpEvents();
			isInitialized = true;
		}
		if (!failList.isEmpty() && isTimeToRetry()) {
			retry();
		}
		for (EventDescriptor descriptor : events) {
			try {
				AttributeList attributes = connection.getAttributes(descriptor.getMBean(), descriptor.getAttributeNames());
				emitEvent(descriptor, attributes);
			} catch (InstanceNotFoundException | ReflectionException | IOException e) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Failed to emit event from jmx2jfr!", e);
			}
		}
	}

	private void retry() {
		List<ObjectName> failListCopy = new ArrayList<ObjectName>(failList);
		for (ObjectName name : failListCopy) {
			try {
				events.add(EventDescriptor.create(registry.getNamingMode(), connection, name, registry.getAttributes().get(name)));
			} catch (Exception e) {
				continue;
			}
			failList.remove(name);
		}
	}

	private boolean isTimeToRetry() {
		return System.currentTimeMillis() - lastRetry > registry.getRetryTime();
	}

	private void emitEvent(EventDescriptor descriptor, AttributeList attributes) {
		InstantEvent event = descriptor.getEventToken().newInstantEvent();
		for (Attribute attribute : attributes.asList()) {
			event.setValue(attribute.getName(), attribute.getValue());
		}
		event.commit();
	}
}
