package com.andycugb.cron.util;

import org.apache.commons.lang.StringUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jbcheng on 2016-03-28.
 */
public class IpUtil {
    // inner ip
    private static Pattern pattern = Pattern
            .compile("^(10\\.|172\\.(1[6-9])|2[0-9]|3[01])\\.|192\\.168\\.");

    /**
     * get local server ips
     * @param includeOuter true when include inner ip
     * @return server ip list
     * @throws SocketException
     */
    public static List<String> getServerIps(boolean includeOuter) throws SocketException {
        List<String> ipList = new ArrayList<String>();
        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = netInterfaces.nextElement();
            Enumeration<InetAddress> netAddresses = netInterface.getInetAddresses();

            while (netAddresses.hasMoreElements()) {

                InetAddress ip = netAddresses.nextElement();
                if (ip instanceof Inet4Address) {
                    String localIp = ip.getHostAddress();
                    if (!StringUtils.equals(localIp, "127.0.0.1") && !includeOuter
                            && !isInnerIp(localIp)) {
                        ipList.add(localIp);
                    }
                }
            }
        }
        return ipList;
    }

    private static boolean isInnerIp(String localIp) {
        return pattern.matcher(localIp).find();
    }

    public static void main(String[]args) throws Exception{
        System.out.println(Arrays.toString(getServerIps(true).toArray()));
        System.out.println("==========================");
        System.out.println(Arrays.toString(getServerIps(false).toArray()));
    }
}
