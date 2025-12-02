import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementação completa com exemplos detalhados de quatro padrões de projeto clássicos:
 * Factory, Singleton, Observer e Decorator.
 * Cada padrão está encapsulado em sua própria seção com classes e interações.
 * Há um método main único que chama exemplos práticos para cada padrão, bem comentado.
 */
public class DesignPatternsDemo {

    // ============================================
    // FACTORY PATTERN
    // ============================================

    /**
     * Interface Produto, define contrato básico para produtos criados pela fábrica.
     */
    interface Produto {
        String getNome();
        double getPreco();
    }

    /**
     * Produto concreto 1: Mouse
     */
    static class Mouse implements Produto {
        private String nome;
        private double preco;

        public Mouse(String nome, double preco) {
            this.nome = nome;
            this.preco = preco;
        }

        public String getNome() { return nome; }
        public double getPreco() { return preco; }
    }

    /**
     * Produto concreto 2: Teclado
     */
    static class Teclado implements Produto {
        private String nome;
        private double preco;

        public Teclado(String nome, double preco) {
            this.nome = nome;
            this.preco = preco;
        }

        public String getNome() { return nome; }
        public double getPreco() { return preco; }
    }

    /**
     * Interface Factory, com método para criar produtos.
     */
    interface ProdutoFactory {
        Produto criarProduto(String tipo);
    }

    /**
     * Fábrica concreta capaz de criar diferentes produtos.
     * Demonstra Factory Method simplificado.
     */
    static class LojaEletronicosFactory implements ProdutoFactory {

        /**
         * Cria produto baseado no parâmetro tipo.
         * @param tipo Tipo de produto solicitado ("mouse" ou "teclado")
         * @return Instância de Produto correspondente
         */
        public Produto criarProduto(String tipo) {
            if ("mouse".equalsIgnoreCase(tipo)) {
                return new Mouse("Mouse Gamer", 150.0);
            } else if ("teclado".equalsIgnoreCase(tipo)) {
                return new Teclado("Teclado Mecânico", 350.0);
            }
            throw new IllegalArgumentException("Tipo desconhecido: " + tipo);
        }
    }

    /**
     * Exemplo demonstrando o uso do Factory Pattern.
     */
    public static void executarFactoryDemo() {
        System.out.println("=== DEMO: Factory Pattern ===");
        ProdutoFactory factory = new LojaEletronicosFactory();
        Produto produto1 = factory.criarProduto("mouse");
        Produto produto2 = factory.criarProduto("teclado");
        System.out.println("Produto criado: " + produto1.getNome() + " - Preço: R$" + produto1.getPreco());
        System.out.println("Produto criado: " + produto2.getNome() + " - Preço: R$" + produto2.getPreco());
    }

    // ============================================
    // SINGLETON PATTERN
    // ============================================

    /**
     * Singleton clássico com instância estática única.
     * Demonstra esquema seguro e "lazy initialization".
     */
    static class ConfiguracaoSingleton {

        private static ConfiguracaoSingleton instancia;

        private Properties propriedades;

        private ConfiguracaoSingleton() {
            propriedades = new Properties();
            propriedades.setProperty("modo", "produção");
            propriedades.setProperty("versao", "1.0");
        }

        /**
         * Retorna a instância única do singleton.
         * Garante a criação da instância apenas sob demanda.
         * @return Instância única
         */
        public static synchronized ConfiguracaoSingleton getInstancia() {
            if (instancia == null) {
                instancia = new ConfiguracaoSingleton();
            }
            return instancia;
        }

        /**
         * Consulta o valor de uma propriedade de configuração.
         * @param chave Nome da propriedade
         * @return Valor associado ou nulo se inexistente
         */
        public String getPropriedade(String chave) {
            return propriedades.getProperty(chave);
        }

        /**
         * Atualiza ou adiciona propriedade.
         * @param chave Nome da propriedade
         * @param valor Valor a definir
         */
        public void setPropriedade(String chave, String valor) {
            propriedades.setProperty(chave, valor);
        }
    }

    /**
     * Exemplo demonstrando o uso do Singleton Pattern.
     */
    public static void executarSingletonDemo() {
        System.out.println("\n=== DEMO: Singleton Pattern ===");
        ConfiguracaoSingleton conf1 = ConfiguracaoSingleton.getInstancia();
        System.out.println("Modo: " + conf1.getPropriedade("modo"));
        ConfiguracaoSingleton conf2 = ConfiguracaoSingleton.getInstancia();
        conf2.setPropriedade("modo", "desenvolvimento");
        System.out.println("Modo atualizado via outra referência: " + conf1.getPropriedade("modo"));
        System.out.println("As duas referências são iguais? " + (conf1 == conf2));
    }

    // ============================================
    // OBSERVER PATTERN
    // ============================================

    /**
     * Interface Observer, define contrato do objeto observador.
     */
    interface Observer {
        void atualizar(String mensagem);
    }

    /**
     * Interface Subject, define contrato do objeto observado.
     */
    interface Subject {
        void adicionarObserver(Observer o);
        void removerObserver(Observer o);
        void notificarObservers();
    }

    /**
     * Subject concreto que representa um produto cujo preço pode variar.
     */
    static class ProdutoObservado implements Subject {

