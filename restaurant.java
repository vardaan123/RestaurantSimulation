import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class restaurant {
	static int numDiners;
	static int numTables;
	static int numCooks;
	static int numDinersFinished = 0;
	
	static cook[] cooks;
	static Table[] tables;
	static Diner[] diners;
	static boolean[] isValidDiner;
	
	public static final int burgerPrepTime = 5;
	public static final int friesPrepTime = 3;
	public static final int cokePrepTime = 1;
	public static final int maxTime = 120;
	
	static int numAvailableBurgerMachines = 1;
	static int numAvailableFriesMachines = 1;
	static int numAvailableCokeMachines = 1;

	static int numAvailableCooks;
	static boolean isBurgerMachineBusy, isFriesMachineBusy, isCokeMachineBusy;
	
	public static Object burgerMachineLock = new Object();
	public static Object friesMachineLock = new Object();
	public static Object cokeMachineLock = new Object();
	
	static int time = 0;
	static Object timerLock = new Object();
	public static Object dinersLock = new Object();
	public static List<Thread> dinerThreadList = new ArrayList<Thread>();
	
	public void tick()
	{
		try {
			Thread.sleep(60);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// increment time (in minutes).
		time = time + 1;
	}
	
	static public String getTimeFormat()
	{
		// convert time from minutes to hh:mm format
		int hours = time / 60;
		int mins = time % 60;
		return String.format("%02d:%02d", hours, mins);
	}
	
	public boolean validateInput(String[] values, int dinerId)
	{
		// check the validity of diner's arrival time and order
		int arrivalTime = Integer.parseInt(values[0]);
		int numBurgersOrdered = Integer.parseInt(values[1]);
		int numFriesOrdered = Integer.parseInt(values[2]);
		int numCokeOrdered = Integer.parseInt(values[3]);
		
		if(!(arrivalTime>=0 && arrivalTime<=120))
		{
			System.out.println("Invalid input for Diner "+dinerId+": Arrival time should lie in range [0, 120].");
			return false;
		}
		
		if(!(numBurgersOrdered>=1))
		{
			System.out.println("Invalid input for Diner "+dinerId+": No. of burgers ordered < 1.");
			return false;
		}
		
		if(!(numFriesOrdered>=0))
		{
			System.out.println("Invalid input for Diner "+dinerId+": No. of fries ordered < 0.");
			return false;
		}
		
		if(!(numCokeOrdered>=0 && numCokeOrdered<=1))
		{
			System.out.println("Invalid input for Diner "+dinerId+": No. of coke ordered should be 0 or 1.");
			return false;
		}
		
		return true;
	}
	
	public restaurant(String filename)
	{
		isBurgerMachineBusy = false;
		isFriesMachineBusy = false;
		isCokeMachineBusy = false;
		
		String str;
		try
		{
			File file = new File(filename);
			Scanner filescanner = new Scanner(file);
			int i = 0;
			
			while(filescanner.hasNext())
			{
				str = filescanner.nextLine();
				if(i < 3)
				{
					if(i==0)
					{
						//initialize restaurant.diners
						restaurant.numDiners = Integer.parseInt(str);
						restaurant.diners = new Diner[restaurant.numDiners+1];
						restaurant.isValidDiner = new boolean[restaurant.numDiners+1];
					}
					else if(i==1)
					{
						//initialize restaurant.tables
						restaurant.numTables = Integer.parseInt(str);
						restaurant.tables = new Table[restaurant.numTables+1];
						for(int idx=1;idx<=restaurant.numTables;idx++)
							restaurant.tables[idx] = new Table(idx);
					}
					else if(i==2)
					{
						//initialize restaurant.cooks
						restaurant.numCooks = Integer.parseInt(str);
						numAvailableCooks = restaurant.numCooks;
						restaurant.cooks = new cook[restaurant.numCooks+1];
						// assign ID's to cooks
						for(int idx=1;idx<=restaurant.numCooks;idx++)
							restaurant.cooks[idx] = new cook(idx);
					}	
				}
				else
				{
					String[] values = str.split(",");
					//check input correctness
					restaurant.isValidDiner[i-2] = this.validateInput(values, i-2);
					
					// Assign attributes to diners
					restaurant.diners[i-2] = new Diner(i-2, Integer.parseInt(values[0]), Integer.parseInt(values[1]),  Integer.parseInt(values[2]), Integer.parseInt(values[3]));
				}
				i += 1;
			}
			filescanner.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File Not found!!!");
		}
	}
	
	public void operate()
	{
		// start all threads for cooks
		Thread cookThreads[]=new Thread[restaurant.numCooks+1];
        for (int i = 1; i<=restaurant.numCooks; i++) { 
          Thread thread=new Thread(restaurant.cooks[i]); 
          thread.start();
          cookThreads[i]=thread; 
        }
        
		while(numDinersFinished < restaurant.numDiners)
		{
			// serve the diner who has arrived
			for(int i=1;i<=restaurant.numDiners;i++)
			{
				if(restaurant.diners[i].arrivalTime == restaurant.time)
				{
					System.out.println(restaurant.getTimeFormat()+" - Diner "+i+" arrives.");
					if(restaurant.isValidDiner[i])
					{
						Thread thread=new Thread(restaurant.diners[i]);
						// start thread for diner who has arrived
				        thread.start();
				        dinerThreadList.add(thread);
					}
					else
					{
						System.out.println(restaurant.getTimeFormat()+" - Diner "+i+" leaves the restaurant as input is invalid.");
						numDinersFinished += 1;
					}
				}
			}
			
			this.tick();
			// notify all events which are waiting e.g. cook waiting for machines to prepare food, diner threads waiting till eating time is over etc.
			synchronized(timerLock)
			{
				timerLock.notifyAll();
			}
		}
		//decrement time to undo the last increment
		restaurant.time -= 1;
		System.out.println(restaurant.getTimeFormat()+" - The last diner leaves the restaurant.");
	}
	
	public static void main(String[] args)
	{
		System.out.println("Welcome to 6431 restaurant");
		System.out.println("Enter the name of file to read");
		Scanner s = new Scanner(System.in);
		String filename = s.nextLine();
		restaurant r = new restaurant(filename);
		r.operate();
		
		s.close();
	}
}
