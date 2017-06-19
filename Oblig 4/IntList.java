//fixed 14.01.2017,20.04.2017 am
class IntList{
	int [] data;
	int len =0;

	IntList(int len) {
		data = new int[Math.max(2,len)];
	}

	IntList() {
		data = new int[16];  // some default size
	}
	void add(int elem) {
		if (len == data.length) {
			int [] b = new int [data.length*2];
			System.arraycopy(data,0, b,0,len);
			//for (int i = 0; i < len; i++) b[i] = data[i];
			data = b;
		}
		data[len++] = elem;
	}// end add
	
	void remove(int index){
		int [] b = new int[data.length-1];
		for(int i = 0; i < index; i++){
			b[i] = data[i];
		}
		for(int i = index; i < b.length; i++){
			b[i] = data[i+1];
		}
		data = b;
		len--;
	}
	
	void print(){
		for(int i = 0; i<len; i++){
			System.out.println("i: "+ i+ "     " + data[i]);
		}
	}

  // adds IntList other to this IntList as last part
  void append (IntList other) {
		if ( len + other.len > data.length) {
			int newLen = Math.max(2*len,len + 2*other.len);
			int [] b = new int [newLen];
			System.arraycopy(data,0,b,0,len);
			data = b;
		}
		System.arraycopy(other.data,0, data, len, other.len);
		len += other.len;
	} // end join other Intlist to this IntList

	void clear(){
		len =0;
	} // end clear;

	int get (int pos){
		if (pos > len-1 ) return -1; else return data [pos];
	}//end get

	int size() {
		return len;
	}//end size
} // end class IntList