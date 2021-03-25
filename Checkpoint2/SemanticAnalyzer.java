import absyn.*;
import java.util.*;
import java.util.function.Function;

public class SemanticAnalyzer implements AbsynVisitor {
    HashMap<String, ArrayList<NodeType>> table;
    final static int SPACES = 4;
    private String currentFunc = "";

    public SemanticAnalyzer() {
        this.table = new HashMap<String, ArrayList<NodeType>>();
    }

    private void indent(int level) {
        for (int i = 0; i < level * SPACES; i++)
            System.out.print(" ");
    }

    public void visit(ExpList expList, int level) {
        while (expList != null) {
            expList.head.accept(this, level);
            expList = expList.tail;
        }
    }

    public void visit(DecList decList, int level) {
        while (decList != null) {
            decList.head.accept(this, level);
            decList = decList.tail;
        }
    }

    public void visit(VarDecList varDecList, int level) {
        while (varDecList != null) {
            varDecList.head.accept(this, level);
            varDecList = varDecList.tail;
        }
    }

    public void visit(AssignExp exp, int level) {
        // indent(level);
        // System.out.println("AssignExp:");
        // level++;
        
        exp.lhs.accept(this, level);
        exp.rhs.accept(this, level);
        // CallExp, OpExp, VarExp
        
        if (getType(exp.lhs) != getType(exp.rhs)) {
            System.out.println("Error, assignment types do not match at: row "+exp.row+" column "+exp.col);
        }
        // System.out.println(exp.lhs + " "+exp.rhs);
    }

    public void visit(IfExp exp, int level) {
        indent(level);
        System.out.println("Entering if block:");
        level++;
        if (exp.test != null) {
            exp.test.accept(this, level);
            if (getType(exp.test) != 0) {
                System.out.println("Error, test condition for if stmt must be of type int at: row "+exp.row+" column "+exp.col);
            }
        } else {
            // indent(level);
            // System.out.println("Invalid Expresion Statement");
        }

        exp.thenpart.accept(this, level);
        if (exp.elsepart != null)
            exp.elsepart.accept(this, level);
        
        printHash(table, level);
        indent(level - 1);
        System.out.println("Leaving if block");
        clearMapLevel(table, level);
    }

    public void visit(IntExp exp, int level) {
        // if (exp.dType == null) {
        //     System.out.println("NULL");
        //     // exp.dType = 0;
        // } else {
        //     System.out.println("not NULL " + exp.dType);
        // }
        // indent(level);
        // System.out.println("IntExp: " + exp.value);
    }

    public void visit(OpExp exp, int level) {
        // indent(level);
        // System.out.print("OpExp:");
        // switch (exp.op) {
        // case OpExp.PLUS:
        //     System.out.println(" + ");
        //     break;
        // case OpExp.MINUS:
        //     System.out.println(" - ");
        //     break;
        // case OpExp.TIMES:
        //     System.out.println(" * ");
        //     break;
        // case OpExp.OVER:
        //     System.out.println(" / ");
        //     break;
        // case OpExp.EQ:
        //     System.out.println(" = ");
        //     break;
        // case OpExp.LT:
        //     System.out.println(" < ");
        //     break;
        // case OpExp.GT:
        //     System.out.println(" > ");
        //     break;
        // case OpExp.GTEQ:
        //     System.out.println(" >= ");
        //     break;
        // case OpExp.LTEQ:
        //     System.out.println(" <= ");
        //     break;
        // case OpExp.EQUALS:
        //     System.out.println(" == ");
        //     break;
        // case OpExp.NEQUALS:
        //     System.out.println(" != ");
        //     break;
        // default:
        //     System.out.println("Unrecognized operator at line " + exp.row + " and column " + exp.col);
        // }
        // level++;
        exp.left.accept(this, level);
        exp.right.accept(this, level);
        if (getType(exp.left) != getType(exp.right)) {
            System.out.println("Error, operand types do not match at: row "+exp.row+" column "+exp.col);
        }
    }

    public void visit(VarExp exp, int level) {
        // indent(level);
        // System.out.println("VarExp: ");
        exp.variable.accept(this, ++level);
    }

    public void visit(NilExp exp, int level) {
        // indent(level);
        // System.out.println("NilExp");
    }

    public void visit(NameTy typ, int level) {
        // indent(level);
        // System.out.print("NameTy:");
        // switch (typ.typ) {
        // case NameTy.INT:
        //     System.out.println(" INT ");
        //     break;
        // case NameTy.VOID:
        //     System.out.println(" VOID ");
        //     break;
        // default:
        //     System.out.println("Unrecognized type at line " + typ.row + " and column " + typ.col);
        // }
    }

