import absyn.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CodeGenerator implements AbsynVisitor {
    HashMap<String, ArrayList<NodeType>> table;
    int mainEntry, globalOffset, offset;
    static int emitLoc = 0;
    static int highEmitLoc = 0;
    final int pc = 7;
    final int gp = 6;
    final int fp = 5;
    final int ac = 0;
    final int ac1 = 1;
    final int ofpFO = 0;
    final int retFO = -1;
    final int initFO = -2;
    String filename;
    String currentFunc = "";


    public CodeGenerator(String filename) {
        this.filename = filename;
        this.table = new HashMap<String, ArrayList<NodeType>>();
    }



    public void visit(DecList decList, int offset, boolean isAddress) {
        clearFile();

        // add input function to hashmap
        table.put("input", new ArrayList<NodeType>());
        VarDecList params = new VarDecList(new SimpleDec(0, 0, new NameTy(0, 0, NameTy.VOID), ""), null);
        addToHash(table, new NodeType("input", new FunctionDec(0, 0, new NameTy(0, 0, NameTy.INT), "input", params, null, 4), 0));

        // add output function to hashmap
        table.put("output", new ArrayList<NodeType>());
        params = new VarDecList(new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INT), "a"), null);
        addToHash(table, new NodeType("output", new FunctionDec(0, 0, new NameTy(0, 0, NameTy.VOID), "output", params, null, 7), 0));

        // prelude for code generation
        emitComment("code for prelude");
        emitRM("LD", gp, 0, ac, "load up with maxaddr");
        emitRM("LDA", fp, 0, gp, "copy gp to fp");
        emitRM("ST", 0, 0, ac, "clear content at loc 0");

        // save location to jump around i/o functions
        int savedLoc = emitSkip(1);
        emitComment("jumping around i/o functions");

        // code for input routine
        emitComment("code for input routine");
        emitRM("ST", ac, -1, fp, "store return");
        emitRO("IN", 0, 0, 0, "input");
        emitRM("LD", pc, -1, fp, "return to caller");

        // code for output routine
        emitComment("code for output routine");
        emitRM("ST", ac, -1, fp, "store return");
        emitRM("LD", ac, -2, fp, "load output value");
        emitRO("OUT", 0, 0, 0, "output");
        emitRM("LD", pc, -1, fp, "return to caller");

        // jump around i/o functions
        int savedLoc2 = emitSkip(0);
        emitBackup(savedLoc);
        emitRM_Abs("LDA", pc, savedLoc2, "jump around i/o code");
        emitRestore();

        // process all code
        emitComment("code for body of program");
        while (decList != null) {
            decList.head.accept(this, offset, false);
            decList = decList.tail;
        }

        // finale
        NodeType func = table.get("main").get(0);
        mainEntry = ((FunctionDec)func.def).funcAddr;
        
        emitComment("code for finale");
        emitRM("ST", fp, globalOffset + ofpFO, fp, "push ofp");
        emitRM("LDA", fp, globalOffset, fp, "push frame");
        emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
        emitRM_Abs("LDA", pc, mainEntry, "jump to main loc");
        emitRM("LD", fp, ofpFO, fp, "pop frame");
        emitRO("HALT", 0, 0, 0, "");

    }

    @Override
    public void visit(ExpList expList, int level, boolean isAddr) {
        System.out.println("here1 ExpList");
        while (expList != null) {
            expList.head.accept(this, level, false);
            expList = expList.tail;
        }
    }

    @Override
    public void visit(AssignExp exp, int level, boolean isAddr) {
        System.out.println("here2 AssignExp");

        emitComment("-> op");
        if (exp.lhs.variable instanceof SimpleVar) {
            exp.lhs.accept(this, level, true);
            emitRM("ST", ac, offset, fp, "op: push left");
            offset--;
        } else {
            System.out.println("notsimplevar");
            exp.lhs.accept(this, level, false);
            offset--;
        }
        exp.rhs.accept(this, level, false);

        emitRM("LD", 1, ++offset, fp, "op: load left");
        emitRM("ST", ac, 0, 1, "assign: store value");
        emitComment("<- op");
    }

    @Override
    public void visit(IfExp exp, int level, boolean isAddr) {
        System.out.println("here3 IfExp");
        
    }

    @Override
    public void visit(IntExp exp, int level, boolean isAddr) {
        System.out.println("here4 IntExp");
        emitComment("-> constant");
        emitRM("LDC", ac, Integer.parseInt(exp.value), 0, "load const");
        emitComment("<- constant");
    }

    @Override
    public void visit(OpExp exp, int level, boolean isAddr) {
        System.out.println("here5 OpExp");
        
        emitComment("-> op");

        exp.left.accept(this, level, isAddr);
        if (exp.left instanceof IntExp) {
            emitRM("ST", ac, offset, fp, "op: push left");
            offset--;
        } else if (exp.left instanceof VarExp) {
            if (((VarExp)exp.left).variable instanceof SimpleVar) {
                emitRM("ST", ac, offset, fp, "op: push left");
            }
            offset--;
        } else if (exp.left instanceof OpExp) {
            emitRM("ST", ac, offset, fp, "op: push left");
            offset--;
        }
        
        exp.right.accept(this, level, isAddr);
        offset++;
        emitRM("LD", 1, offset, fp, "op: load left");

        switch (exp.op) {
        case OpExp.PLUS:
            emitRO("ADD", ac, 1, ac, "op +");
            break;
        case OpExp.MINUS:
            emitRO("SUB", ac, 1, ac, "op -");
            break;
        case OpExp.TIMES:
            emitRO("MUL", ac, 1, ac, "op *");
            break;
        case OpExp.OVER:
            emitRO("DIV", ac, 1, ac, "op /");
            break;
        case OpExp.EQ:
            emitRO("EQU", ac, 1, ac, "op =");
            break;
        case OpExp.LT:
            emitRO("SUB", ac, 1, ac, "op <");
            emitRM("JLT", ac, 2, pc, "br if true");
            emitRM("LDC", ac, 0, 0, "false case");
            emitRM("LDA", pc, 1, pc, "unconditional jmp");
            emitRM("LDC", ac, 1, 0, "true case");
            break;
        case OpExp.GT:
            emitRO("SUB", ac, 1, ac, "op >");
            emitRM("JGT", ac, 2, pc, "br if true");
            emitRM("LDC", ac, 0, 0, "false case");
            emitRM("LDA", pc, 1, pc, "unconditional jmp");
            emitRM("LDC", ac, 1, 0, "true case");
            break;
        case OpExp.GTEQ:
            emitRO("SUB", ac, 1, ac, "op >=");
            emitRM("JGE", ac, 2, pc, "br if true");
            emitRM("LDC", ac, 0, 0, "false case");
            emitRM("LDA", pc, 1, pc, "unconditional jmp");
            emitRM("LDC", ac, 1, 0, "true case");
            break;
        case OpExp.LTEQ:
            emitRO("SUB", ac, 1, ac, "op <=");
            emitRM("JLE", ac, 2, pc, "br if true");
            emitRM("LDC", ac, 0, 0, "false case");
            emitRM("LDA", pc, 1, pc, "unconditional jmp");
            emitRM("LDC", ac, 1, 0, "true case");
            break;
        case OpExp.EQUALS:
            emitRO("SUB", ac, 1, ac, "op ==");
            emitRM("JEQ", ac, 2, pc, "br if true");
            emitRM("LDC", ac, 0, 0, "false case");
            emitRM("LDA", pc, 1, pc, "unconditional jmp");
            emitRM("LDC", ac, 1, 0, "true case");
            break;
        case OpExp.NEQUALS:
            emitRO("SUB", ac, 1, ac, "op !=");
            emitRM("JNE", ac, 2, pc, "br if true");
            emitRM("LDC", ac, 0, 0, "false case");
            emitRM("LDA", pc, 1, pc, "unconditional jmp");
            emitRM("LDC", ac, 1, 0, "true case");
            break;
        }

          emitComment("<- op");
    }

    @Override
    public void visit(VarExp exp, int level, boolean isAddr) {
        System.out.println("here6 VarExp");
        exp.variable.accept(this, ++level, isAddr );
        
    }

    @Override
    public void visit(NilExp exp, int level, boolean isAddr) {
        System.out.println("here7 NilExp");
        
    }

    @Override
    public void visit(NameTy exp, int level, boolean isAddr) {
        System.out.println("here8 NameTy");
        
    }

    @Override
    public void visit(VarDecList varDecList, int level, boolean isAddr) {
        System.out.println("here10 VarDecList");
        while (varDecList != null) {
            varDecList.head.accept(this, level, isAddr);
            varDecList = varDecList.tail;
        }
        
    }

    @Override
    public void visit(SimpleDec varDec, int level, boolean isAddr) {
        System.out.println("here11 SimpleDec");
        if (!varDec.name.isEmpty()) {
            if (varDec.typ.typ == 0) {
                // Add key to hash map at the current level
                if (!table.containsKey(varDec.name)) {
                    table.put(varDec.name, new ArrayList<NodeType>());
                }
                if (level > 0) {
                    varDec.offset = offset;
                    varDec.nestLevel = level;
                    offset--;
                } else {
                    varDec.offset = globalOffset;
                    varDec.nestLevel = level;
                    globalOffset--;
                }
                
                addToHash(table, new NodeType(varDec.name, varDec, level));
                if (level > 0) {
                    emitComment("processing local var: " + varDec.name);
                } else {
                    emitComment("processing global var: " + varDec.name);
                }
            } else {
                System.err.println("Error, variable cannot be void '" + varDec.name + "' at: row " + varDec.row
                        + " column " + varDec.col);
            }
        }
        // varDec.typ.accept(this, ++level, false);
    }

    @Override
    public void visit(ArrayDec exp, int level, boolean isAddr) {
        System.out.println("here12 ArrayDec");
        
    }

    @Override
    public void visit(SimpleVar var, int level, boolean isAddr) {
        System.out.println("here13 SimpleVar");
        if (!table.containsKey(var.name)) {
            System.err.println("Error, use of undefined variable '"+var.name+"' at: row "+var.row+" column "+var.col);
        } else {
            emitComment("-> id");
            emitComment("looking up id: " + var.name);
            NodeType func = table.get(var.name).get(0);
            SimpleDec dec = ((SimpleDec)func.def);
            if (dec.nestLevel > 0) {
                if (isAddr == true) {
                    emitRM("LDA", 0, dec.offset, fp, "load id address");
                } else {
                    emitRM("LD", 0, dec.offset, fp, "load id value");
                }
            } else {
                if (isAddr == true) {
                    emitRM("LDA", 0, dec.offset, gp, "load id address");
                } else {
                    emitRM("LD", 0, dec.offset, gp, "load id value");
                }
            }
            emitComment("<- id");
        }
        
    }

    @Override
    public void visit(IndexVar exp, int level, boolean isAddr) {
        System.out.println("here14 IndexVar");
        
    }

    @Override
    public void visit(CompoundExp exp, int level, boolean isAddr) {
        System.out.println("here15 CompountExp");
        emitComment("-> compound statement");
        if (exp.decs != null)
            exp.decs.accept(this, level, false);
        if (exp.exps != null)
            exp.exps.accept(this, level, false);
        emitComment("<- compound statement");
    }

    @Override
    public void visit(FunctionDec dec, int level, boolean isAddr) {
        System.out.println("here16 functiondec");
        if (!table.containsKey(dec.func)) {
            table.put(dec.func, new ArrayList<NodeType>());
        }
        // save main location
        dec.funcAddr = emitLoc + 1;
        addToHash(table, new NodeType(dec.func, dec, level));
        
        emitComment("processing function: " + dec.func);
        emitComment("jump around function body here");
        // save location to jump around function
        int savedLoc = emitSkip(1);
        emitRM("ST", 0, -1, fp, "store return");
        level++;
        currentFunc = dec.func;
        offset = initFO;
        dec.params.accept(this, level, false);
        if (dec.body != null) {
            dec.body.accept(this, level, false);
        }
        emitRM("LD", pc, retFO, fp, "return to caller");
        // jump around function body
        int savedLoc2 = emitSkip(0);
        emitBackup(savedLoc);
        emitRM_Abs("LDA", pc, savedLoc2, "jump around fn body");
        emitRestore();
        emitComment("<- fundecl");
        clearMapLevel(table, level);
        
    }

    @Override
    public void visit(CallExp exp, int level, boolean isAddr) {
        if (!table.containsKey(exp.func)) {
            System.out.println("Error, use of undefined function '"+exp.func+"[]' at: row "+exp.row+" column "+exp.col);
        } else {
            System.out.println("here17 CallExp");

            int curFO = initFO;
            NodeType func = table.get(exp.func).get(0);
            FunctionDec dec = ((FunctionDec)func.def);

            emitComment("-> call of function: " + exp.func);

            ExpList expList = exp.args;
            while (expList != null) {
                expList.head.accept(this, level, false);
                emitRM("ST", ac, offset + curFO, fp, "op: push left");
                expList = expList.tail;
                curFO--;
            }

            emitRM("ST", fp, offset, fp, "push ofp");
            emitRM("LDA", fp, offset, fp, "push frame");
            emitRM("LDA", 0, 1, pc, "load ac with ret ptr");
            emitRM_Abs("LDA", pc, dec.funcAddr, "jump to fun loc");
            emitRM("LD", fp, 0, fp, "pop frame");
            emitComment("<- call");
        }
        
    }

    @Override
    public void visit(WhileExp exp, int level, boolean isAddr) {
        System.out.println("here18 WhileExp");
        emitComment("-> while");
        emitComment("while: jump after body comes back here");
        int savedLoc = emitSkip(0);
        level++;
        exp.test.accept(this, level, false );
        int savedLoc2 = emitSkip(1);
        emitComment("while: jump to end belongs here");
        exp.body.accept(this, level, false );
        emitRM_Abs("LDA", pc, savedLoc, "while: absolute jmp to test");
        int savedLoc3 = emitSkip(0);
        emitBackup(savedLoc2);
        emitRM_Abs("JEQ", 0, savedLoc3, "while: jmp to end");
        emitRestore();
        emitComment("<- while");

        clearMapLevel(table, level);
    }

    public void visit(ReturnExp exp, int level, boolean isAddr) {
        System.out.println("here19 ReturnExp");
        
    }


    /**
     **************
     * Emit Routines Below Here
     **************
     */

    public int emitSkip(int distance) {
        int i = emitLoc;
        emitLoc += distance;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
        return i;
    }

    public void emitBackup(int loc) {
        if (loc > highEmitLoc) {
            emitComment("BUG in emitBackup");
        }
        emitLoc = loc;
    }

     public void emitRestore() {
        emitLoc = highEmitLoc;
    }

    public void emitRM_Abs(String op, int r, int a, String c) {
        emitToFile(String.format("%3d: %5s %d, %d(%d) \t%s\n",emitLoc, op, r, a - (emitLoc + 1), pc,c));
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    public void emitRO(String op, int r, int s, int t, String c) {
        emitToFile(String.format("%3d: %5s %d, %d, %d \t%s\n", emitLoc, op, r, s, t, c));
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    public void emitRM(String op, int r, int d, int s, String c) {
        emitToFile(String.format("%3d: %5s %d, %d(%d) \t%s\n", emitLoc, op, r, d, s, c));
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    public void emitComment(String text) {
        emitToFile("* " + text + "\n");
    }

    public void emitToFile(String text) {
        try {
            FileWriter fw = new FileWriter(this.filename, true);
            fw.write(text);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearFile() {
        try {
            FileWriter fw = new FileWriter(this.filename, false);
            fw.write("* C-Minus Compilation to TM Code\n* File: " + this.filename + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     **************
     * Hashmap Routines Below Here
     **************
     */



    public void printHash(HashMap<String, ArrayList<NodeType>> table, final int level) {
        table.forEach((s, nodeList) -> {
            Iterator<NodeType> i = nodeList.iterator();
            while (i.hasNext()) {
                NodeType n = i.next();
                if (n.level == level) {
                    int type = 0;
                    
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
    }


    /**
     * getType
     * 
     * @param exp
     * @return -1 for error, 0 for int and 1 for void
     */
    int getType(Exp exp) {
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
