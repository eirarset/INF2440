import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import easyIO.*;
// file: Modell2.java
// Lagt ut 15 feb 2017.Korrigert: Arne Maus, Ifi, UiO
// Som BARE et eksempel, er problemet med å øke fellesvariabelen i  n*antKjerner  ganger løst
// FOUTSETTER bare 1 parallell metode skal utføres.

class Modell2{
	// ****** Problemets FELLES DATA HER
	int i;
	int [] allI;
	final String navn = "TEST AV i++ med synchronized oppdatering som eksempel";

	 // Felles system-variable - samme for 'alle' programmer
	 CyclicBarrier vent,ferdig, heltferdig ; // for at trådene og main venter på hverandre
	 int antTraader;
	 int antKjerner;
	 int numIter ;          // antall ganger for å lage median (1,3,5,,)
	 int nLow,nStep,nHigh;  // laveste, multiplikator, hoyeste n-verdi
	 int n;	                // problemets størrelse
	 String filnavn;
	 volatile boolean stop = false;
	 int med;
	 Out ut;

	 double [] seqTime ;
	 double [] parTime ;

	 /** for også utskrift på fil */
	 synchronized void println(String s) {
		 ut.outln(s);
		 System.out.println(s);
	 }

	 /** for også utskrift på fil */
	 synchronized void print(String s) {
		 ut.out(s);
		 System.out.print(s);
	 }

	 /** initieringen i main-tråden */
	 void intitier(String [] args) {
			nLow = Integer.parseInt(args[0]);
			nStep = Integer.parseInt(args[1]);
			nHigh = Integer.parseInt(args[2]);
			numIter = Integer.parseInt(args[3]);
		    filnavn = args[4];
		    seqTime = new double [numIter];
		    parTime = new double [numIter];
		    ut = new Out(filnavn, true);

			 antKjerner = Runtime.getRuntime().availableProcessors();

			 antTraader = antKjerner;
			 allI = new int [antTraader];
			 vent   = new CyclicBarrier(antTraader+1); //+1, også main
			 ferdig = new CyclicBarrier(antTraader+1); //+1, også main
			 heltferdig = new CyclicBarrier(2);        // for at main skal vente på at tråd 0 er ferdig

             // start trådene
             for (int i = 0; i< antTraader; i++)
		 		 new Thread(new Para(i)).start();
	} // end initier


	public static void main (String [] args) {
		if ( args.length != 5)	{
			System.out.println("use: >java Modell2 <nLow> <nStep> <nHigh> <num iterations> <fil>");
	    } else {
		    new Modell2().utforTest(args);
	    }
	 } // end main


	 void  utforTest (String [] args) {
		intitier(args);
	    println("Test av  "+ navn+ "\n med "+
	              antKjerner + " kjerner , og " + antTraader+" traader,  Median av:" + numIter+" iterasjoner\n");
	    println("\n     n      sekv.tid(ms)   para.tid(ms)    Speedup ");

        for (n = nHigh; n >= nLow; n=n/nStep) {
		    for (med = 0; med < numIter; med++) {
				 long t = System.nanoTime();  // start tidtagning parallell
		         // Start alle trådene parallell beregning nå
				 try {
				   vent.await();   // start de parallelle trådene
				   ferdig.await(); // vent på at trådene er ferdige med beregningene
				 } catch (Exception e) {return;}

	             try { heltferdig.await(); // vent på at  tråd 0 har summert svaret
	             } catch (Exception e) {return;}

				 t = (System.nanoTime()-t);
				 parTime[med] =t/1000000.0;

				 t = System.nanoTime(); // start tidtagning sekvensiell
				 //**** KALL PÅ DIN SEKVENSIELLE METODE  H E R ********
				 sekvensiellMetode (n);
				 t = (System.nanoTime()-t);
				 seqTime[med] =t/1000000.0;

		    } // end for med

		    println(Format.align(n,10)+
		         Format.align(median(seqTime,numIter),12,3)+
			     Format.align(median(parTime,numIter),15,3)+
			     Format.align(median(seqTime,numIter)/median(parTime,numIter),13,4));

	    } // end n-llop

         exit();

	 } // utforTest

	 /** terminate parallel threads*/
	 void exit() {
		stop = true;
		try {  // start the other threads and they terminate
		   vent.await();
		} catch (Exception e) {return;}

        ut.close();
	} // end exit


     /*** HER er din egen sekvensielle metode
          som  selvsagt IKKE ER synchronized, */
	 void sekvensiellMetode (int n){
		 i=0;
		 for (int j=0; j<n; j++){
		 		     i++;
		 }
     } // end sekvensiellMetode

     /*** HER er evt. de av de  parallelle metodene som ER synchronized, langsom */
	 synchronized void addI() {
	 		  i++;
	  }

	 class Para implements Runnable{
		   int ind, fra,til,num;
		   int minI;
		   Para(int in) {
			   ind =in;
			 } // konstruktor


	       /*** HER er dine  parallelle metoder som IKKE er synchronized */
		    void  parallellMetode(int ind) {
		   		 for (int j=fra; j<til; j++){
		   		    minI++;
				}
				allI [ind] = minI;
	        } // end parallellMetode

	        void paraInitier(int n) {
				   num = n/antTraader;
				   fra = ind*num;
				   til = (ind+1)*num;
			       if (ind == antTraader-1) til =n;
			       minI =0;
			}// end paraInitier

	        public void run() { // Her er det som kjores i parallell:

			   while (! stop) {
				      try {  // wait on all other threads + main
					     vent.await();
				      } catch (Exception e) {return;}

	         	      if (! stop) {
					      paraInitier(n);
					      //**** KALL PÅ DINE PARALLELLE METODER  H E R ********
					      parallellMetode(ind); // parameter: traanummeret: ind

					      try{ // make all threads terminate
								ferdig.await();
					      } catch (Exception e) {}

							// tråd nr 0 adderer de 'numThreads' minI - variablene til en felles verdi
							if ( ind == 0) {
								for (int j = 0; j < antTraader; j++) {
									i += allI[j];
								}
								try {  heltferdig.await(); // si fra til main at tråd 0 har summert svaret
								} catch (Exception e) {return;}

							}
					   }// end ! stop thread
			    } // while
		   } // end run
     } // end class Para

	 /** sort double-array a to find median */
	 double median(double a[], int right) {
				int i,k;
				double t;

				for (k = 1 ; k < right; k++) {
					t = a[k] ;
					i = k;

					while ((a[i-1] ) > t ) {
						 a[i] = a[i-1];
						 if (--i == 0) break;
					}
					a[i] = t;
				} // end k
				return (a[a.length/2]);
	 } // end insertSort
}// END class Parallell