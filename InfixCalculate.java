import java.util.Stack;
/*
 * Summary:
 * This class is called by the CalculatorDriver class after the CalculatorDriver class has processed
 * strings that are unsuitable for mathematical operations (excess spaces, invalid characters, undefined
 * variables). 
 * This class' constructor initializes the infixTokenStack global variable of "tokens" of formatted
 * arithmetic data, and then calls the TokenizeString method to "tokenize" the mathematical expression,
 * dividing it into distinct operators and operands, then calls the infixToPostfix method to convert 
 * this stack of tokens to a stack that is in postfix/reverse Polish notation, and finally calls 
 * the postFixCalculate method, defining the global "result" integer variable as the result of 
 * the mathematical operations conducted upon the postfix token stack.
 * Note: substantial portions of this code is not original, and were copied wholesale from 
 * Mark Allen Weiss - Data Structures & Problem Solving Using Java. Fourth Edition. That said, nearly 
 * all of it is changed in some way, as this uses a multi step solution instead of Weiss' in-place 
 * post-fix calculations. This class can be easily modified to display the user's provided input in "postfix", or
 * reverse-Polish notation, which all "infix"-notated mathematical strings are converted to prior to their processing.
 */

public class InfixCalculate { 
	private int result;
	private Stack<Token> infixTokenStack;	
	private Stack<Token> operatorStack; // Operator stack for conversion 
	private Stack<Token> postfixStack; // Stack for postfix machine 
	
	//Final values used frequently in token definitions- correlating numbers with a token "type"
	//for legibility. Taken from textbook, besides addition of "MODULO" (and incrementation of
	//other tokens of lower priority).
	private static final int EOL = 0;
	private static final int VALUE = 1;
	private static final int OPAREN = 2;
	private static final int CPAREN = 3;
	private static final int EXP = 4;
	private static final int MODULO = 5; //modified from Evaluator class in textbook
	private static final int MULT = 6;
	private static final int DIV = 7;
	private static final int PLUS = 8; 
	private static final int MINUS = 9;
	
	//Prectable is an array of "precedence" objects (custom precedence class) from the textbook.
	//Its use is in comparing the relative precedences of tokens in the postfix stack calculations,
	//especially when comparing the precedence of operators relative to one another.
	//The indices of precedence objects here match the indices of their associated final values seen
	//above- for instance VALUE (all integer tokens) has value 1, and precTable[1] is associated with it.
	//"precTable matches order of Token enumeration"
	//int type, int value
	private Precedence[] precTable = {
		new Precedence(0, -1),	//EOL
		new Precedence(0, 0),	//Value
		new Precedence(100, 0),	//OParen
		new Precedence(0, 99),	//CParen
		//increase precedence for exp
		new Precedence(8, 7),	//EXP
		//new precedence for modulo
		new Precedence(5, 6),	//MODULO
		new Precedence(3, 4),	//MULT
		new Precedence(3, 4),	//DIV
		new Precedence(1, 2),	//PLUS
		new Precedence(1, 2)	//MINUS
	};
	/*// A now unused main method that may be used to test the postfix calculator with pre-formatted data:
	public static void main(String[] args) {
		
		InfixCalculate infc = new InfixCalculate("12*~80"); 
		
		System.out.println("Result is: " + infc.result);
				
	}
	//*/
	
	//Called by StackCalculator. Tokenizes expression, converts to postfix, and then calculates
	//to return an integer result
	public InfixCalculate(String inputString) {
		infixTokenStack = new Stack<Token>();
		tokenizeString(inputString);//Tokenizer and place in infixTokenStack
		infixToPostfix();
		result = postFixCalculate();
	}

	//Method that returns the integer result of the calculations from the methods called above.
	public int returnResult() {
		//run the various things needed to be run here
		return result;
	}
	
	//Called by class constructor. Uses formatted string. Makes "tokens", which are individual custom
	//objects that have a particular "type" and a "value"- the value of all non integer tokens is zero.
	public void tokenizeString(String inputString) {
		char[] charArray = inputString.toCharArray();
		String numberString = ""; //initialize blank string to append numbers onto
		for(int i = 0; i < charArray.length; i++) {
			char cha = charArray[i];
			if(cha == '^') //call Tokens using the single integer constructor 
				infixTokenStack.push(new Token(EXP)); 
			if(cha == '/')
				infixTokenStack.push(new Token(DIV)); 
			if(cha == '*')
				infixTokenStack.push(new Token(MULT)); 
			if(cha == '%')
				infixTokenStack.push(new Token(MODULO)); 
			if(cha == '(' || cha == '[' || cha == '{') { 
				infixTokenStack.push(new Token(OPAREN));}
			if(cha == ')' || cha == ']' || cha == '}') {
				infixTokenStack.push(new Token(CPAREN)); }
			if(cha == '+')
				infixTokenStack.push(new Token(PLUS));
			if(cha == '-')
				infixTokenStack.push(new Token(MINUS));
			
			//Concatenate integer digits
			if(Character.isDigit(cha) || cha == '~')
				//otherwise add thing to numberstring
				numberString += cha;
			
			//if end of numberString, make numberString "" again and then push value
			//ie if you have all digits of a number- now time to parse it and push its value
			if(numberString != "" && (i == charArray.length -1 || (!Character.isDigit(charArray[i + 1]))
					/*&& !isBrackets(charArray[i+1])*/)) {
				int valueInt = interpretInt(numberString);
				infixTokenStack.push(new Token(VALUE, valueInt));
				numberString = "";
			}
		}
	}
	
