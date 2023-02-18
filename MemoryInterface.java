public interface MemoryInterface {
    void WriteMemory(int address, byte value) throws RescheduleException;
    byte ReadMemory(int address) throws RescheduleException;
    int sbrk(int amount) throws RescheduleException;
}
