/*
  File Name: tiny.flex
  JFlex specification for the TINY language
*/

import java.util.ArrayList;
import java.util.List;
   
%%
   
%class Lexer
%type Token
%line
%column
    
%eofval{
  //System.out.println("*** Reaching end of file");
  if (tagStack.size() > 0) {
      System.err.println("Error, items left in stack at end of execution:");
      for (int i = 0; i < tagStack.size(); i++) {
         System.err.println("OPEN-"+tagStack.get(i));
      }
  }
  return null;
%eofval};

%{
  private static ArrayList<String> tagStack = new ArrayList<String>();
  private static List<String> relevantTags = List.of("DOC", "TEXT", "DATE", "DOCNO", "HEADLINE", "LENGTH","P");
  // keeps track of the first nonrelevant tag in the stack
  private static int notRelevantIndex = -1;

/**
 * Checks if the stack contains only relevant tags.
 * If empty or containing nonrelevant tags, returns false.
 * 
 * @return Boolean if all tags in stack relevant
 */
  private static Boolean isStackRelevant() {
     if (notRelevantIndex == -1 && tagStack.size() > 0) {
        return true;
     } else {
        return false;
     }
  }

/**
 * Checks if tag is one of the ones listed as relevant.
 * If relevant, returns true.
 *
 * @param input a tag 
 * @return returns true if tag is one of the relevent ones
 */
  private static Boolean isTagRelevant(String input) {
     if (relevantTags.contains(input)) {
        if (input.equals("P") && !isStackRelevant()) {
           return false;
        }
        return true;
     }
     return false;
  }

/**
 * Adds a new tag to the stack, if it is not a relavant tag,
 * it updates the relevant variable.
 *
 * @param input the new tag
 * @return void
 */
  private static void pushOpenTag(String input) {
     tagStack.add(input);
     if (isStackRelevant() && !isTagRelevant(input)) {
        notRelevantIndex = tagStack.size() - 1;
     }
  }

/**
 * Checks if close tag matches the tag on the top of the stack.
 * If it matches, it removes the tag from the stack, and returns true.
 * If it was the last nonrelevant tag in the stack, it resets the 
 * relevant variable.
 * 
 * @param input a close tag
 * @return returns true if close tag matched top of stack
 */
  private static Boolean popCloseTag(String input) {
     if (tagStack.get(tagStack.size()-1).equals(input)) {
        // set stack back to relevant
        if (tagStack.size() - 1 == notRelevantIndex) {
           notRelevantIndex = -1;
        }
        tagStack.remove(tagStack.size()-1);
        return true;
     }
     return false;
  }
%};

/* A line terminator is a \r (carriage return), \n (line feed), or \r\n. */
LineTerminator = \r|\n|\r\n
   
/* White space is a line terminator, space, tab, or form feed. */
WhiteSpace     = {LineTerminator} | [ \t\f]
   
/* A literal integer is is a number beginning with a number between
   one and nine followed by zero or more numbers between zero and nine
   or just a zero.  */
digit = [0-9]
number = {digit}+

/* Either a positive or negative sign */
sign = (\+|\-)?

/* A number that could have a sign, and can include decimal places with or without a preceding 
   number before the decimal place */
signedNumber = ({sign}{number}\.{digit}+)|({sign}{digit}*\.{number})|({sign}{number})

/* A alphanumeric string that includes atleast one alpha character */
word = ([0-9]*[a-zA-Z]+[0-9]*)+

/* A alphanumeric word that includes at least one hyphen within the word, 
   and at least one alpha character. Hyphen cannot appear at beginning or end of word. */
hyphenated = ((([0-9]*[a-zA-Z]+[0-9]*)+-)+[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*)|(([a-zA-Z0-9]+-)+([0-9]*[a-zA-Z]+[0-9]*)+(-[a-zA-Z0-9]+)*)

/* A alphanumeric word that includes at least one apostrophe within the word, 
   and at least one alpha character. Apostrophe cannot appear at beginning or end of word.
   Word can also begin with a hyphenated word, if and only if the only single apostrophe appears 
   after the hyphenated word. */
apostrophized = ((([0-9]*[a-zA-Z]+[0-9]*)+')+[a-zA-Z0-9]+('[a-zA-Z0-9]+)*)|(([a-zA-Z0-9]+')+([0-9]*[a-zA-Z]+[0-9]*)+('[a-zA-Z0-9]+)*)|(({hyphenated})'[a-zA-Z]+)

/* Any character the is not a letter, number or whitespace. */
punctuation = [^a-zA-Z0-9 \t\f\n]

/* A string that begins with '<' and ends with '>' and only includes a single '>'
   and where the second character is not '/' */
OPENTAG = (<[^\/>]+>)|(<[^\/>][^>]+>)|<>
/* A string that begins with '</' and ends with '>' and only includes a single '>' */
CLOSETAG = <[\/][^\>]*>
   
%%
   
/*
   This section contains regular expressions and actions, i.e. Java
   code, that will be executed when the scanner matches the associated
   regular expression. */
   
{OPENTAG}          { String value = yytext();
                     //extracts the tag from between the <> and capitalizes string
                     value = value.substring(1, value.length()-1).trim().split(" ",2)[0].toUpperCase();
                     if (value.length() > 0) {
                        pushOpenTag(value);
                        if (isTagRelevant(value)) {
                           return new Token(Token.OPENTAG, value, yyline, yycolumn);
                        }
                     }
                   }
{CLOSETAG}         { String value = yytext();
                     //extracts the tag from between the </> and capitalizes string
                     value = value.substring(2, value.length()-1).trim().split(" ",2)[0].toUpperCase();
                     if (value.length() > 0) {
                        if (popCloseTag(value)) {
                           if (isTagRelevant(value)) {
                              return new Token(Token.CLOSETAG, value, yyline, yycolumn); 
                           }
                        } else {
                           System.err.println("Error, mismatched CLOSE-"+value+" at line: "+yyline);
                        }
                     }
                   }
{apostrophized}    { if (isStackRelevant()) { return new Token(Token.APOSTROPHIZED, yytext(), yyline, yycolumn); }}
{hyphenated}       { if (isStackRelevant()) { return new Token(Token.HYPHENATED, yytext(), yyline, yycolumn); }}
{word}             { if (isStackRelevant()) { return new Token(Token.WORD, yytext(), yyline, yycolumn); }}
{signedNumber}     { if (isStackRelevant()) { return new Token(Token.NUMBER, yytext(), yyline, yycolumn); }}
{punctuation}      { if (isStackRelevant()) { return new Token(Token.PUNCTUATION, yytext(), yyline, yycolumn); }}
{WhiteSpace}+      { /* skip whitespace */ }  
