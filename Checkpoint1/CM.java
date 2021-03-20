/*
  Created by: Fei Song
  File Name: Main.java
  To Build: 
  After the scanner, tiny.flex, and the parser, tiny.cup, have been created.
    javac Main.java
  
  To Run: 
    java -classpath /usr/share/java/cup.jar:. Main gcd.tiny

  where gcd.tiny is an test input file for the tiny language.
*/

import java.io.*;
import absyn.*;

class CM {
  public static boolean SHOW_TREE = false;

  static public void main(String argv[]) {
    String fileName;
    if (argv[0].equals("-a")) {
      SHOW_TREE = true;
      fileName = argv[1];
      try {
        /* prints out tree to file in same folder as source file */
        PrintStream o = new PrintStream(new File(fileName.replace(".cm", "") + ".abs"));
        System.setOut(o);
        System.setErr(o);
      } catch (FileNotFoundException ex) {
        ex.printStackTrace();
      }
    } else {
      fileName = argv[0];
    }
    /* Start the parser */
    try {
      parser p = new parser(new Lexer(new FileReader(fileName)));
      Absyn result = (Absyn) (p.parse().value);
      if (SHOW_TREE && result != null) {
        System.out.println("The abstract syntax tree is:");
        ShowTreeVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0);
      }
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}
