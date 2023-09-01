package cs451;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {

    private static final String IP_START_REGEX = "/";

    private int id;
    private String ip;
    private int port = -1;

    public boolean populate(String idString, String ipString, String portString) {
        try {
            id = Integer.parseInt(idString);//将性质为string的id转换为decimal

            String ipTest = InetAddress.getByName(ipString).toString();//Determines the IP address of a host, given the host's name and returns as string
            if (ipTest.startsWith(IP_START_REGEX)) {
                ip = ipTest.substring(1);//如果首字母是/的话就从index[1]开始读取
            } else {
                ip = InetAddress.getByName(ipTest.split(IP_START_REGEX)[0]).getHostAddress();//get the ip address of ipTest
            }

            port = Integer.parseInt(portString);
            if (port <= 0) {
                System.err.println("Port in the hosts file must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {//raise an exception if the ip of the host is not suitable for processing
            if (port == -1) {
                System.err.println("Id in the hosts file must be a number!");
            } else {
                System.err.println("Port in the hosts file must be a number!");
            }
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return true;
    }

    public int getId() {
        return id;
    }//这里是前面get 了id

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

}
