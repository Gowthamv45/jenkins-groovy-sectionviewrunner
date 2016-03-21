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

import hudson.console.HyperlinkNote
import hudson.model.Cause
import hudson.model.Hudson
import hudson.model.ParametersAction
import hudson.model.Result
import hudson.model.StringParameterValue
import hudson.plugins.sectioned_view.SectionedViewSection
import jenkins.model.Jenkins

import java.util.concurrent.CancellationException

/**
 * Created by eyinsma on 2016/3/21.
 */
class SectionViewRunner {
    private def out
    private def build
    private JobConfiguration jobConfiguration

    private Map<String, List<String>> sourceJobConfig;
    private Map<String, List<String>> jobSectionNameMap;

    SectionViewRunner(out, build, JobConfiguration jobConfiguration) {
        this.out = out
        this.build = build
        this.jobConfiguration = jobConfiguration
    }

    public void execute(){
        //get source view configuration
        sourceJobConfig = getJobConfigFromView(jobConfiguration.jobViewName)
        if (sourceJobConfig.isEmpty()) {
            out.println "No job found in view: $jobConfiguration.jobViewName"
        }

        //filter the job group name
        jobSectionNameMap = getFinalJobList(jobConfiguration.jobExecutionNameList, sourceJobConfig)


        for (String jobGroupName : jobConfiguration.jobExecutionNameList) {
            List<String> jobSectionList = jobSectionNameMap.get(jobGroupName)
            executeJobs(jobSectionList, sourceJobConfig)
        }
    }


    def executeJobs(jobNameList, jobConfig) {
        for (jobSection in jobNameList) {
            out.println "Ready to run section $jobSection"
            List jobNameListPerSection = jobConfig.get(jobSection)

            jobNameListPerSection.each {
                def par = null
                //par = createParameterActions()
                boolean isSuccess = buildAndWait(build, it, out, par)
            }
        }
    }


    def boolean buildAndWait(parentBuild, jobName, out, params) {
        // Start another job
        def job = Hudson.instance.getJob(jobName)
        if (job == null) {
            out.println "!!" + jobName + " not exist!"
            return false
        }

        if (jobConfiguration.isDryRun) {
            out.println "------------- [ $jobName ] executed -------------"
            return true
        }

        def anotherBuild
        try {
            def future
            if (params == null) {
                future = job.scheduleBuild2(0, new Cause.UpstreamCause(parentBuild))
            } else {
                future = job.scheduleBuild2(0, new Cause.UpstreamCause(parentBuild), params)
            }
            out.println "Waiting for the completion of " + HyperlinkNote.encodeTo('/' + job.url, job.fullDisplayName)
            anotherBuild = future.get()
        } catch (CancellationException x) {
            out.println jobName + " is aborted"
            return false
        } catch (Exception otherX) {
            out.println jobName + " exception!"
            return false
        }
        out.println HyperlinkNote.encodeTo('/' + anotherBuild.url, anotherBuild.fullDisplayName) + " completed. Result was " + anotherBuild.result

        if (anotherBuild.result == Result.SUCCESS) {
            return true
        } else {
            return false
        }
    }

    def createParameterActions(Map<String, String> paras) {
        def inputParams = new ArrayList<StringParameterValue>()
        paras.each {
            k, v -> inputParams.add(new StringParameterValue(k, v))
        }
        return new ParametersAction(inputParams);
    }

    private Map<String, List<String>> getFinalJobList(List<String> jobGroupNameList, Map jobConfig) {
        Map<String, List<String>> mappedSectionName = new HashMap<String, List<String>>()
        Set<String> addedKeys = new HashSet<String>()
        for (int i = 0; i < jobGroupNameList.size(); i++) {
            List<String> mappedKeys = new ArrayList<>();

            for (String key : jobConfig.keySet()) {

                if (key.toUpperCase().contains(jobGroupNameList.getAt(i).toUpperCase())) {
                    if (!addedKeys.contains(key)) {
                        mappedKeys.add(key)
                    }
                }
            }
            mappedSectionName.put(jobGroupNameList.getAt(i), sortListBasedonNumber(mappedKeys))
        }

        return mappedSectionName;
    }

    private Map<String, List<String>> getJobConfigFromView(String viewName) {
        def sections = getSectionFromView(viewName)
        if (sections == null) {
            return Collections.emptyMap()
        }

        Map<String, List<String>> sectionGroupMap = new HashMap<String, List<String>>()
        for (SectionedViewSection section : sections) {

            List<String> jobList = new ArrayList<String>();
            for (item in section.getItems(Jenkins.getInstance())) {
                jobList.add(item.getName())
            }

            if (jobList.isEmpty()) {
                continue
            }

            sectionGroupMap.put(section.getName(), sortListBasedonNumber(jobList))
        }

        return sectionGroupMap
    }

    //sort based on number first, then normal sequence.
    private List<String> sortListBasedonNumber(List<String> sourceList) {
        List<String> newList = new ArrayList<String>();
        List<String> numberList = sourceList.findAll { it =~ /(\d+)/ }
        numberList.sort(true, { a, b -> Integer.parseInt((a =~ /(\d+)/)[0][1]) <=> Integer.parseInt((b =~ /(\d+)/)[0][1]) })
        sourceList.removeAll(numberList)
        sourceList.sort()
        newList.addAll(numberList)
        newList.addAll(sourceList)
        return newList
    }


    private def getSectionFromView(viewName) {
        def v = hudson.model.Hudson.instance.getView(viewName)
        if (v != null) {
            return v.getSections()
        } else return null;
    }


}
