import java.util.ArrayList;
import java.util.Random;

public class PriorityScheduler {
    //arraylist of KernelandProcesses, indexed by PID
    ArrayList<KernelandProcess> realtimeQueue = new ArrayList<>(); //active queue
    ArrayList<KernelandProcess> sleepingQueue = new ArrayList<>();
    ArrayList<KernelandProcess> interactiveQueue = new ArrayList<>();
    ArrayList<KernelandProcess> backgroundQueue = new ArrayList<>();
    int PID = 1; //init at 1 and increment. When one is deleted there won't be another of that number
    //start at 1 instead of 0 because the first PID has to be 1
    KernelandProcess currentKernel; //keep track of running process for OS Device operations

    public KernelandProcess pickRandomProcess(){
        Random rng = new Random();
        int rngNum = rng.nextInt(this.PID);
        KernelandProcess returnedRandomKernel = new KernelandProcess();
        boolean returnedKernelCheck = false;
        boolean retrieveKernel = false;

        while(!returnedKernelCheck) {

            while(!retrieveKernel) {
                for (int i = 0; i < realtimeQueue.size(); i++) {
                    if (realtimeQueue.get(i).ProcessUserID == rngNum) {
                        returnedRandomKernel = realtimeQueue.get(i);
                        retrieveKernel = true;
                    }
                }
                if (!retrieveKernel) {
                    for (int i = 0; i < sleepingQueue.size(); i++) {
                        if (sleepingQueue.get(i).ProcessUserID == rngNum) {
                            returnedRandomKernel = sleepingQueue.get(i);
                            retrieveKernel = true;
                        }
                    }
                }

                if (!retrieveKernel) {
                    for (int i = 0; i < interactiveQueue.size(); i++) {
                        if (interactiveQueue.get(i).ProcessUserID == rngNum) {
                            returnedRandomKernel = interactiveQueue.get(i);
                            retrieveKernel = true;
                        }
                    }
                }

                if (!retrieveKernel) {
                    for (int i = 0; i < backgroundQueue.size(); i++) {
                        if (backgroundQueue.get(i).ProcessUserID == rngNum) {
                            returnedRandomKernel = backgroundQueue.get(i);
                            retrieveKernel = true;
                        }
                    }
                }

                if (returnedRandomKernel.hasPhysMem()) {
                    returnedKernelCheck = true;
                }else{
                    retrieveKernel = false;
                    rngNum = rng.nextInt(this.PID);
                }

            }

        }
        return returnedRandomKernel;
    }

    int CreateProcess(UserlandProcess myNewProcess, PriorityEnum priority) throws RescheduleException {
        KernelandProcess newKernel = new KernelandProcess(myNewProcess, PID);
        newKernel.setPriority(priority); //set process priority

        //initialize queues by inserting first process to arrive as ready
        if(priority == PriorityEnum.RealTime){
            realtimeQueue.add(newKernel);
        }else if(priority == PriorityEnum.Interactive){
            interactiveQueue.add(newKernel);
        }else if(priority == PriorityEnum.Background){
            backgroundQueue.add(newKernel);
        }else{
            //if queues have at least one ready process, then send to sleep queue
            sleepingQueue.add(newKernel);
        }
        this.PID++;
        return this.PID - 1; //return PID-1 to show correct PID after incrementing
    }

    public boolean DeleteProcess(int processId) {
        boolean deleteSuccess = false;

        //since PID != index of arraylist and there are 4 queues,
        //iterate through ALL queues until found
        for(int i = 0; i < realtimeQueue.size(); i++) {
            if (realtimeQueue.get(i).ProcessUserID == processId) {
                realtimeQueue.get(i).freeMemory();
                realtimeQueue.remove(i);
                deleteSuccess = true;
            }
        }
        if(!deleteSuccess) {
            for (int i = 0; i < sleepingQueue.size(); i++) {
                if (sleepingQueue.get(i).ProcessUserID == processId) {
                    sleepingQueue.get(i).freeMemory();
                    sleepingQueue.remove(i);
                    deleteSuccess = true;
                }
            }
        }

        if(!deleteSuccess) {
            for (int i = 0; i < interactiveQueue.size(); i++) {
                if (interactiveQueue.get(i).ProcessUserID == processId) {
                    interactiveQueue.get(i).freeMemory();
                    interactiveQueue.remove(i);
                    deleteSuccess = true;
                }
            }
        }

        if(!deleteSuccess) {
            for (int i = 0; i < backgroundQueue.size(); i++) {
                if (backgroundQueue.get(i).ProcessUserID == processId) {
                    backgroundQueue.get(i).freeMemory();
                    backgroundQueue.remove(i);
                    deleteSuccess = true;
                }
            }
        }
        return deleteSuccess;
    }

