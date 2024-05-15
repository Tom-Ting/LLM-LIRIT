package cn.iselab.mooctest.lit.util;

import cn.iselab.mooctest.lit.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Component
@Slf4j
public class OSUtil {
    @Autowired
    private Environment environment;

    @Autowired
    RecordService recordService;

    private static final Logger logger = LoggerFactory.getLogger(OSUtil.class);

    public static boolean isWin() {
        String os = System.getProperty("os.name");
        boolean isWin = os.startsWith("win") || os.startsWith("Win");
        return isWin;
    }

    public static String getCmd() {
        if (isWin()) {
            return "cmd";
        } else {
            return "bash";
        }
    }

    public static void runCommandAsyn(String command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String exec(String command) {
        StringBuilder returnString = new StringBuilder();
        Runtime runTime = Runtime.getRuntime();
        if (runTime == null) {
            logger.error("Create runtime false!");
            return "Create runtime false!";
        }
        try {
            Process pro = runTime.exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                returnString.append(line + "\n");
            }
            input.close();
            output.close();
            pro.destroy();
        } catch (IOException ex) {
            logger.error("IOException error, cause:{}", ex.getMessage());
        }
        return returnString.toString();
    }

    static public void executeCommand(String[] commandArr) {
        print("Linux command: "
                + java.util.Arrays.toString(commandArr));
        try {
            ProcessBuilder pb = new ProcessBuilder(commandArr);
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            print("Process started !");

            String line;
            while ((line = in.readLine()) != null) {
                print(line);
            }

            proc.destroy();
            print("Process ended !");
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static String runCommand(String[] commands) {
        if (commands == null || commands.length == 0) {
            return null;
        }
        BufferedReader br = null;
        String line = null;
        InputStream is = null;
        InputStreamReader isReader = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            Process proc = pb.start();
            is = proc.getInputStream();
            isReader = new InputStreamReader(is, "utf-8");
            br = new BufferedReader(isReader);
            String result = "";
            while ((line = br.readLine()) != null) {
                result += (line + "\n");
            }
	    InputStream es = proc.getErrorStream();
	    InputStreamReader esReader = new InputStreamReader(es, "utf-8");
	    br = new BufferedReader(esReader);
            while ((line = br.readLine()) != null) {
		logger.error(line);
            }
            proc.destroy();
            return result;
        } catch (IOException e) {
            logger.error("IOException caused by:{}", e.getMessage());
            e.printStackTrace();
            return line;
        } finally {
            if (isReader != null) {
                try {
                    isReader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO
                }
            }
        }
    }

    public static String runCommand(String command) {
        return runCommand(command.split(" "));
    }

    private static void print(String msg) {
        String TAG = Thread.currentThread().getStackTrace()[1].getClassName();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss,SSS");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(df.format(date) + " " + TAG + " - " + msg);
    }

    public static Process exec(List<String> args) throws IOException {
        String[] arrays = new String[args.size()];

        for (int i = 0; i < args.size(); i++) {
            arrays[i] = args.get(i);
        }
        return Runtime.getRuntime().exec(arrays);
    }

    public void sleep(int replaySleepSeconds){
        try {
            TimeUnit.SECONDS.sleep(replaySleepSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void logAndMessage(String s){
        log.info(s);
        recordService.setMessage(s);
    }
}
