public interface Mutex {
    int AttachToMutex(String name);
    boolean Lock(int mutexId) throws Exception;
    void Unlock(int mutexId);
    void ReleaseMutex(int mutexId);
}
