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
import java.util.*; //For the use of the Scanner class
public class CalculatorDriver {
	//Global variable so that once initialized, any saved values for user defined variables in the hash table are 
	//accessible across iterations of prompting and accepting string input from the user.
	StackCalculator cal;  
	public static void main(String[] args) {
		CalculatorDriver cd = new CalculatorDriver();
		cd.cal = new StackCalculator();
		//Greeting message
		System.out.println("The following is a calculator that accepts string input from the user.");
		System.out.println("This calculator accepts the operations +,-,*,/,^, and %. ");
		System.out.println("This calculator uses integer operands (positive and negative).");
		System.out.println("Brackets of the following type are also accepted for mathematical use: ()[]{} ");
		System.out.println("You may define a variable for use in this calculator, such as \"x = 7\".");
		System.out.println("That variable may then be used like any other integer value.");
		System.out.println("Invalid input will result in an error message specific to what you entered.");
		System.out.println("Type \"END\" to end the program. Until then, enjoy!");
		cd.inputPrompt();
	}
	public void inputPrompt() {
		Scanner inputScan = new Scanner(System.in);
		System.out.println("Please enter mathematical input:");
		String input = inputScan.nextLine();
		if(input.equalsIgnoreCase("END")) {
			System.out.println("Program end. Thank you for using this stack-based calculator.");
			return;
		}
		cal.processInput(input); 
		inputPrompt(); //repeat until end is typed
	}

}
