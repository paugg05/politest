FROM jenkins/jenkins:lts-jdk21

USER root
# Docker CLI, required to run build stages inside containers.
RUN apt-get update \
    && apt-get install -y --no-install-recommends docker.io \
    && rm -rf /var/lib/apt/lists/*

USER jenkins
RUN jenkins-plugin-cli --plugins docker-workflow