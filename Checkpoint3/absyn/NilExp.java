package absyn;

public class NilExp extends Exp {

    public NilExp(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}