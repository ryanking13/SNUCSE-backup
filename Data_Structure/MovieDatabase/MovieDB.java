import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Genre, Title 을 관리하는 영화 데이터베이스.
 * 
 * MyLinkedList 를 사용해 각각 Genre와 Title에 따라 내부적으로 정렬된 상태를  
 * 유지하는 데이터베이스이다. 
 */
public class MovieDB {
	
	GenreList list;
	
    public MovieDB() {
    	list = new GenreList();
    }

    public void insert(MovieDBItem item) {
    	
    	int index = list.find(item.getGenre());
    	
    	//if genre exist
    	if(index != -1)
    	{
    		Genre g = list.itemAt(index);
    		if(g == null) System.out.println("null");
    		g.getMovieList().add(item.getTitle());
    	}
    		    	
    	//if genre do not exist
    	else
    	{
	    	Genre newGenre = new Genre(item.getGenre());
	    	newGenre.getMovieList().add(item.getTitle());
	    	list.add(newGenre);
    	}
    	
        //System.err.printf("[trace] MovieDB: INSERT [%s] [%s]\n", item.getGenre(), item.getTitle());
    	
    }

    public void delete(MovieDBItem item) {
    	
    	int index = list.find(item.getGenre());
    	
    	//if genre exist
    	if(index != -1)
    	{
    		Genre g = list.itemAt(index);    		
    		int title_index = g.getMovieList().find(item.getTitle());
    		
    		//if movie exist
    		if(title_index != -1)
    		{
    			g.getMovieList().RemoveAt(title_index);
    		}
    	}
    	
        //System.err.printf("[trace] MovieDB: DELETE [%s] [%s]\n", item.getGenre(), item.getTitle());
    }

    public MyLinkedList<MovieDBItem> search(String term) {

        MyLinkedList<MovieDBItem> results = new MyLinkedList<MovieDBItem>();
        for(Genre g: list)
        {
        	for(String m: g.getMovieList())
        	{
        		if(m.contains(term))
        			results.add(new MovieDBItem(g.getName(), m));
        	}
        }

    	//System.err.printf("[trace] MovieDB: SEARCH [%s]\n", term);


        return results;
    }
    
    public MyLinkedList<MovieDBItem> items() {
    	
        MyLinkedList<MovieDBItem> results = new MyLinkedList<MovieDBItem>();
        
        for(Genre g: list)
        {
        	for(String m: g.getMovieList())
        	{
        		results.add(new MovieDBItem(g.getName(), m));
        	}
        }
        //System.err.printf("[trace] MovieDB: ITEMS\n");     
    	return results;
    }
}

/**
 * genre의 name과 그 genre에 해당하는 MovieList를 가지는 클래스
 */
class Genre implements Comparable<Genre> {
	private String genreName;
	private MovieList movies;
	
	public Genre(String name) {
		genreName = name;
		movies = new MovieList();
	}
	
	public String getName()
	{
		return genreName;
	}
	
	public MovieList getMovieList()
	{
		return movies;
	}
	
	@Override
	public int compareTo(Genre o) {
		if(this.getName().compareTo(o.getName()) > 0) return 1;
		else if(this.getName().compareTo(o.getName()) < 0) return -1;
		else return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((genreName == null) ? 0 : genreName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Genre other = (Genre) obj;
		if (genreName == null) {
			if (other.genreName != null)
				return false;
		} else if (!genreName.equals(other.genreName))
			return false;
		return true;
	}
}
/**
 * Genre의 list를 가지는 클래스
 */
class GenreList extends MyLinkedList<Genre>{

	public GenreList()
	{
		head = new Node<Genre>(null);
	}
	
	//add genre to list in sorted order
	public void add(Genre item)
	{
		MyLinkedListIterator<Genre> i = this.iterator();
		numItems += 1;
		
		while(i.hasNext())
		{
			if(i.next().compareTo(item) > 0)
			{
				i.insertFront(item);
				return;
			}
		}
		
		i.insertNext(item);
	}
	
	//return the index of finding Genre
	public int find(String name)
	{
		int cnt = 1;
		for(Genre node : this)
		{
			if(node.getName().equals(name))
				return cnt; 
		
			cnt++;
		}
		
		return -1;
	}
}

/**
 * Movie의 list를 가지는 클래스
 */
class MovieList extends MyLinkedList<String> {
	
	public MovieList() {
		head = new Node<String>(null);
		numItems = 0;
	}

	//add movie to list in sorted order
	public void add(String item)
	{
		MyLinkedListIterator<String> i = this.iterator();
		numItems += 1;
		
		while(i.hasNext())
		{
			if(i.next().compareTo(item) > 0)
			{
				i.insertFront(item);
				return;
			}
			else if(i.currItem().compareTo(item) == 0)
				return;
		}
		
		i.insertNext(item);
	}
	
	//return the index of finding movie
	public int find(String name)
	{
		int cnt = 1;
		
		for(String s : this)
		{
			if(s.equals(name))
				return cnt; 
		
			cnt++;
		}
		
		return -1;
	}
}