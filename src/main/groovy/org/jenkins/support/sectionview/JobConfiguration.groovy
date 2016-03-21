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
class JobConfiguration {
    public String myJobName
    public boolean isDryRun
    public List<String> jobExecutionNameList
    public String jobViewName

    private def out

    private JobConfiguration(){}

    public static JobConfiguration createJobConfiguration(String myJobName, String paraIsDryRun, String paraJobExecutionList, String paraJobViewName){
        JobConfiguration jobConfiguration = new JobConfiguration();
        jobConfiguration.myJobName = myJobName
        jobConfiguration.setDryRun(paraIsDryRun)
        jobConfiguration.parseJobSequenceList(paraJobExecutionList)
        jobConfiguration.jobViewName = paraJobViewName
    }


    private void parseJobSequenceList(String jobSeq) {
        String[] strs;
        if (jobSeq.contains("->")) {
            strs = jobSeq.split("->")
        } else if (jobSeq.contains(",")) {
            strs = jobSeq.split(",")
        } else if (jobSeq.contains(";")) {
            strs = jobSeq.split(";")
        } else if (jobSeq.contains(":")) {
            strs = jobSeq.split(":")
        }else{
            jobExecutionNameList = new ArrayList<String>()
            jobExecutionNameList.add(jobSeq)
        }

        jobExecutionNameList = Arrays.asList(strs)
    }


    private void setDryRun(String s) {
        if (s == null){
            return
        }
        if (s.compareToIgnoreCase("Yes") == 0) {
            isDryRun = true
        } else {
            isDryRun = false
        }
    }
}
