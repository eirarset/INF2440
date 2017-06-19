import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/*
 * OPPDATERING for andre forsoek:
 * 
 * Har endret metoden getYtterst til aa sjekke om ytterste punktet ligger på samme linje som hovedpunktene. 
 * I saa fall kontrolleres det at punktet ligger mellom de to hovedpunktene
 * 
 * Den parallelle metoden deler naa bare problemet til fire mindre innhyllinger. 
 * For maskiner med mer enn 4 kjerner er ikke dette optimalt, men det gir bra speedup i forhold til hva jeg hadde.
 */


/*
 * Klasse for aa finne den konvekse innhyllingen av en mengde punkter
 */
class Oblig4 {
	
	int MAX_X, MAX_Y;
	int n, traader = 8;
	int minX, maxX, maxY, min, max, yIndeks;
	int[] x, y;
	IntList koHyll;
	CyclicBarrier barrier2 = new CyclicBarrier(traader+1);
	CyclicBarrier barrier3 = new CyclicBarrier(5);
	
	/*
	 * Main-metode som kjoerer test-metoden
	 */
	public static void main(String[] args){
		test();		
	}
	
	/*
	 * Test-metode som kjoerer testene angitt i oppgaveteksten
	 */
	static void test(){
		Oblig4 t = new Oblig4();
		for(int i = 100; i <= 1000; i*=10){
			System.gc();
			t.n = i;
			double sekTider[] = new double[5];
			double paraTider[] = new double[5];
			for(int j = 0; j< 5; j++){
				sekTider[j] = t.sekvensiell();
			}
			t.tegn("Sekv");
			for(int j = 0; j< 5; j++){
				paraTider[j] = t.parallell();
			}
			t.tegn("Para");
			
			Arrays.sort(sekTider);
			Arrays.sort(paraTider);
			
			double speedup = sekTider[2]/paraTider[2];
			System.out.println("n: " + i + " TidSekv: " + sekTider[2] +"ms TidPara: " + paraTider[2] + "ms Speedup: " + speedup);
			
		}
		
		//Resterende verdier for store aa tegne
		
		for(int i = 10000; i <= 10000000; i*=10){
			System.gc();
			t.n = i;
			double sekTider[] = new double[5];
			double paraTider[] = new double[5];
			for(int j = 0; j< 5; j++){
				sekTider[j] = t.sekvensiell();
			}
			for(int j = 0; j< 5; j++){
				paraTider[j] = t.parallell();
			}
			Arrays.sort(sekTider);
			Arrays.sort(paraTider);
			
			double speedup = sekTider[2]/paraTider[2];
			System.out.println("n: " + i + " TidSekv: " + sekTider[2] +"ms TidPara: " + paraTider[2] + "ms Speedup: " + speedup);
		}
		
		
		
	}
	/*
	 * Metode for aa finne min og max x, og max y, og for å initialisere de noedvendige variablene for tegn()
	 */
	void minMaxValues(){
		int tempMin = -1, tempMax = -1;
		int tempMinX = Integer.MAX_VALUE, tempMaxX = Integer.MIN_VALUE;
		int tempMaxY = Integer.MIN_VALUE;
		
		for(int i = 0; i < n; i++){
			if(x[i] > tempMaxX){
				tempMax = i;
				tempMaxX = x[i];
			}
			if(x[i] < tempMinX){
				tempMinX = x[i];
				tempMin = i;
			}
			if(y[i] > tempMaxY){
				tempMaxY = y[i];
			}
		}
		
		MAX_X = tempMaxX;
		MAX_Y = tempMaxY;
		maxX = tempMaxX;
		minX = tempMinX;
		
		min = tempMin;
		max = tempMax;		
	}
	/*
	 * Sekvensiell metode for aa finne den komvekse innhyllingen. Fungerer som oppgaveteksten foreslaar.
	 */
	double sekvensiell(){
		NPunkter17 punkter = new NPunkter17(n);
		x = new int[n];
		y = new int[n];
		punkter.fyllArrayer(x, y);
		koHyll = new IntList();
		
		long t0 = System.nanoTime();
		
		minMaxValues();
		koHyll.add(max);

		
		IntList alle = new IntList();
		for(int i = 0; i < x.length; i++) {
			alle.add(i);
		}
		
        int ytterst = getYtterst(max, min, alle);
        IntList over = getUtenfor(max, min, alle);
        rekursiv(max, min, ytterst, over);
        koHyll.add(min);

        IntList under = getUtenfor(min, max, alle);
        ytterst = getYtterst(min, max, under);
        
        rekursiv(min, max, ytterst, under);

        long t1 = System.nanoTime();
        return (t1-t0)/1000000.0;
	}
	
