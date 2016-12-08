# VersionWatch

This Spring-Boot project helps you to analyse your gradle dependencies and gives you a report including their latest versions. You need to download and run this project. There are 2 different REST endpoints:
- http://localhost:8080/rest/dependency/dependencies/projectPath=&versionState={ALL|UP_TO_DATE|OUTDATED|UNKNOWN}&includeTransitive={true|false}

When you set "Content-Type: application/json" in headers, you will get dependency report in JSON format, otherwise you will get an excel file including your dependencies.

If you have a configured internal Nexus, you can add its URL to src/main/resources/application.properties file.
