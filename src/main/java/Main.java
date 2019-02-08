import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;
import org.apache.commons.lang3.ThreadUtils;
import sun.misc.ThreadGroupUtils;

import java.text.NumberFormat;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //create an object to  lock on it
        Object object = new Object();

        //1- do synchronization
        synchronized (object) {


            Observable ob = Observable
                    .intervalRange(1,100,1,1, TimeUnit.SECONDS)
                    .map(tick -> {

                                Runtime runtime = Runtime.getRuntime();
                                NumberFormat format = NumberFormat.getInstance();
                                StringBuilder sb = new StringBuilder();
                                long maxMemory = runtime.maxMemory();
                                long allocatedMemory = runtime.totalMemory();
                                long freeMemory = runtime.freeMemory();

                                sb.append("free memory: " + format.format(freeMemory / 1024) + "\n");
                                sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n");
                                sb.append("max memory: " + format.format(maxMemory / 1024) + "\n");
                                sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n");
                                return sb.toString();
                            }

                    )
                    .subscribeOn(Schedulers.newThread())
                    .doOnComplete(()->{

                       synchronized (object){
                           //3- after 100 run of this code rx thread notify main thread and process done
                           object.notify();
                       }
                    })
                    ;
            ob.subscribe(s -> {
                System.out.println(s);
            });
            //2- main thread lock here and wait until some thread notify it on object
            object.wait();
        }



    }

}