	/*
	 * Rekursiv sekvensiell metode for aa finne den konvekse innhyllingen.
	 * a, b og c er punkter der c er dette ytterste punktet til linen mellom a og b.
	 */
	void rekursiv(int a, int b, int c, IntList liste){
		int d = getYtterst(a, c, getUtenfor(a, c, liste));
		if(d > -1){
			rekursiv(a, c, d, getUtenfor(a, c, liste));
		}
		
		koHyll.add(c);
		
		int e = getYtterst(c, b, getUtenfor(c,b,liste));
		if(e > -1){
			rekursiv(c, b, e, getUtenfor(c, b, liste));
		}
	}
	
	/*
	 * Metode som returnerer avstanden fra linjen mellom a og b til punktet c.
	 */
	double getAvstand(int a, int b, int c){
		return ((y[a]-y[b]) * x[c]) + ((x[b]-x[a]) * y[c]) + (y[b] * x[a] - y[a] * x[b]);
	}
	
	/*
	 * Metode som returnerer mengden av punkter i liste som ligger utenfor linjen mellom punktene a og b.
	 */
	IntList getUtenfor(int a, int b, IntList liste){
		 IntList utenfor = new IntList();
		 int temp;
	        for(int i = 0; i < liste.size(); i++){
	        	temp = liste.get(i);
	        	double med = getAvstand(a, b, temp);
	        	if(med <= 0.0 && temp != a && temp != b){
	        		utenfor.add(temp);
	        	}
	        }
		return utenfor;
	}
	/*
	 * Metode for å finne det ytterste punktet fra linjen mellom a og b
	 */
	int getYtterst(int a, int b, IntList liste){
		 int ytterst = -1;
	     double avstand = 1;
	     
	        for(int i = 0; i<liste.size(); i++){
	        	
	        	double tempAvstand = getAvstand(a, b, liste.get(i));
	        	
	        	if(tempAvstand < avstand){
	        		if(tempAvstand == 0 && !mellomYtterpunkter(a, b, liste.get(i))){
	        			continue;
	        		}
	        		avstand = tempAvstand;
	        		ytterst = liste.get(i);
	        	}
	        }
		return ytterst;
	}
	
	/*
	 * Metode som returnerer om punktet c, ligger mellm punktene a og b, gitt at de alle ligger paa samme linje.
	 */
	boolean mellomYtterpunkter(int a, int b,  int c){
		double distanceAB = Math.sqrt( Math.pow((x[b]-x[a]), 2) + Math.pow((y[b]-y[a]), 2) );
		double distanceAC = Math.sqrt( Math.pow((x[c]-x[a]), 2) + Math.pow((y[c]-y[a]), 2) );
		double distanceBC = Math.sqrt( Math.pow((x[c]-x[b]), 2) + Math.pow((y[c]-y[b]), 2) );
		
		if(distanceAB > distanceAC && distanceAB > distanceBC){
			return true;
		}
		return false;
	}
	
	/*
	 * Parallell metode for a finne den kombvekse innhyllingen.
	 * Finner foerst min og max parallellt foer den starter traadene som hver finner sin del av innhyllingen.
	 */
	
