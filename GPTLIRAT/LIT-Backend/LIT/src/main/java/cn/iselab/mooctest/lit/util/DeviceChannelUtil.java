package cn.iselab.mooctest.lit.util;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class DeviceChannelUtil {

    private static Map<String, Channel> device2ChannelMap = new HashMap<>();

    private static Map<String, Channel> device2WSChannelMap = new HashMap<>();

    private static Map<String, Channel> device2LogChannelMap = new HashMap<>();

    private static Map<String, Channel> device2LogWSChannelMap = new HashMap<>();

    private DeviceChannelUtil() {

    }

    public static void putOrUpdateDevice2Channel(String deviceSerialNo, Channel channel) {
        device2ChannelMap.put(deviceSerialNo, channel);
    }

    public static void removeDevice2Channel(String deviceSerialNo) {
        device2ChannelMap.remove(deviceSerialNo);
    }

    public static String getDeviceSerialNoByChannel(Channel channel) {
        for (Map.Entry<String, Channel> entry : device2ChannelMap.entrySet()) {
            if (entry.getValue().id().asLongText().equals(channel.id().asLongText())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Channel getChannelByDevice(String deviceSerialNo) {
        return device2ChannelMap.get(deviceSerialNo);
    }

    public static void putOrUpdateDevice2WSChannel(String deviceSerialNo, Channel channel) {
        device2WSChannelMap.put(deviceSerialNo, channel);
    }

    public static void removeDevice2WSChannel(String deviceSerialNo) {
        device2WSChannelMap.remove(deviceSerialNo);
    }

    public static String getDeviceSerialNoByWSChannel(Channel channel) {
        for (Map.Entry<String, Channel> entry : device2WSChannelMap.entrySet()) {
            if (entry.getValue().id().asLongText().equals(channel.id().asLongText())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Channel getWSChannelByDevice(String deviceSerialNo) {
        return device2WSChannelMap.get(deviceSerialNo);
    }

    public static void putOrUpdateDevice2LogChannel(String deviceSerialNo, Channel channel) {
        device2LogChannelMap.put(deviceSerialNo, channel);
    }

    public static void removeDevice2LogChannel(String deviceSerialNo) {
        device2LogChannelMap.remove(deviceSerialNo);
    }

    public static String getDeviceSerialNoByLogChannel(Channel channel) {
        for (Map.Entry<String, Channel> entry : device2LogChannelMap.entrySet()) {
            if (entry.getValue().id().asLongText().equals(channel.id().asLongText())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Channel getLogChannelByDevice(String deviceSerialNo) {
        return device2LogChannelMap.get(deviceSerialNo);
    }

    public static void putOrUpdateDevice2LogWSChannel(String deviceSerialNo, Channel channel) {
        device2LogWSChannelMap.put(deviceSerialNo, channel);
    }

    public static void removeDevice2LogWSChannel(String deviceSerialNo) {
        device2LogWSChannelMap.remove(deviceSerialNo);
    }

    public static String getDeviceSerialNoByLogWSChannel(Channel channel) {
        for (Map.Entry<String, Channel> entry : device2LogWSChannelMap.entrySet()) {
            if (entry.getValue().id().asLongText().equals(channel.id().asLongText())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Channel getLogWSChannelByDevice(String deviceSerialNo) {
        return device2LogWSChannelMap.get(deviceSerialNo);
    }
}
