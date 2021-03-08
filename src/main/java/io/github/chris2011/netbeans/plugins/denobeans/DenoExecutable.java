package io.github.chris2011.netbeans.plugins.denobeans;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.project.Project;
import io.github.chris2011.netbeans.plugins.denobeans.options.FileUtils;
import io.github.chris2011.netbeans.plugins.denobeans.options.StringUtils;
import java.util.Arrays;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class DenoExecutable {
    public static final String DENO_EXECUTABLE_NAME;

//    private static final String INPUT_FILE_PARAM = "--js"; // NOI18N
//    private static final String OUTPUT_FILE_PARAM = "--js_output_file"; // NOI18N
//    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.netbeans/minifierbeans/custom-packages";
    protected final Project project;
    protected final String denoExecutablePath;

    static {
        if (Utilities.isWindows()) {
            DENO_EXECUTABLE_NAME = "/deno.exe"; // NOI18N
        } else {
            DENO_EXECUTABLE_NAME = "/deno"; // NOI18N
        }
    }

    private static final String OPTIONS_PATH = "HTML5/Deno";

    DenoExecutable(String denoExecutablePath, @NullAllowed Project project) {
        assert denoExecutablePath != null;

        this.denoExecutablePath = denoExecutablePath;
        this.project = project;
    }

    @CheckForNull
    public static DenoExecutable getDefault(@NullAllowed Project project) {
        return createExecutable(DENO_EXECUTABLE_NAME, project);
    }

    private static DenoExecutable createExecutable(String denoExecutable, Project project) {
        if (Utilities.isMac()) {
            return new DenoExecutable.MacDenoExecutable(denoExecutable, project);
        }
        return new DenoExecutable(denoExecutable, project);
    }

    String getCommand() {
        return denoExecutablePath;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "DenoExecutable.generate=Deno executable ({0})",})
    public Future<Integer> generate(File inputFile, File outputFile, String compilerFlags) {
        assert !EventQueue.isDispatchThread();
        assert project != null;

        Future<Integer> task = getExecutable("Minification in progress")
                .additionalParameters(getGenerateParams(inputFile, outputFile, compilerFlags))
                .run(getDescriptor());

        assert task != null : denoExecutablePath;
        return task;
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;

        return new ExternalExecutable(getCommand())
                .workDir(getWorkDir())
                .displayName(title)
                .optionsPath(OPTIONS_PATH)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        assert project != null;

        return ExternalExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(OPTIONS_PATH)
                .outLineBased(true)
                .errLineBased(true)
                .postExecution(() -> {
                    project.getProjectDirectory().refresh();
        });
    }

    private File getWorkDir() {
        if (project == null) {
            return FileUtils.TMP_DIR;
        }

        File workDir = FileUtil.toFile(project.getProjectDirectory());

        assert workDir != null : project.getProjectDirectory();

        return workDir;
    }

    private List<String> getGenerateParams(File inputFile, File outputFile, String compilerFlags) {
        List<String> params = new ArrayList<>();

        if (!compilerFlags.isEmpty()) {
            String[] splittedCompilerFlags = compilerFlags.split("; ");

            for (String splittedCompilerFlag : splittedCompilerFlags) {
                String[] splittedKeyAndValue = splittedCompilerFlag.split(" ");

                params.addAll(Arrays.asList(splittedKeyAndValue));
            }
        }

//        params.add(INPUT_FILE_PARAM);
        params.add(inputFile.getAbsolutePath().replace("\\", "/"));
//        params.add(OUTPUT_FILE_PARAM);
//        params.add(outputFile.getAbsolutePath().replace("\\", "/"));

        return getParams(params);
    }

    List<String> getParams(List<String> params) {
        assert params != null;

        return params;
    }

    //~ Inner classes
    private static final class MacDenoExecutable extends DenoExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N

        MacDenoExecutable(String denoExecutablePath, Project project) {
            super(denoExecutablePath, project);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getParams(List<String> params) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(denoExecutablePath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtils.implode(super.getParams(params), "\" \"")); // NOI18N
            sb.append("\""); // NOI18N

            return Collections.singletonList(sb.toString());
        }

    }

}
