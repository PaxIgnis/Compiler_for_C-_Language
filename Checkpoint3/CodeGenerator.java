import absyn.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CodeGenerator implements AbsynVisitor {
    HashMap<String, ArrayList<NodeType>> table;
    int mainEntry, globalOffset;
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
    public void visit(ExpList exp, int level, boolean isAddr) {
        System.out.println("here1");
        
    }

    @Override
    public void visit(AssignExp exp, int level, boolean isAddr) {
        System.out.println("here2");
        
    }

    @Override
    public void visit(IfExp exp, int level, boolean isAddr) {
        System.out.println("here3");
        
    }

    @Override
    public void visit(IntExp exp, int level, boolean isAddr) {
        System.out.println("here4");
        
    }

    @Override
    public void visit(OpExp exp, int level, boolean isAddr) {
        System.out.println("here5");
        
    }

    @Override
    public void visit(VarExp exp, int level, boolean isAddr) {
        System.out.println("here6");
        
    }

    @Override
    public void visit(NilExp exp, int level, boolean isAddr) {
        System.out.println("here7");
        
    }

    @Override
    public void visit(NameTy exp, int level, boolean isAddr) {
        System.out.println("here8");
        
    }

    @Override
    public void visit(VarDecList exp, int level, boolean isAddr) {
        System.out.println("here10");
        
    }

    @Override
    public void visit(SimpleDec exp, int level, boolean isAddr) {
        System.out.println("here11");
        
    }

    @Override
    public void visit(ArrayDec exp, int level, boolean isAddr) {
        System.out.println("here12");
        
    }

    @Override
    public void visit(SimpleVar exp, int level, boolean isAddr) {
        System.out.println("here13");
        
    }

    @Override
    public void visit(IndexVar exp, int level, boolean isAddr) {
        System.out.println("here14");
        
    }

    @Override
    public void visit(CompoundExp exp, int level, boolean isAddr) {
        System.out.println("here15");
        
    }

    @Override
    public void visit(FunctionDec dec, int level, boolean isAddr) {
        System.out.println("functiondec");
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
        currentFunc = dec.func;
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

        
    }

    @Override
    public void visit(CallExp exp, int level, boolean isAddr) {
        System.out.println("here17");
        
    }

    @Override
    public void visit(WhileExp exp, int level, boolean isAddr) {
        System.out.println("here18");
        
    }

    public void visit(ReturnExp exp, int level, boolean isAddr) {
        System.out.println("here19");
        
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
