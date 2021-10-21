import java.io.*;
import java.util.*;

public class SortingTest
{
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			boolean isRandom = false;	// �Է¹��� �迭�� �����ΰ� �ƴѰ�?
			int[] value;	// �Է� ���� ���ڵ��� �迭
			String nums = br.readLine();	// ù ���� �Է� ����
			if (nums.charAt(0) == 'r')
			{
				// ������ ���
				isRandom = true;	// �������� ǥ��

				String[] nums_arg = nums.split(" ");

				int numsize = Integer.parseInt(nums_arg[1]);	// �� ����
				int rminimum = Integer.parseInt(nums_arg[2]);	// �ּҰ�
				int rmaximum = Integer.parseInt(nums_arg[3]);	// �ִ밪

				Random rand = new Random();	// ���� �ν��Ͻ��� �����Ѵ�.

				value = new int[numsize];	// �迭�� �����Ѵ�.
				for (int i = 0; i < value.length; i++)	// ������ �迭�� ������ �����Ͽ� ����
					value[i] = rand.nextInt(rmaximum - rminimum + 1) + rminimum;
			}
			else
			{
				// ������ �ƴ� ���
				int numsize = Integer.parseInt(nums);

				value = new int[numsize];	// �迭�� �����Ѵ�.
				for (int i = 0; i < value.length; i++)	// ���پ� �Է¹޾� �迭���ҷ� ����
					value[i] = Integer.parseInt(br.readLine());
			}

			// ���� �Է��� �� �޾����Ƿ� ���� ����� �޾� �׿� �´� ������ �����Ѵ�.
			while (true)
			{
				int[] newvalue = (int[])value.clone();	// ���� ���� ��ȣ�� ���� ���纻�� �����Ѵ�.

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
						return;	// ���α׷��� �����Ѵ�.
					default:
						throw new IOException("�߸��� ���� ����� �Է��߽��ϴ�.");
				}
				if (isRandom)
				{
					// ������ ��� ����ð��� ����Ѵ�.
					System.out.println((System.currentTimeMillis() - t) + " ms");
				}
				else
				{
					// ������ �ƴ� ��� ���ĵ� ������� ����Ѵ�.
					for (int i = 0; i < newvalue.length; i++)
					{
						System.out.println(newvalue[i]);
					}
				}

			}
		}
		catch (IOException e)
		{
			System.out.println("�Է��� �߸��Ǿ����ϴ�. ���� : " + e.toString());
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
			
			// �ѹ��� Swap�� �Ͼ�� �ʾҴٸ� �̹� ���ĵ� �����̹Ƿ� �����Ѵ�
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
		
		MakeHeap(value, value_size); // Array�� Heap���� �����
		
		int heap_size = value_size;
		
		for(int i = 0; i<value_size; i++)
		{
			heap_size--;
			Swap(value, 0, heap_size); // ���� ū element�� heap�� ������ element�� swap�Ѵ�
			SwapHeap(value, 0, heap_size); // �ٽ� heap�� ������ �����ϵ��� modify�Ѵ�
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
		
		//���� ���� ���� element���� �����Ѵ�
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