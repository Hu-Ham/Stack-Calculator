/*
 * Based on project submitted for: COSC 201, Data Structures and Algorithms, Spring 2020, with Professor Ariel Webster
 *  -Hugh Hamilton 
 * 
 * Program Summary:
 * The driver uses the .processInput() method to parse a string provided by the user to then perform various
 * calculations. Brackets to prioritize input is supported, as is addition, subtraction, multiplication,
 * division, and modulus, with both positive and negative numbers supported. User definition of custom variables 
 * (necessarily consisting of a single upper or lower case Roman alphabetical character) is permitted, and variables 
 * are mutable and accessible throughout run-time.
 * 
 * When initialized, this class initializes a hash table of 52 alphabetical values of a custom class,
 * which has both an integer value and the boolean field of whether it has been defined by the user (false
 * upon initialization).
 * Subsequently, the driver will call the .processInput() method, which will call a series of methods
 * to either evaluate the string passed to it by the driver for errors in formatting, or format it for
 * mathematical analysis. If errors make it impossible to process mathematically at any stage, a custom
 * error message will be printed to console, and the driver will advance to making the subsequent 
 * .processInput() call.
 * Once the checks and formatting have been done with, it is determined whether this is a variable 
 * assignment (ie defining a value for variable 'X'), or whether it is a purely arithmetic expression.
 * In either case, the formatted string will be passed to an instance of the InfixCalculate class to be 
 * arithmetically processed, by first being converted to an expression in postfix/Reverse Polish notation,
 * and then subsequently calculated through a stack-based approach. Once an integer value is calculated,
 * this value is passed back to the StackCalculator class instance, which will print this result to the
 * console in either the phrase "[VARIABLE] is set to [RESULT]" or more plainly "[RESULT]". In the case 
 * of the former, the arithmetic result will be passed to the table of variable names in O(1) time, and
 * the variable's "defined" boolean value will be set to "true".
 */
import java.util.Stack;

public class StackCalculator {
	
	public String inputString; //the original string including whitespace
	//Initialize an array of "variable" objects associated with a given letter of the alphabet, both
	//upper and lower-case.
	public Variable[] variableArray = new Variable[52];
	public Stack<Character> operatorStack;
	public Stack<Integer> operandStack;
	
	//Define default values 
	public StackCalculator(){ 
		//Table with indices corresponding to letters a-z and A-Z (ie 56 spots), each corresponding
		//to a potential variable name.
		for(int i = 0; i < 52; i++) {
			variableArray[i] = new Variable(0, false); //initialize all as false- ie none start out defined.
		}
	}
	
	public void processInput(String s) {
		inputString = s;
		//first thing: remove all whitespace
		s = removeWhitespace(s);
		//first check: balanced brackets
		if(!balancedBrackets(s)) 
			return;
		//second check: invalid characters
		if(!validCharacters(s))
			return;
		//third check: valid variable names
		if(!validVariableNames(s))
			return;
		//fourth check: nonsensical input
		if(!nonsensicalInput(s))
			return;
		//in this case it means check if there are two consecutive operators in the expression
		//If yes, present error message and return
		
		//now determine if expression is an equation
		//check for if there is more than one equals sign
		String[] splitS = s.split("=");
		if(splitS.length > 2) {
			System.out.println("Nonsensical Input " + inputString);
			return;
		}
		
		if(splitS.length == 1) { //not an equation
			//do defined variable check on s
			//if it fails, return
			if(!(isDefined(s)))
				return;
			
			//now process(s)- process method in document that calls other class elsewhere (at the moment
			//at least)
			System.out.println(stackCalculate(s)); //return answer
			//insert output of process(s) which is default zero
			return;
		}
		
		if(splitS.length == 2) { //split in half and therefore an equation
			String lhs = splitS[0]; //left hand side of equation
			String rhs = splitS[1];
			char variableChar = lhs.toCharArray()[0];
			
			if((lhs.length() != 1) || !isAlphabetical(variableChar)) { 
				//check for if there is anything before equals sign besides var
				//and then check if there is a non alphabetical character prior to the equals sign
				System.out.println("Nonsensical Input " + inputString);
				return;
			}
			
			//define result = process(splitS[1])
			int result = stackCalculate(rhs);
			System.out.println(splitS[0] + " is set to "  + result); // = result
			//call method to assign value of result to splitS[0] as a variable 
			//ie splitS[0].toCharArray()[0]
			setValue(result, variableChar); //set value of the variable to 
			return;
		}
	}
	//Method to remove all whitespace
	public String removeWhitespace(String hasWhitespace) {
		String noWhiteSpace = "";
		char[] stringChars = hasWhitespace.toCharArray();
		for(char c: stringChars) 
			if(c != ' ' && c != '	')
				noWhiteSpace += c;
		
		return noWhiteSpace;
	}
	//this takes the string and gives it an initial check to determine if the brackets in it are 
	//balanced correctly
	public boolean balancedBrackets(String bracketString) {
		//create a stack to hold the brackets as they are parsed by the loop that runs through the string
		//must return "too many left parentheses", "mismatched parentheses", 
		//or "too many right parentheses"
		Stack<Character> charStack = new Stack<Character>(); 
		//convert the input string to a char array
		char[] inputArray = bracketString.toCharArray();
		//run through whole array in for loop
		for(char c : inputArray) {
			if(c == '{' || c == '(' || c == '[') { //add left hand parentheses to stack
				charStack.push(c);
			}
			//for the case of an empty stack and a right hand parenthesis:
			if(charStack.isEmpty() && ( c == '}' || c == ')' || c == ']' )) {
				//Error message
				System.out.println("Unbalanced Parentheses Error, Mismatched Parentheses");
				return false;
			}
			
			//for nonempty stack, right hand parentheses: switch statement for value of c
			switch(c) { //if the values match, iterate the loop. Otherwise, return false after message.
			case '}' : 
				if(charStack.pop() == '{')
					break;
			case ')' : 
				if(charStack.pop() == '(')
					break;
			case ']' : 
				if(charStack.pop() == '[')
					break;
				//Error message 
				System.out.println("Unbalanced Parentheses Error, Mismatched Parentheses");
				return false;
			}
		}
		if(!charStack.isEmpty()) { //check for the cases of remaining parentheses
			char paren = charStack.pop();
			if(paren == '{' || paren == '(' || paren == '[') {
				System.out.println("Unbalanced Parentheses Error, Too Many Left Parentheses");
				return false;
			}
		}
		return true; //if the stack is empty and passed all tests
	}
	public boolean validCharacters(String testString) {
		//like the above- have error output messages
		//ALSO do the needful re: running
		char[] inputArray = testString.toCharArray();
		for(char c : inputArray) {
			//Valid characters:
			//A-Z, a-z, space, []{}(), 0-9, +-/*%= operands
			if(!((isNumber(c)) || //0-9
				(isOperator(c)) || //=,*,/,+,-,
				(isBrackets(c)) || //(,),[,],{,}
				(isAlphabetical(c)))) { //A-Z, a-z
				
				System.out.println("Invalid symbol " + c);
				return false;
			}
		}
		return true;
	}
	
