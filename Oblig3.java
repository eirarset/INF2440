/* ***************************************************************************
 * RAPPORT
 *
 * Steg C ble parallelisert ved å telle opp for hver tråd hvilken possisjon
 * denne skulle plassere inn sine verdier inn i arrayen. Dette ble gjort ved å
 * akumulere opptellingene for hver verdi per tråd. Da disse var akumulert ble
 * resultatet for den siste tråden sammen med opptellingen for den siste tråden
 * lagt sammen og lagt tilbake i opptellingstråden. Etter dette gikk en tråd
 * gjennom disse summene og akkumulerte opp startpossisjonene for hver verdi.
 * (se linje 563 - 590.)
 *
 *
 * Testing with an array with _100000000_ random numbers and doing it _9_ times 
 * 
 * Seq: 979.419
 * Par: 486.64
 * speedup: 2.012615074798619
 *
 * ============================================================================
 * 
 * Testing with an array with _10000000_ random numbers and doing it _9_ times 
 * 
 * Seq: 97.894
 * Par: 57.06
 * speedup: 1.7156326673676832
 * 
 * ============================================================================
 *
 * Testing with an array with _1000000_ random numbers and doing it _9_ times 
 * 
 * Seq: 9.923
 * Par: 7.972
 * speedup: 1.2447315604616156
 *
 * ============================================================================
 * 
 * Testing with an array with _100000_ random numbers and doing it _9_ times 
 * 
 * Seq: 0.819
 * Par: 2.169
 * speedup: 0.3775933609958506
 *
 * ============================================================================
 * 
 * Testing with an array with _10000_ random numbers and doing it _9_ times 
 * 
 * Seq: 0.1
 * Par: 1.576
 * speedup: 0.06345177664974619
 *
 * ============================================================================
 * 
 * Testing with an array with _1000_ random numbers and doing it _9_ times 
 * 
 * Seq: 0.009
 * Par: 1.684
 * speedup: 0.005344418052256532
 *
 *
 */



/*
 * Oblig3 i INF2440 Universitet i Oslo
 *
 * Radix sort parallelisert.
 *
 *
 */





/*
 * import java.util.FileWriter
 */
import java.util.Random;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;



class Oblig3 {




  /**
   * Global variables.
   */
  boolean debug = false;
  private final int MAX_VALUE;
	final static int q = Runtime.getRuntime().availableProcessors();
  int numBits = 10;
  int numSif;
  static int testCount = 1;
  static int numberCount = 10000;
  Random randomg = new Random(54544);
	CyclicBarrier bwait, bfinish;



	boolean testSort = true;
	


  int [] maxValues;
	static int [] gCount;





  /**
   * Constructor.
   */
  Oblig3() {
     maxValues = new int [q];
     numSif = (int) Math.pow(2.0, (double) numBits);
     MAX_VALUE = numSif;
  } // end constructor




  /**
   * main method. Kicking it all off.
   */
  public static void main (String [] args) {
    if (args.length > 0) numberCount = Integer.parseInt(args[0]);
    if (args.length > 1) testCount = Integer.parseInt(args[1]);
    new Oblig3().runTest(testCount, numberCount);
  }




  /**
   * Run timing tests.
   */
  void runTest(int testCount, int numberCount) {
    double seq, par;
    int m [] = new int [numberCount];
    String s, nl = "\n";
		for (int i = numberCount; i >= 1000; i /= 10) {
			m = new int [i];
			s = "***Test***" + nl;
			s += "Testing with an array with _" + i + "_ random numbers ";
			s += "and doing it _" + testCount + "_ times " + nl;
			pln(s);
			seq = runSeqTest(m, testCount);
			s = "Seq: " + seq;
			pln(s);
			par = runParTest(m, testCount);
			s = "Par: " + par + nl;
			s += "speedup: " + (seq / par) + nl;
			pln(s);
		}
  }




  /**
   * Run radix sort sequential.
   */
  double runSeqTest(int [] n, int testCount) {
    long [] t = new long [testCount];
    long startTime;
    for (int i = 0; i < t.length; i++) {
			n = populate(n.length);
      startTime = System.nanoTime();
      radix2(n);
      t[i] = System.nanoTime() - startTime;
			if (testSort) testSort(n);
    }
    Arrays.sort(t);
    return t[testCount/2]/1000000.0;
  }




  /**
   * Run radix sort in parallel.
   */
  double runParTest(int [] n, int testCount) {
    long [] t = new long [testCount];
    long startTime;
    for (int i = 0; i < t.length; i++) {
			n = populate(n.length);
      startTime = System.nanoTime();
      radix2Par(n);
      t[i] = System.nanoTime() - startTime;
			if (testSort) testSort(n);
    }
    Arrays.sort(t);
    return t[testCount/2]/1000000.0;
  }




	/**
	 * Populate an array with random numbers.
	 */
	int[] populate(int l) {
    int [] ns = new int [l];
    for (int i = ns.length - 1; i >= 0; i--) {
      ns[i] = randomg.nextInt(MAX_VALUE);
    }
    return ns;
  }




