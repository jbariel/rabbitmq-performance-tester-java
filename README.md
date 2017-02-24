# RabbitMQ performance tester for Java
Java Producer/Consumer with performance capabilities for RabbitMQ.

# Getting started
Clone the repo, and run `mvn` to build the project.

# Usage
This uses file based properties to conenct to a RabbitMQ instance.  To see an example file, you can view [props.properties.example](props.properties.example)

## Running as a .jar file
This app can be run as a standalone jar file.  To do this, you will need to use the `{project.artifactId}-{project.version}-complete.jar` file.  The command will look like:

```
java -cp <location to props.properties file> -jar <location to {project.artifactId}-{project.version}-complete.jar>
```

# Issues
Please use the [Issues tab](../../issues) to report any problems or feature requests.
