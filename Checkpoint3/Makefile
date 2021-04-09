JAVA=java
JAVAC=javac
JFLEX=jflex
CLASSPATH=-cp ./projects/java-cup-11b.jar:.
CUP=$(JAVA) $(CLASSPATH) java_cup.Main
#JFLEX=jflex
#CLASSPATH=-cp /usr/share/java/cup.jar:.
#CUP=cup

all: CM.class

CM.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java Scanner.java CM.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

Lexer.java: cminus.flex
	$(JFLEX) cminus.flex

parser.java: cminus.cup
	#$(CUP) -dump -expect 3 cminus.cup
	$(CUP) -expect 3 cminus.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class absyn/._* *~

test1:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/1.cm

test2:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/2.cm

test3:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/3.cm

test4:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/4.cm

test5:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/5.cm

testCodeGen:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/codegen.cm
