package EPAM2015_lab11.task2;

import java.util.LinkedList;
import java.util.List;

public class AsyncSinusSummer {

    private class SinusThread extends Thread {
        private int thrNumber;
        private double result;

        SinusThread(int thrNumber) {
            this.thrNumber = thrNumber;
        }

        public void run() {
            int startArg = -argBorder + thrNumber;
            for (int i = startArg; i < 0; i += thrQuant) {
                result += Math.sin(i) + Math.sin(-i);
            }
        }
    }

    private int argBorder;
    private int thrQuant;

    public void calculate(int argBorder, int thrQuant,
                          boolean resultPrintNeeded, boolean performPrintNeeded) throws InterruptedException {
        long timer = System.currentTimeMillis();
        argBorder = Math.abs(argBorder);
        checkThrQuant(argBorder, thrQuant);
        this.thrQuant = thrQuant;
        this.argBorder = argBorder;
        List<Thread> thrList = destrBetweenThreads();
        startThreads(thrList);
        waitForThreads(thrList);
        double result = gatherResult(thrList);
        timer = System.currentTimeMillis() - timer;
        if (resultPrintNeeded) {
            System.out.printf("Summing result on interval [-%d .. %d] equals: %.3e.",
                    argBorder, argBorder, result);
        }
        if (performPrintNeeded) {
            System.out.printf(" Performance: %.2f sec.\n", (double) timer / 1000);
        }
    }

    private List<Thread> destrBetweenThreads() {
        LinkedList<Thread> toReturn = new LinkedList<>();
        for (int i = 0; i < thrQuant; i++) {
            toReturn.add(new SinusThread(i));
        }
        return toReturn;
    }

    private void startThreads(List<Thread> thrList) {
        for (Thread thr : thrList) {
            thr.start();
        }
    }

    private void waitForThreads(List<Thread> thrList) throws InterruptedException {
        for (Thread thr : thrList) {
            thr.join();
        }
    }

    private double gatherResult(List<Thread> thrList) {
        double sum = 0;
        for (Thread thr : thrList) {
            sum += ((SinusThread) thr).result;
        }
        return sum;
    }

    private static void checkThrQuant(int argBorder, int thrQuant) {
        if (thrQuant <= 0 || thrQuant > argBorder) {
            throw new IllegalArgumentException("Invalid thread quantity");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int argBorder = 2500000;
        AsyncSinusSummer summer = new AsyncSinusSummer();
        summer.calculate(argBorder, 2, false, false); // warm-up
        summer.calculate(argBorder, 1, true, true);
        summer.calculate(argBorder, 2, true, true);
        summer.calculate(argBorder, 3, true, true);
        summer.calculate(argBorder, 10, true, true);
    }
}