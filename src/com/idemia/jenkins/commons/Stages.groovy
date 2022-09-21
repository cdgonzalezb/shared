#!/usr/bin/env groovy
package com.idemia.jenkins.commons
import groovy.json.JsonSlurper;
import java.lang.String

class Stages{

    private final Script scriptPL

        Stages(Script scriptPL) {
        this.scriptPL = scriptPL
    }

    String urlShareLibrary = "https://r11-sfdh-scm-001.mphr11.morpho.com/bitbucket/scm/psi/co-id-dvop-dops.git"
   
    //General stages
    def business = new Business(scriptPL)

    void stgCheckoutSCM(project){
        scriptPL.stage("Checkout SCM"){
            scriptPL.cleanWs()
            scriptPL.checkout scriptPL.scm
        }
    }
           
    void stgReadParameters(projectName) {
        scriptPL.stage("Read Parameters"){
            def project = business.getProjectInfo(projectName, urlShareLibrary)
            return project
        }
    }

    void stgQualityGate(project){
        scriptPL.stage("Quality Gate"){
            if (project.stgSQualityGate == "true") {
                scriptPL.sleep 60
                scriptPL.timeout(time: 5, unit: 'MINUTES') {
                    def qg = scriptPL.waitForQualityGate()   
                    if (qg.status != 'OK') {
                        scriptPL.error "Pipeline aborted due to quality gate failure: ${qg.status}"
                    }
                }
            }
            else {
                scriptPL.echo "the project do not have quality gate!"
            }
        }
    }

    //Nexus
    void stgUploadArtifactNexus(projectName, pathArtifact){
        scriptPL.stage('Artifact Uploader'){
            business.uploadArtifactNexus(nexusVersion,nexusUrl, nexusGroupId, nexusRepository + scriptPL.env.JOB_BASE_NAME, nexusCredentialsId, projectName, pathArtifact)
        }
    }

    void getEnvironment(project) {
        def projectUCD  = [:]
        switch (scriptPL.env.JOB_BASE_NAME) {
            case "dev":
                projectUCD = project.deploy.dev
                break
            case "test":
                projectUCD = project.deploy.test
                break
            case "master":
                projectUCD = project.deploy.prd
                break
            default:
                break
        }
        return projectUCD
    }

    void getConfigs(project) {
        def configs  = [:]
        configs = project.config.files
        return configs
    }

    void getConfigFileName(project) {
        def fileName  = [:]
        switch (scriptPL.env.JOB_BASE_NAME) {
            case "dev":
                fileName = "dev." + project.config.configFileName
                break
            case "test":
                fileName = "test." + project.config.configFileName
                break
            case "master":
                fileName = "prd." + project.config.configFileName
                break
            default:
                break
        }
        return fileName
    }

    void getTestInfo(project) {
        def projectUCD  = [:]
        switch (scriptPL.env.JOB_BASE_NAME) {
            case "dev":
                projectUCD = project.test.dev
                break
            case "test":
                projectUCD = project.test.test
                break
            case "master":
                projectUCD = project.test.prd
                break
            default:
                break
        }
        return projectUCD
    }

    void getRemoteServer(projectUCD){
        def remote = [:]
        remote.name = projectUCD.name
        remote.host = projectUCD.host
        remote.user = projectUCD.user
        remote.password = projectUCD.password
        remote.allowAnyHosts = true
        return remote
    }
}




