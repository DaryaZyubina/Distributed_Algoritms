package se.kth.ict.id2203.components.crb;

import se.kth.ict.id2203.ports.crb.CrbDeliver;
import se.kth.ict.id2203.ports.rb.RbDeliver;
import se.sics.kompics.address.Address;

public class CrbDataMessage extends RbDeliver
{

	private static final long serialVersionUID = -34913265978484567L;

	private int[] vector;
	private CrbDeliver deliverData;

	public CrbDataMessage(Address source, CrbDeliver deliverData, int[] vector)
	{
		super(source);
		this.vector = vector;
		this.deliverData = deliverData;
	}

	public int[] getVector()
	{
		return vector;
	}

	public CrbDeliver getDeliverData()
	{
		return deliverData;
	}
}
