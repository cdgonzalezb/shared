#!/usr/bin/env groovy
package com.idemia.jenkins.commons

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.ss.usermodel.*;
import groovy.json.JsonSlurper;
import java.lang.String;
import groovy.io.*;
import groovy.json.JsonSlurperClassic;

class Business{
    private final Script scriptPL

    Business(Script scriptPL) {
        this.scriptPL = scriptPL
    }

    void getProjectInfo(projectName, String urlShareLibrary){
        def project
        scriptPL.dir("jenkinssharedlibrary"){
            scriptPL.git url: urlShareLibrary
            def path = scriptPL.pwd()
            def pathJson = path + "/" + "resources/projects/projects.txt"
            def data = scriptPL.readFile(file: pathJson)
            scriptPL.println(data)
            def parser = new JsonSlurperClassic()
            project = parser.parseText(data)
            assert project instanceof Map
            project = project.Projects.find { p -> p.name == projectName } ?: "No encontre nada"
            scriptPL.println project
        }
        scriptPL.cleanWs()
        return project
    }

    void uploadArtifactNexus(nexusVersion,nexusUrl, nexusGroupId, nexusRepository, nexusCredentialsId, projectName, pathArtifact){
        def now = new Date()
        def fechaGen = now.format("yyyyMMdd.HHmm", TimeZone.getTimeZone("GMT-5:00"))
        scriptPL.nexusArtifactUploader(
                    nexusVersion: nexusVersion,
                    protocol: 'http',
                    nexusUrl: nexusUrl,
                    groupId: nexusGroupId,
                    version: "${fechaGen}_${scriptPL.env.BUILD_ID}",                    
                    repository: nexusRepository,
                    credentialsId: nexusCredentialsId,
                    artifacts: [
                        [artifactId: projectName,
                        classifier: '',
                        file: pathArtifact,
                        type: 'zip']
                    ]
                    )
    }

    void uploadArtifactNexus(nexusVersion,nexusUrl, nexusGroupId, nexusRepository, nexusCredentialsId, projectName, pathArtifact, type){
        def now = new Date()
        def fechaGen = now.format("yyyyMMdd.HHmm", TimeZone.getTimeZone("GMT-5:00"))
        scriptPL.nexusArtifactUploader(
                    nexusVersion: nexusVersion,
                    protocol: 'http',
                    nexusUrl: nexusUrl,
                    groupId: nexusGroupId,
                    version: "${fechaGen}_${scriptPL.env.BUILD_ID}",                    
                    repository: nexusRepository,
                    credentialsId: nexusCredentialsId,
                    artifacts: [
                        [artifactId: projectName,
                        classifier: '',
                        file: pathArtifact,
                        type: type]
                    ]
                    )
    }
    
}