    public void visit(SimpleVar var, int level) {
        // indent(level);
        // System.out.println("SimpleVar: " + var.name);
        // Add key to hash map at the current level
        if (!table.containsKey(var.name)) {
            System.out.println("Error, use of undefined variable '"+var.name+"' at: row "+var.row+" column "+var.col);
        }
    }

    public void visit(IndexVar var, int level) {
        var.index.accept(this, ++level);
        // check if index is of int type
        if (!table.containsKey(var.name)) {
            System.out.println("Error, use of undefined array variable '" + var.name + "[]' at: row " + var.row
                    + " column " + var.col);
        } else {
            if (var.index instanceof IntExp) {
            } else if (var.index instanceof VarExp || var.index instanceof CallExp) {
                if (var.index instanceof CallExp) {
                    if (!table.containsKey(((CallExp) var.index).func)) {
                        System.out.println("Error, array index has undefined function '" + var.name + "[]' at: row "
                                + var.row + " column " + var.col);
                    } else {
                        if (!isInteger(((CallExp) var.index).func)) {
                            System.out.println("Error, array index must be of type int '" + var.name + "[]' at: row "
                                + var.row + " column " + var.col);
                        }
                    }
                } else if (((VarExp) var.index).variable instanceof SimpleVar) {

                    if (!table.containsKey(((SimpleVar)((VarExp) var.index).variable).name)) {
                        System.out.println("Error, array index has undefined function '" + var.name + "[]' at: row "
                                + var.row + " column " + var.col);
                    } else {
                        if (!isInteger(((SimpleVar)((VarExp) var.index).variable).name)) {
                            System.out.println("Error, array index must be of type int '" + var.name + "[]' at: row "
                                + var.row + " column " + var.col);
                        }
                    }
                } else if (((VarExp) var.index).variable instanceof IndexVar) {
                    if (!table.containsKey(((IndexVar)((VarExp) var.index).variable).name)) {
                        System.out.println("Error, array index has undefined function '" + var.name + "[]' at: row "
                                + var.row + " column " + var.col);
                    } else {
                        if (!isInteger(((IndexVar)((VarExp) var.index).variable).name)) {
                            System.out.println("Error, array index must be of type int '" + var.name + "[]' at: row "
                                + var.row + " column " + var.col);
                        }
                    }
                }
            } else {
                System.out.println("Error, array index must be of type int '" + var.name + "[]' at: row " + var.row
                        + " column " + var.col);
            }
        }
    }

    public void visit(CallExp exp, int level) {
        // indent(level);
        // System.out.println("CallExp: " + exp.func);
        if (exp.args != null)
            exp.args.accept(this, ++level); /* TODO: Might change */
        if (!table.containsKey(exp.func)) {
            System.out.println("Error, use of undefined function '"+exp.func+"[]' at: row "+exp.row+" column "+exp.col);
        } else {
            NodeType func = table.get(exp.func).get(0);
            if (func == null) {
                System.out.println("Error, use of undefined function '"+exp.func+"[]' at: row "+exp.row+" column "+exp.col);
            } else {
                VarDecList paramL = ((FunctionDec) func.def).params;
                String params = "";
                if (exp.args != null && paramL != null) {
                    ExpList args = exp.args;
                    while (paramL != null) {
                        if (paramL.head instanceof SimpleDec) {
                            params += (((SimpleDec) paramL.head).typ.typ == 0 ? " INT" : " VOID");
                            if (getType(args.head) != ((SimpleDec) paramL.head).typ.typ) {
                                System.out.println("Error, incorrect params in function call '"+exp.func+"[]' at: row "+exp.row+" column "+exp.col);
                                break;
                            }
                        } else if (paramL.head instanceof ArrayDec) {
                            params += (((ArrayDec) paramL.head).typ.typ == 0 ? " INT[]" : " VOID[]");
                            if (getType(args.head) != ((ArrayDec) paramL.head).typ.typ) {
                                System.out.println("Error, incorrect params in function call '"+exp.func+"[]' at: row "+exp.row+" column "+exp.col);
                                break;
                            }
                        }
                        args = args.tail;
                        paramL = paramL.tail;
                        
                        if (args == null) {
                            if (paramL != null) {
                                System.out.println("Error, incorrect params in function call '"+exp.func+"[]' at: row "+exp.row+" column "+exp.col);
                            }
                            break;
                        }
                    }
                } else if ((paramL != null && exp.args == null) || (paramL == null && exp.args != null)) {
                    if (paramL == null && !params.equals("VOID")) {
                        System.out.println("Error, incorrect params in function call '"+exp.func+"[]' at: row "+exp.row+" column "+exp.col);
                    }
                }
            }
        }
    }

