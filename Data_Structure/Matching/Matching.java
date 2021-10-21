import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class Matching
{
	public static final int LENGTH_OF_SUBSTRING = 6;
	public static HashTable hashTable = new HashTable();
	
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true)
		{
			try
			{
				String input = br.readLine();
				if (input.compareTo("QUIT") == 0)
					break;

				command(input);
			}
			catch (IOException e)
			{
				System.out.println("�Է��� �߸��Ǿ����ϴ�. ���� : " + e.toString());
			}
		}
	}

	private static void command(String input) throws IOException
	{
		char command = input.charAt(0); 
		input = input.substring(2); // command�� whitespace�� �����Ѵ�
		
		if(command == '<') // �� ������ �д´�
		{
			BufferedReader fr = new BufferedReader(new FileReader(input));
			
			hashTable = new HashTable();
			int row = 1; // Row of string
			
			while(true)
			{
				String line = fr.readLine();
				
				if(line == null) break; // End Of File
				
				for(int i = 0; i < line.length() - LENGTH_OF_SUBSTRING + 1; i++)
				{
					hashTable.insert(line.substring(i, i + LENGTH_OF_SUBSTRING), row, i+1);
				}
				
				row++;
			}
			
			fr.close();
		}
		
		else if(command == '@')  // Index�� ã�´�
		{
			
			List<AVLItem> stringList = hashTable.findByIndex(Integer.parseInt(input));
			
			if(stringList.isEmpty())  // If no element
				System.out.println("EMPTY");			
			else
			{
				for(int i = 0; i < stringList.size(); i++)
				{
					if(i != 0) System.out.print(" "); // �� string ������ ����
					System.out.print(stringList.get(i).getStr());
				}
				
				System.out.println(); // newline
			}
		}
		
		else if(command == '?') // String�� ã�´�
		{

			List<AVLItem> indexList = hashTable.findByString(input.substring(0, LENGTH_OF_SUBSTRING)); // �ʱ� ��ǥ ��
			
			for(int i = LENGTH_OF_SUBSTRING; i<input.length(); i+= LENGTH_OF_SUBSTRING)
			{
				if(input.length() - i < LENGTH_OF_SUBSTRING) // String�� ���̰� LENGTH_OF_STRING�� ����� �ƴѰ��, index�� ������ ��ܼ� Ȯ���Ѵ�
				{ 
					i = input.length() - LENGTH_OF_SUBSTRING;
				}
				
				List<AVLItem> nextList = hashTable.findByString(input.substring(i, i+LENGTH_OF_SUBSTRING)); // �̾����� ��ǥ ��
				List<AVLItem> newList = new ArrayList<AVLItem>();
				
				
				// indexList�� nextList�� ��ǥ�� ������踦 Ȯ���Ѵ�
				int j = 0, k = 0;
				while(j < indexList.size() && k < nextList.size())
				{
					AVLItem item = indexList.get(j);
					AVLItem nextItem = nextList.get(k);
					
					if(item.getRow() < nextItem.getRow()) j++;
					else if(item.getRow() > nextItem.getRow()) k++;
					
					else
					{
						if(item.getStartIndex() < nextItem.getStartIndex() - i) j++;
						else if(item.getStartIndex() > nextItem.getStartIndex() - i) k++;
						
						else  // ����Ǵ� ��ǥ
						{
							newList.add(item); 
							j++; k++;
						}
					}
					
				}
				indexList = newList; // ����Ǵ� ��ǥ�� ����� ������Ʈ�Ѵ�
				
			}
			
			if(indexList.isEmpty()) // If no element
				System.out.println("(0, 0)");
			
			else
			{
				for(int i = 0; i < indexList.size(); i++)
				{
					if(i != 0) System.out.print(" "); // �� index ������ ����					
					System.out.print("(" + indexList.get(i).getRow() + ", " + indexList.get(i).getStartIndex() + ")");
				}
				System.out.println(); // newline
			}
			
		}
		else
		{
			throw new IOException();
		}
	}
}