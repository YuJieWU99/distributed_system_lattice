package cs451.urb;

import cs451.CreateFile;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class FIFOBroadcast extends Thread{
    private final int hostId;
    private final int messageNum;
    ArrayList<Integer> processPorts = new ArrayList<>();
    Set<Short> receiverCheckList = new TreeSet<>();
    Map<String, CopyOnWriteArraySet<Short>> relayList = new ConcurrentHashMap<>();
    Map<String, CopyOnWriteArraySet<Short>> ack = new ConcurrentHashMap<>();

    public FIFOBroadcast(int hostId, int messageNum, ArrayList<Integer> processPorts, Set<Short> receiverCheckList, Map<String, CopyOnWriteArraySet<Short>> relayList,
    Map<String, CopyOnWriteArraySet<Short>> ack){
        this.hostId = hostId;
        this.messageNum = messageNum;
        this.processPorts = processPorts;
        this.receiverCheckList = receiverCheckList;
        this.relayList = relayList;
        this.ack = ack;
    }

    public void broadcast(int hostId, int messageSeq, ArrayList<Integer> processPorts, Map<String, CopyOnWriteArraySet<Short>> relayList, Map<String, CopyOnWriteArraySet<Short>> ack) throws IOException {
        // generate message to be sent "messageSequence.originalSender ID"
        String messageAck = String.valueOf(messageSeq) + "." + String.valueOf(hostId);
        // record the self-broadcast message in ack (how many other process received the message)
        // and relay list (record what messages that I have received from others I should relay to others)
        relayList.putIfAbsent(messageAck, new CopyOnWriteArraySet<>());
        relayList.get(messageAck).add((short) hostId);
        ack.putIfAbsent(messageAck, new CopyOnWriteArraySet<>());
        ack.get(messageAck).add((short) hostId);
        // broadcast the message to all process (including myself)
        // all broadcast format is "b senderID messageSequence.originalSender messageSequence.originalSender's receivers
        String message = "b" + " " + String.valueOf(hostId) + " " + messageAck;
        if (ack.containsKey(messageAck)){
            String messageInfo = ack.get(messageAck).stream().map(Object::toString).collect(Collectors.joining(","));
            message = "b" + " " + String.valueOf(hostId) + " " + messageAck + " " + messageInfo;
        }
        byte[] messageBroadcast = message.getBytes();
        DatagramSocket hostBroadcast = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        for (Integer processPort : processPorts) {
            DatagramPacket packet = new DatagramPacket(messageBroadcast, messageBroadcast.length, address, processPort);
            hostBroadcast.send(packet);
            //hostBroadcast.close();
        }
        // log b messageSequence
        CreateFile.getInstance().addLogBuffer("b "+messageSeq+"\n");
    }

    public void relayMessage(int hostId, ArrayList<Integer> processPorts, Set<Short> receiverCheckList, Map<String, CopyOnWriteArraySet<Short>> relayList, Map<String, CopyOnWriteArraySet<Short>> ack) throws IOException {
        // relay the message I received to the processes according to "relayList" to let them know I have received the message
        for (String messageSeqAndFrom : relayList.keySet()){
            Set<Short> receiveList = relayList.get(messageSeqAndFrom);
            String messageInfo = null;
            Set<Short> toSendSet;
            if (receiveList != null) {
                toSendSet = receiverCheckList.stream().filter(a -> !receiveList.contains(a)).collect(Collectors.toSet());
            } else {
                toSendSet = Collections.emptySet();
            }
            for (Short process : toSendSet){
                String messageHost = "b" + " " + String.valueOf(hostId) + " " + messageSeqAndFrom;

                if (ack.containsKey(messageSeqAndFrom)){
                    List<Short> collect = new ArrayList<>(ack.get(messageSeqAndFrom));
                    messageInfo = collect.stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));
//                    messageInfo = ack.get(messageSeqAndFrom).stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));
                    messageHost = "b" + " " + String.valueOf(hostId) + " " + messageSeqAndFrom + " " + messageInfo;
                }
                byte[] messagesSend = messageHost.getBytes();
                DatagramSocket hostBroadcast = new DatagramSocket();
                InetAddress address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, processPorts.get(process - 1));
                //System.out.println("resend"+a);
                hostBroadcast.send(packet); //System.out.println("b" + " " + messagesToSend);
                //System.out.println(messageHost);
                hostBroadcast.close();
            }
        }
    }

    @Override
    public void run() {
        for (int i = 1; i < messageNum + 1; i++) {
            try {
                broadcast(hostId, i, processPorts, relayList, ack);
                // log.add("b " + messageSend);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        while(true) {
            try {
                relayMessage(hostId, processPorts, receiverCheckList, relayList, ack);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

}
