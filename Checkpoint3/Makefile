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

#Test cases for AST generation (checkpoint 1)
testAST1:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/1.cm

testAST2:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/2.cm

testAST3:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/3.cm

testAST4:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/4.cm

testAST5:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/5.cm

testAST6:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/6.cm

testAST7:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/7.cm

testAST8:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/8.cm

testAST9:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/9.cm

testAST0:
	java -cp projects/java-cup-11b.jar:. CM -a C-programs/0.cm


#Test cases for SYMbol table (checkpoint 2)
testSYM1:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/1.cm

testSYM2:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/2.cm

testSYM3:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/3.cm

testSYM4:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/4.cm

testSYM5:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/5.cm

testSYM6:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/6.cm

testSYM7:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/7.cm

testSYM8:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/8.cm

testSYM9:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/9.cm

testSYM0:
	java -cp projects/java-cup-11b.jar:. CM -s C-programs/0.cm

#Test cases for tm code generation (checkpoint 3)
testTM1:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/1.cm

testTM2:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/2.cm

testTM3:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/3.cm

testTM4:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/4.cm

testTM5:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/5.cm

testTM6:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/6.cm

testTM7:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/7.cm

testTM8:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/8.cm

testTM9:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/9.cm

testTM0:
	java -cp projects/java-cup-11b.jar:. CM -c C-programs/0.cm

