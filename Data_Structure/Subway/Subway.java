import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Subway {

	public static Map<String, Integer> stationNumberMap  = new HashMap<String, Integer>(); // 역 번호로 찾기 위한 해쉬맵
	public static Map<String, List<Integer>> stationNameMap = new HashMap<String, List<Integer>>(); // 역 이름으로 찾기 위한 해쉬맵
	
	public static List<Station> stationList = new ArrayList<Station>(); // 그래프 역할을 할 인접리스트
	
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
				
				if(inputs.length == 2) //최단시간경로
				{
					getMinimumTime(stationNameMap.get(inputs[0]).get(0), stationNameMap.get(inputs[1]).get(0));
				}
				else if(inputs.length == 3 && inputs[2].compareTo("!") == 0) //최소환승경로
				{
					getMinimumTransfer(stationNameMap.get(inputs[0]).get(0), stationNameMap.get(inputs[1]).get(0));
				}
				
			} 
			catch (IOException e) {
				System.out.println("Error : " + e.getMessage());
			}
		}
	}
	
	/** 입력받은 노선도를 바탕으로 그래프를 구성한다 **/
	public static void composeSubway(File file) throws IOException
	{
			BufferedReader fr = new BufferedReader(new FileReader(file));

			// 역 정보를 입력받는다.
			while(true)
			{		
				String input;
				
				if((input = fr.readLine()) == null){ // 도중 공백 열 없이 input이 끝난 경우
					fr.close(); 
					throw new IOException();
				}
				
				if(input.isEmpty()) // 역 정보 입력이 끝남
					break;
				
				String[] inputs = input.split(" ");
				
				addStation(inputs[0], inputs[1], inputs[2]);			
			}
			
			// 역간 거리 정보를 입력받는다.
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
		
		// 같은 이름의 역이 있는 경우 - 환승역
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

	
	/** 최단시간경로를 구한다 **/
	public static void getMinimumTime(int departure, int arrival)
	{
		List<Track> distance = new ArrayList<Track>(); // 각 vertex로 이르는 거리 정보
		PriorityQueue<PairInt> queue = new PriorityQueue<PairInt>();
		
		for(int i = 0; i < stationList.size(); i++)
			distance.add(new Track()); // 초기 거리 initialize
	
		queue.add(new PairInt(0, departure));
		distance.get(departure).set(0, -1); // 최초 역이므로 previousIndex가 없다(-1)
		
		for(Edge e : stationList.get(departure).connectedStations) //최초 역에서는 환승 소요시간이 없다.
		{
			if(stationList.get(departure).name.equals(stationList.get(e.to).name))
				e.cost = 0;
		}
		for(Edge e : stationList.get(arrival).connectedStations) // 마지막 역에서도 환승 소요시간이 없다.
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

			if(pi.first > distance.get(currStation).timeSum) // 이미 더 작은 값으로 업데이트 된 경우 무시한다
				continue;
			if(currStation == arrival) // arrival에의 최단경로가 찾아진 경우 빠져나온다
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
		
		for(Edge e : stationList.get(departure).connectedStations) //시작 역 환승 시간 초기화
		{
			if(stationList.get(departure).name.equals(stationList.get(e.to).name))
				e.cost = TRANSFER_COST;
		}
		for(Edge e : stationList.get(arrival).connectedStations) //마지막 역 환승 시간 초기화
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
		
		while(trackIndex != -1) // 도착역으로부터 역으로 추적해서 경로를 얻는다.
		{
			track.add(stationList.get(trackIndex).name);
			trackIndex = distance.get(trackIndex).prevIndex;
		}
		
		for(int i = track.size() - 1; i >= 0; i--)
		{
			if(i != track.size() - 1) // 첫 역 이후로 whitespace 삽입 
				System.out.print(" ");
			
			if( i > 0 && track.get(i).equals(track.get(i-1)) ) // 환승
			{
				if(i == 1 || i == track.size() - 1) // 첫 역, 마지막 역은 환승 시간 없음
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
	
	/** 최소환승경로를 구한다 **/
	public static void getMinimumTransfer(int departure, int arrival)
	{
		List<Track> distance = new ArrayList<Track>(); // 각 vertex로 이르는 거리 정보
		PriorityQueue<TrioInt> queue = new PriorityQueue<TrioInt>();
		
		for(int i = 0; i < stationList.size(); i++)
			distance.add(new Track()); // 초기 거리 initialize
	
		queue.add(new TrioInt(0, 0, departure));
		distance.get(departure).set(0, 0, -1); // 최초 역이므로 previousIndex가 없다(-1)
		
		for(Edge e : stationList.get(departure).connectedStations) //최초 역에서는 환승 소요시간이 없다.
		{
			if(stationList.get(departure).name.equals(stationList.get(e.to).name))
				e.cost = 0;
		}
		for(Edge e : stationList.get(arrival).connectedStations) // 마지막 역에서도 환승 소요시간이 없다.
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
			
			if(ti.first > distance.get(currStation).transferSum) // 이미 더 작은 값으로 업데이트 된 경우 무시한다
				continue;
			
			else if(currStation == arrival) // arrival에의 최단경로가 찾아진 경우 빠져나온다
				break;
			
			List<Edge> connectedStations = stationList.get(currStation).connectedStations;
			
			for(int i = 0; i < connectedStations.size(); i++)
			{
				Edge e = connectedStations.get(i);
				int transferCost;
			
				if(stationList.get(currStation).line.equals(stationList.get(e.to).line)) 
					transferCost = 0;
				
				else if(stationList.get(currStation).name.equals(stationList.get(departure).name)
						&& !stationList.get(currStation).line.equals(stationList.get(e.to).line)) // 시작역에서의 환승 처리
					transferCost = 0;
				
				else if(stationList.get(currStation).name.equals(stationList.get(arrival).name)
						&& !stationList.get(currStation).line.equals(stationList.get(e.to).line)) // 도착역에서의 환승 처리
					transferCost = 0;
				
				else
					transferCost = 1;

				int newTransferSum = ti.first + transferCost;
				int newTimeSum = ti.second + e.cost;
				
				// 환승 횟수가 더 적거나, 환승 횟수는 같으나 시간이 적게 걸리는 경우
				if(distance.get(e.to).transferSum > newTransferSum ||
						(distance.get(e.to).transferSum == newTransferSum && distance.get(e.to).timeSum > newTimeSum))
				{
					distance.get(e.to).set(newTimeSum, newTransferSum, currStation);
					queue.add(new TrioInt(newTransferSum, newTimeSum, e.to));
				}
			}
		}
		
		for(Edge e : stationList.get(departure).connectedStations) //시작 역 환승 시간 초기화
		{
			if(stationList.get(departure).name.equals(stationList.get(e.to).name))
				e.cost = TRANSFER_COST;
		}
		for(Edge e : stationList.get(arrival).connectedStations) //마지막 역 환승 시간 초기화
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
		
		while(trackIndex != -1) // 도착역으로부터 역으로 추적해서 경로를 얻는다.
		{
			track.add(stationList.get(trackIndex).name);
			trackIndex = distance.get(trackIndex).prevIndex;
		}
		
		for(int i = track.size() - 1; i >= 0; i--)
		{
			if(i != track.size() - 1) // 첫 역 이후로 whitespace 삽입 
				System.out.print(" ");
			
			if( i > 0 && track.get(i).equals(track.get(i-1)) ) // 환승
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