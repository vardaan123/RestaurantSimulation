
public class Diner implements Runnable{
	int id, arrivalTime, seatingTime, foodPrepTime, leaveTime, numBurgers, numFries, numCokes;
	boolean isBurgerPrepared, isFriesPrepared, isCokePrepared;
	int assignedCookId, assignedTableId;
	boolean isSeated, isFoodPrepared, isFinished;
	public static final int eatingTime = 30;
	
	
	public Diner(int id, int arrivalTime, int numBurgers, int numFries, int numCokes)
	{
		this.id = id;
		this.arrivalTime = arrivalTime;
		this.numBurgers = numBurgers;
		this.numFries = numFries;
		this.numCokes = numCokes;
		this.isBurgerPrepared = false;
		this.isFriesPrepared = false;
		this.isCokePrepared = false;
		this.isSeated = false;
		this.isFinished = false;
		this.isFoodPrepared = false;
		this.assignedCookId = -1;
		this.assignedTableId = -1;
	}

	@Override
	public void run() {
		// search for available table
		synchronized(restaurant.dinersLock)
		{
			while(!this.isSeated)
			{
				for(int i=1;i<=restaurant.numTables;i++)
				{
					if(restaurant.tables[i].isOccupied == false)
					{
						restaurant.tables[i].isOccupied = true;
						this.assignedTableId = restaurant.tables[i].id;
						restaurant.tables[i].dinerId = this.id;
						this.isSeated = true;
						this.seatingTime = restaurant.time;
						break;
					}
				}
				//wait till seat becomes available
				if(!this.isSeated)
				{
					try {
						restaurant.dinersLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println(restaurant.getTimeFormat()+" - Diner "+this.id+" is seated at table "+this.assignedTableId+".");
			restaurant.dinersLock.notifyAll();
		}

		//wait till cook is assigned
		synchronized(restaurant.tables[this.assignedTableId].tableLock)
		{
			while(!restaurant.tables[this.assignedTableId].isCookAssigned)
			{
				try {
					restaurant.tables[this.assignedTableId].tableLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		// wait for food preparation
		synchronized(restaurant.tables[this.assignedTableId].tableLock)
		{
			while(!this.isFoodPrepared)
			{
				try {
					restaurant.tables[this.assignedTableId].tableLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.foodPrepTime = restaurant.time;
//			System.out.println("Food prepared for Diner "+this.id+" at time "+this.foodPrepTime);
			
			// relieve the assigned cook
			restaurant.cooks[this.assignedCookId].isAllocated = false;
		}
		
		// eat the food
		while(restaurant.time < this.foodPrepTime + eatingTime)
		{
			// wait till the clock ticks again
			synchronized(restaurant.timerLock)
				{
				try {
					restaurant.timerLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		// leave the restaurant and relieve table
		synchronized(restaurant.dinersLock)
		{
			restaurant.tables[this.assignedTableId].relieveTable();
			this.isFinished = true;
			this.leaveTime = this.foodPrepTime + eatingTime;
//			System.out.println("Time:"+restaurant.time+"\t Diner "+this.id+" left the restaurant");
			System.out.println(restaurant.getTimeFormat()+" - Diner "+this.id+" finishes. Diner "+this.id+" leaves the restaurant.");
			restaurant.numDinersFinished += 1;
			restaurant.dinersLock.notifyAll();
		}
	}
}
