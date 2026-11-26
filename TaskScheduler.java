import java.util.Scanner;

/**
 * Task Scheduler System (PS No. 79)
 * Concepts: Queue, Priority Queue (Min-Heap), Sorting
 * Priority: 1 (highest) to 5 (lowest). Ties are broken by arrival order (FIFO).
 */
public class TaskScheduler {

    // ---------------------------------------------------------------
    // Task: one unit of work
    // ---------------------------------------------------------------
    static class Task {
        final String id;      // T1, T2, T3 ...
        final String name;
        final int priority;   // 1 (highest) .. 5 (lowest)
        final int order;      // arrival number, used to break ties

        Task(String id, String name, int priority, int order) {
            this.id = id;
            this.name = name;
            this.priority = priority;
            this.order = order;
        }

        // true if this task must run BEFORE the other task
        boolean comesBefore(Task other) {
            if (this.priority != other.priority) {
                return this.priority < other.priority;
            }
            return this.order < other.order;
        }
    }

    // ---------------------------------------------------------------
    // MinHeap: array-based priority queue (no java.util.PriorityQueue)
    // ---------------------------------------------------------------
    static class MinHeap {
        private Task[] data = new Task[4];
        private int size = 0;

        int size()        { return size; }
        boolean isEmpty() { return size == 0; }

        void insert(Task t) {
            if (size == data.length) grow();
            data[size] = t;
            siftUp(size);
            size++;
        }

        Task extractMin() {
            if (size == 0) return null;
            Task min = data[0];
            size--;
            data[0] = data[size];
            data[size] = null;
            if (size > 0) siftDown(0);
            return min;
        }

        // copy of the heap contents (heap itself is not changed)
        Task[] toArray() {
            Task[] copy = new Task[size];
            for (int i = 0; i < size; i++) copy[i] = data[i];
            return copy;
        }

        private void siftUp(int i) {
            while (i > 0) {
                int parent = (i - 1) / 2;
                if (data[i].comesBefore(data[parent])) {
                    swap(i, parent);
                    i = parent;
                } else break;
            }
        }

        private void siftDown(int i) {
            while (true) {
                int left = 2 * i + 1, right = 2 * i + 2, best = i;
                if (left < size && data[left].comesBefore(data[best]))   best = left;
                if (right < size && data[right].comesBefore(data[best])) best = right;
                if (best == i) break;
                swap(i, best);
                i = best;
            }
        }

        private void swap(int a, int b) {
            Task tmp = data[a]; data[a] = data[b]; data[b] = tmp;
        }

        private void grow() {
            Task[] bigger = new Task[data.length * 2];
            for (int i = 0; i < size; i++) bigger[i] = data[i];
            data = bigger;
        }
    }

    // ---------------------------------------------------------------
    // TaskQueue: FIFO queue (linked nodes) for completed tasks
    // ---------------------------------------------------------------
    static class TaskQueue {
        private static class Node {
            Task task; Node next;
            Node(Task t) { task = t; }
        }
        private Node front, rear;
        private int size = 0;

        void enqueue(Task t) {
            Node n = new Node(t);
            if (rear == null) front = rear = n;
            else { rear.next = n; rear = n; }
            size++;
        }

        Task dequeue() {
            if (front == null) return null;
            Task t = front.task;
            front = front.next;
            if (front == null) rear = null;
            size--;
            return t;
        }

        boolean isEmpty() { return size == 0; }

        // prints in execution order without removing anything
        String idsInOrder() {
            StringBuilder sb = new StringBuilder();
            for (Node n = front; n != null; n = n.next) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(n.task.id);
            }
            return sb.toString();
        }
    }

    // ---------------------------------------------------------------
    // Own sorting method: insertion sort (stable) on a copied array
    // ---------------------------------------------------------------
    static void insertionSort(Task[] a) {
        for (int i = 1; i < a.length; i++) {
            Task key = a[i];
            int j = i - 1;
            while (j >= 0 && key.comesBefore(a[j])) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    // ---------------------------------------------------------------
    // Scheduler: ties the pieces together
    // ---------------------------------------------------------------
    static final MinHeap pending = new MinHeap();
    static final TaskQueue completed = new TaskQueue();
    static int counter = 0;   // number of tasks accepted so far

    static void addTask(Scanner sc) {
        System.out.print("Enter task name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Task name cannot be empty.");
            return;
        }
        System.out.print("Enter priority (1-5, 1 = highest): ");
        String text = sc.nextLine().trim();
        int p;
        try {
            p = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            System.out.println("Invalid priority. Enter a value from 1 to 5.");
            return;
        }
        if (p < 1 || p > 5) {
            System.out.println("Invalid priority. Enter a value from 1 to 5.");
            return;
        }
        counter++;
        Task t = new Task("T" + counter, name, p, counter);
        pending.insert(t);
        System.out.println("Task added. ID: " + t.id + " | " + t.name + " | P" + t.priority);
    }

    static void executeNext() {
        if (pending.isEmpty()) {
            System.out.println("No tasks pending.");
            return;
        }
        Task t = pending.extractMin();
        completed.enqueue(t);
        System.out.println("Executing " + t.id + ": " + t.name + " (P" + t.priority + ")");
        System.out.println("Pending tasks: " + pending.size());
    }

    static void viewPending() {
        if (pending.isEmpty()) {
            System.out.println("No tasks pending.");
            return;
        }
        Task[] copy = pending.toArray();   // heap is NOT modified
        insertionSort(copy);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < copy.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(copy[i].id).append(" (P").append(copy[i].priority).append(")");
        }
        System.out.println("Pending: " + sb);
    }

    static void viewCompleted() {
        if (completed.isEmpty()) {
            System.out.println("No completed tasks.");
            return;
        }
        System.out.println("Completed: " + completed.idsInOrder());
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("===== TASK SCHEDULER =====");
            System.out.println("1. Add task");
            System.out.println("2. Execute next");
            System.out.println("3. View pending tasks");
            System.out.println("4. View completed tasks");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            if (!sc.hasNextLine()) break;
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": addTask(sc);      break;
                case "2": executeNext();    break;
                case "3": viewPending();    break;
                case "4": viewCompleted();  break;
                case "5": System.out.println("Goodbye!"); return;
                default:  System.out.println("Invalid option. Enter 1-5.");
            }
        }
    }
}