    void run() throws Exception {
        int i = 0;
        KernelandProcess runningAbuser = null; //keep variable for process that last ran
        KernelandProcess potentialAbuser = null; //keep variable for process that is running now to
                                                // compare with last ran and determine if one is abusing priority
        RunResult completedRun = null;
        int howManyTimesRan = 0; //how many times a process ran (to check for abuse)

        while(true) {
            Random randomNumGen = new Random(); //generate random number between 0-10
            int randomInt = randomNumGen.nextInt(10);

            if(randomInt == 0){ //background, 1/10th of the time
                if(!backgroundQueue.isEmpty()) {
                    while (!backgroundQueue.isEmpty()){ //run every ready process in queue
                        this.currentKernel = backgroundQueue.get(i);
                        potentialAbuser = backgroundQueue.get(i);

                        if ((potentialAbuser != null && !interactiveQueue.isEmpty() && interactiveQueue.get(i) != null) && interactiveQueue.get(i) != potentialAbuser) { //if the last run program is NOT the same as this one (switching processes)
                            backgroundQueue.get(i).tlbInvalidate(); //invalidate the memory/wipe TLB
                        }

                        /* Run the process, and if there's a Reschedule Exception, catch it and delete the process accordingly */
                        try {
                            completedRun = backgroundQueue.get(i).userlandReference.run();
                        }catch(RescheduleException e){
                            OS.getInstance().DeleteProcess(this.currentKernel.ProcessUserID);
                            break;
                        }

                        sleepingQueue.add(backgroundQueue.get(i));
                        backgroundQueue.remove(backgroundQueue.get(i)); //add process to sleep queue, remove from active queue
                        sleep(completedRun.millisecondsUsed); //update wait times for all sleeping processes
                        sleep(-completedRun.millisecondsUsed); //remove runtime from sleeping time
                    }
                }
            }else if(randomInt >= 1 && randomInt < 4){ //interactive, 3/10ths of the time
                if(!interactiveQueue.isEmpty()) {
                    while (!interactiveQueue.isEmpty()) { //run every ready process in queue
                        this.currentKernel = interactiveQueue.get(i);

                        if ((potentialAbuser != null && !interactiveQueue.isEmpty() && interactiveQueue.get(i) != null) && interactiveQueue.get(i) != potentialAbuser) { //if the last run program is NOT the same as this one (switching processes)
                            this.currentKernel.tlbInvalidate(); //invalidate the memory/wipe TLB
                        }

                        /* Run the process, and if there's a Reschedule Exception, catch it and delete the process accordingly */
                        try {
                            completedRun = interactiveQueue.get(i).userlandReference.run();
                        }catch(RescheduleException e){
                            OS.getInstance().DeleteProcess(this.currentKernel.ProcessUserID);
                            break;
                        }

                        sleep(completedRun.millisecondsUsed); //update wait times for all sleeping processes
                        sleep(-completedRun.millisecondsUsed); //remove runtime from sleeping time
                        potentialAbuser = interactiveQueue.get(i); //set potential abuser as proc that just ran
                        if (runningAbuser == null) { //if runningAbuser hasn't been updated at all
                            runningAbuser = potentialAbuser; //set the current potential abuser
                            howManyTimesRan++;
                            sleepingQueue.add(interactiveQueue.get(i));
                            interactiveQueue.remove(interactiveQueue.get(i)); //add process to sleep queue, remove from active queue
                        } else {
                            if (runningAbuser == potentialAbuser) { //if the last run program is the same as this one
                                howManyTimesRan++;
                            } else {
                                howManyTimesRan = 0;
                            }

                            if (howManyTimesRan > 4) { //if the process has run 5+ times, set its priority lower
                                interactiveQueue.get(i).setPriority(PriorityEnum.Background);
                                howManyTimesRan = 0;
                            }
                            sleepingQueue.add(interactiveQueue.get(i));
                            interactiveQueue.remove(interactiveQueue.get(i)); //add process to sleep queue, remove from active queue
                        }
                    }
                }
            }else if(randomInt >= 4){ //realtime, 6/10ths of the time
                if(!realtimeQueue.isEmpty()) {
                    while (!realtimeQueue.isEmpty()) { //run every ready process in queue
                        this.currentKernel = realtimeQueue.get(i);

                        if ((potentialAbuser != null && !interactiveQueue.isEmpty() && interactiveQueue.get(i) != null) && interactiveQueue.get(i) != potentialAbuser) { //if the last run program is NOT the same as this one (switching processes)
                            realtimeQueue.get(i).tlbInvalidate(); //invalidate the memory/wipe TLB
                        }

                        /* Run the process, and if there's a Reschedule Exception, catch it and delete the process accordingly */
                        try {
                            completedRun = realtimeQueue.get(i).userlandReference.run();
                        }catch(RescheduleException e){
                            OS.getInstance().DeleteProcess(this.currentKernel.ProcessUserID);
                            break;
                        }

                        sleep(completedRun.millisecondsUsed); //update wait times for all sleeping processes
                        sleep(-completedRun.millisecondsUsed); //remove runtime from sleeping time
                        potentialAbuser = realtimeQueue.get(i);
                        if (runningAbuser == null) { //if runningAbuser hasn't been updated at all
                            runningAbuser = potentialAbuser; //set the current potential abuser
                            howManyTimesRan++;
                            sleepingQueue.add(realtimeQueue.get(i));
                            realtimeQueue.remove(realtimeQueue.get(i)); //add process to sleep queue, remove from active queue
                        } else {
                            if (runningAbuser == potentialAbuser) { //if the last run program is the same as this one
                                howManyTimesRan++;
                            } else {
                                howManyTimesRan = 0;
                            }

                            if (howManyTimesRan > 4) { //if the process has run 5+ times, set its priority lower
                                realtimeQueue.get(i).setPriority(PriorityEnum.Interactive);
                                howManyTimesRan = 0;
                            }
                            sleepingQueue.add(realtimeQueue.get(i));
                            realtimeQueue.remove(realtimeQueue.get(i)); //add process to sleep queue, remove from active queue
                        }
                    }
                }
            }else if(!interactiveQueue.isEmpty()){
                randomInt = randomNumGen.nextInt(4); //generate new random number between 0-4

                if(randomInt == 1){ //background, 1/4th of the time
                    if(!backgroundQueue.isEmpty()) {
                        while (!backgroundQueue.isEmpty()) { //run every ready process in queue
                            this.currentKernel = backgroundQueue.get(i);

                            if ((potentialAbuser != null && !interactiveQueue.isEmpty() && interactiveQueue.get(i) != null) && interactiveQueue.get(i) != potentialAbuser) { //if the last run program is NOT the same as this one (switching processes)
                                backgroundQueue.get(i).tlbInvalidate(); //invalidate the memory/wipe TLB
                            }

                            /* Run the process, and if there's a Reschedule Exception, catch it and delete the process accordingly */
                            try {
                                completedRun = backgroundQueue.get(i).userlandReference.run();
                            }catch(RescheduleException e){
                                OS.getInstance().DeleteProcess(this.currentKernel.ProcessUserID);
                                break;
                            }

                            potentialAbuser = backgroundQueue.get(i);
                            sleep(completedRun.millisecondsUsed); //update wait times for all sleeping processes
                            sleep(-completedRun.millisecondsUsed); //remove runtime from sleeping time
                        }
                    }
                }else{ //interactive, 3/4ths of the time
                    if(!interactiveQueue.isEmpty()) {
                        while (!interactiveQueue.isEmpty()) { //run every ready process in queue
                            this.currentKernel = interactiveQueue.get(i);

                            if ((potentialAbuser != null && !interactiveQueue.isEmpty() && interactiveQueue.get(i) != null) && interactiveQueue.get(i) != potentialAbuser) { //if the last run program is NOT the same as this one (switching processes)
                                interactiveQueue.get(i).tlbInvalidate(); //invalidate the memory/wipe TLB
                            }

                            /* Run the process, and if there's a Reschedule Exception, catch it and delete the process accordingly */
                            try {
                                completedRun = interactiveQueue.get(i).userlandReference.run();
                            }catch(RescheduleException e){
                                OS.getInstance().DeleteProcess(this.currentKernel.ProcessUserID);
                                break;
                            }

                            sleepingQueue.add(interactiveQueue.get(i));
                            interactiveQueue.remove(interactiveQueue.get(i)); //add process to sleep queue, remove from active queue
                            sleep(completedRun.millisecondsUsed); //update wait times for all sleeping processes
                            sleep(-completedRun.millisecondsUsed); //remove runtime from sleeping time
                            if (runningAbuser == null) { //if runningAbuser hasn't been updated at all
                                runningAbuser = potentialAbuser; //set the current potential abuser
                                howManyTimesRan++;
                                sleepingQueue.add(interactiveQueue.get(i));
                                interactiveQueue.remove(interactiveQueue.get(i)); //add process to sleep queue, remove from active queue
                            } else { //background
                                    if (runningAbuser == potentialAbuser) {  //if the last run program is the same as this one
                                        howManyTimesRan++;
                                    } else {
                                        howManyTimesRan = 0;
                                    }

                                    if (howManyTimesRan > 4) { //if the process has run 5+ times, set its priority lower
                                        interactiveQueue.get(i).setPriority(PriorityEnum.Background);
                                        howManyTimesRan = 0;
                                    }
                                    sleepingQueue.add(interactiveQueue.get(i));
                                    interactiveQueue.remove(interactiveQueue.get(i)); //add process to sleep queue, remove from active queue
                            }
                        }
                    }
                }
            }
            //if the sleeping queue isn't empty, retrieve all ready processes inside
            if(!sleepingQueue.isEmpty()){
                for (int j = 0; j < sleepingQueue.size(); j++) { //iterate through list until a ready process is found
                    if (sleepingQueue.get(j).howLongToSleep <= 0) {
                        if(sleepingQueue.get(j).priorityNum.equals(PriorityEnum.RealTime)){
                            realtimeQueue.add(sleepingQueue.get(j));
                            sleepingQueue.remove(sleepingQueue.get(j));
                        }else if(sleepingQueue.get(j).priorityNum.equals(PriorityEnum.Background)){
                            backgroundQueue.add(sleepingQueue.get(j));
                            sleepingQueue.remove(sleepingQueue.get(j));
                        }else{
                            interactiveQueue.add(sleepingQueue.get(j));
                            sleepingQueue.remove(sleepingQueue.get(j));
                        }
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
