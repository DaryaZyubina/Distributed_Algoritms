package se.kth.ict.id2203.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.ports.epfd.EventuallyPerfectFailureDetector;
import se.kth.ict.id2203.ports.epfd.Restore;
import se.kth.ict.id2203.ports.epfd.Suspect;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.sics.kompics.*;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

import java.util.HashSet;

public class Epfd extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(Epfd.class);

    private Negative<EventuallyPerfectFailureDetector> epfd;
    private Positive<PerfectPointToPointLink> pp2pPos;

    private HashSet<Address> alive;
    private HashSet<Address> suspected;
    private HashSet<Address> allProcesses;

    private Address selfAddress;

    private Positive<Timer> timer = requires(Timer.class);
    private long delay;
    private final long deltaDelay;
    private int seqnum = 0;

    private Handler<CheckTimeout> handleCheck = new Handler<CheckTimeout>(){
        @Override
        public void handle(CheckTimeout checkTimeout) {
            HashSet<Address> intersection = new HashSet<>(alive);
            intersection.retainAll(suspected);
            if(!intersection.isEmpty()){
                delay += deltaDelay;
                logger.info("  delay is {}", delay);
            }
            seqnum++;
            logger.info("\t Seqnum is " + seqnum + " Alive " + alive.toString() + "  Suspected " + suspected.toString());
            for(Address process : allProcesses) {
                if(!alive.contains(process) && !suspected.contains(process)){
                    suspected.add(process);
                    trigger(new Suspect(process), epfd);
                }
                else if(alive.contains(process) && suspected.contains(process)){
                    suspected.remove(process);
                    trigger(new Restore(process), epfd);
                }
                trigger(new Pp2pSend(process, new HeartbeatRequestMessage(selfAddress, seqnum)), pp2pPos);
            }
            alive.clear();
            ScheduleTimeout st = new ScheduleTimeout(delay);
            st.setTimeoutEvent(new CheckTimeout(st));
            trigger(st, timer);
        }
    };

    private Handler<HeartbeatRequestMessage> handleRequest = new Handler<HeartbeatRequestMessage>(){
        @Override
        public void handle(HeartbeatRequestMessage heartbeatRequestMessage) {
            Address process = heartbeatRequestMessage.getSource();
            long delivSeqnum = heartbeatRequestMessage.getSeqnum();
            logger.info("heartbeat from " + process.toString());
            trigger(new Pp2pSend(process,
                    new HeartbeatReplyMessage(selfAddress, delivSeqnum)), pp2pPos);
        }
    };

    private Handler<HeartbeatReplyMessage> handleReply = new Handler<HeartbeatReplyMessage>(){
        @Override
        public void handle(HeartbeatReplyMessage heartbeatReplyMessage) {
            Address process = heartbeatReplyMessage.getSource();
            long sn = heartbeatReplyMessage.getSeqnum();
            if(sn == seqnum || suspected.contains(process)){
                alive.add(process);
                logger.info("alive " + process.toString());
            }
        }
    };

    private Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start start) {
            ScheduleTimeout st = new ScheduleTimeout(delay);
            st.setTimeoutEvent(new CheckTimeout(st));
            trigger(st, timer);
        }
    };

    public Epfd(EpfdInit init) {
        seqnum = 0;
        alive = new HashSet<>(init.getAllAddresses());
        allProcesses = new HashSet<>(init.getAllAddresses());
        selfAddress = init.getSelfAddress();
        alive.remove(selfAddress);
        allProcesses.remove(selfAddress);
        suspected = new HashSet<>();
        delay = init.getInitialDelay();
        deltaDelay = init.getDeltaDelay();
        epfd = provides(EventuallyPerfectFailureDetector.class);
        pp2pPos = requires(PerfectPointToPointLink.class);
        subscribe(handleStart, control);
        subscribe(handleCheck, timer);
        subscribe(handleRequest, pp2pPos);
        subscribe(handleReply, pp2pPos);
    }
}