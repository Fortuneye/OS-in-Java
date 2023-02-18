public class Startup {
    public static void main (String[] args) throws Exception {
        Realtime newHelloWorldProc = new Realtime();
        Interactive newGoodbyeProc = new Interactive();
        Background newSeeYa = new Background();
        maxMemAllocProc memAlloc = new maxMemAllocProc();

        int proc1 = OS.getInstance().CreateProcess(newHelloWorldProc, PriorityEnum.RealTime);
        int proc2 = OS.getInstance().CreateProcess(newGoodbyeProc, PriorityEnum.Interactive);
        int proc3 = OS.getInstance().CreateProcess(newSeeYa, PriorityEnum.Background);
        int proc4 = OS.getInstance().CreateProcess(memAlloc, PriorityEnum.RealTime);

        MutexObject mut1 = new MutexObject();
        mut1.id = 0;
        MutexObject mut2 = new MutexObject();
        mut2.id = 1;
        MutexObject mut3 = new MutexObject();
        mut3.id = 2;
        MutexObject mut4 = new MutexObject();
        mut4.id = 3;
        MutexObject mut5 = new MutexObject();
        mut5.id = 4;
        MutexObject mut6 = new MutexObject();
        mut6.id = 5;
        MutexObject mut7 = new MutexObject();
        mut7.id = 6;
        MutexObject mut8 = new MutexObject();
        mut8.id = 7;
        MutexObject mut9 = new MutexObject();
        mut9.id = 8;
        MutexObject mut10 = new MutexObject();
        mut10.id = 9;

        OS.getInstance().runnableMutexes.add(mut1);
        OS.getInstance().runnableMutexes.add(mut2);
        OS.getInstance().runnableMutexes.add(mut3);
        OS.getInstance().runnableMutexes.add(mut4);
        OS.getInstance().runnableMutexes.add(mut5);
        OS.getInstance().runnableMutexes.add(mut6);
        OS.getInstance().runnableMutexes.add(mut7);
        OS.getInstance().runnableMutexes.add(mut8);
        OS.getInstance().runnableMutexes.add(mut9);
        OS.getInstance().runnableMutexes.add(mut10);

        OS.getInstance().run();
    }
}