    public void visit(WhileExp exp, int level) {
        indent(level);
        System.out.println("Entering while block:");
        level++;
        exp.test.accept(this, level);
        exp.body.accept(this, level);
        printHash(table, level);
        if (getType(exp.test) != 0) {
            System.out.println("Error, test condition for while loop must be of type int at: row "+exp.row+" column "+exp.col);
        }
        indent(level - 1);
        System.out.println("Leaving while block");
        clearMapLevel(table, level);
    }

    public void visit(ReturnExp exp, int level) {
        // indent(level);
        // System.out.println("ReturnExp:");
        // 
        if (exp.exp instanceof NilExp) {
            if (isInteger(currentFunc)) {
                System.out.println("Error, mismatched return type, should be void '"+currentFunc+"[]' at: row "+exp.row+" column "+exp.col);
            }
        } else {
            exp.exp.accept(this, ++level);
            if (!(getType(exp.exp) == 0 && isInteger(currentFunc)) && !(getType(exp.exp) == 1 && !isInteger(currentFunc))) {
                System.out.println("Error, mismatched return type '"+currentFunc+"[]' at: row "+exp.row+" column "+exp.col);
            }
        }
    }

    public void visit(CompoundExp exp, int level) {
        // indent(level);
        // System.out.println("CompoundExp: ");
        // level++;
        if (exp.decs != null)
            exp.decs.accept(this, level);
        if (exp.exps != null)
            exp.exps.accept(this, level);
    }

    public void visit(FunctionDec dec, int level) {
        // Add key to hash map at the current level
        if (!table.containsKey(dec.func)) {
            table.put(dec.func, new ArrayList<NodeType>());
        }
        // table.get(dec.func).add(new NodeType(dec.func, dec, level));
        addToHash(table, new NodeType(dec.func, dec, level));

        indent(level);
        System.out.println("Entering the scope for the function " + dec.func + ":");
        level++;
        currentFunc = dec.func;
        dec.params.accept(this, level);
        if (dec.body != null) {
            dec.body.accept(this, level);
        }
        
        
        
        printHash(table, level);
        indent(level - 1);
        System.out.println("Leaving the function scope");
        // dec.result.accept(this, level);
        // clears all items from inner scopes
        clearMapLevel(table, level);
        
    }

    public void visit(SimpleDec varDec, int level) {
        if (!varDec.name.isEmpty()) {
            if (varDec.typ.typ == 0) {
                // Add key to hash map at the current level
                if (!table.containsKey(varDec.name)) {
                    table.put(varDec.name, new ArrayList<NodeType>());
                }
                // table.get(varDec.name).add(new NodeType(varDec.name, varDec, level));
                addToHash(table, new NodeType(varDec.name, varDec, level));
            } else {
                System.out.println("Error, variable cannot be void '" + varDec.name + "' at: row " + varDec.row
                        + " column " + varDec.col);
            }

        }
        // indent(level);
        // System.out.println("SimpleDec: " + varDec.name);
        varDec.typ.accept(this, ++level);

    }

    public void visit(ArrayDec varDec, int level) {
        if (varDec.typ.typ == 0) {
            // Add key to hash map at the current level
            if (!table.containsKey(varDec.name)) {
                table.put(varDec.name, new ArrayList<NodeType>());
            }
            // table.get(varDec.name).add(new NodeType(varDec.name, varDec, level));
            addToHash(table, new NodeType(varDec.name, varDec, level));
        } else {
            System.out.println("Error, array variable cannot be void '" + varDec.name + "[]' at: row " + varDec.row + " column "
                    + varDec.col);
        }

        // indent(level);
        // System.out.println("ArrayDec: " + varDec.name);
        // final int fLevel = level++;
        // varDec.typ.accept(this, level);
        // varDec.size.accept(this, level);
        
        // remove hashmap from all levels higher than the current one
        // table.forEach((s, nodeList) -> {
        //     Iterator<NodeType> i = nodeList.iterator();
        //     while (i.hasNext()) {
        //         NodeType n = i.next();
        //         if (n.level < fLevel) {
        //             i.remove();
        //         }
        //     }
        // });
    }

