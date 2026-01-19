pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins: agent
spec:
  serviceAccountName: jenkins-admin
  containers:
  - name: maven
    image: maven:3.8-openjdk-17
    command:
    - cat
    tty: true
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
  - name: docker
    image: docker:24-dind
    securityContext:
      privileged: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run
  - name: kubectl
    image: bitnami/kubectl:1.28
    command:
    - cat
    tty: true
  volumes:
  - name: maven-cache
    emptyDir: {}
  - name: docker-sock
    emptyDir: {}
"""
        }
    }
    
    environment {
        DOCKER_IMAGE = 'youseflol/simple-java-app'
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build & Test') {
            steps {
                container('maven') {
                    sh '''
                        echo "Building the application..."
                        mvn clean package -DskipTests
                        echo "Running tests..."
                        mvn test
                    '''
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                container('docker') {
                    sh '''
                        # Wait for Docker daemon to be ready
                        timeout 60 sh -c 'until docker info; do sleep 1; done'
                        
                        echo "Building Docker image..."
                        docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} .
                        docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest
                        
                        echo "Docker images built:"
                        docker images | grep simple-java-app
                    '''
                }
            }
        }
        
        stage('Push Docker Image') {
            steps {
                container('docker') {
                    sh '''
                        echo "Logging in to Docker Hub..."
                        echo ${DOCKER_CREDENTIALS_PSW} | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin
                        
                        echo "Pushing Docker images..."
                        docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                        docker push ${DOCKER_IMAGE}:latest
                        
                        echo "Images pushed successfully!"
                    '''
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                container('kubectl') {
                    sh '''
                        echo "Deploying to Kubernetes..."
                        kubectl apply -f k8s/
                        
                        echo "Waiting for deployment to be ready..."
                        kubectl rollout status deployment/simple-java-app --timeout=5m
                        
                        echo "Deployment complete!"
                        kubectl get pods -l app=simple-java-app
                        kubectl get svc simple-java-app-service
                        kubectl get ingress simple-java-app-ingress
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            echo 'Cleaning up...'
        }
    }
}