	double parallell(){
		NPunkter17 mengde = new NPunkter17(n);
		x = new int[n];
		y = new int[n];
		mengde.fyllArrayer(x, y);
		koHyll = new IntList();

		
		long t0 = System.nanoTime();
		min = -1;
		max = -1;
		minX = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		maxY = Integer.MIN_VALUE;
		

		for(int i = 0; i<traader; i++){
			new Thread(new arbeiderMinMax(i)).start();
		}
		
		try {
			barrier2.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		MAX_X = maxX;
		MAX_Y = maxY;
		
		IntList empty = new IntList();
		for(int i = 0; i<n; i++){
			empty.add(i);
		}
		
		int north = getYtterst(max, min, empty);
		int south = getYtterst(min, max, empty);
		
		arbeiderKoHyll[] subHylls = new arbeiderKoHyll[4];
		
		
		//Starter hver av traadene som finner sin del av innhyllingen
		subHylls[0] = new arbeiderKoHyll(max, north, getUtenfor(max, north, empty));
		new Thread(subHylls[0]).start();
		subHylls[1] = new arbeiderKoHyll(north, min, getUtenfor(north, min, empty));
		new Thread(subHylls[1]).start();
		subHylls[2] = new arbeiderKoHyll(min, south, getUtenfor(min, south, empty));
		new Thread(subHylls[2]).start();
		subHylls[3] = new arbeiderKoHyll(south, max, getUtenfor(south, max, empty));
		new Thread(subHylls[3]).start();
		
		try {
			barrier3.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		
		//Legger til hver av traadenes egne innhyllinger i hoved-innhyllingen
		for(int i = 0; i<subHylls.length; i++){
			for(int j = 0; j < subHylls[i].localHyll.size(); j++){
				koHyll.add(subHylls[i].localHyll.get(j));
			}
		}
			
		long t1 = System.nanoTime();
		return (t1-t0)/1000000.0;
	}
	
	/*
	 * Metode for aa tegne innhyllingen
	 */
	void tegn(String tittel){
		TegnUt tegn = new TegnUt(this, koHyll, tittel);
	}
	
	/*
	 * Synkronisert metode for oppdatering av min og max verider
	 */
	synchronized void minMax(int maxITraad, int minITraad, int yITraad, int iMax, int iMin){
		if(maxITraad > maxX){
			maxX = maxITraad;
			max = iMax;
		}
		if(minITraad < minX){
			minX = minITraad;
			min = iMin;
		}
		if(yITraad > maxY){
			maxY = yITraad;
		}
	}
	
	/*
	 * Metode tilsvarende den sekevnsiell rekursive, men med mulighet for aa definere en overordnet innhylling
	 */
	void paraRekursiv(int a, int b, int c, IntList mengde, IntList hyll){
		int d = getYtterst(a, c, getUtenfor(a, c, mengde));
		if(d > -1){
			paraRekursiv(a, c, d, getUtenfor(a, c, mengde), hyll);
		}
		hyll.add(c);
		
		int e = getYtterst(c, b, getUtenfor(c, b, mengde));
		if(e > -1){
			paraRekursiv(c, b, e, getUtenfor(c, b, mengde), hyll);
		}
	}
	
	/*
	 * Runnable objekt for aa finne min og max parallelt.
	 */
	class arbeiderMinMax implements Runnable{
		
		arbeiderMinMax(int id){
			this.id = id;
		}
		
		int id;
		
		int localMaxX = Integer.MIN_VALUE, localMinX = Integer.MAX_VALUE, localMaxY = Integer.MIN_VALUE;
		int localIndexMaxX = -1, localIndexMinX = -1, localIndexMaxY = -1;

		public void run() {
			maxMin();
			minMax(localMaxX, localMinX, localMaxY, localIndexMaxX, localIndexMinX);
			
			try {
				barrier2.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
		
		void maxMin(){
			for(int i = id; i < x.length; i+= traader){
				if(x[i] > localMaxX){
					localMaxX = x[i];
					localIndexMaxX = i;
				}
				if(x[i] < localMinX){
					localMinX = x[i];
					localIndexMinX = i;
				}
				if(y[i] > localMaxY){
					localMaxY = y[i];
					localIndexMaxY = i;
				}
			}
		}
		
	}
	
	/*
	 * Runnable objekt for aa finne den konvekse innhyllingen rekursivt, ved aa dele seg i to deler.
	 */
	
	class arbeiderKoHyll implements Runnable{
		IntList sub, localHyll;
		int a, b;
		
		arbeiderKoHyll(int a, int b, IntList sub){
			this.a = a;
			this.b = b;
			this.sub = sub;
			localHyll = new IntList();
			
		}
		
		public void run(){
			
			localHyll.add(a);
			int ytterst = getYtterst(a, b, sub);
			paraRekursiv(a, b, ytterst, sub, localHyll);
			
			
			try {
				barrier3.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
	}
}