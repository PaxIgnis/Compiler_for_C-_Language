package absyn;

public class NameTy extends Absyn {
    public final static int INT = 0;
    public final static int VOID = 1;

    public int typ;

    public NameTy(int row, int col, int typ) {
        this.row = row;
        this.col = col;
        this.typ = typ;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}
