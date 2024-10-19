import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import static java.lang.System.out;

public class PapaiNoel {
    private volatile boolean crencaCriancas = true;
    private final Semaphore descrenca = new Semaphore(0);
    private final static int ANO_FINAL = 2028;
    private AtomicInteger ano = new AtomicInteger(2024);
    private static Random generator = new Random();

    private final static int QTD_RENAS = 9;
    private final static int QTD_ELFOS = 10;
    private final static int QTD_ELFOS_NECESSARIA = 3;

    private final Semaphore filaElfos;
    private final CyclicBarrier barreiraTresElfos;
    private final CyclicBarrier trabalhoElfos;
    private final CyclicBarrier totalRenas;
    private final CyclicBarrier treno;
    private final Semaphore atencao;
    private final static int ULTIMA_RENA = 0;
    private final static int TERCEIRO_ELFO = 0;

    class Rena implements Runnable {
        int id;

        Rena(int id) { this.id = id; }

        public void run() {
            while (crencaCriancas) {
                try {
                    // aguarda a chegada do Natal
                    Thread.sleep(900 + generator.nextInt(200));

                    // aguarda todas as renas chegarem
                    int rena = totalRenas.await();
                    // a última rena a chegar adquire a atencao do Papai Noel
                    if (rena == ULTIMA_RENA) {
                        atencao.acquire();
                        out.println("--- Entrega para o Natal de " + ano + " ---");
                        if (ano.incrementAndGet() == ANO_FINAL)
                        {
                            crencaCriancas = false;
                            descrenca.release();
                        }
                    }

                    // as renas devem ir para o treno para realizarem o trabalho
                    rena = treno.await();
                    Thread.sleep(generator.nextInt(20));   // entregas

                    if (rena == ULTIMA_RENA) {
                        atencao.release();
                        out.println("--- Entregas do Natal finalizadas ---");
                    }
                } catch (InterruptedException e) {
                    // thread interrupted for program cleanup
                } catch (BrokenBarrierException e) {
                    // another thread in the barrier was interrupted
                }
            }
            out.println("Rena " + id + " se aposenta");
        }
    }

    class Elfo implements Runnable {
        int id;

        Elfo(int id) { this.id = id; }

        public void run() {
            try {
                Thread.sleep(generator.nextInt(2000));

                while (crencaCriancas) {
                    // apenas 3 elfos podem pedir ajuda por vez
                    filaElfos.acquire();
                    out.println("Elfo " + id + " precisa de ajuda");

                    // aguarda os 3 elfos
                    int elfo = barreiraTresElfos.await();

                    // o terceiro elfo pede a atencao do Papai Noel
                    if (elfo == TERCEIRO_ELFO)
                        atencao.acquire();

                    // aguarda todos os elfos receberem ajuda
                    Thread.sleep(generator.nextInt(500));
                    out.println("Elfo " + id + " ganhou ajuda");
                    trabalhoElfos.await();

                    if (elfo == TERCEIRO_ELFO)
                        atencao.release();

                    // libera a fila para outros elfos
                    filaElfos.release();

                    // simula o trabalho dos elfos
                    Thread.sleep(generator.nextInt(2000));
                }
            } catch (InterruptedException e) {
                // trata a interrupcao da thread
            } catch (BrokenBarrierException e) {
                // tratamento de excessao
            }
            out.println("Elfo " + id + " se aposenta");
        }
    }

    class EntregaBrinquedos implements Runnable {
        boolean brinquedosEntregues;
        EntregaBrinquedos() { brinquedosEntregues = false; }
        public void run() {
            brinquedosEntregues = !brinquedosEntregues;
            if (brinquedosEntregues)
                out.println("--- Todos os brinquedos entregues ---");
            else
                out.println("--- Todas as renas voltam para as férias ---");
        }
    }

    public PapaiNoel() {
        atencao = new Semaphore(1, true);
        filaElfos = new Semaphore(QTD_ELFOS_NECESSARIA, true);    // use a fair semaphore
        barreiraTresElfos = new CyclicBarrier(QTD_ELFOS_NECESSARIA,
                () -> out.println("--- Elfos estao batendo na porta ---"));
        trabalhoElfos = new CyclicBarrier(QTD_ELFOS_NECESSARIA,
                () -> out.println("--- ELFOS RETORNAM AO TRABALHO ---"));
        totalRenas = new CyclicBarrier(QTD_RENAS, new Runnable() {
            public void run() {
                out.println("---  Renas voltam para o Natal " + ano +" ---");
            }});
        treno = new CyclicBarrier(QTD_RENAS, new EntregaBrinquedos());

        ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < QTD_ELFOS; ++i)
            threads.add(new Thread(new Elfo(i)));
        for (int i = 0; i < QTD_RENAS; ++i)
            threads.add(new Thread(new Rena(i)));
        out.println("Natal no " + ano + " :");
        for (Thread t : threads)
            t.start();

        try {
            // wait until !descrencaCriancas
            descrenca.acquire();
            out.println("acabou o Papai noel");
            for (Thread t : threads)
                t.interrupt();
            for (Thread t : threads)
                t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        out.println("Fim");
    }
}