
public class ObjectQueue implements ObjectQueueInterface {
    private Object[] items;
    private int front;
    private int rear;
    private int count;
    
    public ObjectQueue() {
        items = new Object[4];
        front = 0;
        rear = -1;
        count = 0;
    }
    
    public boolean isEmpty() {
        return count == 0;
    }
    
    public boolean isFull() {
        return count == items.length;
    }
    
    public void clear() {
        items = new Object[4];
        front = 0;
        rear = -1;
        count = 0;
    }
    
    public void insert(Object o) {
        if (isFull())
            resize(2 * items.length);
        
        rear += 1;
        rear = rear % items.length;
        count++;
        items[rear] = o;
    }
    
    public Object remove() {
        if (isEmpty())
            return null;
            
        Object fItem = items[front++];
        front %= items.length;
        count--;
        
        if (count < items.length / 4)
            resize(items.length / 2);
        return fItem;
    }
    
    public Object query() {
        return items[front];
    }
    
    private void resize(int size) {
        Object[] newQueue = new Object[size];
        
        for (int i = 0; i < count; i++) {
            newQueue[i] = items[front++];
            front %= items.length;
        }
        
        front = 0;
        rear = count - 1;
        items = newQueue;
    }
    
}
