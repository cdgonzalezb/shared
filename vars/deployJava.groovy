#!/usr/bin/env groovy
import com.idemia.jenkins.commons.Stages
import com.idemia.jenkins.java.javaStgs

def call(project) {
    def stages = new Stages(this)
    def javastgs = new javaStgs(this)

    node (label:project.node) {
        // obtener codigo
        stages.stgCheckoutSCM(project)
        // Obtener dependencias y compilar
        javastgs.stgBuild(project)
        // Realizar analisis estatico de codigo
        javastgs.stgAnalysisSonar(project)
        // Evaluar quality gate
        stages.stgQualityGate(project)
        // enviar artefacto a servidor de despliegue
        javastgs.stgSshCopy(project)
        // generar backup de aplicacion en servidor de despliegue
        // javastgs.stgSshCreateBackup(project)
        // detener servicio a cambiar
        javastgs.stgSshStopService(project)
        // eliminar version anterior en del servidor de despliegue
        javastgs.stgSshDeleteContent(project)
        // mover version nueva a ruta de aplicacion en servidor de despliegue
        javastgs.stgSshMoveContent(project)
        // ejecutar servicio de java
        javastgs.stgSshExecuteService(project)
        // subir artefactos a nexus.
        javastgs.stgUploadArtifactNexus(project)
    }
}