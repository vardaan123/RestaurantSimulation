
public class cook implements Runnable{
	boolean isAllocated;
	
	int id;
	int numBurgersOrdered, numFriesOrdered, numCokeOrdered;
	int assignedDinerId;
	int burgerPrepStartTime, firesPrepStartTime, cokePrepStartTime;
	
	
	public cook(int id)
	{
		this.id = id;
		isAllocated = false;
		
		numBurgersOrdered = 0;
		numFriesOrdered = 0;
		numCokeOrdered = 0;
		assignedDinerId = -1;
		burgerPrepStartTime = -1;
		firesPrepStartTime = -1;
		cokePrepStartTime = -1;
	}
	
	public void prepareBurger()
	{
		int startTime = restaurant.time;
		restaurant.isBurgerMachineBusy = true;
		int endTime = startTime + this.numBurgersOrdered * restaurant.burgerPrepTime;
//		this.burgerPrepStartTime = startTime;
		System.out.println(restaurant.getTimeFormat()+" - Cook "+this.id+" uses the burger machine.");
		
		//wait till burger is prepared
		while(restaurant.time < endTime)
		{
			synchronized(restaurant.timerLock)
			{
				try {
					restaurant.timerLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
//		System.out.println("Time:"+restaurant.time+"\t Cook "+this.id+" finished preparing Burger for Diner "+this.assignedDinerId);
		
		synchronized(restaurant.burgerMachineLock)
		{
			// set burger machine as available
			restaurant.isBurgerMachineBusy = false;
			restaurant.diners[this.assignedDinerId].isBurgerPrepared = true;
		}
	}
	
	public void prepareFries()
	{
		int startTime = restaurant.time;
		restaurant.isFriesMachineBusy = true;
		int endTime = startTime + this.numFriesOrdered * restaurant.friesPrepTime;
//		this.firesPrepStartTime = startTime;
		System.out.println(restaurant.getTimeFormat()+" - Cook "+this.id+" uses the fries machine.");
		
		//wait till fries is prepared
		while(restaurant.time < endTime)
		{
			synchronized(restaurant.timerLock)
			{
				try {
					restaurant.timerLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
//		System.out.println("Time:"+restaurant.time+"\t Cook "+this.id+" finished preparing Fries for Diner "+this.assignedDinerId);
		synchronized(restaurant.friesMachineLock)
		{
			// set fries machine as available
			restaurant.isFriesMachineBusy = false;
			restaurant.diners[this.assignedDinerId].isFriesPrepared = true;
		}
	}
	
	public void prepareCoke()
	{
		int startTime = restaurant.time;
		restaurant.isCokeMachineBusy = true;
		int endTime = startTime + this.numCokeOrdered * restaurant.cokePrepTime;
//		this.cokePrepStartTime = startTime;
		System.out.println(restaurant.getTimeFormat()+" - Cook "+this.id+" uses the coke machine.");
		
		//wait till coke is prepared
		while(restaurant.time < endTime)
		{
			synchronized(restaurant.timerLock)
			{
				try {
					restaurant.timerLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
//		System.out.println("Time:"+restaurant.time+"\t Cook "+this.id+" finished preparing Coke for Diner "+this.assignedDinerId);
		synchronized(restaurant.cokeMachineLock)
		{
			// set coke machine as available
			restaurant.isCokeMachineBusy = false;
			restaurant.diners[this.assignedDinerId].isCokePrepared = true;
		}
	}
	
	public Table getTableForCook(int cookId)
	{
		synchronized(restaurant.dinersLock)
		{
			int index = -1;
			for(int i=1;i<=restaurant.numTables;i++)
			{
				if(restaurant.tables[i].isOccupied && !restaurant.tables[i].isCookAssigned)
				{
					index = restaurant.tables[i].id;
					break;
				}
			}
			if(index==-1)
				return null;
			restaurant.tables[index].isCookAssigned = true;
			restaurant.tables[index].assignedCookId = cookId;
//			System.out.println("Time:"+restaurant.time+"\t Diner "+restaurant.tables[index].dinerId +"'s order on Table "+index+" processed by Cook "+cookId);
			System.out.println(restaurant.getTimeFormat()+" - Cook "+cookId+" processes Diner "+restaurant.tables[index].dinerId+"'s order.");
			synchronized(restaurant.tables[index].tableLock)
			{
				restaurant.tables[index].tableLock.notifyAll();
			}
			
			//assign diner details to cook
			restaurant.cooks[cookId].isAllocated = true;
			restaurant.cooks[cookId].assignedDinerId = restaurant.tables[index].dinerId;
			
			//assign cook details to table
			restaurant.diners[restaurant.tables[index].dinerId].assignedCookId = cookId;
			
			// copy order details to cook
			this.numBurgersOrdered = restaurant.diners[restaurant.tables[index].dinerId].numBurgers;
			this.numFriesOrdered = restaurant.diners[restaurant.tables[index].dinerId].numFries;
			this.numCokeOrdered = restaurant.diners[restaurant.tables[index].dinerId].numCokes;
			
			restaurant.dinersLock.notifyAll();
			return restaurant.tables[index];
		}
	}
	
	@Override
	public void run() {
		while(restaurant.numDinersFinished < restaurant.numDiners)
		{
			Table assignedTable = getTableForCook(this.id);

			// get allocated to a diner
			if(assignedTable != null)
			{
				// wait till entire order is prepared
				while(!restaurant.diners[assignedTable.dinerId].isBurgerPrepared || !restaurant.diners[assignedTable.dinerId].isFriesPrepared || !restaurant.diners[assignedTable.dinerId].isCokePrepared)
				{
					//if burger machine is free and burgers not prepared yet, use the burger machine
					if(!restaurant.diners[assignedTable.dinerId].isBurgerPrepared && !restaurant.isBurgerMachineBusy)
					{
						this.prepareBurger();
					}
					
					//if fries machine is free and fries not prepared yet, use the fries machine
					if(!restaurant.diners[assignedTable.dinerId].isFriesPrepared && !restaurant.isFriesMachineBusy)
					{
						this.prepareFries();
					}
					
					//if coke machine is free and coke not prepared yet, use the coke machine
					if(!restaurant.diners[assignedTable.dinerId].isCokePrepared && !restaurant.isCokeMachineBusy)
					{
						this.prepareCoke();
					}
				}			
//				System.out.println("Time:"+restaurant.time+"\t Cook "+this.id+" served order to Diner "+this.assignedDinerId);
				System.out.println(restaurant.getTimeFormat()+" - Diner "+ this.assignedDinerId+"'s order is ready. Diner "+this.assignedDinerId+" starts eating.");
				
		        restaurant.diners[this.assignedDinerId].isFoodPrepared = true;
		        
		        //notify diners who are waiting at the table to check if food is prepared/served
		        synchronized(assignedTable.tableLock)
				{
		        	assignedTable.tableLock.notifyAll();
				}
			}
		}
	}
}

