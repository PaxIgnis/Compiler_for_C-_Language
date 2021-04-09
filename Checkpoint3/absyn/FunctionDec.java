package absyn;

public class FunctionDec extends Dec {
    public NameTy result;
    public String func;
    public VarDecList params;
    public CompoundExp body;
    public int funcAddr;

    public FunctionDec(int row, int col, NameTy result, String func, VarDecList params, CompoundExp body) {
        this.row = row;
        this.col = col;
        this.result = result;
        this.func = func;
        this.params = params;
        this.body = body;
        this.funcAddr = 0;
    }

    public FunctionDec(int row, int col, NameTy result, String func, VarDecList params, CompoundExp body, int funcAddr) {
        this.row = row;
        this.col = col;
        this.result = result;
        this.func = func;
        this.params = params;
        this.body = body;
        this.funcAddr = funcAddr;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, false);
    }
}
