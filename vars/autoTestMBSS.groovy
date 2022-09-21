#!/usr/bin/env groovy
def call() {
    def gitBranch = branch
    def urlGitLab = "https://r11-sfdh-scm-001.mphr11.morpho.com/bitbucket/scm/psi/co-id-dvop-dops-tests.git"
    def mavenHome = tool name: "maven_3.5.3"
    def nodeHome = tool name: "node-v10.16.3"
    def npmHome = "/npm"
    env.PATH = "${mavenHome}\\bin;${env.PATH}" 
    env.PATH = "${nodeHome};${env.PATH}"
    env.PATH = "${npmHome};${env.PATH}"

    stage("Checkout SCM"){
        git branch: gitBranch, credentialsId: 'b4cf9c89-b754-432d-bd39-28bc65bcfed4', url: urlGitLab
        println("Branch utilizado:  " + gitBranch)
        println("ambiente utilizado:  " + ambiente)
        println("feature utilizado:  " + feature)
    }
    stage("Install node_modules"){
        sh '''#!/bin/bash
                |echo Install protractor:
                |echo ======================
                |sudo npm install -g protractor
                '''.stripMargin()

        sh '''#!/bin/bash
                |echo Install typescript:
                |echo ======================
                |sudo npm install -g typescript
                '''.stripMargin()

        sh '''#!/bin/bash
                |echo Install:
                |echo ======================
                |sudo npm install'''.stripMargin()
    }
    stage("Build"){
        sh '''#!/bin/bash
                |echo Build:
                |echo ======================
                |sudo tsc'''.stripMargin()
    }
    try {
        stage("Test"){
            println("Executing test...")

            if (ambiente.equals("Calificacion")){
                newAmbiente = "http://10.127.11.175"
                println("ambiente de Calificacion")
            }
            else if (ambiente.equals("Desarrollo")){
                newAmbiente = "http://10.127.11.180"
                println("ambiente de Desarrollo")
            }
            println(feature)
            if (feature.equals("Todos")){
                exe ="protractor ts-compiled/config/config.js --params.urlPortal=" + newAmbiente
                println(exe)
                }
            else if (feature.equals("Login")) {
                exe = "protractor ts-compiled/config/config.js --suite Login --params.urlPortal=" + newAmbiente
                println(exe)
                }
            else{
                println("el parametro no existe para la ejecucion")
                }

            println("Comando de ejecucion pruebas" + exe)

            sh exe
        }
    } catch(e) {
        println "Algun escenario de pruebas presento fallo en la ejecucion..."
        throw e
    } finally {
        // publish a report
        cucumber fileIncludePattern: '**/reports/json-reports/*.json', 
        sortingMethod: 'ALPHABETICAL'
        // publish html
        publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true, 
            reportDir: '/home/mphadmin/working_dir/workspace/TestAutomations_MBSSFacial/reports/html-reports',
            reportFiles: 'cucumber_report.html',
            reportName: 'Report_MBSS_HTML',
            reportTitles: 'Report'])
            System.clearProperty("hudson.model.DirectoryBrowserSupport.CSP");
            System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "sandbox allow-same-origin allow-scripts; default-src *; script-src * 'unsafe-inline'; img-src *; style-src * 'unsafe-inline'; font-src * data:");

    }

} 