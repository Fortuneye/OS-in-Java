import java.util.Random;

public class RandomDevice implements Device {

    Random[] randomArray = new Random[10]; //array of 10 Random objects

    @Override
    public int Open(String s) {
        int i = 0;

        if(s != null){
            for(i = 0; this.randomArray[i] != null; i++){} //get next empty spot in array
            Random newDevice = new Random(Integer.parseInt(s)); //create a new Random object with the value of s as the seed
            this.randomArray[i] = newDevice; //insert into the array
        }else{
            for(i = 0; this.randomArray[i] != null; i++){} //get next empty spot in array
            Random newDevice = new Random(); //create a new Random object with the value of s as the seed
            this.randomArray[i] = newDevice; //insert into the array
        }
        return i;
    }

    @Override
    public void Close(int id) {
        this.randomArray[id] = null; //null the entry in the array
    }

    @Override
    public byte[] Read(int id, int size) {
        byte[] readByteArray = new byte[size]; //make an empty byte array with the size given
        Random randomNum = this.randomArray[id]; //get the ID of the random object we want
        for(int i = 0; i < size; i++){
            readByteArray[i] = (byte) randomNum.nextInt(); //fill the array with bytes of the random numbers
        }
        return readByteArray;
    }

    @Override
    public void Seek(int id, int to) {
        for(int i = 0; i < to; i++){
            byte readRandomBytes = (byte) this.randomArray[i].nextInt(); //read random bytes and don't return
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        return 0; //return 0 and do nothing
    }
}
