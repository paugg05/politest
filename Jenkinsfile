pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-25'
            args '-u root -v maven-repo:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock --add-host=host.docker.internal:host-gateway -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal'
        }
    }

    options {
        timestamps()
    }

    stages {
        stage('Build and test') {
            parallel {
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
                // These two modules start Kafka and MySQL containers in their tests. Running them one after
                // the other keeps only one broker and one database alive at a time, which is much lighter.
                stage('Modules with Docker tests') {
                    stages {
                        stage('web') {
                            steps {
                                dir('politest') {
                                    sh 'mvn -B -ntp verify'
                                }
                            }
                        }
                        stage('stats-service') {
                            steps {
                                dir('stats-service') {
                                    sh 'mvn -B -ntp verify'
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Check for skipped tests') {
            steps {
                // A skipped test passes silently. Docker-based tests are skipped when Docker is unreachable,
                // so a skip is treated as a failure rather than as a green build.
                sh '''
                    if grep -rEl --include='TEST-*.xml' 'skipped="[1-9]' . ; then
                        echo "Some tests were skipped; failing the build."
                        exit 1
                    fi
                '''
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