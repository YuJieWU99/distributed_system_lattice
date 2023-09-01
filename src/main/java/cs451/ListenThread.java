package cs451;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ListenThread extends Thread{
    private final int hostId;
    ArrayList<Integer> hostPort = new ArrayList<>();
    ArrayList<Integer> senderPort = new ArrayList<>();
    Map<String, ArrayList<Integer>> ackSelf = new ConcurrentHashMap<>();

    public ListenThread(int hostId, ArrayList<Integer> hostPort, ArrayList<Integer> senderPort,  Map<String, ArrayList<Integer>> ackSelf) throws SocketException {
        this.hostPort = hostPort;
        this.hostId = hostId;
        this.senderPort = senderPort;
        this.ackSelf = ackSelf;
    }
    public void acknowledge(int hostId, int receiverId, ArrayList<Integer> messageSequence, ArrayList<Integer> hostPort) throws IOException {
        String messageInfo = messageSequence.stream().map(Object::toString).collect(Collectors.joining(","));
        String messageHost = "ack" +" "+ hostId +" "+messageInfo;
        byte[] messageToSend = messageHost.getBytes();
        DatagramSocket hostAck = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket packet = new DatagramPacket(messageToSend, messageToSend.length, address, (Integer) hostPort.get(receiverId-1));
        hostAck.send(packet);
        hostAck.close();
        /*
        for (int k = 0; k <messageSequence.size(); k++){
            String messageInfo = String.valueOf(messageSequence.get(k));
            String messageHost = "ack" +" "+ hostId +" "+messageInfo;
            byte[] messageToSend = messageHost.getBytes();
            DatagramSocket hostAck = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(messageToSend, messageToSend.length, address, (Integer) hostPort.get(receiverId-1));
            hostAck.send(packet);
            hostAck.close();
        }
*/
    }

    public void listen(int hostId, ArrayList<Integer> hostPort, Map<String, ArrayList<Integer>> ackSelf, long currentTimeMillis) throws IOException {

//        for (int i = 0; i < senderPort.size(); i++){
//            DatagramSocket sender = new DatagramSocket( (int) senderPort.get(i));
//            sender.setReuseAddress(true);
//            byte[] messageReceive = new byte[1024];
//            DatagramPacket packet = new DatagramPacket(messageReceive, messageReceive.length);
//            sender.receive(packet);
//            String delivered = new String(packet.getData(),"UTF-8");
//            delivered = delivered.trim();
//            sender.close();
//            if(!log.contains("d" + delivered)){
//                log.add("d" + delivered);
//                System.out.println(log.toString()+hostId);
//                System.out.println("d" + " " +hostPort.indexOf(senderPort.get(i)) + " "+ delivered);
//            }
//        }

        DatagramSocket sender = new DatagramSocket((int) hostPort.get(hostId - 1));
        sender.setReuseAddress(true);
        byte[] messageReceive = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageReceive, messageReceive.length);
        sender.receive(packet);
        String delivered = new String(packet.getData(), StandardCharsets.UTF_8);
        delivered = delivered.trim();
        String messageOne = delivered.split("\\s+")[0];
        String messageTwo = delivered.split("\\s+")[1];
        sender.close();
        //System.out.println(delivered);
//        if (delivered.contains("ack")) {
//            String messageThree = delivered.split("\\s+")[2];
//            Set<Integer> messageSplit = Arrays.stream(messageThree.split(",")).map(Integer::parseInt).collect(Collectors.toSet());//messageSplit is integer list
//            ackResend.putIfAbsent(messageTwo, new ArrayList<>());
//            ackResend.get(messageTwo).addAll(messageSplit);
//            System.out.println("ackResend is:");
//            System.out.println(ackResend.toString());
////            if (!ackResend.get(messageTwo).contains(Integer.valueOf(messageThree))) {
////                ackResend.get(messageTwo).add(Integer.valueOf(messageThree));
////                System.out.println("ackResend is:");
////                System.out.println(ackResend.toString());
////            }
//        } else {
        ackSelf.putIfAbsent(messageOne, new ArrayList<>());
        if (!ackSelf.get(messageOne).contains(Integer.valueOf(messageTwo))) {
            // deliver the message
            CreateFile.getInstance().addLogBuffer("d "+ delivered+"\n");
            ackSelf.get(messageOne).add(Integer.valueOf(messageTwo));

            // send back the acknowledgement
            acknowledge(hostId, Integer.parseInt(messageOne), ackSelf.get(messageOne), hostPort);
            //System.out.println(ackSelf.size());
            //System.out.println(ACK.toString());
        }
        int total = 0;
        Collection<ArrayList<Integer>> listOfValues = ackSelf.values();
        for (ArrayList<Integer> oneList : listOfValues) {
            total += oneList.size();
        }
        if (total == 11900){
            long usedTime = System.currentTimeMillis() - currentTimeMillis;
            System.out.println("used time: " + usedTime);
        }

        //long usedTime = System.currentTimeMillis() - currentTimeMillis;
       // System.out.println("used time: " + usedTime + " " + LocalDateTime.now());
        /*
            if(!ackBroadcast.contains(delivered)){
                CreateFile.getInstance().addLogBuffer("d "+ delivered+"\n");
                ackBroadcast.add(delivered);
                //ackTable.putIfAbsent(messageSender, new ArrayList<>());
                //ackTable.get(messageSender).add(Integer.valueOf(messageSequence));
                //logger.add(messageSequence);
                //System.out.println("d "+ delivered);
            }
            else{
                note.add("added before");
            }
        }*/
        /*
        if(logger.size()==messageNum){
            if(!ACK.contains("ack "+hostId)){
                ACK.add("ack "+hostId);
                //System.out.println(ACK.toString());
            }
            else{
                note.add("added before");
            }
        }
        else{
            note.add("Haven't reach");
        }
*/



        /*
        }
        if(messageType == "ack"){
            if (!log.contains(delivered)){
                log.add(delivered);
                System.out.println(log.toString());
            }
            else{
                System.out.println("added before");
            }
        }else{
            if(!logger.contains("d"+ messageSequence)){
                log.add("d "+ delivered);
                logger.add("d" + messageSequence);
                System.out.println(log.toString());
            }else{
                note.add(delivered);
            }
        }
*/
 //       }

    }

    public void run(){
        try {
            long currentTimeMillis = System.currentTimeMillis();
            while(true){
                //System.out.println("system is listening");

                listen(hostId, hostPort, ackSelf, currentTimeMillis);

            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

