import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;

public class FakeFileSystem implements Device{

    RandomAccessFile[] randomFileArray = new RandomAccessFile[10]; //array of 10 random access files

    @Override
    public int Open(String s) throws Exception {

        int i = 0;

        if(s == null){ //if file is null, throw an exception
            throw new Exception();
        }

        if(randomFileArray[0] == null){ //if the array is empty, fill it at index 0
            randomFileArray[0] = new RandomAccessFile(s, "rw");

        }else {

            for (i = i; randomFileArray[i] != null; i++) {} //iterate until we hit an empty spot in the array
            randomFileArray[i] = new RandomAccessFile(s, "rw"); //create a new file in read only mode
        }

        return i; //return the index we inserted the new file into in the array
    }

    @Override
    public void Close(int id) throws IOException {
        if(this.randomFileArray[id] != null) {
            RandomAccessFile randomFile = randomFileArray[id];
            randomFile.close(); //close the randomAccessFile
            Arrays.fill(randomFileArray, null); //clear out internal array
        }
    }

    @Override
    public byte[] Read(int id, int size) throws IOException {
        RandomAccessFile foundDevice = this.randomFileArray[id];
        byte[] readByteArray = new byte[size]; //create a byte array using the size parameter
        foundDevice.read(readByteArray); //read() will read bytes of data into the byte array
        return readByteArray;
    }

    @Override
    public void Seek(int id, int to) throws IOException {
        RandomAccessFile foundDevice = this.randomFileArray[id];
        byte[] buff = new byte[(int) foundDevice.length() - to]; //get a byte array and set its length
                                                                 // to the length of the file - our offset
        foundDevice.read(buff, to, buff.length); //read our bytes into the buffer, make our to
                                                 // parameter the offset, and make our max length the length of the buffer
    }

    @Override
    public int Write(int id, byte[] data) throws IOException {
        RandomAccessFile foundDevice = this.randomFileArray[id];
        foundDevice.write(data); //write bytes into the randomAccessFile
        return (int) foundDevice.length(); //return the length of the file
    }
}
