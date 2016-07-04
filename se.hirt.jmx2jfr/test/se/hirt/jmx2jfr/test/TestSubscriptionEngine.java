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
package se.hirt.jmx2jfr.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import se.hirt.jmx2jfr.Agent;
import se.hirt.jmx2jfr.Registry;
import se.hirt.jmx2jfr.SubscriptionCommand;
import se.hirt.jmx2jfr.SubscriptionEngine;

/**
 * Yeah, well, it's a hack, don't expect much testing.
 * 
 * @author Marcus Hirt
 */
public class TestSubscriptionEngine {
	//@Test - can only run one of these at a time - bloody JFR producers cannot unregister events :/
	public void testRunSubscriptionCommand() throws FileNotFoundException, XMLStreamException {
		Registry registry = Registry.from(Agent.class.getResourceAsStream("jmxprobes.xml"));
		assertNotNull(registry);		
		assertTrue(registry.getAttributes().size() > 0);
		assertTrue(registry.getPeriod() >= 1000);
		assertTrue(registry.getDelay() > 0);
		
		SubscriptionCommand command = new SubscriptionCommand(ManagementFactory.getPlatformMBeanServer(), registry);
		command.run();
	}
	
	@Test
	public void testRunSubscriptionEngine() throws XMLStreamException, InterruptedException {
		Registry registry = Registry.from(Agent.class.getResourceAsStream("jmxprobes.xml"));
		assertNotNull(registry);		
		assertTrue(registry.getAttributes().size() > 0);
		assertTrue(registry.getPeriod() >= 1000);
		assertTrue(registry.getDelay() > 0);
		
		SubscriptionEngine engine = new SubscriptionEngine(ManagementFactory.getPlatformMBeanServer(), registry);
		engine.start();
	
		// Sleep for a while to allow me to hook up and do a recording... 
		// erhm... I mean... to run the engine and notice any failures.
		Thread.sleep(100000);
	}
}
