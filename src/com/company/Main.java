package com.company;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static String MACRegex = "([\\da-fA-F]{2}-){5}[\\da-fA-F]{2}";

    public static void main(String[] args) throws SocketException {
        ArrayList<NetworkInterface> inets = getReachableInterfaces(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface net : inets) {
            String ip = getSubnetIPRange(net);
            int tmp = (32 - getMaskPrefLen(net)) * 64;
            int ipsAmount = tmp - (2 * tmp / 256);
            displayInterfaceInformation(net);
            System.out.println("Scanning...");
            printNetworkDevices(ip, ipsAmount);
        }
        System.out.println("Done");
    }

    public static ArrayList<NetworkInterface> getReachableInterfaces(Enumeration<NetworkInterface> nets) {
        ArrayList<NetworkInterface> resArr = new ArrayList<>();
        try {
            while (nets.hasMoreElements()) {
                NetworkInterface currNet = nets.nextElement();
                if (currNet.isUp() && !currNet.isLoopback())
                    resArr.add(currNet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resArr;
    }

    public static void printNetworkDevices(String startIP, int iterations) {
        IP start = new IP(startIP);
        for (int i = 0; i < iterations; i++) {
            String answer = getARPAnswer(start.IP);
            if(answer != null) {
                System.out.println(answer);
                System.out.println("IP: " + start.IP);
                System.out.println("------------------------------");
            }
            start.inc();
        }
    }

    public static String getSubnetIPRange(NetworkInterface networkInterface) {
        String address = networkInterface.getInterfaceAddresses().get(0).getAddress().getHostAddress();
        short[] localAddress = strToAddr(address, 4);
        int prefLen = getMaskPrefLen(networkInterface);
        int shft = 0xffffffff << (32 - prefLen);
        short[] mask = new short[4];
        mask[0] = (short) (((shft & 0xff000000) >> 24) & 0xff);
        mask[1] = (short) (((shft & 0x00ff0000) >> 16) & 0xff);
        mask[2] = (short) (((shft & 0x0000ff00) >> 8) & 0xff);
        mask[3] = (short) ((shft & 0x000000ff) & 0xff);

        for (int i = 0; i < 3; i++) {
            localAddress[i] = (short) (localAddress[i] & mask[i]);
        }

        String maskStr = mask[0] + "." + mask[1] + "." + mask[2] + "." + mask[3];
        System.out.println("Mask: " + maskStr);

        String ip = localAddress[0] + "." + localAddress[1] + "." + localAddress[2] + "." + localAddress[3];
        System.out.println(ip);
        return ip;
    }

    static void displayInterfaceInformation(NetworkInterface netInt) {
        System.out.printf("Display name: %s\n", netInt.getDisplayName());
        System.out.printf("Name: %s\n", netInt.getName());
        Enumeration<InetAddress> inetAddresses = netInt.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("InetAddress: %s\n", inetAddress);
        }
        System.out.print("\n");
    }

    private static int getMaskPrefLen(NetworkInterface networkInterface) {
        return networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength();
    }

    public static short[] strToAddr(String addr, int count) {
        short[] answer = new short[count];
        addr = addr + ".";
        for(int i = 0; i < count - 1; i++) {
            answer[i] = Short.parseShort(addr.substring(0, addr.indexOf('.')));
            addr = addr.substring(addr.indexOf('.') + 1);
        }

        return answer;
    }

    private static String getARPAnswer(String addr) {
        getCmdAnswer("arp refresh");
        String command = "arp -a " + addr;
        String answer = getCmdAnswer(command);
        Matcher matcher = Pattern.compile(MACRegex).matcher(answer);

        if(matcher.find()) {
            return "MAC-address: " + matcher.group();
        } else {
            return null;
        }
    }

    private static String getCmdAnswer(String command) {
        StringBuilder answer = new StringBuilder();
        try
        {
            Process process = Runtime.getRuntime().exec(command);
            Scanner cmdIn = new Scanner(process.getInputStream());
            while(cmdIn.hasNextLine()) answer.append(cmdIn.nextLine());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return answer.toString();
    }

}