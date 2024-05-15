package cn.iselab.mooctest.device.util;

import cn.iselab.mooctest.device.common.constant.IOSCommandConstants;
import cn.iselab.mooctest.device.common.constant.PathConstants;
import cn.iselab.mooctest.device.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ExecuteUtil {
    private static final int SUCCESS = 0;

    private static final String SUCCESS_MESSAGE = "Process executed successfully.";

    private static final String ERROR_MESSAGE = "Process failed.";

    private static final Logger logger = LoggerFactory.getLogger(ExecuteUtil.class);

    private static boolean minicapOpened = false;

    private static boolean minicapConnected = false;

    public static boolean isMinicapOpened() {
        return minicapOpened;
    }

    public static boolean isMinicapConnected() {
        return minicapConnected;
    }

    public ExecuteUtil(Device device) { }

    public static void executeIOSMinicap(String deviceUdid) {
        String minicapPath = PathConstants.IOS_MINICAP_PATH;
        String result = OSUtil.runCommand(minicapPath + "/build/ios_minicap -u " + deviceUdid + " -p " + "12345" + " -r 400x600");
        logger.info("ios minicap start result:{}", result);
        if (!result.contains("error")) {
            logger.info("--------IOS Minicap start:{}--------", SUCCESS_MESSAGE);
        } else {
            logger.info("--------IOS Minicap start:{}--------", ERROR_MESSAGE);
        }
    }

    public static void executeIOSWda(String deviceUdid) {
        String wdaStartCmd = IOSCommandConstants.getIOSWdaStartCommand(deviceUdid);
        String result = OSUtil.runCommand(wdaStartCmd);
        logger.info("wda start result:{}", result);
        if (!result.contains("error")) {
            logger.info("--------IOS wda start:{}--------", SUCCESS_MESSAGE);
        } else {
            logger.info("--------IOS wda start:{}--------", ERROR_MESSAGE);
        }
    }

    public static void executeIOSWdaProxy(String deviceUdid) {
        String wdaProxyStartCmd = IOSCommandConstants.getIOSWdaProxyStartCommand(deviceUdid, DeviceManagementUtil.getIosDeviceBySerialNo(deviceUdid).getForwardWdaPort());
        String result = OSUtil.runCommand(wdaProxyStartCmd);
        logger.info("wdaProxy start result:{}", result);
        if (!result.contains("error")) {
            logger.info("--------IOS wdaProxy start:{}--------", SUCCESS_MESSAGE);
        } else {
            logger.info("--------IOS wdaProxy start:{}--------", ERROR_MESSAGE);
        }
        String sessionId = WdaClientUtil.getSessionId(deviceUdid);
        DeviceManagementUtil.getIosDeviceBySerialNo(deviceUdid).setWdaSession(sessionId);
    }

    public static void installIpa(String deivceUdid, String ipaPath) {
        String iosDeviceInstallerCmd = IOSCommandConstants.ideviceInstaller(deivceUdid, ipaPath);
        String result = OSUtil.runCommand(iosDeviceInstallerCmd);
        System.out.println(result);
    }

    public static void iosScreenShot(String deviceUdid, String outputPath) {
        String iosDeviceScreenShotCmd = IOSCommandConstants.ideviceScreenShot(deviceUdid, outputPath);
        String result = OSUtil.runCommand(iosDeviceScreenShotCmd);
        System.out.println(result);
    }

    public static void createForward(String deviceUdid) {
        Device device = DeviceManagementUtil.getIosDeviceBySerialNo(deviceUdid);
//        device.setForwardMiniCapPort(PortManagementUtil.useFirstIOSMiniCapPort());
        device.setForwardMiniCapPort(12345);
        device.setForwardWdaPort(PortManagementUtil.useFirstIOSWdaPort());
        logger.info("IOS minicap & wda created device:{}, minicap port:{}, wda port:{}", deviceUdid,
                device.getForwardMiniCapPort(),
                device.getForwardWdaPort());
    }

    public static void removeForward(String deviceUdid) {
        Device device = DeviceManagementUtil.getIosDeviceBySerialNo(deviceUdid);
        killMinicapServer(device.getForwardMiniCapPort());
        killWdaServer(device.getForwardWdaPort(), deviceUdid);
        logger.info("IOS minicap & wda removed device:{}, minicap port:{}, wad port:{}", deviceUdid, device.getForwardMiniCapPort(), device.getForwardWdaPort());
        if (device.getForwardMiniCapPort() != 0) {
            PortManagementUtil.recoverIOSMiniCapPort(device.getForwardMiniCapPort());
        }
        if (device.getForwardWdaPort() != 0) {
            PortManagementUtil.recoverIOSWdaPort(device.getForwardWdaPort());
        }
        device.setForwardMiniCapPort(0);
        device.setForwardWdaPort(0);
    }

    private static void killMinicapServer(int port) {
        if (!String.valueOf(port).isEmpty()) {
            String result = OSUtil.runCommand(IOSCommandConstants.getIOSMatchMinicapOrWdaProxyPort(port)).replace("\n", "");
            if (result != null && !result.isEmpty()) {
                int serverPort = Integer.valueOf(result);
                String killCmd = "kill -9 " + serverPort;
                OSUtil.runCommand(killCmd);
            }
        }
    }

    private static void killWdaServer(int port, String deviceId) {
        if (!String.valueOf(port).isEmpty() && !deviceId.isEmpty()) {
            String wdaProxyPort = OSUtil.runCommand(IOSCommandConstants.getIOSMatchMinicapOrWdaProxyPort(port)).replace("\n", "");
            String wdaPort = OSUtil.runCommand(IOSCommandConstants.getIOSMatchWdaPort(deviceId)).replace("\n", "");
            if (wdaProxyPort != null && wdaPort != null && !wdaProxyPort.isEmpty() && !wdaPort.isEmpty()) {
                int wdaProxyServerPort = Integer.valueOf(wdaProxyPort);
                int wdaServerPort = Integer.valueOf(wdaPort);
                String killWdaProxyCmd = "kill -9" + wdaProxyServerPort;
                String killWdaCmd = "kill -9 " + wdaServerPort;
                OSUtil.runCommand(killWdaProxyCmd);
                OSUtil.runCommand(killWdaCmd);
            }
        }
    }

    private static void readProcessOutput(final Process process) {
        read(process.getInputStream(), System.out);
        read(process.getErrorStream(), System.err);
    }

    private static void read(InputStream inputStream, PrintStream outputStream) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outputStream.println(line);
                if (line.contains("quirks")) {
                    Thread.sleep(10);
                    minicapOpened = true;
                } else if (line.contains("New client connection")) {
                    minicapConnected = true;
                }
            }

        } catch (IOException e) {
            logger.error("IO read Exception, cause:{}", e.getMessage());
        } catch (InterruptedException e) {
            logger.error("IO Interrupted Exception, cause:{}", e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("IO inputStream close Exception, cause:{}", e.getMessage());
            }
        }
    }

}
