import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.web.ControllerSwitchesResource;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.types.DatapathId;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MulticastGroupsResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(MulticastGroupsResource.class);

    //todo: parameters and return values are placeholders

    @Post("create")
    public String Create()
    {
        return "";
    }

    //Use delete http command?
    @Post("delete")
    public void Delete(String group)
    {
        return;
    }

    @Post("join")
    public void Join(String host, String group)
    {
        return;
    }

    @Post("unjoin")
    public void Unjoin(String host, String group)
    {
        return;
    }

    @Get("list")
    public String List()
    {
        return "";
    }
}

//This is an EXAMPLE
public class ListStaticEntriesResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(ListStaticEntriesResource.class);

    @Get("json")
    public SFPEntryMap ListStaticFlowEntries() {
        IStaticEntryPusherService sfpService =
                (IStaticEntryPusherService)getContext().getAttributes().
                        get(IStaticEntryPusherService.class.getCanonicalName());

        String param = (String) getRequestAttributes().get("switch");
        if (log.isDebugEnabled())
            log.debug("Listing all static flow/group entires for switch: " + param);

        if (param.toLowerCase().equals("all")) {
            return new SFPEntryMap(sfpService.getEntries());
        } else {
            try {
                Map<String, Map<String, OFMessage>> retMap = new HashMap<String, Map<String, OFMessage>>();
                retMap.put(param, sfpService.getEntries(DatapathId.of(param)));
                return new SFPEntryMap(retMap);

            } catch (NumberFormatException e){
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST, ControllerSwitchesResource.DPID_ERROR);
            }
        }
        return null;
    }
}


