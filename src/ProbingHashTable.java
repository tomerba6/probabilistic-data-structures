import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

public class ProbingHashTable<K, V> implements HashTable<K, V> {
    final static int DEFAULT_INIT_CAPACITY = 4;
    final static double DEFAULT_MAX_LOAD_FACTOR = 0.75;
    private final Element<K, V> DELETED = new Element<>(null, null);
    final private HashFactory<K> hashFactory;
    final private double maxLoadFactor;
    private int capacity;
    private int k;
    private HashFunctor<K> hashFunc;
    private Element<K,V>[] table;
    private int tableSize;


    /*
     * You should add additional private fields as needed.
     */

    public ProbingHashTable(HashFactory<K> hashFactory, int k, double maxLoadFactor) {
        this.hashFactory = hashFactory;
        this.maxLoadFactor = maxLoadFactor;
        this.capacity = 1 << k;
        this.k = k;
        this.hashFunc = hashFactory.pickHash(k);
        this.table = new Element[capacity];
        this.tableSize = 0;

    }
	
	public ProbingHashTable(HashFactory<K> hashFactory) {
        this(hashFactory, DEFAULT_INIT_CAPACITY, DEFAULT_MAX_LOAD_FACTOR);
    }

    public V search(K key) {
        int index = hashFunc.hash(key);
        for (int i = 0; i < this.capacity; i++) {
            if (table[index] == null) {
                return null;
            }

            if (table[index] != DELETED && table[index].key().equals(key)) {
                return table[index].satelliteData();
            }

            index = (index + 1) % capacity;
        }
        return null;
    }

    public void insert(K key, V value) {
        if ((double) (this.tableSize + 1) / this.capacity >= this.maxLoadFactor) {
            rehashTable();
        }

        int index = this.hashFunc.hash(key);
        while (this.table[index] != null && this.table[index] != DELETED) {
            index = (index + 1) % this.capacity;
        }

        this.table[index] = new Element<>(key, value);
        this.tableSize++;
    }

    private void rehashTable() {
        this.k++;
        int newCapacity = this.capacity << 1;
        Element<K,V>[] newTable = new Element[newCapacity];

        this.hashFunc = hashFactory.pickHash(k);
        for (int i = 0; i < this.capacity; i = i + 1) {
            if (this.table[i] != null && this.table[i] != DELETED) {
                int index = this.hashFunc.hash(this.table[i].key());
                while (newTable[index] != null) {
                    index = (index + 1) % newCapacity;
                }

                newTable[index] = this.table[i];
            }
        }

        this.table = newTable;
        this.capacity = newCapacity;
    }

    public boolean delete(K key) {
        int index = hashFunc.hash(key);
        for (int i = 0; i < this.capacity; i++) {
            if (table[index] == null) {
                return false;
            }

            if (table[index] != DELETED && table[index].key().equals(key)) {
                table[index] = DELETED;
                tableSize--;
                return true;
            }

            index = (index + 1) % capacity;
        }
        return false;
    }

    public HashFunctor<K> getHashFunc() {
        return hashFunc;
    }

    public int capacity() { return capacity; }
}
