import java.util.ArrayList;
import java.util.List;

/** �� ���� **/
public class Station
{
	public String number;
	public String name;
	public String line;
	public List<Edge> connectedStations = new ArrayList<Edge>();

	public Station(String number, String name, String line)
	{
		this.number = number;
		this.name = name;
		this.line = line;
	}
}

/** ���� **/
class Edge
{
	public int to;
	public int cost;
	
	public Edge(int to, int cost)
	{
		this.to = to;
		this.cost = cost;
	}
}

/** Ž�� ������ ������ �����ϴ� Ŭ���� **/
class Track
{
	public int timeSum;
	public int transferSum;
	public int prevIndex; // ���� �������� ��ȣ
	
	public Track()
	{
		this.timeSum = Integer.MAX_VALUE;
		this.transferSum = Integer.MAX_VALUE;
	}
	
	public Track(int time, int index)
	{
		set(time, index);
	}
	
	public Track(int time, int transfer, int index)
	{
		set(time, transfer, index);
	}
	
	public void set(int time, int index)
	{
		this.timeSum = time;
		this.prevIndex = index;
	}
	
	public void set(int time, int transfer, int index)
	{
		this.timeSum = time;
		this.transferSum = transfer;
		this.prevIndex = index;
	}
}

/** �ִܽð���ο� �̿� **/
class PairInt implements Comparable<PairInt>
{
	int first;
	int second;
	
	public PairInt(int first, int second)
	{
		this.first = first;
		this.second = second;
	}
	
	@Override
	public int compareTo(PairInt o)
	{
		if(this.first < o.first) return -1;
		else if(this.first > o.first) return 1;
		else return 0;
	}
}

/** �ּ�ȯ�°�ο� �̿� **/
class TrioInt implements Comparable<TrioInt>
{
	int first;
	int second;
	int third;
	
	public TrioInt(int first, int second, int third)
	{
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	@Override
	public int compareTo(TrioInt o)
	{
		if(this.first < o.first) return -1;
		else if((this.first == o.first) && (this.second < o.second)) return -1;
		else if(this.first > o.first) return 1;
		else if((this.first == o.first) && (this.second > o.second)) return 1;
		else return 0;
	}
}