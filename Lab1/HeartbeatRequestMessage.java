package se.kth.ict.id2203.components;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class HeartbeatRequestMessage extends Pp2pDeliver {
    private static final long serialVersionUID = 1877543957485623268L;
    private long seqnum;

    protected HeartbeatRequestMessage(Address source, long seqnum) {
        super(source);
        this.seqnum = seqnum;
    }

    public long getSeqnum() {
        return this.seqnum;
    }

    public void setSeqnum(long seqnum) {
        this.seqnum = seqnum;
    }
}
