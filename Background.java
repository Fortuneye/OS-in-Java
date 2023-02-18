public class Background extends UserlandProcess {

    int state = 0;
    int fd = 0, fd2 = 0, fd3 = 0, fd4 = 0, mutexID = 0;

    @Override
    public RunResult run() throws Exception {
        RunResult seeYaRun = new RunResult();
        System.out.println("See You Soon World");
        seeYaRun.millisecondsUsed = (10000); //get milliseconds of runtime
        seeYaRun.ranToTimeout = true; //return true, run was successful

        /* state machine testing */
        if(this.state == 0){

            mutexID = OS.getInstance().AttachToMutex("newMutex5");
            OS.getInstance().Lock(mutexID);

            OS.getInstance().sbrk(10240);
            OS.getInstance().WriteMemory(1023, (byte) 7777);
            int test1 = OS.getInstance().ReadMemory(1023);
            OS.getInstance().WriteMemory(90, (byte) 89);
            int test2 = OS.getInstance().ReadMemory(90);

            this.fd = OS.getInstance().Open("pipe egg");
            byte[] readBytes = "egg test string".getBytes();
            OS.getInstance().Write(this.fd, readBytes);
            byte[] readDiffBytes= OS.getInstance().Read(fd, 5);

        }else if(this.state == 1){

            int test2 = OS.getInstance().ReadMemory(90);

            this.fd2 = OS.getInstance().Open("random 100");
            OS.getInstance().Write(this.fd, OS.getInstance().Read(fd2, 10));

        }else if(this.state == 2){
            OS.getInstance().Unlock(mutexID);

            this.fd3 = OS.getInstance().Open("file testdata.txt");
            byte[] fileData = OS.getInstance().Read(this.fd3, 60);
            OS.getInstance().Write(this.fd, fileData);

        }else if(this.state == 3){
            this.fd4 = OS.getInstance().Open("pipe newpipe");
            byte[] readBytes = "egg test string".getBytes();
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

        return seeYaRun;
    }


}
