package io.github.chris2011.netbeans.plugins.denobeans;

import java.awt.EventQueue;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.openide.util.Utilities;

public class InstallerExecutable {
    public static final String DENO_EXECUTABLE_NAME;

    protected final String denoExecutablePath;

    static {
        if (Utilities.isWindows()) {
            DENO_EXECUTABLE_NAME = "/deno.exe"; // NOI18N
        } else {
            DENO_EXECUTABLE_NAME = "/deno"; // NOI18N
        }
    }

    private static final String OPTIONS_PATH = "HTML5/Deno";

    InstallerExecutable(String denoExecutablePath) {
        assert denoExecutablePath != null;

        this.denoExecutablePath = denoExecutablePath;
    }

    @CheckForNull
    public static InstallerExecutable getDefault() {
        return createExecutable(DENO_EXECUTABLE_NAME);
    }

    private static InstallerExecutable createExecutable(String denoExecutable) {
        if (Utilities.isMac()) {
            return new InstallerExecutable.MacInstallerExecutable(denoExecutable);
        }
        return new InstallerExecutable(denoExecutable);
    }

    String getCommand() {
        return denoExecutablePath;
    }

    public Future<Integer> generate() {
        assert !EventQueue.isDispatchThread();

        Future<Integer> task = getExecutable("Installation in progress")
                .run(getDescriptor());

        assert task != null : denoExecutablePath;
        return task;
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;

        return new ExternalExecutable(getCommand())
//                .workDir(getWorkDir())
                .displayName(title)
                .optionsPath(OPTIONS_PATH)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        return ExternalExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(OPTIONS_PATH)
                .outLineBased(true)
                .errLineBased(true);
    }

    //~ Inner classes
    private static final class MacInstallerExecutable extends InstallerExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N

        MacInstallerExecutable(String denoExecutablePath) {
            super(denoExecutablePath);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

//        @Override
//        List<String> getParams(List<String> params) {
//            StringBuilder sb = new StringBuilder(200);
//            sb.append("\""); // NOI18N
//            sb.append(denoExecutablePath);
//            sb.append("\" \""); // NOI18N
//            sb.append(StringUtils.implode(super.getParams(params), "\" \"")); // NOI18N
//            sb.append("\""); // NOI18N
//
//            return Collections.singletonList(sb.toString());
//        }
    }

}
