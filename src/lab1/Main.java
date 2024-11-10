package lab1;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Random;
//відправник
class Sender implements Runnable {
    private BlockingQueue<String> mailQueue;
    private String name;
    public Sender(BlockingQueue<String> mailQueue, String name) {
        this.mailQueue = mailQueue;
        this.name = name;
    }
    @Override
    public void run() {
        try {
            String mail = "Mail from " + name;
            System.out.println(name + " is sending: " + mail);
            mailQueue.put(mail);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(name + " was interrupted while sending.");
        }
    }
}
//працівник
class PostalWorker implements Runnable {
    private BlockingQueue<String> mailQueue;
    private boolean open;
    public PostalWorker(BlockingQueue<String> mailQueue) {
        this.mailQueue = mailQueue;
        this.open = true;
    }
    public void closePostOffice() {
        this.open = false;
    }
    @Override
    public void run() {
        while (open || !mailQueue.isEmpty()) {
            try {
                String mail = mailQueue.poll(5, TimeUnit.SECONDS);
                if (mail != null) {
                    System.out.println("Postal worker is processing: " + mail);
                    Thread.sleep(1000);
                    new Thread(new Delivery(mail)).start();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Postal worker was interrupted.");
            }
        }
        System.out.println("Post office is closed.");
    }
}
//доставка
class Delivery implements Runnable {
    private String mail;
    private Random random = new Random();
    public Delivery(String mail) {
        this.mail = mail;
    }
    @Override
    public void run() {
        int deliveryTime = random.nextInt(5000) + 1000;
        try {
            System.out.println(mail + " is on its way. Estimated time: " + deliveryTime + " ms");
            Thread.sleep(deliveryTime);
            System.out.println(mail + " has been delivered.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(mail + " delivery was interrupted.");
        }
    }
}
public class Main {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> mailQueue = new LinkedBlockingQueue<>();
        //працівник
        PostalWorker postalWorker = new PostalWorker(mailQueue);
        Thread workerThread = new Thread(postalWorker);
        //відправник
        Thread sender1 = new Thread(new Sender(mailQueue, "lab1.Sender 1"));
        Thread sender2 = new Thread(new Sender(mailQueue, "lab1.Sender 2"));
        Thread sender3 = new Thread(new Sender(mailQueue, "lab1.Sender 3"));
        //запуск
        workerThread.start();
        sender1.start();
        sender2.start();
        sender3.start();
        //кінець відправника
        sender1.join();
        sender2.join();
        sender3.join();
        //кінець робочого дня
        postalWorker.closePostOffice();
        workerThread.join();
    }
}
