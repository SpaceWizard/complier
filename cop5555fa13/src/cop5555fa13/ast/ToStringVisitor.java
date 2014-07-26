package cop5555fa13.ast;

public class ToStringVisitor implements ASTVisitor {
	
	StringBuilder sb;
	
	public ToStringVisitor(){
		sb = new StringBuilder();
	}

	public String getString() {
		return sb.toString();
	}

	@Override
	public Object visitDec(Dec dec, Object arg) {
		sb.append(arg)
		.append("Dec:")
		.append(dec.type)
		.append(" ")
		.append(dec.ident.getText());
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		sb.append(arg)
		.append("Program:")
		.append(program.ident.getText());
		String indent = arg + "  ";
		for (Dec dec: program.decList){sb.append('\n'); dec.visit(this,indent); ;}
		for (Stmt stmt: program.stmtList){sb.append('\n'); stmt.visit(this, indent);}
		sb.append('\n');
		return null;
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		sb.append(arg)
		.append("AlternativeStmt:\n");
		String indent0 = arg + "  ";
		alternativeStmt.expr.visit(this,indent0);
		sb.append(indent0 + "if");
		String indent1 = indent0 + "  ";
		for (Stmt stmt: alternativeStmt.ifStmtList){sb.append('n'); stmt.visit(this, indent1);}
		sb.append(indent0 + "else");
		for (Stmt stmt: alternativeStmt.elseStmtList){sb.append('n'); stmt.visit(this, indent1);}
		return null;
	}

	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg) throws Exception {
		sb.append(arg)
		.append("PauseStatement:\n");
		String indent = arg + "  ";
		pauseStmt.expr.visit(this,indent);
		return null;
	}

	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg) throws Exception {
		sb.append(arg)
		.append("IterationStmt:\n");
		String indent = arg + "  ";
		iterationStmt.expr.visit(this,indent);
		indent = indent + "  ";
		for (Stmt stmt: iterationStmt.stmtList){sb.append('\n'); stmt.visit(this, indent);}
		return null;
	}

	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		String indent = arg + "  ";
		sb.append(arg)
		.append("AssignPixelStmt:\n")
		.append(indent);
		sb.append(assignPixelStmt.lhsIdent.getText()).append('\n');
		assignPixelStmt.pixel.visit(this,indent);
		return null;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg) throws Exception {
		String indent = arg + "  ";
		sb.append(arg)
		.append("AssignExprStmt:")
		.append('\n')
		.append(indent)
		.append(assignExprStmt.lhsIdent.getText())
		.append('\n');
		assignExprStmt.expr.visit(this,indent);
		return null;
	}


	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		sb.append(arg)
		.append("Pixel:\n");
		String indent = arg + "  ";	
		pixel.redExpr.visit(this,indent);
		sb.append('\n');
		pixel.greenExpr.visit(this, indent);
		sb.append('\n');
		pixel.blueExpr.visit(this, indent);
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("SinglePixelAssignmentStmt:\n")
		.append(indent)
		.append(singlePixelAssignmentStmt.lhsIdent.getText())
		.append('\n');
		singlePixelAssignmentStmt.xExpr.visit(this,indent);
		sb.append("\n");
		singlePixelAssignmentStmt.yExpr.visit(this,indent);
		sb.append("\n");
		singlePixelAssignmentStmt.pixel.visit(this,indent);

		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("SingleSampleAssignmentStmt:\n")
		.append(indent)
		.append(singleSampleAssignmentStmt.lhsIdent.getText())
		.append('\n');
		singleSampleAssignmentStmt.xExpr.visit(this,indent);
		sb.append('\n');
		singleSampleAssignmentStmt.yExpr.visit(this,indent);
		sb.append('\n')
		.append(indent)
		.append(singleSampleAssignmentStmt.color.getText())
		.append('\n');
		singleSampleAssignmentStmt.rhsExpr.visit(this,indent);
		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("ScreenLocationAssignmentStmt:\n")
		.append(indent)
		.append(screenLocationAssignmentStmt.lhsIdent)
		.append('\n');
		screenLocationAssignmentStmt.xScreenExpr.visit(this,indent);
		sb.append('\n');
		screenLocationAssignmentStmt.yScreenExpr.visit(this,indent);
		return null;

	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("ShapeAssignmentStmt:\n")
		.append(indent)
		.append(shapeAssignmentStmt.lhsIdent)
		.append('\n');
		shapeAssignmentStmt.width.visit(this,indent);
		sb.append('\n');
		shapeAssignmentStmt.height.visit(this,indent);
		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg) throws Exception {
		String indent = arg + "  ";	
		sb.append(arg)
		.append("SetVisibleAssignmentStmt:\n")
		.append(indent)
		.append(setVisibleAssignmentStmt.lhsIdent)
		.append('\n');
		setVisibleAssignmentStmt.expr.visit(this,indent);
		return null;
	}

	@Override
	public Object FileAssignStmt(FileAssignStmt fileAssignStmt,
			Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("FileAssignStmt:\n")
		.append(indent)
		.append(fileAssignStmt.lhsIdent)
		.append('\n')
		.append(indent)
		.append(fileAssignStmt.fileName);
		return null;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("ConditionalExpr:\n");
		conditionalExpr.condition.visit(this, indent);
		sb.append('\n');
		conditionalExpr.trueValue.visit(this, indent);
		sb.append('\n');
		conditionalExpr.falseValue.visit(this, indent);
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("BinaryExpr:\n");
		binaryExpr.e0.visit(this, indent);
		sb.append(indent)
		.append(binaryExpr.op.getText())
		.append('\n');
		binaryExpr.e1.visit(this, indent);
		return null;
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("SampleExpr:\n")
		.append(sampleExpr.ident.getText())
		.append('\n');
		sampleExpr.xLoc.visit(this,indent);
		sb.append('\n');
		sampleExpr.yLoc.visit(this,indent);
		sb.append(indent)
		.append(sampleExpr.color);
		return null;
	}

	@Override
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception{
		String indent = arg + "  ";	
		sb.append(arg)
		.append("ImageAttributeExpr:\n")
		.append(indent)
		.append(imageAttributeExpr.ident.getText())
		.append('\n')
		.append(indent)
		.append(imageAttributeExpr.selector.getText());
		return null;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception{	
		sb.append(arg)
		.append("IdentExpr: ")
		.append(identExpr.ident.getText());
		return null;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception{
		sb.append(arg)
		.append("IntLitExpr: ")
		.append(intLitExpr.intLit.getText());
		return null;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception{
		sb.append(arg)
		.append("BooleanLitExpr: ")
		.append(booleanLitExpr.booleanLit.getText());
		return null;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)throws Exception {
		sb.append(arg)
		.append("PreDefExpr: ")
		.append(PreDefExpr.constantLit.getText()); 
		return null;
	}


	
	

}
