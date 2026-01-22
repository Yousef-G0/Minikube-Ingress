pipeline {
  agent {
    kubernetes {
      defaultContainer 'maven'
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
    command: ['cat']
    tty: true
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2

  - name: docker
    image: docker:24-cli
    command: ['cat']
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock

  volumes:
  - name: maven-cache
    emptyDir: {}

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
    stage('Install Kubectl'){
			steps {
				sh '''
				curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                		chmod +x kubectl
                		mv kubectl /usr/local/bin/kubectl
				'''
			}
		}

    stage('Deploy to Kubernetes') {
      steps {
        container('maven') {
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
