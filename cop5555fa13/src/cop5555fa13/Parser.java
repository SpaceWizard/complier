package cop5555fa13;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import cop5555fa13.TokenStream;
//import cop5555fa13.SimpleParser.SyntaxException;
import cop5555fa13.TokenStream.Token;
import cop5555fa13.TokenStream.Kind;
import cop5555fa13.ast.ASTNode;
import cop5555fa13.ast.ASTVisitor;
import cop5555fa13.ast.AlternativeStmt;
import cop5555fa13.ast.AssignExprStmt;
import cop5555fa13.ast.AssignPixelStmt;
import cop5555fa13.ast.AssignStmt;
import cop5555fa13.ast.BinaryExpr;
import cop5555fa13.ast.BooleanLitExpr;
import cop5555fa13.ast.ConditionalExpr;
import cop5555fa13.ast.PreDefExpr;
import cop5555fa13.ast.Dec;
import cop5555fa13.ast.Expr;
import cop5555fa13.ast.FileAssignStmt;
import cop5555fa13.ast.IdentExpr;
import cop5555fa13.ast.ImageAttributeExpr;
import cop5555fa13.ast.IntLitExpr;
import cop5555fa13.ast.IterationStmt;
import cop5555fa13.ast.PauseStmt;
import cop5555fa13.ast.Pixel;
import cop5555fa13.ast.Program;
import cop5555fa13.ast.SampleExpr;
import cop5555fa13.ast.ScreenLocationAssignmentStmt;
import cop5555fa13.ast.SetVisibleAssignmentStmt;
import cop5555fa13.ast.ShapeAssignmentStmt;
import cop5555fa13.ast.SinglePixelAssignmentStmt;
import cop5555fa13.ast.SingleSampleAssignmentStmt;
import cop5555fa13.ast.Stmt;
import cop5555fa13.ast.TypeCheckVisitor;
import static cop5555fa13.TokenStream.Kind.*;

public class Parser {
	
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
	
	//ADD THESE FIELDS  TO YOUR PARSER
	TokenStream stream;
	Token tempToken;
	int index;
	Token progName;  //keep the program name in case you don't generate an AST
	List<SyntaxException> errorList;  //save the error for grading purposes

	public Parser(TokenStream initialized_stream) {
		errorList = new ArrayList<SyntaxException>();
		this.stream = initialized_stream;
		//ADD THIS TO YOUR CONSTRUCTUR
		
	}
	
	//THIS IS THE MAIN PUBLIC parse method.  Note that it does not throw exceptions.  
	//If any make it to this level without having been caught, the exception is added to the list.
	//If the program parsed correctly, return its AST. Otherwise return null.
	public Program parse() {
		Program p = null;
		try{
		p = parseProgram();
		match(EOF);
		} catch(SyntaxException e){
			errorList.add(e);
		}
		if (errorList.isEmpty()){
			return p;
		} else 
			return null;
	}
	
	public List<SyntaxException> getErrorList(){
		return errorList;
	}
	
	public String getProgName(){
		return (progName != null ?  progName.getText() : "no program name");
	}

