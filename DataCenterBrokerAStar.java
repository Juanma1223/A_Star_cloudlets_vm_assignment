package org.cloudbus.cloudsim.brokers;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.vms.Vm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class DataCenterBrokerAStar extends DatacenterBrokerAbstract {

    /**
     * Index of the last VM selected from the {@link #getVmExecList()}
     * to run some Cloudlet.
     */
    private int lastSelectedVmIndex;

    /**
     * Index of the last Datacenter selected to place some VM.
     */
    private int lastSelectedDcIndex;

    // Mocked vm and cloudlet lists
    private ArrayList<MockVm> vms = new ArrayList<MockVm>();
    private ArrayList<MockCloudlet> cloudlets = new ArrayList<MockCloudlet>();
    // 
    private float maxExecTime = 0;

    /**
     * Creates a new DatacenterBroker.
     *
     * @param simulation the CloudSim instance that represents the simulation the
     *                   Entity is related to
     */
    public DataCenterBrokerAStar(final CloudSim simulation) {
        this(simulation, "");
    }
    
    /**
     * Creates a DatacenterBroker giving a specific name.
     *
     * @param simulation the CloudSim instance that represents the simulation the
     *                   Entity is related to
     * @param name       the DatacenterBroker name
     */
    public DataCenterBrokerAStar(final CloudSim simulation, final String name) {
        super(simulation, name);
        // Map cloudlets and vms to mock classes for an easier manipulation
        this.mapCloudlets(this.getCloudletSubmittedList());
        this.mapVms(this.getVmWaitingList());
        // Bind sorted cloudlets with their corresponding real vm
        this.bindVms(this.cloudlets)
        this.lastSelectedVmIndex = -1;
        this.lastSelectedDcIndex = -1;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * <b>It applies a Round-Robin policy to cyclically select
     * the next Datacenter from the list. However, it just moves
     * to the next Datacenter when the previous one was not able to create
     * all {@link #getVmWaitingList() waiting VMs}.
     * </p>
     *
     * <p>
     * This policy is just used if the selection of the closest Datacenter is not
     * enabled.
     * Otherwise, the {@link #closestDatacenterMapper(Datacenter, Vm)} is used
     * instead.
     * </p>
     *
     * @param lastDatacenter {@inheritDoc}
     * @param vm             {@inheritDoc}
     * @return {@inheritDoc}
     * @see DatacenterBroker#setDatacenterMapper(java.util.function.BiFunction)
     * @see #setSelectClosestDatacenter(boolean)
     */
    @Override
    protected Datacenter defaultDatacenterMapper(final Datacenter lastDatacenter, final Vm vm) {
        if (getDatacenterList().isEmpty()) {
            throw new IllegalStateException("You don't have any Datacenter created.");
        }

        if (lastDatacenter != Datacenter.NULL) {
            return getDatacenterList().get(lastSelectedDcIndex);
        }

        /*
         * If all Datacenter were tried already, return Datacenter.NULL to indicate
         * there isn't a suitable Datacenter to place waiting VMs.
         */
        if (lastSelectedDcIndex == getDatacenterList().size() - 1) {
            return Datacenter.NULL;
        }

        return getDatacenterList().get(++lastSelectedDcIndex);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * <b>It applies a Round-Robin policy to cyclically select
     * the next Vm from the {@link #getVmWaitingList() list of waiting VMs}.
     * </p>
     *
     * @param cloudlet {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Vm defaultVmMapper(final Cloudlet cloudlet) {
        if (cloudlet.isBoundToVm()) {
            return cloudlet.getVm();
        }

        if (getVmExecList().isEmpty()) {
            return Vm.NULL;
        }

        /*
         * If the cloudlet isn't bound to a specific VM or the bound VM was not created,
         * cyclically selects the next VM on the list of created VMs.
         */
        lastSelectedVmIndex = ++lastSelectedVmIndex % getVmExecList().size();
        return getVmFromCreatedList(lastSelectedVmIndex);
    }

    private void sortCloudlets() {
        for (int i = 0; i < cloudlets.size(); i++) {
            int bestVm = minimumExecVm(cloudlets.get(i));
            vms.get(bestVm).addCloudlet(cloudlets.get(i));
        }
        // Print result
        for (int i = 0; i < vms.size(); i++) {
            System.out.println("Cloudlets vm" + i);
            vms.get(i).printCloudlets();
        }
    }

    // Get the one vm that doesn't exceed current max execution time out of all vms
    // in case there is no option but to exceed max execution time, return the least
    // increasing
    // max execution time vm
    private int minimumExecVm(MockCloudlet cloudlet) {
        int minVmIndex = 0;
        double currMin = Double.POSITIVE_INFINITY;
        for (int i = 0; i < vms.size(); i++) {
            // Calculate cloudlet's execution time on this vm
            float vmExecTime = cloudlet.mips / vms.get(i).getExecPower();
            float execTime = vmExecTime + vms.get(i).currentExecTime;
            if (execTime < currMin) {
                minVmIndex = i;
                currMin = execTime;
            }
        }
        // Return best vm to execute the cloudlet on
        return minVmIndex;
    }

    // Internal usage mock cloudlets, easier to manipulate
    public class MockCloudlet {
        public int pid;
        public float mips;

        public float getMips() {
            return mips;
        }
    }

    // Internal usage mock vm, easier to manipulate
    public class MockVm {
        public ArrayList<MockCloudlet> cloudlets = new ArrayList<MockCloudlet>();
        public float currentExecTime = 0;
        public int pes = 1;
        public float pesmips = 1000;

        public void addCloudlet(MockCloudlet cloudlet) {
            this.cloudlets.add(cloudlet);
            this.currentExecTime = this.currentExecTime + (cloudlet.mips / getExecPower());
        }

        public float getExecPower() {
            return pes * pesmips;
        }

        public void printCloudlets() {
            for (int i = 0; i < cloudlets.size(); i++) {
                System.out.println(cloudlets.get(i).mips);
            }
        }
    }
}
