JAVAC=javac
JFLEX=jflex
#JFLEX=/Users/fsong/projects/jflex/bin/jflex

all: Token.class Lexer.class Scanner.class

%.class: %.java
	$(JAVAC) $^

Lexer.java: tiny.flex
	$(JFLEX) tiny.flex

clean:
	rm -f Lexer.java *.class *~
