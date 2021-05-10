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
package se.kth.ict.id2203.components.crb;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Start;
import se.sics.kompics.Handler;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.kth.ict.id2203.ports.crb.CausalOrderReliableBroadcast;
import se.kth.ict.id2203.ports.crb.CrbBroadcast;
import se.sics.kompics.Positive;
import se.sics.kompics.Negative;
import se.kth.ict.id2203.ports.rb.RbBroadcast;
import se.sics.kompics.address.Address;

public class WaitingCrb extends ComponentDefinition
{

	private static final Logger logger = LoggerFactory.getLogger(WaitingCrb.class);
	
	private Positive<ReliableBroadcast> rb = requires(ReliableBroadcast.class);
	private Negative<CausalOrderReliableBroadcast> corb = provides(CausalOrderReliableBroadcast.class);
	
	private final Address self;
	private Integer seqNum;
	private int[] vector;
	private final Set<Address> processes;
	private List<CrbDataMessage> pendingMessages;

	private Handler<Start> startHandler = new Handler<Start>()
	{
		@Override
		public void handle(Start event)
		{
			logger.info("crb created.");
		}
	};
	
	private Handler<CrbBroadcast> broadcastHandler = new Handler<CrbBroadcast>()
	{
		@Override
		public void handle(CrbBroadcast event)
		{
			int[] newVec = vector.clone();
			newVec[self.getId() - 1] = seqNum;
			seqNum++;
			CrbDataMessage msg = new CrbDataMessage(self, event.getDeliverEvent(), newVec);
			trigger(new RbBroadcast(msg), rb);
		}
	};
	
	private Handler<CrbDataMessage> crbDeliver = new Handler<CrbDataMessage>()
	{
		@Override
		public void handle(CrbDataMessage event)
		{
			pendingMessages.add(event);

			pendingMessages.sort((obj0, obj1) ->
			{
				if (Arrays.equals(obj0.getVector(), obj1.getVector()))
				{
					return obj0.getSource().getId() < obj1.getSource().getId() ? -1 : 1;
				}
				else if (allLessThanAll(obj0.getVector(), obj1.getVector()))
				{
					return -1;
				}
				else
				{
					return 1;
				}
			});

			CrbDataMessage tempMessage;
			Iterator<CrbDataMessage> pendingIter = pendingMessages.iterator();
			while (pendingIter.hasNext())
			{
				tempMessage = pendingIter.next();
				if (allLessThanAll(tempMessage.getVector(), vector))
				{
					pendingIter.remove();
					vector[tempMessage.getSource().getId() - 1] += 1;
					trigger(tempMessage.getDeliverData(), corb);
				}
				else
				{
					break;
				}
			}
		}
	};
	
	private boolean allLessThanAll(int[] vector1, int[] vector2)
	{
		boolean isLess = false;
		if (vector1.length == vector2.length)
		{
			for (int i = 0; i < vector1.length; i++)
			{
				if (vector1[i] <= vector2[i])
				{
					isLess = true;
				}
				else
				{
					isLess = false;
					break;
				}
			}
		}
			
		return isLess;
	}

	public WaitingCrb(WaitingCrbInit init)
	{
		this.self = init.getSelfAddress();
		this.processes = new HashSet<>(init.getAllAddresses());
		this.seqNum = 0;
		this.vector = new int[processes.size()];
		pendingMessages = new LinkedList<>();
		Arrays.fill(vector, 0);

		subscribe(startHandler, control);
		subscribe(broadcastHandler, corb);
		subscribe(crbDeliver, rb);
	}

}
