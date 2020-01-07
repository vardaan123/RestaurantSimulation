
public class Table {
	int id;
	boolean isOccupied;
	boolean isCookAssigned;
	int dinerId;
	int assignedCookId;
	public Object tableLock = new Object();
	
	//reset the attributes of table
	public void relieveTable() {
		this.isOccupied = false;
		this.isCookAssigned = false;
		this.dinerId = -1;
		this.assignedCookId = -1;
	}
	
	public Table(int id)
	{
		this.id = id;
		this.isOccupied = false;
		this.isCookAssigned = false;
		this.dinerId = -1;
		this.assignedCookId = -1;
	}
}
