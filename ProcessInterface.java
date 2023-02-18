import java.io.IOException;

public interface ProcessInterface {
    int CreateProcess(UserlandProcess myNewProcess, PriorityEnum priority) throws RescheduleException;

    boolean DeleteProcess(int processId) throws IOException;

    void Sleep(int milliseconds);
    void run() throws Exception;
}

