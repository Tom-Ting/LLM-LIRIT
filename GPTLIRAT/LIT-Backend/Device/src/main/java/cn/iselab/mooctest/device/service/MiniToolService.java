package cn.iselab.mooctest.device.service;

import cn.iselab.mooctest.device.common.constant.ADBCommandConstants;
import cn.iselab.mooctest.device.common.constant.MiniToolConstants;
import cn.iselab.mooctest.device.common.constant.PathConstants;
import cn.iselab.mooctest.device.model.Banner;
import cn.iselab.mooctest.device.model.Device;
import cn.iselab.mooctest.device.service.handler.AndroidControlHandler;
import cn.iselab.mooctest.device.util.CommandUtil;
import cn.iselab.mooctest.device.util.DeviceManagementUtil;
import cn.iselab.mooctest.device.util.ExecuteUtil;
import cn.iselab.mooctest.device.util.PortManagementUtil;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A service which creates MiniCap and MiniTouch sockets.
 * The MiniCap socket sends screenshots data to the remote eureka service
 * {@code lit}. The MiniTouch socket performs interfaces
 * for triggering multi-touch events.
 */
public class MiniToolService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private IDevice iDevice;

    private final Device device;

    private final String host;

    private final int port;

    private Thread dataReaderThread;

    private Thread imageHandleThread;

    private Thread imageTransferThread;

    private String miniTouchFile;

    private String miniCapFile;

    private final BlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();

    private boolean isRunning = false;

    private Channel channel;

    private final Banner banner = new Banner();

    private InputStream miniTouchInputStream;

    private OutputStream miniTouchOutputStream;

    private Socket miniTouchSocket;


    public MiniToolService(IDevice iDevice, String host, int port) {
        this.iDevice = iDevice;
        this.device = DeviceManagementUtil.getDeviceBySerialNo(iDevice.getSerialNumber());
        this.host = host;
        this.port = port;
    }

    public MiniToolService(String deviceUdid, String host, int port) {
        this.device = DeviceManagementUtil.getIosDeviceBySerialNo(deviceUdid);
        this.host = host;
        this.port = port;
    }

    public Banner getBanner() {
        return banner;
    }

    public void initIOSMiniCap() {
        ExecuteUtil.createForward(this.device.getSerialNumber());
        startIOSMinicap();
        startIOSWdaService();
    }

    public void initMiniTool() {
        String abi = device.getAbi();
        String sdk = device.getSdk();

        if (Integer.parseInt(sdk) < 16) {
            miniTouchFile = MiniToolConstants.MINITOUCH_NOPIE;
            miniCapFile = MiniToolConstants.MINICAP_NOPIE;
        } else {
            miniTouchFile = MiniToolConstants.MINITOUCH_BIN;
            miniCapFile = MiniToolConstants.MINICAP_BIN;
        }
        log.info("abi:{}, sdk:{}", abi, sdk);

        if (!isMiniCapInstalled()) {
            File miniCapBinFile = new File(MiniToolConstants.getMinicapBin(), abi + File.separator + miniCapFile);
            File miniTouchBinFile = new File(MiniToolConstants.getMiniTouchBin(), abi + File.separator + miniTouchFile);
            File miniCapSoFile = new File(MiniToolConstants.getMinicapSo(), "android-" + sdk
                    + File.separator + abi + File.separator + MiniToolConstants.MINICAP_SO);
            try {
//                iDevice.pushFile(miniCapBinFile.getAbsolutePath(), PathConstants.REMOTE_DEVICE_PATH
//                        + File.separator + miniCapFile);
//                iDevice.pushFile(miniCapSoFile.getAbsolutePath(), PathConstants.REMOTE_DEVICE_PATH
//                        + File.separator + MiniToolConstants.MINICAP_SO);

                log.info("miniCapBinFile:"+miniCapBinFile.getAbsolutePath());
                log.info("remote_device_path:"+PathConstants.REMOTE_DEVICE_PATH
                        + '/' + miniCapFile);

                iDevice.pushFile(miniCapBinFile.getAbsolutePath(), PathConstants.REMOTE_DEVICE_PATH
                        + '/' + miniCapFile);

                log.info("miniCapSoFile:"+miniCapSoFile.getAbsolutePath());
                log.info("remote_device_path:"+PathConstants.REMOTE_DEVICE_PATH
                        + '/' + MiniToolConstants.MINICAP_SO);

                iDevice.pushFile(miniCapSoFile.getAbsolutePath(), PathConstants.REMOTE_DEVICE_PATH
                        + '/' + MiniToolConstants.MINICAP_SO);
                CommandUtil.executeShellCommand(iDevice, String.format(ADBCommandConstants.CHMOD_COMMAND,
                        PathConstants.REMOTE_DEVICE_PATH, miniCapFile));

                iDevice.pushFile(miniTouchBinFile.getAbsolutePath(), PathConstants.REMOTE_DEVICE_PATH
                        + '/' + miniTouchFile);
                CommandUtil.executeShellCommand(iDevice, String.format(ADBCommandConstants.CHMOD_COMMAND,
                        PathConstants.REMOTE_DEVICE_PATH, miniTouchFile));
            } catch (SyncException e) {
                log.error("initMiniTool SyncException:", e);
            } catch (IOException e) {
                log.error("initMiniTool IOException:", e);
            } catch (AdbCommandRejectedException e) {
                log.error("initMiniTool AdbCommandRejectedException:", e);
            } catch (TimeoutException e) {
                log.error("initMiniTool TimeoutException:", e);
            }
        }
        createForward();
        startMiniTool();
    }

    public void reStartMiniTool() {
        closeThread();
        startMiniTool();
    }

    public void closeMiniTool() {
        removeForward();
        closeThread();
    }

    public void closeIOSMiniCap() {
        ExecuteUtil.removeForward(device.getSerialNumber());
        closeThread();
    }

    private void closeThread() {
        isRunning = false;
        if (dataReaderThread != null) {
            try {
                dataReaderThread.join();
            } catch (InterruptedException e) {
                log.error("DataReaderThread stop wrong");
                dataReaderThread.interrupt();
            }
        }
        if (imageHandleThread != null) {
            try {
                imageHandleThread.join();
            } catch (InterruptedException e) {
                log.error("ImageHandleThread stop wrong");
                imageHandleThread.interrupt();
            }
        }
        if (imageTransferThread != null) {
            try {
                if (channel != null) {
                    channel.close();
                }
                imageTransferThread.join();
            } catch (InterruptedException e) {
                log.error("ImageTransferThread stop wrong");
                imageTransferThread.interrupt();
            }
        }
        if (miniTouchSocket != null) {
            try {
                miniTouchSocket.close();
            } catch (IOException e) {
                log.error("minitouch socket close error");
            }
        }
        if (miniTouchOutputStream != null) {
            try {
                miniTouchOutputStream.close();
            } catch (IOException e) {
                log.error("minitouch outputstream close error");
            }
        }
        if (miniTouchInputStream != null) {
            try {
                miniTouchInputStream.close();
            } catch (IOException e) {
                log.error("minitouch inputstream close error");
            }
        }
        channel = null;
        dataQueue.clear();
    }

    /**
     * Start minicap and minitouch threads.
     */
    private void startMiniTool() {
        new Thread(new MiniToolThread(true)).start();
        new Thread(new MiniToolThread(false)).start();
//        connectMiniTouchSocket();
        new Thread(new StartInitialThread()).start();
    }

    private void startIOSWdaService() {
        new Thread(new IOSWdaServiceThread()).start();
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            log.error("wda thread sleep error");
        }
        new Thread(new IOSWdaProxyThread()).start();
    }

    private void startIOSMinicap() {
        new Thread(new IOSMiniCapThread()).start();
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            log.error("ios minicap thread sleep error");
        }
        new Thread(new StartInitialThread(true)).start();
    }

    private void connectMiniTouchSocket() {
        try {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                log.error("thread sleep error");
            }
            miniTouchSocket = new Socket("127.0.0.1", device.getForwardMiniTouchPort());
            miniTouchInputStream = miniTouchSocket.getInputStream();
            miniTouchOutputStream = miniTouchSocket.getOutputStream();
            byte[] buffer = new byte[4096];
            int realLen=miniTouchInputStream.read(buffer);
            if (buffer.length != realLen) {
                buffer = subByteArray(buffer, 0, realLen);
            }
            String result = new String(buffer);
            String[] array = result.split("\\s|\n");
            banner.setVersion(Integer.parseInt(array[1]));
            banner.setMaxPoint(Integer.parseInt(array[3]));
            banner.setMaxPress(Integer.parseInt(array[6]));
            banner.setMaxX(Integer.parseInt(array[4]));
            banner.setMaxY(Integer.parseInt(array[5]));
        } catch (IOException e) {
            log.error("minitouch socket connect error");
        }
    }

    private boolean isMiniCapInstalled() {
        String installedCommand = String.format(ADBCommandConstants.MINICAP_INSTALLED, device.getResolution(), device.getResolution(), 0);
        String output = CommandUtil.executeShellCommand(iDevice, installedCommand);
        return output.startsWith("{");
    }

    private void createForward() {
        try {
//            device.setForwardMiniCapPort(PortManagementUtil.useFirstMiniCapPort());
            device.setForwardMiniCapPort(12345);
            device.setForwardMiniTouchPort(PortManagementUtil.useFirstMiniTouchPort());
            log.info("port:{} {}", device.getForwardMiniCapPort(), device.getForwardMiniTouchPort());
            iDevice.createForward(device.getForwardMiniCapPort(), "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            iDevice.createForward(device.getForwardMiniTouchPort(), "minitouch", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (IOException e) {
            log.error("createForward IOException:", e);
        } catch (AdbCommandRejectedException e) {
            log.error("createForward AdbCommandRejectedException:", e);
        } catch (TimeoutException e) {
            log.error("createForward TimeoutException:", e);
        }
    }

    private void removeForward() {
        try {
            iDevice.removeForward(device.getForwardMiniCapPort(), "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            iDevice.removeForward(device.getForwardMiniTouchPort(), "minitouch", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            PortManagementUtil.recoverMiniCapPort(device.getForwardMiniCapPort());
            PortManagementUtil.recoverMiniTouchPort(device.getForwardMiniTouchPort());
            device.setForwardMiniCapPort(0);
            device.setForwardMiniTouchPort(0);
        } catch (IOException e) {
            log.error("createForward IOException:", e);
        } catch (AdbCommandRejectedException e) {
            log.error("createForward AdbCommandRejectedException:", e);
        } catch (TimeoutException e) {
            log.error("createForward TimeoutException:", e);
        }
    }

    private byte[] byteMerger(byte[] byte1, byte[] byte2) {
        byte[] byte3 = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, byte3, 0, byte1.length);
        System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
        return byte3;
    }

    private byte[] subByteArray(byte[] byte1, int start, int end) {
        byte[] byte2 = new byte[0];
        try {
            byte2 = new byte[end - start];
        } catch (NegativeArraySizeException e) {
            log.error("subByteArray error:", e);
        }
        System.arraycopy(byte1, start, byte2, 0, end - start);
        return byte2;
    }

    class IOSMiniCapThread implements Runnable {
        @Override
        public void run() {
            ExecuteUtil.executeIOSMinicap(device.getSerialNumber());
        }
    }

    class IOSWdaServiceThread implements Runnable {
        @Override
        public void run() {
            ExecuteUtil.executeIOSWda(device.getSerialNumber());
        }
    }

    class IOSWdaProxyThread implements Runnable {
        @Override
        public void run() {
            ExecuteUtil.executeIOSWdaProxy(device.getSerialNumber());
        }
    }

    /**
     * Start minicap and minitouch threads.
     */
    class MiniToolThread implements Runnable {

        private final boolean isMiniCap;

        MiniToolThread(boolean isMiniCap) {
            this.isMiniCap = isMiniCap;
        }

        @Override
        public void run() {
            String startMiniCmd;
            if (isMiniCap) {
                startMiniCmd = String.format(ADBCommandConstants.MINICAP_START, miniCapFile,
                        device.getResolution(), device.getResolution(), 0);
            } else {
                startMiniCmd = String.format(ADBCommandConstants.MINITOUCH_START, miniTouchFile);
            }
            log.info("mini start:{}", startMiniCmd);
            CommandUtil.executeShellCommand(iDevice, startMiniCmd);
        }
    }

    /**
     * Test the connection to minicap.
     */
    class StartInitialThread implements Runnable {

        private boolean isIOS = false;

        StartInitialThread() {
        }

        StartInitialThread(boolean isIOS) {
            this.isIOS = isIOS;
        }

        @Override
        public void run() {
            log.info("minicap socket connect test start");
            try {
                byte[] bytes;
                int len = 4096;
                int tryTime = 50;
                InputStream inputStream;
                while (tryTime > 0) {
                    Socket socket = new Socket("127.0.0.1", device.getForwardMiniCapPort());
                    inputStream = socket.getInputStream();
                    bytes = new byte[len];
                    log.info("ios socket img len:{}", bytes.length);
                    int readLen = inputStream.read(bytes);
                    if (readLen == -1) {
                        Thread.sleep(10);
                        socket.close();
                        inputStream.close();
                    } else {
                        dataQueue.add(Arrays.copyOfRange(bytes, 0, readLen));
                        isRunning = true;
                        imageTransferThread = new Thread(new ImageTransferThread());
                        imageTransferThread.start();
                        dataReaderThread = new Thread(new DataReaderThread(socket));
                        dataReaderThread.start();
                        if (isIOS) {
                            imageHandleThread = new Thread(new ImageHandleThread(true));
                        } else {
                            imageHandleThread = new Thread(new ImageHandleThread());
                        }
                        imageHandleThread.start();
                        break;
                    }
                    tryTime--;
                }
                log.info("minicap socket connect test end");
            } catch (IOException | InterruptedException e) {
                log.error("StartInitialThread Exception: ", e);
            }
        }
    }

    /**
     * Read the data from minicap.
     */
    class DataReaderThread implements Runnable {

        private static final int BUFF_SIZE = 4096;
        private final Socket socket;
        private InputStream inputStream = null;

        DataReaderThread(Socket socket) {
            this.socket = socket;
            try {
                this.inputStream = socket.getInputStream();
            } catch (IOException e) {
                log.error("minicap get socket inputstream fail");
            }
        }

        @Override
        public void run() {
            log.info("image data reader start");
            try {
                while (isRunning) {
                    byte[] buffer = new byte[BUFF_SIZE];
                    int readLen = inputStream.read(buffer);
                    if (readLen == -1) {
                        return;
                    }
                    if (readLen != BUFF_SIZE) {
                        buffer = subByteArray(buffer, 0, readLen);
                    }
                    dataQueue.add(buffer);
                }
            } catch (IOException e) {
                log.error("minicap inputstream read fail");
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        log.error("minicap socket close error");
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("minicap inputstream close error");
                    }
                }
                log.info("image data reader end");
            }
        }
    }

    /**
     * Process the data from minicap and send it to the main platform.
     */
    class ImageHandleThread implements Runnable {

        private int readBannerBytes = 0;
        private int bannerLength = 2;
        private int readFrameBytes = 0;
        private int frameBodyLength = 0;
        private byte[] frameBody = new byte[0];
        private int oneFifthCycle = 0;
        private boolean isIOS = false;

        public ImageHandleThread() {
        }

        public ImageHandleThread(boolean isIOS) {
            this.isIOS = isIOS;
        }

        @Override
        public void run() {
            log.info("image parse and transfer start");
            while (isRunning) {
                byte[] buffer = new byte[0];
                try {
                    buffer = dataQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int length = buffer.length;
                for (int cursor = 0; cursor < length; ) {
                    int ch = buffer[cursor] & 0xff;
                    if (readBannerBytes < bannerLength) {
                        cursor = parserBanner(cursor, ch);
                    } else if (readFrameBytes < 4) {
                        // the sum of the beginning 4 numbers = frame buffer size
                        frameBodyLength += (ch << (readFrameBytes * 8));
                        cursor++;
                        readFrameBytes++;
                    } else {
                        if (length - cursor >= frameBodyLength) {
                            byte[] subByte = subByteArray(buffer, cursor, cursor + frameBodyLength);
                            frameBody = byteMerger(frameBody, subByte);
                            if (frameBody[0] != -1 || frameBody[1] != -40) {
                                log.error("Frame body does not start with JPG header");
                                return;
                            }
                            final byte[] finalBytes = subByteArray(frameBody, 0, frameBody.length);
                            // Mark: this info msg pollutes the output.
//                            log.info("imgLen:{}", finalBytes.length);
                            if (channel != null) {
                                if (isIOS) {
                                    oneFifthCycle++;
                                    if (oneFifthCycle != 0 && (oneFifthCycle % 5 == 0 || oneFifthCycle > 6)) {
                                        channel.writeAndFlush(Unpooled.copiedBuffer(finalBytes));
                                        oneFifthCycle = 0;
                                    }
                                } else {
                                    channel.writeAndFlush(Unpooled.copiedBuffer(finalBytes));
                                }
                            }

                            cursor += frameBodyLength;
                            frameBodyLength = 0;
                            readFrameBytes = 0;
                            frameBody = new byte[0];
                        } else {
                            byte[] subByte = subByteArray(buffer, cursor, length);
                            frameBody = byteMerger(frameBody, subByte);
                            frameBodyLength -= (length - cursor);
                            readFrameBytes += (length - cursor);
                            cursor = length;
                        }
                    }
                }
            }
            log.info("image parse end");
        }

        private int parserBanner(int cursor, int ch) {
            switch (readBannerBytes) {
                case 0:
                    // version
                    banner.setVersion(ch);
                    break;
                case 1:
                    // length
                    bannerLength = ch;
                    banner.setLength(ch);
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    // pid
                    int pid = banner.getPid();
                    pid += (ch << ((readBannerBytes - 2) * 8));
                    banner.setPid(pid);
                    break;
                case 6:
                case 7:
                case 8:
                case 9:
                    // real width
                    int realWidth = banner.getReadWidth();
                    realWidth += (ch << ((readBannerBytes - 6) * 8));
                    banner.setReadWidth(realWidth);
                    break;
                case 10:
                case 11:
                case 12:
                case 13:
                    // real height
                    int realHeight = banner.getReadHeight();
                    realHeight += (ch << ((readBannerBytes - 10) * 8));
                    banner.setReadHeight(realHeight);
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                    // virtual width
                    int virtualWidth = banner.getVirtualWidth();
                    virtualWidth += (ch << ((readBannerBytes - 14) * 8));
                    banner.setVirtualWidth(virtualWidth);
                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                    // virtual height
                    int virtualHeight = banner.getVirtualHeight();
                    virtualHeight += (ch << ((readBannerBytes - 18) * 8));
                    banner.setVirtualHeight(virtualHeight);
                    break;
                case 22:
                    // orientation
                    banner.setOrientation(ch * 90);
                    break;
                case 23:
                    // quirks
                    banner.setQuirks(ch);
                    break;
            }

            cursor += 1;
            readBannerBytes += 1;

            if (readBannerBytes == bannerLength) {
                log.info("Banner:[version={}, length={}, pid={}, readWidth={}, readHeight={}, virtualWidth={}, " +
                                "virtualHeight={}, orientation={}, quirks={}", banner.getVersion(), banner.getLength(),
                        banner.getPid(), banner.getReadWidth(), banner.getReadHeight(), banner.getVirtualWidth(),
                        banner.getVirtualHeight(), banner.getOrientation(), banner.getQuirks());
            }
            return cursor;
        }
    }

    /**
     * Start the netty client to connect to the main platform.
     */
    class ImageTransferThread implements Runnable {
        @Override
        public void run() {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                ChannelPipeline ch = socketChannel.pipeline();
                                ch.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                                ch.addLast(new LengthFieldPrepender(4));
                                ch.addLast(new AndroidControlHandler(device.getSerialNumber(), miniTouchOutputStream));
                            }
                        });

                ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
                channel = channelFuture.channel();
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("startImageTransfer InterruptedException", e);
                Thread.currentThread().interrupt();
            } finally {
                eventLoopGroup.shutdownGracefully();
                channel = null;
                log.info("image data transfer end");
            }
        }
    }
}
