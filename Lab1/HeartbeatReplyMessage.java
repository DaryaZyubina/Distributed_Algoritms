package se.kth.ict.id2203.components;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class HeartbeatReplyMessage extends Pp2pDeliver {
    private static final long serialVersionUID = 37139497845896124L;
    private long seqnum;

    protected HeartbeatReplyMessage(Address source, long seqnum) {
        super(source);
        this.seqnum = seqnum;
    }

    public long getSeqnum() {
        return seqnum;
    }

    public void setSeqnum(long seqnum) {
        this.seqnum = seqnum;
    }
}