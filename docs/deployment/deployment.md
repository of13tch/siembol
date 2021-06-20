# Deployment
## Build artifacts
Building and publishing artifacts are executed by [GitHub Actions](/.github/workflows/ci.yml) triggered by events in the siembol repository.

### Java artifacts
Java artifacts are published to Central Maven Repository - [Sonatype OSS Repository Hosting](https://central.sonatype.org/pages/ossrh-guide.html)
- Snapshots - They are built if the version in [POM](/pom.xml) contains `SNAPSHOT`. Snapshots are usually not stable and we suggest to use releases in a production environment
- Releases - They are built if the version in [POM](/pom.xml) does not contain `SNAPSHOT` and are published in Central Maven Repository

### Docker images
Docker images are built both from snapshots and releases. 
- The images are tagged by two tags:
    - `latest` for tagging the latest image 
    - The version of the application from [POM](/pom.xml) 
- Springboot applications
    - An application is loaded using  [springboot properties launcher](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-executable-jar-format.html)
    - [dockerfile](/deployment/docker/Dockerfile.java)
    - [storm-topology-manager](https://hub.docker.com/r/gresearchdev/siembol-storm-topology-manager/)
    - [config-editor-rest](https://hub.docker.com/r/gresearchdev/siembol-config-editor-rest/)
    - [responding-stream](https://hub.docker.com/r/gresearchdev/siembol-responding-stream/)

- Config editor UI
    - A Single page Angular application 
    - nginx-server with configurations
    - [dockerfile](/deployment/docker/Dockerfile.config-editor-ui)

- Storm topologies
    - Images are used for launching storm topologies by storm topology manager
    - [dockerfile](/deployment/docker/Dockerfile.storm)
    - Storm cli
    - Siembol java storm topology artifact
    - [parsing-storm](https://hub.docker.com/r/gresearchdev/siembol-parsing-storm/)
    - [enriching-storm](https://hub.docker.com/r/gresearchdev/siembol-enriching-storm/)
    - [alerting-storm](https://hub.docker.com/r/gresearchdev/siembol-alerting-storm/)
