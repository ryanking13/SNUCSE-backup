import java.io.*;
import java.util.*;

public class SortingTest
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			boolean isRandom = false;	// 입력받은 배열이 난수인가 아닌가?
			int[] value;	// 입력 받을 숫자들의 배열
			String nums = br.readLine();	// 첫 줄을 입력 받음
			if (nums.charAt(0) == 'r')
			{
				// 난수일 경우
				isRandom = true;	// 난수임을 표시

				String[] nums_arg = nums.split(" ");

				int numsize = Integer.parseInt(nums_arg[1]);	// 총 갯수
				int rminimum = Integer.parseInt(nums_arg[2]);	// 최소값
				int rmaximum = Integer.parseInt(nums_arg[3]);	// 최대값

				Random rand = new Random();	// 난수 인스턴스를 생성한다.

				value = new int[numsize];	// 배열을 생성한다.
				for (int i = 0; i < value.length; i++)	// 각각의 배열에 난수를 생성하여 대입
					value[i] = rand.nextInt(rmaximum - rminimum + 1) + rminimum;
			}
			else
			{
				// 난수가 아닐 경우
				int numsize = Integer.parseInt(nums);

				value = new int[numsize];	// 배열을 생성한다.
				for (int i = 0; i < value.length; i++)	// 한줄씩 입력받아 배열원소로 대입
					value[i] = Integer.parseInt(br.readLine());
			}

			// 숫자 입력을 다 받았으므로 정렬 방법을 받아 그에 맞는 정렬을 수행한다.
			while (true)
			{
				int[] newvalue = (int[])value.clone();	// 원래 값의 보호를 위해 복사본을 생성한다.

				String command = br.readLine();

				long t = System.currentTimeMillis();
				switch (command.charAt(0))
				{
					case 'B':	// Bubble Sort
						newvalue = DoBubbleSort(newvalue);
						break;
					case 'I':	// Insertion Sort
						newvalue = DoInsertionSort(newvalue);
						break;
					case 'H':	// Heap Sort
						newvalue = DoHeapSort(newvalue);
						break;
					case 'M':	// Merge Sort
						newvalue = DoMergeSort(newvalue);
						break;
					case 'Q':	// Quick Sort
						newvalue = DoQuickSort(newvalue);
						break;
					case 'R':	// Radix Sort
						newvalue = DoRadixSort(newvalue);
						break;
					case 'X':
						return;	// 프로그램을 종료한다.
					default:
						throw new IOException("잘못된 정렬 방법을 입력했습니다.");
				}
				if (isRandom)
				{
					// 난수일 경우 수행시간을 출력한다.
					System.out.println((System.currentTimeMillis() - t) + " ms");
				}
				else
				{
					// 난수가 아닐 경우 정렬된 결과값을 출력한다.
					for (int i = 0; i < newvalue.length; i++)
					{
						System.out.println(newvalue[i]);
					}
				}

			}
		}
		catch (IOException e)
		{
			System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoBubbleSort(int[] value)
	{
		boolean isSwapped;
		
		for(int i=0; i<value.length - 1; i++)
		{
			isSwapped = false;
			for(int j = 0; j < value.length - 1 - i; j++)
			{
				if(value[j] > value[j+1])
				{
					Swap(value, j, j+1);
					isSwapped = true;
				}
			}
			
			// 한번도 Swap이 일어나지 않았다면 이미 정렬된 상태이므로 종료한다
			if(!isSwapped) break;
		}
		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoInsertionSort(int[] value)
	{
		for(int i = 1; i < value.length; i++)
		{
			int val = value[i]; // New element
			
			int j;
			for(j = i; j > 0; j--)
			{
				if(value[j-1] > val)
				{
					value[j] = value[j-1]; // Shift
					continue;
				}				
				break;
			}
			value[j] = val; // Insert
		}
		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoHeapSort(int[] value)
	{	
		int value_size = value.length;
		
		MakeHeap(value, value_size); // Array를 Heap으로 만든다
		
		int heap_size = value_size;
		
		for(int i = 0; i<value_size; i++)
		{
			heap_size--;
			Swap(value, 0, heap_size); // 가장 큰 element를 heap의 마지막 element와 swap한다
			SwapHeap(value, 0, heap_size); // 다시 heap의 성질을 만족하도록 modify한다
		}
		return (value);
	}

	private static void MakeHeap(int[] heap, int size)
	{
		for(int i = size/2; i>=0; i--)
		{
			SwapHeap(heap, i,  size);
		}
	}
	
	private static void SwapHeap(int[] heap, int index, int size)
	{
		int biggerindex = index;
		
		if( (index*2 + 1 < size) && (heap[biggerindex] < heap[index*2 + 1])) // left child
		{
			biggerindex = index*2+1;
		}
		if( (index*2 + 2 < size) && (heap[biggerindex] < heap[index*2 + 2])) // right child
		{
			biggerindex = index*2+2;
		}
		
		if(biggerindex != index)
		{
			Swap(heap, index, biggerindex);
			SwapHeap(heap, biggerindex, size);
		}		
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoMergeSort(int[] value)
	{
		int value_size = value.length;
		
		if(value_size < 2) return value;
		
		int[] value_1 = new int[value_size/2];
		int[] value_2 = new int[value_size - value_size/2];
		
		System.arraycopy(value, 0, value_1, 0, value_1.length);
		System.arraycopy(value, value_1.length, value_2, 0, value_2.length);
		
		DoMergeSort(value_1);
		DoMergeSort(value_2);
		
		int cnt_total = 0, cnt1 = 0, cnt2 = 0;
		
		while(cnt1 < value_1.length && cnt2 < value_2.length)
		{
			if(value_1[cnt1] < value_2[cnt2])
			{
				value[cnt_total++] = value_1[cnt1++];
			}
			else
			{
				value[cnt_total++] = value_2[cnt2++];
			}
		}
		
		while(cnt1 < value_1.length) value[cnt_total++] = value_1[cnt1++];
		while(cnt2 < value_2.length) value[cnt_total++] = value_2[cnt2++];
		
		return (value);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	private static int[] DoQuickSort(int[] value)
	{
		QuickSortSwapping(value, 0, value.length -1);
		return (value);
	}

	private static void QuickSortSwapping(int[] value, int start, int end)
	{
		if(start >= end) return;
		
		int mid_val = value[(start+end)/2];
		int left = start;
		int right = end;
		
		while(left < right)
		{
			while(value[left] < mid_val && left < right) left++;
			while(value[right] > mid_val && left < right) right--;
			
			if(left < right)
			{
				Swap(value, left++, right--);
			}
		}
		
		//같은 값을 같는 element들을 제외한다
		while(value[left] == mid_val && left > start) left--;
		while(value[right] == mid_val && right < end) right++;
		
		QuickSortSwapping(value, start, left);
		QuickSortSwapping(value, right, end);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////

	private static int[] DoRadixSort(int[] value)
	{
		int value_size = value.length;
		int[] count = new int[10];
		int max_num_length = 10; // length of MAX_INT;
		int radix_val;
		int[] positive_value = new int[value_size];
		int[] negative_value = new int[value_size];
		int positive_cnt = 0, negative_cnt = 0;
		
		//place positive and negative numbers in different array
		for(int i = 0; i < value_size; i++)
		{
			if(value[i] > 0) 
				positive_value[positive_cnt++] = value[i];
			else 
				negative_value[negative_cnt++] = -value[i]; // setting it positive (for comparing radix)
		}
		
		//negative
		for(int n = 0; n < max_num_length; n++)
		{
			Arrays.fill(count, 0);
			radix_val = (int)Math.pow(10, n);
			
			for(int i = 0; i<negative_cnt; i++)
			{
				int index = (negative_value[i] / radix_val) % 10;
				count[index]++;
			}
			
			for(int i = 1; i<count.length; i++)
			{
				count[i] += count[i-1];
			}
			
			for(int i = 0; i < negative_cnt; i++)
			{
				int index = (negative_value[i] / radix_val) % 10;
				value[negative_cnt - 1 - (count[index] - 1)] = negative_value[i]; // descending order ( in positive )
				count[index]--;
			}
			
			System.arraycopy(value, 0, negative_value, 0, negative_cnt);
		}
		
		for(int i = 0; i <negative_cnt; i++)
		{	
			value[i] = -value[i]; // returning to negative 
		}
		
		//positive
		for(int n = 0; n < max_num_length; n++)
		{
			Arrays.fill(count, 0);
			radix_val = (int)Math.pow(10, n);
			
			for(int i = 0; i<positive_cnt; i++)
			{
				int index = (positive_value[i] / radix_val) % 10;
				count[index]++;
			}
			
			for(int i = 1; i<count.length; i++)
			{
				count[i] += count[i-1];
			}
			
			for(int i = positive_cnt - 1; i >=0; i--)
			{
				int index = (positive_value[i] / radix_val) % 10;
				value[negative_cnt + count[index] - 1] = positive_value[i]; // place after negative
				count[index]--;
			}
			System.arraycopy(value, negative_cnt, positive_value, 0, positive_cnt);
		}
		
		return (value);
	}
	
	private static void Swap(int[] value, int p1, int p2)
	{
		int tp = value[p1];
		value[p1] = value[p2];
		value[p2] = tp;
	}
}