#!/usr/bin/env groovy
package com.idemia.jenkins.angular
import java.lang.String

class AngularStgs {

    private final Script scriptPL

    AngularStgs(Script scriptPL) {
        this.scriptPL = scriptPL
    }

    def angularFuncs = new com.idemia.jenkins.angular.AngularFuncs(scriptPL)

    void stgGetDependencies(project) {
        scriptPL.stage('Get Dependencies') {
            angularFuncs.getDependencies(project)
        }
    }

    void stgBuild(project) {
        scriptPL.stage('Build') {
            angularFuncs.build(project)
        }
    }

    void stgUnitTest(project) {
        scriptPL.stage('Unit Test') {
            angularFuncs.unitTest(project)
        }
    }

    void stgAnalysisSonar(project) {
        scriptPL.stage('SBI Analysis') {
            angularFuncs.analysisSonar(project)
        }
    }

    void stgCompressLocalFile(project) {
        scriptPL.stage('compress artifact') {
            angularFuncs.compressLocalFile(project)
        }
    }

     void stgSaveArtifacts() {
        scriptPL.stage('Create Artifact Jenkins') {
            angularFuncs.saveArtifacts()
        }
    }

    void stgSshCopy(project) {
        scriptPL.stage('SSH Copy'){
            angularFuncs.sshCopy(project)
        }
    }

    void stgSshCreateBackup(project) {
        scriptPL.stage('SSH Create Backup'){
            angularFuncs.sshCreateBackup(project)
        }
    }

    void stgSshDeleteContent(project) {
        scriptPL.stage('SSH Delete Content'){
            angularFuncs.sshDeleteContent(project)
        }
    }

    void stgSshMoveContent(project) {
        scriptPL.stage('SSH Move Content'){
            angularFuncs.sshMoveContent(project)
        }
    }

    void stgExecuteFuncionalTest (project) {
        scriptPL.stage('Execute Functional Test'){
            angularFuncs.executeFuncionalTest(project)
        }
    }

    //Nexus
    void stgUploadArtifactNexus(project){
        scriptPL.stage('Artifact Uploader'){
            angularFuncs.uploadArtifactNexus(project)
        }
    }
}