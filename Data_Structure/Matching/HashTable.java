import java.util.ArrayList;
import java.util.List;

/**
 * 
 * HashTable
 * Each element of HashTable - AVLTree
 *
 */
class HashTable
{
	public final int SIZE_OF_TABLE = 100;
	public AVLTree[] hashTable;	// 해시테이블
	
	public HashTable()
	{
		hashTable = new AVLTree[SIZE_OF_TABLE];
		
		for(int i = 0; i < SIZE_OF_TABLE; i++)
		{
			hashTable[i] = new AVLTree();
		}
	}
	
	public void insert(String str, int row, int startIndex) // String의 hashValue를 찾아서 hashTable에 삽입한다
	{
		int hashValue = getHashValue(str);
		
		hashTable[hashValue].setRoot(hashTable[hashValue].insert(hashTable[hashValue].getRoot(), str, row, startIndex));
	}
	
	public List<AVLItem> findByIndex(int index) // @ command가 들어왔을 때
	{
		List<AVLItem> result = hashTable[index].getStrings(hashTable[index].getRoot(), new ArrayList<AVLItem>());
		return result;
	}
	
	public List<AVLItem> findByString(String str) // ? command가 들어왔을 때
	{
		int hashValue = getHashValue(str);
		
		return hashTable[hashValue].getPositions(str);
	}
	
	public boolean contains()
	{
		//NOT IMPLEMENTED
		return true;
	}
	
	public void remove()
	{
		//NOT IMPLEMENTED
	}
	
	private int getHashValue(String str)  // Hash Value를 구한다
	{
		int hashValue = 0;
		
		for(int i = 0; i<str.length(); i++)
		{
			hashValue = (hashValue + (int)str.charAt(i)) % SIZE_OF_TABLE;
		}
		
		return hashValue;
	}
}