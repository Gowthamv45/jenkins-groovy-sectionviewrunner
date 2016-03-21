
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
