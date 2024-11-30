import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Sistema {

    private static Map<Integer, List<Integer>> grafo = new HashMap<>();
    private static Map<Integer, Integer> alocacao = new HashMap<>();

    public static void main(String[] args) throws IOException {

//        if (args.length != 3) {
//            System.out.println("Uso: <comando> <algoritmo> <sala>");
//            return;
//        }

//        int algoritmo = Integer.parseInt(args[1]);
//        String arquivoSala = args[2];


        // Obtendo as entradas do programa (TESTE)
        int algoritmo = 2;
        String arquivoSala = "src/sala10.txt";

        for (int i = 1; i <= 12 ; i++) {
            arquivoSala = "src/sala" +i+ ".txt";
            carregarSala(arquivoSala);
            algoritmoBasico(algoritmo);
            grafo.clear();
            alocacao.clear();
        }

        if (algoritmo < 1 || algoritmo > 4) {
            System.out.println("Algoritmo inválido.");
        } else {
//            carregarSala(arquivoSala);
//            algoritmoBasico(algoritmo);
        }
    }

    /**
     * Método responsável por popular o hashmap que representa o grafo
     * @param arquivoSala
     */
    private static void carregarSala(String arquivoSala) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(arquivoSala));
        String linha;
        while ((linha = br.readLine()) != null) {
            if (linha.startsWith("e")) {
                String partes[] = linha.split(" ");
                int mesa = Integer.parseInt(partes[1]);
                int vizinho = Integer.parseInt(partes[2]);

                //Percorre cada linha do arquivo para vincular o vértice ao seu vizinho e vice-versa
                grafo.computeIfAbsent(mesa, k -> new ArrayList<>()).add(vizinho);
                grafo.computeIfAbsent(vizinho, k -> new ArrayList<>()).add(mesa);
            }
        }
    }

    /**
     * Realiza o processamento base de alocação de tipos de prova
     * @param estrategiaPontuacao
     */
    private static void algoritmoBasico(int estrategiaPontuacao) {
        //Lista para armazenar os vértices que ainda não possuem provas
        List<Integer> naoAlocados = new ArrayList<>(grafo.keySet());

        while (!naoAlocados.isEmpty()) {
            //Obter vértice com a maior pontuação
            int melhor = calculaMaiorPontuacao(naoAlocados, estrategiaPontuacao);
            //Obter o próximo tipo de prova ainda não alocado
            int tipo = alocarTipo(melhor);
            //Alocar tipo de prova ao vértice
            alocacao.put(melhor, tipo);

            //Retirar o vértice da lista de não alocados
            naoAlocados.remove((Integer) melhor);
        }

        //Para determinar o número de tipos de provas alocados, usamos um set (conjunto), pois ele desconsidera valores duplicados
        Set<Integer> tiposProva = new HashSet<>(alocacao.values());
        System.out.println(tiposProva.size());
    }

    /**
     * Calcula o vértice de maior pontuação
     * @param vertices Lista de vértices a serem comparados
     * @param estrategiaPontuacao Estratégia utilizada para a verificação (1 - Grau; 2 - Vizinhos com prova; 3 - Vizinhos sem prova)
     * @return int
     */
    private static int calculaMaiorPontuacao(List<Integer> vertices, int estrategiaPontuacao) {
        int melhor = vertices.getFirst();
        double pontuacaoVertice = 0;
        double pontuacaoMelhor = 0;
        for (Integer v : vertices) {
            switch (estrategiaPontuacao) {
                case 1:
                    pontuacaoVertice = pontuacaoGrau(v);
                    pontuacaoMelhor = pontuacaoGrau(melhor);
                    break;
                case 2:
                    pontuacaoVertice = pontuacaoVizinhosComProva(v);
                    pontuacaoMelhor = pontuacaoVizinhosComProva(melhor);
                    break;
                case 3:
                    pontuacaoVertice = pontuacaoVizinhosSemProva(v);
                    pontuacaoMelhor = pontuacaoVizinhosSemProva(melhor);
                    break;
                case 4:
                    pontuacaoVertice = pontuacaoAlgoritmoA4(v);
                    pontuacaoMelhor = pontuacaoAlgoritmoA4(melhor);
                    break;
            }

            if (pontuacaoVertice > pontuacaoMelhor) {
                melhor = v;
            }
        }
        return melhor;
    }

    /**
     * Retorna o próximo tipo de prova ainda não alocado para os vizinhos do vértice informado no parâmetro
     * @param mesa
     * @return int
     */
    private static int alocarTipo(int mesa) {
        Set<Integer> tiposUsados = new HashSet<>();
        //Para cada vizinho da mesa, verifica se a alocação já foi feita. Caso tenha sido feita, armazena o tipo usado
        for (int vizinho : grafo.get(mesa)) {
            if (alocacao.containsKey(vizinho)) {
                tiposUsados.add(alocacao.get(vizinho));
            }
        }
        int tipo = 1;
        //Calcula o próximo tipo (em ordem crescente) ainda não alocado
        while (tiposUsados.contains(tipo)) tipo++;
        return tipo;
    }

    /**
     * Calcula a pontuação do vértice com base no seu grau
     * @param mesa
     * @return int
     */
    private static int pontuacaoGrau(int mesa) {
        return grafo.get(mesa).size();
    }

    /**
     * Calcula a pontuação do vértice com base no número de vizinhos com prova
     * @param mesa
     * @return int
     */
    private static int pontuacaoVizinhosComProva(int mesa) {
        int count = 0;
        for (int vizinho : grafo.get(mesa)) {
            if (alocacao.containsKey(vizinho)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calcula a pontuação do vértice com base no número de vizinhos sem prova
     * @param mesa
     * @return int
     */
    private static int pontuacaoVizinhosSemProva(int mesa) {
        int count = 0;
        for (int vizinho : grafo.get(mesa)) {
            if (!alocacao.containsKey(vizinho)) {
                count++;
            }
        }
        return count;
    }

    private static double pontuacaoAlgoritmoA4(int mesa) {
        // Estado do grafo (quantidade de vértices alocados e não alocados)
        int totalVertices = grafo.size();
        int alocados = alocacao.size();
        int naoAlocados = totalVertices - alocados;

        // Pesos adaptativos
        double alfa = 1.0;  // Peso para o grau do vértice
        double beta = 2.0 * naoAlocados / totalVertices;  // Peso dinâmico para vizinhos sem prova
        double gamma = 1.5 * alocados / totalVertices;   // Peso dinâmico para vizinhos com prova

        int grau = pontuacaoGrau(mesa);
        int vizinhosComProva = pontuacaoVizinhosComProva(mesa);
        int vizinhosSemProva = pontuacaoVizinhosSemProva(mesa);

        // Fórmula combinada
        return alfa * grau + beta * vizinhosSemProva - gamma * vizinhosComProva;
    }
}