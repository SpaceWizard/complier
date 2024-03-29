package cop5555fa13.ast;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.Token;

public class PreDefExpr extends Expr {
	final Token constantLit;
	//final Kind kind = Kind.INT_LIT;
	
	public PreDefExpr(Token intLit) {
		super();
		this.constantLit = intLit;
	}
	
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitPreDefExpr(this, arg);
	}

}
