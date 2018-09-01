package cloudsim.ext.datacenter;

import java.util.*;
import java.util.Collections;
import java.util.List;
import cloudsim.ext.datacenter.VirtualMachineState;
import cloudsim.*;
import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
import gridsim.PEList;

import java.util.Map;

/**
 * This class implements {@link VmLoadBalancer} with a Round Robin policy.
 *
 * @author ghimeji riadh
 *
 */
public class heft extends VmLoadBalancer implements CloudSimEventListener  {
    private Map<Integer, Integer> currentAllocationCounts;
    private static List<Cloudlet> cloudletlist, cloudletlistsjf;
    private Map<Integer, VirtualMachineState> vmStatesList;
    private int listsize,min;
    private LinkedList<VirtualMachine> vmList;

   // private Map<Integer, VirtualMachineState> vmStatesList;
    private int currVm = -1;

    public heft(DatacenterController dcb){
        dcb.addCloudSimEventListener(this);
        this.vmStatesList = dcb.getVmStatesList();
        this.currentAllocationCounts = Collections.synchronizedMap(new HashMap<Integer, Integer>());
        this.cloudletlist = dcb.getCloudletList();
        this.cloudletlistsjf = new LinkedList<>();
        cloudletlistsjf = cloudletlist;
        vmList = new LinkedList<VirtualMachine>();
    }

    /* (non-Javadoc)
     * @see cloudsim.ext.VMLoadBalancer#getVM()
     */

    public int getNextAvailableVm(){
        for(Cloudlet c : cloudletlist){
            System.out.print(c.getCloudletId());
        }
        System.out.println(" *****"+ cloudletlist.size());

        currVm++;

        if (currVm >= vmStatesList.size()){
            currVm = 0;
        }

        allocatedVm(currVm);

        return currVm;

    }

    @Override
    public void cloudSimEventFired(CloudSimEvent e) {
        if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
            int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
            vmStatesList.put(vmId, VirtualMachineState.BUSY);
        } else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
            int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
            vmStatesList.put(vmId, VirtualMachineState.AVAILABLE);
        }
    }
}
