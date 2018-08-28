# KotlinFuzzer
## Overview
This fuzzer generates simple Kotlin programs (syntactically correct, but not always correctly executed due to division by zero, stucking in loops, ArrayOutOfBoundsExceptions, etc.).

Then *KotlinFuzzer* compiles obtained sources with Kotlin compiler (there is **Kotlin JVM**, **Kotlin/Native** or **'split'** mode avaliable) and runs compiled programs.

All compiling or running errors are displayed. 
Also *KotlinFuzzer* dispalys an information about difference in behaviour (successfull/unsuccessfull compilation or runs, program output) between Kotlin JVM and Kotlin/Native programs in 'split' mode.

So far, 1 unique compiler bug has been found: [see the comment](https://youtrack.jetbrains.com/issue/KT-25204).

## Building and configuring
To build project from sources you should have Gradle (version 4.9 or higher): [Main page](https://gradle.org/).

Also you should define _**KOTLIN_HOME**_ variable in your environment as a path to your Kotlin JVM home directory.

In addition, if you want to use Kotlin/Native testing features, you must define **'native-path'** option in the config file or by argument (a path to your Kotlin/Native home directory)


After setting up the environment, just follow these steps:
- Clone or download project;
- Execute following command:
  - on Windows:
  ```
  gradlew.bat makeAll
  ```
  - on Unix:
  ```
  ./gradlew makeAll
  ```
 - Go to **'build/libs'** folder.
 There you can find generated JAR file and **'run.sh'** script for launch.
 
 
 [Gradle User Manual](https://docs.gradle.org/current/userguide/userguide.html)
## Usage
*KotlinFuzzer* can be configured with command line options or through the settings file, defined by **'property-file'** option.
Command line options have a higher priority than the configuration file.

To display all avaliable options and usage tips, run *KotlinFuzzer* with **'-h'** or **'--help'** option.

The settings file is **'conf/default.properties'** by default, it is stored in **'src/main/kotlin/resouces'** folder and will be added to JAR archive while building, but you can choose your own file by using **'property-file'** option.

## Realized features
- Arithmetic operations
- Functions
- Local variables (declaration, initialization)
- Control flow (**if**, **when**, **break**, **continue**, **while**)
- Casts

## Upcoming features
- **For** loop
- Typechecks
- Using **if** and **when** as a statements
- Arrays
- Exceptions (**throw**, **cath**, **finally**)
- Classes
- Lambdas
