package EPAM2015_lab11.task3;

import java.lang.reflect.Array;
import java.util.LinkedList;

public class MergeSorter<T extends Comparable<T>> {

    private T[] workSpace;
    private T[] toSort;
    private LinkedList<Thread> thrList;

    // For better performance passed value of threads quantity 'thrQuant' is rounded to the nearest value of pow of two
    // by means of 'specifyThrQuant' method.
    public void sortAsync(T[] toSort, int thrQuant, boolean performPrintNeeded) {
        long timer = System.currentTimeMillis();
        init(toSort);
        thrQuant = specifyThrQuant(toSort.length, thrQuant);
        recAsyncSort(0, toSort.length - 1, thrQuant);
        waitForThreads();
        recPostAsyncMerge(0, toSort.length - 1, thrQuant);
        reset();
        timer = System.currentTimeMillis() - timer;
        if (performPrintNeeded) {
            System.out.printf("Performance: %.2f sec.\n", (double) timer / 1000);
        }
    }

    public void sortSimple(T[] toSort, boolean performPrintNeeded) {
        long timer = System.currentTimeMillis();
        init(toSort);
        recSort(0, toSort.length - 1);
        reset();
        timer = System.currentTimeMillis() - timer;
        if (performPrintNeeded) {
            System.out.printf("Performance: %.2f sec.\n", (double) timer / 1000);
        }
    }

    private void waitForThreads() {
        for (Thread thr : thrList) {
            try {
                thr.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void init(T[] toSort) {
        try {
            this.workSpace = (T[]) Array.newInstance(toSort.getClass().getComponentType(), toSort.length);
            this.toSort = toSort;
            this.thrList = new LinkedList<>();
        } catch (OutOfMemoryError er) {
            throw new IllegalArgumentException("Invalid array length");
        }
    }

    private void recAsyncSort(int loBound, int hiBound, int thrQuant) {
        if (thrQuant == 1) {
            thrList.add(new Thread(() -> this.recSort(loBound, hiBound)));
            thrList.getLast().start();
            return;
        }
        int mid = (loBound + hiBound) / 2;
        recAsyncSort(loBound, mid, thrQuant / 2);
        recAsyncSort(mid + 1, hiBound, thrQuant / 2);
    }

    // Implements post-merge of previously sorted by means of multithreading parts of array.
    private void recPostAsyncMerge(int loBound, int hiBound, int thrQuant) {
        if (thrQuant == 1) { //If thrQuant == 1, no post-merge needed
            return;
        }
        int mid = (loBound + hiBound) / 2;
        if (thrQuant == 2) {
            merge(loBound, mid + 1, hiBound);
            return;
        }
        recPostAsyncMerge(loBound, mid, thrQuant / 2);
        recPostAsyncMerge(mid + 1, hiBound, thrQuant / 2);
        merge(loBound, mid + 1, hiBound);
    }

    private void recSort(int loBound, int hiBound) {
        if (loBound == hiBound) {
            return;
        }
        int mid = (loBound + hiBound) / 2;
        recSort(loBound, mid);
        recSort(mid + 1, hiBound);
        merge(loBound, mid + 1, hiBound);
    }

    private void reset() {
        this.workSpace = null;
        this.toSort = null;
        this.thrList = null;
    }

    private void merge(int loIndexAr1, int loIndexAr2, int upperBoundAr2) {
        int loBoundAr1 = loIndexAr1;
        int workSpaceIndex = loIndexAr1;
        int upperBoundAr1 = loIndexAr2 - 1;
        while (loIndexAr1 <= upperBoundAr1 && loIndexAr2 <= upperBoundAr2) {
            if (toSort[loIndexAr1].compareTo(toSort[loIndexAr2]) < 0) {
                workSpace[workSpaceIndex++] = toSort[loIndexAr1++];
            } else {
                workSpace[workSpaceIndex++] = toSort[loIndexAr2++];
            }
        }
        int rest = 0;
        if (loIndexAr1 <= upperBoundAr1) {
            rest = upperBoundAr1 - loIndexAr1 + 1;
            System.arraycopy(toSort, loIndexAr1, workSpace, workSpaceIndex, rest);
        }
        if (loIndexAr2 <= upperBoundAr2) {
            rest = upperBoundAr2 - loIndexAr2 + 1;
            System.arraycopy(toSort, loIndexAr2, workSpace, workSpaceIndex, rest);
        }
        workSpaceIndex += rest;
        System.arraycopy(workSpace, loBoundAr1, toSort, loBoundAr1, workSpaceIndex - loBoundAr1);
    }

    // Specifies and returns quantity of threads based on current array length and passed value of threads quantity.
    // Value of passed to method threads quantity is rounded to the first value of power of two,
    // that doesn't exceed length of current array and is smaller or equals to passed to method value of threads quantity.
    private int specifyThrQuant(int arrayLen, int thrQuant) {
        if (thrQuant < 1) {
            throw new IllegalArgumentException("Invalid thread quantity");
        }
        thrQuant = thrQuant >= arrayLen ? arrayLen : thrQuant;
        if (thrQuant >= 2) {
            for (int mask = 1 << 30; mask > 0; mask >>>= 1) {
                if ((mask & thrQuant) > 0) {
                    return mask;
                }
            }
        }
        return 1;
    }

    // Auxiliary method
    private static Integer[] randomArray(int length, int hiBorder) {
        Integer[] toReturn = new Integer[length];
        for (int i = 0; i < toReturn.length; i++) {
            toReturn[i] = (int) (Math.random() * hiBorder);
        }
        System.out.println("array is ready");
        return toReturn;
    }

    public static void main(String[] args) {
        MergeSorter<Integer> sorter = new MergeSorter<>();
        int border = 10000000;
        Integer[] array = randomArray(border, border);
        Integer[] array1 = array.clone();
//        System.out.println(Arrays.toString(array));
        sorter.sortSimple(array, true);
//        System.out.println(Arrays.toString(array));
        sorter.sortAsync(array1, 2, true);
//        System.out.println(Arrays.toString(array));
    }
}