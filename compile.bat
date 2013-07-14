ERASE *.class
javac *.java
jar -cvf Mozaiek.jar *.class
jarsigner Mozaiek.jar tstkey
PAUSE