	//All negative unary operators were converted to "~" instead of "-"
	//As a result, they must be re-converted prior to mathematical processing.
	public int interpretInt (String intString) {
		if(intString.contains("~")) {
			intString = intString.replace("~", "");
			return (-1)*Integer.parseInt(intString); //convert to negative
		}
		return Integer.parseInt(intString); //for value without ~
	}
	
	//After tokenization- convert to reverse Polish notation 
	public void infixToPostfix() { 
		operatorStack = new Stack<>(); //initialize stack to hold operators
		postfixStack = new Stack<>(); //initialize stack to hold ALL values
		for(Token t : infixTokenStack) { //traverse stack from bottom value to top
			int tokenType = t.getType();
			
			if(t.getType() == VALUE) { //case: integer token
				postfixStack.push(t);
			}
			else if(t.getType() == OPAREN) {//case: open parentheses, ie (,[,{
				operatorStack.push(t);
			} 
			else if(t.getType() == CPAREN) {
				Token popVal = operatorStack.pop();

				while(popVal.getType() != OPAREN && !operatorStack.isEmpty()) {
					postfixStack.push(popVal);
					popVal = operatorStack.pop();
				}
			}
			else { //case: non bracket operator token
				if(!operatorStack.isEmpty() && precTable[tokenType].inputSymbol
						<= precTable[operatorStack.peek().getType()].topOfStack) {
					postfixStack.push(operatorStack.pop());						
				}
				operatorStack.push(t);
			}
			
		}
		//final operation- push any remaining values from operator stack to poxfix
		while(!operatorStack.isEmpty()) {
			Token operatorToken = operatorStack.pop();
			if(operatorToken.getValue() != OPAREN)
				postfixStack.push(operatorToken);
		}
		
	}
	
	public int postFixCalculate() {
		Stack<Integer> resultsStack = new Stack<Integer>();
		for(Token pft: postfixStack) { //postfix token
			if(pft.getType() == VALUE) 
				resultsStack.push(pft.getValue());
			else {
				int operatorValue = pft.getType(); // "Top operator"
				
				int rhs = resultsStack.pop();
				int lhs = resultsStack.pop();
				
				if(operatorValue == EXP) //case of exponentiation
					resultsStack.push((int) Math.pow(lhs, rhs)); //cast to int from double
				
				else if(operatorValue == PLUS)
					resultsStack.push(lhs + rhs);
				
				else if(operatorValue == MINUS )
					resultsStack.push( lhs - rhs );
				
				else if(operatorValue == MULT)
					resultsStack.push(lhs * rhs);
				
				else if(operatorValue == DIV)
					resultsStack.push(lhs / rhs);
				
				else if(operatorValue == MODULO)
					resultsStack.push(lhs % rhs);
			}
		}
		return resultsStack.pop(); 
	}
	
	//copy and paste of StackCalculator's isBrackets method. Called for basic checks.
	public boolean isBrackets(char input) {
		if((input == '[') || (input == ']') || (input == '{') || (input == '}') ||
		   (input == '(') || (input == ')'))
			return true;
		return false; //default case
	}	
	
	//From the textbook:
	//private class Token- three overloaded methods to initialize. has int type and int value
	private class Token { //static class?
		public Token() { 
			this( 0 ); 
		}
		public Token( int t ) { 
			this( t, 0 ); 
		}
		public Token( int t, int v ) { 
			type = t; value = v; 
		}
		
		public int getType() { 
			return type; 
		}
		public int getValue( ) { 
			return value; 
		}
		private int type = 0;
		private int value = 0;	
	}
	
	//precedence class- "input symbol" and "top of stack" variables are crucial in determining relative
	//precedence of tokens.
	private class Precedence { 
		public int inputSymbol;
		public int topOfStack;
		
		public Precedence( int inSymbol, int topSymbol) {
			inputSymbol = inSymbol;
			topOfStack = topSymbol;
		}
	}
}

