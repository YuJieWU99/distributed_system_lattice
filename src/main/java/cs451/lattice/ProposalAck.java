package cs451.lattice;

import cs451.CreateFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class ProposalAck extends Thread{
    private final int hostId;
    private final int proNum;
    private int proEleMaxNum;
    private int eleMaxNum;
    private Boolean active;
    private int ackCount;
    private int nackCount;
    private int activeProNum;
    Map<Integer, CopyOnWriteArraySet<Integer>> proValue = new ConcurrentHashMap<>();
    private final BufferedReader br;
    Map<Integer, CopyOnWriteArraySet<Short>> broList = new ConcurrentHashMap<>();
    ArrayList<Integer> processPorts = new ArrayList<>();
    Map<Short, CopyOnWriteArraySet<Short>> ack = new ConcurrentHashMap<>(); // 第几个process的第几个propose sequence要被acknowledge
    Map<Integer, CopyOnWriteArraySet<Short>> nack = new ConcurrentHashMap<>();

    public ProposalAck(int hostId, int proNum, int proEleMaxNum, int eleMaxNum, ArrayList<Integer> processPorts, BufferedReader br, Map<Integer, CopyOnWriteArraySet<Short>> broList,
                    Map<Short, CopyOnWriteArraySet<Short>> ack, Map<Integer, CopyOnWriteArraySet<Short>> nack, Map<Integer, CopyOnWriteArraySet<Integer>> proValue){
        this.hostId = hostId;
        this.proNum = proNum;
        this.proEleMaxNum = proEleMaxNum;
        this.eleMaxNum = eleMaxNum;
        this.processPorts = processPorts;
        this.br = br;
        this.broList = broList;
        this.ack = ack;
        this.nack = nack;
        this.proValue = proValue;
    }
    public void acknow(int hostId, ArrayList<Integer> processPorts, Map<Short, CopyOnWriteArraySet<Short>> ack) throws IOException {
        for (Short process : ack.keySet()){
            Integer processId = Integer.valueOf(process);
            //String messageInfo = null;
            for (Short proposeSequence: ack.get(process)){
                //messageInfo = collect.stream().map(Integer::valueOf).map(Object::toString).collect(Collectors.joining(","));
                String messageHost = "ack" + " " + hostId + " " + proposeSequence;
                byte[] messagesSend = messageHost.getBytes();
                DatagramSocket hostBroadcast = new DatagramSocket();
                InetAddress address = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(messagesSend, messagesSend.length, address, processPorts.get(processId - 1));
                hostBroadcast.send(packet); //System.out.println("b" + " " + messagesToSend);
                //System.out.println(messageHost);
                hostBroadcast.close();
            }
        }
    }

    public void run() {
        while(true) {
            try {
                acknow(hostId, processPorts, ack);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}


