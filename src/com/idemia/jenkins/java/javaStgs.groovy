#!/usr/bin/env groovy
package com.idemia.jenkins.java
import java.lang.String

class javaStgs {

    private final Script scriptPL
    
    javaStgs(Script scriptPL) {
        this.scriptPL = scriptPL
    }

    def javaFuncs = new com.idemia.jenkins.java.javaFuncs(scriptPL)

    void stgBuild(project) {
        scriptPL.stage('Build') {
            javaFuncs.build(project)
        }
    }

    void stgAnalysisSonar(project) {
        scriptPL.stage('SBI Analysis sonar') {
            javaFuncs.analysisSonar(project)
        }
    }

    void stgSshCopy(project) {
         scriptPL.stage('SSH Copy'){
            javaFuncs.sshCopy(project)
        }
    }

    void stgSshCreateBackup(project) {
         scriptPL.stage('SSH Create Backup'){
            javaFuncs.sshCreateBackup(project)
        }
    }

    void stgSshStopService(project) {
         scriptPL.stage('SSH Stop Service'){
            javaFuncs.sshStopService(project)
        }
    }

    void stgSshDeleteContent(project) {
         scriptPL.stage('SSH Delete Content'){
            javaFuncs.sshDeleteContent(project)
        }
    }

    void stgSshMoveContent(project) {
         scriptPL.stage('SSH Move Content'){
            javaFuncs.sshMoveContent(project)
        }
    }

    void stgSshExecuteService(project) {
         scriptPL.stage('SSH Execute Service'){
            javaFuncs.sshExecuteService(project)
        }
    }

    void stgUploadArtifactNexus(project){
        scriptPL.stage('Artifact Uploader'){
            javaFuncs.uploadArtifactNexus()
        }
    }
}