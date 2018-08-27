# KotlinFuzzer
## Overview
This fuzzer generates simple Kotlin programs (syntactically correct, but not always correctly executed due to division by zero, stucking in loops, ArrayOutOfBoundsExceptions, etc.).

Then *KotlinFuzzer* compiles obtained sources with Kotlin compiler (there is **Kotlin JVM**, **Kotlin/Native** or **'split'** mode avaliable) and runs compiled programs.

All compiling or running errors are displayed. 
Also *KotlinFuzzer* dispalys an information about difference in behaviour (successfull/unsuccessfull compilation or runs, program output) between Kotlin JVM and Kotlin/Native programs in 'split' mode.

## Building and configuring
To compile from sources use following steps:


## Usage
*KotlinFuzzer* can be configured with command line options or through the settings file, defined by **'property-file'** option.

To display all avaliable options and usage tips, run *KotlinFuzzer* with **'-h'** or **'--help'** option.

## Realized features

## Upcoming features
