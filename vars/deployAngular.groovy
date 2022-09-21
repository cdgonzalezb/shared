#!/usr/bin/env groovy
import com.idemia.jenkins.commons.Stages
import com.idemia.jenkins.angular.AngularStgs

def call(project) {
    def stages = new Stages(this)
    def angularstgs = new AngularStgs(this)

    node (label:project.node) {
        // Obtener codigo
        stages.stgCheckoutSCM(project)
        // Obtener dependencias
        angularstgs.stgGetDependencies(project)
        // compilar proyecto
        angularstgs.stgBuild(project)
        // ejecutar pruebas unitarias
        angularstgs.stgUnitTest(project)
        // ejecutar analisis en sonarqube
        angularstgs.stgAnalysisSonar(project)
        // revisar quality gate de sonar
        stages.stgQualityGate(project)
        // comprimir artefacto
        angularstgs.stgCompressLocalFile(project)
        // guardar artefacto en jenkins
        angularstgs.stgSaveArtifacts()
        // enviar artefacto a servidor de despliegue
        angularstgs.stgSshCopy(project)
        // generar backup de aplicacion en servidor de despliegue
        // angularstgs.stgSshCreateBackup(project)
        // eliminar version anterior en del servidor de despliegue
        angularstgs.stgSshDeleteContent(project)
        // mover version nueva a ruta de aplicacion en servidor de despliegue
        angularstgs.stgSshMoveContent(project)
        // ejecutar prueba funcionales
        angularstgs.stgExecuteFuncionalTest(project)
        // enviar artefacto a nexus
        //angularstgs.stgUploadArtifactNexus(project)
    }
}