import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class A_Star_cloudlets {
    // Cloudlet list
    static class Cloudlet {
        public int pid;
        public float mips;

        public float getMips(){
            return mips;
        }
    }

    static class Vm {
        public ArrayList<Cloudlet> cloudlets = new ArrayList<Cloudlet>();
        public float currentExecTime = 0;
        public int pes = 1;
        public float pesmips = 1000;

        public void addCloudlet(Cloudlet cloudlet) {
            this.cloudlets.add(cloudlet);
            this.currentExecTime = this.currentExecTime + (cloudlet.mips/getExecPower());
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

    static ArrayList<Vm> vms = new ArrayList<Vm>();
    static ArrayList<Cloudlet> cloudlets = new ArrayList<Cloudlet>();
    static float maxExecTime = 0;
    
    public static void main(String[] args) {
        generateVms();
        generateCloudlets();
        sortCloudlets();
    }

    static void generateVms() {
        // Create vm list
        // Create two sample vms
        Vm vm1 = new Vm();
        Vm vm2 = new Vm();
        vms.add(vm1);
        vms.add(vm2);
    }

    static void generateCloudlets() {
        // Generate random test cloudlets
        for (int i = 0; i < 5; i++) {
            Cloudlet newCloudlet = new Cloudlet();
            float mips = new Random().nextInt(10000);
            newCloudlet.mips = mips;
            cloudlets.add(newCloudlet);
        }
        Collections.sort(cloudlets, Comparator.comparing(Cloudlet::getMips));
        Collections.reverse(cloudlets);
    }

    static void sortCloudlets() {
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
    static int minimumExecVm(Cloudlet cloudlet) {
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

}
