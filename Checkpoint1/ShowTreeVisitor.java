import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( ExpList expList, int level ) {
    while( expList != null ) {
      expList.head.accept( this, level );
      expList = expList.tail;
    } 
  }

  public void visit( DecList decList, int level ) {
    while( decList != null ) {
      decList.head.accept( this, level );
      decList = decList.tail;
    } 
  }

  public void visit( VarDecList varDecList, int level ) {
    while( varDecList != null ) {
      varDecList.head.accept( this, level );
      varDecList = varDecList.tail;
    } 
  }

  public void visit( AssignExp exp, int level ) {
    indent( level );
    System.out.println( "AssignExp:" );
    level++;
    exp.lhs.accept( this, level );
    exp.rhs.accept( this, level );
  }

  public void visit( IfExp exp, int level ) {
    indent( level );
    System.out.println( "IfExp:" );
    level++;
    exp.test.accept( this, level );
    exp.thenpart.accept( this, level );
    if (exp.elsepart != null )
       exp.elsepart.accept( this, level );
  }

  public void visit( IntExp exp, int level ) {
    indent( level );
    System.out.println( "IntExp: " + exp.value ); 
  }

  public void visit( OpExp exp, int level ) {
    indent( level );
    System.out.print( "OpExp:" ); 
    switch( exp.op ) {
      case OpExp.PLUS:
        System.out.println( " + " );
        break;
      case OpExp.MINUS:
        System.out.println( " - " );
        break;
      case OpExp.TIMES:
        System.out.println( " * " );
        break;
      case OpExp.OVER:
        System.out.println( " / " );
        break;
      case OpExp.EQ:
        System.out.println( " = " );
        break;
      case OpExp.LT:
        System.out.println( " < " );
        break;
      case OpExp.GT:
        System.out.println( " > " );
        break;
      case OpExp.GTEQ:
        System.out.println( " >= " );
        break;
      case OpExp.LTEQ:
        System.out.println( " <= " );
        break;
      case OpExp.EQUALS:
        System.out.println( " == " );
        break;
      case OpExp.NEQUALS:
        System.out.println( " != " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    exp.left.accept( this, level );
    exp.right.accept( this, level );
  }

  public void visit( VarExp exp, int level ) {
    indent( level );
    System.out.println( "VarExp: " );
    exp.variable.accept(this, ++level );
  }

  public void visit( NilExp exp, int level ) {
    indent( level );
    System.out.println( "NilExp");
  }

  public void visit( NameTy typ, int level ) {
    indent( level );
    System.out.print( "NameTy:" ); 
    switch( typ.typ ) {
      case NameTy.INT:
        System.out.println( " INT " );
        break;
      case NameTy.VOID:
        System.out.println( " VOID " );
        break;
      default:
        System.out.println( "Unrecognized type at line " + typ.row + " and column " + typ.col);
    }
  }

  public void visit( SimpleVar var, int level ) {
    indent( level );
    System.out.println( "SimpleVar: " + var.name );
  }

  public void visit( IndexVar var, int level ) {
    indent( level );
    System.out.println( "IndexVar: " + var.name );
    var.index.accept(this, ++level );
  }

  public void visit( CallExp exp, int level ) {
    indent( level );
    System.out.println( "CallExp: " + exp.func );
    if (exp.args != null)
      exp.args.accept(this, ++level ); /* TODO: Might change */
  }

  public void visit( WhileExp exp, int level ) {
    indent( level );
    System.out.println( "WhileExp:" );
    level++;
    exp.body.accept(this, level );
    exp.test.accept(this, level );
  }

  public void visit( ReturnExp exp, int level ) {
    indent( level );
    System.out.println( "ReturnExp:" );
    exp.exp.accept(this, ++level );
  }

  public void visit( CompoundExp exp, int level ) {
    indent( level );
    System.out.println( "CompoundExp: " );
    level++;
    if (exp.decs != null)
      exp.decs.accept(this, level );
    if (exp.exps != null)
      exp.exps.accept(this, level );
  }

  public void visit( FunctionDec dec, int level ) {
    indent( level );
    System.out.println( "FunctionDec: " + dec.func );
    level++;
    dec.result.accept(this, level );
    dec.params.accept(this, level );
    dec.body.accept(this, level );
  }

  public void visit( SimpleDec varDec, int level ) {
    indent( level );
    System.out.println( "SimpleDec: " + varDec.name );
    varDec.typ.accept(this, ++level );
  }

  public void visit( ArrayDec varDec, int level ) {
    indent( level );
    System.out.println( "ArrayDec: " + varDec.name );
    level++;
    varDec.typ.accept(this, level );
    varDec.size.accept(this, level );
  }

}
