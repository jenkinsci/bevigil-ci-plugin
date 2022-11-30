# BeVigil-Plugin

## Introduction

<br />
<br />

<p align="center">
    <img alt="image" src="https://user-images.githubusercontent.com/58368421/204227445-b8a45002-9e87-4206-bc09-e9808ab5435e.png" width="200">
</p>




<br />

This is a plugin to scan Android and ioS applications in your jenkins pipelines using [CloudSek BeVigil](https://bevigil.com/). BeVigil can 
analyze code at scale and easily search for API keys, regexes, etc to see the matches in different files of an application.
<br />
<br />
 

## Getting started

To get started with using the plugin, follow these steps:

1. Add a build step which uses the plugin on your Jenkins CI Build Pipeline:
<br /><br />
<img width="250" alt="image" src="https://user-images.githubusercontent.com/58368421/204229185-bf93fc9c-5583-483a-ab84-fe5ab97d3f7b.png">


2. Now, configure the following information about your app on the build step:
<br /><br />
 <img width="928" alt="image" src="https://user-images.githubusercontent.com/58368421/204229406-1f938505-04b2-4787-8411-d607cbfefe93.png">


- **API KEY**: Your BeVigil API Key
- **App Type**: Select Android/ioS
- **App Path**: This is the path to your built app relative to the root of your jenkins workspace.
- **PackageName**: Enter the package name for your application
- **ScanTImeout**: This the time (in minutes) after which the scan will timeout on the plugin.
- **Severity Threshold**: This tells BeVigil to set a threshold for the vulnerabilities:
    - Low: This includes low, medium and high vulnerabilities
    - Medium: This includes medium and high vulnerabilities
    - High: This includes only high vulnerabilities

3. Save your build step, and start a new build. If all goes well, the plugin should print the report to stdout:
<br /><br />
<img width="528" alt="image" src="https://user-images.githubusercontent.com/58368421/204229994-6529d643-cbe2-41e1-8c32-436c431883fe.png">


## Issues

Report issues and enhancements in the [Jenkins issue tracker](https://issues.jenkins-ci.org/).

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