	public boolean validVariableNames(String testString) {
		//go through loop until variable
		//store var as character in memory, continue going until next character is either 
		//another variable or an operator
		//if it is another alphabetical char, add to stored var name and go until either end of loop
		//or until equals sign. Then print "Invalid Variable Name VARNAME", VARNAME being the concat-
		//enated series of letters
		String varName = ""; //empty string that will have var chars concatenated with it. If length
		char prevLetter = '2'; //note that 2 is not a letter. This is so that by default it does not
		char[] inputArray = testString.toCharArray();
		//inputarray
		for(int i = 0; i < inputArray.length; i++) {
			if(isAlphabetical(inputArray[i]) && isAlphabetical(prevLetter)) { 
				varName += "" + prevLetter + inputArray[i];
				i++;
				while(i < inputArray.length && isAlphabetical(inputArray[i])) {
					varName += "" + inputArray[i];
					i++;
				}
				System.out.println("Invalid Variable Name " + varName);
				return false;
			}
			prevLetter = inputArray[i];
		}
		return true;
	}
	
	//Method to determine if there is invalid input
	public boolean nonsensicalInput(String testString) {
		//whitespace original string is included in event of displaying
		//"Nonsensical Input " + hasWhitespace
		//that stirng is "inputString"
		//in this case it means check if there are two consecutive operators in the expression, or
		//if the expression ends with an operator. If yes, present error message and return
		//one sticky bit- if the end operand is a closing bracket that's ok 
		char prevChar = 'j'; 
		char[] inputArray = testString.toCharArray();
		for(int i = 0; i < inputArray.length; i++) {
			//is operator and is not 
			//if it is not that case that --
			if((isOperator(inputArray[i]) && isOperator(prevChar)) &&
				!((inputArray[i] == '*') && (prevChar == '*')) &&
				!(inputArray[i] == '-')) {
				System.out.println("Nonsensical Input " + inputString); //class variable
				return false;
			}
			prevChar = inputArray[i];
		}
		return true; //if the test does not come back false
	}
	
	//Method to check for if defined
	//Method to determine whether a variable has been defined by the user
	public boolean isDefined(String testString) {
		char[] inputArray = testString.toCharArray();
		for(char c: inputArray) {
			if(isAlphabetical(c)) {
				int index = charToIndex(c);
				
				if(!variableArray[index].defined) {
					System.out.println("Undefined Variable " + c);
					return false;
				}
			}
		}
		return true;
	}
	
	//Ascii to index method
	public int charToIndex(char c) {
		int charVal = (int)c; //convert c to an ASCII value
		int index = 0; //initialize index with 'A'
		if(charVal >= 65 && charVal <= 90) //case: char c is an uppercase letter
			index = c - 65; //the index of this on the variable array for uppercase letters
		if(charVal >= 97 && charVal <= 122) //case: char c is a lowercase letter
			index = c - 71; //index for lowercase letters
		return index;
	}
	
	//Method for user to define a variable corresponding to an alphabetical character
	public void setValue(int value, char c) {
		int index = charToIndex(c);
		variableArray[index].setValue(value);
		variableArray[index].setDefined(true);
	}
	
