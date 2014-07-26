package cop5555fa13.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.ast.*;

public class TypeCheckVisitor implements ASTVisitor {
	
	HashMap<String, Kind> symbolTable;
	
	public TypeCheckVisitor(){
		symbolTable = new HashMap<String, Kind>();
		errorNodeList = new ArrayList();
		errorLog = new StringBuilder();
	}
	public List getErrorNodeList(){return errorNodeList;}
	public boolean isCorrect(){
		return errorNodeList.size()==0;
	}
	public String getLog(){
		return errorLog.toString();
	}
	List<ASTNode> errorNodeList;
	
	public StringBuilder errorLog;//we need to fix it later
	//StringBuilder errorLog;
	@Override
	public Object visitDec(Dec dec, Object arg) {
		if(symbolTable.containsKey(dec.ident.getText())) {
			errorNodeList.add(dec);
			errorLog.append(dec.ident.toString()+" its kind being "+dec.type+" has been declared twice at line "+dec.ident.getLineNumber()+"\n");
		} else {
			symbolTable.put(dec.ident.getText(), dec.type);
		}
		return null;
	}
	
	//public String getLog1() {
	//	
	//}
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		if(!program.decList.isEmpty()) {
			for (Dec dec : program.decList) {
				dec.visit(this, null);
			}
		}
		if (!program.stmtList.isEmpty()) {
			for (Stmt stmt : program.stmtList) {
				if(stmt!=null){
					stmt.visit(this, null);
				}
			}
		}
		return null;
	}
	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		// TODO: faggot stole my code
		if(alternativeStmt.expr.visit(this,null)==Kind._boolean) {
			if(alternativeStmt.ifStmtList.size()>0) {
				for (Stmt stmt : alternativeStmt.ifStmtList) {
					if(stmt!=null){
						stmt.visit(this, null);
					}
				}
			}
			if(alternativeStmt.elseStmtList.size()>0) {
				for (Stmt stmt : alternativeStmt.elseStmtList) {
					if(stmt!=null){
						stmt.visit(this, null);
					}
				}
			}
			return null;
		} else {
			errorNodeList.add(alternativeStmt);
			errorLog.append("expr in alternativeStmt is not bool\n");
			return null;
		}
	}
	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		if(pauseStmt.expr.visit(this,null)==Kind._int) {
			return null;
		} else {
			errorNodeList.add(pauseStmt);
			errorLog.append("non-int assigned in a pauseStmt \n");
			return null;
		}
	}
	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		if(iterationStmt.expr.visit(this,null)==Kind._boolean) {
			if(iterationStmt.stmtList.size()>0) {
				for(Stmt stmt:iterationStmt.stmtList) {
					stmt.visit(this,null);
				}
			}
			return null;
		} else {
			errorNodeList.add(iterationStmt);
			errorLog.append("non-boolean assigned in a pauseStmt \n");
			return null;
		}
	}
	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		if(!symbolTable.containsKey(assignPixelStmt.lhsIdent.getText())) {
			errorNodeList.add(assignPixelStmt);
			errorLog.append("variable undefined: "+assignPixelStmt.lhsIdent.getText()+"\n");
			return null;
		} else if(symbolTable.get(assignPixelStmt.lhsIdent.getText()) == Kind.pixel) {
			assignPixelStmt.pixel.visit(this,null);
			//assignPixelStmt.imageFlag = false;
			return null;
		} else if(symbolTable.get(assignPixelStmt.lhsIdent.getText()) == Kind.image) {
			assignPixelStmt.pixel.visit(this,null);
			assignPixelStmt.imageFlag = true;
			return null;
		} else {
			errorNodeList.add(assignPixelStmt);
			errorLog.append("non-pixel: "+symbolTable.get(assignPixelStmt.lhsIdent.getText())+" assigned in a assignPixelStmt \n");
			return null;
		}
	}
	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		if(pixel.blueExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(pixel);
			errorLog.append("blue parameter in the pixel is not int \n");
			return null;
		} else if(pixel.redExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(pixel);
			errorLog.append("red parameter in the pixel is not int \n");
			return null;
		} else if(pixel.greenExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(pixel);
			errorLog.append("green parameter in the pixel is not int \n");
			return null;
		} else {
			return Kind.pixel;
		}
	}
	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		if(!symbolTable.containsKey(singlePixelAssignmentStmt.lhsIdent.getText())) {
			errorNodeList.add(singlePixelAssignmentStmt);
			errorLog.append("variable undefined: "+singlePixelAssignmentStmt.lhsIdent.getText()+"\n");
			return null;
		} else if(symbolTable.get(singlePixelAssignmentStmt.lhsIdent.getText())!=Kind.image) {
			errorNodeList.add(singlePixelAssignmentStmt);
			errorLog.append("non-image assigned in a singlePixelAssignmentStmt \n");
			return null;
		} else if(singlePixelAssignmentStmt.xExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(singlePixelAssignmentStmt);
			errorLog.append("non-int used as a xExpr\n");
			return null;
		} else if(singlePixelAssignmentStmt.yExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(singlePixelAssignmentStmt);
			errorLog.append("non-int used as a yExpr\n");
			return null;
		} else {
			//System.out.println("damne");
			singlePixelAssignmentStmt.pixel.visit(this,null);
			return null;
		}
		
	}
	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		if(!symbolTable.containsKey(singleSampleAssignmentStmt.lhsIdent.getText())) {
			errorNodeList.add(singleSampleAssignmentStmt);
			errorLog.append("variable undefined: "+singleSampleAssignmentStmt.lhsIdent.getText()+"\n");
			return null;
		} else if(symbolTable.get(singleSampleAssignmentStmt.lhsIdent.getText())!=Kind.image) {
			errorNodeList.add(singleSampleAssignmentStmt);
			errorLog.append("non-image assigned in a singleSampleAssignmentStmt \n");
			return null;
		} else if(singleSampleAssignmentStmt.xExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(singleSampleAssignmentStmt);
			errorLog.append("non-int used as a xExpr\n");
			return null;
		} else if(singleSampleAssignmentStmt.yExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(singleSampleAssignmentStmt);
			errorLog.append("non-int used as a yExpr\n");
			return null;
		} else if(singleSampleAssignmentStmt.rhsExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(singleSampleAssignmentStmt);
			errorLog.append("rhs is not _int\n");
			return null;
		}
		return null;
	}
	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		if(!symbolTable.containsKey(screenLocationAssignmentStmt.lhsIdent.getText())) {
			errorNodeList.add(screenLocationAssignmentStmt);
			errorLog.append("variable undefined: "+screenLocationAssignmentStmt.lhsIdent.getText()+"\n");
			return null;
		} else if(symbolTable.get(screenLocationAssignmentStmt.lhsIdent.getText())!=Kind.image) {
			errorNodeList.add(screenLocationAssignmentStmt);
			errorLog.append("screen location assigned to an non-image \n");
			return null;
		} else if(screenLocationAssignmentStmt.xScreenExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(screenLocationAssignmentStmt);
			errorLog.append("screen location xScreenExpr is not an int\n");
			return null;
		} else if(screenLocationAssignmentStmt.yScreenExpr.visit(this,null)!=Kind._int) {
			errorNodeList.add(screenLocationAssignmentStmt);
			errorLog.append("screen location yScreenExpr is not an int\n");
			return null;
		} else {
			return null;
		}
	}
	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		if(!symbolTable.containsKey(shapeAssignmentStmt.lhsIdent.getText())) {
			errorNodeList.add(shapeAssignmentStmt);
			errorLog.append("variable undefined: "+shapeAssignmentStmt.lhsIdent.getText()+"\n");
			return null;
		} else if(symbolTable.get(shapeAssignmentStmt.lhsIdent.getText())!=Kind.image) {
			errorNodeList.add(shapeAssignmentStmt);
			errorLog.append("type mismatch \n");
			return null;
		} else if(shapeAssignmentStmt.width.visit(this,null)!=Kind._int) {
			errorNodeList.add(shapeAssignmentStmt);
			errorLog.append("width is not an int\n");
			return null;
		} else if(shapeAssignmentStmt.height.visit(this,null)!=Kind._int) {
			errorNodeList.add(shapeAssignmentStmt);
			errorLog.append("height is not an int\n");
			return null;
		} else {
			return null;
		}
	}
	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		if(!symbolTable.containsKey(setVisibleAssignmentStmt.lhsIdent.getText())) {
			errorNodeList.add(setVisibleAssignmentStmt);
			errorLog.append("variable undefined: "+setVisibleAssignmentStmt.lhsIdent.getText()+"\n");
			return null;
		} else if(symbolTable.get(setVisibleAssignmentStmt.lhsIdent.getText())!=Kind.image) {
			errorNodeList.add(setVisibleAssignmentStmt);
			errorLog.append("setVisibleAssignmentStmt does not set an image\n");
			return null;
		} else if(setVisibleAssignmentStmt.expr.visit(this,null)!=Kind._boolean) {
			errorNodeList.add(setVisibleAssignmentStmt);
			errorLog.append("setVisibleAssignmentStmt citeria is not a bool\n");
			return null;
		} else {
			return null;
		}
	}
	@Override
	public Object FileAssignStmt(cop5555fa13.ast.FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		if(!symbolTable.containsKey(fileAssignStmt.lhsIdent.getText())) {
			errorNodeList.add(fileAssignStmt);
			errorLog.append("variable undefined: "+fileAssignStmt.lhsIdent.getText()+"\n");
			return null;
		} else if(symbolTable.get(fileAssignStmt.lhsIdent.getText()) == Kind.image) {
			return null;
		} else {
			errorNodeList.add(fileAssignStmt);
			errorLog.append("file assigned to an non-image\n");
			return null;
		}
	}
	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		if(conditionalExpr.trueValue.visit(this,null)!=conditionalExpr.falseValue.visit(this,null)) {
			errorNodeList.add(conditionalExpr);
			errorLog.append("type mismatch \n");
			return null;
		} else if(conditionalExpr.condition.visit(this,null) != Kind._boolean) {
			errorNodeList.add(conditionalExpr);
			errorLog.append("condition is not boolean\n");
			return null;
		}else {
			return conditionalExpr.trueValue.visit(this,null);
		}
	}
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		//System.out.println(binaryExpr.op.kind);
		if(binaryExpr.op.kind == Kind.AND || binaryExpr.op.kind == Kind.OR) {
			if(binaryExpr.e0.visit(this,null)!=Kind._boolean) {
				errorNodeList.add(binaryExpr);
				errorLog.append("expression 1 is not boolean\n");
				return null;
			} else if(binaryExpr.e1.visit(this,null)!=Kind._boolean) {
				errorNodeList.add(binaryExpr);
				errorLog.append("expression 2 is not boolean\n");
				return null;
			} else {
				return Kind._boolean;
			}
		} else if(binaryExpr.op.kind == Kind.PLUS || binaryExpr.op.kind == Kind.MINUS || binaryExpr.op.kind == Kind.TIMES || binaryExpr.op.kind == Kind.DIV || binaryExpr.op.kind == Kind.MOD) {
			if(binaryExpr.e0.visit(this,null)!=Kind._int) {
				errorNodeList.add(binaryExpr);
				errorLog.append("expression 1 is not _int\n");
				return null;
			} else if(binaryExpr.e1.visit(this,null)!=Kind._int) {
				errorNodeList.add(binaryExpr);
				errorLog.append("expression 2 is not _int\n");
				return null;
			} else {
				return Kind._int;
			}
		} else if (binaryExpr.op.kind == Kind.EQ || binaryExpr.op.kind == Kind.NEQ) {
			if(binaryExpr.e0.visit(this,null)!=binaryExpr.e1.visit(this,null)) {
				errorNodeList.add(binaryExpr);
				errorLog.append("rhs type: "+binaryExpr.e0.visit(this,null)+" lhs type: "+binaryExpr.e1.visit(this,null)+"\n");
				return Kind._boolean;
			} else {
				return Kind._boolean;
			}
		} else if (binaryExpr.op.kind == Kind.LSHIFT || binaryExpr.op.kind == Kind.RSHIFT) {
			if(binaryExpr.e0.visit(this,null)!=Kind._int) {
				errorNodeList.add(binaryExpr);
				errorLog.append("expression 1 is not _int\n");
				return null;
			} else if(binaryExpr.e1.visit(this,null)!=Kind._int) {
				errorNodeList.add(binaryExpr);
				errorLog.append("expression 2 is not _int\n");
				return null;
			} else {
				return Kind._int;
			}
		} else if (binaryExpr.op.kind == Kind.LT || binaryExpr.op.kind == Kind.GT || binaryExpr.op.kind == Kind.LEQ || binaryExpr.op.kind == Kind.GEQ) {
			if(binaryExpr.e0.visit(this,null)!=Kind._int) {
				errorNodeList.add(binaryExpr);
				errorLog.append("expression 1 is not _int\n");
				return null;
			} else if(binaryExpr.e1.visit(this,null)!=Kind._int) {
				errorNodeList.add(binaryExpr);
				errorLog.append("expression 2 is not _int\n");
				return null;
			} else {
				return Kind._boolean;
			}
		} else {
			errorNodeList.add(binaryExpr);
			errorLog.append("op is a"+binaryExpr.op.kind+"\n");
			return null;
		}
	}
	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {
		if(!symbolTable.containsKey(sampleExpr.ident.getText())) {
			errorNodeList.add(sampleExpr);
			errorLog.append("variable undefined: "+sampleExpr.ident.getText()+"\n");
			return null;
		} else if(symbolTable.get(sampleExpr.ident.getText())!=Kind.image) {
			errorNodeList.add(sampleExpr);
			errorLog.append("ident in sampleExpr is not image\n");
			return null;
		} else if(sampleExpr.xLoc.visit(this,null)!=Kind._int) {
			errorNodeList.add(sampleExpr);
			errorLog.append("xLoc in sampleExpr is not int\n");
			return null;
		} else if(sampleExpr.yLoc.visit(this,null)!=Kind._int) {
			errorNodeList.add(sampleExpr);
			errorLog.append("yLoc in sampleExpr is not int\n");
			return null;
		} else {
			return Kind._int;
		}
	}
	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		return Kind._int;
	}
	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		Kind retKind;
		if(!symbolTable.containsKey(identExpr.ident.getText())) {
			errorNodeList.add(identExpr);
			errorLog.append("variable undefined: "+identExpr.ident.getText()+"\n");
			return null;
		} else { 
			retKind = symbolTable.get(identExpr.ident.getText());
			return retKind;
		}
	}
	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
//		if(intLitExpr.intLit.kind!=Kind.INT_LIT) {
//			errorNodeList.add(intLitExpr);
//			errorLog.append("what is this i don't even\n");
//		}
		return Kind._int;
	}
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		return Kind._boolean;
	}
	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)
			throws Exception {
		return Kind._int;
	}
	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		if(!symbolTable.containsKey(assignExprStmt.lhsIdent.getText())) {
			//System.out.println("one!");
			errorNodeList.add(assignExprStmt);
			errorLog.append("variable undefined: "+assignExprStmt.lhsIdent.getText()+"\n");
			return null;
		} else if(symbolTable.get(assignExprStmt.lhsIdent.getText()) == assignExprStmt.expr.visit(this,null)) {
			//System.out.println("rwo!");
			return null;
		} else {
			//System.out.println("threee! THRRRREEEEEEE!");
			errorNodeList.add(assignExprStmt);
			errorLog.append("type of expr: " +assignExprStmt.expr.visit(this,null)+ " different from that of ident: "+symbolTable.get(assignExprStmt.lhsIdent.getText())+"\n");
			return null;
		}
	}
}