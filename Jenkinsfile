pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Container Image') {
            steps {
                sh 'podman build -t petbattle-api:latest .'
            }
        }

        stage('Local Deploy') {
            steps {
                sh './start_petbattle.sh'
            }
        }
    }
}
