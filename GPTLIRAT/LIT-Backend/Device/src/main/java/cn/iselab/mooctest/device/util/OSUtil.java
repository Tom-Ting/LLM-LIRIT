package cn.iselab.mooctest.device.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OSUtil {
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
//            pb.redirectErrorStream(true);
            Process proc = pb.start();
            is = proc.getInputStream();
            isReader = new InputStreamReader(is, StandardCharsets.UTF_8);

            // Append errors to log output.
            try (BufferedReader esReader = new BufferedReader(
                    new InputStreamReader(proc.getErrorStream()))) {
                String errorMsg;
                while ((errorMsg = esReader.readLine()) != null) {
                    logger.error(errorMsg);
                }
            }

            br = new BufferedReader(isReader);
            String result = "";
            while ((line = br.readLine()) != null) {
//                System.out.println(line);
                result += (line + "\n");
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
        if (command.contains("|") && command.contains("grep")) {
            List<String> commandList = new ArrayList<>();
            commandList.add("/bin/sh");
            commandList.add("-c");
            commandList.add(command);
            String[] commands = new String[commandList.size()];
            commandList.toArray(commands);
            return runCommand(commands);
        } else {
            return runCommand(command.split(" "));
        }

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

}
