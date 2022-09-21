#!/usr/bin/env groovy
package com.idemia.jenkins.java
import com.idemia.jenkins.commons.*
import java.lang.String
import java.time.*
import java.time.format.DateTimeFormatter

class javaFuncs {
    
    private final Script scriptPL

    javaFuncs(Script scriptPL) {
        this.scriptPL = scriptPL
    }

    def commons = new com.idemia.jenkins.commons.Stages(scriptPL)

    void build(project) {
        scriptPL.withMaven(jdk: 'jdk8_u60', maven: 'maven_3.5.3', mavenSettingsConfig: '188537a8-98be-4251-b505-3608cb705da0') { 
            scriptPL.sh 'mvn clean install -s mvn-settings.xml -DskipTests'
        }
    }

    void analysisSonar(project) {    
        scriptPL.withSonarQubeEnv('sbi1') {
            scriptPL.withMaven(jdk: 'jdk8_u60', maven: 'maven_3.5.3', mavenSettingsConfig: '188537a8-98be-4251-b505-3608cb705da0') { 
                scriptPL.sh 'mvn -s mvn-settings.xml -DskipTests sonar:sonar \
                                -Dsonar.projectKey=' + project.name + ' \
                                -Dsonar.projectName=' + project.name + ' \
                                -Dsonar.sources=.\
                                -Dsonar.test=.\
                                -Dsonar.exclusions=**/assets/**,**/test/**,node_modules\\**,dist\\**,coverage\\**,src\\app\\resources\\**,src\\app\\config\\**,src\\app\\testClass\\**'
            }
        }
    }

    void uploadArtifactNexus() {
        if (scriptPL.env.JOB_BASE_NAME == 'test') {
            scriptPL.withMaven(jdk: 'jdk8_u60', maven: 'maven_3.5.3', mavenSettingsConfig: '188537a8-98be-4251-b505-3608cb705da0') { 
                scriptPL.sh  "mvn clean deploy -DskipTests"
            }
        }
    }

    void sshCopy (project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.echo 'el servidor tiene nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
            def remote = commons.getRemoteServer(projectUCD)
            scriptPL.sshCommand remote: remote, command: "rm -rf ${project.intoSSHPath}", sudo:true
            scriptPL.sshCommand remote: remote, command: "mkdir -p ${project.intoSSHPath}", sudo:true
            scriptPL.sshPut remote: remote, from: "${project.fromPath}", into: "${project.intoSSHPath}", filterRegex: /.jar$/
            if (project.config.hasConfigs == "true") {
                scriptPL.echo "============ configuration properties ============"
                scriptPL.sshPut remote: remote, from: "${project.config.fromConfigsPath}", into: "${project.intoSSHPath}", filterRegex: /.properties$/
            }
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }

    void sshCreateBackup (project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.echo 'el servidor tiene nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
            def remote = commons.getRemoteServer(projectUCD)
            def now = LocalDateTime.now()
            def backupDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
            scriptPL.sshCommand remote: remote, command: "mkdir -p ${project.backupFolder}", sudo:true
            scriptPL.sshCommand remote: remote, command: "tar -cvf ${project.backupFolder}${project.backupFileName}-${backupDate}.tar.gz ${project.deployPath}/*.jar", sudo:true
            if (project.config.hasConfigs == "true") {
                scriptPL.echo "============ backup configuration properties ============"
                scriptPL.sshCommand remote: remote, command: "tar -cvf ${project.backupFolder}${project.config.backupConfigFileName}-${backupDate}.tar.gz ${project.config.deployPathConfig}/*.properties", sudo:true
            }
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }
    
    void sshStopService (project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.echo 'el servidor tiene nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
            def remote = commons.getRemoteServer(projectUCD)
            scriptPL.sshCommand remote: remote, command: "kill -9 \$(ps -fea | grep '${project.javaServiceName}' | grep -v grep | awk '{ print \$2 }')", sudo: true, failOnError: false
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }

    void sshDeleteContent (project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.echo 'el servidor tiene nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
            def remote = commons.getRemoteServer(projectUCD)
            scriptPL.sshCommand remote: remote, command: "rm -rf ${project.deployPath}/*.jar", sudo: true
            if (project.config.hasConfigs == "true") {
                scriptPL.echo "============ delete configuration properties ============"
                scriptPL.sshCommand remote: remote, command: "rm -rf ${project.config.deployPathConfig}/${project.config.configFileName}", sudo: true
            }
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }

    void sshMoveContent (project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.echo 'el servidor tiene nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
            def remote = commons.getRemoteServer(projectUCD)
            scriptPL.sshCommand remote: remote, command: "mv ${project.publishPath} ${project.deployPath}/.", sudo: true
            // Obtener configuraciones.
            def configs = commons.getConfigs(project)
            for(config in configs) {
                scriptPL.println(config)
                // validar si existe configuracion
                if (config.hasConfig == "true") {
                    // validar si existen archivos de configuracion
                    if (config.environment.hasEnvironment == "true") {
                        scriptPL.echo "============ move configuration properties ============"
                        // obtener nombre del archivo segun el ambiente 
                        def fileName = ""
                        switch (scriptPL.env.JOB_BASE_NAME) {
                            case "dev":
                                fileName = config.environment.dev
                                break
                            case "test":
                                fileName = config.environment.test
                                break
                            case "master":
                                fileName = config.environment.prd
                                break
                            default:
                                break
                        }
                        scriptPL.echo "accion a ejecutar: mv ${project.config.configPublishPath}/${fileName} ${project.config.deployPathConfig}/${config.configFileName}"
                        // mover el archivo de configuracion a la ruta indicada
                        scriptPL.sshCommand remote: remote, command: "mv ${project.config.configPublishPath}/${fileName} ${project.config.deployPathConfig}/${config.configFileName}", sudo: true
                    }
                }
            }
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }

    void sshExecuteService(project) {
        def projectUCD  = commons.getEnvironment(project)
        if (projectUCD.hasEnvironment == 'true') {
            scriptPL.timeout(time: 1, unit: "MINUTES") {
                scriptPL.echo 'el servidor tiene nombre: ' + projectUCD.name + ' con ip:' +  projectUCD.host
                def remote = commons.getRemoteServer(projectUCD)
                def textToExecute = """#! /bin/bash
                echo 'ingreso a carpeta'
                cd ${project.deployPath}
                echo 'ejecucion del jar'
                sudo nohup java -jar ${project.deployPath}/\$(ls -lrt ${project.deployPath} | grep '.jar\$' | awk '{ print \$9 }') > nohup.log 2>&1 &
                """
                scriptPL.writeFile file: 'abc.sh', text: textToExecute
                scriptPL.sshScript remote: remote, script: 'abc.sh'
            }
        } else {
            scriptPL.echo 'no hay configuracion de ambiente para desplegar'
        }
    }
}
