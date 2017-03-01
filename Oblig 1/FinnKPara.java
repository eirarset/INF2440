import java.util.Arrays;
import java.util.Random;

/*
FinnKPara er en parallellisering av A2 algoritmen beskrevet i oblig 1 INF2440 v17, som finner de k stoerste elementene in en array.
Den parallelliserte versjonen egner seg klart best ved hoye n (n>1000000).

*/


public class FinnKPara {
	
	int[] a;
	int str;
	int traader;
	int k;
	
	double[] tiderPara;

	/*Konstruktoer som genrerer en array a fylt med tilfeldige genererte tall av et Random objekt, men en brukerbestemt randomSeed*/
	public FinnKPara(int str, int ran, int traader, int k){
		this.str = str;
		a = new int[str];
		
		Random rg = new Random(ran);
		
		for(int i = 0; i<str; i++){
			a[i] = rg.nextInt(str);
		}
		
		tiderPara = new double[7];
		this.traader = traader;
		this.k = k;
		
	}

	/*Main metoden brukes hovedsaklig for testing og illustrering algoritmen.
	Det opprettes 7 like FinnKPara objekter, og sorterer en gang paa dem alle, foer den presenterier mediantiden for algoritmen.*/
	public static void main(String[] args) {
		
		if(args.length != 3){
			System.out.println("Feil antall argumenter. Bruk:\n java FinnKPara <n> <k> <random-seed>");
			System.exit(-1);
		}
		
		int str = Integer.parseInt(args[0]);
		int k = Integer.parseInt(args[1]);
		int randomSeed = Integer.parseInt(args[2]);
		int traader = Runtime.getRuntime().availableProcessors();
		
		System.out.println("Kjerner tilgjengelige: " + traader);
		System.out.println("k: " + k);
		System.out.println("n: " + str +"\n");
		
		double[] tider = new double[7];
		
		FinnKPara fk;
		for(int i= 0; i < 7; i++){
			fk = new FinnKPara(str, randomSeed, traader, k);
			tider[i] = fk.finnK();
			System.out.println("Tid " + i + " A2 Parallell: " + tider[i] + "ms");
		}
		Arrays.sort(tider);
		System.out.println("Median tid A2 parallell: " + tider[3] + "ms");
		
		
	}
	/*finnK er den overordene metoden som styrer algoritmen. Den starter med aa beregne fordelingen av arrayet, til hver sin traad.
	Den starter traadene, som i sin tur returnerer med sine k stoerste elementer paa sine k foerste plasser i arrayet. Deretter fortar den
	sekvensiell  innstikksortering paa hvert omraade for hver traad. Den star da igjen med de k stoerste elementene i arrayet. */
	
	double finnK(){
		long t0 = System.nanoTime();
		Thread[] threads = new Thread[traader];
		
		boolean breaksEven = ((a.length%traader) == 0);
		int[] startIndekser = new int[traader];
		int[] lengder = new int[traader];
		for(int i = 0; i<traader; i++){
			int id = i;
			int indeksStart;
			int lengde;
			
			if(breaksEven){
				lengde = (a.length/traader);
				indeksStart = id*(a.length/traader);
			} else {
				lengde = (id < (traader-1)) ? (a.length/(traader-1)) : (a.length%(traader-1));
				indeksStart = id*(a.length/(traader-1));
			}
			startIndekser[i] = indeksStart;
			lengder[i] = lengde;
			
			Sorterer s = new Sorterer(id, indeksStart, lengde);
			Thread t = new Thread(s);
			t.start();
			threads[i] = t;
			
		}
		
			try {
				for(int i = 0; i<traader; i++){
					threads[i].join();
					}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for(int i = 0; i<traader; i++){
				int temp;
				int j;
				for(int q = startIndekser[i]; q<(startIndekser[i] + k); q++ ){
					temp = a[k-1];
					
					if(a[q] > temp){
						a[k-1] = a[q];
						a[q] = temp;
						
						temp = a[k-1];
						j = k-1;
						while(j > 0 && temp > a[j-1]){
							a[j] = a[j-1];
							j--;
						}
						a[j] = temp;
					}
				}
			}
		
		
		long t1 = System.nanoTime()-t0;
		double tid = t1/1000000.0;
		return tid;
	}

	/* Sorterer en en klasse av Runnable objekter */
	
	
	private class Sorterer implements Runnable {
		
		int id;
		int indeksStart;
		int strBit;
		
		/*Konstruktoer som tar essentsiell data om objektet*/
		public Sorterer(int id, int indeksStart, int strBit){
			this.id = id;
			this.indeksStart = indeksStart;
			this.strBit = strBit;

		}
		
		/*Metode som foretar den initielle sekvensielle innstikk sorteringen av de k foeste elementene i en spesifisert bit a det globale arrayet a.*/
		public void sorterKfoerste(int start, int lengde){
			int temp;
			int j;
			for(int i = start; i < (start+k); i++){
				j = i;
				temp = a[i];
				
				while (j > start && temp > a[j-1]){
					a[j] = a[j-1];
					j--;
				}
				a[j] = temp;
			}
		}

		/*Metode som foretar sorteringen av hele delen av arrayet tilhoerende traaden. Kunne med fordel brukt samme metode som sorterKfoerste, med
		enkle modifikasjoner*/
		public void sorterBit(int start, int lengde){
			int temp;
			int j;
			int kPos = start + k;
			
			for(int i = kPos; i < start + lengde; i++){
				temp = a[kPos-1];
				
				if(a[i] > temp){
					a[kPos-1] = a[i];
					a[i] = temp;
					
					temp = a[kPos-1];
					j = kPos-1;
					while(j>start && temp > a[j-1]){
						a[j] = a[j-1];
						j--;
					}
					a[j] = temp;
				}
			}
		}
		
		/*Metode som foerst setter i gang sorteringen av de k foerste elementene, foer den tar hele biten. Etter dette har kjoert ligger de
		k stoerste elementene i de k foerste plassene i denne traads del av arrayet.*/
		public void run(){

			sorterKfoerste(indeksStart, strBit);
			
			sorterBit(indeksStart, strBit);
		
		}
	}

}
