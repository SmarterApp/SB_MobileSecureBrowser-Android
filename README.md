# Welcome to the Mobile Secure Browser (MSB) Project for Android
The Mobile Secure Browser for Android ensures a common, secure online testing experience by preventing users from switching to other applications and from performing certain hardware actions such as taking screenshots.

## License ##
This project is licensed under a variety of licenses as documented in the source code.

## Getting Involved ##
We would be happy to receive feedback on its capabilities, problems, or future enhancements:

* For general questions or discussions, including bugs or enhancements, please use the [Forum](http://forum.opentestsystem.org/viewforum.php?f=17).
* Feel free to **Fork** this project and develop your changes!

## Build Instructions
* Install Eclipse for Java Developers with Android SDK 4.2
* Clone the source code from this project and import it into Eclipse:
    * Click “File”-> “Import…”, then choose “Existing Android Code into Workspace”, click “next”, and then specify the folder of the source code checked out from Mercurial. The project will be imported to the Eclipse workspace.
* Change the package name and refactor all code to reflect this change (Eclipse can help you do this).
* Create a directory tree at the root level of the repository named `build/keystore/`
* Follow the [Android process for signing your app](http://developer.android.com/tools/publishing/app-signing.html) and creating a keystore.
* Create a keystore named `keystore` and place it in `build/keystore/`
* In order to differentiate your app from the official app, change its package name. The package name is a string that uniquely identifies each app on the Google Play Store, and is included in the file `AndroidManifest.xml`.
* Build the project
    * Choose the project In “Package Explorer”, and then Click “Project” -> “Build Project”. Make sure there are no compile errors.
* Export the project to apk file
An apk file is the application file to install an application on any Android devices. To create an apk file, follow these steps:
    * Right click the project, then choose “Export…”, in export destination, choose “Android”->”Export Android Application”
    * Click “next” and then choose the project
    * Next, you will be asked to specify the keystore and password.
    * You will then be asked to specify key alias, use the same keystore and password in the previous step
    * Specify the folder and the name of the apk file, and press “finish” to complete this procedure (an apk file should be created).