public class VirtualToPhysicalMapping {
    public boolean isDirty;
    public int physPageNumber;
    public int diskSpaceNumber;

    VirtualToPhysicalMapping(){
        this.isDirty = false;
    }
}
