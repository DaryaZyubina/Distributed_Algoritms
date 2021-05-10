package se.kth.ict.id2203.components.rb;

import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.kth.ict.id2203.ports.rb.RbDeliver;
import se.sics.kompics.address.Address;

public class RbDataMessage extends BebDeliver
{

	private static final long serialVersionUID = 202127777779651547L;

	private final RbDeliver data;
	private final Integer seqNum;
	private final Address superSource;

	public RbDataMessage(Address source, RbDeliver data, Integer seqNum)
	{
		super(source);
		this.data = data;
		this.seqNum = seqNum;
		this.superSource = super.getSource();
	}
	
	public RbDeliver getData()
	{
		return data;
	}

	public Integer getSeqNum()
	{
		return seqNum;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean isEqual = false;

		if (obj instanceof RbDataMessage)
		{
			RbDataMessage castedObj = (RbDataMessage) obj;
			isEqual = (this.seqNum.equals((castedObj).getSeqNum())
					&& superSource.equals((castedObj).getSource()));
		}
		
		return isEqual;
	}
	
	@Override
	public int hashCode()
	{
		return (this.seqNum * superSource.hashCode());
	}
}
