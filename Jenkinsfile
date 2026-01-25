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
  - name: git
    image: alpine/git:latest
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
        GITHUB_CREDENTIALS = credentials('github-credentials')
        GITOPS_REPO = 'simple-java-app-gitops'
        GITHUB_USER = 'Yousef-G0'
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
        
        stage('Update GitOps Repository') {
            steps {
                container('git') {
                    sh '''
                        echo "==================================="
                        echo "Updating GitOps Repository"
                        echo "==================================="
                        
                        # Clone GitOps repo
                        git clone https://${GITHUB_CREDENTIALS_USR}:${GITHUB_CREDENTIALS_PSW}@github.com/${GITHUB_USER}/${GITOPS_REPO}.git
                        cd ${GITOPS_REPO}
                        
                        # Update image tag in deployment.yaml
                        echo "Current image:"
                        grep "image:" k8s/deployment.yaml
                        
                        sed -i "s|image: .*simple-java-app:.*|image: ${DOCKER_IMAGE}:${BUILD_NUMBER}|g" k8s/deployment.yaml
                        
                        echo "Updated image:"
                        grep "image:" k8s/deployment.yaml
                        
                        # Configure git
                        git config user.email "jenkins@ci.local"
                        git config user.name "Jenkins CI"
                        
                        # Commit changes
                        git add k8s/deployment.yaml
                        git commit -m "🚀 Deploy build #${BUILD_NUMBER} - Update image to ${DOCKER_IMAGE}:${BUILD_NUMBER}" || echo "No changes to commit"
                        
                        # Push to GitHub
                        git push https://${GITHUB_CREDENTIALS_USR}:${GITHUB_CREDENTIALS_PSW}@github.com/${GITHUB_USER}/${GITOPS_REPO}.git main
                        
                        echo "==================================="
                        echo " GitOps repository updated!"
                        echo " ArgoCD will sync automatically"
                        echo "==================================="
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo '=================================='
            echo ' Pipeline completed successfully!'
            echo '=================================='
            echo ' Docker image: ${DOCKER_IMAGE}:${BUILD_NUMBER}'
            echo ' GitOps repo updated'
            echo ' ArgoCD will deploy in ~3 minutes'
            echo '=================================='
        }
        failure {
            echo ' Pipeline failed!'
        }
    }
}