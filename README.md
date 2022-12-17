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

The BeVigil-CI plugin is intended to be used as an additional step in build pipelines that already build android or ios apps. In this section, we will asume that you already have a pipeline running to build your application.



### Installing the plugin
To use this plugin, you need to install it in your Jenkins installation. To do this, first login to your jenkins server, and click on "Manage Plugins" on the Manage Jenkins page:

![](./docs/manage-plugins.png)

Next, navigate to the available tab, and search for the "BeVigil VI" plugin. Click the checkbox next the plugin, and then click on "Install without Restart" to install the plugin.

### Using the plugin in your jenkins build


1. Add a build step which uses the plugin on your Jenkins CI Build Pipeline:
<br /><br />
<img  alt="image" src="./docs/add-build-step.png">


2. Now, configure the following information about your app on the build step:
<br /><br />

 <img alt="image" src="./docs/plugin-details-form.png">




- **API KEY**: Your BeVigil API Key
- **App Type**: Select Android/ioS
- **App Path**: This is the path to your built app relative to the root of your jenkins workspace.
- **Package Name**: Enter the package name for your application
- **Scan Timeout**: This the time (in minutes) after which the scan will timeout on the plugin.
- **Severity Threshold**: This tells BeVigil to set a threshold for the vulnerabilities:
    - Low: This includes low, medium and high vulnerabilities
    - Medium: This includes medium and high vulnerabilities
    - High: This includes only high vulnerabilities

3. Save your build step, and start a new build. If all goes well, the plugin should print the report to 
stdout.

### How the plugin works
<img alt="image" src="./docs/zoomed-output.png">

Mobile applications are often full of security issues which when not identified can cost organizations a lot. This plugin helps to identify potential security vulnerabilities in the code. By running a scan during the build process, developers can catch and fix vulnerabilities early on, which can help to prevent security breaches and protect the integrity of the application. The plugin can be easily configured making it a valuable tool for any organization that values security.

The plugin recieves an AWS presigned URL where it uploaded the build file of the app after reading it. 
After uploading the file successfully, the file is scanned using BeVigil's API for vulnerabilites. Finally, we get the output for the scan. 

<img alt="image" src="./docs/diagram.png">
<br /><br />

## Output
<img alt="image" src="./docs/plugin-output.png">


## Issues

Report issues and enhancements in the [Issue tracker](https://github.com/Bevigil/Bevigil-CI-Plugin/issues).

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

