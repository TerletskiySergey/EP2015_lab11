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
            int compositeIndex;
            int modulo;
            int maxIndex = rest.length - 1 + sqrtSetSize;
            double sqrtIndex;
            for (int i = loIndex; i <= hiIndex && i != -1; i = sqrtSet.nextSetBit(++i)) {
                curPrime = 2 * i + 3;
                modulo = (2 * maxIndex + 3) % curPrime;
                compositeIndex = modulo % 2 == 0
                        ? maxIndex - modulo / 2 - sqrtSetSize
                        : (int) (maxIndex - modulo / 2.0 - curPrime / 2.0 - sqrtSetSize);
                sqrtIndex = (Math.pow(curPrime, 2) - 3) / 2 - sqrtSetSize;
                while (compositeIndex >= sqrtIndex
                        && compositeIndex >= 0) {
                    rest[compositeIndex] = true;
                    compositeIndex -= curPrime;
                }
            }
        }
    }

    private volatile BitSet sqrtSet;
    private int sqrtSetSize;
    private volatile boolean[] rest;
    private int primeOrdinal;
    private int primesQuant;
    private int cache = 500000;

    public void setCache(int cache) {
        this.cache = cache;
    }

    public int getCache() {
        return cache;
    }

    private static int calcSizeByBorder(int border) {
        return border % 2 == 0 ? border / 2 - 1 : border / 2;
    }

    public void boltSimple(int border, boolean primesPrintNeeded, boolean performPrintNeeded) {
        if (!borderCorrect(border)) {
            if (performPrintNeeded) {
                printPerfomance(0);
            }
            return;
        }
        long timer = System.currentTimeMillis();
        primeOrdinal = 2;
        primesQuant = 1;
        sqrtSetSize = calcSizeByBorder(border);
        sqrtSet = new BitSet(sqrtSetSize);
        sqrtSet.set(0, sqrtSetSize);
        long curPrime;
        long j;
        for (int i = 0; i != -1; i = sqrtSet.nextSetBit(++i)) {
            curPrime = 2 * i + 3;
            j = (curPrime * curPrime - 3) / 2;
            for (; j < sqrtSetSize; j += curPrime) {
                sqrtSet.clear((int) j);
            }
        }
        timer = System.currentTimeMillis() - timer;
        if (primesPrintNeeded) {
            printInterval(sqrtSet, 0);
        }
        if (performPrintNeeded) {
            printPerfomance(timer);
        }
    }

    public void boltCached(int border, boolean primesPrintNeeded, boolean performPrintNeeded) {
        if (border < 9) {
            boltSimple(border, primesPrintNeeded, performPrintNeeded);
            return;
        }
        long timer = System.currentTimeMillis();
        boltToRoot(border, primesPrintNeeded);
        int loIndex = sqrtSetSize;
        int hiIndex;
        int endIndex = calcSizeByBorder(border) - 1;
        while (loIndex <= endIndex) {
            hiIndex = Math.min(endIndex, loIndex + this.cache);
            boltInterval(loIndex, hiIndex, primesPrintNeeded, performPrintNeeded);
            loIndex = hiIndex + 1;
        }
        timer = System.currentTimeMillis() - timer;
        if (performPrintNeeded) {
            printPerfomance(timer);
        }
    }

    public void boltCached(int loBorder, int hiBorder, boolean primesPrintNeeded, boolean performPrintNeeded) {
        if (!borderCorrect(loBorder, hiBorder)) {
            throw new IllegalArgumentException(loBorder + " > " + hiBorder);
        }
        long timer = System.currentTimeMillis();
        boltToRoot(hiBorder, false);
        int loIndex = calcSizeByBorder(loBorder) - 1;
        int hiIndex;
        int endIndex = calcSizeByBorder(hiBorder) - 1;
        while (loIndex <= endIndex) {
            hiIndex = Math.min(endIndex, loIndex + this.cache);
            boltInterval(loIndex, hiIndex, primesPrintNeeded, performPrintNeeded);
            loIndex = hiIndex + 1;
        }
        timer = System.currentTimeMillis() - timer;
        if (performPrintNeeded) {

            printPerfomance(timer);
        }
    }

    private void boltToRoot(int border, boolean primesPrintNeeded) {
        int sqrtSetSize = (int) Math.sqrt(border);
        boltSimple(sqrtSetSize, primesPrintNeeded, false);
    }

    private void printInterval(BitSet interval, int loIndex) {
        if (loIndex == 0) {
            System.out.println("1: 2");
        }
        for (int i = 0; i != -1; i = interval.nextSetBit(++i)) {
            if (interval.get(i)) {
                System.out.println(primeOrdinal++ + ": " + (2 * (i + loIndex) + 3));
            }
        }
    }

    private void printPerfomance(long timer) {
        try {
            primesQuant += sqrtSet.cardinality();
        } catch (NullPointerException ex) {
        }
        System.out.print("Primes quantity: " + primesQuant + ".");
        System.out.printf(" Performance: %.2f sec.\n", (double) timer / 1000);
    }

    private void boltInterval(int loIndex, int hiIndex, boolean primesPrintNeeded, boolean performPrintNeeded) {
        int compositeIndex;
        int modulo;
        int curPrime;
        double sqrtIndex;
        BitSet interval = new BitSet();
        interval.set(0, hiIndex - loIndex + 1);
        for (int i = 0; i != -1; i = sqrtSet.nextSetBit(++i)) {
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
        if (performPrintNeeded) {
            primesQuant += interval.cardinality();
        }
        if (primesPrintNeeded) {
            printInterval(interval, loIndex);
        }
    }

    public void boltAsync(int border, int thrQuant, boolean primesPrintNeeded, boolean performPrintNeeded) {
        if (border < 9) {
            boltSimple(border, primesPrintNeeded, performPrintNeeded);
            return;
        }
        checkArgsForAsyncBolt(border, thrQuant);
        long timer = System.currentTimeMillis();
        boltToRoot(border, primesPrintNeeded);
        List<Thread> threadList = destrBetweenThreads(thrQuant);
        startThreads(threadList);
        waitForThreads(threadList);
        analyseRest(timer, primesPrintNeeded, performPrintNeeded);
        rest = null;
    }

    private void analyseRest(long timer, boolean primesPrintNeeded, boolean performPrintNeeded) {
        int restLength = rest.length;
        int loIndex = sqrtSetSize;
        for (int i = 0; i < restLength; i++) {
            if (!rest[i]) {
                primesQuant++;
                if (primesPrintNeeded) {
                    System.out.println(primeOrdinal++ + ": " + (2 * (i + loIndex) + 3));
                }
            }
        }
        timer = System.currentTimeMillis() - timer;
        if (performPrintNeeded) {
            primesQuant += sqrtSet.cardinality();
            System.out.print("Primes quantity: " + primesQuant + ".");
            System.out.printf(" Performance: %.2f sec.\n", (double) timer / 1000);
        }
    }

    private List<Thread> destrBetweenThreads(int thrQuant) {
        int sqrtSetPrimesQuant = sqrtSet.cardinality();
        thrQuant = thrQuant > sqrtSetPrimesQuant ? sqrtSetPrimesQuant : thrQuant;
        int primesStep = (int) Math.ceil(1.0 * sqrtSetPrimesQuant / thrQuant);
        int primesCounter = 0;
        int loIndex = 0;
        int hiIndex = 0;
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i != -1; i = sqrtSet.nextSetBit(++i)) {
            if (primesCounter++ == 0) {
                loIndex = i;
            }
            if (primesCounter == primesStep) {
                threadList.add(new SieveThread(loIndex, i));
                loIndex = 0;
                primesCounter = 0;
            }
            hiIndex = i;
        }
        if (loIndex != 0) {
            threadList.add(new SieveThread(loIndex, hiIndex));
        }
        return threadList;
    }

    private void checkArgsForAsyncBolt(int border, int thrQuant) {
        try {
            int restSize = calcSizeByBorder(border) - calcSizeByBorder((int) Math.sqrt(border));
            rest = new boolean[restSize];
        } catch (OutOfMemoryError er) {
            throw new IllegalArgumentException("Invalid border value");
        }
        if (thrQuant <= 0) {
            throw new IllegalArgumentException("Invalid thread quantity");
        }
    }

    private static boolean borderCorrect(int border) {
        return border >= 2;
    }

    private static boolean borderCorrect(int loBorder, int hiBorder) {
        return loBorder <= hiBorder;
    }

    private static void startThreads(List<Thread> threadList) {
        for (Thread thr : threadList) {
            thr.start();
        }
    }

    private static void waitForThreads(List<Thread> threadList) {
        for (Thread thr : threadList) {
            try {
                thr.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        EratSieve sieve = new EratSieve();
        int border = 1000000000;
//        int border = Integer.MAX_VALUE;
//        sieve.boltSimple(border, false, true);
        sieve.boltCached(120, true, true);
        sieve.boltCached(113, 113, true, true);
//        sieve.boltAsync(border, 1, false, true);
//        sieve.boltAsync(border, 2, false, true);
    }
}