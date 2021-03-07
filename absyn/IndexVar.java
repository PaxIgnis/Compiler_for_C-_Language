package absyn;

public class IndexVar extends Var {
    public String name;
    public IntExp index;

    public IndexVar(int row, int col, String name, IntExp index) {
        this.row = row;
        this.col = col;
        this.name = name;
        this.index = index;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
