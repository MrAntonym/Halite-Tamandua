#!/bin/bash

javac SlothBot2.java
javac SlothBot3.java
javac SlothBot4.java
javac Tamandua1.java
./halite -d "60 60" "java SlothBot2" "java SlothBot3" "java SlothBot4" "java Tamandua1"
