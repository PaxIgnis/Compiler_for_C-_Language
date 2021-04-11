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
import java.util.*;

class CM {
  public static boolean SHOW_TREE = false;
  public static boolean SHOW_SYM = false;
  public static boolean SHOW_CODE = false;
  public static boolean ASTError = false;
  public static boolean SYMError = false;
  static public void main(String argv[]) {
    
    String fileName;
    if (argv[0].equals("-a")) {
      SHOW_TREE = true;

      fileName = argv[1];
      try {
        /* prints out tree to file in same folder as source file */
        PrintStream o = new PrintStream(new File(fileName.replace(".cm", "") + ".abs"));
        System.setOut(o);
        // System.setErr(o);
      } catch (FileNotFoundException ex) {
        ex.printStackTrace();
      }
    } else if (argv[0].equals("-s")) {
      SHOW_SYM = true;
      fileName = argv[1];
      try {
        /* prints out tree to file in same folder as source file */
        PrintStream o = new PrintStream(new File(fileName.replace(".cm", "") + ".sym"));
        System.setOut(o);
        // System.setErr(o);
      } catch (FileNotFoundException ex) {
        ex.printStackTrace();
      }
    } else if (argv[0].equals("-c")) {
      SHOW_CODE = true;
      fileName = argv[1];
    } else {
      fileName = argv[0];
    }
    /* Start the parser */
    try {
      System.out.println(fileName);
      parser p = new parser(new Lexer(new FileReader(fileName)));
      Absyn result = (Absyn) (p.parse().value);
      if (SHOW_TREE && result != null) {
        System.out.println("\nThe abstract syntax tree is:");
        ShowTreeVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0, false);
      }
      if (SHOW_SYM && result != null) {
        System.out.println("\nEntering the global scope:");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        result.accept(analyzer, 1, false);
        analyzer.printHash(analyzer.table, 1);
        System.out.println("Leaving the global scope");
      }
      if (SHOW_CODE && result != null) {
       
        PrintStream o = System.out;
        System.setOut(new PrintStream(new OutputStream() {
          public void write(int b) {}
        }));
        ShowTreeVisitor visitor = new ShowTreeVisitor();
        result.accept(visitor, 0, false);
        if (visitor.errorFound == true) {
          ASTError = true;
        }
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        result.accept(analyzer, 1, false);
        analyzer.printHash(analyzer.table, 1);
        if (analyzer.errorFound == true) {
          SYMError = true;
        }
        System.setOut(o);

        if (ASTError == true) {
          System.err.println("Error(s) found when generating the AST, cannot continue.");
        } else if (SYMError == true) {
          System.err.println("Error(s) found when generating symbol table/type checking, cannot continue.");
        } else {
          System.out.println("starting code generator");
          CodeGenerator gen = new CodeGenerator(fileName.replace(".cm", "") + ".tm");
          result.accept(gen, 0, false);
        }
        
      }
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}
