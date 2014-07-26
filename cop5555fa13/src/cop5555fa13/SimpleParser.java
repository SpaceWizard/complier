package cop5555fa13;


import java.io.EOFException;

import cop5555fa13.TokenStream;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.TokenStream.Kind;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String msg) {
			super(msg);
			this.t = t;
		}

		public String toString() {
			return super.toString() + "\n" + t.toString();
		}
		
		public Kind getKind(){
			return t.kind;
		}
	}

	TokenStream stream;
	
	Token tempToken;
	
	int index=0;
	/* You will need additional fields */

    /** creates a simple parser.  
     * 
     * @param initialized_stream  a TokenStream that has already been initialized by the Scanner 
     */
	public SimpleParser(TokenStream initialized_stream) {
		this.stream = initialized_stream;
		/* You probably want to do more here */
	}

	/* This method parses the input from the given token stream.  If the input is correct according to the phrase
	 * structure of the language, it returns normally.  Otherwise it throws a SyntaxException containing
	 * the Token where the error was detected and an appropriate error message.  The contents of your
	 * error message will not be graded, but the "kind" of the token will be.
	 */
	public void parse() throws SyntaxException {
	    /* You definitely need to do more here */
		consume();
		match(Kind.IDENT);
		match(Kind.LBRACE);
		Rec_Dec();
		Rec_Stmt();
		//fags stole my code
		match(Kind.RBRACE);
		match(Kind.EOF);
	}
	
	public void Rec_Dec() throws SyntaxException {
		while(isKind(Kind.image,Kind.pixel,Kind._int,Kind._boolean)) {
			Dec();
		}
	}
	
	public void Rec_Stmt() throws SyntaxException {
		while(isKind(Kind.SEMI,Kind.IDENT,Kind.pause,Kind._while,Kind._if)) {
			Stmt();
		}
	}
	
	public void Dec() throws SyntaxException{
		consume();
		match(Kind.IDENT);
		match(Kind.SEMI);
	}
	
	public void Stmt() throws SyntaxException {
		switch (tempToken.kind) {
		case SEMI:
			consume();
			break;
		case IDENT:
			AssignStmt();
			break;
		case pause:
			PauseStmt();
			break;
		case _while:
			IterationStmt();
			break;
		case _if:
			AlternativeStmt();
			break;
		default:
			throw new SyntaxException(tempToken,"What is this I don't even");
		}
	}
	
	public void AssignStmt() throws SyntaxException {
		consume();
		if(isKind(Kind.ASSIGN)) {
			consume();
			switch (tempToken.kind) {
			case STRING_LIT:
				consume();
				match(Kind.SEMI);
				break;
			case LBRACE:
				Pixel();
				match(Kind.SEMI);
				break;
			case IDENT: case INT_LIT: case BOOLEAN_LIT: case x: case y: case Z: case SCREEN_SIZE: case LPAREN:
				Expr();
				match(Kind.SEMI);
				break;
			default:
				throw new SyntaxException(tempToken,"Problema within an assignment statement after = " + tempToken.kind);
			}
		}
		else if(isKind(Kind.DOT)) {
			consume();
			switch (tempToken.kind) {
			case shape:
				shape();
				break;
			case location:
				location();
				break;
			case visible:
				visible();
				break;
			case pixels:
				pixels();
				break;
			default:
				throw new SyntaxException(tempToken,"Problema within an assignment statement," +
						" expectiong shape,location,visible,pixels " + tempToken.kind);
			}
		}
		else {
			throw new SyntaxException(tempToken,"Problema within an assignment statement expected : . or =, actual: " + tempToken.kind);
		}
	}
	
	public void shape() throws SyntaxException {
		consume();
		match(Kind.ASSIGN);
		match(Kind.LSQUARE);
		Expr();
		match(Kind.COMMA);
		Expr();
		match(Kind.RSQUARE);
		match(Kind.SEMI);
	}
	
	public void location() throws SyntaxException {
		consume();
		match(Kind.ASSIGN);
		match(Kind.LSQUARE);
		Expr();
		match(Kind.COMMA);
		Expr();
		match(Kind.RSQUARE);
		match(Kind.SEMI);
	}
	
	public void visible() throws SyntaxException {
		consume();
		match(Kind.ASSIGN);
		Expr();
		match(Kind.SEMI);
	}
	
	public void pixels() throws SyntaxException {
		consume();
		match(Kind.LSQUARE);
		Expr();
		match(Kind.COMMA);
		Expr();
		match(Kind.RSQUARE);
		if(isKind(Kind.ASSIGN)) {
			consume();
			Pixel();
			match(Kind.SEMI);
		}
		else if(isKind(Kind.red,Kind.green,Kind.blue)) {
			consume();
			match(Kind.ASSIGN);
			Expr();
			match(Kind.SEMI);
		}
		else {
			throw new SyntaxException(tempToken,"Problema within an pixels statement expected : =,red,green,blue actual: " + tempToken.kind);
		}
	}
	
	public void PauseStmt() throws SyntaxException {
		consume();
		Expr();
		match(Kind.SEMI);
	}
	
	public void IterationStmt() throws SyntaxException {
		consume();
		match(Kind.LPAREN);
		Expr();
		match(Kind.RPAREN);
		match(Kind.LBRACE);
		Rec_Stmt();
		match(Kind.RBRACE);
	}
	
	public void AlternativeStmt() throws SyntaxException {
		consume();
		match(Kind.LPAREN);
		Expr();
		match(Kind.RPAREN);
		match(Kind.LBRACE);
		Rec_Stmt();
		match(Kind.RBRACE);
		if(isKind(Kind._else)) {
			consume();
			match(Kind.LBRACE);
			Rec_Stmt();
			match(Kind.RBRACE);
		}
	}
	
	public void Pixel() throws SyntaxException {
		match(Kind.LBRACE);
		match(Kind.LBRACE);
		Expr();
		match(Kind.COMMA);
		Expr();
		match(Kind.COMMA);
		Expr();
		match(Kind.RBRACE);
		match(Kind.RBRACE);
	}
	
	public void Expr() throws SyntaxException {
		OrExpr();
		if(isKind(Kind.QUESTION)) {
			consume();
			Expr();
			match(Kind.COLON);
			Expr();
		}
		else if(!isKind(Kind.COMMA,Kind.SEMI,Kind.RSQUARE,Kind.RBRACE,Kind.COLON,Kind.RPAREN)) {
			throw new SyntaxException(tempToken,"Problema with an expression : it's followed by sonething unexpected: " + tempToken.kind);
		}
	}
	
	public void OrExpr() throws SyntaxException {
		AndExpr();
		while(isKind(Kind.OR)) {
			consume();
			AndExpr();
		}
	}
	
	public void AndExpr() throws SyntaxException {
		EqualityExpr();
		while(isKind(Kind.AND)) {
			consume();
			EqualityExpr();
		}
	}
	
	public void EqualityExpr() throws SyntaxException {
		RelExpr();
		while(isKind(Kind.EQ,Kind.NEQ)) {
			consume();
			RelExpr();
		}
	}
	
	public void RelExpr() throws SyntaxException {
		ShiftExpr();
		while(isKind(Kind.LEQ,Kind.LT,Kind.GEQ,Kind.GT)) {
			consume();
			ShiftExpr();
		}
	}
	
	public void ShiftExpr() throws SyntaxException {
		AddExpr();
		while(isKind(Kind.LSHIFT,Kind.RSHIFT)) {
			consume();
			AddExpr();
		}
	}
	
	public void AddExpr() throws SyntaxException {
		MultExpr();
		while(isKind(Kind.PLUS,Kind.MINUS)) {
			consume();
			MultExpr();
		}
	}
	
	public void MultExpr() throws SyntaxException {
		PrimaryExpr();
		while(isKind(Kind.TIMES,Kind.MOD,Kind.DIV)) {
			consume();
			PrimaryExpr();
		}
	}
	
	public void PrimaryExpr() throws SyntaxException {
		if(isKind(Kind.INT_LIT,Kind.BOOLEAN_LIT,Kind.x,Kind.y,Kind.Z,Kind.SCREEN_SIZE)) {
			consume();
		}
		else if(isKind(Kind.LPAREN)) {
			consume();
			Expr();
			match(Kind.RPAREN);
		}
		else if(isKind(Kind.IDENT)) {
			consume();
			if(isKind(Kind.LSQUARE)) {
				consume();
				Expr();
				match(Kind.COMMA);
				Expr();
				match(Kind.RSQUARE);
				match(Kind.red,Kind.green,Kind.blue);
			}
			else if(isKind(Kind.DOT)) {
				consume();
				match(Kind.width,Kind.height,Kind.x_loc,Kind.y_loc);
			}
		}
		else {
			throw new SyntaxException(tempToken,"something went wrong in a primary expression after IDENT: " + tempToken.kind);
		}
	}
	
	/* You will need to add more methods*/

	
	Token consume()  {
		tempToken = stream.getToken(index);
		if(!isKind(Kind.EOF)) {
			index++;
		}
		System.out.println(tempToken.toString());
		return tempToken;
	}
	
	Token match(Kind... kindCollection) throws SyntaxException {
		Kind kin = null;
		for (Kind kind : kindCollection) {
			System.out.println(kind);
			if (isKind(kind)) {
				return consume();
			}
			kin=kind;
		}
		throw new SyntaxException(tempToken,"expected : " + kin + " actual: " + tempToken.kind);
		
	}
	
	boolean isKind(Kind... kindCollection) {
		for (Kind kind : kindCollection) {
			if(kind == tempToken.kind) {
				return true;
			}
		}
		return false;
	}
	
	/* Java hint -- Methods with a variable number of parameters may be useful.  
	 * For example, this method takes a token and variable number of "kinds", and indicates whether the
	 * kind of the given token is among them.  The Java compiler creates an array holding the given parameters.
	 */
/*	   private boolean isKind(Token t, Kind... kinds) {
		Kind k = t.kind;
		for (int i = 0; i != kinds.length; ++i) {
			if (k==kinds[i]) return true;
		}
		return false;
	  }*/

}