	//private 
	Program parseProgram() throws SyntaxException {
		consume();
		try {
			progName = match(IDENT);
		} catch (SyntaxException e) {
			errorList.add(e);
			while (!isKind(IDENT,EOF,LBRACE)){ consume(); }
			if(isKind(IDENT)) {consume();}
		}
		//if(isKind(EOF)) {}
		try {
			match(LBRACE);
		} catch (SyntaxException e) {
			errorList.add(e);
			while (!isKind(LBRACE,SEMI,image,_int,_boolean,pixel,EOF)){ consume(); }
			if(isKind(LBRACE)) {consume();}
		}
		List<Dec> decList = new ArrayList<Dec>();
		while (inFirstDec()) {
			try{
				decList.add(parseDec());
			}
			catch(SyntaxException e){
				errorList.add(e);
				//skip tokens until next semi, consume it, then continue parsing
				while (!isKind(SEMI,image,_int,_boolean,pixel, EOF)){ consume(); }
				if (isKind(SEMI)){consume();}  //IF A SEMI, CONSUME IT BEFORE CONTINUING
			}
		}

		List<Stmt> stmtList = new ArrayList<Stmt>();
		//ADD YOUR OWN CODE TO DEAL WITH STATEMENTS
		Stmt tempStmt;
		while (inFirstStmt()) {
			try {
				tempStmt = parseStmt();
				if(tempStmt!=null){
						stmtList.add(tempStmt);
				}
			}
			catch(SyntaxException e){
				errorList.add(e);
				while (!isKind(SEMI, EOF) && !inFirstStmt()){ consume(); }
				if (isKind(SEMI)){consume();}  //IF A SEMI, CONSUME IT BEFORE CONTINUING
			}
		}
		try {
			match(RBRACE);
		} catch (SyntaxException e) {
			errorList.add(e);
			while (!isKind(RBRACE,EOF)){ consume(); }
			if(isKind(RBRACE)){consume();}
		}
		
		if (errorList.isEmpty()) { 
			Program prog = new Program(progName, decList, stmtList);
			TypeCheckVisitor v = new TypeCheckVisitor();
			try {
				prog.visit(v, null);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if(!v.isCorrect()) {
				System.out.println(v.getLog());
			}
			return new Program(progName, decList, stmtList);
		}
		
		System.out.println("error" + (errorList.size()>1?"s parsing program ":" parsing program ") + getProgName());
		for(SyntaxException e: errorList){		
			System.out.println(e.getMessage() + " at line" + e.t.getLineNumber());
		}
		return null;
	}
	
	public boolean inFirstDec() {
		if(isKind(Kind.image,Kind.pixel,Kind._int,Kind._boolean)) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean inFirstStmt() {
		if(isKind(Kind.SEMI,Kind.IDENT,Kind.pause,Kind._while,Kind._if)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Dec parseDec() throws SyntaxException{
		Token typeToken = match(Kind.image,Kind.pixel,Kind._int,Kind._boolean);
		Token retT = match(Kind.IDENT);
		match(Kind.SEMI);
		Kind retType = typeToken.kind;
		return new Dec(retType,retT);
	}
	
	List<Stmt> parseAllStmt() throws SyntaxException {
		List<Stmt> stmtList = new ArrayList<Stmt>();
		Stmt tempStmt;
		while (inFirstStmt()) {
			try {
				tempStmt = parseStmt();
				if(tempStmt!=null){
						stmtList.add(tempStmt);
				}
			}
			catch(SyntaxException e){
				errorList.add(e);
				while (!isKind(SEMI, EOF) && !inFirstStmt()){ consume(); }
				if (isKind(SEMI)){consume();}  //IF A SEMI, CONSUME IT BEFORE CONTINUING
			}
		}
		return stmtList;
	}
	
	Stmt parseStmt() throws SyntaxException {
		switch (tempToken.kind) {
		case SEMI:
			consume();
			return null;
		case IDENT:
			return parseAssignStmt();
		case pause:
			return parsePauseStmt();
		case _while:
			return parseIterationStmt();
		case _if:
			return parseAlternativeStmt();
		default:
			throw new SyntaxException(tempToken,"What is this I don't even");
		}
	}
	
	public AssignStmt parseAssignStmt() throws SyntaxException {
		Token lhsIdent = match(Kind.IDENT);
		AssignStmt ret = null;
		if(isKind(Kind.ASSIGN)) {
			consume();
			switch (tempToken.kind) {
			case STRING_LIT:
				ret = new FileAssignStmt(lhsIdent,consume());
				match(Kind.SEMI);
				break;
			case LBRACE:
				ret = new AssignPixelStmt(lhsIdent,parsePixel());
				match(Kind.SEMI);
				break;
			case IDENT: case INT_LIT: case BOOLEAN_LIT: case x: case y: case Z: case SCREEN_SIZE: case LPAREN:
				ret = new AssignExprStmt(lhsIdent,parseExpr());
				match(Kind.SEMI);
				break;
			default:
				throw new SyntaxException(tempToken,"Problema within an assignment statement after = " + tempToken.kind);
			}
		} else if(isKind(Kind.DOT)) {
			consume();
			switch (tempToken.kind) {
			case shape:
				ret = parseShape(lhsIdent);
				break;
			case location:
				ret = parseLocation(lhsIdent);
				break;
			case visible:
				ret = new SetVisibleAssignmentStmt(lhsIdent,parseVisible());
				break;
			case pixels:
				ret = parsePixels(lhsIdent);
				break;
			default:
				throw new SyntaxException(tempToken,"Problema within an assignment statement," +
						" expectiong shape,location,visible,pixels " + tempToken.kind);
			}
		} else {
			throw new SyntaxException(tempToken,"Problema within an assignment statement expected : . or =, actual: " + tempToken.kind);
		}
		return ret;
	}
	
	public PauseStmt parsePauseStmt() throws SyntaxException {
		Expr expr;
		match(Kind.pause);
		expr = parseExpr();
		match(Kind.SEMI);
		return new PauseStmt(expr);
	}
	
	public IterationStmt parseIterationStmt() throws SyntaxException {
		Expr expr;
		List<Stmt> stmtList = new ArrayList<Stmt>();
		match(Kind._while);
		match(Kind.LPAREN);
		expr = parseExpr();
		match(Kind.RPAREN);
		//System.out.println("pren good");
		match(Kind.LBRACE);
		//System.out.println("lrace good");
		stmtList = parseAllStmt();
		//System.out.println("stmt good");
		match(Kind.RBRACE);
		//System.out.println("rbrace good");
		return new IterationStmt(expr,stmtList);
	}
	
	public AlternativeStmt parseAlternativeStmt() throws SyntaxException {
		Expr expr;
		List<Stmt> IfStmtList = new ArrayList<Stmt>();
		List<Stmt> ElseStmtList = new ArrayList<Stmt>();
		match(Kind._if);
		match(Kind.LPAREN);
		expr = parseExpr();
		match(Kind.RPAREN);
		match(Kind.LBRACE);
		IfStmtList = parseAllStmt();
		match(Kind.RBRACE);
		if(isKind(Kind._else)) {
			consume();
			match(Kind.LBRACE);
			ElseStmtList = parseAllStmt();
			match(Kind.RBRACE);
		}
		return new AlternativeStmt(expr,IfStmtList,ElseStmtList);
	}
	
	public Pixel parsePixel() throws SyntaxException {
		match(Kind.LBRACE);
		match(Kind.LBRACE);
		Expr redExpr = parseExpr();
		match(Kind.COMMA);
		Expr greenExpr = parseExpr();
		match(Kind.COMMA);
		Expr blueExpr = parseExpr();
		match(Kind.RBRACE);
		match(Kind.RBRACE);
		return new Pixel(redExpr,greenExpr,blueExpr);
	}
	
	public ShapeAssignmentStmt parseShape(Token lhs) throws SyntaxException {
		Expr exprX;
		Expr exprY;
		consume();
		match(Kind.ASSIGN);
		match(Kind.LSQUARE);
		exprX = parseExpr();
		match(Kind.COMMA);
		exprY = parseExpr();
		match(Kind.RSQUARE);
		match(Kind.SEMI);
		return new ShapeAssignmentStmt(lhs,exprX,exprY);
	}
	
	public ScreenLocationAssignmentStmt parseLocation(Token lhs) throws SyntaxException {
		Expr exprX;
		Expr exprY;
		consume();
		match(Kind.ASSIGN);
		match(Kind.LSQUARE);
		exprX = parseExpr();
		match(Kind.COMMA);
		exprY = parseExpr();
		match(Kind.RSQUARE);
		match(Kind.SEMI);
		return new ScreenLocationAssignmentStmt(lhs,exprX,exprY);
	}
	
	public Expr parseVisible() throws SyntaxException {
		Expr expr;
		consume();
		match(Kind.ASSIGN);
		expr = parseExpr();
		match(Kind.SEMI);
		return expr;
	}
	
	public AssignStmt parsePixels(Token lhs) throws SyntaxException {
		Expr exprX;
		Expr exprY;
		match(Kind.pixels);
		match(Kind.LSQUARE);
		exprX = parseExpr();
		match(Kind.COMMA);
		exprY = parseExpr();
		match(Kind.RSQUARE);
		if(isKind(Kind.ASSIGN)) {
			Pixel pixel;
			consume();
			pixel = parsePixel();
			match(Kind.SEMI);
			return new SinglePixelAssignmentStmt(lhs,exprX,exprY,pixel);
		}
		else if(isKind(Kind.red,Kind.green,Kind.blue)) {
			Token color;
			Expr rhs;
			color = consume();
			match(Kind.ASSIGN);
			rhs = parseExpr();
			match(Kind.SEMI);
			//System.out.println("SinglePixelAssignmentStmt");
			return new SingleSampleAssignmentStmt(lhs,exprX,exprY,color,rhs);
		}
		else {
			throw new SyntaxException(tempToken,"Problema within an pixels statement expected : =,red,green,blue actual: " + tempToken.kind);
		}
	}
	
	public Expr parseExpr() throws SyntaxException {
		Expr condition;
		condition = parseOrExpr();
		if(isKind(Kind.QUESTION)) {
			Expr trueValue;
			Expr falseValue;
			consume();
			trueValue = parseExpr();
			match(Kind.COLON);
			falseValue = parseExpr();
			return new ConditionalExpr(condition,trueValue,falseValue);
		} else if(!isKind(Kind.COMMA,Kind.SEMI,Kind.RSQUARE,Kind.RBRACE,Kind.COLON,Kind.RPAREN)) {
			throw new SyntaxException(tempToken,"Problema with an expression : it's followed by sonething unexpected: " + tempToken.kind);
		}
		return condition;
	}
	
	public Expr parseOrExpr() throws SyntaxException {
		Expr left;
		left = parseAndExpr();
		if(isKind(Kind.OR)) {
			Expr right;
			Token op = tempToken;
			consume();
			right = parseOrExpr();
			return new BinaryExpr(left,op,right);
		}
		return left;
	}
	
	public Expr parseAndExpr() throws SyntaxException {
		Expr left;
		left = parseEqualityExpr();
		if(isKind(Kind.AND)) {
			Expr right;
			Token op = tempToken;
			consume();
			right = parseAndExpr();
			return new BinaryExpr(left,op,right);
		}
		return left;
	}
	
	public Expr parseEqualityExpr() throws SyntaxException {
		Expr left;
		left = RelExpr();
		if(isKind(Kind.EQ,Kind.NEQ)) {
			Expr right;
			Token op = tempToken;
			consume();
			right = parseEqualityExpr();
			return new BinaryExpr(left,op,right);
		}
		return left;
	}
	
	public Expr RelExpr() throws SyntaxException {
		Expr left;
		left = ShiftExpr();
		if(isKind(Kind.LEQ,Kind.LT,Kind.GEQ,Kind.GT)) {
			Expr right;
			Token op = tempToken;
			consume();
			right = RelExpr();
			return new BinaryExpr(left,op,right);
		}
		return left;
	}
	
	public Expr ShiftExpr() throws SyntaxException {
		Expr left;
		left = AddExpr();
		while(isKind(Kind.LSHIFT,Kind.RSHIFT)) {
			Expr right;
			Token op = tempToken;
			consume();
			right = ShiftExpr();
			return new BinaryExpr(left,op,right);
		}
		return left;
	}
	
	public Expr AddExpr() throws SyntaxException {
		Expr left;
		left = MultExpr();
		while(isKind(Kind.PLUS,Kind.MINUS)) {
			Expr right;
			Token op = tempToken;
			consume();
			right = AddExpr();
			return new BinaryExpr(left,op,right);
		}
		return left;
	}
	
	public Expr MultExpr() throws SyntaxException {
		Expr left;
		left = parsePrimaryExpr();
		while(isKind(Kind.TIMES,Kind.MOD,Kind.DIV)) {
			Expr right;
			Token op = tempToken;
			consume();
			right = MultExpr();
			return new BinaryExpr(left,op,right);
		}
		return left;
	}
	
	public Expr parsePrimaryExpr() throws SyntaxException {
		if(isKind(Kind.INT_LIT)) {
			return new IntLitExpr(consume());
		} else if(isKind(Kind.BOOLEAN_LIT)) {
			return new BooleanLitExpr(consume());
		} else if(isKind(Kind.x,Kind.y,Kind.Z,Kind.SCREEN_SIZE)) {
			return new PreDefExpr(consume());
		} else if(isKind(Kind.LPAREN)) {
			Expr expr;
			consume();
			expr = parseExpr();
			match(Kind.RPAREN);
			return expr;
		} else if(isKind(Kind.IDENT)) {
			Token ident =consume();
			if(isKind(Kind.LSQUARE)) {
				Expr exprX;
				Expr exprY;
				Token color;
				consume();
				exprX = parseExpr();
				match(Kind.COMMA);
				exprY = parseExpr();
				match(Kind.RSQUARE);
				color = match(Kind.red,Kind.green,Kind.blue);
				return new SampleExpr(ident,exprX,exprY,color);
			} else if(isKind(Kind.DOT)) {
				consume();
				return new ImageAttributeExpr(ident,match(Kind.width,Kind.height,Kind.x_loc,Kind.y_loc));
			}
			return new IdentExpr(ident);
		} else {
			throw new SyntaxException(tempToken,"something went wrong in a primary expression after IDENT: " + tempToken.kind);
		}
	}
	
	public Token consume()  {
		Token retToken = tempToken;
		tempToken = stream.getToken(index);
		//Token retToken = stream.getToken(index-1);
		if(!isKind(Kind.EOF)) {
			index++;
		}
		return retToken;
	}
	
	public Token match(Kind... kindCollection) throws SyntaxException {
		for (Kind kind : kindCollection) {
			if (isKind(kind)) {
				Token retToken = tempToken;
				consume();
				return retToken;
			}
		}
		String tempstring = "";
		for (Kind kind : kindCollection) {
			tempstring+=kind;
			tempstring+=",";
		}
		throw new SyntaxException(tempToken,"expected : " + tempstring + " actual: " + tempToken.kind);
	}
	
	boolean isKind(Kind... kindCollection) {
		for (Kind kind : kindCollection) {
			if(kind == tempToken.kind) {
				return true;
			}
		}
		return false;
	}
	
}
