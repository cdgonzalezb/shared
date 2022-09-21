#!/usr/bin/env groovy
package com.idemia.jenkins.angular
import com.idemia.jenkins.commons.*
import java.lang.String
import java.time.*
import java.time.format.DateTimeFormatter

class AngularFuncs {
    
    private final Script scriptPL

    AngularFuncs(Script scriptPL) {
        this.scriptPL = scriptPL
    }

    def commons = new com.idemia.jenkins.commons.Stages(scriptPL)

    void getDependencies(project) {
        scriptPL.sh '''#!/bin/bash
                |echo get depencies:
                |echo ======================
                |npm install'''.stripMargin()
    }

    void build(project) {
        scriptPL.sh '''#!/bin/bash
                |echo run ng build:
                |echo ======================
                |npm run ng build'''.stripMargin()
    }
    
    void unitTest(project) {
        if (project.stgUnitTests == "true"){
        scriptPL.sh '''#!/bin/bash
                |echo npm run test-ci:
                |echo ======================
                |npm run test-ci'''.stripMargin()
        } else {
            scriptPL.echo 'no hay pruebas unitarias para ejecutar en el proyecto'
        }
    }

    void analysisSonar(project) {
        // Sonar constants
        def sonarProjectKey = project.name
        def sonarProjectName = project.name
        def sonarProjectVersion='0.0.1'
        def sonarSources='./src,./e2e'
        def sonarExclusions='**/node_modules/**,**/*.spec.ts,**/dist/**,**/coverage/**,'
        def sonarTests='./src,./e2e'
        def sonarTestInclusions='**/*.spec.ts'
        def sonarTypescriptNode='./node/node'
        def sonarTypescriptLcovReportPath='./coverage/lcov.info'
        def SONAR_SCANNER_HOME = scriptPL.tool "sonar-scanner-3.2.0.1227"

        if (project.stgUnitTests == "true"){
            scriptPL.withSonarQubeEnv('sbi1') {
                scriptPL.sh "echo \$SONAR_SCANNER_HOME"
                scriptPL.sh """#!/bin/sh
                    ${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                    -Dsonar.projectKey=${sonarProjectKey} \
                    -Dsonar.projectName=${sonarProjectName} \
                    -Dsonar.sources=${sonarSources} \
                    -Dsonar.typescript.lcov.reportPaths=${sonarTypescriptLcovReportPath} \
                    -Dsonar.exclusions=${sonarExclusions}
                """
            }
        } else {
            scriptPL.withSonarQubeEnv('sbi1') {
                scriptPL.sh "echo \$SONAR_SCANNER_HOME"
                scriptPL.sh """#!/bin/sh
                    ${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                    -Dsonar.projectKey=${sonarProjectKey} \
                    -Dsonar.projectName=${sonarProjectName} \
                    -Dsonar.sources=${sonarSources} \
                    -Dsonar.exclusions=${sonarExclusions}
                """
            }
        }
    }

    void compressLocalFile(project) {
        // Build contants
        def archiveFilename='dist.zip'
        scriptPL.sh """#!/bin/sh
            # Cleanup - Remove previous archive if exists
            if [[ -f "${archiveFilename}" ]];then
                rm ${archiveFilename}
            fi
            """
        // Archive artefact
        scriptPL.zip archive: true, dir: 'dist', glob: '', zipFile: "${archiveFilename}"
    }

    void saveArtifacts() {
        scriptPL.archiveArtifacts '*.zip'
    }

    void sshCopy (project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.echo 'servidor nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
            def remote = commons.getRemoteServer(projectUCD)
            scriptPL.sshPut remote: remote, from: "${project.fromPath}", into: "${project.intoSSHPath}"
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }

    void sshCreateBackup (project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.echo 'servidor nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
            def remote = commons.getRemoteServer(projectUCD)
            def now = LocalDateTime.now()
            def backupDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
            scriptPL.sshCommand remote: remote, command: "tar -cvf ${project.backupFolder}${project.backupFileName}-${backupDate}.tar.gz ${project.deployPath}/*", sudo:true
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }

    void sshDeleteContent (project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.echo 'servidor tiene nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
            def remote = commons.getRemoteServer(projectUCD)
            scriptPL.sshCommand remote: remote, command: "rm -r ${project.deployPath}/*", sudo: true
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }

    void sshMoveContent (project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.echo 'servidor tiene nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
            def remote = commons.getRemoteServer(projectUCD)
            scriptPL.sshCommand remote: remote, command: "mv ${project.publishPath} ${project.deployPath}/.", sudo: true
            scriptPL.sshCommand remote: remote, command: "chmod -R 777 ${project.deployPath}/*", sudo: true
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }

    void executeFuncionalTest (project){
        def testUCD  = commons.getTestInfo(project)
        def execTest = testUCD.hasAutomatedTest
        if(execTest == 'true'){
            scriptPL.sleep(15)
            def testJenkinsJob = testUCD.job
            scriptPL.echo "job de pruebas seleccionado ${testJenkinsJob}"
            scriptPL.echo "rama seleccionada ${testUCD.branch}"
            scriptPL.echo "ambiente seleccionado ${testUCD.ambiente}"
            scriptPL.echo "funcionalidad seleccionada ${testUCD.feature}"
            scriptPL.build job: testJenkinsJob, propagate: false, wait: false, parameters:[
                                scriptPL.string(name: 'branch', value: testUCD.branch),
                                scriptPL.string(name: 'ambiente', value: testUCD.ambiente),
                                scriptPL.string(name: 'feature', value: testUCD.feature)
                            ]
        }
    }

    void uploadArtifactNexus(project){
        def archiveFilename='registraduria'
        scriptPL.sh """#!/bin/bash
        |echo run publish:
        |echo ======================
        npm publish ./dist/${archiveFilename}""".stripMargin()
    }
}
