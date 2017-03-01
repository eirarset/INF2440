import java.util.Arrays;
import java.util.Random;

/*FinnKSek er en sekvensiell loesning av A2 algoritmen beskrevet i oblig 1 INF2440 v17, som finner de k stoerste elementene i en array.
*/

public class FinnKSek {
	
	int[] a;
	int str;
	
	double[] tiderA2;
	double[] tiderArraysSort;


	/*Konstruktoer som generer array med en Random generator basert paa brukerdefinert randomSeed.*/
	public FinnKSek(int str, int ran){
		this.str = str;
		a = new int[str];
		
		Random rg = new Random(ran);
		for(int i = 0; i<str ; i++){
			a[i] = rg.nextInt(str);
		}
		tiderA2 = new double[7];
		tiderArraysSort = new double[7];
	}
	/*Main staar for presentasjon av tidene, om sammenligning av de to arrayer*/
	
	public static void main(String[] args) {
		
		if(args.length != 3){
			System.out.println("Feil antall argumenter. Bruk:\njava FinnKSek <n> <k> <random-seed>");
			System.exit(-1);
		}
		
		int n = Integer.parseInt(args[0]);
		int k = Integer.parseInt(args[1]);
		int randomSeed = Integer.parseInt(args[2]);
		
		System.out.println("k: " + k);
		System.out.println("n: " + n + "\n");
		
		FinnKSek fk = new FinnKSek(n, 10);
		
		int[] sortertA2 = fk.finnKStoerste(k, fk.a, 0);
		
		for(int i = 1; i<7; i++){
			sortertA2 = fk.finnKStoerste(k, fk.a, i);
		}
		

		Arrays.sort(fk.tiderA2);
		System.out.println("Median tid A2 Sekvensiell: " + fk.tiderA2[3] + "ms\n");
		
		
		
		int[] sortertArraysSort = fk.sorterArraysSort(fk.a, 0);
		for(int i = 1; i<7; i++){
			sortertArraysSort = fk.sorterArraysSort(fk.a, i);
		}
		Arrays.sort(fk.tiderArraysSort);
		System.out.println("Median tid Arrays.sort(): " + fk.tiderArraysSort[3] + "ms\n");
		
		System.out.println("K foerste sortert A2 sekvensielt (oeverst) og sortert med Arrays.sort() (nederst).");
		for(int i = 0; i<k; i++){
			System.out.print("|" + sortertA2[i] );
		}
		System.out.println("|");
		for(int i = 1; i<=k; i++){
			System.out.print("|" + sortertArraysSort[n-i]);
		}
		System.out.println("|");
		

		
	}

	/*Metode som innstikksorterer de foerste k elementer i et array*/
	
	void sortKFoerste(int k, int[] arr){
	
		int temp;
		int j;
		
		for(int i = 0; i<k; i++){
			
			j = i;
			temp = arr[i];
			
			while(j>0 && temp > arr[j-1]){
				arr[j] = arr[j-1];
				j--;
			}
			arr[j] = temp;
			
		}
	}
	
	/*Metode som foerst igangsetter sorteringen av de k foerste elementer i arrayet, foer den innstikksorterer de resterende ved aa sammenligne
	med det minste elemntet av de k stoerste a[k-1]*/
	int[] finnKStoerste(int k, int a[], int iter){
		
		
		int sortert[] = Arrays.copyOf(a, a.length);
		
		long t0 = System.nanoTime();
		sortKFoerste(k, sortert);
		
		int temp;
		int j;
		
		for(int i = k; i < sortert.length; i++){
			temp = sortert[k-1];
			
			if(sortert[i] > temp){
				sortert[k-1] = sortert[i];
				sortert[i] = temp;
				
				temp = sortert[k-1];
				j = k-1;
				while(j > 0 && temp > sortert[j-1]){
					sortert[j] = sortert[j-1];
					j--;
				}
				sortert[j] = temp;
			}
		}
		long t1 = System.nanoTime() - t0;
		double sortTid = (t1/1000000.0);
		System.out.println("Tid " + iter + " A2: " + sortTid + "ms");
		
		tiderA2[iter] = sortTid;
		return sortert;
	}
	
	/*Metode for java egen Arrays.sort(), med tidtagning..*/
	int[] sorterArraysSort(int[] a, int iter){
		int[] sortert = Arrays.copyOf(a, a.length);
		
		long t0 = System.nanoTime();
		
		Arrays.sort(sortert);
		
		long t1 = System.nanoTime()-t0;
		double sortTid = (t1/1000000.0);
		System.out.println("Tid " + iter + " Arrays.sort(): " + sortTid + "ms");
		
		tiderArraysSort[iter] = sortTid;
		
		return sortert;
	}
}
