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
    public String Create(String fmJson)
    {
        IIPv4MulticastModule multicastModule =
                (IIPv4MulticastModule)getContext().getAttributes().
                        get(IIPv4MulticastModule.class.getCanonicalName());



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


