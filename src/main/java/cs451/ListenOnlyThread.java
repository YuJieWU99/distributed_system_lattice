package cs451;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ListenOnlyThread extends Thread {
    private final int hostId;
    ArrayList<Integer> hostPort = new ArrayList<>();
    private int senderPort;
    Map<String, Set<Integer>> ackResend = new ConcurrentHashMap<>();
    private int wait;
    private int receiverId;
    Set<Integer> checkList = new TreeSet<>();


    public ListenOnlyThread(int wait, int receiverId, int hostId, ArrayList<Integer> hostPort, int senderPort, Map<String, Set<Integer>> ackResend, Set<Integer> checkList) throws SocketException {
        this.hostPort = hostPort;
        this.hostId = hostId;
        this.senderPort = senderPort;
        this.ackResend = ackResend;
        this.receiverId = receiverId;
        this.wait = wait;
        this.checkList = checkList;
    }
    //    public void acknowledge(int hostId, int receiverId, ArrayList<Integer> messageSequence, ArrayList<Integer> hostPort) throws IOException {
//        String messageInfo = messageSequence.stream().map(Object::toString).collect(Collectors.joining(","));
//        String messageHost = "ack" +" "+ hostId +" "+messageInfo;
//        byte[] messageToSend = messageHost.getBytes();
//        DatagramSocket hostAck = new DatagramSocket();
//        InetAddress address = InetAddress.getByName("localhost");
//        DatagramPacket packet = new DatagramPacket(messageToSend, messageToSend.length, address, (Integer) hostPort.get(receiverId-1));
//        hostAck.send(packet);
//        hostAck.close();
//    }
    public void resend(int wait, int hostId, int receiverId, int senderPort, Map<String, Set<Integer>> ackResend, Set<Integer> checkList) throws IOException, InterruptedException {
        String resendToHost = String.valueOf(receiverId);
        Set<Integer> resendListH = ackResend.get(resendToHost);
//        System.out.println("resend list");
//        System.out.println(resendListH);
//        System.out.println("removed checklist");

        if (resendListH != null){
            checkList.removeAll(resendListH);
        }
        // System.out.println("the checklist is");
        // System.out.println(checkList);
        // Set<Integer> checkTList = Stream.iterate(1, item -> item+1).limit(messageNum).collect(Collectors.toSet());
        // checkTList.removeAll(resendListH);
        // System.out.println(checkList);
        if (checkList.size() > 50){
            for (Integer a : checkList){
                String hostInfo = String.valueOf(hostId);
                String messageHost = hostInfo + " " + String.valueOf(a);
                byte[] messagesSend = messageHost.getBytes();
                DatagramSocket hostBroadcast = new DatagramSocket();
                InetAddress address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, senderPort);
                //System.out.println("resend"+a);
                hostBroadcast.send(packet); //System.out.println("b" + " " + messagesToSend);
                hostBroadcast.close();
                Thread.sleep(wait); //control the flow
                //Thread.sleep(30);
            }

        }else {
            for (Integer a : checkList){
                String hostInfo = String.valueOf(hostId);
                String messageHost = hostInfo + " " + String.valueOf(a);
                byte[] messagesSend = messageHost.getBytes();
                DatagramSocket hostBroadcast = new DatagramSocket();
                InetAddress address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, senderPort);
                //System.out.println("smaller than 50 resend"+a);
                hostBroadcast.send(packet); //System.out.println("b" + " " + messagesToSend);
                hostBroadcast.close();
            }
        }

    }
    public void listenOnly(int wait, Set<Integer> checkList, int receiverId, int senderPort, int hostId, ArrayList<Integer> hostPort, Map<String, Set<Integer>> ackResend) throws IOException, InterruptedException {
        /*
        for (int i = 0; i < senderPort.size(); i++){
            DatagramSocket sender = new DatagramSocket( (int) senderPort.get(i));
            sender.setReuseAddress(true);
            byte[] messageReceive = new byte[1024];
            DatagramPacket packet = new DatagramPacket(messageReceive, messageReceive.length);
            sender.receive(packet);
            String delivered = new String(packet.getData(),"UTF-8");
            delivered = delivered.trim();
            sender.close();
            if(!log.contains("d" + delivered)){
                log.add("d" + delivered);
                System.out.println(log.toString()+hostId);
                System.out.println("d" + " " +hostPort.indexOf(senderPort.get(i)) + " "+ delivered);
            }
        }
*/
        DatagramSocket sender = new DatagramSocket((int) hostPort.get(hostId - 1));
        sender.setReuseAddress(true);
        byte[] messageReceive = new byte[1024];
        DatagramPacket packet = new DatagramPacket(messageReceive, messageReceive.length);
        sender.receive(packet);
        String delivered = new String(packet.getData(), StandardCharsets.UTF_8);
        delivered = delivered.trim();
        String messageTwo = delivered.split("\\s+")[1];
        sender.close();
        String messageThree = delivered.split("\\s+")[2];
        Set<Integer> messageSplit = Arrays.stream(messageThree.split(",")).map(Integer::parseInt).collect(Collectors.toSet());//messageSplit is integer list
        ackResend.putIfAbsent(messageTwo, new TreeSet<>());
        ackResend.get(messageTwo).addAll(messageSplit);
        //resend(wait, hostId, receiverId, senderPort, ackResend, checkList);
//            if (!ackResend.get(messageTwo).contains(Integer.valueOf(messageThree))) {
//                ackResend.get(messageTwo).add(Integer.valueOf(messageThree));
//                System.out.println("ackResend is:");
//                System.out.println(ackResend.toString());
//            }


                //System.out.println(ACK.toString());
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


    }

    public void run(){
        try {
            while(true){
                //System.out.println("system is listening");
                listenOnly(wait, checkList, receiverId, senderPort, hostId, hostPort, ackResend);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e);
        }
    }
}

