public class Interactive extends UserlandProcess {

    int state = 0;
    int fd = 0, fd2 = 0, fd3 = 0, fd4 = 0, mutexID;

    @Override
    public RunResult run() throws Exception {
        RunResult sleepRun = new RunResult();
        System.out.println("Goodbye World");
        sleepRun.millisecondsUsed = (10000); //get milliseconds of runtime
        sleepRun.ranToTimeout = true; //return true, run was successful

        /* state machine testing */
        if(this.state == 0){

            mutexID = OS.getInstance().AttachToMutex("newMutex");
            OS.getInstance().Lock(mutexID);

            OS.getInstance().sbrk(10240);
            OS.getInstance().WriteMemory(500, (byte) 88888);
            int test1 = OS.getInstance().ReadMemory(500);

            OS.getInstance().WriteMemory(999999999, (byte) 6666);

            this.fd = OS.getInstance().Open("pipe egg");
            byte[] readBytes2 = OS.getInstance().Read(this.fd, 7);
            byte[] readBytes = "yet another string".getBytes();
            OS.getInstance().Write(this.fd, readBytes);

        }else if(this.state == 1){

            this.fd2 = OS.getInstance().Open("random 6");
            OS.getInstance().Write(this.fd, OS.getInstance().Read(this.fd2, 10));

        }else if(this.state == 2){
            this.fd3 = OS.getInstance().Open("file testdata2.txt");
            byte[] fileData = OS.getInstance().Read(this.fd3, 60);
            OS.getInstance().Write(this.fd, fileData);

        }else if(this.state == 3){
            this.fd4 = OS.getInstance().Open("pipe newpipe");
            byte[] readBytes = "yet another string".getBytes();
            byte[] readDiffBytes= OS.getInstance().Read(fd4, 5);
            OS.getInstance().Write(this.fd4, readBytes);

        }else if(this.state == 10){
            OS.getInstance().Close(this.fd);
            OS.getInstance().Close(this.fd2);
            OS.getInstance().Close(this.fd3);
            OS.getInstance().Close(this.fd4);
        }else{
        }

        this.state++;

        return sleepRun;
    }

}