        private List<Observer> observers = new CopyOnWriteArrayList<>();
        private String nome;
        private double preco;

        public ProdutoObservado(String nome, double preco) {
            this.nome = nome;
            this.preco = preco;
        }

        public void setPreco(double preco) {
            boolean mudou = Double.compare(this.preco, preco) != 0;
            this.preco = preco;
            if (mudou) {
                System.out.println("\nPreço do " + nome + " mudou para R$" + preco);
                notificarObservers();
            }
        }

        public double getPreco() {
            return preco;
        }

        @Override
        public void adicionarObserver(Observer o) {
            observers.add(o);
        }

        @Override
        public void removerObserver(Observer o) {
            observers.remove(o);
        }

        @Override
        public void notificarObservers() {
            for (Observer o : observers) {
                o.atualizar("O preço do produto " + nome + " mudou para R$" + preco);
            }
        }
    }

    /**
     * Observer concreto que age ao receber notificações do produto observado.
     */
    static class UsuarioConsumidor implements Observer {
        private String nome;

        public UsuarioConsumidor(String nome) {
            this.nome = nome;
        }

        @Override
        public void atualizar(String mensagem) {
            System.out.println("[" + nome + "] recebeu notificação: " + mensagem);
        }
    }

    /**
     * Exemplo demonstrando o uso do Observer Pattern.
     */
    public static void executarObserverDemo() {
        System.out.println("\n=== DEMO: Observer Pattern ===");
        ProdutoObservado produto = new ProdutoObservado("Smartphone XYZ", 2500.00);

        UsuarioConsumidor user1 = new UsuarioConsumidor("Ana");
        UsuarioConsumidor user2 = new UsuarioConsumidor("Carlos");
        produto.adicionarObserver(user1);
        produto.adicionarObserver(user2);

        // Mudança de preço notifica todos inscritos
        produto.setPreco(2300.00);
        produto.setPreco(2200.00);

        // Remove um observer
        produto.removerObserver(user1);
        produto.setPreco(2100.00);
    }

    // ============================================
    // DECORATOR PATTERN
    // ============================================

    /**
     * Interface base para produtos com descrição.
     */
    interface ProdutoDecoratorBase {
        String getDescricao();
        double getPreco();
    }

    /**
     * Produto simples, implementação base.
     */
    static class ProdutoSimples implements ProdutoDecoratorBase {
        private String descricao;
        private double preco;

        public ProdutoSimples(String descricao, double preco) {
            this.descricao = descricao;
            this.preco = preco;
        }

        @Override
        public String getDescricao() {
            return descricao;
        }

        @Override
        public double getPreco() {
            return preco;
        }
    }

    /**
     * Classe abstrata do Decorator, implementa a interface e tem referência para o componente.
     */
    static abstract class ProdutoDecorator implements ProdutoDecoratorBase {
        protected ProdutoDecoratorBase produtoBase;

        public ProdutoDecorator(ProdutoDecoratorBase produtoBase) {
            this.produtoBase = produtoBase;
        }

        @Override
        public String getDescricao() {
            return produtoBase.getDescricao();
        }

        @Override
        public double getPreco() {
            return produtoBase.getPreco();
        }
    }

    /**
     * Decorator que adiciona garantia estendida ao produto.
     */
    static class ProdutoComGarantia extends ProdutoDecorator {
        public ProdutoComGarantia(ProdutoDecoratorBase produtoBase) {
            super(produtoBase);
        }

        @Override
        public String getDescricao() {
            return super.getDescricao() + " + Garantia Estendida";
        }

        @Override
        public double getPreco() {
            // Custo extra fixo para garantia
            return super.getPreco() + 150.00;
        }
    }

    /**
     * Decorator que adiciona embalagem premium ao produto.
     */
    static class ProdutoComEmbalagemPremium extends ProdutoDecorator {
        public ProdutoComEmbalagemPremium(ProdutoDecoratorBase produtoBase) {
            super(produtoBase);
        }

        @Override
        public String getDescricao() {
            return super.getDescricao() + " + Embalagem Premium";
        }

        @Override
        public double getPreco() {
            // Custo extra fixo para embalagem
            return super.getPreco() + 30.00;
        }
    }

    /**
     * Exemplo demonstrando o uso do Decorator Pattern.
     */
    public static void executarDecoratorDemo() {
        System.out.println("\n=== DEMO: Decorator Pattern ===");
        ProdutoDecoratorBase produto = new ProdutoSimples("Notebook UltraSlim", 5500.00);
        System.out.println("Produto base: " + produto.getDescricao() + " - Preço: R$" + produto.getPreco());

        produto = new ProdutoComGarantia(produto);
        System.out.println("Após adicionar garantia: " + produto.getDescricao() + " - Preço: R$" + produto.getPreco());

        produto = new ProdutoComEmbalagemPremium(produto);
        System.out.println("Após adicionar embalagem premium: " + produto.getDescricao() + " - Preço: R$" + produto.getPreco());
    }

    // ============================================
    // MÉTODO MAIN, EXECUTA TODOS OS DEMAIS
    // ============================================

    public static void main(String[] args) {
        executarFactoryDemo();
        executarSingletonDemo();
        executarObserverDemo();
        executarDecoratorDemo();
    }
}
