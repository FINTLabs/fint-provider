pipeline {
    parameters {
        string(name: 'BUILD_FLAGS', defaultValue: '', description: 'Gradle build flags')
    }
    agent { label 'docker' }
    stages {
        stage('Build') {
            steps {
                sh 'git clean -fdx'
                sh "docker build --tag ${GIT_COMMIT} --build-arg buildFlags=${params.BUILD_FLAGS} ."
            }
        }
        stage('Publish Latest') {
            when {
                branch 'master'
            }
            steps {
                withDockerRegistry([credentialsId: 'fintlabsacr.azurecr.io', url: 'https://fintlabsacr.azurecr.io']) {
                    sh "docker tag ${GIT_COMMIT} fintlabsacr.azurecr.io/provider:build.${BUILD_NUMBER}"
                    sh "docker push fintlabsacr.azurecr.io/provider:build.${BUILD_NUMBER}"
                }
            }
        }
        stage('Publish Version') {
            when {
                tag pattern: "v\\d+\\.\\d+\\.\\d+(-\\w+-\\d+)?", comparator: "REGEXP"
            }
            steps {
                script {
                    VERSION = TAG_NAME[1..-1]
                }
                sh "docker tag ${GIT_COMMIT} fintlabsacr.azurecr.io/provider:${VERSION}"
                withDockerRegistry([credentialsId: 'fintlabsacr.azurecr.io', url: 'https://fintlabsacr.azurecr.io']) {
                    sh "docker push fintlabsacr.azurecr.io/provider:${VERSION}"
                }
            }
        }
        stage('Publish PR') {
            when { changeRequest() }
            steps {
                sh "docker tag ${GIT_COMMIT} fintlabsacr.azurecr.io/provider:${BRANCH_NAME}.${BUILD_NUMBER}"
                withDockerRegistry([credentialsId: 'fintlabsacr.azurecr.io', url: 'https://fintlabsacr.azurecr.io']) {
                    sh "docker push fintlabsacr.azurecr.io/provider:${BRANCH_NAME}.${BUILD_NUMBER}"
                }
            }
        }
    }
}
