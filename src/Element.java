/**
 * This class represents a general element in a general data structure, 
 * with key and satellite data.
 * @param <K> The type of the key.
 * @param <V> The type of the satellite data.
 */
public class Element<K,V> {	
//--------------------fields-------------------------------------------
	private K key;
	private V satelliteData;

//--------------------constructors-------------------------------------
	public Element(K key, V satelliteData) {
		this.key = key;
		this.satelliteData = satelliteData;
	}
	
	public Element(K key) {
		this(key, null);
	}
	
	public Element(Element<K,V> element) {
		this(element.key(), element.satelliteData());
	}

//--------------------methods-------------------------------------
	public K key() {
		return this.key;
	}
	
	public V satelliteData() {
		return this.satelliteData;
	}
	
	public void setKey(K key) {
		this.key = key;
	}
	
	public void setSatData(V satelliteData) {
		this.satelliteData = satelliteData;
	}
	
	public boolean equals(Object other){
		boolean ans = false;        
		if (other instanceof Element<?,?>) {
			Element<?,?> castedOther = (Element<?,?>) other;  
			boolean sameSatData;
			if (this.satelliteData() == null)
				sameSatData = (castedOther.satelliteData() == null);
			else {
				sameSatData = this.satelliteData().equals(castedOther.satelliteData());
			}
            ans = this.key() == castedOther.key() & sameSatData;
        }        
        return ans;
	}
	
	public String toString() {
		return "[" + this.key() + "]";
		//return "[" + this.key() + ", " + this.satelliteData() + "]";
	}
	
}
