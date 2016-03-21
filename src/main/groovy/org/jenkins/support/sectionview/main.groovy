import org.jenkins.support.sectionview.JobConfiguration
import org.jenkins.support.sectionview.SectionViewRunner

println "--------start----------"

//check job status, if job is running, abort.
def myJobName = build.getEnvironment(listener).get('JOB_NAME')

//init
def jobConf = init()
def out = jobConf['out']
def jobConfiguration = JobConfiguration.createJobConfiguration(
        myJobName,
        getCurrentJobParameter("IsDryRun"),
        getCurrentJobParameter("JobGroupSequence"),
        getCurrentJobParameter("AssociatedViewName")
)

SectionViewRunner sectionViewRunner = new SectionViewRunner(out, build, jobConfiguration)
sectionViewRunner.execute()

//build.result = Result.SUCCESS
println "--------stop----------"

def String getCurrentJobParameter(String paraName) {
    def v = build.buildVariableResolver.resolve(paraName)
    println( "parameters: $paraName : $v")
    return v
}


def init() {
    def config = new HashMap()
    def bindings = getBinding()
    config.putAll(bindings.getVariables())
    return config
}
