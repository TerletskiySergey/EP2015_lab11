package EPAM2015_lab11.task1;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class EratSieve {

    private class SieveThread extends Thread {

        private int loIndex;
        private int hiIndex;

        public SieveThread(int loIndex, int hiIndex) {
            this.loIndex = loIndex;
            this.hiIndex = hiIndex;
        }

        public void run() {
            int curPrime;
            int curPrimeSqIndex;
            for (int i = loIndex; i <= hiIndex; i++) {
                if (set.get(i)) {
                    curPrime = 2 * i + 3;
                    curPrimeSqIndex = (curPrime * curPrime - 3) / 2;
                    for (int j = curPrimeSqIndex; j < set.length(); j += curPrime) {
                        set.clear(j);
                    }
                }
            }
        }
    }

    private volatile BitSet set;
    private int primeOrdinal;
    private int primesQuant;
    private int cache = 500000;

    private static boolean isInRange(int toCheck) {
        return toCheck >= 2;
    }

    public void setCache(int cache) {
        this.cache = cache;
    }

    public int getCache() {
        return cache;
    }

    public void boltSimple(int border, boolean primesPrintNeeded, boolean performPrintNeeded) {
        if (!isInRange(border)) {
            return;
        }
        long interval = System.currentTimeMillis();
        primeOrdinal = 2;
        primesQuant = 1;
        int setSize = border % 2 == 0 ? border / 2 - 1 : border / 2;
        set = new BitSet();
        set.set(0, setSize);
        if (primesPrintNeeded) {
            System.out.println("1: 2");
        }
        for (int i = 0; i < setSize; i++) {
            if (set.get(i)) {
                int curPrime = 2 * i + 3;
                long j = ((int) Math.pow(curPrime, 2) - 3) / 2;
                for (; j < setSize; j += curPrime) {
                    set.clear((int) j);
                }
            }
        }
        if (primesPrintNeeded) {
            printInterval(set, 0);
        }
        interval = System.currentTimeMillis() - interval;
        if (performPrintNeeded) {
            primesQuant += set.cardinality();
            System.out.print("Primes quantity: " + primesQuant + ".");
            System.out.println(" Performance: " + (interval / 1000) + " sec.");
        }
    }

    public void boltCached(int border, boolean primesPrintNeeded, boolean performPrintNeeded) {
        if (border < 9) {
            boltSimple(border, primesPrintNeeded, performPrintNeeded);
            return;
        }
        long interval = System.currentTimeMillis();
        set = new BitSet();
        boltToRoot(border, primesPrintNeeded);
        int loIndex = set.length();
        int hiIndex;
        int endIndex = border % 2 == 0 ? border / 2 - 2 : border / 2 - 1;
        while (loIndex <= endIndex) {
            hiIndex = Math.min(endIndex, loIndex + this.cache);
            boltInterval(loIndex, hiIndex, primesPrintNeeded);
            loIndex = hiIndex + 1;
        }
        interval = System.currentTimeMillis() - interval;
        if (performPrintNeeded) {
            System.out.print("Primes quantity: " + primesQuant + ".");
            System.out.println(" Performance: " + (interval / 1000) + " sec.");
        }
    }

    private void boltToRoot(int border, boolean primesPrintNeeded) {
        int sqrtSetSize = (int) Math.sqrt(border);
        boltSimple(sqrtSetSize, primesPrintNeeded, false);
    }

    private void printInterval(BitSet block, int loIndex) {
        if (loIndex == 0) {
            System.out.println("1: 2");
        }
        for (int i = 0; i < block.length(); i++) {
            if (block.get(i)) {
                System.out.println(primeOrdinal++ + ": " + (2 * (i + loIndex) + 3));
            }
        }
    }

    private void boltInterval(int loIndex, int hiIndex, boolean primesPrintNeeded) {
        int compositeIndex;
        int modulo;
        int curPrime;
        double sqrtIndex;
        BitSet interval = new BitSet();
        interval.set(0, hiIndex - loIndex + 1);
        for (int i = 0; i < set.length(); i++) {
            if (set.get(i)) {
                curPrime = 2 * i + 3;
                modulo = (2 * hiIndex + 3) % curPrime;
                compositeIndex = modulo % 2 == 0
                        ? hiIndex - modulo / 2 - loIndex
                        : (int) (hiIndex - modulo / 2 - i - 1.5) - loIndex;
                sqrtIndex = (Math.pow(curPrime, 2) - 3) / 2 - loIndex;
                while (compositeIndex >= sqrtIndex
                        && compositeIndex >= 0) {
                    interval.clear(compositeIndex);
                    compositeIndex -= curPrime;
                }
            }
        }
        primesQuant += interval.cardinality();
        if (primesPrintNeeded) {
            printInterval(interval, loIndex);
        }
    }

/*    private List<Thread> destrBetweenThreads(int border, int thrQuant) {
        int sqrtSetSize = set.length();
        int setSize = border % 2 == 0 ? border / 2 - 1 : border / 2;
        thrQuant = setSize - sqrtSetSize < thrQuant
                ? setSize - sqrtSetSize : thrQuant;

        int indexStep = (setSize - sqrtSetSize) / thrQuant;
        int loIndex = sqrtSetSize;
        int hiIndex;
        LinkedList<Thread> toReturn = new LinkedList<>();
        while (loIndex < setSize) {
            hiIndex = Math.min(setSize - 1, loIndex + indexStep);
            toReturn.add(new SieveThread(loIndex, hiIndex, sqrtSetSize));
            loIndex = hiIndex + 1;
        }
        return toReturn;
    }*/

    private List<Thread> destrBetweenThreads(int border, int thrQuant) {
        int sqrtSetPrimesQuant = primesQuant - 1;
        thrQuant = thrQuant > sqrtSetPrimesQuant ? sqrtSetPrimesQuant : thrQuant;
        int primesStep = (int) Math.ceil(1.0 * sqrtSetPrimesQuant / thrQuant);
        int primesCounter = 0;
        int loIndex = 0;
        int hiIndex = -1;
        List<Thread> thrList = new ArrayList<>();
        for (int i = 0; i < set.length(); i++) {
            if (set.get(i)) {
                if (primesCounter++ == 0) {
                    loIndex = i;
                }
                if (primesCounter == primesStep) {
                    hiIndex = i;
                    thrList.add(new SieveThread(loIndex, hiIndex));
                    primesCounter = 0;
                }
            }
        }
        if (loIndex > hiIndex) {
            hiIndex = set.length() - 1;
            thrList.add(new SieveThread(loIndex, hiIndex));
        }
        return thrList;
    }

    public void boltAsync(int border, int thrQuant, boolean primesPrintNeeded, boolean performPrintNeeded) {
        if (border < 9) {
            boltSimple(border, primesPrintNeeded, performPrintNeeded);
            return;
        }
        long interval = System.currentTimeMillis();
        boltToRoot(border, false);
        List<Thread> thrList = destrBetweenThreads(border, thrQuant);
        int setSize = border % 2 == 0 ? border / 2 - 1 : border / 2;
        set.set(set.length(), setSize);
        for (Thread thr : thrList) {
            thr.start();
        }
        for (Thread thr : thrList) {
            try {
                thr.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        if (primesPrintNeeded) {
            printInterval(set, 0);
        }
        interval = System.currentTimeMillis() - interval;
        if (performPrintNeeded) {
            primesQuant = set.cardinality() + 1;
            System.out.print("Primes quantity: " + primesQuant + ".");
            System.out.println(" Performance: " + (interval / 1000) + " sec.");
        }
    }

    public void boltAsync1(int border, int thrQuant, boolean primesPrintNeeded, boolean performPrintNeeded) {
        long interval = System.currentTimeMillis();
        boltToRoot(border, false);
        List<Thread> thrList = destrBetweenThreads(border, thrQuant);
        int setSize = border % 2 == 0 ? border / 2 - 1 : border / 2;
        set.set(set.length(), setSize);
        for (Thread thr : thrList) {
            thr.start();
        }
        for (Thread thr : thrList) {
            try {
                thr.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        interval = System.currentTimeMillis() - interval;
        if (primesPrintNeeded) {
            printInterval(set, 0);
        }
        if (performPrintNeeded) {
            primesQuant = set.cardinality() + 1;
            System.out.print("Primes quantity: " + primesQuant + ".");
            System.out.println(" Performance: " + (interval / 1000) + " sec.");
        }
    }

    public static boolean isPrime(int toCheck) {
        if (toCheck < 2) {
            return false;
        }
        for (int i = 2; i * i <= toCheck; i++) {
            if (toCheck % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        EratSieve sieve = new EratSieve();
        int border = 1;
//        int border = Integer.MAX_VALUE;
        sieve.boltSimple(border, true, true);
//        sieve.boltCached(border, false, true);
//        sieve.boltAsync(border, 3, false, true);
//        sieve.boltAsync1(border, 1, false, true);
//        sieve.boltAsync1(border, 2, false, true);
//        sieve.boltAsync1(border, 3, false, true);
    }
}