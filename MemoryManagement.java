import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

public class MemoryManagement implements MemoryInterface{

    byte[][] physicalPages = new byte[1024][1024]; //[page][offset]
    BitSet freeList = new BitSet(1024); //bitset to track blocks/pages currently in use
    FakeFileSystem ffs = new FakeFileSystem();
    int nextToWriteOut = -1;

    //TLB
    int tlbVirtual = -1;
    int tlbPhysical = -1;

    public MemoryManagement() throws Exception {
        this.nextToWriteOut = ffs.Open("swapfile");
    }


    private int virtualToPhysicalMapping(int virtual) throws Exception {
        int addrHolder = virtual;
        int pageNumber = addrHolder / 1024; //GET VIRTUAL PAGE
        int offset = addrHolder % 1024;

            if (pageNumber > 1024) { //if our page number is invalid, throw an exception
                throw new RescheduleException();
            }

            //go into the page array in KLP to GET OUR PHYSICAL PAGE
            int physMem = OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].physPageNumber;

            if(physMem == -1){

                int j = 0;
                while(j < 1024 && this.freeList.get(j)){ //check if freelist has available pages
                    j++;
                }

                if(j >= 1024){

                    //pick a random process that has physical memory attached
                    KernelandProcess randomProc = OS.getInstance().pickRandomProcess();

                    int i = 0;
                    //get number of available physical pages from the process
                    while(i < randomProc.virtToPhysMap.length && randomProc.virtToPhysMap[i] != null && randomProc.virtToPhysMap[i].physPageNumber != -1){
                        i++;
                    }
                    Random rng = new Random();
                    int rngNum = rng.nextInt(i); //get random number out of the number of pages

                    if(randomProc.virtToPhysMap[rngNum].isDirty == true){ //if the page is dirty, we need to write to disk

                        this.nextToWriteOut = ffs.Open("swapfile");
                        //open the swapfile and write the page to it.
                        ffs.Write(this.nextToWriteOut, this.physicalPages[randomProc.virtToPhysMap[rngNum].physPageNumber]);
                        randomProc.virtToPhysMap[rngNum].diskSpaceNumber = this.nextToWriteOut;

                        //dirty flag is reset on swap
                        randomProc.virtToPhysMap[rngNum].isDirty = false;
                    }

                    //remap the page table
                    OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].physPageNumber = randomProc.virtToPhysMap[rngNum].physPageNumber;

                    //unmap the random page
                    randomProc.virtToPhysMap[rngNum].physPageNumber = -1;

                        if(OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].diskSpaceNumber != -1){ //if current page has been previously written out

                            //we need to load it in
                            byte[] diskData = ffs.Read(OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].diskSpaceNumber, 1024);
                            for(int x = 0; x < 1024; x++){
                                this.physicalPages[OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].physPageNumber][x] = diskData[x];
                            }

                        }else{ //if we aren't loading, we clear the page
                            for(int x = 0; x < 1024; x++){
                                this.physicalPages[OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].physPageNumber][x] = 0;
                            }
                        }

                        physMem = OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].physPageNumber;

                }else{
                    boolean memAlloc = false;
                    int i = 0;
                    while(memAlloc == false){ //loop until we've allocated as much as requested
                        if(!freeList.get(i)){ //if the bitSet is false (open space for memory)...
                            OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].physPageNumber = i;
                            freeList.set(i, true); //MARK PHYSICAL PAGE AS IN USE
                            memAlloc = true;
                            j++;
                        }
                        i++;
                    }
                    physMem = OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].physPageNumber;
                }

            }

            return physMem; //RETURN DATA
    }

    @Override
    public void WriteMemory(int address, byte value) throws RescheduleException {
        int addrHolder = address;
        int offset = addrHolder % 1024;
        int pageNumber = addrHolder / 1024; //GET VIRTUAL PAGE
        int physPage = 0;
        try {
            physPage = virtualToPhysicalMapping(addrHolder);
        } catch (Exception e) {
            throw new RescheduleException();
        }

        OS.getInstance().getRunningKernel().virtToPhysMap[pageNumber].isDirty = true;

        this.physicalPages[physPage][offset] = value;
    }

    @Override
    public byte ReadMemory(int address) throws RescheduleException {
        int addrHolder = address;
        int offset = addrHolder % 1024;
        int physPage = 0;
        try {
            physPage = virtualToPhysicalMapping(addrHolder);
        } catch (Exception e) {
            throw new RescheduleException();
        }

        return this.physicalPages[physPage][offset];
    }

    @Override
    public int sbrk(int amount) throws RescheduleException {

        if(amount < 1024){ //if our amount is less than a single page, give at least one page (1024 bytes)
            amount = 1;

        }else {

            //divide by 1024, amount passed will be asking for entire pages of memory (1024 bytes) (i.e. sbrk(4096) -> 4 * 1024 -> 4 pages)
            //(FIND NUMBER OF PAGES TO ADD)
            amount = amount / 1024;
        }

        if(amount > 1024){ //if the amount requested is beyond how much memory we have, throw an exception
            throw new RescheduleException();
        }

        int j = 0; //variable to find the next spot in page array to store physical addresses
        int topOfMemory = 0; //variable to keep track of the next top of memory

        //loop until we find the next top of memory (FIND END OF EXISTING VIRTUAL SPACE)

        if(OS.getInstance().getRunningKernel().virtToPhysMap[j] == null){
            j = 0;

        }else {
            while ( j < 1024 && OS.getInstance().getRunningKernel().virtToPhysMap[j] != null) {
                j++;
            }
        }

        if(j == 0){ //if we didnt iterate/first time allocating, top of memory is 0
            topOfMemory = 0;

        } else if(j == 1024){
            throw new RescheduleException();

        }else{ //otherwise, get our last top of memory, add 1 to it to account for the start of arrays being 0,
               // then multiply by 1024 to tell user actual page size
            topOfMemory = j * 1024;
        }

        int howManyAllocated = 0; //variable to keep track of how much memory we've allocated

        while(howManyAllocated < amount){ //loop until we've allocated as much as requested
                OS.getInstance().getRunningKernel().virtToPhysMap[j] = new VirtualToPhysicalMapping(); //ADD MAPPING TO END OF EXISTING VIRTUAL SPACE
                OS.getInstance().getRunningKernel().virtToPhysMap[j].physPageNumber = -1;
                OS.getInstance().getRunningKernel().virtToPhysMap[j].diskSpaceNumber = -1;
                howManyAllocated++;
                j++;
        }

        return topOfMemory;
    }

    public void tlbInvalidate(){
        this.tlbVirtual = -1;
        this.tlbPhysical = -1;
    }


    public void freeMemory(int address) {
        int i = 0;
        int j = 0;
        while(i < 1024 && !freeList.isEmpty()){ //loop for the entirety of the block in memory
            this.physicalPages[address][i] = 0; //set that specific address as 0/empty
            i++;
        }
        i = 0;
        this.freeList.set(address, false);
    }
}
