import java.util.*;
import java.util.concurrent.*;

/**
  * ParaRadix.sort sorts integer array a in parallel with the Right Radix (ordinary Radix) method.
  * use: ParaRadix.sort(a);
  * Applies faster sub-algoritms: insertSort (a.length <100) or sequential Radix (a.length < 150000)
  * Approximatly 5-10 times as fast as Arrays.sort, when n > 200 000 .
  * Developed by and copyleft: Arne Maus, Dept of Informatics, Univ. of Oslo.
  * Freely avavailable under the Berkley licence if this comment is retained wherever you use it.
  ************************************************************************************************/
class ParaRadix{
	static CyclicBarrier wait,finished, sync ;
	static int [] a, b;
	static int numThreads;
    static int[]localMax ;
    static int allCount[] [];

	public static void main (String [] args) {
		   //  If you want to test this, run: > java ParaRadix <n>
	         a = new int[new Integer(args[0]).intValue()];
	         Random r = new Random(123);
        	 for (int i =0; i< a.length;i++){ a[i] = r.nextInt(a.length);} // random fill >=0}
		     double t = System.nanoTime();
		     new ParaRadix().init(a);
		     System.out.println("Sorted "+a.length+" integers in "+((System.nanoTime()-t)/1000000.0)+" millisec.");
		     for (int i = 1;i<a.length;i++)
		       if (a[i-1] > a[i] ){ System.out.println("Error: a["+(i-1)+"] = "+a[i-1]+ "> a["+i+"] = "+a[i]);}
	 } // end main

     static void sort (int [] a) {
			// initiate
			new ParaRadix().init(a);
	 } // end sort

	 void init (int [] a) {
		 if (a.length < 100 ) {insertSort(a);
	   } else if (a.length < 150000){ SeqRadix.radix2(a) ;
	   } else{
			numThreads = Runtime.getRuntime().availableProcessors();
			wait   =   new CyclicBarrier(numThreads+1); //+1, also main
			finished = new CyclicBarrier(numThreads+1); //+1, also main
			sync =     new CyclicBarrier(numThreads);
			localMax = new int [numThreads];
			allCount = new int[numThreads][];
			this.a =a;
			b = new int [a.length];

			 // start threads
			 for (int i = 0; i< numThreads; i++)
				 (new Thread(new Para(i))).start();

			 try{  // do parallell
					 wait.await();
					 finished.await();
			}catch(Exception e) {}
       	 } // end else
	 }


	 class Para implements Runnable{
		   int ind;
		   Para(int i) { ind =i;} // constructor
		   int [] localCount;
		   int myMax ;
		   int left, right;
		   int bit1, bit2;

		   void findLocalMax(){
		   		  int num= (a.length/numThreads);   // antall elementer per traad
		   		  left = num*ind;
		   		   myMax = a[left];
		   		  right= left +num;
		   		  if (ind == numThreads -1) right = a.length;

		   		  for (int i = left+1; i < right; i++) {
		   		 	  if (a[i] > myMax) myMax = a[i];
		   		  }
		   		  localMax[ind] =myMax;
	       } // end FindLocalMax

	        void paraRadix(int [] a, int [] b) {
		         // 2 digit radixSort: a[]
		         int  numBit = 2, n =a.length;
		         while (myMax >= (1<<numBit) )numBit++; // antall siffer i max

		         // bestem antall bit i siffer1 og siffer2
		         bit1 = numBit/2;
		         bit2 = numBit-bit1;

		         paraRadixSort( a,b, bit1, 0);    // first digit sort a[] to b[]

		         try{ // start all treads  ------------- Sync 3 --------------------
				 			sync.await();
				 } catch (Exception e) {return;}

		         paraRadixSort( b,a, bit2, bit1);// second digit sort b[] to a[]
		   } // end

		    void paraRadixSort ( int [] a, int [] b, int maskLen, int shift){
		         int  acumVal = 0,t, n = a.length;
		         int mask = (1<<maskLen) -1;
		         localCount = new int [mask+1];
		         int num, sumC =0;

		        // b) count=the frequency of each radix value in a
		         for (int i = left; i < right; i++) {
		            localCount[(a[i]>> shift) & mask]++;
		         }
		         allCount[ind] = localCount;  // for all threads to read

			     try{ // start all threads ---------------- Sync 2 and 4 -------------------
						sync.await();
				 } catch (Exception e) {return;}

			     localCount = new int[localCount.length];

		        // c) Add up in 'count' - accumulated values in NEW localCount
		        for (int val = 0; val < mask+1; val++) {
					 for (int i = 0; i < ind; i++) {
						   sumC += allCount[i][val];
					  }
					  localCount [val] = sumC;
					  for (int i = ind; i < numThreads; i++) {
					  	   sumC += allCount[i][val];
					  }
			    }

			    // no need to sync between c) and d) - only local variables
		        // d) move numbers in sorted order a to b
		         for (int i = left; i < right; i++) {
		            b[localCount[(a[i]>>shift) & mask]++] = a[i];
		         }
           }// end paraRadixSort


	       public void run() {
				     try {  // wait on all other threads + main
					      wait.await();
	         	     } catch (Exception e) {return;}

                      // phase A - find max
					   findLocalMax();
					   try{ // start all treads  ----------- Sync 1 ----------------
							sync.await();
					   } catch (Exception e) {return;}

					   // all threads calculate the same max
					   for (int i = 0; i < numThreads; i++)
					   if (myMax < localMax[i]) myMax = localMax[i];

                       paraRadix(a,b);

					   try {  // wait on all other threads + main   --  Sync 5 -----------
						   finished.await();
					   } catch (Exception e) {return;}
			} // end run

     } // end class Para

	 static void  insertSort(int []a ) {
				int i,k;
				int t;

				for (k = 1 ; k < a.length; k++) {
					t = a[k] ;
					i = k;

					while ((a[i-1] ) > t ) {
						 a[i] = a[i-1];
						 if (--i == 0) break;
					}
					a[i] = t;
				} // end k
	 } // end insertSort


//	class SeqRadix{
   void radix2(int [] a) {
		// 2 digit radixSort: a[]
		int max = a[0], numBit = 2, n =a.length;
		// a) find max value in a[]
		for (int i = 1 ; i < n ; i++)
		if (a[i] > max) max = a[i];

		while (max >= (1<<numBit) )numBit++; // number of bits in max

		// determine number o
		int bit1 = numBit/2,
			bit2 = numBit-bit1;
		int[] b = new int [n];
		radixSort( a,b, bit1, 0);    // første siffer fra a[] til b[]
		radixSort( b,a, bit2, bit1);// andre siffer, tilbake fra b[] til a[]
	} // end radix2


	/** Sort a[] on one digit ; number of bits = maskLen, shiftet up shift bits */
	void radixSort ( int [] a, int [] b, int maskLen, int shift){
	  int  acumVal = 0, j, n = a.length;
	  int mask = (1<<maskLen) -1;
	  int [] count = new int [mask+1];

	 // b) count=the frequency of each radix value in a
	  for (int i = 0; i < n; i++) {
		 count[(a[i]>> shift) & mask]++;
	  }

	 // c) Add up in 'count' - accumulated values
	  for (int i = 0; i <= mask; i++) {
		   j = count[i];
			count[i] = acumVal;
			acumVal += j;
	   }
	 // c) move numbers in sorted order a to b
	  for (int i = 0; i < n; i++) {
		 b[count[(a[i]>>shift) & mask]++] = a[i];
	  }
  }// end radixSort
  //}// end class SeqRadix

}// END class ParaRadix
