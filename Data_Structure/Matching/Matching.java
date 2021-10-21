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
				System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
			}
		}
	}

	private static void command(String input) throws IOException
	{
		char command = input.charAt(0); 
		input = input.substring(2); // command와 whitespace를 제거한다
		
		if(command == '<') // 새 파일을 읽는다
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
		
		else if(command == '@')  // Index를 찾는다
		{
			
			List<AVLItem> stringList = hashTable.findByIndex(Integer.parseInt(input));
			
			if(stringList.isEmpty())  // If no element
				System.out.println("EMPTY");			
			else
			{
				for(int i = 0; i < stringList.size(); i++)
				{
					if(i != 0) System.out.print(" "); // 각 string 사이의 공백
					System.out.print(stringList.get(i).getStr());
				}
				
				System.out.println(); // newline
			}
		}
		
		else if(command == '?') // String을 찾는다
		{

			List<AVLItem> indexList = hashTable.findByString(input.substring(0, LENGTH_OF_SUBSTRING)); // 초기 좌표 값
			
			for(int i = LENGTH_OF_SUBSTRING; i<input.length(); i+= LENGTH_OF_SUBSTRING)
			{
				if(input.length() - i < LENGTH_OF_SUBSTRING) // String의 길이가 LENGTH_OF_STRING의 배수가 아닌경우, index를 앞으로 당겨서 확인한다
				{ 
					i = input.length() - LENGTH_OF_SUBSTRING;
				}
				
				List<AVLItem> nextList = hashTable.findByString(input.substring(i, i+LENGTH_OF_SUBSTRING)); // 이어지는 좌표 값
				List<AVLItem> newList = new ArrayList<AVLItem>();
				
				
				// indexList와 nextList의 좌표간 연결관계를 확인한다
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
						
						else  // 연결되는 좌표
						{
							newList.add(item); 
							j++; k++;
						}
					}
					
				}
				indexList = newList; // 연결되는 좌표만 남기고 업데이트한다
				
			}
			
			if(indexList.isEmpty()) // If no element
				System.out.println("(0, 0)");
			
			else
			{
				for(int i = 0; i < indexList.size(); i++)
				{
					if(i != 0) System.out.print(" "); // 각 index 사이의 공백					
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