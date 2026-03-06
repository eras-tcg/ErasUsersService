pipeline {
    agent any

    environment {
        IMAGE_NAME = 'users-service'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${GIT_COMMIT} ./users_service"
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                sh """
                    docker stop ${IMAGE_NAME}-staging || true
                    docker rm ${IMAGE_NAME}-staging || true
                    docker run -d \\
                        --name ${IMAGE_NAME}-staging \\
                        --restart unless-stopped \\
                        --env-file /etc/users-service/.env \\
                        -p 8090:8080 \\
                        ${IMAGE_NAME}:${GIT_COMMIT}
                """
            }
        }

        stage('Approve Production Deploy') {
            when {
                branch 'main'
            }
            steps {
                timeout(time: 30, unit: 'MINUTES') {
                    input message: 'Deploy to production?', ok: 'Deploy'
                }
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                sh """
                    docker stop ${IMAGE_NAME}-prod || true
                    docker rm ${IMAGE_NAME}-prod || true
                    docker run -d \\
                        --name ${IMAGE_NAME}-prod \\
                        --restart unless-stopped \\
                        --env-file /etc/users-service/.env \\
                        -p 8080:8080 \\
                        ${IMAGE_NAME}:${GIT_COMMIT}
                """
            }
        }
    }

    post {
        failure {
            echo 'Pipeline failed.'
        }
        success {
            echo 'Pipeline completed successfully.'
        }
    }
}
