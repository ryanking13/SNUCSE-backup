import java.util.ArrayList;
import java.util.List;

/**
 * Element of Hash Table - AVLTree
 * Each Node of AVLTree - AVLNode(Linked List)
 */
class AVLTree
{
	private AVLNode root;
	
	public AVLTree()
	{
		root = null;
	}
	
	public AVLNode getRoot(){ return root; }
	public void setRoot(AVLNode root){ this.root = root; }
	
	public List<AVLItem> getStrings(AVLNode node, List<AVLItem> list) // PreOrder로 Tree를 순회하면서 모든 String을 Concatenate해서 반환한다.
	{	
		if(node == null) return list;
		
		else
		{
			list.add(node.getItem());
			list = getStrings(node.getLeftNode(), list);
			list = getStrings(node.getRightNode(), list);
			
			return list;
		}
	}
	
	public List<AVLItem> getPositions(String str) // 특정 substring의 모든 좌표를 찾아 반환한다
	{
		AVLNode root = getRoot();
		
		while(root != null)
		{	
			if(str.compareTo(root.getItem().getStr()) == 0)
			{
				List<AVLItem> result = root.getAllItems();
				
				return result;
			}
			
			else if(str.compareTo(root.getItem().getStr()) > 0) root = root.getRightNode();
			else root = root.getLeftNode();
		}
		
		return new ArrayList<AVLItem>();
	}
	
	public AVLNode insert(AVLNode root, String str, int row, int startIndex) // string과 좌표를 삽입한다.
	{
		if(root == null) // string이 tree에 존재하지 않을 경우
		{
			root = new AVLNode(new AVLItem(str, row, startIndex));
		}
		
		else 
		{
			if(str.compareTo(root.getItem().getStr()) == 0) // string이 tree에 존재할 경우 삽입한다
			{
				root.insert(new AVLItem(str, row, startIndex));
			}
		
			else if(str.compareTo(root.getItem().getStr()) > 0)
			{
				root.setRightNode(insert(root.getRightNode(), str, row, startIndex));
				root = doBalancing(root);
			}
			else
			{
				root.setLeftNode(insert(root.getLeftNode(), str, row, startIndex));
				root = doBalancing(root);
			}
		}
		
		return root;
	}

	public void remove() {
		// NOT IMPLEMENTED
	} 
	
	public AVLNode doBalancing(AVLNode node) // Balanced Tree로 만든다
	{
		int diff = getDifference(node);
		
		if(diff > 1) // 왼쪽 tree가 깊을 때
		{
			if(getDifference(node.getLeftNode()) > 0)
			{
				node = rotateLL(node);
			}
			else
			{
				node = rotateLR(node);
			}
		}
		
		else if(diff < -1) // 오른쪽 tree가 깊을 때
		{
			if(getDifference(node.getRightNode()) < 0)
			{
				node = rotateRR(node);
			}
			else
			{
				node = rotateRL(node);
			}
		}
		
		return node;
	}
	
	private int getHeight(AVLNode node)  // 해당 노드의 height를 구한다.
	{
		if(node == null)
			return 0;
		
		return 1 + Math.max(getHeight(node.getLeftNode()), getHeight(node.getRightNode()));
			
	}

	private int getDifference(AVLNode node) // 해당 노드의 left-right child간의 height difference를 구한다
	{
		return getHeight(node.getLeftNode()) - getHeight(node.getRightNode());
	}

	private AVLNode rotateLL(AVLNode parentNode)
	{
		AVLNode childNode = parentNode.getLeftNode();
		parentNode.setLeftNode(childNode.getRightNode());
		childNode.setRightNode(parentNode);
		
		return childNode;
	}
	
	private AVLNode rotateRR(AVLNode parentNode)
	{
		AVLNode childNode = parentNode.getRightNode();
		parentNode.setRightNode(childNode.getLeftNode());
		childNode.setLeftNode(parentNode);
		
		return childNode;
	}
	
	private AVLNode rotateRL(AVLNode parentNode)
	{
		AVLNode childNode = parentNode.getRightNode();
		parentNode.setRightNode(rotateLL(childNode));
		
		return rotateRR(parentNode);
	}
	
	private AVLNode rotateLR(AVLNode parentNode)
	{
		AVLNode childNode = parentNode.getLeftNode();
		parentNode.setLeftNode(rotateRR(childNode));
		
		return rotateLL(parentNode);
	}
}

/**
 * Node(Linked list) of AVLTree
 * Each Node of AVLNode - AVLItem
 */
class AVLNode
{
	private AVLItem item = null;
	private AVLNode leftNode = null;
	private AVLNode rightNode = null;
	
	public AVLNode() {}
	
	public AVLNode(AVLItem item)
	{
		setItem(item);
	}
	
	public AVLItem getItem() { return item; }
	public AVLNode getLeftNode() { return leftNode; }
	public AVLNode getRightNode() { return rightNode; }
	
	public void setItem(AVLItem item) { this.item = new AVLItem(item.getStr(), item.getRow(), item.getStartIndex()); }
	public void setLeftNode(AVLNode node) { this.leftNode = node; }
	public void setRightNode(AVLNode node) { this.rightNode = node; }

	public void insert(AVLItem item) // 새 item를 삽입한다
	{
		AVLItem head = getItem();
		
		while(head.hasNext())
			head = head.getNext();
		
		head.setNext(item);
	}
	
	public List<AVLItem> getAllItems() // AVLNode의 모든 item의 List를 반환한다
	{
		List<AVLItem> list = new ArrayList<AVLItem>();
		
		AVLItem head = getItem();
		
		list.add(head);
		
		while(head.hasNext())
		{
			list.add(head.getNext());			
			head = head.getNext();
		}
		
		return list;
	}
}

/**
 * Item(Node) of AVLNode
 */
class AVLItem
{
	private String str;
	private int row;  // index of row number
	private int startIndex;  // start index of string
	
	private AVLItem next;
	
	
	public AVLItem(String str, int row, int startIndex)
	{
		this.next = null;
		this.str = str;
		this.row = row;
		this.startIndex = startIndex;
	}
	
	public AVLItem(int row, int startIndex)
	{
		this.next = null;
		this.str = null;
		this.row = row;
		this.startIndex = startIndex;
	}
	
	public String getStr() { return str; }
	public int getRow() { return row; }
	public int getStartIndex() { return startIndex; }
	public AVLItem getNext() { return next; }
	
	public void setNext(AVLItem item) { this.next = item; }
	
	public boolean hasNext()
	{
		return (this.getNext() != null);
	}
}