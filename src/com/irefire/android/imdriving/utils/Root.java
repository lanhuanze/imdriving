package com.irefire.android.imdriving.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Root {

	private static final Logger l = LoggerFactory.getLogger(Root.class);

    public boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    public boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    public boolean checkRootMethod2() {
        try {
            File file = new File("/system/app/Superuser.apk");
            return file.exists();
        } catch (Exception e) {
        	l.warn("/system/app/Superuser.apk no found:" + e.getMessage());
        } return false;
    }

    public boolean checkRootMethod3() {
        return new ExecShell().executeCommand(ExecShell.SHELL_CMD.check_su_binary)!=null;
    }
    
    public boolean checkRootMethod4() {
    	
    }
    
    private static class ExecShell {


        public static enum SHELL_CMD {
            check_su_binary(new String[] { "/system/xbin/which", "su" });

            String[] command;

            SHELL_CMD(String[] command) {
                this.command = command;
            }
        }

        public ArrayList<String> executeCommand(SHELL_CMD shellCmd) {
            String line = null;
            ArrayList<String> fullResponse = new ArrayList<String>();
            Process localProcess = null;
            try {
                localProcess = Runtime.getRuntime().exec(shellCmd.command);
            } catch (Exception e) {
                return null;
            }
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    localProcess.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    localProcess.getInputStream()));
            try {
                while ((line = in.readLine()) != null) {
                    l.debug("--> Line received: " + line);
                    fullResponse.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
           	l.debug("--> Full response was: " + fullResponse);
            return fullResponse;
        }
    }
}   


