import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe para processamento e análise de dados numéricos.
 * Contém funções para filtragem, cálculo estatístico e normalização.
 */
public class DataProcessor {

    /**
     * Filtra os valores que estão dentro do intervalo [min, max].
     * Útil para limpar dados e focar em valores relevantes.
     * @param dados Lista de dados (Double) a serem filtrados
     * @param min Limite mínimo do intervalo
     * @param max Limite máximo do intervalo
     * @return Lista filtrada contendo apenas valores dentro do intervalo
     */
    public List<Double> filtrarDados(List<Double> dados, double min, double max) {
        List<Double> filtrados = new ArrayList<>();
        for (double d : dados) {
            if (d >= min && d <= max) filtrados.add(d);
        }
        return filtrados;
    }

    /**
     * Calcula a média aritmética simples dos dados fornecidos.
     * Essencial para ter uma medida central dos dados.
     * @param dados Lista de valores numéricos
     * @return Média dos valores, ou 0 se lista vazia
     */
    public double media(List<Double> dados) {
        double soma = 0;
        for (double d : dados) soma += d;
        return dados.isEmpty() ? 0 : soma / dados.size();
    }

    /**
     * Calcula o desvio padrão dos dados, medindo dispersão em relação à média.
     * Utilizado para entender variabilidade dos dados.
     * @param dados Lista de valores numéricos
     * @return Desvio padrão, ou 0 para lista vazia
     */
    public double desvioPadrao(List<Double> dados) {
        double m = media(dados);
        double soma = 0;
        for (double d : dados) soma += Math.pow(d - m, 2);
        return dados.isEmpty() ? 0 : Math.sqrt(soma / dados.size());
    }

    /**
     * Normaliza os dados para o intervalo [0,1].
     * Importante para padronizar escalas e facilitar comparações.
     * @param dados Lista de valores numéricos
     * @return Lista de valores normalizados
     */
    public List<Double> normalizar(List<Double> dados) {
        if (dados.isEmpty()) return new ArrayList<>();
        double max = Collections.max(dados);
        double min = Collections.min(dados);
        // Prevenção de divisão por zero
        if (max == min) return dados.stream().map(d -> 0.5).collect(Collectors.toList());
        List<Double> normalizados = new ArrayList<>();
        for (double d : dados) normalizados.add((d - min) / (max - min));
        return normalizados;
    }

    /**
     * Calcula a mediana dos dados.
     * Fornece valor central com resistência a valores extremos.
     * @param dados Lista de dados numéricos
     * @return Mediana, ou 0 se lista vazia
     */
    public double mediana(List<Double> dados) {
        if (dados.isEmpty()) return 0;
        List<Double> dadosOrdenados = new ArrayList<>(dados);
        Collections.sort(dadosOrdenados);
        int meio = dadosOrdenados.size() / 2;
        if (dadosOrdenados.size() % 2 == 0) {
            return (dadosOrdenados.get(meio - 1) + dadosOrdenados.get(meio)) / 2;
        } else {
            return dadosOrdenados.get(meio);
        }
    }

    /**
     * Detecta e remove outliers usando o método do intervalo interquartil.
     * Melhora a qualidade da análise removendo valores discrepantes.
     * @param dados Lista de dados numéricos
     * @return Dados filtrados sem outliers
     */
    public List<Double> removerOutliers(List<Double> dados) {
        if (dados.size() < 4) return new ArrayList<>(dados); // Não suficiente para quartis
        List<Double> sorted = new ArrayList<>(dados);
        Collections.sort(sorted);
        double q1 = sorted.get(sorted.size() / 4);
        double q3 = sorted.get(3 * sorted.size() / 4);
        double iqr = q3 - q1;
        double limiteInferior = q1 - 1.5 * iqr;
        double limiteSuperior = q3 + 1.5 * iqr;

        List<Double> filtrados = new ArrayList<>();
        for (double d : dados) {
            if (d >= limiteInferior && d <= limiteSuperior) filtrados.add(d);
        }
        return filtrados;
    }
}

/**
 * Classe encarregada da visualização de dados em formato textual de barras.
 * Útil para gráficos simples rápidos na console.
 */
public class Grafico {

    /**
     * Plota um gráfico de barras horizontal baseado em valores normalizados.
     * Cada barra usa '|' repetidos proporcionalmente ao valor.
     * @param dados Lista de valores normalizados de 0 a 1
     * @param titulo Título do gráfico
     */
    public void plotar(List<Double> dados, String titulo) {
        System.out.println("Gráfico: " + titulo);
        for (int i = 0; i < dados.size(); i++) {
            // Max 40 barras para melhor visualização
            int barras = (int) (dados.get(i) * 40);
            System.out.print(i + ": ");
            for (int j = 0; j < barras; j++) System.out.print("|");
            System.out.println(" " + String.format("%.3f", dados.get(i)));
        }
    }
}

/**
 * Classe que apresenta um resumão estatístico dos dados.
 * Auxilia o usuário a entender características principais dos dados.
 */
public class Dashboard {

    /**
     * Exibe média, mediana, desvio padrão, dados normalizados e dados originais.
     * @param dados Lista de dados numéricos
     * @param dp Instância da classe DataProcessor para cálculos
     */
    public void exibirResumo(List<Double> dados, DataProcessor dp) {
        System.out.println("---- Resumo Estatístico ----");
        System.out.println("Dados originais: " + dados);
        System.out.println("Média: " + String.format("%.3f", dp.media(dados)));
        System.out.println("Mediana: " + String.format("%.3f", dp.mediana(dados)));
        System.out.println("Desvio padrão: " + String.format("%.3f", dp.desvioPadrao(dados)));

        List<Double> semOutliers = dp.removerOutliers(dados);
        System.out.println("Dados sem outliers: " + semOutliers);

        List<Double> normalizados = dp.normalizar(semOutliers);
        System.out.println("Dados normalizados: " + normalizados);
    }
}

/**
 * Classe utilitária para gerar dados fake para testes e análises.
 */
public class Utils {

    /**
     * Gera uma lista de dados aleatórios no intervalo especificado.
     * @param n Quantidade de dados
     * @param min Valor mínimo possível
     * @param max Valor máximo possível
     * @return Lista com dados aleatórios
     */
    public List<Double> gerarDadosAleatorios(int n, double min, double max) {
        Random rand = new Random();
        List<Double> dados = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            dados.add(min + (max - min) * rand.nextDouble());
        }
        return dados;
    }
}

/**
 * Classe principal para executar a análise e visualização dos dados.
 */
public class Main {

    public static void main(String[] args) {
        Utils utils = new Utils();
        DataProcessor dp = new DataProcessor();
        Grafico grafico = new Grafico();
        Dashboard dashboard = new Dashboard();

        // Gerar dados com alguns valores fora do intervalo para teste (exemplo: 15 dados entre 0 e 15)
        List<Double> dados = utils.gerarDadosAleatorios(15, 0, 15);

        // Filtrar dados entre 2 e 10 para focar na faixa a analisar
        List<Double> filtrados = dp.filtrarDados(dados, 2, 10);

        // Mostrar resumo estatístico completo dos dados filtrados
        dashboard.exibirResumo(filtrados, dp);

        // Obter os dados normalizados para plotagem
        List<Double> dadosNormalizados = dp.normalizar(dp.removerOutliers(filtrados));

        // Exibir gráfico baseado nos dados processados
        grafico.plotar(dadosNormalizados, "Gráfico de Dados Filtrados e Normalizados");
    }
}
