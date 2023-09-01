package cs451.urb;
import cs451.CreateFile;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class FIFOListen extends Thread{
    private final int hostId;
    ArrayList<Integer> processPorts = new ArrayList<>();
    ArrayList<String> deliver = new ArrayList<>();
    Map<String, CopyOnWriteArraySet<Short>> relayList = new ConcurrentHashMap<>();
    Map<String, CopyOnWriteArraySet<Short>>  ack = new ConcurrentHashMap<>();


    public FIFOListen(int hostId, ArrayList<Integer> processPorts, ArrayList<String> deliver, Map<String, CopyOnWriteArraySet<Short>> relayList, Map<String, CopyOnWriteArraySet<Short>> ack){
        this.hostId = hostId;
        this.processPorts = processPorts;
        this.deliver = deliver;
        this.relayList = relayList;
        //this.ackSelf = ackSelf;
        this.ack = ack;
    }

    // ack sender your message 'messageAck' has arrived at my port, do not send me again
    public void acknowledgeRelay(int hostId, int fromPort, String messageAck) throws IOException {
        String messageHost = "ack" +" "+ hostId +" "+messageAck;
        byte[] messageToSend = messageHost.getBytes();
        DatagramSocket hostAck = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket packet = new DatagramPacket(messageToSend, messageToSend.length, address, fromPort);
        hostAck.send(packet);
        hostAck.close();
    }


    public void listen(int hostId, ArrayList<Integer> processPorts, ArrayList<String> deliver, Map<String, CopyOnWriteArraySet<Short>> relayList, Map<String, CopyOnWriteArraySet<Short>> ack, ArrayList<String> relayRecord) throws IOException {
        // listen to the port
        DatagramSocket listenFromPort = new DatagramSocket((int) processPorts.get(hostId - 1));
        listenFromPort.setReuseAddress(true);
        byte[] messageReceive = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageReceive, messageReceive.length);
        listenFromPort.receive(packet);
        String message = new String(packet.getData(), StandardCharsets.UTF_8);
        listenFromPort.close();
        message = message.trim();
        String messageType = message.split("\\s+")[0];
        // if the listened message contains b means someone is sending a message it received from others, or its own message
        if (messageType.contains("b")) {
            String messageSender = message.split("\\s+")[1];
            String messageSeqAndFrom = message.split("\\s+")[2];
            // add the senderID under the list of messageSequence.originalSender in "ack"
            if (!deliver.contains(messageSeqAndFrom)){
                ack.putIfAbsent(messageSeqAndFrom, new CopyOnWriteArraySet<>());
                if (message.split("\\s+").length ==4){
                    String acked = message.split("\\s+")[3];
                    //System.out.println(acked);
                    List<String> ackedList = Arrays.asList(acked.split(","));
                    Set<String> ackedSet = new HashSet<>(ackedList);
                    Set<Short> ackedSetIn = new TreeSet<>();
                    for (String s : ackedSet) {
                        ackedSetIn.add(Short.parseShort(s));
                    }
                    ack.get(messageSeqAndFrom).addAll(ackedSetIn);
                }
                ack.get(messageSeqAndFrom).add((short) hostId);
                ack.get(messageSeqAndFrom).add(Short.valueOf(messageSender));
                //System.out.println(ack);
                if (ack.get(messageSeqAndFrom).size() > processPorts.size()/2){
                    deliver.add(messageSeqAndFrom);
                    ack.remove(messageSeqAndFrom);
                }

            }
            acknowledgeRelay(hostId, processPorts.get(Integer.parseInt(messageSender) - 1), messageSeqAndFrom);
            if (!relayRecord.contains(messageSeqAndFrom)){
                relayList.putIfAbsent(messageSeqAndFrom, new CopyOnWriteArraySet<>());
                relayList.get(messageSeqAndFrom).add((short) hostId);
            }
            // if the received process of one messageSequence.originalSender exceeds N/2, add the message to "deliver" to wait to be delivered
            // reply to the one who send me the proof of receive that I know you received so that you will not send more relay message to me
        }
        // If I receive the message contains 'ack'
        if (messageType.contains("ack")) {
            String messageSender = message.split("\\s+")[1];
            String messageSeqAndFrom = message.split("\\s+")[2];
            // relayList.putIfAbsent(messageSeqAndFrom, new TreeSet<>());
            // add the one who receive my relay message next time I won't relay to him
            if (!relayRecord.contains(messageSeqAndFrom)){
                relayList.putIfAbsent(messageSeqAndFrom, new CopyOnWriteArraySet<>());
                relayList.get(messageSeqAndFrom).add(Short.valueOf(messageSender));
                if (relayList.get(messageSeqAndFrom).size() == processPorts.size()){
                    relayRecord.add(messageSeqAndFrom);
                    relayList.remove(messageSeqAndFrom);
                    //System.out.println(relayList);
                }
            }

        }
    }
    public void run(){
        try {
            // initialize for FIFO deliver
            ArrayList<String> relayRecord = new ArrayList<>();
            while(true){
                //System.out.println("system is listening");
                listen(hostId, processPorts, deliver, relayList, ack, relayRecord);
                //deliverMessage(hostId, processPorts, deliver, ack, deliverLog, delivered);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
