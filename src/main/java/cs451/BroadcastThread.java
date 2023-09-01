package cs451;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BroadcastThread extends Thread{
    private int hostId;
    ArrayList<Integer> hostPort = new ArrayList<>();
    ArrayList<Integer> senderPort = new ArrayList<>();
    ArrayList<String> log = new ArrayList<>();
    ArrayList<Integer> logger = new ArrayList<>();

    private int messageNum;
    Map<String, ArrayList<Integer>> ackResend = new ConcurrentHashMap<>();

    public BroadcastThread(int hostId, ArrayList hostPort, ArrayList senderPort, int messageNum, ArrayList<String> log, ArrayList<Integer> logger, Map<String, ArrayList<Integer>> ackResend) throws SocketException {
        this.hostId = hostId;
        this.hostPort = hostPort;
        this.messageNum = messageNum;
        this.senderPort = senderPort;
        this.log = log;
        this.logger = logger;
        this.ackResend = ackResend;
    }
    public void resend(int hostId, int messageNum, ArrayList<Integer> senderPort, ArrayList<Integer> hostPort, Map<String, ArrayList<Integer>> ackResend) throws IOException {
        for (int h = 0; h < senderPort.size(); h++) {
            String resendToHost = String.valueOf(hostPort.indexOf(senderPort.get(h))+1);
            ArrayList<Integer> resendListH = ackResend.get(resendToHost);

            for (int m = 1; m < messageNum+1; m++) {
                if (null != resendListH && resendListH.contains(m)){
                    continue;
                }

                String hostInfo = String.valueOf(hostId);
                String messageHost = hostInfo + " " + String.valueOf(m);
                byte[] messagesSend = messageHost.getBytes();
                DatagramSocket hostBroadcast = new DatagramSocket();
                InetAddress address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, (Integer) senderPort.get(h));
                hostBroadcast.send(packet);
                //System.out.println("b" + " " + messagesToSend);
                hostBroadcast.close();


            /*    else {
                    note.add("added before");
                }*/
            }
        }
    }

    public void broadcast(int hostId, ArrayList senderPort, String messagesToSend) throws IOException {
        /*for(int m = 0; m < messageNum+1; m++){
            for(int h = 0; h < senderPort.size(); h++){
                if(ACK.contains("ack "+ String.valueOf(hostPort.indexOf(senderPort.get(h))+1)+ " " + m)){
                    broadcast(hostId, senderPort, hostPort, m)
                }
            }
        }*/

        for (int j = 0; j < senderPort.size(); j++){
            String hostInfo = String.valueOf(hostId);
            String messageHost = hostInfo +" "+ String.valueOf(messagesToSend);
            byte[] messagesSend = messageHost.getBytes();
            DatagramSocket hostBroadcast = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length ,address , (Integer) senderPort.get(j));
            hostBroadcast.send(packet);
            hostBroadcast.close();
        }
        System.out.println("b "+messagesToSend);
        CreateFile.getInstance().addLogBuffer("b "+messagesToSend+"\n");
        //log.add("b "+messagesToSend);
    }

    @Override
    public void run() {
        for (int i = 1; i < messageNum + 1; i++) {
            try {
                broadcast(hostId, senderPort, String.valueOf(i));
                //log.add("b " + messageSend);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        while(true) {
            try {
                resend(hostId, messageNum, senderPort, hostPort, ackResend);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        /*
        try{
            while(true) {
                if(ACK.size()==hostPort.size()){
                    throw new InterruptedException();
                }else{
                    for (int i = 1; i < messageNum + 1; i++) {
                        String messageSend = String.valueOf(i);
                        try {
                            broadcast(hostId, senderPort, messageSend);
                            System.out.println("b "+messageSend);
                            //log.add("b " + messageSend);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                    }
                }
            }
            }catch (InterruptedException e) {
            System.out.println(e);
        }


        for(int h = 0; h < 15; h++){
            for (int i = 1; i < messageNum+1; i++){
                String messageSend = String.valueOf(i);
                try {
                    broadcast(hostId, senderPort, messageSend);
                    log.add("b " + messageSend);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        //cancel(ACK, isEnd);

        while(true){
            for (int i = 1; i < messageNum+1; i++){
                String messageSend = String.valueOf(i);
                try {
                    broadcast(hostId, senderPort, messageSend);
                    //log.add("b " + messageSend);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        for (int h = 1; h < senderPort.size(); h++){
            for (int j = 1; j < messageNum+1; j++){
                for (int i = 1; i < messageNum+1; i++){
                    String messageSend = String.valueOf(i);
                    try {
                        broadcast(hostPort, messageSend);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

         */
    }
}

