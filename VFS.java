import java.io.IOException;
import java.util.Objects;
import java.util.StringTokenizer;

public class VFS implements Device{

    Device[] deviceArray = new Device[100]; //array of devices
    int[] idArray = new int[100]; //array of VFS IDs

    PipeDevice pipeDevice = new PipeDevice(); //the pipe device
    FakeFileSystem fileSysDevice = new FakeFileSystem(); //the file system device
    RandomDevice randomDevice = new RandomDevice(); //the random device

    @Override
    public int Open(String s) throws Exception {
        int id = 0;
        int i = 0;

        //get the first word in the string command
        StringTokenizer stringTokenizer = new StringTokenizer(s);
        stringTokenizer.nextToken();
        String deviceName = stringTokenizer.nextToken();


        //if command wants a random device
        if(s.contains("random") || s.contains("Random")){
            id = randomDevice.Open(deviceName); //open the device and save the ID

            //if command wants a pipe device
        }else if(s.contains("pipe") || s.contains("Pipe")){
            id = pipeDevice.Open(deviceName); //open the device and save the ID

            //if command wants a file device
        }else if(s.contains("file") || s.contains("File")){
            id = fileSysDevice.Open(deviceName); //open the device and save the ID
        }

        //if the device array is empty, just throw the new device in the array at index 0
        if(this.deviceArray[0] == null){

            if(s.contains("pipe") || s.contains("Pipe")){
                this.deviceArray[0] = pipeDevice;

            }else if(s.contains("random") || s.contains("Random")){
                this.deviceArray[0] = randomDevice;

            }else if(s.contains("file") || s.contains("File")){
                this.deviceArray[0] = fileSysDevice;
            }

            this.idArray[0] = id; //save the ID in the ID array

        }else {
            //loop the device array to get the next empty space available
            for (i = i; this.deviceArray[i] != null && i < this.deviceArray.length; i++) {}

            //put the device in the next empty spot in the device array
            if(s.contains("pipe") || s.contains("Pipe")){
                this.deviceArray[i] = pipeDevice;

            }else if(s.contains("random") || s.contains("Random")){
                this.deviceArray[i] = randomDevice;

            }else if(s.contains("file") || s.contains("File")){
                this.deviceArray[i] = fileSysDevice;
            }

            this.idArray[i] = id; //save the ID in the ID array
        }

        return i; //return the spot in the array that we saved the device in
    }

    @Override
    public void Close(int id) throws IOException {
        int readingIndex = this.idArray[id]; //get the device ID
        Device readingDevice = this.deviceArray[id]; //get the device
        readingDevice.Close(readingIndex);// close the device within the device

        this.deviceArray[id] = null; //null the device array at the location of the ID
        this.idArray[id] = 0; //remove an ID if there is one
    }

    @Override
    public byte[] Read(int id, int size) throws IOException {
        int readingIndex = this.idArray[id]; //get the device ID
        Device readingDevice = this.deviceArray[id]; //get the device

        return readingDevice.Read(readingIndex, size); //send the device ID and size parameter to the Read function
    }

    @Override
    public void Seek(int id, int to) throws IOException {
        int readingIndex = this.idArray[id]; //get the device ID
        Device readingDevice = this.deviceArray[id]; //get the device

        readingDevice.Seek(readingIndex, to); //send the device ID and size parameter to the Seek function
    }

    @Override
    public int Write(int id, byte[] data) throws IOException {
        int readingIndex = this.idArray[id]; //get the device ID
        Device readingDevice = this.deviceArray[id]; //get the device

        return readingDevice.Write(readingIndex, data); //send the device ID and size parameter to the Write function
    }
}
