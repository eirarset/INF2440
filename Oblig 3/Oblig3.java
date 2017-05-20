import java.util.concurrent.*;
import java.util.*;

/*
 * Oblig3 INF2440, UiO v17
 * Parallell multi høyre radix
 */
public class Oblig3 {
	
	CyclicBarrier vent, ventTraad;
	int[] a;
	int[] b;
	int n;
	int kjerner;
	int max;
	int seed = 777;
	int numIter;
	int[][] allCount;
	int NUM_BITS = 7;
	boolean swap;
	
	public Oblig3(String[] args){
		this.n = Integer.parseInt(args[0]);
		a = new int[n];
		b = new int[n];
		kjerner = Runtime.getRuntime().availableProcessors();
		ventTraad = new CyclicBarrier(kjerner);
		vent = new CyclicBarrier(kjerner + 1);
		allCount = new int[kjerner][];
		numIter = Integer.parseInt(args[1]);
		
	}

	
	public static void main(String[] args){
		if(args.length != 2){
			System.out.println("Feil antall argumenter.\nBruk:\n$ java Oblig3 <n> <antallTester>");
		} else{
			Oblig3 test = new Oblig3(args);
			test.utfoerTester();
		}
		
	}
	
	
	/*
	 *  Metode som utfører numIter sekvensielle og parallelle sorteringer. Arrayey nulstilles mellom hver gang, og arrayet testes med testSort() mellom hver iterasjon.
	 */
	void utfoerTester(){
		System.out.println("n: " + n + ". Antall iterasjoner: " + numIter + ". Antall kjerner: " + kjerner);
		double[] tider = new double[numIter];
		for(int i = 0; i < numIter; i++){
			fyllRandom();
			System.out.println("Utfoerer sekvensiell sortering nr: " + (i+1));
			long t0 = System.nanoTime();
			a = radixMulti(a);
			long t1 = System.nanoTime();
			double tempTid = (t1-t0)/1000000.0;
			tider[i] = tempTid;
			testSort();
		}
		Arrays.sort(tider);
		double seqTid = tider[tider.length/2];
	
		
		for(int i = 0; i < numIter; i++){
			reset();
			System.out.println("Utfoerer parallell sortering nr: " + (i + 1));
			long t0 = System.nanoTime();
			multiRadixPara();
			long t1 = System.nanoTime();
			double tempTid = (t1-t0)/1000000.0;
			tider[i] = tempTid;
			testSort();
		}
		Arrays.sort(tider);
		double paraTid = tider[tider.length/2];
		
		System.out.println("Median tider:\n" + "Sekvensiell multiRadix: " + seqTid + "ms\nParallell multiRadix: " + paraTid + "ms\nSpeedup: " + seqTid/paraTid);
		
		
	}
	
	/*
	 *  Metode som fyller arrayet a med tilfeldige tall 0...n-1.
	 */
	
	void fyllRandom(){
		Random rg = new Random(seed);
		for(int i = 0; i< n; i++){
			a[i] = rg.nextInt(n-1);
		}
	}
	
	/*
	 *  Metode for nullstilling av a, b og allCount.
	 */
	void reset(){
		a = new int[n];
		b = new int[n];
		allCount = new int[kjerner][];
		fyllRandom();
	}
	
	/*
	 *  Metode av Arne Maus for aa teste sorteringen.
	 */
	void testSort(){
		for (int i = 0; i< n-1; i++) {
		   if (a[i] > a[i+1]){
		      System.out.println("SorteringsFEIL på plass: "+i +" a["+i+"]:"+a[i]+" > a["+(i+1)+"]:"+a[i+1]);
		      return;
		  }
	  }
	 }
	
