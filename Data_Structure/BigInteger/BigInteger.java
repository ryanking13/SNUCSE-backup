import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class BigInteger
{
    public static final String QUIT_COMMAND = "quit";
    public static final String MSG_INVALID_INPUT = "입력이 잘못되었습니다.";
 
    public static final Pattern OPERAND_PATTERN = Pattern.compile("[\\-\\+]?\\s*\\d+");
    public static final Pattern OPERATOR_PATTERN = Pattern.compile("\\d+\\s*[\\*\\-\\+]");
    
    private final int MAX_NUM_LENGTH = 203;
    private char[] number = new char[MAX_NUM_LENGTH];
    private boolean isNegative = false;
    
    public BigInteger(String s)
    {
    	char negativeCheck = s.charAt(0);
    	int stringCut = 0;
    	switch(negativeCheck)
    	{
    		case '+':
    			isNegative = false;
    			stringCut++; 			
    			break;
    		case '-':
    			isNegative = true;
    			stringCut++;
    			break;

    		default:
    			isNegative = false;
    			break;
    	}
    	
    	//empty space elimination
    	while(s.charAt(stringCut) == ' ' || s.charAt(stringCut) == '\t') stringCut++;    	
    	number = s.substring(stringCut).toCharArray();
    }
 
    public BigInteger add(BigInteger big)
    {
    	//setting expression to normal form ( num1(+) + num2(+) )
    	
    	if(this.isNegative == false && big.isNegative == true)
    	{
    		big.isNegative = false;
    		return this.subtract(big);
    	}
    	
    	else if(this.isNegative == true && big.isNegative == false)
    	{
    		this.isNegative = false;
    		return big.subtract(this);
    	}
    	
    	else if(this.isNegative == true && big.isNegative == true)
    	{
    		if(this.number[0] != '0' || big.number[0] != '0')
    				System.out.print('-');
    	}
    	
    	
    	char[] reversed_number = new char[MAX_NUM_LENGTH];
    	Arrays.fill(reversed_number, '0');
    	char[] true_number = new char[MAX_NUM_LENGTH];
    	int length = max(this.number.length, big.number.length);
    	
    	for(int i = 0; i < this.number.length; i++)
    	{
    		reversed_number[i] = this.number[this.number.length - 1 - i];
    	}
    	
    	for(int i = 0; i< big.number.length; i++)
    	{
    		reversed_number[i] += (big.number[big.number.length - 1 - i] - '0');
    	}
    	
    	for(int i = 0; i<length; i++)
    	{
    		int exceeding = reversed_number[i] - '0';
    		
    		if(exceeding > 9)
    		{
    			reversed_number[i] -= 10;
    			reversed_number[i+1]++;
    			
    			if(i == length -1)
    			{
    				length++;
    				break;
    			}
    		}
    	}
    	
    	//zero elimination
    	int cnt = 0;
    	while(cnt < length && reversed_number[length - 1 - cnt] == '0') 
    		cnt++;
    	
    	//exact zero check
    	if(cnt == length) true_number[0] = '0';
    	
    	for(int i = 0; i < length - cnt; i++)
    	{
    		true_number[i] = reversed_number[length - 1 - cnt - i];
    	}
    	
    	this.number = true_number;
    	return this;
    }
 
    public BigInteger subtract(BigInteger big)
    {
    	//setting expressing to normal form ( big(+) - small(+) )
    	
    	if(this.isNegative == false && big.isNegative == true)
    	{
    		big.isNegative = false;
    		return this.add(big);
    	}
    	
    	else if(this.isNegative == true && big.isNegative == false)
    	{
    		big.isNegative = true;
    		return this.add(big);
    	}
    	
    	else if(this.isNegative == true && big.isNegative == true)
    	{
    		big.isNegative = false;
    		this.isNegative = false;
    		return big.subtract(this);
    	}
    	
    	else if(isNumberSwap(this.number, big.number))
    	{
    		System.out.print('-');
    		return big.subtract(this);
    	}
    	
    	char[] reversed_number = new char[MAX_NUM_LENGTH];
    	Arrays.fill(reversed_number, '0');
    	char[] true_number = new char[MAX_NUM_LENGTH];
    	int length = max(this.number.length, big.number.length);
    	
    	for(int i = 0; i < this.number.length; i++)
    		reversed_number[i] = this.number[this.number.length - 1 - i];
    	
    	for(int i = 0; i < big.number.length; i++)
    	{
    		reversed_number[i] -= (big.number[big.number.length -1 -i] - '0');
    	}
    	for(int i = 0; i <length; i++)
    	{
    		int shortage = reversed_number[i] - '0';
    		
    		if(shortage < 0)
    		{
    			reversed_number[i+1]--;
    			reversed_number[i] +=10;
    		}
    	}    	
    	//zero elimination
    	int cnt = 0;
    	while(cnt < length && reversed_number[length - 1 - cnt] == '0') 
    		cnt++;
    	
    	//exact zero check
    	if(cnt == length) true_number[0] = '0';
    	
    	for(int i = 0; i< length - cnt; i++)
    		true_number[i] = reversed_number[length - 1 - cnt - i];
    	
    	this.number = true_number;
    	return this;
    }
 
    public BigInteger multiply(BigInteger big)
    {
    	
    	char[] reversed_number = new char[MAX_NUM_LENGTH];
    	Arrays.fill(reversed_number, '0');
    	char[] true_number = new char[MAX_NUM_LENGTH];
    	int length = this.number.length + big.number.length - 1;

    	for(int i = 0; i<this.number.length; i++)
    	{
    		for(int j = 0; j<big.number.length; j++)
    		{   			
    			reversed_number[i+j] += (this.number[this.number.length - 1 - i] - '0') * (big.number[big.number.length - 1 - j] - '0');
    		}
    	}
    	
    	for(int i = 0; i <length; i++)
    	{
    		int exceeding = reversed_number[i] - '0';
    		
    		if(exceeding > 9)
    		{
    			reversed_number[i+1] += exceeding / 10;
    			reversed_number[i] = (char)('0' + exceeding % 10);
    			
    			if(i == length - 1)
    			{
    				length++; break;
    			}
    		}
    	}
    	
    	//zero elimination
    	int cnt = 0;
    	while(cnt < length && reversed_number[length - 1 - cnt] == '0')
    		cnt++;
    	
    	//exact zero check
    	if(cnt == length) true_number[0] = '0';
    	
    	// add minus
    	if(this.isNegative != big.isNegative && cnt != length) System.out.print('-');
    	
    	for(int i = 0; i<length - cnt; i++)
    		true_number[i] = reversed_number[length - 1 - cnt - i];
    	
    	this.number = true_number;
    	return this;
    }
 
    @Override
    public String toString()
    {
    	String s = "";
    	for(char c : number)
    	{
    		if(c == '\u0000') //array end
    			break;
    		
    	    s += c;
    	}
    	return s;
    }
 
    static BigInteger evaluate(String input) throws IllegalArgumentException
    {
    	Matcher m = OPERAND_PATTERN.matcher(input);
    	m.find();
    	String arg1 = m.group();
    	m.find();
    	String arg2 = m.group();
    	
    	int arg2StartPosition = m.start();
    	
    	m = OPERATOR_PATTERN.matcher(input);   	
    	m.find();
    	char operand = input.charAt(m.end() - 1);
    	
    	//double operand elimination
    	if(arg2StartPosition == m.end() - 1)
    	{
    		arg2 = arg2.substring(1);
    	}
    	
    	//parsing check
    	//System.out.println("arg1 : " + arg1 + "\narg2 :" + arg2 + "\nop : " + operand);
    	
        BigInteger num1 = new BigInteger(arg1);
        BigInteger num2 = new BigInteger(arg2);
        BigInteger result = new BigInteger("init"); //just initializing
        
        switch(operand)
        {
        	case '+':
        		result = num1.add(num2);
        		break;
        	case '-':
        		result = num1.subtract(num2);
        		break;
        	case '*':
        		result = num1.multiply(num2);
        		break;
        }
        return result;
    }
 
    public static void main(String[] args) throws Exception
    {
        try (InputStreamReader isr = new InputStreamReader(System.in))
        {
            try (BufferedReader reader = new BufferedReader(isr))
            {
                boolean done = false;
                while (!done)
                {
                    String input = reader.readLine();
 
                    try
                    {
                        done = processInput(input);
                    }
                    catch (IllegalArgumentException e)
                    {
                        System.err.println(MSG_INVALID_INPUT);
                    }
                }
            }
        }
    }
 
    static boolean processInput(String input) throws IllegalArgumentException
    {
        boolean quit = isQuitCmd(input);
 
        if (quit)
        {
            return true;
        }
        else
        {
            BigInteger result = evaluate(input);
            System.out.println(result.toString());

            return false;
        }
    }
 
    static boolean isQuitCmd(String input)
    {
        return input.equalsIgnoreCase(QUIT_COMMAND);
    }
    
    int max(int a, int b)
    {
    	if(a > b) return a;
    	else return b;
    }
    
    //return true if b is bigger
    boolean isNumberSwap(char[] a, char[] b)
    {
    	if(a.length < b.length) return true;
    	else if(a.length == b.length)
    	{
    		for(int i = 0; i<a.length; i++)
    		{
    			if(a[i] > b[i]) return false;
    			else if(a[i] < b[i]) return true;
    		}
    		return false; // same
    	}	
    	else return false;
    }
}
