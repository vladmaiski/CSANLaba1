package com.company;

public class IP {
    String IP;

    public IP(String IP) {
        this.IP = IP;
    }

    public void inc() {
        final short BYTES_AMOUNT = 4;
        final short MAX_BYTE_SIZE = 254;
        String[] arr;
        arr = IP.split("\\.");
        short[] ipbytes = new short[BYTES_AMOUNT];

        for (int i = 0; i < BYTES_AMOUNT; i++) {
            ipbytes[i] = Short.parseShort(arr[i]);
        }

        for (int i = BYTES_AMOUNT - 1; i >= 0; i--) {
            if (ipbytes[i] != MAX_BYTE_SIZE) {
                ipbytes[i]++;
                break;
            } else {
                if(i == BYTES_AMOUNT - 1) {
                    ipbytes[i] = 0;
                } else {
                    ipbytes[i] = 1;
                }
                ipbytes[i - 1]++;
            }
        }

        StringBuilder resIP = new StringBuilder();
        for (int i = 0; i < BYTES_AMOUNT; i++) {
            if (i != 0)
                resIP.append(".");
            resIP.append(ipbytes[i]);
        }
        IP = resIP.toString();
    }
}
