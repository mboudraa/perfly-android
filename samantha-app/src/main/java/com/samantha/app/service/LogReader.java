package com.samantha.app.service;

import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;
import timber.log.Timber;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public final class LogReader {

    private LogReader() {
    }

    //dalvikvm:D InputReader:I ActivityManager:I
    public static void readLog(final String filter, final LogCallback callback) {

        final String command = String.format("logcat -d time %s", filter);
        try {
            executeShellCommand(
                    new Command(1, command) {

                        @Override
                        public void commandOutput(int id, String line) {
                            if (callback != null) {
                                callback.onLogOutput(line);
                            }
                        }

                        @Override
                        public void commandTerminated(int id, String reason) {
                            Timber.w("End Reading Log cause -> %s", reason);
                        }

                        @Override
                        public void commandCompleted(int id, int exitCode) {

                        }
                    });

            clearShell();

        } catch (IOException e) {
            Timber.e(e, "Error reading log");
        } catch (RootDeniedException e) {
            Timber.e(e, "Error reading log");
        }

    }

    private static void clearShell() throws IOException, RootDeniedException {
        executeShellCommand(new Command(0, "logcat -c") {
            @Override
            public void commandOutput(int i, String s) {

            }

            @Override
            public void commandTerminated(int i, String s) {

            }

            @Override
            public void commandCompleted(int i, int i1) {

            }
        });
    }

    private static void executeShellCommand(Command command) throws IOException, RootDeniedException {

        try {
            Shell.runRootCommand(command);
        } catch (TimeoutException e) {
            executeShellCommand(command);
        } catch (Exception e) {
            Timber.w("Impossible to execute shell command as root");
        }
    }

    private static void clearLogcat() throws IOException {
        Runtime.getRuntime().exec("logcat -c");
    }


    public static interface LogCallback {
        void onLogOutput(String line);
    }
}