	/**
	 * Test if arrays are sorted.
	 */
	void testSort(int [] n) {
		for (int i = 1; i < n.length; i++)
			if (n[i] < n[i - 1]) 
				pln("On line " + i + " there is a value smaller than an earlier value.");
	}
  /**
   * Finds the largest value in the range between {@code startpoint}
   * and {@code endpoint} in an int array {@code n}.
   * @param n
   * @param startpoint
   * @param endpoint
   */
  int findMax(int [] n, int startpoint, int endpoint) {
    if (debug) pln("In array n we got: " + startpoint + " as a startpoint and "
        + endpoint + " as a endpoint.");
    int max = 0;
    for (int i = startpoint; i <= endpoint; i++)
      if (n[i] > max) max = n[i];
    return max;
  }








  /** 
   * Radix sort with two digits.
   */
  static void radix2(int [] a) {



    // 2 digit radixSort: a[]
    int max = a[0], numBit = 2;

    // a) finn max verdi i a[]
    for (int i = 1 ; i < a.length ; i++)
      if (a[i] > max) max = a[i];



		while (max >= (1<<numBit)) numBit++; // antall siffer i max



    // bestem antall bit i siffer1 og siffer2 
    int bit1 = numBit/2,
        bit2 = numBit-bit1;

    int[] b = new int [a.length];


    radixSort(a, b, bit1, 0); // første siffer fra a[] til b[]
    radixSort(b, a, bit2, bit1);// andre siffer, tilbake fra b[] til a[]

  } // end radix2




  /** 
   * Sort a[] on one digit ; number of bits = maskLen, shiftet up ‘shift’ bits.
   */ 
  static void radixSort ( int [] a, int [] b, int maskLen, int shift){
    boolean debug = false;
    int acumVal = 0, j, n = a.length;
    int mask = (1<<maskLen) -1;

    int [] count = new int [mask+1];


		
    for (int i = 0; i < n; i++) {
      count[(a[i]>> shift) & mask]++; }
		
		

    for (int i = 0; i <= mask; i++) {
      j = count[i];
      count[i] = acumVal; acumVal += j;
    }



    for (int i = 0; i < n; i++) {
      b[count[(a[i]>>shift) & mask]++] = a[i]; 
		}

  }// end radixSort




  /** 
   * Parallel version of radix sort with two digits.
   */
  void radix2Par(int [] a) {
    int l;
    int [][] allCount = new int[q][];
		int [][] allAcumCount = new int[q][];
    int [] sumCount;
    int [] b = new int [a.length];
    long starttime = System.nanoTime();
    bwait = new CyclicBarrier(q);
    bfinish = new CyclicBarrier(q + 1);
    Runnable [] t = new Runnable [q];
    l = a.length;
    int max;
    int numBit = 2;



    // a, find maxValue.
    for (int i = 0; i < q; i++) {
      int startpoint = l/q*i;
      int endpoint;
      if (i != q - 1 )
        endpoint = l/q*(i+1)-1;
      else
        endpoint = l-1;
      t[i] = new MaxValueRunner(i, a, startpoint, endpoint);
      new Thread(t[i]).start();
    }


    
    try {
      bfinish.await();
    } catch (Exception e) {return;}

    // end find maxValue.


		//sets maxValues[0] as max
		max = maxValues[0];
		while (max >= (1<<numBit)) numBit++; // antall siffer i max

		
		

    // b, count values

    int bit1 = numBit/2,
        bit2 = numBit-bit1;

    int mask = (1<<bit1) -1;

		sumCount = new int[mask + 1];

    for (int i = 0; i < q; i++) {
      int startpoint = l/q*i;
      int endpoint;
      if (i != q - 1 )
        endpoint = l/q*(i+1)-1;
      else
        endpoint = l-1;
      allCount[i] = new int [mask+1];
			allAcumCount[i] = new int [mask + 1];
      t[i] = new RadixRunner2(i, a, b, allCount, allAcumCount, startpoint, endpoint, bit1, bit2);
      new Thread(t[i]).start();
    }



		try {
			bfinish.await();
		} catch (Exception e) {return;}
  }




  /**
   * This class is a help class to start code in the different threads.
   * It will reuse the threads for each step in the process.
   * @param index         What thread.
   * @param a             What array.
   * @param startpoint    First value which the thread got responsibility for.
   * @param endpoint      Last value which the thread got responsibility for.
   */
  class MaxValueRunner implements Runnable {
    final int index, startpoint, endpoint;
    final int [] a;
    MaxValueRunner(int index, int [] a, int startpoint, int endpoint) {
      this.index = index;
      this.startpoint = startpoint;
      this.endpoint = endpoint;
      this.a = a;
    }
    public void run() {
      pfindMax();
      try {
        bwait.await();
      } catch (Exception e) {return;}
      // sets the maxValues[0] as max.
      if (index == 0)
        mergeMaxValues();
      try {
        bfinish.await();
      } catch (Exception e) {return;}
    }



    void pfindMax() {
      maxValues[index] = findMax(a, startpoint, endpoint);
    }

    
    
    void mergeMaxValues() {
      for (int i = 1; i < maxValues.length; i++)
        if (maxValues[i] > maxValues[0]) maxValues[0] = maxValues[i];
    }
  } // End Class MaxValueRunner




