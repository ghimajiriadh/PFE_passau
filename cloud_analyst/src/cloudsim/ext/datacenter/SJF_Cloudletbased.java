package cloudsim.ext.datacenter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cloudsim.Cloudlet;
import cloudsim.CloudletList;
import cloudsim.DatacenterBroker;
import cloudsim.VMMAllocationPolicy;
import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;
import gridsim.PEList;


public class SJF_Cloudletbased extends VmLoadBalancer implements CloudSimEventListener {

	private Map<Integer, VirtualMachineState> vmStatesList;
	private CloudletList list;
	private int min,listsize;
	private Map<Integer, Integer> currentAllocationCounts;
	private static List<Cloudlet> cloudletList,cloudletListSJF;
	private DatacenterBroker broker;

	
	public SJF_Cloudletbased(DatacenterController dcb) {
		// TODO Auto-generated constructor stub
		this.vmStatesList = dcb.getVmStatesList();
		//list = new CloudletList();
		dcb.addCloudSimEventListener(this);
		this.vmStatesList = dcb.getVmStatesList();
		this.currentAllocationCounts = Collections.synchronizedMap(new HashMap<Integer, Integer>());
		cloudletList = dcb.getCloudletList();
		System.out.println(cloudletList.size());
		cloudletListSJF = new LinkedList<Cloudlet>();
		list = new CloudletList();
		try {
			this.broker = new DatacenterBroker(dcb.getDataCenterName());
			sendSJFcloudlist();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendSJFcloudlist() {
	    System.out.println("sssssssssssssss");
		// TODO Auto-generated method stub
		while (cloudletList.size()!=0){
			System.out.println("*************************88888888888");
		//min = Integer.MIN_VALUE;
			min = 0;
		for (int i=0;i<cloudletList.size();i++){
			if (cloudletList.get(i).getCloudletLength() < cloudletList.get(min).getCloudletLength())
				min = i;
		}
			cloudletListSJF.add(cloudletList.get(min));
			cloudletList.remove(min);	
		}
		//broker.submitCloudletList(cloudletListSJF);
		convert_list_to_cloudlist(cloudletListSJF);
	}

	//convert list of cloudlets to cloudletlist to send to broker.
	private void convert_list_to_cloudlist(List<Cloudlet> clist) {
		// TODO Auto-generated method stub
		/*Add every element of cloudletlistSJF to cloudletlist
		 * for (int i=0;i<clist.size();i++){
			list.add(clist.get(i));
		}*/
		// add list of cloudlets to cloudletlist
		list.addAll(clist);
		broker.submitCloudletList(list);
		
	}

	/**
	 * @return The VM id of a VM so that the number of active tasks on each VM is kept
	 * 			evenly distributed among the VMs.
	 */
	@Override
	public int getNextAvailableVm(){
		int vmId = -1;
		
		//Find the vm with least number of allocations
		
		//If all available vms are not allocated, allocated the new ones
		if (currentAllocationCounts.size() < vmStatesList.size()){
			for (int availableVmId : vmStatesList.keySet()){
				if (!currentAllocationCounts.containsKey(availableVmId)){
					vmId = availableVmId;
					break;
				}				
			}
		} else {
			int currCount;
			int minCount = Integer.MAX_VALUE;
			
			for (int thisVmId : currentAllocationCounts.keySet()){
				currCount = currentAllocationCounts.get(thisVmId);
				if (currCount < minCount){
					minCount = currCount;
					vmId = thisVmId;
				}
			}
		}	
		
		
		
		
		allocatedVm(vmId);
		
		return vmId;
		
	}
	

	public void cloudSimEventFired(CloudSimEvent e) {
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			
			Integer currCount = currentAllocationCounts.remove(vmId);
			if (currCount == null){
				currCount = 1;
			} else {
				currCount++;
			}
			
			currentAllocationCounts.put(vmId, currCount);
			
		} else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			Integer currCount = currentAllocationCounts.remove(vmId);
			if (currCount != null){
				currCount--;
				currentAllocationCounts.put(vmId, currCount);
			}
		}
	}
	


}
