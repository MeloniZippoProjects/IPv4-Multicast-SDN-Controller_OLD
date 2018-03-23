import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.util.FlowModUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.*;

import java.util.*;



public class IPv4MulticastModule implements IOFMessageListener, IFloodlightModule, IIPv4MulticastModule {

    protected IFloodlightProviderService floodlightProvider;
    protected IRestApiService restApiService;

    protected SubnetUtils unicastPool;
    protected SubnetUtils multicastPool;

    protected Map<String, Set<String>> multicastGroups;
    protected Map<String, Integer> OFGroupsIds;

    // Rule timeouts
    private static short IDLE_TIMEOUT = 10; // in seconds
    private static short HARD_TIMEOUT = 20; // every 20 seconds drop the entry

    public Set<String> getGroupsSet() {
        return multicastGroups.keySet();
    }

    public void addGroup(String group)
    {
        if(!multicastGroups.containsKey(group))
            multicastGroups.put(group, new HashSet<String>());
    }

    public void addToGroup(String group, String host)
    {
        if(multicastGroups.containsKey(group)) {
            multicastGroups.get(group).add(host);
        }
        else
        {
            //todo: exception?
        }
    }

    public Command receive(IOFSwitch iofSwitch, OFMessage ofMessage, FloodlightContext floodlightContext) {
        Ethernet eth =
                IFloodlightProviderService.bcStore.get(floodlightContext,
                        IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

        //consider only ipv4 packets
        if(eth.getEtherType() == EthType.IPv4)
        {
            IPv4 payload = (IPv4) eth.getPayload();
            IPv4Address destinationAddress = payload.getDestinationAddress();

            //todo: check if toString is correct output
            //if set contains the dest address, it is a valid multicast group
            if(getGroupsSet().contains(destinationAddress.toString()))
            {
                if(!OFGroupsIds.containsKey(destinationAddress.toString()))
                    createNewOFGroup(iofSwitch, destinationAddress.toString());

                //get available action types
                OFActions actions = iofSwitch.getOFFactory().actions();

                //create flow-mod for this packet in
                OFFlowAdd.Builder flowModBuilder = iofSwitch.getOFFactory().buildFlowAdd();
                flowModBuilder.setIdleTimeout(IDLE_TIMEOUT);
                flowModBuilder.setHardTimeout(HARD_TIMEOUT);
                flowModBuilder.setBufferId(OFBufferId.NO_BUFFER);
                flowModBuilder.setOutPort(OFPort.ANY);
                flowModBuilder.setCookie(U64.of(0));
                flowModBuilder.setPriority(FlowModUtils.PRIORITY_MAX);

                ArrayList<OFAction> actionList = new ArrayList<OFAction>();
                int groupId = OFGroupsIds.get(destinationAddress.toString());
                actionList.add(actions.buildGroup().setGroup(OFGroup.of(groupId)).build());

                flowModBuilder.setActions(actionList);

                //create matcher for this multicast ip
                Match.Builder matchBuilder = iofSwitch.getOFFactory().buildMatch();
                matchBuilder.setExact(MatchField.ETH_TYPE, EthType.IPv4)
                        .setExact(MatchField.IPV4_DST, destinationAddress);

                flowModBuilder.setMatch(matchBuilder.build());
                iofSwitch.write(flowModBuilder.build());
            }
        }

        return Command.CONTINUE;
    }

    private void createNewOFGroup(IOFSwitch iofSwitch, String multicastAddress) {
        Set<String> hosts = multicastGroups.get(multicastAddress);
        int groupId;
        if(!OFGroupsIds.isEmpty())
             groupId = Collections.max(OFGroupsIds.values()) + 1;
        else
            groupId = 1;

        OFGroupAdd multicastActionGroup = iofSwitch.getOFFactory().buildGroupAdd()
                .setGroup(OFGroup.of(groupId))    //todo: is it an id? make them unique to avoid overwriting?
                .setGroupType(OFGroupType.ALL)
                .build();

        List<OFBucket> buckets = multicastActionGroup.getBuckets();

        //get available action types
        OFActions actions = iofSwitch.getOFFactory().actions();
        //Open Flow extendable matches, needed to create actions
        OFOxms oxms = iofSwitch.getOFFactory().oxms();

        for(String host : hosts)
        {
            ArrayList<OFAction> actionList = new ArrayList<OFAction>();
            OFActionSetField forwardAction = actions.buildSetField()
                    .setField(
                            oxms.buildIpv4Dst()
                                    .setValue(IPv4Address.of(host))
                                    .build()
                    ).build();
            actionList.add(forwardAction);

            OFBucket forwardPacket = iofSwitch.getOFFactory().buildBucket()
                    .setActions(actionList)
                    .setWatchGroup(OFGroup.ANY)
                    .setWatchPort(OFPort.ANY)
                    .build();

            buckets.add(forwardPacket);
        }

        iofSwitch.write(multicastActionGroup);
    }

    public String getName() {
        return "IPv4 Multicast Module";
    }

    public boolean isCallbackOrderingPrereq(OFType ofType, String s) {
        return false;
    }

    public boolean isCallbackOrderingPostreq(OFType ofType, String s) {
        return false;
    }

    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }

    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IRestApiService.class);
        return l;
    }

    public void init(FloodlightModuleContext floodlightModuleContext) throws FloodlightModuleException {
        floodlightProvider = floodlightModuleContext.getServiceImpl(IFloodlightProviderService.class);
        restApiService = floodlightModuleContext.getServiceImpl(IRestApiService.class);

        //todo: maybe change it in a configuration file
        unicastPool = new SubnetUtils("192.168.0.0/24");
        multicastPool = new SubnetUtils("192.168.1.0/28");
    }

    public void startUp(FloodlightModuleContext floodlightModuleContext) throws FloodlightModuleException {

    }
}
