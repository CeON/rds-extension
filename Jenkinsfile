MAIN_BRANCH="master"

pipeline {
    agent {
        docker { image 'openjdk:8u342-jdk' }
    }

    parameters {
        string(name: 'branch', defaultValue: params.branch ?: MAIN_BRANCH, description: 'Branch to build', trim: true)
    }

    options {
        buildDiscarder logRotator(artifactNumToKeepStr: '1', numToKeepStr: '3')
        disableConcurrentBuilds()
        timeout(activity: true, time: 10)
    }

    environment {
        ARTIFACTORY_DEPLOY=credentials('ICM_ARTIFACTORY_JENKINSCI')
        MAVEN_OPTS = "-Dmaven.repo.local=/home/jenkins/.m2/repository"
    }

    stages {

        stage('Build') {
            steps {
               echo 'Building dataverse.'
               sh './mvnw package'
            }

            post {
                always {
                    recordIssues(tools: [mavenConsole(), java()])
                    junit skipPublishingChecks: true, testResults: '**/target/surefire-reports/*.xml'
                    recordCoverage(tools: [[parser: 'JACOCO']])
            	}
            }
        }

        stage('Deploy') {
            when {
                expression { params.branch == MAIN_BRANCH }
            }

            steps {
               echo 'Deploying artifacts.'
               sh './mvnw deploy -Pdeploy -s settings.xml'
            }
        }

    }
}