	/**
	 * Help class to start radix sort in parallel.
	 */
	class RadixRunner2 implements Runnable {
		int index, startpoint, endpoint, maskLen, shift, mask, bit1, bit2;
		int a[], b[], count[], allCount[][], allAcumCount[][];
		RadixRunner2(int index, int [] a, int [] b, int [][] allCount, 
				int [][] allAcumCount, int startpoint, int endpoint, 
				int bit1, int bit2) {
			this.index = index;
			this.startpoint = startpoint;
			this.endpoint = endpoint;
			this.maskLen = maskLen;
			this.shift = shift;
			this.a = a;
			this.b = b;
			this.allCount = allCount;
			this.allAcumCount = allAcumCount;
			this.count = allCount[index];
			this.bit1 = bit1;
			this.bit2 = bit2;
		}


		public void run() {
			try {
				// b
				shift = 0;
				mask = (1 << bit1) - 1;
				bwait.await();
				allCount[index] = frequencyCount(startpoint, endpoint, a, count, mask, shift);
				bwait.await();
				// end b


				// C del
				int startpointacu = allCount[0].length/q*index;
				int endpointacu;
				if (index != q-1) 
					endpointacu = (allCount[0].length/q)*(index+1) - 1;
				else
					endpointacu = allCount[0].length - 1;


				acumulatePerValueFreqCount(startpointacu, endpointacu, allCount, allAcumCount);


				bwait.await();



				// C hoveddel, bruker tall fra 
				if (index == 0) accumulateFreqCount(index, allCount);


				bwait.await();


				// D
				moveNumbers(index, startpoint, endpoint, a, b, allCount[0], allAcumCount, shift, mask);


				bwait.await();



				// b igjen
				shift = bit1;
				mask = (1 << bit2) - 1;

				allCount[index] = frequencyCount(startpoint, endpoint, b, count, mask, shift);
				bwait.await();

				// C
				startpointacu = allCount[0].length/q*index;
				if (index != q-1) 
					endpointacu = (allCount[0].length/q)*(index+1) - 1;
				else
					endpointacu = allCount[0].length - 1;
				acumulatePerValueFreqCount(startpointacu, endpointacu, allCount, allAcumCount);
				bwait.await();

				// C hoveddel, bruker tall fra 
				if (index == 0) accumulateFreqCount(index, allCount);



				bwait.await();

				moveNumbers(index, startpoint, endpoint, b, a, allCount[0], allAcumCount, shift, mask);

				bfinish.await();
			} catch (Exception e) {return;} 

		}




		/**
		 * Count the frequency of each digit.
		 * Skritt B
		 */
		int [] frequencyCount(int startpoint, int endpoint, int [] array,
				int [] _localCount, int mask, int shift) {
			int [] localCount = new int [_localCount.length];
			for (int i = startpoint; i <= endpoint; i++) {
				localCount[(array[i]>> shift) & mask]++; }
			return localCount;
		}




		/**
		 * Accumulate sums of the frequency count.
		 * Skritt C.
		 */
		void accumulateFreqCount(int index, int [][] allCount) {
			int n, acumVal = 0;
			for (int i = 0; i < allCount[index].length; i++) {
				n = allCount[q-1][i];
				allCount[index][i] = acumVal; acumVal += n;
			}
		}




		/**
		 * Acumulate per value.
		 * Del av skritt C.
		 */
		void acumulatePerValueFreqCount(int startpoint, int endpoint, 
				int [][] allCount, int [][] allAcumCount) {
			int acumVal, n;
			for (int i = startpoint; i <= endpoint; i++) {
				acumVal = 0;
				for (int j = 0; j < q; j++) {
					allAcumCount[j][i] = acumVal;
					acumVal += allCount[j][i];
				}
				// lagrer verdien i den siste trådens plass i allCount
				allCount[q-1][i] = acumVal;
			}
		}




		/**
		 * Move values from one array to another.
		 * Skritt D
		 */
		void moveNumbers(int index, int startpoint, int endpoint, int [] fromArray, int []
				toArray, int [] count, int [][] allAcumCount, int shift, int mask) {


			int number, offset; 
			for (int i = startpoint; i <= endpoint; i++) {
				number = (fromArray[i]>>shift) & mask;
				offset = allAcumCount[index][number]++;
				toArray[count[number]+offset] = fromArray[i];
			}
		}
	}




  /**
   * Print to screen and/or file.
   */
  synchronized static void pln(int s) {
    p("" + s + "\n");
  }




  /**
   * Print to screen and/or file.
   */
  synchronized static void pln(String s) {
    p(s + "\n");
  }




  /**
   * Print to screen and/or file.
   */
  synchronized static void pln() {
    p("\n");
  }




  /**
   * Print to screen and/or file.
   */
  synchronized static void p(int s) {
    p("" + s);
  }




  /**
   * Print to screen and/or file.
   */
  synchronized static void p(String s) {
    System.out.print(s);
    //if (filewrite) file.
  }





} // End Oblig3 class
//pln(Thread.currentThread().getName() + " running");