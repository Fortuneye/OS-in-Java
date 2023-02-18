import java.util.ArrayList;

public class BasicScheduler{

    //arraylist of KernelandProcesses, indexed by PID
    ArrayList<KernelandProcess> processList = new ArrayList<>(); //active queue
    ArrayList<KernelandProcess> sleepingQueue = new ArrayList<>();
    int PID = 1; //init at 1 and increment. When one is deleted there won't be another of that number
                 //start at 1 instead of 0 because the first PID has to be 1

    //creates a process. PID increments every time it's called
    //creates a new kernelandProcess using the new PID created and the given userlandProcess
    //then adds it to the processList
    public int CreateProcess(UserlandProcess myNewProcess, PriorityEnum priority) throws RescheduleException {
        KernelandProcess newKernel = new KernelandProcess(myNewProcess, PID);
        if(processList.isEmpty()){ //if the active queue is empty, add the new process
            processList.add(newKernel);
        }else{ //if the active queue already has a process in it, add it to the sleeping queue
            sleepingQueue.add(newKernel);
        }
        PID++;
        return PID - 1; //return PID-1 to show correct PID after incrementing
    }

    //deletes a process based on the PID.
    //It returns true if it found and deleted the process and false otherwise.
    public boolean DeleteProcess(int processId) {
        boolean deleteSuccess = false;
        //since PID != index of arraylist, iterate through all until found
        for(int i = 0; i <= processList.size(); i++) {
            if (processList.get(i).ProcessUserID == processId) {
                processList.remove(i);
                deleteSuccess = true;
            }
        }
        //if the process isn't in the active queue, check sleeping queue
        for(int i = 0; i <= sleepingQueue.size(); i++) {
            if (sleepingQueue.get(i).ProcessUserID == processId) {
                sleepingQueue.remove(i);
                deleteSuccess = true;
            }
        }
        return deleteSuccess;
    }

    //round robin scheduler using sleeping queue
    void run() throws Exception {
        int i = 0;
        while(i < PID - 1) {
            if (processList.isEmpty()) { //if the active queue is empty,
                sleep(-10000); //take 10000 ms from the sleep queue
                for (int j = 0; j < sleepingQueue.size(); j++) { //iterate through list until a ready process is found
                    if (sleepingQueue.get(j).howLongToSleep <= 0) {
                        processList.add(sleepingQueue.get(j));
                        sleepingQueue.remove(sleepingQueue.get(j));
                    }
                }
            } else {
                RunResult runResultWaitTime = processList.get(i).userlandReference.run();
                sleepingQueue.add(processList.get(i));
                processList.remove(processList.get(i)); //add process to sleep queue, remove from active queue
                sleep(runResultWaitTime.millisecondsUsed); //update wait times for all sleeping processes
                sleep(-runResultWaitTime.millisecondsUsed); //remove runtime from sleeping time
                for (int j = 0; j < sleepingQueue.size(); j++) { //iterate through list until a ready process is found
                    if (sleepingQueue.get(j).howLongToSleep <= 0) {
                        processList.add(sleepingQueue.get(j));
                        sleepingQueue.remove(sleepingQueue.get(j));
                    }
                }
            }
        }
    }

    //update all sleeping processes
    public void sleep(int milliseconds) {
        int lengthOfWaiting = 0;
        for(int i = 0; i < sleepingQueue.size(); i++){
            lengthOfWaiting = milliseconds * (i + 1); //waiting time == placement in queue and process running time
            //if the process in the sleep queue has a sleeping time of 0 (or less)...
            if(sleepingQueue.get(i).howLongToSleep <= 0){
                //update sleep time with its waiting time within the queue
                sleepingQueue.get(i).sleep(lengthOfWaiting);

            }else if(milliseconds < 0){ //if the given millisecond variable is negative
                //simply use it for subtracting the sleeping time within the process
                sleepingQueue.get(i).sleep(milliseconds);
            }
        }
    }
}
