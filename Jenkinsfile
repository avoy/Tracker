pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh '/usr/local/apache-ant-1.9.3/bin/ant compile'
            }
        }
    }
}