	public int acquireValue(char c) {
		int index = charToIndex(c);
		return variableArray[index].getValue();
	}
		
	public boolean isAlphabetical(char input) {
		int asciiInput = (int)input;
		if((asciiInput >= 65 && asciiInput <= 90) || //A-Z ASCII (65-90 indices)
		   (asciiInput >= 97 && asciiInput <= 122)) //a-z ASCII (97-122 indices)
			return true;
		return false; //default case
	}
	public boolean isNumber(char input) {
		int asciiVal = (int)input;
		if(asciiVal >= 48 && asciiVal <= 57) //0-9 ASCII (48-57 indices)
			return true;
		return false;
	}
	public boolean isOperator(char input) {
		if((input == '/') || (input == '%') || (input == '=') || (input == '*') || 
		   (input == '+') || (input == '-'))
			return true;
		return false; //default case
	}
	public boolean isBrackets(char input) {
		if((input == '[') || (input == ']') || (input == '{') || (input == '}') ||
		   (input == '(') || (input == ')'))
			return true;
		return false; //default case
	}
	
	//Finally called method- converts unary negative operations from subtraction, and returns zero for
	//certain input string types, and initializes "InfixCalculate" objects.
	public int stackCalculate(String expression) {
		if(expression.length() == 0) //for cases of no input
			return 0;
		if(expression.length() >= 2 && expression.charAt(0) == '-' && expression.charAt(1) == '-') {
			expression = expression.replaceFirst("--", "");
		}
		
		if(expression.length() >= 2 && expression.charAt(0) == '-' && isNumber(expression.charAt(1))) {
			expression = expression.replaceFirst("-", "~");
		}
		if(!expression.contains("0") && !expression.contains("1") && !expression.contains("2")
		   && !expression.contains("3") && !expression.contains("4") && !expression.contains("5")
		   && !expression.contains("6") && !expression.contains("7") && !expression.contains("8")
		   && !expression.contains("9")) {
			return 0; //if it does not contain any number digits
		}
		
		//Substring replacement
		//turn into an array of strings - initial parsing. if it is a series of numbers before 
		//operands, turn into a single string
	//turn ** into ^, simplify --- to ~ and -- to nothing.
		expression = expression.replace("**", "^");
		while(expression.contains("---") || expression.contains("+--") || expression.contains("*--") ||
			  expression.contains("/--") || expression.contains("%--") || expression.contains("^--") ||
			  expression.contains("{--") || expression.contains("[--") || expression.contains("(--")){
			expression = expression.replace("---", "-");
			expression = expression.replace("+--", "+");
			expression = expression.replace("*--", "*");
			expression = expression.replace("/--", "/");
			expression = expression.replace("%--", "%");
			expression = expression.replace("^--", "^");
			expression = expression.replace("(--", "(");
			expression = expression.replace("[--", "[");
			expression = expression.replace("{--", "{");

		}
		
		while(expression.contains("--") || expression.contains("+-") || expression.contains("*-") ||
			  expression.contains("/-") || expression.contains("%-") || expression.contains("^-")) {
			expression = expression.replace("--", "-~");
			expression = expression.replace("+-", "+~");
			expression = expression.replace("*-", "*~");
			expression = expression.replace("/-", "/~");
			expression = expression.replace("%-", "%~");
			expression = expression.replace("^-", "^~");
		}
		
		
		//in addition: consider case of +--
		//in addition: consider case of initial - or initial --
		char[] expressionChArray = expression.toCharArray();
		//call variable assigner
		char prevChar = ' ';
		//for(char c: expressionChArray) {
		for(int i = 0; i < expressionChArray.length; i++) {
			//if i == 0 and i is -, then it is an n
			
			char c = expressionChArray[i]; //current char while traversing array
			if(isAlphabetical(c)) { //process acquireValue through stackCalculate method as well
				expression = expression.replace(c + "", replaceMinus(acquireValue(c)));
			}//to replace - with ~
			if(isOperator(prevChar)&& c == '-') { 
				
				if(i != 1) {
				//if preceding is -, and current is minus, and next is minus
				//then delete current and next
				//strBld.replace(i, i, "~");
					//expression.replaceFirst(prevChar + "-", prevChar + "~");
				//if an operator precedes a minus, ex -- or *-, then replace with '~'
				}
			}
			prevChar = c;
		}
		
		//System.out.println("EXPRESSION IS: " + expression);
		InfixCalculate infCal = new InfixCalculate(expression);
		int result = infCal.returnResult();
		//push digits to a digit stack
		//return stack calculated answer
		return result; //return result
	}
	public String replaceMinus(int varValue) {
		String valueString = "" + varValue;
		valueString = valueString.replaceAll("-", "~");
		return valueString;
	}
}

class Variable{
	int value;
	boolean defined;
	public Variable(int value, boolean defined) {
		this.value = value;
		this.defined = defined;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public boolean isDefined() {
		return defined;
	}
	public void setDefined(boolean defined) {
		this.defined = defined;
	}
	
}