import java.io.*;
import java.util.*;

public class CalculatorTest
{
	public static ArrayList<Character> operators = new ArrayList<Character>() {{
		add('+'); add('-'); add('*'); add('/');
		add('%'); add('~'); add('^'); add('('); add(')');
	}};
	
	public static ArrayList<Integer> operatorsOrder = new ArrayList<Integer>() {{
		add(1); add(1); add(2); add(2); 
		add(2); add(3); add(4); add(5); add(5);
	}}; // operator orders (각 index의 순서는 'operators'의 index와 동등 )
	
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			try
			{
				String input = br.readLine();
				if (input.compareTo("q") == 0)
					break;
				
				command(input);
			}
			catch (Exception e)
			{
				System.out.println("ERROR");
			}
		}
	}

	private static void command(String input) throws Exception
	{
		Stack<Pair> total_stk = new Stack<Pair>(); // stack of temporary results
		Stack<Character> operator_stk = new Stack<Character>(); // stack of operators
		char [] inputArray = input.toCharArray();
		
		boolean isNumberTurn = true;
		int counter = 0;
		
		while(counter < inputArray.length)
		{
			char ch = inputArray[counter];
			
			if(ch >= '0' && ch <= '9') // number
			{
				isNumberTurn = false;
				counter = ParseNumber(counter, inputArray, total_stk);
			}
			
			else if(operators.contains(ch)) // operator
			{
				if(isNumberTurn)
				{
					if(ch == '-') //unary
						ch = '~';
					
					else if(ch == '('); // do nothing
					
					else // Illegal operator
					{
						throw new Exception();
					}
				}
				
				try
				{
					FixOperatorOrder(ch, operator_stk, total_stk);
				}
				catch (Exception e)
				{
					//System.out.println("error here");
					throw new Exception();
				}
				
				if(ch != ')')
				{
					isNumberTurn = true;
				}
				
				counter++;
			}
			
			else if(ch == ' ' || ch == '\t')
			{
				counter++;
			}
			
			else
			{
				throw new Exception();
			}
		}
		
		if(isNumberTurn == true) // expression ends with operator ( except '(' )
			throw new Exception();
		
		while(!operator_stk.empty()) // calculate stack leftovers
		{
			Calculate(operator_stk.peek(), total_stk);
			operator_stk.pop();
		}
		
		
		if(total_stk.size() != 1) // illegal input
		{
			throw new Exception();
		}
		else
		{
			Pair p = total_stk.peek();
			total_stk.pop();
			System.out.println(p.getstr()); // postfix expression
			System.out.println(p.getnum()); // calculated result
		}
	}
	
	/**
	 * Parse number
	 * @param index that number starts
	 * @param array total input
	 * @param total_stk stack that parsed number will be stored
	 * @return index that number ends
	 */
	private static int ParseNumber(int cnt, char[] array, Stack<Pair> total_stk)
	{
		String num_str = new String();
		while(cnt < array.length && (array[cnt] >= '0' && array[cnt] <= '9'))
		{
			num_str += array[cnt];
			cnt++;
		}
		
		if(total_stk.empty())
		{
			total_stk.add(new Pair(Long.parseLong(num_str), num_str + " "));
		}
		
		else
		{
			String prevStr = total_stk.peek().getstr();
			total_stk.add(new Pair(Long.parseLong(num_str), prevStr + num_str + " "));
		}
		
		return cnt; // index that number ends
	}
	
	/**
	 * According to operator calculation order, do calculation and push operator to stack
	 * @param operator current operator input
	 * @param operator_stk stack of previous operators
	 * @param total_stk stack that will save calculated results
	 */
	private static void FixOperatorOrder(char operator, Stack<Character> operator_stk, Stack<Pair> total_stk) throws Exception
	{
		if(operator == '(')
		{
			operator_stk.add(operator);
		}
		
		else if(operator == ')')
		{
				while(operator_stk.peek() != '(') // do calculation until '(' comes
				{
					Calculate(operator_stk.peek(), total_stk);
					operator_stk.pop();
				}
				operator_stk.pop(); //remove '('
			
		}
		
		else // other operators
		{		
			while(!operator_stk.empty())
			{
				int stkOprOrder = operatorsOrder.get(operators.indexOf(operator_stk.peek())); // order of operator on top of stack
				int curOprOrder = operatorsOrder.get(operators.indexOf(operator)); // order of current operator
				
				if( operator_stk.peek() == '(' || stkOprOrder < curOprOrder) // check calculation order
					break;
				
				//right-assosiative operator check
				if(stkOprOrder == 3 && curOprOrder == 3) // if unary minus is on top of stack and unary minus is given
					break; // just push it to operator stack;
				
				else if(stkOprOrder == 4 && curOprOrder == 4) // if ^ is on top of stack and ^ is given
					break;

				Calculate(operator_stk.peek(), total_stk);
				operator_stk.pop();
			}

			operator_stk.push(operator);
		}
	}
	/**
	 * Do calculation
	 * @param operator operator or calculation
	 * @param total_stk stack that calculation result will be saved
	 */
	private static void Calculate(char operator, Stack<Pair> total_stk) throws Exception
	{
		long num1 = total_stk.peek().getnum();
		String str = total_stk.peek().getstr();
		total_stk.pop();
		
		if(operator == '~') //only unary operator
		{
			num1 = -num1;
			total_stk.push(new Pair(num1, str + "~ "));
			return;
		}

		long num2 = total_stk.peek().getnum();
		total_stk.pop();
		
		switch(operator)
		{	
		case '+':
			total_stk.push(new Pair(num2+num1, str + "+ "));
			break;
		
		case '-':
			total_stk.push(new Pair(num2-num1, str + "- "));
			break;
			
		case '*':
			total_stk.push(new Pair(num2*num1, str + "* "));
			break;
		
		case '/':
			if(num1 == 0) throw new Exception();
			total_stk.push(new Pair(num2/num1, str + "/ "));
			break;
			
		case '%':
			if(num1 == 0) throw new Exception();
			total_stk.push(new Pair(num2%num1, str + "% "));
			break;
		
		case '^':
			if(num1 < 0 && num2 == 0) throw new Exception();
			total_stk.push(new Pair((long) Math.pow(num2, num1), str + "^ "));
			break;
			
		default: //  '(', ')' or else
			throw new Exception();
		}
	}
}

class Pair
{
	private long num; // 다음에 사용되어야 하는 operand
	private String expr; // 계산 과정에서 변환된 postfix expression
	
	public Pair(long n, String str)
	{
		num = n;
		expr = str;
	}
	
	public long getnum()
	{
		return num;
	}
	
	public String getstr()
	{
		return expr;
	}
}