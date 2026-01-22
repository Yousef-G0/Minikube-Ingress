pipeline {
  agent {
    kubernetes {
      defaultContainer 'maven'
      yaml """
apiVersion: v1
kind: Pod
spec:
  serviceAccountName: jenkins-admin

  containers:
  - name: maven
    image: maven:3.8-openjdk-17
    command: ["sleep"]
    args: ["infinity"]

  - name: docker
    image: docker:24-cli
    command: ["sleep"]
    args: ["infinity"]
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock

  - name: kubectl
    image: lachlanevenson/k8s-kubectl:v1.29.0
    command: ["sleep"]
    args: ["infinity"]

  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
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
        sh '''
          mvn clean package -DskipTests
          mvn test
        '''
      }
    }

    stage('Build Docker Image') {
      steps {
        container('docker') {
          sh '''
            timeout 60 sh -c 'until docker info; do sleep 1; done'
            docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} .
            docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest
          '''
        }
      }
    }

    stage('Push Docker Image') {
      steps {
        container('docker') {
          sh '''
            echo "${DOCKER_CREDENTIALS_PSW}" | docker login \
              -u "${DOCKER_CREDENTIALS_USR}" --password-stdin
            docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
            docker push ${DOCKER_IMAGE}:latest
          '''
        }
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        container('kubectl') {
          sh '''
            which kubectl
            kubectl version --client
            kubectl apply -f k8s/
            kubectl rollout status deployment/simple-java-app --timeout=5m
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
      echo 'Pipeline completed successfully'
    }
    failure {
      echo 'Pipeline failed'
    }
  }
}
