#!/usr/bin/env groovy
import groovy.json.JsonSlurperClassic
def call(git_post) {
    node{
        stage('Calling JOB'){
            def jobName = git_post_repository_name
            def jobTargetBranch = git_post_push_changes_0_new_name
            build job: "/${jobName}/${jobTargetBranch}", propagate: false, wait: false
        }
    }
}