public class Realtime extends UserlandProcess {

    int state = 0;
    int fd = 0, fd2 = 0, fd3 = 0, fd4 = 0, fd5 = 0, fd6 = 0, mutexID = 0;

    @Override
    public RunResult run() throws Exception {
        RunResult sleepRun = new RunResult();
        System.out.println("Hello World");
        sleepRun.millisecondsUsed = (10000); //get milliseconds of runtime
        sleepRun.ranToTimeout = true; //return true, run was successful

        /* state machine testing */
        if(this.state == 0){

            mutexID = OS.getInstance().AttachToMutex("newMutex");
            OS.getInstance().AttachToMutex("newMutex2");
            OS.getInstance().AttachToMutex("newMutex3");
            OS.getInstance().AttachToMutex("newMutex4");
            OS.getInstance().AttachToMutex("newMutex5");
            OS.getInstance().Lock(mutexID);

            OS.getInstance().sbrk(10240);
            OS.getInstance().WriteMemory(1300, (byte) 7);
            int readmem1 = OS.getInstance().ReadMemory(1300);
            OS.getInstance().WriteMemory(1, (byte) 9);
            int readmem2 = OS.getInstance().ReadMemory(1);
            OS.getInstance().WriteMemory(1, (byte) 10);
            int sbrk2 = OS.getInstance().sbrk(10240);
            int readmem3 = OS.getInstance().ReadMemory(1);
            OS.getInstance().WriteMemory(1023, (byte) 55);
            int readmem4 = OS.getInstance().ReadMemory(1023);

            this.fd = OS.getInstance().Open("pipe egg");
            byte[] readBytes = "egg test string".getBytes();
            OS.getInstance().Write(this.fd, readBytes);
            byte[] readDiffBytes= OS.getInstance().Read(fd, 5);

        }else if(this.state == 1){

            int readmem4 = OS.getInstance().ReadMemory(1023);

            this.fd2 = OS.getInstance().Open("random 9845");
            OS.getInstance().Write(this.fd, OS.getInstance().Read(fd2, 10));

        }else if(this.state == 2) {
            this.fd3 = OS.getInstance().Open("file testdata.txt");
            byte[] fileData = OS.getInstance().Read(this.fd3, 60);
            OS.getInstance().Write(this.fd, fileData);

        }else if(this.state == 3){
            this.fd4 = OS.getInstance().Open("pipe newpipe");
            byte[] readBytes = "testing one more pipe".getBytes();
            byte[] readDiffBytes= OS.getInstance().Read(fd4, 5);
            OS.getInstance().Write(this.fd4, readBytes);

        }else if(this.state == 4) {
            this.fd5 = OS.getInstance().Open("pipe otherNewPipe");
            this.fd6 = OS.getInstance().Open("pipe pipeItUp");
            int fdTemp1 = OS.getInstance().Open("random 70");
            int fdTemp2 = OS.getInstance().Open("file testdata3.txt");
            byte[] readBytes = "testing one more pipe AGAIN".getBytes();
            byte[] readDiffBytes = OS.getInstance().Read(fd4, 5);
            OS.getInstance().Write(this.fd4, readBytes);
            OS.getInstance().Write(fd5, OS.getInstance().Read(fdTemp1, 9));
            OS.getInstance().Write(fd6, OS.getInstance().Read(fdTemp2, 55));
            OS.getInstance().Close(fdTemp1);
            OS.getInstance().Close(fdTemp2);

        }else if(this.state == 10){

            OS.getInstance().Unlock(mutexID);
            OS.getInstance().ReleaseMutex(mutexID);

            OS.getInstance().Close(this.fd6);
            OS.getInstance().Close(this.fd);
            OS.getInstance().Close(this.fd4);
            OS.getInstance().Close(this.fd3);
            OS.getInstance().Close(this.fd2);
            OS.getInstance().Close(this.fd5);
        }else{
        }

        this.state++;

        return sleepRun;
    }
}
