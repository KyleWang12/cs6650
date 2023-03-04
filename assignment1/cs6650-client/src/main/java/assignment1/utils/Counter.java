package assignment1.utils;

public class Counter {
  private static int count = 0;

  public synchronized void increment() {
    count++;
  }

  public int getCount() {
    return count;
  }
}
