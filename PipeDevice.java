import java.util.*;

public class PipeDevice implements Device {

    HashMap<String, byte[]> pipeBuffer = new HashMap<>(); //hashmap data structure to contain our pipes
    byte[] currentPipe = new byte[100]; //a reference to the current working pipe
    String currentPipeString = ""; //a reference to the current working pipe's string
    ArrayList<String> listOfPipeNames = new ArrayList<>(); //an array to keep track of names and indexes after deletion

    @Override
    public int Open(String s){
        int i = 0;

        if(!pipeBuffer.containsKey(s)){ //if we DON'T already have a pipe under the desired name...
            byte[] dummyBuffer = new byte[10]; //create an empty buffer of bytes
            this.pipeBuffer.put(s, dummyBuffer); //put our pipe name and empty buffer in our data structure
            this.currentPipe = this.pipeBuffer.get(s); //set our current pipe values + increment
            this.currentPipeString = s;
            listOfPipeNames.add(this.currentPipeString);
            i = this.pipeBuffer.size() - 1;
        }else{
            this.currentPipe = this.pipeBuffer.get(s);
            this.currentPipeString = s;

            for (Map.Entry<String, byte[]> set : this.pipeBuffer.entrySet()) { //iterate our entire pipe structure
                //loop until we get a matching pipe in our hashmap
                if(Objects.equals(set.getKey(), s)) {
                    break;
                }else{
                    i++;
                }
            }
        }
        return i; //return the placement of the pipe in the hashmap
    }

    @Override
    public void Close(int id) {
        if(this.pipeBuffer.size() != 0) {
            int i = 0;
            for (Map.Entry<String, byte[]> set : this.pipeBuffer.entrySet()) { //loop our entire pipe hashmap
                if(Objects.equals(this.listOfPipeNames.get(id), set.getKey())){ //when we find the correct key
                    this.pipeBuffer.remove(this.listOfPipeNames.get(id), set.getValue()); //remove it from the hashmap
                    this.listOfPipeNames.remove(id); //remove it from our name array
                    this.listOfPipeNames.add(id, null); //add a null element where our entry would be so
                                                                //that we can correctly access all keys after deletion
                    break;
                }
                i++;
            }
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        byte[] readBytes = new byte[size]; //create byte array with the size we pass in

        int i = 0;
        for (Map.Entry<String, byte[]> set : this.pipeBuffer.entrySet()) { //loop our entire pipe hashmap
            if(Objects.equals(this.listOfPipeNames.get(id), set.getKey())){ //when we find the correct key
                this.currentPipeString = set.getKey();
                this.currentPipe = set.getValue();
                break;
            }
            i++;
        }

        for(int j = 0; j < size; j++){
            if(j < this.currentPipe.length && j < readBytes.length) {
                readBytes[j] = this.currentPipe[j]; //insert bytes into the new byte array
            }
        }

        return readBytes;
    }

    @Override
    public void Seek(int id, int to) {
        int i = 0;
        for (Map.Entry<String, byte[]> set : this.pipeBuffer.entrySet()) { //loop our entire pipe hashmap
            if(Objects.equals(this.listOfPipeNames.get(id), set.getKey())){ //when we find the correct key
                this.currentPipeString = set.getKey();
                this.currentPipe = set.getValue();
                break;
            }
            i++;
        }

        byte[] readBytes = new byte[this.currentPipe.length - to];

        for(int j = to; j < this.currentPipe.length; j++){ //start iterating through our pipe starting at the offset
            readBytes[j] = this.currentPipe[j]; //read bytes and insert into new array
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        int i = 0;
        for (Map.Entry<String, byte[]> set : this.pipeBuffer.entrySet()) { //loop our entire pipe hashmap
            if(Objects.equals(this.listOfPipeNames.get(id), set.getKey())){ //when we find the correct key
                this.currentPipeString = set.getKey();
                this.currentPipe = data; //retrieve our data
                break;
            }
            i++;
        }

        this.pipeBuffer.put(this.currentPipeString, this.currentPipe); //overwrite the pipe with our new data

        return this.currentPipe.length; //return the length
    }
}
