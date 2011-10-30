#To compile with java version 1.4 change the compile command to
#   javac -source 1.4 -classpath "." *.java
all:
	/usr/lib/jvm/java-7-openjdk-i386/bin/javac  *.java

clean:
	rm -rf *.class 


