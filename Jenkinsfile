pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-25'
            args '-u root -v maven-repo:/root/.m2'
        }
    }

    options {
        timestamps()
    }

    stages {
        stage('Build and test') {
            parallel {
                stage('web') {
                    steps {
                        dir('politest') {
                            sh 'mvn -B -ntp verify'
                        }
                    }
                }
                stage('party-service') {
                    steps {
                        dir('party-service') {
                            sh 'mvn -B -ntp verify'
                        }
                    }
                }
                stage('figure-service') {
                    steps {
                        dir('figure-service/figure-service') {
                            sh 'mvn -B -ntp verify'
                        }
                    }
                }
            }
        }

        stage('Archive artifacts') {
            steps {
                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
    }
}