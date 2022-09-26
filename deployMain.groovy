#!/usr/bin/env groovy
pipeline {
  agent {
    node {
      label 'prueba'
         }
  }  
  options {
              disableConcurrentBuilds ()
  }
 stages {  
        stage('Checkout') {
        steps {
          cleanWs()
          println(env.JOB_NAME)
          this.cleanWs()
          this.checkout this.scm
          println("Building with maven")
        }
      }
        stage ("Building Maven and Sonar Scanning"){
        steps {
                this.withSonarQubeEnv('sbi1') {
        this.withMaven(jdk: 'jdk11_u18.9', maven: 'maven_3.8.5') {
                this.sh 'mvn clean package deploy '
                       }                    
                }
              }
      }             
  }
  post {
        always {
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true        
        }
    }
}