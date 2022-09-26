#!/usr/bin/env groovy
import com.idemia.jenkins.commons.Stages

def call() {
    node ("any"){
        try {
            def stages = new Stages(this)
            def projectFullPath = env.JOB_NAME
            println(env.JOB_NAME)
            def listPath = projectFullPath.split("/")
            println(listPath.inspect())
            def projectName = listPath[2]
            println(projectName)
            def project = stages.stgReadParameters(projectName)
            def projectType = project.technology
            switch(projectType) {
                case 'ESB':
                    deployIIB(project)
                    break
                case 'Java':
                    deployJava(project)    
                    break
                case 'Angular':
                    deployAngular(project)    
                    break
                default:
                    script {
                        error "Proyecto invalido"
                    }
                    break
            }
        }catch(e){
            throw e
        }   
        return this
    }
}
