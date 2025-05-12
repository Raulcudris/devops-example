pipeline {
    agent any
    
    triggers {
        GenericTrigger(
            genericVariables: [
                [key: 'image_name', value: '$.image_name'],
                [key: 'image_tag', value: '$.image_tag'],
                [key: 'commit_sha', value: '$.commit_sha']
            ],
            tokenCredentialId: 'jenkins-webhook-token',
            printContributedVariables: true
        )
    }
    
    environment {
        DOCKER_CREDS = credentials('docker-hub-creds')
        KUBECONFIG = credentials('kubeconfig')
    }
    
    stages {
        stage('Prepare Deployment') {
            steps {
                script {
                    // Actualizar el deployment con la nueva imagen
                    sh """
                        kubectl set image deployment/devops-example \
                        devops-example=${env.image_name}:${env.image_tag} \
                        --record
                    """
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    // Esperar a que el despliegue se complete
                    sh "kubectl rollout status deployment/devops-example --timeout=300s"
                    
                    // Obtener URL del servicio
                    def SERVICE_URL = sh(
                        script: "kubectl get svc devops-example -o jsonpath='{.status.loadBalancer.ingress[0].ip}'",
                        returnStdout: true
                    ).trim()
                    
                    echo "Aplicación desplegada en: http://${SERVICE_URL}"
                }
            }
        }
        
        stage('Verify') {
            steps {
                script {
                    // Prueba básica de funcionamiento
                    def SERVICE_URL = sh(
                        script: "kubectl get svc devops-example -o jsonpath='{.status.loadBalancer.ingress[0].ip}'",
                        returnStdout: true
                    ).trim()
                    
                    sh "curl -sSf http://${SERVICE_URL}"
                }
            }
        }
    }
    
    post {
        always {
            slackSend(
                channel: '#deployments',
                message: """Deployment ${currentBuild.currentResult}
                | Aplicación: ${env.image_name}:${env.image_tag}
                | Commit: ${env.commit_sha}
                | Build: ${env.BUILD_URL}"""
            )
        }
    }
}