package cop5555fa13.ast;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.Token;

public class IntLitExpr extends Expr {
	final Token intLit;
	//final Kind kind = Kind.INT_LIT;
	
	public IntLitExpr(Token intLit) {
		super();
		this.intLit = intLit;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIntLitExpr(this, arg);
	}

}
