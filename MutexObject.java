import java.util.ArrayList;

public class MutexObject{

    String mutexName = new String();
    boolean holdingOrNot = false;
    int id = 0;
    ArrayList<KernelandProcess> processesAttached = new ArrayList<>();
}
