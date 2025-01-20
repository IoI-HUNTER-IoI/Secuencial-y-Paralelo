package cardsorter;

import java.util.*;
import java.util.concurrent.*;

// Los resultados obtenidos son los siguientes:
/*
        Tiempo secuencial:         Tiempo paralelo: 
                1484 ms                 475 ms
                1469 ms                 480 ms
                1510 ms                 495 ms
                1474 ms                 495 ms
                1513 ms                 488 ms
                1503 ms                 482 ms
                1494 ms                 493 ms
                1487 ms                 492 ms

Con esto podemos concluir que por un proceso paralelo el tiempo de ordenamiento 
es menor que un proceso secuencial, es decir, ordena las cartas en un menor tiempo
*/
public class CardSorter {

    // TENDREMOS NUESTRA BARAJA DE 4,000,000 DE CARTAS
    public static List<Naipe> CrearBaraja(int cantidad) {
        List<Naipe> baraja = new ArrayList<>(cantidad);
        String[] figura = {"Corazones", "Treboles", "Picas", "Diamantes"};
        // Se resta 2 porque esos son los comodines
        for (int i = 0; i < cantidad - 2; i++) {
            baraja.add(new Naipe(figura[i % 4], (i % 13) + 1));
        }
        // Agregar comodines
        baraja.add(new Naipe("Comodin", 0));
        baraja.add(new Naipe("Comodin", 0));
        Collections.shuffle(baraja);
        return baraja;
    }

    // ESTE SERA NUESTRO PROCESO SECUENCIAL
    public static List<Naipe> OrdenamientoSecuencial(List<Naipe> baraja) {
        List<Naipe> ordenarBaraja = new ArrayList<>(baraja);
        Collections.sort(ordenarBaraja);
        return ordenarBaraja;
    }

    // ESTE SERA NUUESTRO PROCESO PARALELO
    public static List<Naipe> OrdenamientoParalelo(List<Naipe> baraja) throws InterruptedException, ExecutionException {
        // Calculamos nuestro numero de procesadores, en mi caso tengo 12
        int procesadores = Runtime.getRuntime().availableProcessors();
        ExecutorService ejecutar = Executors.newFixedThreadPool(procesadores);
        // dividimos la baraja segun el numero de procesadores que tengamos
        int TamañoParte = baraja.size() / procesadores;

        List<Future<List<Naipe>>> futures = new ArrayList<>();
        for (int i = 0; i < procesadores; i++) {
            int inicio = i * TamañoParte;
            
            int fin;
            // Verificamos si este es el ultimo hilo
            if(i == procesadores - 1)
                fin = baraja.size();
            else
                fin = (i + 1) * TamañoParte;
                
            futures.add(ejecutar.submit(() -> {
                List<Naipe> sublista = baraja.subList(inicio, fin);
                sublista.sort(null);
                return sublista;
            }));
        }

        List<Naipe> ordenarbaraja = new ArrayList<>();
        for (Future<List<Naipe>> future : futures) {
            ordenarbaraja.addAll(future.get());
        }
        ejecutar.shutdown();
        ordenarbaraja.sort(null); // Combinar resultados
        return ordenarbaraja;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int TamañoBaraja = 4000000;
        List<Naipe> baraja = CrearBaraja(TamañoBaraja);

        // Secuencial
        long inicioSecuencial = System.currentTimeMillis();
        List<Naipe> ResultadoSecuencial = OrdenamientoSecuencial(baraja);
        long terminaSecuencial = System.currentTimeMillis();
        System.out.println("Tiempo secuencial: " + (terminaSecuencial - inicioSecuencial) + " ms");

        // Paralelo
        long inicioParalelo = System.currentTimeMillis();
        List<Naipe> ResultadoParalelo = OrdenamientoParalelo(baraja);
        long terminaParalelo = System.currentTimeMillis();
        System.out.println("Tiempo paralelo: " + (terminaParalelo - inicioParalelo) + " ms");

        // Validación
        System.out.println("Los mazos estan ordenados igualmente: " + ResultadoSecuencial.equals(ResultadoParalelo));
    }
}

class Naipe implements Comparable<Naipe> {
    private final String figura;
    private final int valor; // 1 (A) - 13 (K), 0 para comodines

    public Naipe(String figura, int valor) {
        this.figura = figura;
        this.valor = valor;
    }

    public String getFigura() {
        return figura;
    }

    public int getValor() {
        return valor;
    }

    @Override
    public int compareTo(Naipe otro) {
        List<String> OrdenFigura = Arrays.asList("Corazones", "Treboles", "Picas", "Diamantes", "Comodin");
        int CompararFiguras = Integer.compare(OrdenFigura.indexOf(this.figura), OrdenFigura.indexOf(otro.figura));        
        if(CompararFiguras != 0 )
            return CompararFiguras;
        else
            return Integer.compare(this.valor, otro.valor);
    }
}