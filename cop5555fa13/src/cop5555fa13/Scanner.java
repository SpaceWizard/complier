package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.AND;
import static cop5555fa13.TokenStream.Kind.ASSIGN;
import static cop5555fa13.TokenStream.Kind.COLON;
import static cop5555fa13.TokenStream.Kind.COMMA;
import static cop5555fa13.TokenStream.Kind.COMMENT;
import static cop5555fa13.TokenStream.Kind.DIV;
import static cop5555fa13.TokenStream.Kind.DOT;
import static cop5555fa13.TokenStream.Kind.EOF;
import static cop5555fa13.TokenStream.Kind.EQ;
import static cop5555fa13.TokenStream.Kind.GEQ;
import static cop5555fa13.TokenStream.Kind.GT;
import static cop5555fa13.TokenStream.Kind.IDENT;
import static cop5555fa13.TokenStream.Kind.INT_LIT;
import static cop5555fa13.TokenStream.Kind.LEQ;
import static cop5555fa13.TokenStream.Kind.LPAREN;
import static cop5555fa13.TokenStream.Kind.LSHIFT;
import static cop5555fa13.TokenStream.Kind.LSQUARE;
import static cop5555fa13.TokenStream.Kind.LT;
import static cop5555fa13.TokenStream.Kind.MINUS;
import static cop5555fa13.TokenStream.Kind.MOD;
import static cop5555fa13.TokenStream.Kind.NEQ;
import static cop5555fa13.TokenStream.Kind.NOT;
import static cop5555fa13.TokenStream.Kind.OR;
import static cop5555fa13.TokenStream.Kind.PLUS;
import static cop5555fa13.TokenStream.Kind.QUESTION;
import static cop5555fa13.TokenStream.Kind.RPAREN;
import static cop5555fa13.TokenStream.Kind.RSHIFT;
import static cop5555fa13.TokenStream.Kind.RSQUARE;
import static cop5555fa13.TokenStream.Kind.SEMI;
import static cop5555fa13.TokenStream.Kind.TIMES;
import static cop5555fa13.TokenStream.Kind.Z;
import static cop5555fa13.TokenStream.Kind._else;
import static cop5555fa13.TokenStream.Kind._if;
import static cop5555fa13.TokenStream.Kind._int;
import static cop5555fa13.TokenStream.Kind.image;
import static cop5555fa13.TokenStream.Kind.red;
import static cop5555fa13.TokenStream.Kind.x;
import static cop5555fa13.TokenStream.Kind.y;
import static cop5555fa13.TokenStream.Kind.*;

import java.io.FileReader;
import java.util.HashMap;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;

public class Scanner {

 //ADD METHODS AND FIELDS
	public static TokenStream stream;
	public Scanner(TokenStream inputstream) {
		stream = inputstream;
	}
	
	private int index = 0;
	
	private int ch;
	
	private enum State{
		START,
		EOF,
		WHITE_SPACE,
		COMMENT,
		GOT_BACKSLASH,
		GOT_NOT,
		IDENT_PART,
		DIGITS,
		GOT_EQUAL,
		GOT_LESS_THAN,
		GOT_GREATER_THAN,
		GOT_DOUBLE_QUOTE
	}
	
	public void fetchChar(){
		if(index>=stream.inputChars.length) {
			ch = -1;
		} else {
			ch = stream.inputChars[index];
		}
		index++;
	}
	
//	public Scanner(TokenStream stream) {
//		//IMPLEMENT THE CONSTRUCTOR
//	}


	public void scan() throws LexicalException {
		//THIS IS PROBABLY COMPLETE
		Token t;
		do {
			t = next();
			if (t.kind.equals(COMMENT)) {
				stream.comments.add((Token) t);
			} else
				stream.tokens.add(t);
		} while (!t.kind.equals(EOF));
	}

