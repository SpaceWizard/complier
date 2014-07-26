package cop5555fa13.ast;

import static cop5555fa13.TokenStream.Kind.*;

import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.runtime.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {
	
	private ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
	private String progName;
	
	private int slot = 0;
	private int getSlot(String name){
		Integer s = slotMap.get(name);
		if (s != null) return s;
		else{
			slotMap.put(name, slot);
			return slot++;
		}		
	}

	HashMap<String,Integer> slotMap = new HashMap<String,Integer>();
	
	// map to look up JVM types correspondingHashMap<K, V> language
	static final HashMap<Kind, String> typeMap = new HashMap<Kind, String>();
	static {
		typeMap.put(_int, "I");
		typeMap.put(pixel, "I");
		typeMap.put(_boolean, "Z");
		typeMap.put(image, "Lcop5555fa13/runtime/PLPImage;");
	}

	@Override
	public Object visitDec(Dec dec, Object arg) {
		MethodVisitor mv = (MethodVisitor)arg;
		//insert source line number info into classfile
		Label l = new Label();
		mv.visitLabel(l);
		mv.visitLineNumber(dec.ident.getLineNumber(),l);
		//get name and type
		String varName = dec.ident.getText();
		Kind t = dec.type;
		String jvmType = typeMap.get(t);
		Object initialValue = (t == _int || t==pixel || t== _boolean) ? Integer.valueOf(0) : null;
		//add static field to class file for this variable
		FieldVisitor fv = cw.visitField(ACC_STATIC, varName, jvmType, null,
				initialValue);
		fv.visitEnd();
		//if this is an image, generate code to create an empty image
		if (t == image){
			//System.out.println("imageinit");
			mv.visitTypeInsn(NEW, PLPImage.className);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, PLPImage.className, "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, progName, varName, typeMap.get(image));
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String sourceFileName = (String) arg;
		progName = program.getProgName();
		String superClassName = "java/lang/Object";

		// visit the ClassWriter to set version, attributes, class name and
		// superclass name
		cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, progName, null, superClassName,
				null);
		//Optionally, indicate the name of the source file
		cw.visitSource(sourceFileName, null);
		// initialize creation of main method
		String mainDesc = "([Ljava/lang/String;)V";
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", mainDesc, null, null);
		mv.visitCode();
		Label start = new Label();
		mv.visitLabel(start);
		mv.visitLineNumber(program.ident.getLineNumber(), start);		
		
		slotMap.put("x", 0);
		slotMap.put("y", 1);
		slot = 2;
		
		//visit children
		for(Dec dec : program.decList){
			dec.visit(this,mv);
		}
		for (Stmt stmt : program.stmtList){
			stmt.visit(this, mv);
		}
		
		//add a return statement to the main method
		mv.visitInsn(RETURN);
		
		//finish up
		Label end = new Label();
		mv.visitLabel(end);
		//visit local variables. The one is slot 0 is the formal parameter of the main method.
		mv.visitLocalVariable("args","[Ljava/lang/String;",null, start, end, getSlot("args"));
		//if there are any more local variables, visit them now.
		// ......
		mv.visitLocalVariable("x","I",null, start, end, getSlot("x"));
		mv.visitLocalVariable("y","I",null, start, end, getSlot("y"));
		//finish up method
		mv.visitMaxs(1,1);
		mv.visitEnd();
		//convert to bytearray and return 
		return cw.toByteArray();
	}

	@Override
	public Object visitAlternativeStmt(AlternativeStmt alternativeStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		Label elseLabel = new Label();
		Label endOfAlternativeLabel = new Label();
		alternativeStmt.expr.visit(this,mv);
		mv.visitJumpInsn(IFEQ,elseLabel);
		for (Stmt ifStmt : alternativeStmt.ifStmtList) {
			ifStmt.visit(this,mv);
		}
		mv.visitJumpInsn(GOTO,endOfAlternativeLabel);
		mv.visitLabel(elseLabel);
		for (Stmt ifStmt : alternativeStmt.elseStmtList) {
			ifStmt.visit(this,mv);
		}
		mv.visitLabel(endOfAlternativeLabel);
		return null;
	}

	@Override
	public Object visitPauseStmt(PauseStmt pauseStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		pauseStmt.expr.visit(this, mv);
		mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, 
	    		"pause", "(I)V");
		return null;
	}

	@Override
	public Object visitIterationStmt(IterationStmt iterationStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		Label bodyLabel = new Label();
		Label guardLabel = new Label();
		mv.visitJumpInsn(GOTO,guardLabel);
		mv.visitLabel(bodyLabel);
		for (Stmt iterStmt : iterationStmt.stmtList) {
			iterStmt.visit(this,mv);
		}
		mv.visitLabel(guardLabel);
		iterationStmt.expr.visit(this,mv);
		mv.visitJumpInsn(IFNE,bodyLabel);
		return null;
	}

	@Override
	public Object visitAssignPixelStmt(AssignPixelStmt assignPixelStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//System.out.println("wat");
		if(assignPixelStmt.imageFlag == false) {
			//System.out.println("pxel");
			assignPixelStmt.pixel.visit(this,mv);
			mv.visitVarInsn(ISTORE,getSlot(assignPixelStmt.lhsIdent.getText()));
		} else {
			//System.out.println("img");
			Label Body = new Label();
			Label widthGuard = new Label();
			Label heightGuard = new Label();
			String imageName = assignPixelStmt.lhsIdent.getText();
			
//		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//		mv.visitVarInsn(ISTORE,getSlot(assignPixelStmt.lhsIdent.getText()));
//		mv.visitTypeInsn(INSTANCEOF,"I");
//		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
			
			mv.visitLdcInsn(0);
			mv.visitVarInsn(ISTORE, 0);
			mv.visitLdcInsn(0);
			mv.visitVarInsn(ISTORE, 1);
			
			mv.visitJumpInsn(GOTO,heightGuard);
			mv.visitLabel(Body);
			
			mv.visitFieldInsn(GETSTATIC, progName, imageName, typeMap.get(image));
			mv.visitVarInsn(ILOAD,0);
			mv.visitVarInsn(ILOAD,1);
			assignPixelStmt.pixel.visit(this,mv);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
					"setPixel", "(III)V");
//		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
//		    	"updateFrame", PLPImage.updateFrameDesc);
			
			mv.visitIincInsn(1,1);	
			mv.visitLabel(heightGuard);
			mv.visitVarInsn(ILOAD,1);
			mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className,"getHeight", "()I");
			mv.visitJumpInsn(IF_ICMPLT,Body);
			
			mv.visitLabel(widthGuard);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			mv.visitIincInsn(0,1);
			mv.visitVarInsn(ILOAD,0);
			mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className,"getWidth", "()I");
			mv.visitJumpInsn(IF_ICMPLT,Body);
			mv.visitFieldInsn(GETSTATIC, progName, imageName, typeMap.get(image));
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
			    	"updateFrame", PLPImage.updateFrameDesc);
		}
