public class maxMemAllocProc extends UserlandProcess {

    int state = 0;
    int fd = 0, fd2 = 0, fd3 = 0, fd4 = 0, fd5 = 0, fd6 = 0;

    @Override
    public RunResult run() throws Exception {
        RunResult sleepRun = new RunResult();
        System.out.println("Memory allocator");
        sleepRun.millisecondsUsed = (10000); //get milliseconds of runtime
        sleepRun.ranToTimeout = true; //return true, run was successful

        /* state machine testing */
        if (this.state == 0) {
            System.out.println("Allocating all memory!");
            OS.getInstance().sbrk(1048576);

            for (int i = 0; i < 1024; i++) {
                OS.getInstance().WriteMemory(i * 1024, (byte) 9);
            }
            System.out.println("Memory allocated!");
            this.state++;

        } if (this.state == 1){
            OS.getInstance().WriteMemory(1024, (byte)99);
        }
        return sleepRun;
    }
}