    public void printHash(HashMap<String, ArrayList<NodeType>> table, final int level) {
        table.forEach((s, nodeList) -> {
            Iterator<NodeType> i = nodeList.iterator();
            while (i.hasNext()) {
                NodeType n = i.next();
                if (n.level == level) {
                    int type = 0;
                    indent(level);
                    if (n.def instanceof SimpleDec) {
                        type = ((SimpleDec) n.def).typ.typ;
                        System.out.println(n.name + ": " + (type == 0 ? "INT" : "VOID"));
                    } else if (n.def instanceof ArrayDec) {
                        type = ((ArrayDec) n.def).typ.typ;
                        String arraySize = ((ArrayDec) n.def).size.value.equals("0") ? ""
                                : ((ArrayDec) n.def).size.value;
                        System.out.println(n.name + "[" + arraySize + "]: " + (type == 0 ? "INT" : "VOID"));
                    } else if (n.def instanceof FunctionDec) {
                        type = ((FunctionDec) n.def).result.typ;
                        String params = "";
                        VarDecList paramL = ((FunctionDec) n.def).params;
                        while (paramL != null) {
                            if (paramL.head instanceof SimpleDec) {
                                params += (((SimpleDec) paramL.head).typ.typ == 0 ? " INT," : " VOID,");
                            } else if (paramL.head instanceof ArrayDec) {
                                params += (((ArrayDec) paramL.head).typ.typ == 0 ? " INT[]," : " VOID[],");
                            }
                            paramL = paramL.tail;
                        }
                        if (params.length() != 0) {
                            params = params.substring(0, params.length() - 1);
                            params += " ";
                        }

                        System.out.println(n.name + ": " + "(" + params + ") -> " + (type == 0 ? "INT" : "VOID"));
                    }

                }

            }
        });
    }

    public void clearMapLevel(HashMap<String, ArrayList<NodeType>> table, final int level) {
        table.forEach((s, nodeList) -> {
            Iterator<NodeType> i = nodeList.iterator();
            while (i.hasNext()) {
                NodeType n = i.next();
                if (n.level >= level) {
                    i.remove();
                }
            }
        });
    }

    public void addToHash(HashMap<String, ArrayList<NodeType>> table, NodeType node) {
        Iterator<NodeType> i = table.get(node.name).iterator();
        while (i.hasNext()) {
            NodeType n = i.next();
            if (n.level == node.level) {
                System.out.println("Error, double declaration at: row " + node.def.row + ", column " + node.def.col
                        + ". Initial declaration at: row " + n.def.row + ", column " + n.def.col + ".");
                return;
            }
        }
        table.get(node.name).add(node);
    }

    boolean isInteger(String name) {
        Iterator<NodeType> i = table.get(name).iterator();
        while (i.hasNext()) {
            NodeType n = i.next();
            if (n.def instanceof FunctionDec && ((FunctionDec)n.def).result.typ == 0) {
                return true;
            } else if (n.def instanceof SimpleDec && ((SimpleDec) n.def).typ.typ == 0) {
                return true;
            } else if (n.def instanceof ArrayDec && ((ArrayDec) n.def).typ.typ == 0) {
                return true;
            }
        }
        return false;
        // System.out.println(((FunctionDec)dType).result + "  " + ((FunctionDec)dType).func);
        // if (dType instanceof SimpleDec && ((SimpleDec)dType).typ.typ == 0) {
        //     return true;
        // } else if (dType instanceof ArrayDec && ((ArrayDec)dType).typ.typ == 0) {
        //     return true;
        // } else if (dType instanceof FunctionDec && ((FunctionDec)dType).result.typ == 0) {
        //     return true;
        // }
        // return false;
    }
    /**
     * getType
     * 
     * @param exp
     * @return -1 for error, 0 for int and 1 for void
     */
    int getType(Exp exp) {
        // System.out.println(exp);
        if (exp instanceof IntExp) {
            return 0;
        } else if (exp instanceof VarExp || exp instanceof CallExp) {
            if (exp instanceof CallExp) {
                if (!table.containsKey(((CallExp) exp).func)) {
                    return -1;
                } else {
                    if (!isInteger(((CallExp) exp).func)) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            } else if (((VarExp) exp).variable instanceof SimpleVar) {
                if (!table.containsKey(((SimpleVar)((VarExp) exp).variable).name)) {
                    return -1;
                } else {
                    if (!isInteger(((SimpleVar)((VarExp) exp).variable).name)) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            } else if (((VarExp) exp).variable instanceof IndexVar) {
                if (!table.containsKey(((IndexVar)((VarExp) exp).variable).name)) {
                    return -1;
                } else {
                    if (!isInteger(((IndexVar)((VarExp) exp).variable).name)) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            } else {
                return -1;
            }
        } else if (exp instanceof OpExp) {
            return getType(((OpExp) exp).left);
        } else if (exp instanceof AssignExp) {
            return getType(((AssignExp) exp).lhs);
        }
        return -1;
    }
}