//		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//		mv.visitLdcInsn("faggot");
//		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
		return null;
	}

	@Override
	public Object visitPixel(Pixel pixel, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		pixel.redExpr.visit(this, mv);
		pixel.greenExpr.visit(this, mv);
		pixel.blueExpr.visit(this, mv);
		mv.visitMethodInsn(INVOKESTATIC, "cop5555fa13/runtime/Pixel", 
	    		"makePixel", "(III)I");
		return null;
	}

	@Override
	public Object visitSinglePixelAssignmentStmt(
			SinglePixelAssignmentStmt singlePixelAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = singlePixelAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, typeMap.get(image));
		mv.visitInsn(DUP);
		singlePixelAssignmentStmt.xExpr.visit(this, mv);
		singlePixelAssignmentStmt.yExpr.visit(this, mv);
		singlePixelAssignmentStmt.pixel.visit(this, mv);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"setPixel", "(III)V");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitSingleSampleAssignmentStmt(
			SingleSampleAssignmentStmt singleSampleAssignmentStmt, Object arg)
			throws Exception {
		//System.out.println("vizit sample assign");
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = singleSampleAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, typeMap.get(image));
		mv.visitInsn(DUP);
		singleSampleAssignmentStmt.xExpr.visit(this, mv);
		singleSampleAssignmentStmt.yExpr.visit(this, mv);
		if(singleSampleAssignmentStmt.color.getText().equals("red")){
			mv.visitLdcInsn(0);
		} else if(singleSampleAssignmentStmt.color.getText().equals("green")) {
			mv.visitLdcInsn(1);
		} else if(singleSampleAssignmentStmt.color.getText().equals("blue")) {
			mv.visitLdcInsn(2);
		} else {
			System.out.println("problem sample assign");
		}
		singleSampleAssignmentStmt.rhsExpr.visit(this, mv);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"setSample", "(IIII)V");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", "()V");
		return null;
	}

	@Override
	public Object visitScreenLocationAssignmentStmt(
			ScreenLocationAssignmentStmt screenLocationAssignmentStmt,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = screenLocationAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName, imageName, typeMap.get(image));
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		screenLocationAssignmentStmt.xScreenExpr.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "x_loc", "I");
		screenLocationAssignmentStmt.yScreenExpr.visit(this, mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "y_loc", "I");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object visitShapeAssignmentStmt(
			ShapeAssignmentStmt shapeAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = shapeAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		shapeAssignmentStmt.height.visit(this ,mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "height", "I");
		shapeAssignmentStmt.width.visit(this ,mv);
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "width", "I");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateImageSize", "()V");
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", "()V");
		return null;
	}

	@Override
	public Object visitSetVisibleAssignmentStmt(
			SetVisibleAssignmentStmt setVisibleAssignmentStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave image on top of stack
		String imageName = setVisibleAssignmentStmt.lhsIdent.getText();
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		//duplicate address.  Will consume one for updating setVisible field
		//and one for invoking updateFrame.
		mv.visitInsn(DUP);
		//visit expr on rhs to leave its value on top of the stack
		setVisibleAssignmentStmt.expr.visit(this,mv);
		//set visible field
		mv.visitFieldInsn(PUTFIELD, PLPImage.className, "isVisible", 
				"Z");	
	    //generate code to update frame, consuming the second image address.
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		return null;
	}

	@Override
	public Object FileAssignStmt(cop5555fa13.ast.FileAssignStmt fileAssignStmt,
			Object arg) throws Exception {
		if (fileAssignStmt.fileName.kind == Kind.STRING_LIT){
		MethodVisitor mv = (MethodVisitor)arg;
		//generate code to leave address of target image on top of stack
	    String image_name = fileAssignStmt.lhsIdent.getText();
	    //if(fileAssignStmt.fileName)
	    mv.visitFieldInsn(GETSTATIC, progName, image_name, typeMap.get(image));
	    //generate code to duplicate this address.  We'll need it for loading
	    //the image and again for updating the frame.
	    mv.visitInsn(DUP);
		//generate code to leave address of String containing a filename or url
	    mv.visitLdcInsn(fileAssignStmt.fileName.getText().replace("\"", ""));
		//System.out.println(fileAssignStmt.fileName);
	    //generate code to get the image by calling the loadImage method
	    mv.visitMethodInsn(INVOKEVIRTUAL, 
	    		PLPImage.className, "loadImage", PLPImage.loadImageDesc);
	    //generate code to update frame
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"updateFrame", PLPImage.updateFrameDesc);
		}
	    return null;
	    
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr,
			Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		Label falseConditionLabel = new Label();
		Label endOfExprLabel = new Label();
		conditionalExpr.condition.visit(this,mv);
		mv.visitJumpInsn(IFEQ,falseConditionLabel);
		conditionalExpr.trueValue.visit(this,mv);
		mv.visitJumpInsn(GOTO,endOfExprLabel);
		mv.visitLabel(falseConditionLabel);
		conditionalExpr.falseValue.visit(this,mv);
		mv.visitLabel(endOfExprLabel);
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		Label jmpLabel = new Label();
		Label endOfExprLabel = new Label();
		binaryExpr.e0.visit(this,mv);
		binaryExpr.e1.visit(this,mv);
		Kind opKind = binaryExpr.op.kind;
		if(opKind == Kind.PLUS) {
			mv.visitInsn(IADD);
		} else if(opKind == Kind.MINUS) {
			mv.visitInsn(ISUB);
		} else if(opKind == Kind.TIMES) {
			mv.visitInsn(IMUL);
		} else if(opKind == Kind.MOD) {
			mv.visitInsn(IREM);
		} else if(opKind == Kind.DIV) {
			mv.visitInsn(IDIV);
		} else if(opKind == Kind.LSHIFT) {
			mv.visitInsn(ISHL);
		} else if(opKind == Kind.RSHIFT) {
			mv.visitInsn(ISHR);
		} else if(opKind == Kind.AND) {
			mv.visitInsn(IAND);
		} else if(opKind == Kind.OR) {
			mv.visitInsn(IOR);
		} else if(opKind == Kind.EQ) {
			mv.visitJumpInsn(IF_ICMPEQ,jmpLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO,endOfExprLabel);
			mv.visitLabel(jmpLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
		} else if(opKind == Kind.NEQ) {
			mv.visitJumpInsn(IF_ICMPNE,jmpLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO,endOfExprLabel);
			mv.visitLabel(jmpLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
		} else if(opKind == Kind.LT) {
			mv.visitJumpInsn(IF_ICMPLT,jmpLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO,endOfExprLabel);
			mv.visitLabel(jmpLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
		} else if(opKind == Kind.GT) {
			mv.visitJumpInsn(IF_ICMPGT,jmpLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO,endOfExprLabel);
			mv.visitLabel(jmpLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
		} else if(opKind == Kind.LEQ) {
			mv.visitJumpInsn(IF_ICMPLE,jmpLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO,endOfExprLabel);
			mv.visitLabel(jmpLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
		} else if(opKind == Kind.GEQ) {
			mv.visitJumpInsn(IF_ICMPGE,jmpLabel);
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO,endOfExprLabel);
			mv.visitLabel(jmpLabel);
			mv.visitLdcInsn(1);
			mv.visitLabel(endOfExprLabel);
		} else {
			System.out.println("sonething went wrong in binary"+opKind);
		}
		return null;
	}

	@Override
	public Object visitSampleExpr(SampleExpr sampleExpr, Object arg)
			throws Exception {
		//System.out.println("sample visited");
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = sampleExpr.ident.getText();
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		sampleExpr.xLoc.visit(this,mv);
		sampleExpr.yLoc.visit(this,mv);
		if(sampleExpr.color.getText().equals("red")){
			mv.visitLdcInsn(0);
		} else if(sampleExpr.color.getText().equals("green")) {
			mv.visitLdcInsn(1);
		} else if(sampleExpr.color.getText().equals("blue")) {
			mv.visitLdcInsn(2);
		} else {
			System.out.println("something is wrong in sample "+sampleExpr.color.getText());
		}
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, 
	    		"getSample", "(III)I");
		return null;
	}

	@Override
	//TODO changed
	public Object visitImageAttributeExpr(
			ImageAttributeExpr imageAttributeExpr, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String imageName = imageAttributeExpr.ident.getText();
		mv.visitFieldInsn(GETSTATIC, progName,imageName,PLPImage.classDesc);
		//mv.visitInsn(DUP);
		Kind attr = imageAttributeExpr.selector.kind;
		if(attr == Kind.width) {
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className,"getWidth", "()I");
		} else if(attr == Kind.height) {
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className,"getHeight", "()I");
		} else if(attr == Kind.x_loc) {
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className,"getX_loc", "()I");
		} else if(attr == Kind.y_loc) {
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className,"getY_loc", "()I");
		}
		return null;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		mv.visitVarInsn(ILOAD,getSlot(identExpr.ident.getText()));
		return null;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		int intLitValue;
		intLitValue = intLitExpr.intLit.getIntVal();
		mv.visitLdcInsn(intLitValue);
		return null;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		String lit = booleanLitExpr.booleanLit.getText();
		int val = lit.equals("true")? 1 : 0;
		mv.visitLdcInsn(val);
		return null;
	}

	@Override
	public Object visitPreDefExpr(PreDefExpr PreDefExpr, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		Kind kind = PreDefExpr.constantLit.kind;
		if(kind == Kind.Z){
			mv.visitLdcInsn(255);
		} else if(kind == Kind.SCREEN_SIZE) {
			mv.visitLdcInsn(PLPImage.SCREENSIZE);
		} else if(kind == Kind.x) {
			mv.visitVarInsn(ILOAD,0);
		} else if(kind == Kind.y) {
			mv.visitVarInsn(ILOAD,1);
		}
		//mv.visitLdcInsn(PreDefExpr.constantLit.getIntVal());
		return null;
	}

	@Override
	public Object visitAssignExprStmt(AssignExprStmt assignExprStmt, Object arg)
			throws Exception {
		MethodVisitor mv = (MethodVisitor)arg;
		//System.out.println("assign phase1");
		assignExprStmt.expr.visit(this, mv);
		//System.out.println("assign phase2");
		mv.visitVarInsn(ISTORE,getSlot(assignExprStmt.lhsIdent.getText()));
		//System.out.println("assign phase3");
		return null;
	}

}

