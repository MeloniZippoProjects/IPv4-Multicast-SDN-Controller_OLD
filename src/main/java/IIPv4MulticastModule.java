import java.util.Set;

public interface IIPv4MulticastModule {

    Set<String> getGroupsSet();
    void addGroup(String group);
    void addToGroup(String group, String host);
}
