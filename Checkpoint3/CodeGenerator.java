import absyn.*;
import jdk.jfr.events.FileWriteEvent;

import java.io.FileWriter;
import java.io.IOException;
import java.time.chrono.HijrahDate;
import java.util.*;

public class CodeGenerator implements AbsynVisitor {
    int mainEntry, globalOffset;
    static int emitLoc = 0;
    static int highEmitLoc = 0;
    final int pc;
    String filename;


    public CodeGenerator(String filename) {
        this.filename = filename;
    }

    public void visit(Absyn trees) {
        
    }

    public void visit(DecList decs, int offset, Boolean isAddress) {

    }


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

    void emitRM_Abs(String op, int r, int a, String c) {
        emitToFile(String.format("%3d: %5s %d, %d(%d) \t%s\n",emitLoc, op, r, a - (emitLoc + 1), pc,c));
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    void emitRO(String op, int r, int s, int t, String c) {
        emitToFile(String.format("%3d: %5s %d, %d, %d \t%s\n", emitLoc, op, r, s, t, c));
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    void emitRM(String op, int r, int d, int s, String c) {
        emitToFile(String.format("%3d: %5s %d, %d(%d) \t%s\n", emitLoc, op, r, d, s, c));
        ++emitLoc;
        if (highEmitLoc < emitLoc) {
            highEmitLoc = emitLoc;
        }
    }

    void emitComment(String text) {
        emitToFile("* " + text + "\n");
    }

    void emitToFile(String text) {
        try {
            FileWriter fw = new FileWriter(this.filename);
            fw.write(text);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
