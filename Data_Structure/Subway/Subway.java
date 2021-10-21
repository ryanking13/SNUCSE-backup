import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Subway {

	public static Map<String, Integer> stationNumberMap  = new HashMap<String, Integer>(); // �� ��ȣ�� ã�� ���� �ؽ���
	public static Map<String, List<Integer>> stationNameMap = new HashMap<String, List<Integer>>(); // �� �̸����� ã�� ���� �ؽ���
	
	public static List<Station> stationList = new ArrayList<Station>(); // �׷��� ������ �� ��������Ʈ
	
	public static final int TRANSFER_COST = 5;
	
	public static void main(String[] args)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		try{
			composeSubway(new File(args[0]));
		}
		catch(Exception e){
			System.out.println("Error : " + e.getMessage());
			return;
		}
		
		while(true)
		{
			try {
				String input = br.readLine();
				
				if(input.compareTo("QUIT") == 0)
					break;
				
				String[] inputs = input.split(" ");
				
				if(inputs.length == 2) //�ִܽð����
				{
					getMinimumTime(stationNameMap.get(inputs[0]).get(0), stationNameMap.get(inputs[1]).get(0));
				}
				else if(inputs.length == 3 && inputs[2].compareTo("!") == 0) //�ּ�ȯ�°��
				{
					getMinimumTransfer(stationNameMap.get(inputs[0]).get(0), stationNameMap.get(inputs[1]).get(0));
				}
				
			} 
			catch (IOException e) {
				System.out.println("Error : " + e.getMessage());
			}
		}
	}
	
	/** �Է¹��� �뼱���� �������� �׷����� �����Ѵ� **/
	public static void composeSubway(File file) throws IOException
	{
			BufferedReader fr = new BufferedReader(new FileReader(file));

			// �� ������ �Է¹޴´�.
			while(true)
			{		
				String input;
				
				if((input = fr.readLine()) == null){ // ���� ���� �� ���� input�� ���� ���
					fr.close(); 
					throw new IOException();
				}
				
				if(input.isEmpty()) // �� ���� �Է��� ����
					break;
				
				String[] inputs = input.split(" ");
				
				addStation(inputs[0], inputs[1], inputs[2]);			
			}
			
			// ���� �Ÿ� ������ �Է¹޴´�.
			while(true)
			{
				String input;
				
				if((input = fr.readLine()) == null) // EOF
					break;
				
				String[] inputs = input.split(" ");
				
				addConnection(stationNumberMap.get(inputs[0]), stationNumberMap.get(inputs[1]), Integer.parseInt(inputs[2]));
			}
			
			fr.close();		
	}

	public static void addStation(String number, String name, String line)
	{
		int cnt = stationList.size();
		
		
		stationList.add(new Station(number, name, line));
		
		stationNumberMap.put(number, cnt);
		
		// ���� �̸��� ���� �ִ� ��� - ȯ�¿�
		if(stationNameMap.containsKey(name))
		{
			List<Integer> indexList = stationNameMap.get(name);
			
			for(int index: indexList)
			{
				stationList.get(index).connectedStations.add(new Edge(cnt, TRANSFER_COST));
				stationList.get(cnt).connectedStations.add(new Edge(index, TRANSFER_COST));
			}
			
			indexList.add(cnt);
		}
		
		else
		{
			List<Integer> list = new ArrayList<Integer>();
			list.add(cnt);
			stationNameMap.put(name, list);
		}
	}
	
	public static void addConnection(int departure, int arrival, int cost)
	{
		stationList.get(departure).connectedStations.add(new Edge(arrival, cost));
	}

	
	/** �ִܽð���θ� ���Ѵ� **/
	public static void getMinimumTime(int departure, int arrival)
	{
		List<Track> distance = new ArrayList<Track>(); // �� vertex�� �̸��� �Ÿ� ����
		PriorityQueue<PairInt> queue = new PriorityQueue<PairInt>();
		
		for(int i = 0; i < stationList.size(); i++)
			distance.add(new Track()); // �ʱ� �Ÿ� initialize
	
		queue.add(new PairInt(0, departure));
		distance.get(departure).set(0, -1); // ���� ���̹Ƿ� previousIndex�� ����(-1)
		
		for(Edge e : stationList.get(departure).connectedStations) //���� �������� ȯ�� �ҿ�ð��� ����.
		{
			if(stationList.get(departure).name.equals(stationList.get(e.to).name))
				e.cost = 0;
		}
		for(Edge e : stationList.get(arrival).connectedStations) // ������ �������� ȯ�� �ҿ�ð��� ����.
		{
			if(stationList.get(arrival).name.equals(stationList.get(e.to).name))
			{
				for(Edge arr : stationList.get(e.to).connectedStations)
				{
					if(arr.to == arrival)
						arr.cost = 0;
				}
			}
		}
		
		while(!queue.isEmpty())
		{
			PairInt pi = queue.poll();
			int currStation = pi.second;

			if(pi.first > distance.get(currStation).timeSum) // �̹� �� ���� ������ ������Ʈ �� ��� �����Ѵ�
				continue;
			if(currStation == arrival) // arrival���� �ִܰ�ΰ� ã���� ��� �������´�
				break;
			
			List<Edge> connectedStations = stationList.get(currStation).connectedStations;
			
			for(int i = 0; i < connectedStations.size(); i++)
			{
				Edge e = connectedStations.get(i);
				int newTimeSum = pi.first + e.cost;
				
				if(distance.get(e.to).timeSum > newTimeSum)
				{	
					distance.get(e.to).set(newTimeSum, currStation);
					queue.add(new PairInt(newTimeSum, e.to));
				}
			}
		}
		
		for(Edge e : stationList.get(departure).connectedStations) //���� �� ȯ�� �ð� �ʱ�ȭ
		{
			if(stationList.get(departure).name.equals(stationList.get(e.to).name))
				e.cost = TRANSFER_COST;
		}
		for(Edge e : stationList.get(arrival).connectedStations) //������ �� ȯ�� �ð� �ʱ�ȭ
		{
			if(stationList.get(arrival).name.equals(stationList.get(e.to).name))
			{
				for(Edge arr : stationList.get(e.to).connectedStations)
				{
					if(arr.to == arrival)
						arr.cost = TRANSFER_COST;
				}
			}		
		}
		
		List<String> track = new ArrayList<String>();
		int minimumSum = distance.get(arrival).timeSum;
		int trackIndex = arrival;
		
		while(trackIndex != -1) // ���������κ��� ������ �����ؼ� ��θ� ��´�.
		{
			track.add(stationList.get(trackIndex).name);
			trackIndex = distance.get(trackIndex).prevIndex;
		}
		
		for(int i = track.size() - 1; i >= 0; i--)
		{
			if(i != track.size() - 1) // ù �� ���ķ� whitespace ���� 
				System.out.print(" ");
			
			if( i > 0 && track.get(i).equals(track.get(i-1)) ) // ȯ��
			{
				if(i == 1 || i == track.size() - 1) // ù ��, ������ ���� ȯ�� �ð� ����
					System.out.print(track.get(i));
				else
					System.out.print("[" + track.get(i) + "]");
				
				i = i - 1;
			}
			
			else
				System.out.print(track.get(i));
		}
		
		System.out.println("\n" + minimumSum);
	}
	
	/** �ּ�ȯ�°�θ� ���Ѵ� **/
	public static void getMinimumTransfer(int departure, int arrival)
	{
		List<Track> distance = new ArrayList<Track>(); // �� vertex�� �̸��� �Ÿ� ����
		PriorityQueue<TrioInt> queue = new PriorityQueue<TrioInt>();
		
		for(int i = 0; i < stationList.size(); i++)
			distance.add(new Track()); // �ʱ� �Ÿ� initialize
	
		queue.add(new TrioInt(0, 0, departure));
		distance.get(departure).set(0, 0, -1); // ���� ���̹Ƿ� previousIndex�� ����(-1)
		
		for(Edge e : stationList.get(departure).connectedStations) //���� �������� ȯ�� �ҿ�ð��� ����.
		{
			if(stationList.get(departure).name.equals(stationList.get(e.to).name))
				e.cost = 0;
		}
		for(Edge e : stationList.get(arrival).connectedStations) // ������ �������� ȯ�� �ҿ�ð��� ����.
		{
			if(stationList.get(arrival).name.equals(stationList.get(e.to).name))
			{
				for(Edge arr : stationList.get(e.to).connectedStations)
				{
					if(arr.to == arrival)
						arr.cost = 0;
				}
			}
		}
		
		while(!queue.isEmpty())
		{
			TrioInt ti = queue.poll();

			int currStation = ti.third;
			
			if(ti.first > distance.get(currStation).transferSum) // �̹� �� ���� ������ ������Ʈ �� ��� �����Ѵ�
				continue;
			
			else if(currStation == arrival) // arrival���� �ִܰ�ΰ� ã���� ��� �������´�
				break;
			
			List<Edge> connectedStations = stationList.get(currStation).connectedStations;
			
			for(int i = 0; i < connectedStations.size(); i++)
			{
				Edge e = connectedStations.get(i);
				int transferCost;
			
				if(stationList.get(currStation).line.equals(stationList.get(e.to).line)) 
					transferCost = 0;
				
				else if(stationList.get(currStation).name.equals(stationList.get(departure).name)
						&& !stationList.get(currStation).line.equals(stationList.get(e.to).line)) // ���ۿ������� ȯ�� ó��
					transferCost = 0;
				
				else if(stationList.get(currStation).name.equals(stationList.get(arrival).name)
						&& !stationList.get(currStation).line.equals(stationList.get(e.to).line)) // ������������ ȯ�� ó��
					transferCost = 0;
				
				else
					transferCost = 1;

				int newTransferSum = ti.first + transferCost;
				int newTimeSum = ti.second + e.cost;
				
				// ȯ�� Ƚ���� �� ���ų�, ȯ�� Ƚ���� ������ �ð��� ���� �ɸ��� ���
				if(distance.get(e.to).transferSum > newTransferSum ||
						(distance.get(e.to).transferSum == newTransferSum && distance.get(e.to).timeSum > newTimeSum))
				{
					distance.get(e.to).set(newTimeSum, newTransferSum, currStation);
					queue.add(new TrioInt(newTransferSum, newTimeSum, e.to));
				}
			}
		}
		
		for(Edge e : stationList.get(departure).connectedStations) //���� �� ȯ�� �ð� �ʱ�ȭ
		{
			if(stationList.get(departure).name.equals(stationList.get(e.to).name))
				e.cost = TRANSFER_COST;
		}
		for(Edge e : stationList.get(arrival).connectedStations) //������ �� ȯ�� �ð� �ʱ�ȭ
		{
			if(stationList.get(arrival).name.equals(stationList.get(e.to).name))
			{
				for(Edge arr : stationList.get(e.to).connectedStations)
				{
					if(arr.to == arrival)
						arr.cost = TRANSFER_COST;
				}
			}		
		}
		
		List<String> track = new ArrayList<String>();
		int minimumSum = distance.get(arrival).timeSum;
		int trackIndex = arrival;
		
		while(trackIndex != -1) // ���������κ��� ������ �����ؼ� ��θ� ��´�.
		{
			track.add(stationList.get(trackIndex).name);
			trackIndex = distance.get(trackIndex).prevIndex;
		}
		
		for(int i = track.size() - 1; i >= 0; i--)
		{
			if(i != track.size() - 1) // ù �� ���ķ� whitespace ���� 
				System.out.print(" ");
			
			if( i > 0 && track.get(i).equals(track.get(i-1)) ) // ȯ��
			{
				if(i == 1 || i == track.size() - 1)
					System.out.print(track.get(i));
	
				else
					System.out.print("[" + track.get(i) + "]");
				
				i = i - 1;
			}
			
			else
				System.out.print(track.get(i));
		}
		
		System.out.println("\n" + minimumSum);		
	}
}