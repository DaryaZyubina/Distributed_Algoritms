package se.kth.ict.id2203.components.rb;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import se.sics.kompics.Handler;

import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Start;
import se.kth.ict.id2203.ports.rb.RbBroadcast;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.sics.kompics.Negative;

public class EagerRb extends ComponentDefinition
{

	private static final Logger logger = LoggerFactory.getLogger(EagerRb.class);

	private Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
	private Negative<ReliableBroadcast> rb = provides(ReliableBroadcast.class);

	private final Address self;
	private Integer seqNum;
	private Set<RbDataMessage> deliveredMsgs;

	private Handler<Start> startHandler = new Handler<Start>()
	{
		@Override
		public void handle(Start event)
		{
			logger.info("rb created.");
		}
	};

	private Handler<RbBroadcast> rbBroadcastHandler = new Handler<RbBroadcast>()
	{
		@Override
		public void handle(RbBroadcast event)
		{
			seqNum++;
			RbDataMessage msg = new RbDataMessage(self, event.getDeliverEvent(), seqNum);
			trigger(new BebBroadcast(msg), beb);
		}

	};

	private Handler<RbDataMessage> rbDelivery = new Handler<RbDataMessage>()
	{
		@Override
		public void handle(RbDataMessage event)
		{
			if (!deliveredMsgs.contains(event))
			{
				deliveredMsgs.add(event);
				trigger(event.getData(), rb);
				trigger(new BebBroadcast(event), beb);
			}
		}
	};

	public EagerRb(EagerRbInit init)
	{
		this.self = init.getSelfAddress();
		this.seqNum = 0;
		this.deliveredMsgs = new HashSet<>();

		subscribe(startHandler, control);
		subscribe(rbBroadcastHandler, rb);
		subscribe(rbDelivery, beb);
	}
}
