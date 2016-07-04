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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;

/**
 * Sets up the executor responsible for running the SubscriptionCommad.
 * 
 * @author Marcus Hirt
 */
public class SubscriptionEngine {
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
	private final MBeanServerConnection connection;
	private final Registry registry;

	private class DaemonThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, "jmx2jfr subscription thread");
			thread.setDaemon(true);
			return thread;
		}
	}
	
	public SubscriptionEngine(MBeanServerConnection connection, Registry registry) {
		this.connection = connection;
		this.registry = registry;
		
	}

	public void start() {
		executor.scheduleAtFixedRate(new SubscriptionCommand(connection, registry), registry.getDelay(), registry.getPeriod(), TimeUnit.MILLISECONDS);
	}
	
	public void stop() {
		executor.shutdown();
	}
}