	/*
	 *  Metode som igangsetter den parallelle sorteringen, ved a starta en traad for hver kjerne.
	 */
	void multiRadixPara(){
		max = a[0];
		
		for(int i = 0; i < kjerner; i++){
			new Thread(new Para(i)).start();
		}
		try {
			vent.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		if(swap){
			a = b;
		}
		
	}
	
	
	/*
	 *  Metode for oppdatering av det globale max variable.
	 */
	synchronized void sendMaks(int m){
		if(m > max){
			max = m;
		}
	}
	
	/*
	 *  Metode for aa sette swap variable.
	 */
	synchronized void setSwap(){
		swap = true;
	}
	
	/*
	 *  Klasse for den parallelle sorteringen
	 */
	class Para implements Runnable{
		
		int start, slutt, traadNr;
		int[] lokalTemp, temp;
		
		Para(int traadNr){
			this.traadNr = traadNr;
			start = traadNr * (n/kjerner);
			slutt = (traadNr != kjerner-1) ? start + (n / kjerner) : n;
			

		}
		/*
		 * Metode som foerst finner max i a, foer den avgjoer antall siffer for sorteringen, og deretter setter i gang denne sorteringen
		 */
		public void run(){
			finnMaks();
			int numBit = 2;
			while(max >= (1L << numBit)){
				numBit++;
			}
			
			int siffer = ((numBit/NUM_BITS) < 1) ? 1 : (numBit/NUM_BITS);
			int bit = numBit / siffer;
			int rest = numBit % siffer;
			int[] lengder = new int[siffer];
			
			if(!(siffer%2 == 0) ){
				setSwap();
			}
			
			for(int i = 0; i < siffer; i++){
				lengder[i] = bit;
				if(rest > 0){
					lengder[i]++;
					rest--;
				}
			}
			int[] tilArr = new int[0], fraArr;
			int shift = 0;

			for(int i = 0; i < siffer; i++){
				
				fraArr = (i%2 == 0) ? a : b;
				tilArr = (i%2 == 0) ? b : a;
				int tempLengde = lengder[i];
				sort(fraArr, tilArr, tempLengde, shift);
				shift+= tempLengde;
				try {
					ventTraad.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
			}
			a = tilArr;
			
			try {
				vent.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}	
		}
		
		/*
		 *  Metode for sorteringen, som foretar demn lokale opptellingen av de forksjellige verdiene i a.
		 */
		void sort(int[] a, int[] b, int numSif, int shift){
			int mask = (1<<numSif)-1;
			lokalTemp = new int[mask+1];
			
			
			for(int i = start; i < slutt; i++){
				lokalTemp[(a[i]>>shift & mask)]++;
			}
			
			allCount[traadNr] = lokalTemp;
			try {
				ventTraad.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
			lokalTemp = new int[lokalTemp.length];
			int count = 0;
			for(int sum = 0; sum < mask+1; sum++){
				for(int i = 0; i < traadNr; i++){
					count += allCount[i][sum];
					
				}
				lokalTemp[sum] = count;
				for(int i = traadNr; i < kjerner; i++){
					count += allCount[i][sum];
				}
			}
			
			for(int i = start; i < slutt; i++){
				b[lokalTemp[(a[i]>>shift) & mask]++] = a[i];
			}
		}
		/*
		 *  Metode som finner max i traadens del, og sender den til den globale metoden for oppdatering av max
		 */
		void finnMaks(){
			int minMaks = a[start];
			for(int i = start + 1; i < slutt; i++){
				if(a[i] > minMaks){
					minMaks = a[i];
				}
			}
			try {
				sendMaks(minMaks);
				ventTraad.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Sekvensiell metode av Arne Maus for beregning av speedup.
	 */
	int []  radixMulti(int [] a) {
		  // 1-5 digit radixSort of : a[]
		  int max = a[0], numBit = 2, numDigits, n =a.length;
		  int [] bit ;

		 // a) finn max verdi i a[]
		  for (int i = 1 ; i < n ; i++)
			   if (a[i] > max) max = a[i];
		  while (max >= (1L<<numBit) )numBit++; // antall binaere siffer i max

		  // bestem antall bit i numBits sifre
		  numDigits = Math.max(1, numBit/NUM_BITS);
		  bit = new int[numDigits];
		  int rest = (numBit%numDigits), sum =0;;

		  // fordel bitene vi skal sortere paa jevnt
		  for (int i = 0; i < bit.length; i++){
			  bit[i] = numBit/numDigits;
		      if ( rest-- > 0)  bit[i]++;
		  }

		  int[] t=a, b = new int [n];

		  for (int i =0; i < bit.length; i++) {
			  radixSort( a,b,bit[i],sum );    // i-te siffer fra a[] til b[]
			  sum += bit[i];
			  // swap arrays (pointers only)
			  t = a;
			  a = b;
			  b = t;
		  }
		  if (bit.length%2 != 0 ) {
			  // et odde antall sifre, kopier innhold tilbake til original a[] (nå b)
			  System.arraycopy (a,0,b,0,a.length);
		  }

		  return a;
	 }
	
	/**
	 * Sekvensiell metode av Arne Maus for beregning av speedup.
	 */
	void radixSort ( int [] a, int [] b, int maskLen, int shift){
		  int  acumVal = 0, j, n = a.length;
		  int mask = (1<<maskLen) -1;
		  int [] count = new int [mask+1];

		 // b) count=the frequency of each radix value in a
		  for (int i = 0; i < n; i++) {
			 count[(a[i]>>> shift) & mask]++;
		  }

		 // c) Add up in 'count' - accumulated values
		  for (int i = 0; i <= mask; i++) {
			   j = count[i];
				count[i] = acumVal;
				acumVal += j;
		   }
		 // d) move numbers in sorted order a to b
		  for (int i = 0; i < n; i++) {
			 b[count[(a[i]>>>shift) & mask]++] = a[i];
		  }
	}
}
