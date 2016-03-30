JFLAGS = -g
JC = javac
.SUFFIXES:	.java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	test.java

default:	classes

classes:	$(CLASSES:.java=.class)
	chmod a+x pipair 

clean:	
	$(RM) *.class