	private Token next() throws LexicalException{
        State state = State.START;
        
		Token t = null;
		
		int tokenBegin = 0;
        
		StringBuilder sb = new StringBuilder();
		do {
			
			fetchChar();
			switch(state) {
			case START:
				tokenBegin = index-1;
				switch(ch) {
				case -1:
					t = stream.new Token(EOF,tokenBegin,index);
					break;
				case ' ': case'\r': case'\n': case'\t':
					break;
				case '.':
					t = stream.new Token(DOT,tokenBegin,index);
					break;
				case ';':
					t = stream.new Token(SEMI,tokenBegin,index);
					break;
				case ',':
					t = stream.new Token(COMMA,tokenBegin,index);
					break;
				case '(':
					t = stream.new Token(LPAREN,tokenBegin,index);
					break;
				case ')':
					t = stream.new Token(RPAREN,tokenBegin,index);
					break;
				case '[':
					t = stream.new Token(LSQUARE,tokenBegin,index);
					break;
				case ']':
					t = stream.new Token(RSQUARE,tokenBegin,index);
					break;
				case '{':
					t = stream.new Token(LBRACE,tokenBegin,index);
					break;
				case '}':
					t = stream.new Token(RBRACE,tokenBegin,index);
					break;
				case ':':
					t = stream.new Token(COLON,tokenBegin,index);
					break;
				case '?':
					t = stream.new Token(QUESTION,tokenBegin,index);
					break;
				case '|':
					t = stream.new Token(OR,tokenBegin,index);
					break;
				case '&':
					t = stream.new Token(AND,tokenBegin,index);
					break;
				case '+':
					t = stream.new Token(PLUS,tokenBegin,index);
					break;
				case '-':
					t = stream.new Token(MINUS,tokenBegin,index);
					break;
				case '*':
					t = stream.new Token(TIMES,tokenBegin,index);
					break;
				case '%':
					t = stream.new Token(MOD,tokenBegin,index);
					break;
				case '0':
					t = stream.new Token(INT_LIT,tokenBegin,index);
					break;
				case '/':
					state = State.GOT_BACKSLASH;
					break;
				case '=':
					state = State.GOT_EQUAL;
					break;
				case '!':
					state = State.GOT_NOT;
					break;
				case '"':
					state = State.GOT_DOUBLE_QUOTE;
					break;
				case '<':
					state = State.GOT_LESS_THAN;
					break;
				case '>':
					state = State.GOT_GREATER_THAN;
					break;
				default:
					if(Character.isDigit(ch)) {
						state = State.DIGITS;
					} else if (Character.isJavaIdentifierPart(ch)) {
						sb.append((char)ch);
						state = State.IDENT_PART;
					}
					else {
						throw stream.new LexicalException(tokenBegin,"illegal character");
					}
				}
				break;
			case GOT_BACKSLASH :
				if (ch == '/') {
					state = State.COMMENT;
				}
				else {
					index--;
					t = stream.new Token(DIV,tokenBegin,index);
				}
				break;
			case COMMENT :
				if(ch=='\r' || ch=='\n' || ch==-1) {
					index--;
					t = stream.new Token(COMMENT,tokenBegin,index);
				}
				break;
			case GOT_EQUAL:
				if(ch=='=') {
					t = stream.new Token(EQ,tokenBegin,index);
				}
				else {
					index--;
					t = stream.new Token(ASSIGN,tokenBegin,index);
				}
				break;
			case GOT_NOT:
				if(ch=='=') {
					t = stream.new Token(NEQ,tokenBegin,index);
				}
				else {
					index--;
					t = stream.new Token(NOT,tokenBegin,index);
				}
				break;
			case GOT_LESS_THAN :
				switch(ch) {
				case '=':
					t = stream.new Token(LEQ,tokenBegin,index);
					break;
				case '<':
					t = stream.new Token(LSHIFT,tokenBegin,index);
					break;
				default:
					index--;
					t = stream.new Token(LT,tokenBegin,index);
				}
				break;
			case GOT_GREATER_THAN :
				switch(ch) {
				case '=':
					t = stream.new Token(GEQ,tokenBegin,index);
					break;
				case '>':
					t = stream.new Token(RSHIFT,tokenBegin,index);
					break;
				default:
					index--;
					t = stream.new Token(GT,tokenBegin,index);
				}
				break;
			case GOT_DOUBLE_QUOTE:
				
				if(ch=='"') {
					t = stream.new Token(STRING_LIT,tokenBegin,index);
				} else if (ch==-1) {
					throw stream.new LexicalException(tokenBegin, "string not properly closed");
				}
				break;
			case DIGITS:
				if(!Character.isDigit(ch)) {
					index--;
					t = stream.new Token(INT_LIT,tokenBegin,index);
				}
				break;
			case IDENT_PART:
				if(Character.isJavaIdentifierPart(ch)) {
					sb.append((char)ch);
				}
				else {
					index--;
					String temp = sb.toString();
					//System.out.println(temp);
					switch(temp){
						case "image":
							t = stream.new Token(image,tokenBegin,index);
							break;
						case "int":
							t = stream.new Token(_int,tokenBegin,index);
							break;
						case "boolean":
							t = stream.new Token(_boolean,tokenBegin,index);
							break;
						case "pixel":
							t = stream.new Token(pixel,tokenBegin,index);
							break;
						case "pixels":
							t = stream.new Token(pixels,tokenBegin,index);
							break;
						case "red":
							t = stream.new Token(red,tokenBegin,index);
							break;
						case "green":
							t = stream.new Token(green,tokenBegin,index);
							break;
						case "blue":
							t = stream.new Token(blue,tokenBegin,index);
							break;
						case "Z":
							t = stream.new Token(Z,tokenBegin,index);
							break;
						case "shape":
							t = stream.new Token(shape,tokenBegin,index);
							break;
						case "width":
							t = stream.new Token(width,tokenBegin,index);
							break;
						case "height":
							t = stream.new Token(height,tokenBegin,index);
							break;
						case "location":
							t = stream.new Token(location,tokenBegin,index);
							break;
						case "x_loc":
							t = stream.new Token(x_loc,tokenBegin,index);
							break;
						case "y_loc":
							t = stream.new Token(y_loc,tokenBegin,index);
							break;
						case "SCREEN_SIZE":
							t = stream.new Token(SCREEN_SIZE,tokenBegin,index);
							break;
						case "visible":
							t = stream.new Token(visible,tokenBegin,index);
							break;
						case "x":
							t = stream.new Token(x,tokenBegin,index);
							break;
						case "y":
							t = stream.new Token(y,tokenBegin,index);
							break;
						case "pause":
							t = stream.new Token(pause,tokenBegin,index);
							break;
						case "while":
							t = stream.new Token(_while,tokenBegin,index);
							break;
						case "if":
							t = stream.new Token(_if,tokenBegin,index);
							break;
						case "else":
							t = stream.new Token(_else,tokenBegin,index);
							break;
						case "true":
							t = stream.new Token(BOOLEAN_LIT,tokenBegin,index);
							break;
						case "false":
							t = stream.new Token(BOOLEAN_LIT,tokenBegin,index);
							break;
						default:
							t = stream.new Token(IDENT,tokenBegin,index);
					}
				}
				break;
			default:
				throw stream.new LexicalException(tokenBegin,"undefined state");
			}
		} while (t==null);
		
		return t;
		
        //COMPLETE THIS METHOD.  THIS IS THE FUN PART!
	}
}
