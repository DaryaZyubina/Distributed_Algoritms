/**
 * This file is part of the ID2203 course assignments kit.
 * 
 * Copyright (C) 2009-2013 KTH Royal Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.ict.id2203.components.beb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Set;
import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.sics.kompics.ComponentDefinition;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.Positive;
import se.sics.kompics.Negative;
import se.sics.kompics.Handler;

public class BasicBroadcast extends ComponentDefinition
{

	private static final Logger logger = LoggerFactory.getLogger(BasicBroadcast.class);
	
	private Positive<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);
	private Negative<BestEffortBroadcast> beb = provides(BestEffortBroadcast.class);
	
	private final Address self;
	private final Set<Address> processes;

	private Handler<Start> startHandler = new Handler<Start>()
	{
		@Override
		public void handle(Start event)
		{
			logger.info("beb created at process {}", self);
		}
	};

	private Handler<BebBroadcast> broadcastHandler = new Handler<BebBroadcast>()
	{
		@Override
		public void handle(BebBroadcast event)
		{
			for (Address process : processes)
			{
				BebDataMessage msg = new BebDataMessage(process, event.getDeliverEvent());
				trigger(new Pp2pSend(process, msg), pp2p);
			}
		}
	};

	private Handler<BebDataMessage> deliverHandler = new Handler<BebDataMessage>()
	{
		@Override
		public void handle(BebDataMessage event)
		{
			trigger(event.getData(), beb);
		}
	};
	
	public BasicBroadcast(BasicBroadcastInit init)
	{
		this.self = init.getSelfAddress();
		this.processes = new HashSet<>(init.getAllAddresses());
		
		subscribe(startHandler, control);
		subscribe(broadcastHandler, beb);
		subscribe(deliverHandler, pp2p);
	}
}
