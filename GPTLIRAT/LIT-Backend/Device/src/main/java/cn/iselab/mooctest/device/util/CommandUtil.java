package cn.iselab.mooctest.device.util;

import com.android.ddmlib.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CommandUtil {

    private static Logger log = LoggerFactory.getLogger(CommandUtil.class);

    private CommandUtil() {
    }

    public static String executeShellCommand(IDevice device, String command) {
        CollectingOutputReceiver output = new CollectingOutputReceiver();
        try {
            device.executeShellCommand(command, output, 0, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("TimeoutException:{}", e);
        } catch (AdbCommandRejectedException e) {
            log.error("AdbCommandRejectedException:{}", e);
        } catch (ShellCommandUnresponsiveException e) {
            log.error("ShellCommandUnresponsiveException:{}", e);
        } catch (IOException e) {
            log.error("IOException:{}", e);
        }
        return output.getOutput();
    }

}
