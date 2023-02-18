import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class OS implements ProcessInterface, Device, OSInterface{

    private static OS instance = new OS();
    static PriorityScheduler schedulerInstance = new PriorityScheduler();

    //empty private constructor allows class to be a singleton
    private OS(){};

    //use getInstance to get access to OS object
    public static OS getInstance(){
        return instance;
    }

    public KernelandProcess pickRandomProcess(){
        return schedulerInstance.pickRandomProcess();
    }

    public int CreateProcess(UserlandProcess myNewProcess, PriorityEnum priority) throws RescheduleException {
        return schedulerInstance.CreateProcess(myNewProcess, priority);
    }

    public boolean DeleteProcess(int processId) {
        //close every open device & free memory
        int i = 0;
        try {
            releaseAllMutexes(schedulerInstance.currentKernel);
            while (i < schedulerInstance.currentKernel.VFSid) {
                schedulerInstance.currentKernel.Close(i);
                i++;
            }
        }catch(Exception e){}
        return schedulerInstance.DeleteProcess(processId);
    }

    public void Sleep(int milliseconds) {
        schedulerInstance.sleep(milliseconds);
    }

    public void run() throws Exception {
        schedulerInstance.run();
    }

    public KernelandProcess getRunningKernel(){
        return schedulerInstance.currentKernel;
    }

    /** Memory **/

    public void WriteMemory(int address, byte value) throws RescheduleException {
        schedulerInstance.currentKernel.WriteMemory(address, value);
    }

    public byte ReadMemory(int address) throws RescheduleException {

        return schedulerInstance.currentKernel.ReadMemory(address);
    }

    public int sbrk(int amount) throws RescheduleException {
        return schedulerInstance.currentKernel.sbrk(amount);
    }

    /*** Devices ***/

    @Override
    public int Open(String s) throws Exception {
        return schedulerInstance.currentKernel.Open(s);
    }

    @Override
    public void Close(int id) throws IOException {
        schedulerInstance.currentKernel.Close(id);
    }

    @Override
    public byte[] Read(int id, int size) throws IOException {
        return schedulerInstance.currentKernel.Read(id, size);
    }

    @Override
    public void Seek(int id, int to) throws IOException {
        schedulerInstance.currentKernel.Seek(id, to);
    }

    @Override
    public int Write(int id, byte[] data) throws IOException {
        return schedulerInstance.currentKernel.Write(id, data);
    }

    /* MUTEX */

    //mutex lists
    //WAITING MUTEXES is a list of the mutexes being held right now
    ArrayList<MutexObject> waitingMutexes = new ArrayList<>();
    //RUNNABLE MUTEXES are ones not active right now
    ArrayList<MutexObject> runnableMutexes = new ArrayList<>();

    //helper function to release all mutexes a process holds
    private boolean releaseAllMutexes(KernelandProcess currentKernel){
        boolean allReleased = false;
        for(int i = 0; i < waitingMutexes.size(); i++){
            MutexObject currentMutex = waitingMutexes.get(i);
            if(currentMutex.processesAttached.contains(currentKernel)) {
                ReleaseMutex(currentMutex.id);
                allReleased = true;
            }
        }

        for(int i = 0; i < runnableMutexes.size(); i++){
            MutexObject currentMutex = runnableMutexes.get(i);
            if(currentMutex.processesAttached.contains(currentKernel)) {
                ReleaseMutex(currentMutex.id);
                allReleased = true;
            }
        }
        return allReleased;
    }

    //helper function to get a mutex based on the ID
    private MutexObject findCurrentMutex(int id){
        boolean foundMutex = false; //boolean to keep track if we found a mutex matching the ID
        MutexObject caughtMutex = null; //variable to keep track of that potentially found mutex

        //iterate active mutex queue until mutex is found (or not)
        for(int i = 0; i < waitingMutexes.size(); i++){
            if (Objects.equals(waitingMutexes.get(i).id, id)) {
                foundMutex = true;
                caughtMutex = waitingMutexes.get(i);
                break;
            }
        }

        //if mutex still isnt found, iterate the sleep mutex queue instead
        if(foundMutex == false) {
            for (int i = 0; i < runnableMutexes.size(); i++) {
                if (Objects.equals(runnableMutexes.get(i).id, id)) {
                    foundMutex = true;
                    caughtMutex = runnableMutexes.get(i);
                    break;
                }
            }
        }
        return caughtMutex; //return the mutex
    }

    @Override
    public int AttachToMutex(String name) {

        boolean foundMutex = false; //boolean to keep track if we found a mutex
        MutexObject caughtMutex = new MutexObject(); //variable to keep track of that potentially found mutex

        //iterate active mutex queue until mutex is found (or not)
            for(int i = 0; i < waitingMutexes.size(); i++){
                if (Objects.equals(waitingMutexes.get(i).mutexName, name)) {
                    foundMutex = true;
                    caughtMutex = waitingMutexes.get(i);
                    break;
                }
            }

        //if mutex still isnt found, iterate the sleep mutex queue instead
            if(foundMutex == false) {
                for (int i = 0; i < runnableMutexes.size(); i++) {
                    if (Objects.equals(runnableMutexes.get(i).mutexName, name)) {
                        foundMutex = true;
                        caughtMutex = runnableMutexes.get(i);
                        break;
                    }
                }
            }

            //if we found the mutex, then return its ID and add the current process to the mutex's list
            if(foundMutex == true){
                caughtMutex.processesAttached.add(schedulerInstance.currentKernel);
                waitingMutexes.add(caughtMutex); //mutex is active
                runnableMutexes.remove(caughtMutex);
                return caughtMutex.id;
            }else{
                //if we didnt find it, retrieve an unused mutex
                MutexObject unusedMutex = runnableMutexes.get(0);
                unusedMutex.mutexName = name;
                unusedMutex.processesAttached.add(schedulerInstance.currentKernel);
                waitingMutexes.add(unusedMutex); //mutex is active
                runnableMutexes.remove(unusedMutex);
                return unusedMutex.id;
            }
    }

    @Override
    public boolean Lock(int mutexId) throws Exception {

        boolean lockFailOrNot = true;

        try{
            //get the mutex with the given ID
            MutexObject currentMutexObj = findCurrentMutex(mutexId);

            //if we couldnt find the object in our queues/it doesn't exist, return false
            //or the current processs already has the mutex locked (avoid double locking)
            if(currentMutexObj == null || schedulerInstance.currentKernel.mutexLocked == true
                    || currentMutexObj.holdingOrNot == true
                    || !currentMutexObj.processesAttached.contains(schedulerInstance.currentKernel)){
                throw new Exception();
            }

            //lock the mutex's process
            schedulerInstance.currentKernel.mutexLocked = true;
            schedulerInstance.currentKernel.lockedMutex = currentMutexObj;
            currentMutexObj.holdingOrNot = true; //mutex is locked

            //if anything fails above, put the process in the wait queue
        }catch(Exception e){

            lockFailOrNot = false;
            schedulerInstance.sleepingQueue.add(schedulerInstance.currentKernel);

            try{
                schedulerInstance.realtimeQueue.remove(schedulerInstance.currentKernel);
            }catch(Exception ex){
                try{
                    schedulerInstance.interactiveQueue.remove(schedulerInstance.currentKernel);
                }catch(Exception exc){
                    try{
                        schedulerInstance.backgroundQueue.remove(schedulerInstance.currentKernel);
                    }catch(Exception exce){
                        throw new Exception();
                    }
                }
            }
        }

        return lockFailOrNot;
    }

    @Override
    public void Unlock(int mutexId) {

        MutexObject retrievedMutex = findCurrentMutex(mutexId);

        //check to make sure current process holds the mutex (and it exists)
        if((retrievedMutex != null) && (retrievedMutex.holdingOrNot == true
                && retrievedMutex.processesAttached.contains(schedulerInstance.currentKernel)
                && waitingMutexes.contains(retrievedMutex))){

            //"unlock" the mutex
            schedulerInstance.currentKernel.mutexLocked = false;
            schedulerInstance.currentKernel.lockedMutex = null;
            retrievedMutex.holdingOrNot = false;

            //after unlock, pull new mutex from the wait queue
            Random randomMutex = new Random();

            if(waitingMutexes.size() != 0) {
                int randomID = randomMutex.nextInt(waitingMutexes.size());
                for (int i = 0; i < waitingMutexes.size(); i++) {
                    if (Objects.equals(waitingMutexes.get(i).id, randomID)) {
                        runnableMutexes.add(waitingMutexes.get(i));
                        waitingMutexes.remove(waitingMutexes.get(i));
                        break;
                    }
                }
            }
        }else{

        }

    }

    @Override
    public void ReleaseMutex(int mutexId){

        MutexObject retrievedMutex = findCurrentMutex(mutexId);

        //if mutex exists...
        if (retrievedMutex != null) {

            //check if the process holds the mutex... if it does, unlock it
            if (retrievedMutex.processesAttached.contains(schedulerInstance.currentKernel)) {
                Unlock(mutexId);
            }

            //if no other processes are attached, remove name information
            if(retrievedMutex.processesAttached.size() == 1){
                retrievedMutex.mutexName = null;
                retrievedMutex.holdingOrNot = false; //mutex is no longer being held
            }

            //remove process from attached list
            retrievedMutex.holdingOrNot = false;
            retrievedMutex.processesAttached.remove(schedulerInstance.currentKernel);

            //update runnable/waiting lists
            if(waitingMutexes.contains(retrievedMutex)){
                runnableMutexes.add(retrievedMutex);
                waitingMutexes.remove(retrievedMutex);
            }
        }
    }
}
