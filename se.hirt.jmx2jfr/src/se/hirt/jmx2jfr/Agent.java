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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

/**
 * Agent which records MBean attributes as JFR events.
 * 
 * @author Marcus Hirt
 */
public class Agent {
	private final static String DEFAULT_CONFIG = "jmxprobes.xml";

	public static void premain(String agentArguments, Instrumentation instrumentation) {
		if (!isJFRAvailable()) {
			Logger.getLogger(Agent.class.getName()).log(Level.SEVERE,
					"This agent only works together with a HotSpot JDK 7 or 8 (possibly also JRockit JDK 6). A version which transparently works with JDK 9 will be released after the release of JDK 9.");
		}

		if (agentArguments == null || agentArguments.trim().length() == 0) {
			agentArguments = DEFAULT_CONFIG;
		}

		File file = new File(agentArguments);
		try {
			InputStream stream = new FileInputStream(file);
			SubscriptionEngine engine = new SubscriptionEngine(ManagementFactory.getPlatformMBeanServer(), Registry.from(stream));
			engine.start();
		} catch (FileNotFoundException | XMLStreamException e) {
			Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, "Failed to read jfr probe definitions from " + file.getPath(), e);
		}
	}

	private static boolean isJFRAvailable() {
		try {
			Class.forName("com.oracle.jrockit.jfr.InstantEvent");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}
}
