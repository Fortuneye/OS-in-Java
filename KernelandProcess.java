import java.io.IOException;
import java.util.ArrayList;

public class KernelandProcess implements Device{
    UserlandProcess userlandReference; //reference to userlandProcess
    int ProcessUserID = 0; //PID from CreateProcess
    int howLongToSleep = 0;
    PriorityEnum priorityNum;
    int VFSid = 0; //VFS ID
    VFS virtualFileSystem = new VFS(); //VFS global object

    //mutex the process is locked by
    MutexObject lockedMutex = null;
    boolean mutexLocked = false;

    //INDEX = (PAGE OF) VIRTUAL ADDRESS.... CONTENTS = PAGE

    VirtualToPhysicalMapping[] virtToPhysMap = new VirtualToPhysicalMapping[1024];

    static MemoryManagement memory; //every process shares the same memory

    static {
        try {
            memory = new MemoryManagement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //constructor for kernelandProcess
    KernelandProcess(UserlandProcess newUserland, int PID) throws RescheduleException {
        this.userlandReference = newUserland;
        this.ProcessUserID = PID;
    }

    public KernelandProcess() {
    }

    //update the sleep time for the process
    public void sleep(int milliseconds){
        this.howLongToSleep = this.howLongToSleep + milliseconds;
    }

    public void setPriority(PriorityEnum priorityNumber){
        this.priorityNum = priorityNumber;
    }

    /** PAGING **/

    public boolean hasPhysMem(){
        int i = 0;

        while(i < virtToPhysMap.length && virtToPhysMap[i] != null && virtToPhysMap[i].physPageNumber != -1){
            i++;
        }

        return i > 0;
    }

    //write to main memory
    public void WriteMemory(int address, byte value) throws RescheduleException {
        memory.WriteMemory(address, value);
    }

    //read from main memory
    public byte ReadMemory(int address) throws RescheduleException {
        return memory.ReadMemory(address);
    }

    //allocate memory
    public int sbrk(int amount) throws RescheduleException {
        return memory.sbrk(amount);
    }

    //invalidate the TLB on a process switch
    public void tlbInvalidate(){
        memory.tlbInvalidate();
    }

    //free our local memory by accessing the main memory
    public void freeMemory(){
        int i = 0;
        while(i < 1024 && this.virtToPhysMap[i] != null){ //loop the entire page array
            if(this.virtToPhysMap[i].physPageNumber != -1) {
                memory.freeMemory(this.virtToPhysMap[i].physPageNumber); //send our allocated block of memory into the freeMemory method
                this.virtToPhysMap[i] = null; //set our page array index to -1 (unlink from main memory)
            }
            i++;
        }
    }

    /** DEVICES **/

    @Override
    public int Open(String s) throws Exception {
        this.VFSid = virtualFileSystem.Open(s);
        return this.VFSid;
    }

    @Override
    public void Close(int id) throws IOException {
        virtualFileSystem.Close(id);
    }

    @Override
    public byte[] Read(int id, int size) throws IOException {
        return virtualFileSystem.Read(id, size);
    }

    @Override
    public void Seek(int id, int to) throws IOException {
        virtualFileSystem.Seek(id, to);
    }

    @Override
    public int Write(int id, byte[] data) throws IOException {
        return virtualFileSystem.Write(id, data);
    }
}
