package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class BroadcastOnlyThread extends Thread{
    private final int hostId;
    ArrayList<Integer> hostPort = new ArrayList<>();
    private final int senderPort;
    private final int messageNum;
    private final int receiverId;
    Map<String, Set<Integer>> ackResend = new ConcurrentHashMap<>();
    Set<Integer> checkList = new TreeSet<>();
    private final int wait;

    public BroadcastOnlyThread(int wait, int hostId, ArrayList<Integer> hostPort,int receiverId, int senderPort, int messageNum, Map<String, Set<Integer>> ackResend, Set<Integer> checkList) throws SocketException {
        this.hostId = hostId;
        this.hostPort = hostPort;
        this.messageNum = messageNum;
        this.senderPort = senderPort;
        this.ackResend = ackResend;
        this.checkList = checkList;
        this.receiverId = receiverId;
        this.wait = wait;
    }
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
//        for (Integer a : checkList){
//            String hostInfo = String.valueOf(hostId);
//            String messageHost = hostInfo + " " + String.valueOf(a);
//            byte[] messagesSend = messageHost.getBytes();
//            DatagramSocket hostBroadcast = new DatagramSocket();
//            InetAddress address = InetAddress.getByName("localhost");
//            DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, senderPort);
//            System.out.println("resend"+a);
//            hostBroadcast.send(packet); //System.out.println("b" + " " + messagesToSend);
//            hostBroadcast.close();
//        }
//        for (int m = 1; m < messageNum+1; m++) {
//            if (null != resendListH && resendListH.contains(m)){
//                continue;
//            }
//            String hostInfo = String.valueOf(hostId);
//            String messageHost = hostInfo + " " + String.valueOf(m);
//            byte[] messagesSend = messageHost.getBytes();
//            DatagramSocket hostBroadcast = new DatagramSocket();
//            InetAddress address = InetAddress.getByName("localhost");
//            DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, senderPort);
//            hostBroadcast.send(packet); //System.out.println("b" + " " + messagesToSend);
//            hostBroadcast.close();
//            // sleep
//            //Thread.sleep( 200);
//        }
    }

    public void broadcast(int wait, int hostId, int senderPort, String messagesToSend) throws IOException, InterruptedException {
        /*for(int m = 0; m < messageNum+1; m++){
            for(int h = 0; h < senderPort.size(); h++){
                if(ACK.contains("ack "+ String.valueOf(hostPort.indexOf(senderPort.get(h))+1)+ " " + m)){
                    broadcast(hostId, senderPort, hostPort, m)
                }
            }
        }*/
        String hostInfo = String.valueOf(hostId);
        String messageHost = hostInfo +" "+ String.valueOf(messagesToSend);
        byte[] messagesSend = messageHost.getBytes();
        DatagramSocket hostBroadcast = new DatagramSocket();
        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length ,address , senderPort);
        hostBroadcast.send(packet);
        hostBroadcast.close();
        Thread.sleep(20);
        //Thread.sleep(wait);

//        for (int j = 0; j < senderPort.size(); j++){
//            String hostInfo = String.valueOf(hostId);
//            String messageHost = hostInfo +" "+ String.valueOf(messagesToSend);
//            byte[] messagesSend = messageHost.getBytes();
//            DatagramSocket hostBroadcast = new DatagramSocket();
//            InetAddress address = InetAddress.getByName("localhost");
//            DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length ,address , (Integer) senderPort.get(j));
//            hostBroadcast.send(packet);
//            hostBroadcast.close();
//        }
        //System.out.println("b "+messagesToSend);
        CreateFile.getInstance().addLogBuffer("b "+messagesToSend+"\n");
        //log.add("b "+messagesToSend);
    }

    @Override
    public void run() {
        for (int i = 1; i < messageNum + 1; i++) {
            try {
                broadcast(wait, hostId, senderPort, String.valueOf(i));
                // log.add("b " + messageSend);
            } catch (IOException | InterruptedException e) {
                System.out.println(e);
            }
        }
        System.out.println(LocalDateTime.now());
        while(true) {
            try {
                LocalDateTime time1 = LocalDateTime.now();
                resend(wait, hostId, receiverId, senderPort, ackResend, checkList);
                LocalDateTime time2 = LocalDateTime.now();
                //System.out.println(Duration.between(time1, time2).toMillis());
                //Thread.sleep( 200);
            } catch (IOException | InterruptedException e) {
                System.out.println(e);
            }
        }
    }
}

