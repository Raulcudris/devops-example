pipeline {
    agent {
        kubernetes {
            label 'jenkins-agent'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  name: jenkins-agent
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:latest
  - name: docker
    image: docker:latest
    command: ['cat']
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  - name: kubectl
    image: bitnami/kubectl:latest
    command: ['cat']
    tty: true
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        KUBECONFIG = credentials('kubeconfig')
    }
    
    stages {
        stage('Deploy to Kubernetes') {
            steps {
                container('kubectl') {
                    script {
                        sh '''
                            kubectl apply -f deployment.yaml
                            kubectl apply -f service.yaml
                            kubectl rollout status deployment/devops-example
                        '''
                    }
                }
            }
        }
        
        stage('Smoke Test') {
            steps {
                container('kubectl') {
                    script {
                        def SERVICE_URL = sh(returnStdout: true, script: "minikube service devops-example --url").trim()
                        sh "curl -sSf ${SERVICE_URL}"
                    }
                }
            }
        }
    }
    
    post {
        success {
            slackSend channel: '#devops', message: "Deployment succeeded: ${env.BUILD_URL}"
        }
        failure {
            slackSend channel: '#devops', message: "Deployment failed: ${env.BUILD_URL}"
        }
    }
}