package cn.iselab.mooctest.device.service;

import cn.iselab.mooctest.device.common.constant.ADBCommandConstants;
import cn.iselab.mooctest.device.common.constant.PathConstants;
import cn.iselab.mooctest.device.model.UINodeVO;
import cn.iselab.mooctest.device.service.handler.SAXHandler;
import cn.iselab.mooctest.device.util.CommandUtil;
import cn.iselab.mooctest.device.util.DeviceManagementUtil;
import cn.iselab.mooctest.device.util.ExecuteUtil;
import cn.iselab.mooctest.device.util.ImageUtil;
import com.android.ddmlib.IDevice;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentService.class);

    private static List<UINodeVO> retrieveNodes(String xmlPath) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            SAXHandler handler = new SAXHandler();
            parser.parse(new File(xmlPath), handler);
            return handler.getNodeList();
        } catch (ParserConfigurationException e) {
            LOGGER.error("getUINode ParserConfigurationException", e);
        } catch (SAXException e) {
            LOGGER.error("getUINode SAXException", e);
        } catch (IOException e) {
            LOGGER.error("getUINode IOException", e);
        }
        return Collections.emptyList();
    }

    private static void pullScreenshot(String serialNo, String outputPath) throws Exception {
        Set<String> deviceUdids = DeviceManagementUtil.getIosDeviceUdids();
        if (deviceUdids.contains(serialNo)) {
            ExecuteUtil.iosScreenShot(serialNo, outputPath);
        } else {
            IDevice iDevice = DeviceManagementService.getInstance().getIDevice(serialNo);
            pullScreenshot(iDevice, outputPath);
        }
    }

    private static void pullScreenshot(IDevice iDevice, String outputPath) throws Exception {
        CommandUtil.executeShellCommand(iDevice, String.format(ADBCommandConstants.TAKE_SCREENSHOT, "curFullscreen.png"));
        iDevice.pullFile("/data/local/tmp/curFullscreen.png", outputPath);
    }

    private static void pullXml(IDevice iDevice, String outputPath) throws Exception {
        System.out.println(CommandUtil.executeShellCommand(iDevice, ADBCommandConstants.DUMP_UI_XML));
        iDevice.pullFile(PathConstants.REMOTE_XML_PATH, outputPath);
    }

    private static String zip(String src, String dest) {
        File srcFile = new File(src);
        if (!srcFile.exists() && !srcFile.mkdirs()) {
            LOGGER.error("Cannot create file {}.", srcFile.getAbsolutePath());
        }
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        try {
            ZipFile zipFile = new ZipFile(dest);
            if (srcFile.isDirectory()) {
                zipFile.addFolder(srcFile, parameters);
            } else {
                zipFile.addFile(srcFile, parameters);
            }
            return dest;
        } catch (ZipException e) {
            LOGGER.error("Error on zipping file.", e);
        }
        return null;
    }

    /**
     * Read a file and return its content as a byte array.
     *
     * @param filePath
     * @return
     */
    private static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Error on reading file " + filePath + ".", e);
        }
        return buffer;
    }

    /**
     * Find the leaf node in which the point (x, y) locate.
     *
     * @param nodes
     * @param x
     * @param y
     * @return
     */
    private static UINodeVO findLeafNode(List<UINodeVO> nodes, int x, int y) {
        UINodeVO leaf = null;
        for (UINodeVO n : nodes) {
            if (pointInside(x, y, n)) {
                if (null == leaf) {
                    leaf = n;
                } else if (pointInside(n.getxPosition(), n.getyPosition(), leaf)
                        && pointInside(n.getxPosition() + n.getWidth(),
                        n.getyPosition() + n.getHeight(), leaf)) { // node n is inside leaf
                    leaf = n;
                }
            }
        }
        return leaf;
    }

    /**
     * Whether point (x, y) is inside the given node.
     *
     * @param x
     * @param y
     * @param node
     * @return
     */
    private static boolean pointInside(int x, int y, UINodeVO node) {
        return x >= node.getxPosition()
                && x <= node.getxPosition() + node.getWidth()
                && y >= node.getyPosition()
                && y <= node.getyPosition() + node.getHeight();
    }

    private static String getActivityInfo(IDevice iDevice) {
        String activityInfo = CommandUtil.executeShellCommand(iDevice, ADBCommandConstants.GET_ACTIVITY);
        String[] lines = activityInfo.split("\n");
        for (String line : lines) {
            if (line.contains("mResume")) {
                return line;
            }
        }
        return null;
    }

    public byte[] zipAndGet(String serialNo, long scriptId) {
        String filePath = PathConstants.COMPONENT_STORAGE_PATH + File.separator + serialNo + "_" + scriptId;
        String zipPath = zip(filePath + File.separator, filePath + ".zip");
        if (zipPath != null) {
            return getBytes(zipPath);
        }
        return null;
    }

    public boolean recordClick(String serialNo, String scriptName, int stepIndex, int x, int y) {
        return recordClick(serialNo, scriptName, stepIndex, x, y, PathConstants.COMPONENT_STORAGE_PATH);
    }

    public boolean recordClick(String serialNo, String scriptName, int stepIndex, int x, int y, String prefix) {
        String baseDirPath = prefix + File.separator +
                scriptName + File.separator +
                "step" + stepIndex;
        File baseDir = new File(baseDirPath);
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            LOGGER.error("Failed to create base directories {}", baseDirPath);
        }

        String screenshotPath = baseDirPath + File.separator + "screenshot.png";
        String xmlPath = baseDirPath + File.separator + "ui.xml";
        try {
            pullScreenshot(serialNo, screenshotPath);

            UINodeVO leaf = null;
            String activity = null;
            // 非ios,至于ios怎么处理的，暂时不知道
            if (!DeviceManagementUtil.getIosDeviceUdids().contains(serialNo)) {
                // Xml files and activities can only be retrieved under Android systems.
                IDevice iDevice = DeviceManagementService.getInstance().getIDevice(serialNo);
                pullXml(iDevice, xmlPath);
                // Attempt to cut & save the clicked element.
                leaf = findLeafNode(retrieveNodes(xmlPath), x, y);
                if (leaf != null) {
                    String elementPath = baseDirPath + File.separator + "element.png";
                    ImageUtil.cutImg(screenshotPath, elementPath, leaf);
                } else {
                    LOGGER.warn("Cannot find leaf node for ({}, {}).", x, y);
                }
                activity = getActivityInfo(iDevice);
            }

            // Record clicking and activity info into a file.
            String infoPath = baseDirPath + File.separator + "info.json";
            try (FileWriter writer = new FileWriter(infoPath)) {
                JSONObject info = new JSONObject()
                        .put("x", x)
                        .put("y", y);
//                        .put("device", new JSONObject(DeviceManagementUtil.find(serialNo)));
//                if (leaf != null) {
//                    info.put("node_attrib", leaf.getAttributes());
//                }
//                if (activity != null) {
//                    info.put("activity", activity);
//                }
                writer.write(info.toString());
            } catch (IOException e) {
                LOGGER.error("Error on writing clicking info.", e);
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Error on recording clicking action.", e);
        }
        return false;
    }

    public byte[] getCurScreenShot(String serialNo) {
        String screenshotPath = PathConstants.COMPONENT_STORAGE_PATH + File.separator + "curFullscreen_" + serialNo + ".png";
        try {
            pullScreenshot(serialNo, screenshotPath);
            return getBytes(screenshotPath);
        } catch (Exception e) {
            LOGGER.error("Error on taking screenshots.", e);
            return new byte[0];
        }
    }

    public void executeCommand(String serialNo, String command) {
        IDevice iDevice = DeviceManagementService.getInstance().getIDevice(serialNo);
        CommandUtil.executeShellCommand(iDevice, command);
    }

}
