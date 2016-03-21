/*
 * ----------------------------------------------------------------------
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * ----------------------------------------------------------------------
 */

package org.jenkins.support.sectionview

/**
 * Created by eyinsma on 2016/3/21.
 */
class ShellExecutor {
    def out

    ShellExecutor(out){
        this.out = out
    }

    public def executeShell(String command, String workDir, boolean isOutputEnable) {
        return executeOnShell(command, new File(workDir), isOutputEnable)
    }

    private def executeOnShell(String command, File workingDir, isOutputEnable) {
        out.println command
        def pb = new ProcessBuilder(addShellPrefix(command))
                .directory(workingDir)
                .redirectErrorStream(true)

        if (!isOutputEnable) {
            pb.redirectOutput(new File('/dev/null'))
        }

        def process = pb.start()
        if (isOutputEnable) {
            process.inputStream.eachLine { out.println it }
        }

        process.waitFor();
        return process.exitValue()
    }

    private def addShellPrefix(String command) {
        def commandArray = new String[3]
        commandArray[0] = "sh"
        commandArray[1] = "-c"
        commandArray[2] = command
        return commandArray
    }
}
